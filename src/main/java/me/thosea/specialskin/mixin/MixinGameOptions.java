package me.thosea.specialskin.mixin;

import me.thosea.specialskin.SkinSettings;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GameOptions.Visitor;
import net.minecraft.client.render.entity.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public abstract class MixinGameOptions {
	@Inject(method = "accept", at = @At("TAIL"))
	private void accept(Visitor visitor, CallbackInfo ci) {
		visitor.accept("specialskin.enabled", SkinSettings.ENABLED);
		visitor.accept("specialskin.globalMode", SkinSettings.GLOBAL_MODE);
		visitor.accept("specialskin.skinMode", SkinSettings.SKIN_MODE);
		visitor.accept("specialskin.capeMode", SkinSettings.CAPE_MODE);
		visitor.accept("specialskin.tabMode", SkinSettings.TAB_MODE);
		visitor.accept("specialskin.modelType", SkinSettings.MODEL_TYPE);

		for(PlayerModelPart part : PlayerModelPart.values()) {
			boolean wasEnabled = SkinSettings.ENABLED_PARTS.contains(part);
			boolean isEnabled = visitor.visitBoolean(
					"specialskin.modelPart_" + part.getName(),
					wasEnabled);

			if(isEnabled != wasEnabled) {
				if(isEnabled) {
					SkinSettings.ENABLED_PARTS.add(part);
				} else {
					SkinSettings.ENABLED_PARTS.remove(part);
				}
			}
		}
	}
}
