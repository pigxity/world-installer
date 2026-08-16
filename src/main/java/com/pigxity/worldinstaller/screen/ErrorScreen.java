package com.pigxity.worldinstaller.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ErrorScreen extends CenteredMessageScreen {

    protected ErrorScreen(String title, Screen parent) {
        super(Component.literal("ERROR: " + title), parent);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 50, this.height / 2 + 40, 100, 20).build());
    }

}
