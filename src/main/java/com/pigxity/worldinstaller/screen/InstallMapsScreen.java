package com.pigxity.worldinstaller.screen;

import com.pigxity.worldinstaller.ModConfig;
import com.pigxity.worldinstaller.WorldInstallerClient;
import com.pigxity.worldinstaller.file.UnzipThread;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.pigxity.worldinstaller.file.FileUtils.*;

@Environment(EnvType.CLIENT)
public class InstallMapsScreen extends Screen {

    private static final int LIST_WIDTH = 250;
    private static final int LIST_HEIGHT = 120;
    private static final int LIST_ROW_HEIGHT = 20;
    private static final int ELEMENT_PADDING = 20;

    private final Screen parent;
    private final File savesDirectory;
    private final File installDirectory;

    private FileList fileList;
    private Button continueButton;
    private CompletableFuture<List<File>> filesFuture;

    public InstallMapsScreen(Screen parent, ModConfig config) {
        super(Component.literal("Select a zip file:"));
        this.parent = parent;

        this.savesDirectory = this.minecraft.getLevelSource().getBaseDir().toFile();
        this.installDirectory = config.resolveInstallDirectory();
    }

    private void errorScreen(String errorMessage) {
        WorldInstallerClient.LOGGER.warn(errorMessage);
        this.minecraft.setScreen(new ErrorScreen(errorMessage, this.parent));
    }
    private void errorScreen(Exception exception) {
        exception.printStackTrace();
        this.minecraft.setScreen(new ErrorScreen(Arrays.toString(exception.getStackTrace()), this.parent));
    }

    private int getListTop() {
        return (this.height - LIST_HEIGHT) / 2;
    }
    private int getListBottom() {
        return getListTop() + LIST_HEIGHT;
    }

    private FileList createFileList() {
        FileList list = new FileList(
                minecraft,
                LIST_WIDTH,
                LIST_HEIGHT,
                getListTop(),
                LIST_ROW_HEIGHT
        );
        list.setX(this.width / 2 - LIST_WIDTH / 2);
        return list;
    }

    private void onConfirm() {
        FileList.Entry selected = fileList.getSelected();
        if (selected == null) {
            return;
        }
        File selectedFile = selected.getFile();

        this.minecraft.setScreen(new LoadingScreen(this.parent));

        try {
            // nested world directory, extract directly into saves
            if (fileNotInRootDir(selectedFile, "level.dat")) {
                new UnzipThread(selectedFile.getPath(), savesDirectory, this.minecraft).start();
            } else {
                // top-level level.dat
                File dir = new File(savesDirectory, selectedFile.getName().replaceFirst("[.][^.]+$", ""));
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    throw new IOException("Failed to create directory " + dir);
                }
                new UnzipThread(selectedFile.getPath(), dir, this.minecraft).start();
            }
        } catch (Exception e) {
            errorScreen(e);
        }
    }

    @Override
    protected void init() {
        filesFuture = CompletableFuture.supplyAsync(()
                -> getWorldFiles(installDirectory));

        fileList = createFileList();
        this.addRenderableWidget(fileList);

        final int buttonY = getListBottom() + ELEMENT_PADDING;

        continueButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CONTINUE, button -> onConfirm())
                .bounds(this.width / 2 - 105, buttonY, 100, 20).build());
        continueButton.active = false;

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent))
                .bounds(this.width / 2 + 5, buttonY, 100, 20).build());
    }

    @Override
    public void tick() {
        super.tick();

        if (filesFuture != null && filesFuture.isDone()) {
            List<File> files = filesFuture.join();

            if (files.isEmpty()) {
                errorScreen("Cannot find a valid zip file in " + installDirectory.getAbsolutePath());
                filesFuture = null;
                return;
            }

            for (File file : files) {
                fileList.add(file);
            }

            fileList.selectFirst();
            continueButton.active = true;

            filesFuture = null;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(
                this.font,
                this.title,
                this.width / 2,
                getListTop() - ELEMENT_PADDING - this.font.lineHeight,
                CommonColors.WHITE
        );
    }

}
