package me.thosea.specialskin.mixin;

import me.thosea.specialskin.accessor.PlayerEntryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerListHud.class)
public class MixinListHud {
	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;isInSingleplayer()Z"))
	private boolean onShouldDrawSkins(MinecraftClient client) {
		// Always draw skins on tab list
		return true;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/PlayerListEntry;getSkinTextures()Lnet/minecraft/client/util/SkinTextures;"))
	private SkinTextures getSkinTextures(PlayerListEntry entry) {
		PlayerEntryAccessor accessor = (PlayerEntryAccessor) entry;

		return accessor.sskin$overrideInTab() // false if no texture
				? accessor.sskin$getTexture()
				: entry.getSkinTextures(); // vanilla
	}
}
