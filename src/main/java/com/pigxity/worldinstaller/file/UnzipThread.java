package com.pigxity.worldinstaller.file;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;

import java.io.File;

import static com.pigxity.worldinstaller.file.FileUtils.unzipFile;

public class UnzipThread extends Thread {
    private final String fileZip;
    private final File destDir;
    private final MinecraftClient client;

    public UnzipThread(String fileZip, File destDir, MinecraftClient client) {
        this.fileZip = fileZip;
        this.destDir = destDir;
        this.client = client;
    }

    public void run() {
        try {
            unzipFile(this.fileZip, this.destDir);
            new File(this.fileZip).delete();
            this.client.execute(() -> client.setScreen(new SelectWorldScreen(new TitleScreen())));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
