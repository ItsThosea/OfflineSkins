package me.thosea.offlineskins.screen;

import com.google.common.collect.ImmutableList;
import me.thosea.offlineskins.SkinSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static me.thosea.offlineskins.SkinSettings.PREFIX;

public final class SkinPackScreen extends Screen {
	private final Screen parent;
	private OptionListWidget list;

	private String name = "";

	private final FileHolder defaultSkinFile = new FileHolder("skin.png");
	private final FileHolder defaultCapeFile = new FileHolder("cape.png");

	private final FileHolder selfSkinFile = new FileHolder("skin-self.png");
	private final FileHolder selfCapeFile = new FileHolder("cape-self.png");

	private final FileHolder otherSkinFile = new FileHolder("skin-others.png");
	private final FileHolder otherCapeFile = new FileHolder("cape-others.png");

	private ButtonWidget finishButton;

	private static final class FileHolder {
		private final String id;
		private File file;

		FileHolder(String id) {
			this.id = id;
		}
	}

	public SkinPackScreen(Screen parent) {
		super(Text.translatable(PREFIX + "skinPacks"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		this.list = new OptionListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);

		List<Object> elements = cast(list.children());
		elements.add(new TextEntry(Text.translatable(PREFIX
				+ "skinPacks.explanation")));

		TextFieldEntry textField = new TextFieldEntry();
		elements.add(textField);

		list.addOptionEntry(
				makeOption("defaultSkin", defaultSkinFile),
				makeOption("defaultCape", defaultCapeFile));
		list.addOptionEntry(
				makeOption("selfSkin", selfSkinFile),
				makeOption("selfCape", selfCapeFile));
		list.addOptionEntry(
				makeOption("otherSkin", otherSkinFile),
				makeOption("otherCape", otherCapeFile));

		addSelectableChild(list);

		this.addDrawableChild(ButtonWidget.builder(
				Text.translatable(PREFIX + "skinPacks.discard"), (button) -> {
					this.close();
				}).dimensions(this.width / 2 - 125, this.height - 27, 120, 20).build());
		this.addDrawableChild(finishButton = ButtonWidget.builder(
				Text.translatable(PREFIX + "skinPacks.finish"), (button) -> {
					checkValid();

					if(button.active) {
						makePack();
					}
				}).dimensions(this.width / 2 + 5, this.height - 27, 120, 20).build());

		textField.setText(name);
	}

	private void makePack() {
		File packDir = client.getResourcePackDir().toFile();
		File packZip = new File(packDir, name + ".zip");

		if(packZip.exists()) {
			setErrorScreen("errorExists");
			return;
		}

		try {
			if(!packZip.createNewFile()) {
				setErrorScreen("errorMake");
				return;
			}
		} catch(Exception e) {
			setErrorScreen("errorMake");
			return;
		}

		try(var output = new ZipOutputStream(new FileOutputStream(packZip))) {
			String desc = Formatting.DARK_AQUA + "Skin pack for OfflineSkins\\n";
			desc += Formatting.GOLD + "Name: " + Formatting.RED + Formatting.ITALIC;
			desc += StringUtils.abbreviate(name.replaceAll("[\"\\\\]", ""), 18);

			byte[] bytes = ("{\n" +
					"  \"pack\": {\n" +
					"    \"pack_format\": " + SharedConstants.RESOURCE_PACK_VERSION + ",\n" +
					"    \"description\": \"" + desc + "\"\n" +
					"  }\n" +
					"}")
					.getBytes(StandardCharsets.UTF_8);

			ZipEntry mcMeta = new ZipEntry("pack.mcmeta");
			mcMeta.setComment("OfflineSkins Skin Pack");
			mcMeta.setTime(System.currentTimeMillis());
			mcMeta.setSize(bytes.length);
			output.putNextEntry(mcMeta);
			output.write(bytes, 0, bytes.length);

			Path iconPath = FabricLoader
					.getInstance()
					.getModContainer("offlineskins")
					.orElseThrow()
					.findPath("assets/offlineskins/icon.png")
					.orElseThrow();

			try(var input = Files.newInputStream(iconPath)) {
				bytes = input.readAllBytes();
			}

			ZipEntry icon = new ZipEntry("pack.png");
			icon.setSize(bytes.length);
			icon.setTime(System.currentTimeMillis());
			output.putNextEntry(icon);
			output.write(bytes);

			List<FileHolder> toProcess = List.of(
					defaultSkinFile, defaultCapeFile,
					selfSkinFile, selfCapeFile,
					otherSkinFile, otherCapeFile);

			for(FileHolder fileHolder : toProcess) {
				if(fileHolder.file == null) continue;
				if(!fileHolder.file.exists()) continue;

				ZipEntry entry = new ZipEntry("assets/offlineskins/" + fileHolder.id);

				try(var input = new FileInputStream(fileHolder.file)) {
					bytes = input.readAllBytes();
				}

				entry.setSize(bytes.length);
				entry.setTime(System.currentTimeMillis());
				output.putNextEntry(entry);
				output.write(bytes, 0, bytes.length);
			}
		} catch(IOException e) {
			setErrorScreen("errorCopy");
			return;
		}

		client.setScreen(new SimpleMessageScreen(
				Text.translatable(PREFIX + "skinPacks.created",
						Text.literal(name).formatted(Formatting.GRAY, Formatting.ITALIC)),
				Text.translatable(PREFIX + "skinPacks.created.back"),
				makePackScreen()));
	}

	private PackScreen makePackScreen() {
		return new PackScreen(
				client.getResourcePackManager(),
				manager -> {
					client.options.refreshResourcePacks(manager);
					client.setScreen(parent);
				},
				client.getResourcePackDir(),
				Text.translatable("resourcePack.title"));
	}

	private void setErrorScreen(String error) {
		client.setScreen(new SimpleMessageScreen(
				Text.translatable(PREFIX + "skinPacks.finish." + error),
				this
		));
	}

	private SimpleOption<?> makeOption(String key, FileHolder holder) {
		return SkinSettings.makeButtonOption(PREFIX + "skinPacks." + key,
				() -> {
					client.setScreen(new SetFileScreen(
							this,
							Text.translatable(PREFIX + "skinPacks." + key + ".set"),
							Text.translatable("offlineskins.settings.skinPacks.setFile"),
							holder.file,
							true,
							this::isValidFile,
							file -> {
								Text fileText = Text.literal(file.getName())
										.formatted(Formatting.GRAY, Formatting.ITALIC);

								String textKey = PREFIX + "skinPacks.invalidFile";
								Text invalidFile = Text.translatable(textKey, fileText);

								MinecraftClient client = MinecraftClient.getInstance();
								client.setScreen(new SimpleMessageScreen(invalidFile, client.currentScreen));
							},
							file -> {
								holder.file = file;
								checkValid();
							}
					));
				});
	}

	private boolean isValidFile(File file) {
		try(FileInputStream stream = new FileInputStream(file)) {
			NativeImage image = NativeImage.read(stream);

			int width = image.getWidth();
			int height = image.getHeight();
			return width == 64 && (height == 32 || height == 64);
		} catch(Exception e) {
			return false;
		}
	}

	private <T> T cast(Object obj) {
		return (T) obj;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		list.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, title, this.width / 2, 20, 16777215);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackgroundTexture(context);
	}

	@Override
	public void close() {
		this.client.setScreen(this.parent);
	}

	private class TextFieldEntry extends ElementListWidget.Entry<TextFieldEntry> {
		private final TextEntry descText = new TextEntry(
				Text.translatable("offlineskins.settings.skinPacks.name")
		);
		private final TextEntry enterText = new TextEntry(
				Text.translatable("offlineskins.settings.skinPacks.name.enter")
		);
		private final TextFieldWidget delegate = new TextFieldWidget(
				client.textRenderer,
				width / 2 - 8, 0, // x/y
				150, 20, // width/height
				Text.empty()
		);

		public TextFieldEntry() {
			delegate.setChangedListener(newName -> {
				name = newName;
				checkValid();
			});
		}

		public void setText(String text) {
			delegate.setText(text);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return ImmutableList.of(new Selectable() {
				@Override
				public Selectable.SelectionType getType() {
					return delegate.getType();
				}

				@Override
				public void appendNarrations(NarrationMessageBuilder builder) {
					builder.put(NarrationPart.TITLE, name);
				}
			});
		}

		@Override
		public List<? extends Element> children() {
			return Collections.emptyList();
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if(delegate.mouseClicked(mouseX, mouseY, button)) {
				delegate.setFocused(true);
				return true;
			} else {
				delegate.setFocused(false);
				return false;
			}
		}

		@Override
		public void setFocused(boolean focused) {
			delegate.setFocused(focused);
		}

		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			return delegate.keyPressed(keyCode, scanCode, modifiers);
		}

		@Override
		public boolean charTyped(char chr, int modifiers) {
			return delegate.charTyped(chr, modifiers);
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			y -= 3;

			delegate.setY(y);
			descText.render(context, index, y, width / 2 - 240,
					entryWidth, entryHeight,
					mouseX, mouseY,
					hovered, tickDelta);

			if(name.isBlank()) {
				enterText.render(context, index, y, width / 2 - 157,
						entryWidth, entryHeight,
						mouseX, mouseY,
						hovered, tickDelta);
			}

			delegate.render(context, mouseX, mouseY, tickDelta);
		}
	}

	private void checkValid() {
		// File Name

		if(!new String(name.getBytes(StandardCharsets.UTF_8)).equals(name)) {
			// Non UTF-8 characters
			setFinishButtonError("errorInvalid");
			return;
		}

		try {
			if(name.isBlank()) {
				setFinishButtonError("errorBlank");
				return;
			}

			if(name.contains("/") || name.contains("\\")) {
				setFinishButtonError("errorInvalid");
				return;
			}

			File packDir = client.getResourcePackDir().toFile();
			File file = new File(packDir, name + ".zip");

			if(file.exists()) {
				setFinishButtonError("errorExists");
				return;
			}

			if(!file.createNewFile() || !file.delete()) {
				setFinishButtonError("errorInvalid");
				return;
			}
		} catch(IOException ignored) {
			setFinishButtonError("errorInvalid");
			return;
		}

		// Do we have any files
		if(defaultSkinFile.file == null && defaultCapeFile.file == null
				&& selfSkinFile.file == null && selfCapeFile.file == null
				&& otherSkinFile.file == null && otherCapeFile.file == null) {
			setFinishButtonError("errorNoFiles");
			return;
		}

		// All valid
		finishButton.active = true;
		finishButton.setTooltip(Tooltip.of(Text.empty()));
	}

	private void setFinishButtonError(String error) {
		finishButton.active = false;
		finishButton.setTooltip(Tooltip.of(Text.translatable(
				"offlineskins.settings.skinPacks.finish." + error)));
	}
}
