package me.thosea.offlineskins.mixin;

import com.mojang.authlib.GameProfile;
import me.thosea.offlineskins.SkinSettings;
import me.thosea.offlineskins.accessor.PlayerAccessor;
import me.thosea.offlineskins.accessor.PlayerEntryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinClientPlayer extends PlayerEntity implements PlayerAccessor {
	@Unique private boolean isCustom;

	@Shadow @Nullable
	private PlayerListEntry playerListEntry;

	protected MixinClientPlayer(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
		throw new AssertionError("Nuh uh");
	}

	@Overwrite
	@Nullable
	public PlayerListEntry getPlayerListEntry() {
		if(playerListEntry == null) {
			this.playerListEntry = MinecraftClient.getInstance()
					.getNetworkHandler()
					.getPlayerListEntry(getUuid());

			if(playerListEntry != null) {
				((PlayerEntryAccessor) playerListEntry).refreshOfflineSkins(this);
			}
		}

		return playerListEntry;
	}

	@Override
	public boolean isPartVisible(PlayerModelPart part) {
		return isCustom
				? SkinSettings.ENABLED_PARTS.contains(part)
				: super.isPartVisible(part);
	}

	@Override
	public void setCustomSkin(boolean enabled) {
		this.isCustom = enabled;
	}

	@Redirect(method = "getSkinTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/PlayerListEntry;getSkinTexture()Lnet/minecraft/util/Identifier;"))
	private Identifier getSkinTextures(PlayerListEntry entry) {
		PlayerEntryAccessor accessor = (PlayerEntryAccessor) entry;
		Identifier textures = accessor.osSkinTexture();

		return textures == null ? entry.getSkinTexture() : textures;
	}

	@Redirect(method = "getCapeTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/PlayerListEntry;getCapeTexture()Lnet/minecraft/util/Identifier;"))
	private Identifier getCapeTexture(PlayerListEntry entry) {
		PlayerEntryAccessor accessor = (PlayerEntryAccessor) entry;
		Identifier textures = accessor.osCapeTexture();

		return textures == null ? entry.getCapeTexture() : textures;
	}
}
