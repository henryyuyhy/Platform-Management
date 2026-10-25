package com.henry.createplatform.mixin;

import java.util.List;

import com.henry.createplatform.client.ScreenWidgetAccess;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 让别的 mixin 能把控件挂进界面。
 *
 * <p>不转发 {@code addWidget}（它是泛型方法、还带交集类型边界，调用方难写），直接把
 * {@code Screen} 自己的 {@code children} / {@code renderables} 两个列表暴露出来，效果等价：
 * 进 children 接收输入，进 renderables 才会被画出来。</p>
 */
@Mixin(Screen.class)
public abstract class ScreenMixin implements ScreenWidgetAccess {

    @Shadow
    @Final
    private List<GuiEventListener> children;

    @Shadow
    @Final
    public List<Renderable> renderables;

    @Override
    public void createplatform$addRenderableChild(GuiEventListener child) {
        children.add(child);
        if (child instanceof Renderable renderable)
            renderables.add(renderable);
    }
}
