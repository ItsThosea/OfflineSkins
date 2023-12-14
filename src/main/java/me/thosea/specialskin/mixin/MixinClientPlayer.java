package me.thosea.specialskin.mixin;

import com.mojang.authlib.GameProfile;
import me.thosea.specialskin.SkinSettings;
import me.thosea.specialskin.accessor.PlayerEntryAccessor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinClientPlayer extends PlayerEntity {
	@Shadow
	@Nullable
	public abstract PlayerListEntry getPlayerListEntry();

	protected MixinClientPlayer(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
		throw new AssertionError("Nuh uh");
	}

	@Override
	public boolean isPartVisible(PlayerModelPart part) {
		PlayerEntryAccessor accessor = (PlayerEntryAccessor) getPlayerListEntry();

		return accessor != null && accessor.sskin$isOverridden()
				? SkinSettings.ENABLED_PARTS.contains(part)
				: super.isPartVisible(part);
	}

	@Redirect(method = "getSkinTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/PlayerListEntry;getSkinTexture()Lnet/minecraft/util/Identifier;"))
	private Identifier getSkinTextures(PlayerListEntry entry) {
		PlayerEntryAccessor accessor = (PlayerEntryAccessor) entry;

		return accessor.sskin$overrideSkins()
				? accessor.sskin$skinTexture()
				: entry.getSkinTexture();
	}

	@Redirect(method = "getCapeTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/PlayerListEntry;getCapeTexture()Lnet/minecraft/util/Identifier;"))
	private Identifier getCapeTexture(PlayerListEntry entry) {
		PlayerEntryAccessor accessor = (PlayerEntryAccessor) entry;
		Identifier textures = accessor.sskin$capeTexture();

		return textures == null ? entry.getCapeTexture() : textures;
	}
}
