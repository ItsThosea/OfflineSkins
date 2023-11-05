package me.thosea.offlineskins.mixin;

import me.thosea.offlineskins.ScreenAccessor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Screen.class)
public class ScreenMixin implements ScreenAccessor {
	@Mutable @Shadow @Final protected Text title;

	@Override
	public void setTitle(Text title) {
		this.title = title;
	}
}
