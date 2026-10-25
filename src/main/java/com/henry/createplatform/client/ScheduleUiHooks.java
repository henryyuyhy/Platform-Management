package com.henry.createplatform.client;

import java.util.List;
import java.util.function.BiConsumer;

import com.henry.createplatform.api.Platforms;
import com.simibubi.create.content.trains.schedule.destination.DestinationInstruction;
import com.simibubi.create.content.trains.schedule.DestinationSuggestions;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.gui.widget.CompositeWidget;
import com.simibubi.create.foundation.gui.widget.TooltipArea;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

/**
 * 时刻表编辑界面的纯逻辑（不放 mixin 里，避免 mixin 类里出现 lambda/内部类引用自身字段）。
 *
 * <p>Create 的「目的地」指令编辑行本来只有一个站名输入框。这里把站名框收窄一点，在后面补一个
 * 「站台」输入框（数据键 {@code Platform}，由 {@code ModularGuiLine} 自动读写指令的 NBT data）。</p>
 */
public final class ScheduleUiHooks {

    /** 站名输入框宽度（Create 原版是 121）。 */
    public static final int STATION_INPUT_WIDTH = 84;
    /** 站台输入框相对整行的起始偏移。 */
    public static final int PLATFORM_INPUT_X = 88;
    public static final int PLATFORM_INPUT_WIDTH = 54;
    /** AllGuiTextures.SCHEDULE 的高度，用于还原 Create 补全浮层的 Y 偏移。 */
    private static final int SCHEDULE_WINDOW_HEIGHT = 226;

    private ScheduleUiHooks() {}

    /**
     * 织入 {@code TextScheduleInstruction#initConfigurationWidgets} 的 Redirect 实现：只对「目的地」指令
     * 拆成两个输入框，其它 TextScheduleInstruction 子类（改名、取件）保持原样。
     */
    public static ModularGuiLineBuilder addDestinationInputs(ModularGuiLineBuilder builder, int x, int width,
                                                             BiConsumer<EditBox, TooltipArea> transform, String dataKey,
                                                             Object self) {
        if (!(self instanceof DestinationInstruction))
            return builder.addTextInput(x, width, transform, dataKey);

        builder.addTextInput(x, STATION_INPUT_WIDTH, transform, dataKey);
        builder.addTextInput(x + PLATFORM_INPUT_X, PLATFORM_INPUT_WIDTH, ScheduleUiHooks::configurePlatformInput,
            "Platform");
        return builder;
    }

    private static void configurePlatformInput(EditBox box, TooltipArea tooltip) {
        box.setMaxLength(24);
        box.setFilter(Platforms::isPlatformFilterInput);
        tooltip.withTooltip(List.of(
            Component.translatable("createplatform.schedule.platform.tip"),
            Component.translatable("createplatform.schedule.platform.tip1").withStyle(ChatFormatting.GRAY),
            Component.translatable("createplatform.schedule.platform.tip2").withStyle(ChatFormatting.DARK_GRAY)));
    }

    /** 按添加顺序取第 {@code index} 个 EditBox（0 = 站名框，1 = 站台框）。 */
    public static EditBox nthEditBox(List<? extends GuiEventListener> children, int index) {
        int seen = 0;
        for (GuiEventListener listener : children) {
            if (!(listener instanceof EditBox box))
                continue;
            if (seen == index)
                return box;
            seen++;
        }
        return null;
    }

    /**
     * 取时刻表编辑行里的控件。
     *
     * <p>这些控件不在 {@code ScheduleScreen#children()} 里，而是被塞进 Create 的
     * {@code EditorSubWidgets}（{@code ScreenOverlay} → {@code CompositeWidget}）自己的 children。
     * 那个字段是私有嵌套类型，mixin 里没法 shadow（字段匹配要求类型完全一致），但
     * {@code CompositeWidget#children()} 是 public，从界面的 children 里认出这个 overlay 即可。</p>
     */
    public static List<? extends GuiEventListener> lineWidgets(Screen screen) {
        for (GuiEventListener listener : screen.children()) {
            if (!(listener instanceof CompositeWidget composite))
                continue;
            for (GuiEventListener child : composite.children()) {
                if (child instanceof EditBox)
                    return composite.children();
            }
        }
        return List.of();
    }

    /**
     * 重新把目的地补全挂到「站名框」上。
     *
     * <p>Create 的 {@code ScheduleScreen#updateEditorSubwidgets} 会给编辑行里<b>每一个</b> EditBox 都建一次
     * {@code DestinationSuggestions}，最后一个生效——多出站台框后补全就跑到站台框去了。这里在方法末尾按
     * 当前站名框重建一次补全，并把站台框的 responder 清掉（否则在站台框里打字会弹站名补全）。</p>
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static DestinationSuggestions createSuggestions(Screen screen, EditBox stationBox, List viableStations) {
        Minecraft minecraft = Minecraft.getInstance();
        // 与 Create 原实现一致：补全浮层出现在 GUI 顶边 + 33；ScheduleScreen 的窗口是
        // AllGuiTextures.SCHEDULE(256x226)，由 AbstractContainerScreen 居中，顶边 = (height - 226) / 2。
        int topPos = (screen.height - SCHEDULE_WINDOW_HEIGHT) / 2;
        DestinationSuggestions suggestions =
            new DestinationSuggestions(minecraft, screen, stationBox, minecraft.font, viableStations, false, topPos + 33);
        suggestions.setAllowSuggestions(true);
        suggestions.updateCommandInfo();
        return suggestions;
    }


}
