package me.thosea.offlineskins.mixin;

import me.thosea.offlineskins.accessor.PlayerEntryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
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

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/PlayerListEntry;getSkinTexture()Lnet/minecraft/util/Identifier;"))
	private Identifier getSkinTextures(PlayerListEntry entry) {
		PlayerEntryAccessor accessor = (PlayerEntryAccessor) entry;

		return accessor.overrideInTab() // false if no texture
				? accessor.osCapeTexture()
				: entry.getSkinTexture(); // vanilla
	}
}
