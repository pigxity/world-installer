package com.pigxity.worldinstaller.mixin;

import com.pigxity.worldinstaller.WorldInstallerClient;
import com.pigxity.worldinstaller.file.FileUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin {
    @Shadow
    @Final
    private LinearLayout layout;

    @Inject(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/screens/worldselection/EditWorldScreen;BACKUP_BUTTON:Lnet/minecraft/network/chat/Component;",
                    shift = At.Shift.BEFORE
            )
    )
    private void worldinstaller$addExportButton(
            Minecraft client,
            LevelStorageSource.LevelStorageAccess storageSession,
            String worldName,
            BooleanConsumer callback,
            CallbackInfo info
    ) {
        this.layout.addChild(Button.builder(Component.literal("Export World"), button -> {
            button.active = false;

            Path worldDirectory = storageSession.getLevelPath(LevelResource.ROOT);
            Path zipFile = WorldInstallerClient.getConfig()
                    .resolveExportDirectory()
                    .toPath()
                    .resolve(storageSession.getLevelId() + ".zip");

            CompletableFuture.runAsync(() -> {
                try {
                    FileUtils.zipDirectory(worldDirectory, zipFile, Set.of("session.lock")); //skip session.lock
                } catch (java.io.IOException exception) {
                    throw new CompletionException(exception);
                }
            }).whenComplete((unused, exception) -> {
                if (exception != null) {
                    WorldInstallerClient.LOGGER.error("Failed to export world", exception);

                    client.execute(() -> {
                        showToast(client, "ERROR", "Failed to export world: " + exception.toString());
                        button.active = true;
                    });

                    return;
                }

                client.execute(() -> {
                    showToast(client, "World exported", "Exported world to " + zipFile.toAbsolutePath());
                    callback.accept(false); //close screen
                });
            });
        }).width(200).build());
    }

    private static void showToast(Minecraft client, String title, String description) {
        client.getToastManager().addToast(SystemToast.multiline(
                client,
                SystemToast.SystemToastId.WORLD_BACKUP,
                Component.literal(title),
                Component.literal(description)
        ));
    }
}
