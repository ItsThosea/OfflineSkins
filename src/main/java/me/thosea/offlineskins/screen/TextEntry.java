package me.thosea.offlineskins.screen;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;

public class TextEntry extends ElementListWidget.Entry<TextEntry> {
	private Text text;

	public TextEntry(Text text) {
		this.text = text;
	}

	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

	@Override
	public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();

		int i = y + entryHeight / 2;
		int j = x + entryWidth - 8;
		int k = client.textRenderer.getWidth(this.text);
		int l = (x + j - k) / 2;
		int m = i - 9 / 2;
		context.drawTextWithShadow(client.textRenderer, this.text, l, m, -6250336);
	}

	@Override
	public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
		return null;
	}

	@Override
	public List<? extends Selectable> selectableChildren() {
		return ImmutableList.of(new Selectable() {
			@Override
			public SelectionType getType() {
				return SelectionType.HOVERED;
			}

			@Override
			public void appendNarrations(NarrationMessageBuilder builder) {
				builder.put(NarrationPart.TITLE, text);
			}
		});
	}
	@Override
	public List<? extends Element> children() {
		return Collections.emptyList();
	}
}
