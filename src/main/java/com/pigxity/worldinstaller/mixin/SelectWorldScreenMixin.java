package com.pigxity.worldinstaller.mixin;

import com.pigxity.worldinstaller.screen.InstallMapsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {

	private Screen parent;

	protected SelectWorldScreenMixin(Text title) {
		super(title);
	}

	private ButtonWidget installMapButton;

	@Inject(at = @At("RETURN"), method = "init")
	private void worldinstaller$addInstallButton(CallbackInfo info) {
		this.installMapButton = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.literal("Install World"), (button) -> {
			try {
				MinecraftClient.getInstance().setScreen(new InstallMapsScreen((SelectWorldScreen)(Screen)this) {
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).dimensions(
				7, 7, 100, 20
		).build());
	}

}