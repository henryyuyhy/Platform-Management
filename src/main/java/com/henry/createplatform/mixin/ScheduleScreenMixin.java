package com.henry.createplatform.mixin;

import java.util.List;

import com.henry.createplatform.client.ScheduleUiHooks;
import com.simibubi.create.content.trains.schedule.DestinationSuggestions;
import com.simibubi.create.content.trains.schedule.IScheduleInput;
import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import com.simibubi.create.content.trains.schedule.destination.DestinationInstruction;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 时刻表编辑界面：站台框插进编辑行后，Create 会给「每个」EditBox 都建一次目的地补全，最后一个生效，
 * 于是站名补全跑到了站台框上。这里在 {@code updateEditorSubwidgets} 末尾把补全重新挂回站名框。
 *
 * <p>编辑行控件藏在 {@code EditorSubWidgets}（CompositeWidget）里，取法见
 * {@link ScheduleUiHooks#lineWidgets}——不能 shadow 那个字段，它的类型是私有嵌套类。</p>
 */
@Mixin(ScheduleScreen.class)
public abstract class ScheduleScreenMixin {

    @Shadow
    private DestinationSuggestions destinationSuggestions;

    @Invoker("getViableStations")
    protected abstract List<?> createplatform$viableStations(IScheduleInput field);

    @Inject(method = "updateEditorSubwidgets(Lcom/simibubi/create/content/trains/schedule/IScheduleInput;)V",
            at = @At("TAIL"))
    private void createplatform$rebindSuggestions(IScheduleInput field, CallbackInfo ci) {
        if (!(field instanceof DestinationInstruction))
            return;

        Screen screen = (Screen) (Object) this;
        List<? extends GuiEventListener> widgets = ScheduleUiHooks.lineWidgets(screen);
        EditBox stationBox = ScheduleUiHooks.nthEditBox(widgets, 0);
        EditBox platformBox = ScheduleUiHooks.nthEditBox(widgets, 1);

        // 站台框不该弹站名补全。
        if (platformBox != null)
            platformBox.setResponder(null);
        if (stationBox == null)
            return;

        // 站名框原本的 responder 是 ScheduleScreen#onDestinationEdited（读同一个字段），
        // 所以只要把字段指向「绑在站名框上」的新补全实例即可。
        destinationSuggestions = ScheduleUiHooks.createSuggestions(screen, stationBox,
            createplatform$viableStations(field));
    }
}
