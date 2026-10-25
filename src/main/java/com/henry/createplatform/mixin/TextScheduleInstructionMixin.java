package com.henry.createplatform.mixin;

import java.util.function.BiConsumer;

import com.henry.createplatform.client.ScheduleUiHooks;
import com.simibubi.create.content.trains.schedule.destination.TextScheduleInstruction;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.gui.widget.TooltipArea;

import net.minecraft.client.gui.components.EditBox;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 时刻表「目的地」指令的编辑行：把唯一一个文本输入框拆成「站名筛选 + 站台限制」两个。
 *
 * <p>Create 的 {@code TextScheduleInstruction#initConfigurationWidgets} 里只有一处
 * {@code builder.addTextInput(...)}；这里拦截它，对「目的地」指令追加第二个输入框（数据键
 * {@code Platform}），其它子类（改名、取件）原样透传。</p>
 */
@Mixin(TextScheduleInstruction.class)
public abstract class TextScheduleInstructionMixin {

    @Redirect(
        method = "initConfigurationWidgets(Lcom/simibubi/create/foundation/gui/ModularGuiLineBuilder;)V",
        at = @At(value = "INVOKE",
                 target = "Lcom/simibubi/create/foundation/gui/ModularGuiLineBuilder;addTextInput(IILjava/util/function/BiConsumer;Ljava/lang/String;)Lcom/simibubi/create/foundation/gui/ModularGuiLineBuilder;"))
    private ModularGuiLineBuilder createplatform$splitInputs(ModularGuiLineBuilder builder, int x, int width,
                                                             BiConsumer<EditBox, TooltipArea> transform, String dataKey) {
        return ScheduleUiHooks.addDestinationInputs(builder, x, width, transform, dataKey, (Object) this);
    }
}
