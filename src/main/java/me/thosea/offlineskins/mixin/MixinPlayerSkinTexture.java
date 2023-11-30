package me.thosea.offlineskins.mixin;

import me.thosea.offlineskins.OfflineSkins;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerSkinTexture.class)
public abstract class MixinPlayerSkinTexture {
	@Redirect(method = "loadTexture", at = @At(remap = false, value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V"))
	private void onError(Logger logger, String msg, Throwable e) {
		if(OfflineSkins.FORWARD_EXCEPTION) {
			Util.throwUnchecked(e); // trigger error
		} else {
			logger.warn(msg, e);
		}
	}

	@Inject(method = "remapTexture", at = @At(remap = false, value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V"))
	private void onError(NativeImage image, CallbackInfoReturnable<NativeImage> cir) {
		if(OfflineSkins.FORWARD_EXCEPTION) {
			throw new RuntimeException("The skin texture size is invalid. " +
					"(" + image.getWidth() + "x" + image.getHeight() + ")");
		}
	}
}
