package me.thosea.offlineskins.mixin;

import com.mojang.authlib.GameProfile;
import me.thosea.offlineskins.OfflineSkins;
import me.thosea.offlineskins.SkinSettings;
import me.thosea.offlineskins.accessor.PlayerAccessor;
import me.thosea.offlineskins.accessor.PlayerEntryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class MixinPlayerEntry implements PlayerEntryAccessor {
	@Unique private Identifier osSkinTexture;
	@Unique private Identifier osCapeTexture;
	@Unique private boolean isOverridden;
	@Unique private boolean overrideInTab;

	@Shadow
	public abstract GameProfile getProfile();

	@Shadow
	public abstract Identifier getSkinTexture();
	@Shadow
	@Nullable
	public abstract Identifier getCapeTexture();

	@Override
	public void refreshOfflineSkins(PlayerAccessor player) {
		boolean local = isLocalPlayer();

		if(!shouldOverride(local)) {
			if(player != null) {
				player.setCustomSkin(false);
			}
			this.osSkinTexture = null;
			this.osCapeTexture = null;
			this.overrideInTab = false;
			this.isOverridden = false;
			return;
		}

		if(player != null) {
			player.setCustomSkin(true);
		}

		this.isOverridden = true;
		this.overrideInTab = SkinSettings.TAB_MODE.getValue().allow.get(local);

		this.osSkinTexture = getSkinTexture(local);
		this.osCapeTexture = getCapeTexture(local);
	}

	@Unique
	private boolean shouldOverride(boolean local) {
		if(!SkinSettings.ENABLED.getValue()) return false;
		if(OfflineSkins.ERROR != null) return false;
		if(!SkinSettings.GLOBAL_MODE.getValue().allow.get(local)) return false;
		return true;
	}

	@Unique
	private Identifier getSkinTexture(boolean local) {
		if(SkinSettings.SKIN_MODE.getValue().allow.get(local)) {
			return local ? OfflineSkins.SELF_SKIN_ID : OfflineSkins.OTHER_SKIN_ID;
		} else {
			return null;
		}
	}

	@Unique
	private Identifier getCapeTexture(boolean local) {
		if(SkinSettings.CAPE_MODE.getValue().allow.get(local)) {
			return local ? OfflineSkins.SELF_CAPE_ID : OfflineSkins.OTHER_CAPE_ID;
		} else {
			return null;
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
		return isOverridden;
	}

	@Override
	public Identifier osSkinTexture() {
		return osSkinTexture;
	}

	@Override
	public Identifier osCapeTexture() {
		return osCapeTexture;
	}

	@Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
	private void onGetModel(CallbackInfoReturnable<String> cir) {
		if(isOverridden) {
			cir.setReturnValue(SkinSettings.MODEL_TYPE.getValue().name);
		}
	}
}
