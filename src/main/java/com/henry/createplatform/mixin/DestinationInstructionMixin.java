package com.henry.createplatform.mixin;

import java.util.ArrayList;
import java.util.List;

import com.henry.createplatform.api.Platforms;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DiscoveredPath;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.simibubi.create.content.trains.schedule.destination.DestinationInstruction;
import com.simibubi.create.content.trains.station.GlobalStation;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 「目的地」指令：站台限制的匹配与显示。
 *
 * <p>原版逻辑是「按站名正则收集候选车站 → 找一条最短路」。这里只在玩家确实填了站台限制时接管整个
 * {@code start}：先按站名筛，再按站台号筛，其余（售票员判定、失败状态、冷却）逐字照抄 Create 6.0.11
 * 的实现，保证不填站台时走的是原版代码，老时刻表行为零变化。</p>
 */
@Mixin(DestinationInstruction.class)
public abstract class DestinationInstructionMixin {

    @Inject(method = "start(Lcom/simibubi/create/content/trains/schedule/ScheduleRuntime;Lnet/minecraft/world/level/Level;)Lcom/simibubi/create/content/trains/graph/DiscoveredPath;",
            at = @At("HEAD"), cancellable = true)
    private void createplatform$startWithPlatformFilter(ScheduleRuntime runtime, Level level,
                                                        CallbackInfoReturnable<DiscoveredPath> cir) {
        DestinationInstruction self = (DestinationInstruction) (Object) this;
        String rawFilter = self.getData() == null ? "" : self.getData().getString("Platform");
        List<Integer> platformFilter = Platforms.parseFilter(rawFilter);
        if (platformFilter.isEmpty())
            return; // 没填站台 → 完全交给 Create 原逻辑

        Train train = runtime.train;
        if (!train.hasForwardConductor() && !train.hasBackwardConductor()) {
            train.status.missingConductor();
            runtime.startCooldown();
            cir.setReturnValue(null);
            return;
        }

        String regex = self.getFilterForRegex();
        ArrayList<GlobalStation> candidates = new ArrayList<>();
        boolean nameMatched = false;
        for (GlobalStation station : train.graph.getPoints(EdgePointType.STATION)) {
            if (!station.name.matches(regex))
                continue;
            nameMatched = true;
            if (!Platforms.matchesAny(platformFilter, Platforms.of(station).createplatform$getPlatform()))
                continue;
            candidates.add(station);
        }

        DiscoveredPath best = train.navigation.findPathTo(candidates, Double.MAX_VALUE);
        if (best == null) {
            if (nameMatched)
                train.status.failedNavigationNoTarget(Platforms.describeLabel(self.getFilter(), platformFilter));
            else
                train.status.failedNavigationNoTarget(self.getFilter());
            runtime.startCooldown();
            cir.setReturnValue(null);
            return;
        }

        cir.setReturnValue(best);
    }

    /**
     * 时刻表条目卡片上的摘要文字：填了站台限制就在站名后面追加 {@code  #2}，让玩家一眼看出这条不是停在整站。
     */
    @ModifyArg(method = "getSummary()Lnet/createmod/catnip/data/Pair;",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"),
               index = 0)
    private String createplatform$summaryWithPlatform(String label) {
        DestinationInstruction self = (DestinationInstruction) (Object) this;
        String rawFilter = self.getData() == null ? "" : self.getData().getString("Platform");
        if (Platforms.parseFilter(rawFilter).isEmpty())
            return label;
        return label + Component.translatable("createplatform.schedule.summary_suffix", Platforms.describe(rawFilter))
            .getString();
    }
}
