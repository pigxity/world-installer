package com.pigxity.worldinstaller.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LoadingScreen extends CenteredMessageScreen {

    protected LoadingScreen(Screen parent) {
        super(Component.literal("Installing world to saves directory..."), parent);
    }

}
