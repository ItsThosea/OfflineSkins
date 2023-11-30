package me.thosea.offlineskins.mixin;

import com.mojang.authlib.GameProfile;
import me.thosea.offlineskins.accessor.PlayerAccessor;
import me.thosea.offlineskins.accessor.PlayerEntryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.PlayerSkinProvider.Key;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(targets = "net.minecraft.client.texture.PlayerSkinProvider$1")
public final class MixinSkinProvider {
	@Inject(method = "load(Lnet/minecraft/client/texture/PlayerSkinProvider$Key;)Ljava/util/concurrent/CompletableFuture;", at = @At("TAIL"))
	private void getTextureFuture(Key key, CallbackInfoReturnable<CompletableFuture<SkinTextures>> cir) {
		GameProfile profile = key.profile();
		cir.getReturnValue().whenCompleteAsync((textures, error) -> {
			if(textures == null) return;

			MinecraftClient client = MinecraftClient.getInstance();
			client.submit(() -> {
				ClientPlayNetworkHandler network = client.getNetworkHandler();
				if(network == null) return;
				PlayerListEntry entry = network.getPlayerListEntry(profile.getId());
				if(entry == null) return;
				if(client.world == null) return;
				PlayerEntity player = client.world.getPlayerByUuid(profile.getId());
				((PlayerEntryAccessor) entry).refreshOfflineSkins((PlayerAccessor) player);
			});
		});
	}
}
