package com.henry.createplatform.client;

import net.minecraft.client.gui.components.events.GuiEventListener;

/**
 * 把 {@code Screen#addWidget} 这类 protected 方法暴露出来，供别的 mixin 调用。
 *
 * <p>（{@code @Shadow} 只对「目标类自己声明的成员」生效，{@code StationScreen} 的 mixin 不能直接
 * shadow 它从 {@code Screen} 继承来的方法，所以在这一层做转发。）</p>
 */
public interface ScreenWidgetAccess {

    void createplatform$addRenderableChild(GuiEventListener child);
}
