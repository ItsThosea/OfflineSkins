package me.thosea.offlineskins.accessor;

import net.minecraft.client.util.SkinTextures;

public interface PlayerEntryAccessor {
	void refreshOfflineSkins(PlayerAccessor player);
	boolean overrideInTab();
	boolean isOverriddenOfflineSkins();
	SkinTextures getOfflineSkinsTexture();
}
