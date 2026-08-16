package com.pigxity.worldinstaller;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UncheckedIOException;

public class WorldInstallerClient implements ClientModInitializer {
    public static final String MOD_ID = "worldinstaller";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ModConfig config;

    @Override
    public void onInitializeClient() {
        try {
            config = ModConfig.load(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json"));
        } catch (java.io.IOException e) {
            throw new UncheckedIOException("Failed to load config", e);
        }
    }

    public static ModConfig getConfig() {
        return config;
    }
}
