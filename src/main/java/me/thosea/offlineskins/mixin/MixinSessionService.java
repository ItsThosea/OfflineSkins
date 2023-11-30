package me.thosea.offlineskins.mixin;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(YggdrasilMinecraftSessionService.class)
public class MixinSessionService {
	@Overwrite(remap = false)
	public String getSecurePropertyValue(Property property) {
		return property.value();
	}
}
