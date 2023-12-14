package me.thosea.specialskin.mixin;

import com.mojang.authlib.GameProfile;
import me.thosea.specialskin.SkinSettings;
import me.thosea.specialskin.accessor.PlayerEntryAccessor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.player.PlayerEntity;
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

	@Redirect(method = "getSkinTextures", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/PlayerListEntry;getSkinTextures()Lnet/minecraft/client/util/SkinTextures;"))
	private SkinTextures getSkinTextures(PlayerListEntry entry) {
		PlayerEntryAccessor accessor = (PlayerEntryAccessor) entry;

		return accessor.sskin$isSkinOverridden()
				? accessor.sskin$getTexture()
				: entry.getSkinTextures();
	}
}
