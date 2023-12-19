package me.thosea.specialskin.accessor;

import net.minecraft.util.Identifier;

public interface PlayerEntryAccessor {
	void sskin$refresh();
	boolean sskin$isOverridden();
	boolean sskin$overrideSkins();
	boolean sskin$overrideInTab();
	boolean sskin$overrideEnabledParts();
	Identifier sskin$skinTexture();
	Identifier sskin$capeTexture();
}
