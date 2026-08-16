package com.pigxity.worldinstaller.mixin;

import com.pigxity.worldinstaller.WorldInstallerClient;
import com.pigxity.worldinstaller.file.FileUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin {
    @Shadow
    @Final
    private DirectionalLayoutWidget layout;

    @Inject(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/screen/world/EditWorldScreen;BACKUP_TEXT:Lnet/minecraft/text/Text;",
                    shift = At.Shift.BEFORE
            )
    )
    private void worldinstaller$addExportButton(
            MinecraftClient client,
            LevelStorage.Session storageSession,
            String worldName,
            BooleanConsumer callback,
            CallbackInfo info
    ) {
        this.layout.add(ButtonWidget.builder(Text.literal("Export World"), button -> {
            button.active = false;

            Path worldDirectory = storageSession.getDirectory(WorldSavePath.ROOT);
            Path zipFile = WorldInstallerClient.getConfig()
                    .resolveExportDirectory()
                    .toPath()
                    .resolve(storageSession.getDirectoryName() + ".zip");

            CompletableFuture.runAsync(() -> {
                try {
                    FileUtils.zipDirectory(worldDirectory, zipFile);
                } catch (java.io.IOException exception) {
                    throw new CompletionException(exception);
                }
            }).whenComplete((unused, exception) -> {
                if (exception != null) {
                    WorldInstallerClient.LOGGER.error("Failed to export world", exception);
                }
                client.execute(() -> button.active = true);
            });
        }).width(200).build());
    }
}
