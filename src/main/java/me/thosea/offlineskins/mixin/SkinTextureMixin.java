package me.thosea.offlineskins.mixin;

import me.thosea.offlineskins.OfflineSkins;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerSkinTexture.class)
public abstract class SkinTextureMixin {
	@Redirect(method = "loadTexture", at = @At(remap = false, value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V"))
	private void onError(Logger logger, String msg, Throwable e) {
		if(OfflineSkins.IS_LOADING) {
			Util.throwUnchecked(e);
		} else {
			logger.warn(msg, e);
		}
	}
}
