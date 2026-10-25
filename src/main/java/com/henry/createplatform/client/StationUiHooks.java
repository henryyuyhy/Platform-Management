package com.henry.createplatform.client;

import com.henry.createplatform.api.Platforms;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.station.StationBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 列车站界面（Create 的「schedule_2」底图 200×127）上的站台号输入框。
 *
 * <p>位置在标题行右端、紧挨车站名右边（GUI 右上角）：x+128 起是「站台」标签，x+152 起是输入框。
 * 原来放在底栏中间，会和别的模组往车站界面底栏加的控件打架，所以挪到标题行——那里除居中的站名和
 * 站名旁边的编辑铅笔图标外没有别的内容。</p>
 */
public final class StationUiHooks {

    public static final int GUI_WIDTH = 200;
    public static final int GUI_HEIGHT = 127;

    private static final int LABEL_X = 128;
    private static final int LABEL_Y = 4;
    private static final int FIELD_X = 152;
    private static final int FIELD_Y = 3;
    private static final int FIELD_W = 42;
    private static final int FIELD_H = 8;
    /** 标题行文字颜色与 Create 的站名文字一致（浅色底上的深棕红）。 */
    private static final int LABEL_COLOR = 0xFF592424;

    private StationUiHooks() {}

    public static PlatformEditBox createPlatformBox(int screenWidth, int screenHeight, String value) {
        int guiLeft = (screenWidth - GUI_WIDTH) / 2;
        int guiTop = (screenHeight - GUI_HEIGHT) / 2;
        Font font = Minecraft.getInstance().font;
        PlatformEditBox box = new PlatformEditBox(font, guiLeft + FIELD_X, guiTop + FIELD_Y, FIELD_W, FIELD_H,
            Component.empty());
        // 原版文本框外观：黑底 + 1px 边框（聚焦时边框变白），比自绘底色更像“可以输入的地方”。
        box.setBordered(true);
        box.setTextColor(0xFFE0E0E0);
        box.setMaxLength(9);
        box.setFilter(Platforms::isStationPlatformInput);
        // 1.21.1 里 setSuggestion 是「未聚焦就一直画」，占位符要用 setHint（空且未聚焦才画）。
        box.setHint(Component.translatable("createplatform.station.platform.none"));
        box.setValue(value == null ? "" : value);
        return box;
    }

    /** 输入框自己在 renderables 里画，这里只补左边的「站台」标签。 */
    public static void renderPlatformLabel(GuiGraphics graphics, int screenWidth, int screenHeight) {
        int x = (screenWidth - GUI_WIDTH) / 2 + LABEL_X;
        int y = (screenHeight - GUI_HEIGHT) / 2 + LABEL_Y;
        graphics.drawString(Minecraft.getInstance().font,
            Component.translatable("createplatform.station.platform").getString(), x, y, LABEL_COLOR, false);
    }

    public static PlatformEditBox findPlatformBox(Screen screen) {
        for (GuiEventListener listener : screen.children()) {
            if (listener instanceof PlatformEditBox box)
                return box;
        }
        return null;
    }

    /** 站台号与站名一样存在 {@code GlobalStation} 上（服务端权威，客户端读同步过来的副本）。 */
    public static String platformOf(GlobalStation station) {
        return station == null ? "" : Platforms.of(station).createplatform$getPlatform();
    }

    public static StationBlockEntity blockEntityOf(Object screen) {
        return ((StationScreenAccess) screen).createplatform$blockEntity();
    }
}
