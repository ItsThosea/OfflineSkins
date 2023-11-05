package me.thosea.offlineskins.mixin;

import com.mojang.authlib.GameProfile;
import me.thosea.offlineskins.PlayerAccessor;
import me.thosea.offlineskins.PlayerListEntryAccessor;
import me.thosea.offlineskins.SkinSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class PlayerMixin extends PlayerEntity implements PlayerAccessor {
	@Unique private boolean isCustom;

	@Shadow @Nullable
	private PlayerListEntry playerListEntry;

	protected PlayerMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
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
				((PlayerListEntryAccessor) playerListEntry).refresh(this);
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
}
