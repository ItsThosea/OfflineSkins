package me.thosea.specialskin.accessor;

import net.minecraft.util.Identifier;

public interface PlayerEntryAccessor {
	void sskin$refresh();
	boolean sskin$isOverridden();
	boolean sskin$overrideSkins();
	boolean sskin$overrideInTab();
	Identifier sskin$skinTexture();
	Identifier sskin$capeTexture();
}
