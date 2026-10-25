package com.henry.createplatform.mixin;

import java.util.ArrayList;
import java.util.List;

import com.henry.createplatform.api.Platforms;
import com.simibubi.create.content.trains.schedule.destination.DestinationInstruction;
import com.simibubi.create.content.trains.schedule.destination.TextScheduleInstruction;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 时刻表条目悬停提示：填了站台限制就补一行「站台限制: 1,2」。
 */
@Mixin(TextScheduleInstruction.class)
public abstract class TextScheduleInstructionTooltipMixin {

    @Inject(method = "getTitleAs(Ljava/lang/String;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void createplatform$appendPlatformLine(String type, CallbackInfoReturnable<List<Component>> cir) {
        if (!((Object) this instanceof DestinationInstruction))
            return;

        String rawFilter = ((DestinationInstruction) (Object) this).getData().getString("Platform");
        if (Platforms.parseFilter(rawFilter).isEmpty())
            return;

        List<Component> lines = new ArrayList<>(cir.getReturnValue());
        lines.add(Platforms.platformTooltip(rawFilter).withStyle(ChatFormatting.GRAY));
        cir.setReturnValue(lines);
    }
}
