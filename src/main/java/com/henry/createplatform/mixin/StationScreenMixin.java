package com.henry.createplatform.mixin;

import com.henry.createplatform.client.PlatformEditBox;
import com.henry.createplatform.client.ScreenWidgetAccess;
import com.henry.createplatform.client.StationScreenAccess;
import com.henry.createplatform.client.StationUiHooks;
import com.henry.createplatform.net.SetStationPlatformPayload;
import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.content.trains.station.StationScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.network.PacketDistributor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 列车站界面：底栏加一个「站台」输入框，回车 / 关界面时把站台号发给服务端。
 *
 * <p>Create 自己处理站名是同一套节奏（界面上回车一次、{@code removed()} 再兜一次），这里照抄，
 * 服务端只负责归一化 + 重新同步。</p>
 */
@Mixin(StationScreen.class)
public abstract class StationScreenMixin {

    @Inject(method = "init()V", at = @At("TAIL"))
    private void createplatform$addField(CallbackInfo ci) {
        GlobalStation station = ((StationScreenAccess) (Object) this).createplatform$station();
        if (station == null)
            return;

        Screen screen = (Screen) (Object) this;
        PlatformEditBox box = StationUiHooks.createPlatformBox(screen.width, screen.height,
            StationUiHooks.platformOf(station));
        ((ScreenWidgetAccess) this).createplatform$addRenderableChild(box);
    }

    @Inject(method = "renderWindow", at = @At("TAIL"))
    private void createplatform$renderFieldLabel(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks,
                                                 CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        StationUiHooks.renderPlatformLabel(graphics, screen.width, screen.height);
    }

    /**
     * 站台框在标题行右端，而 Create 把整条标题行当作「点一下改站名」的热区，会把点击吞掉拿去聚焦站名框，
     * 所以这里先一步接管落在站台框上的点击。
     */
    @Inject(method = "mouseClicked(DDI)Z", at = @At("HEAD"), cancellable = true)
    private void createplatform$clickPlatformField(double mouseX, double mouseY, int button,
                                                   CallbackInfoReturnable<Boolean> cir) {
        Screen screen = (Screen) (Object) this;
        PlatformEditBox box = StationUiHooks.findPlatformBox(screen);
        if (box == null || !box.isMouseOver(mouseX, mouseY))
            return;

        box.mouseClicked(mouseX, mouseY, button);
        // Screen 的焦点 API 声明在 AbstractContainerEventHandler（Screen 的父类）上，公开可调。
        ((ContainerEventHandler) (Object) this).setFocused(box);
        cir.setReturnValue(true);
    }

    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void createplatform$confirmOnEnter(int keyCode, int scanCode, int modifiers,
                                               CallbackInfoReturnable<Boolean> cir) {
        if (!(((Screen) (Object) this).getFocused() instanceof PlatformEditBox box))
            return;
        if (keyCode != InputConstants.KEY_RETURN && keyCode != InputConstants.KEY_NUMPADENTER)
            return;
        box.setFocused(false);
        createplatform$send(box);
        cir.setReturnValue(true);
    }

    @Inject(method = "removed()V", at = @At("TAIL"))
    private void createplatform$sendOnClose(CallbackInfo ci) {
        PlatformEditBox box = StationUiHooks.findPlatformBox((Screen) (Object) this);
        if (box != null)
            createplatform$send(box);
    }

    @Unique
    private void createplatform$send(PlatformEditBox box) {
        StationBlockEntity blockEntity = ((StationScreenAccess) (Object) this).createplatform$blockEntity();
        if (blockEntity == null)
            return;
        PacketDistributor.sendToServer(new SetStationPlatformPayload(blockEntity.getBlockPos(), box.getValue()));
    }
}
