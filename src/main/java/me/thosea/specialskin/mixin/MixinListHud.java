package me.thosea.specialskin.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.thosea.specialskin.SkinSettings;
import me.thosea.specialskin.accessor.PlayerEntryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerListHud.class)
public class MixinListHud {
	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;isInSingleplayer()Z"))
	private boolean onShouldDrawSkins(MinecraftClient client, Operation<Boolean> original) {
		return SkinSettings.ENABLED.getValue() || original.call(client);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/PlayerListEntry;getSkinTextures()Lnet/minecraft/client/util/SkinTextures;"))
	private SkinTextures getSkinTextures(PlayerListEntry entry, Operation<SkinTextures> original) {
		PlayerEntryAccessor accessor = (PlayerEntryAccessor) entry;

		return accessor.sskin$overrideInTab()
				? accessor.sskin$getTexture()
				: original.call(entry);
	}
}
