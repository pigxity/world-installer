package com.pigxity.worldinstaller.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import java.io.File;

public final class FileList extends ObjectSelectionList<FileList.Entry> {
    public FileList(Minecraft client, int width, int height, int top, int itemHeight) {
        super(client, width, height, top, itemHeight);
    }

    public void add(File file) {
        addEntry(new Entry(file));
    }

    public void selectFirst() {
        if (!this.children().isEmpty()) {
            this.setSelected(this.children().getFirst());
        }
    }

    public final class Entry extends ObjectSelectionList.Entry<Entry> {
        private final File file;

        public Entry(File file) {
            this.file = file;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            graphics.text(minecraft.font, file.getName(), this.getContentX() + 2, this.getContentY() + 3, CommonColors.WHITE);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            return event.button() == 0;
        }

        @Override
        public Component getNarration() {
            return Component.literal(file.getName());
        }

        public File getFile() {
            return file;
        }
    }
}
