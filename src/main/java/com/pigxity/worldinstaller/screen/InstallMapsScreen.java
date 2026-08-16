package com.pigxity.worldinstaller.screen;

import com.pigxity.worldinstaller.ModConfig;
import com.pigxity.worldinstaller.WorldInstallerClient;
import com.pigxity.worldinstaller.file.UnzipThread;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

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
    private final MinecraftClient client;
    private final File savesDirectory;
    private final File installDirectory;

    private FileList fileList;
    private ButtonWidget continueButton;
    private CompletableFuture<List<File>> filesFuture;

    public InstallMapsScreen(Screen parent, ModConfig config) {
        super(Text.literal("Select a zip file:"));
        this.parent = parent;
        this.client = MinecraftClient.getInstance();

        this.savesDirectory = new File(this.client.runDirectory.getPath() + File.separator + "saves");
        this.installDirectory = config.resolveInstallDirectory();
    }

    private void errorScreen(String errorMessage) {
        WorldInstallerClient.LOGGER.warn(errorMessage);
        this.client.setScreen(new ErrorScreen(errorMessage, this.parent));
    }
    private void errorScreen(Exception exception) {
        exception.printStackTrace();
        this.client.setScreen(new ErrorScreen(Arrays.toString(exception.getStackTrace()), this.parent));
    }

    private int getListTop() {
        return (this.height - LIST_HEIGHT) / 2;
    }
    private int getListBottom() {
        return getListTop() + LIST_HEIGHT;
    }

    private FileList createFileList() {
        FileList list = new FileList(
                client,
                LIST_WIDTH,
                LIST_HEIGHT,
                getListTop(),
                LIST_ROW_HEIGHT
        );
        list.setX(this.width / 2 - LIST_WIDTH / 2);
        return list;
    }

    private void onConfirm() {
        FileList.Entry selected = fileList.getSelectedOrNull();
        if (selected == null) {
            return;
        }
        File selectedFile = selected.getFile();

        this.client.setScreen(new LoadingScreen(this.parent));

        try {
            // nested world directory, extract directly into saves
            if (fileNotInRootDir(selectedFile, "level.dat")) {
                new UnzipThread(selectedFile.getPath(), savesDirectory, this.client).start();
            } else {
                // top-level level.dat
                File dir = new File(savesDirectory, selectedFile.getName().replaceFirst("[.][^.]+$", ""));
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    throw new IOException("Failed to create directory " + dir);
                }
                new UnzipThread(selectedFile.getPath(), dir, this.client).start();
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
        this.addDrawableChild(fileList);

        final int buttonY = getListBottom() + ELEMENT_PADDING;

        continueButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CONTINUE, (b) -> onConfirm())
                .dimensions(this.width / 2 - 105, buttonY, 100, 20).build());
        continueButton.active = false;

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (b) -> this.client.setScreen(this.parent))
                .dimensions(this.width / 2 + 5, buttonY, 100, 20).build());
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                getListTop() - ELEMENT_PADDING - this.textRenderer.fontHeight,
                16777215
        );
    }

}
