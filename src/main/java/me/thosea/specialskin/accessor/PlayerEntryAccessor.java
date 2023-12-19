package me.thosea.specialskin.accessor;

import net.minecraft.client.util.SkinTextures;

public interface PlayerEntryAccessor {
	void sskin$refresh();
	
	boolean sskin$isOverridden();
	boolean sskin$isSkinOverridden();
	boolean sskin$overrideInTab();
	boolean sskin$overrideEnabledParts();

	SkinTextures sskin$getTexture();
}
