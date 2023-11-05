package me.thosea.offlineskins.mixin;

import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import me.thosea.offlineskins.OfflineSkins;
import me.thosea.offlineskins.PlayerAccessor;
import me.thosea.offlineskins.PlayerListEntryAccessor;
import me.thosea.offlineskins.SkinSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin implements PlayerListEntryAccessor {
	@Mutable @Final @Shadow private Supplier<SkinTextures> texturesSupplier;
	@Unique private Supplier<SkinTextures> originalSupplier;
	@Unique private boolean isOverridden;

	@Unique private final Supplier<Boolean> isLocalPlayer = Suppliers.memoize(() -> {
		MinecraftClient client = MinecraftClient.getInstance();
		return client.player != null && getProfile().getId().equals(client.player.getUuid());
	});

	@Inject(method = "<init>", at = @At("TAIL"))
	private void afterCreate(GameProfile profile, boolean secureChatEnforced, CallbackInfo ci) {
		originalSupplier = texturesSupplier;
	}

	@Shadow
	public abstract GameProfile getProfile();

	@Override
	public void refresh(PlayerAccessor player) {
		isOverridden = SkinSettings.ENABLED.getValue() &&
				SkinSettings.GLOBAL_MODE
						.getValue().allow
						.get(isLocalPlayer.get().booleanValue());

		if(!isOverridden) {
			if(player != null) player.setCustomSkin(false);
			texturesSupplier = originalSupplier;
			return;
		}

		if(player != null) player.setCustomSkin(true);
		texturesSupplier = Suppliers.memoize(() -> {
			SkinTextures skin = originalSupplier.get();

			return new SkinTextures(
					getSkinTexture(skin),
					null,
					getCapeTexture(skin),
					null,
					SkinSettings.MODEL_TYPE.getValue(),
					true
			);
		});
	}

	@Unique
	private Identifier getSkinTexture(SkinTextures fallback) {
		boolean local = isLocalPlayer.get();
		if(SkinSettings.SKIN_MODE.getValue().allow.get(local)) {
			return local ? OfflineSkins.SELF_SKIN_ID : OfflineSkins.OTHER_SKIN_ID;
		} else {
			return fallback.texture();
		}
	}

	@Unique
	private Identifier getCapeTexture(SkinTextures fallback) {
		boolean local = isLocalPlayer.get();
		if(SkinSettings.CAPE_MODE.getValue().allow.get(local)) {
			return local ? OfflineSkins.SELF_CAPE_ID : OfflineSkins.OTHER_CAPE_ID;
		} else {
			return fallback.capeTexture();
		}
	}

	@Override
	public boolean isOverridden() {
		return isOverridden;
	}
}
