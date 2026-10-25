package com.henry.createplatform.mixin;

import com.henry.createplatform.api.StationPlatform;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.station.GlobalStation;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 给 Create 的 {@code GlobalStation} 加一个「站台号」字段。
 *
 * <p>站名存在 {@code GlobalStation.name} 而不是方块实体里（见 {@code StationBlockEntity#updateName}），
 * 所以站台号也挂在同一个对象上：跟着铁路存档一起持久化，并且随 {@code TrackGraphSync.pointAdded}
 * 一起同步给客户端（车站界面要用）。</p>
 *
 * <p>NBT 键用 {@code CreatePlatform}，避免和 Create 以后的字段撞名；读旧存档时缺键 → 空串（未设置）。</p>
 */
@Mixin(GlobalStation.class)
public abstract class GlobalStationMixin implements StationPlatform {

    @Unique
    private String createplatform$platform = "";

    @Override
    public String createplatform$getPlatform() {
        return createplatform$platform;
    }

    @Override
    public void createplatform$setPlatform(String platform) {
        createplatform$platform = platform == null ? "" : platform;
    }

    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;ZLcom/simibubi/create/content/trains/graph/DimensionPalette;)V",
            at = @At("TAIL"))
    private void createplatform$readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean migration,
                                        DimensionPalette dimensions, CallbackInfo ci) {
        createplatform$platform = tag.getString("CreatePlatform");
    }

    @Inject(method = "write(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Lcom/simibubi/create/content/trains/graph/DimensionPalette;)V",
            at = @At("TAIL"))
    private void createplatform$writeNbt(CompoundTag tag, HolderLookup.Provider registries,
                                         DimensionPalette dimensions, CallbackInfo ci) {
        if (!createplatform$platform.isEmpty()) {
            tag.putString("CreatePlatform", createplatform$platform);
        }
    }

    @Inject(method = "read(Lnet/minecraft/network/FriendlyByteBuf;Lcom/simibubi/create/content/trains/graph/DimensionPalette;)V",
            at = @At("TAIL"))
    private void createplatform$readSync(FriendlyByteBuf buffer, DimensionPalette dimensions, CallbackInfo ci) {
        createplatform$platform = buffer.readUtf();
    }

    @Inject(method = "write(Lnet/minecraft/network/FriendlyByteBuf;Lcom/simibubi/create/content/trains/graph/DimensionPalette;)V",
            at = @At("TAIL"))
    private void createplatform$writeSync(FriendlyByteBuf buffer, DimensionPalette dimensions, CallbackInfo ci) {
        buffer.writeUtf(createplatform$platform);
    }
}
