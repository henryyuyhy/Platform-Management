package com.henry.createplatform.net;

import com.henry.createplatform.Createplatform;
import com.henry.createplatform.api.Platforms;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.graph.TrackGraphLocation;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.station.StationBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 客户端 → 服务端：设置某个列车站的站台号。
 *
 * <p>服务端处理方式与 Create 自己的 {@code StationEditPacket} 一致：改 {@code GlobalStation} 字段后
 * 调 {@code Create.RAILWAYS.sync.pointAdded(...)} 把该站点重新同步给所有客户端，再标记存档脏
 * （车站数据保存在铁路存档里，不是方块实体 NBT）。</p>
 */
public record SetStationPlatformPayload(BlockPos pos, String platform) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SetStationPlatformPayload> TYPE =
        new CustomPacketPayload.Type<>(Createplatform.id("set_station_platform"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetStationPlatformPayload> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetStationPlatformPayload::pos,
            ByteBufCodecs.STRING_UTF8, SetStationPlatformPayload::platform,
            SetStationPlatformPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(TYPE, STREAM_CODEC, SetStationPlatformPayload::handle);
    }

    private static void handle(SetStationPlatformPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> apply(payload, context));
    }

    private static void apply(SetStationPlatformPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player))
            return;

        BlockPos pos = payload.pos();
        // 只接受离自己足够近的车站界面发来的修改，避免任意改别人车站。
        if (player.blockPosition().distSqr(pos) > 64 * 64)
            return;

        ServerLevel level = player.serverLevel();
        if (!level.isLoaded(pos))
            return;
        if (!(level.getBlockEntity(pos) instanceof StationBlockEntity stationBe))
            return;

        GlobalStation station = stationBe.getStation();
        TrackGraphLocation location = stationBe.edgePoint.determineGraphLocation();
        if (station == null || location == null)
            return;

        Platforms.of(station).createplatform$setPlatform(Platforms.normalizeStation(payload.platform()));
        Create.RAILWAYS.sync.pointAdded(location.graph, station);
        Create.RAILWAYS.markTracksDirty();
    }
}
