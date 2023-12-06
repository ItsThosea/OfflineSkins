package me.thosea.offlineskins.accessor;

import net.minecraft.util.Identifier;

public interface PlayerEntryAccessor {
	void refreshOfflineSkins(PlayerAccessor player);
	boolean overrideInTab();
	boolean isOverriddenOfflineSkins();
	Identifier osSkinTexture();
	Identifier osCapeTexture();
}
