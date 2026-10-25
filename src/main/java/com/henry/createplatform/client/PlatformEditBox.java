package com.henry.createplatform.client;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * 站台号输入框的标记类型。
 *
 * <p>车站界面里本来就有站名、列车名两个 EditBox，用类型区分可以把「我们加的那个」从
 * {@code Screen#children()} 里认出来，不必持有字段引用。</p>
 */
public class PlatformEditBox extends EditBox {

    public PlatformEditBox(net.minecraft.client.gui.Font font, int x, int y, int width, int height,
                           Component message) {
        super(font, x, y, width, height, message);
    }
}
