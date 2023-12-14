package me.thosea.specialskin.mixin;

import me.thosea.specialskin.accessor.PlayerEntryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.PlayerSkinProvider.Key;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(targets = "net.minecraft.client.texture.PlayerSkinProvider$1")
public final class MixinSkinProvider {
	@Inject(method = "load(Ljava/lang/Object;)Ljava/lang/Object;", at = @At("TAIL"))
	private void getTextureFuture(Object value, CallbackInfoReturnable<Object> cir) {
		var future = ((CompletableFuture<SkinTextures>) cir.getReturnValue());
		UUID id = ((Key) value).profileId();

		future.whenCompleteAsync((textures, error) -> {
			if(textures == null) return;

			MinecraftClient client = MinecraftClient.getInstance();
			client.submit(() -> {
				ClientPlayNetworkHandler network = client.getNetworkHandler();
				if(network == null) return;
				PlayerListEntry entry = network.getPlayerListEntry(id);
				if(entry == null) return;
				((PlayerEntryAccessor) entry).sskin$refresh();
			});
		});
	}
}
