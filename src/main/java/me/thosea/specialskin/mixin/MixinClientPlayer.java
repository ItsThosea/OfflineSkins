package me.thosea.specialskin.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.thosea.specialskin.SkinSettings;
import me.thosea.specialskin.accessor.PlayerEntryAccessor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinClientPlayer extends MixinPlayer {
	@Shadow
	@Nullable
	public abstract PlayerListEntry getPlayerListEntry();

	@Override
	protected void onIsPartEnabled(PlayerModelPart part, CallbackInfoReturnable<Boolean> cir) {
		PlayerEntryAccessor accessor = (PlayerEntryAccessor) getPlayerListEntry();

		if(accessor != null && accessor.sskin$overrideEnabledParts()) {
			cir.setReturnValue(SkinSettings.ENABLED_PARTS.contains(part));
		}
	}

	@WrapOperation(method = "getSkinTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/PlayerListEntry;getSkinTexture()Lnet/minecraft/util/Identifier;"))
	private Identifier getSkinTextures(PlayerListEntry entry, Operation<Identifier> original) {
		PlayerEntryAccessor accessor = (PlayerEntryAccessor) entry;

		return accessor.sskin$overrideSkins()
				? accessor.sskin$skinTexture()
				: original.call(entry);
	}

	@WrapOperation(method = "getCapeTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/PlayerListEntry;getCapeTexture()Lnet/minecraft/util/Identifier;"))
	private Identifier getCapeTexture(PlayerListEntry entry, Operation<Identifier> original) {
		PlayerEntryAccessor accessor = (PlayerEntryAccessor) entry;
		Identifier textures = accessor.sskin$capeTexture();

		return textures == null
				? original.call(entry)
				: textures;
	}
}
