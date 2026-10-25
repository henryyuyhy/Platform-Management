package com.henry.createplatform.mixin;

import com.henry.createplatform.client.StationScreenAccess;
import com.simibubi.create.content.trains.station.AbstractStationScreen;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.station.StationBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 把 {@code AbstractStationScreen} 的 {@code station} / {@code blockEntity} 暴露成接口方法。
 *
 * <p>字段声明在父类上，直接在 {@code StationScreen} 的 mixin 里 {@code @Shadow} 继承字段不稳妥，
 * 所以在声明它的这一层织入访问器。</p>
 */
@Mixin(AbstractStationScreen.class)
public abstract class AbstractStationScreenMixin implements StationScreenAccess {

    @Shadow
    protected GlobalStation station;

    @Shadow
    protected StationBlockEntity blockEntity;

    @Override
    public GlobalStation createplatform$station() {
        return station;
    }

    @Override
    public StationBlockEntity createplatform$blockEntity() {
        return blockEntity;
    }
}
