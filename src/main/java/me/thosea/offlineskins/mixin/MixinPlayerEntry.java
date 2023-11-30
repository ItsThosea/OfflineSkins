package me.thosea.offlineskins.mixin;

import com.mojang.authlib.GameProfile;
import me.thosea.offlineskins.OfflineSkins;
import me.thosea.offlineskins.SkinSettings;
import me.thosea.offlineskins.accessor.PlayerAccessor;
import me.thosea.offlineskins.accessor.PlayerEntryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerListEntry.class)
public abstract class MixinPlayerEntry implements PlayerEntryAccessor {
	@Unique private SkinTextures offlineSkinsTexture;
	@Unique private boolean overrideInTab;

	@Shadow
	public abstract GameProfile getProfile();
	@Shadow
	public abstract SkinTextures getSkinTextures();

	@Override
	public void refreshOfflineSkins(PlayerAccessor player) {
		boolean local = isLocalPlayer();

		if(!shouldOverride(local)) {
			if(player != null) {
				player.setCustomSkin(false);
			}
			this.offlineSkinsTexture = null;
			this.overrideInTab = false;
			return;
		}

		if(player != null) {
			player.setCustomSkin(true);
		}

		this.overrideInTab = SkinSettings.TAB_MODE.getValue().allow.get(local);

		SkinTextures skin = getSkinTextures();
		this.offlineSkinsTexture = new SkinTextures(
				getSkinTexture(skin, local),
				null,
				getCapeTexture(skin, local),
				null,
				SkinSettings.MODEL_TYPE.getValue(),
				true
		);
	}

	@Unique
	private boolean shouldOverride(boolean local) {
		if(!SkinSettings.ENABLED.getValue()) return false;
		if(OfflineSkins.ERROR != null) return false;
		if(!SkinSettings.GLOBAL_MODE.getValue().allow.get(local)) return false;
		return true;
	}

	@Unique
	private Identifier getSkinTexture(SkinTextures fallback, boolean local) {
		if(SkinSettings.SKIN_MODE.getValue().allow.get(local)) {
			return local ? OfflineSkins.SELF_SKIN_ID : OfflineSkins.OTHER_SKIN_ID;
		} else {
			return fallback.texture();
		}
	}

	@Unique
	private Identifier getCapeTexture(SkinTextures fallback, boolean local) {
		if(SkinSettings.CAPE_MODE.getValue().allow.get(local)) {
			return local ? OfflineSkins.SELF_CAPE_ID : OfflineSkins.OTHER_CAPE_ID;
		} else {
			return fallback.capeTexture();
		}
	}

	@Unique
	private boolean isLocalPlayer() {
		MinecraftClient client = MinecraftClient.getInstance();
		if(client.player == null) return false;
		if(!getProfile().getId().equals(client.player.getUuid())) return false;

		return true;
	}

	@Override
	public boolean overrideInTab() {
		return overrideInTab;
	}

	@Override
	public boolean isOverriddenOfflineSkins() {
		return offlineSkinsTexture != null;
	}

	@Override
	public SkinTextures getOfflineSkinsTexture() {
		return offlineSkinsTexture;
	}
}
