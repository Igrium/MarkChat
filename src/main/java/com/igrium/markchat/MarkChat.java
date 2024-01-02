package com.igrium.markchat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.igrium.markchat.config.MarkChatConfig;

public class MarkChat implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("markchat");

    private static MarkChat instance;

    public static MarkChat getInstance() {
        return instance;
    }

    private MarkChatConfig config;

    public MarkChatConfig getConfig() {
        return config;
    }

    @Override
    public void onInitialize() {
        instance = this;
        initConfig();
    }

    private void initConfig() {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve("markchat.json");

        if (Files.isRegularFile(configFile)) {
            try (BufferedReader reader = Files.newBufferedReader(configFile)) {
                config = MarkChatConfig.fromJson(reader);
            } catch (Exception e) {
                LOGGER.error("Error loading MarkChat config.", e);
            }
        }

        if (config == null) config = new MarkChatConfig();

        // Re-save to add any missing values.
        try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
            writer.write(config.toJson());
        } catch (Exception e) {
            LOGGER.error("Error saving MarkChat config.", e);
        }
    }
}