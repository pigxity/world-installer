package com.piggygaming.ezmapdl.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;

import java.io.File;

public class FileList extends AlwaysSelectedEntryListWidget<FileList.Entry> {
    public FileList(MinecraftClient client, int width, int height, int top, int itemHeight) {
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

    public class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
        private final File file;

        public Entry(File file) {
            this.file = file;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
            context.drawTextWithShadow(client.textRenderer, file.getName(), x + 4, y + 5, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                FileList.this.setSelected(this);
                return true;
            }

            return false;
        }

        @Override
        public Text getNarration() {
            return Text.literal(file.getName());
        }

        public File getFile() {
            return file;
        }
    }
}
