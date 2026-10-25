package com.henry.createplatform;

import com.henry.createplatform.net.SetStationPlatformPayload;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import org.slf4j.Logger;

@Mod(Createplatform.MODID)
public class Createplatform {

    public static final String MODID = "createplatform";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Createplatform(IEventBus modBus) {
        modBus.addListener((RegisterPayloadHandlersEvent event) -> SetStationPlatformPayload.register(event));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
