package me.thosea.specialskin.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import static me.thosea.specialskin.SkinSettings.PREFIX;

public class SimpleMessageScreen extends Screen {
	public final Text message;
	public final Text buttonText;
	public final Runnable action;

	private int messageX;
	private int messageY;

	public SimpleMessageScreen(Text message, Text buttonText, Runnable action) {
		super(Text.empty());
		this.message = message;
		this.action = action;
		this.buttonText = buttonText;
	}

	public SimpleMessageScreen(Text message, Text buttonText, Screen parent) {
		this(message, buttonText,
				() -> MinecraftClient.getInstance().setScreen(parent));
	}

	public SimpleMessageScreen(Text message, Screen parent) {
		this(message,
				Text.translatable(PREFIX + "back"),
				() -> MinecraftClient.getInstance().setScreen(parent));
	}

	@Override
	protected void init() {
		int halfWidth = client.textRenderer.getWidth(message) / 2;
		this.messageX = (width / 2) - (halfWidth);
		this.messageY = height / 2 - 20;

		this.addDrawableChild(ButtonWidget.builder(buttonText, (button) -> {
			action.run();
		}).dimensions(this.width / 2 - 75, messageY + 50, 150, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context);
		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(
				client.textRenderer, message,
				messageX, messageY,
				16777215);
	}

	public int getMessageX() {
		return messageX;
	}

	public int getMessageY() {
		return messageY;
	}

	@Override
	public void close() {
		action.run();
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

}
