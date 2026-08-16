package com.pigxity.worldinstaller.mixin;

import com.pigxity.worldinstaller.WorldInstallerClient;
import com.pigxity.worldinstaller.screen.InstallMapsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {

	protected SelectWorldScreenMixin(Component title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "init")
	private void worldinstaller$addInstallButton(CallbackInfo info) {
		this.addRenderableWidget(Button.builder(Component.literal("Install World"), button -> {
			Minecraft.getInstance().setScreen(new InstallMapsScreen(
					(SelectWorldScreen)(Object)this,
					WorldInstallerClient.getConfig()
			));
		}).bounds(
				7, 7, 100, 20
		).build());
	}

}
