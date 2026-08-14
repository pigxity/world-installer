package com.piggygaming.ezmapdl.screen;

import com.piggygaming.ezmapdl.EasyMapDownload;
import com.piggygaming.ezmapdl.file.UnzipThread;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.piggygaming.ezmapdl.file.FileUtils.*;

@Environment(EnvType.CLIENT)
public class InstallMapsScreen extends Screen {

    private final Screen parent;
    private final MinecraftClient client;
    private final File savesDirectory;

    private FileList fileList;
    private CompletableFuture<List<File>> filesFuture;

    public InstallMapsScreen(Screen parent) throws IOException {
        super(Text.literal("Select a zip file:"));
        this.parent = parent;
        this.client = MinecraftClient.getInstance();
        this.savesDirectory = new File(this.client.runDirectory.getPath() + File.separator + "saves");
    }

    private void errorScreen(String errorMessage) {
        EasyMapDownload.LOGGER.warn(errorMessage);
        this.client.setScreen(new ErrorScreen(errorMessage, this.parent));
    }
    private void errorScreen(Exception exception) {
        exception.printStackTrace();
        this.client.setScreen(new ErrorScreen(Arrays.toString(exception.getStackTrace()), this.parent));
    }

    private FileList fileListFromFiles(List<File> files) {
        FileList fileList = new FileList(client, 200, 300, this.height / 2, this.width / 2);

        for (File file : files) {
            fileList.add(file);
        }

        fileList.selectFirst();
        return fileList;
    }

    private void onConfirm() {
        assert fileList.getSelectedOrNull() != null;
        File selectedFile = fileList.getSelectedOrNull().getFile();

        File newFile = new File(savesDirectory.getPath() + File.separator + selectedFile.getName());
        this.client.setScreen(new LoadingScreen(this.parent));

        if (selectedFile.renameTo(newFile)) {
            try {
                //nested world directory, extract directly into saves
                if (fileNotInRootDir(newFile, "level.dat")) {
                    UnzipThread thread = new UnzipThread(newFile.getPath(), savesDirectory, this.client);
                    thread.start();

                //top-level level.dat
                } else {
                    File dir = new File(savesDirectory.getPath() + File.separator + newFile.getName().replaceFirst("[.][^.]+$", ""));
                    dir.mkdirs();

                    UnzipThread thread = new UnzipThread(newFile.getPath(), dir, this.client);
                    thread.start();
                }
            } catch (Exception e) {
                errorScreen(e);
            }
        }
    }

    @Override
    protected void init() {
        filesFuture = CompletableFuture.supplyAsync(()
                -> getWorldFiles(System.getProperty("user.home") + File.separator + "Downloads"));

        fileList = new FileList(client, 200, 300, this.height / 2, this.width / 2);
        this.addDrawableChild(fileList);

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CONTINUE, (b) -> onConfirm())
                .dimensions(this.width / 2 - 105, this.height /2 + 40, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (b) -> this.client.setScreen(this.parent))
                .dimensions(this.width / 2 + 5, this.height /2 + 40, 100, 20).build());
    }

    @Override
    public void tick() {
        super.tick();

        if (filesFuture != null && filesFuture.isDone()) {
            List<File> files = filesFuture.join();

            if (files.isEmpty()) {
                errorScreen("Cannot find a valid zip file");
            }

            for (File file : files) {
                fileList.add(file);
            }

            fileList.selectFirst();

            filesFuture = null;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 100, 16777215);
    }

}
