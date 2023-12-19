package me.thosea.specialskin.mixin;

import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// To be overridden by MixinClientPlayer
@Mixin(PlayerEntity.class)
public abstract class MixinPlayer {
	@Inject(method = "isPartVisible", at = @At("HEAD"), cancellable = true)
	protected abstract void onIsPartEnabled(PlayerModelPart part, CallbackInfoReturnable<Boolean> cir);
}
