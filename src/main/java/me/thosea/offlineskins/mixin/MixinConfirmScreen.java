package me.thosea.offlineskins.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.thosea.offlineskins.accessor.ConfirmScreenAccessor;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ConfirmScreen.class)
public final class MixinConfirmScreen implements ConfirmScreenAccessor {
	@Shadow @Final @Mutable private Text message;
	@Shadow @Final @Mutable protected BooleanConsumer callback;
	@Shadow @Final private List<ButtonWidget> buttons;

	@Override
	public void setMessage(Text message) {
		this.message = message;
	}

	@Override
	public Text getMessage() {
		return message;
	}

	@Override
	public void setCallback(BooleanConsumer callback) {
		this.callback = callback;
	}

	@Override
	public List<ButtonWidget> getButtons() {
		return buttons;
	}
}
