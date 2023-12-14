package me.thosea.specialskin.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import me.thosea.specialskin.SkinSettings;
import me.thosea.specialskin.SpecialSkin;
import me.thosea.specialskin.accessor.PlayerEntryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class MixinPlayerEntry implements PlayerEntryAccessor {
	@Unique private Identifier ssSkinTexture;
	@Unique private Identifier ssCapeTexture;
	@Unique private boolean isOverridden;
	@Unique private boolean overrideSkins;
	@Unique private boolean overrideInTab;

	@Shadow
	public abstract GameProfile getProfile();
	@Shadow
	public abstract Identifier getSkinTexture();
	@Shadow
	@Nullable
	public abstract Identifier getCapeTexture();

	@Inject(method = "method_2956", at = @At("TAIL"))
	private void afterLoadTextures(Type type, Identifier id, MinecraftProfileTexture texture, CallbackInfo ci) {
		sskin$refresh();
	}

	@Override
	public void sskin$refresh() {
		boolean local = isLocalPlayer();

		if(!shouldOverride(local)) {
			this.ssSkinTexture = null;
			this.ssCapeTexture = null;
			this.isOverridden = false;
			this.overrideSkins = false;
			this.overrideInTab = false;
			return;
		}

		this.isOverridden = true;
		this.overrideInTab = SkinSettings.TAB_MODE.getValue().allow.get(local);

		this.ssSkinTexture = getSkinTexture(local);
		this.ssCapeTexture = getCapeTexture(local);
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
		this.overrideSkins = SkinSettings.SKIN_MODE.getValue().allow.get(local);
		return local ? SpecialSkin.SELF_SKIN_ID : SpecialSkin.OTHER_SKIN_ID;
	}

	@Unique
	private Identifier getCapeTexture(boolean local) {
		if(SkinSettings.CAPE_MODE.getValue().allow.get(local)) {
			return local ? SpecialSkin.SELF_CAPE_ID : SpecialSkin.OTHER_CAPE_ID;
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
	public boolean sskin$isOverridden() {
		return isOverridden;
	}

	@Override
	public boolean sskin$overrideSkins() {
		return overrideSkins;
	}

	@Override
	public boolean sskin$overrideInTab() {
		return overrideInTab;
	}

	@Override
	public Identifier sskin$skinTexture() {
		return ssSkinTexture;
	}

	@Override
	public Identifier sskin$capeTexture() {
		return ssCapeTexture;
	}

	@Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
	private void onGetModel(CallbackInfoReturnable<String> cir) {
		if(isOverridden) {
			cir.setReturnValue(SkinSettings.MODEL_TYPE.getValue().name);
		}
	}
}
