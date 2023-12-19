package me.thosea.specialskin.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.thosea.specialskin.SkinSettings;
import me.thosea.specialskin.accessor.PlayerEntryAccessor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.util.SkinTextures;
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

	@WrapOperation(method = "getSkinTextures", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/PlayerListEntry;getSkinTextures()Lnet/minecraft/client/util/SkinTextures;"))
	private SkinTextures getSkinTextures(PlayerListEntry entry, Operation<SkinTextures> original) {
		PlayerEntryAccessor accessor = (PlayerEntryAccessor) entry;

		return accessor.sskin$isSkinOverridden()
				? accessor.sskin$getTexture()
				: original.call(entry);
	}
}
