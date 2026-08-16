package com.pigxity.worldinstaller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public record ModConfig(String installDirectory, String exportDirectory) {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ModConfig DEFAULT = new ModConfig("~/Downloads", "~/Downloads");

    public static ModConfig load(Path configFile) throws IOException {
        if (Files.notExists(configFile)) {
            Files.createDirectories(configFile.getParent());
            try (Writer writer = Files.newBufferedWriter(configFile)) {
                GSON.toJson(DEFAULT, writer);
            }
            return DEFAULT;
        }

        try (Reader reader = Files.newBufferedReader(configFile)) {
            return GSON.fromJson(reader, ModConfig.class);
        }
    }

    public File resolveInstallDirectory() {
        return resolveDirectory(installDirectory);
    }

    public File resolveExportDirectory() {
        return resolveDirectory(exportDirectory);
    }

    private static File resolveDirectory(String directory) {
        if (directory.equals("~")) {
            return new File(System.getProperty("user.home"));
        }
        if (directory.startsWith("~/") || directory.startsWith("~\\")) {
            return new File(System.getProperty("user.home"), directory.substring(2));
        }
        return new File(directory);
    }
}
