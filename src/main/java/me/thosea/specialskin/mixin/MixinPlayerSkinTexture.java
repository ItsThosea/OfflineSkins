package me.thosea.specialskin.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.thosea.specialskin.SpecialSkin;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerSkinTexture.class)
public abstract class MixinPlayerSkinTexture {
	@WrapOperation(method = "loadTexture", at = @At(remap = false, value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V"))
	private void onError(Logger instance, String msg, Throwable e, Operation<Void> original) {
		if(SpecialSkin.FORWARD_EXCEPTION) {
			Util.throwUnchecked(e); // trigger error
		} else {
			original.call(instance, msg, e);
		}
	}

	@Inject(method = "remapTexture", at = @At(remap = false, value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V"))
	private void onError(NativeImage image, CallbackInfoReturnable<NativeImage> cir) {
		if(SpecialSkin.FORWARD_EXCEPTION) {
			throw new RuntimeException("The skin texture size is invalid. " +
					"(" + image.getWidth() + "x" + image.getHeight() + ")");
		}
	}
}
