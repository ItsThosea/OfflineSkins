package me.thosea.offlineskins.screen;

import me.thosea.offlineskins.accessor.ConfirmScreenAccessor;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static me.thosea.offlineskins.SkinSettings.PREFIX;

public class SetFileScreen extends ConfirmScreen {
	private final Screen parent;
	private final ConfirmScreenAccessor accessor;
	private final Function<File, Boolean> allowFile;
	private final Text message;

	public boolean allowNoFile;

	public final File ogFile;
	public final Consumer<File> onInvalidFile;
	public File selected;

	public SetFileScreen(Screen parent,
	                     Text title,
	                     Text message,
	                     File selected,
	                     boolean allowNoFile,
	                     Function<File, Boolean> allowFile,
	                     Consumer<File> onInvalidFile,
	                     Consumer<File> handler) {
		super(null, title, message, ScreenTexts.DONE, ScreenTexts.CANCEL);

		this.ogFile = selected;
		this.selected = selected;
		this.allowNoFile = allowNoFile;
		this.onInvalidFile = onInvalidFile == null ? file -> {} : onInvalidFile;
		this.allowFile = allowFile == null ? file -> true : allowFile;
		this.parent = parent;
		this.message = message;
		this.accessor = (ConfirmScreenAccessor) this;

		// We can't access properties before calling super
		accessor.setCallback(confirmed -> {
			if(confirmed) {
				setButtonActivation();

				if(!accessor.getButtons().get(0).active) {
					return;
				}

				handler.accept(this.selected);
			}

			close();
		});

		if(ogFile != null) {
			Text text = Text.translatable(PREFIX + "currentFile", ogFile.getName());
			accessor.setMessage(combine(text));
		}
	}

	@Override
	public void filesDragged(List<Path> paths) {
		if(paths.isEmpty()) return;

		Path path = paths.get(0);
		File file;

		try {
			file = path.toFile();
		} catch(Exception e) {
			return;
		}

		if(allowFile.apply(file)) {
			selected = file;
		} else {
			onInvalidFile.accept(file);
		}

		if(client.currentScreen != this) return;

		clearAndInit();
		clearAndInit();
	}

	private boolean isAdding;

	@Override
	protected void addButtons(int y) {
		isAdding = true;
		super.addButtons(y);

		if(allowNoFile) {
			this.addButton(ButtonWidget.builder(
							Text.translatable(PREFIX + "removeFileButton"),
							button -> {
								selected = null;
								button.active = false;
								clearAndInit();
								clearAndInit();
							})
					.dimensions(this.width / 2 - 155 + 80, y + 25, 150, 20).build());
		}

		setButtonActivation();
	}

	private void setButtonActivation() {
		if(isAdding) {
			isAdding = false;
		} else {
			clearAndInit();
			return;
		}

		ButtonWidget yes = accessor.getButtons().get(0);

		if(selected != null && !selected.exists()) {
			selected = null;
		}

		if(allowNoFile) {
			ButtonWidget remove = accessor.getButtons().get(2);
			remove.active = selected != null;
		}

		if(Objects.equals(selected, ogFile)) {
			yes.active = false;

			if(selected != null) {
				Text text = Text.translatable(PREFIX + "currentFile", makeFileText());
				accessor.setMessage(combine(text));
			} else {
				accessor.setMessage(message);
			}
		} else {
			yes.active = true;

			Text text = selected == null
					? Text.translatable(PREFIX + "removeFile")
					: Text.translatable(PREFIX + "setFileTo", makeFileText());
			accessor.setMessage(combine(text));
		}
	}

	private Text makeFileText() {
		return Text.literal(selected.getName()).formatted(Formatting.GRAY, Formatting.ITALIC);
	}

	private Text combine(Text other) {
		return message.copy().append("\n").append(other);
	}

	public Screen getParent() {
		return parent;
	}

	@Override
	public void close() {
		client.setScreen(parent);
	}
}
