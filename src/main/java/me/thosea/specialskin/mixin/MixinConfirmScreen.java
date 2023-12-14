package me.thosea.specialskin.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.thosea.specialskin.accessor.ConfirmScreenAccessor;
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
	public void sskin$setMessage(Text message) {
		this.message = message;
	}

	@Override
	public void sskin$setCallback(BooleanConsumer callback) {
		this.callback = callback;
	}

	@Override
	public List<ButtonWidget> sskin$getWidgets() {
		return buttons;
	}
}
