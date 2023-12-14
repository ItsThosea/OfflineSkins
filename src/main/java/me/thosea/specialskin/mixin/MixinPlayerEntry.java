package me.thosea.specialskin.mixin;

import com.mojang.authlib.GameProfile;
import me.thosea.specialskin.SkinSettings;
import me.thosea.specialskin.SpecialSkin;
import me.thosea.specialskin.accessor.PlayerEntryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListEntry.class)
public abstract class MixinPlayerEntry implements PlayerEntryAccessor {
	@Unique private SkinTextures specialSkinTexture;
	@Unique private boolean overrideSkin;
	@Unique private boolean overrideInTab;

	@Shadow
	public abstract GameProfile getProfile();
	@Shadow
	public abstract SkinTextures getSkinTextures();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void afterCreate(GameProfile profile, boolean secureChatEnforced, CallbackInfo ci) {
		sskin$refresh();
	}

	@Override
	public void sskin$refresh() {
		boolean local = isLocalPlayer();

		if(!shouldOverride(local)) {
			this.specialSkinTexture = null;
			this.overrideInTab = false;
			this.overrideSkin = false;
			return;
		}

		this.overrideInTab = SkinSettings.TAB_MODE.getValue().allow.get(local);

		this.specialSkinTexture = new SkinTextures(
				getSkinTexture(local),
				null,
				getCapeTexture(local),
				null,
				SkinSettings.MODEL_TYPE.getValue(),
				true
		);
	}

	@Unique
	private boolean shouldOverride(boolean local) {
		if(!SkinSettings.ENABLED.getValue()) return false;
		if(SpecialSkin.ERROR != null) return false;
		if(!SkinSettings.GLOBAL_MODE.getValue().allow.get(local)) return false;
		return true;
	}

	@Unique
	private Identifier getSkinTexture(boolean local) {
		this.overrideSkin = SkinSettings.SKIN_MODE.getValue().allow.get(local);
		return local ? SpecialSkin.SELF_SKIN_ID : SpecialSkin.OTHER_SKIN_ID;
	}

	@Unique
	private Identifier getCapeTexture(boolean local) {
		if(SkinSettings.CAPE_MODE.getValue().allow.get(local)) {
			return local ? SpecialSkin.SELF_CAPE_ID : SpecialSkin.OTHER_CAPE_ID;
		} else {
			return getSkinTextures().capeTexture();
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
	public boolean sskin$isOverridden() {
		return specialSkinTexture != null;
	}

	@Override
	public boolean sskin$isSkinOverridden() {
		return overrideSkin;
	}

	@Override
	public boolean sskin$overrideInTab() {
		return overrideInTab;
	}

	@Override
	public SkinTextures sskin$getTexture() {
		return specialSkinTexture;
	}
}
