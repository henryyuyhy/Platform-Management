package com.henry.createplatform.client;

import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.station.StationBlockEntity;

/**
 * 由 {@code AbstractStationScreenMixin} 织入的访问器：{@code StationScreen} 的 {@code station} /
 * {@code blockEntity} 字段声明在父类 {@code AbstractStationScreen} 上，需要在父类这一层取。
 */
public interface StationScreenAccess {

    GlobalStation createplatform$station();

    StationBlockEntity createplatform$blockEntity();
}
