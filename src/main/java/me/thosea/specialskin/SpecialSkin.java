package me.thosea.specialskin;

import me.thosea.specialskin.accessor.PlayerEntryAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;

public final class SpecialSkin implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("specialskin");

	public static String ERROR = null;
	public static boolean FORWARD_EXCEPTION = false;

	private static final Identifier SKIN_ID = new Identifier("specialskin", "skin.png");
	private static final Identifier CAPE_ID = new Identifier("specialskin", "cape.png");

	public static Identifier SELF_SKIN_ID = SKIN_ID;
	public static Identifier SELF_CAPE_ID = CAPE_ID;

	public static Identifier OTHER_SKIN_ID = SKIN_ID;
	public static Identifier OTHER_CAPE_ID = CAPE_ID;

	public static boolean ENTERING_CUSTOM_MODEL_PARTS = false;

	@Override
	public void onInitializeClient() {
		ResourceManagerHelper
				.get(ResourceType.CLIENT_RESOURCES)
				.registerReloadListener(new SkinLoader());

		KeyBinding toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"specialskin.keybind.name",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_L,
				"specialskin.keybind.category"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while(toggleKey.wasPressed()) {
				SkinSettings.ENABLED.setValue(!SkinSettings.ENABLED.getValue());
			}
		});
	}

	public static void refresh() {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayNetworkHandler network = client.getNetworkHandler();

		if(network != null && client.world != null) {
			for(PlayerListEntry entry : network.getListedPlayerListEntries()) {
				((PlayerEntryAccessor) entry).sskin$refresh();
			}
		}
	}

	private static class SkinLoader implements SimpleSynchronousResourceReloadListener {
		@Override
		public Identifier getFabricId() {
			return new Identifier("specialskin", "skin_loader");
		}

		@Override
		public void reload(ResourceManager manager) {
			try {
				FORWARD_EXCEPTION = true;
				init(manager);
				ERROR = null;
				refresh();
			} catch(Throwable e) {
				LOGGER.error("An error has occurred while loading textures", e);
				LOGGER.error("The mod will remain disabled until textures have been loaded successfully.");

				Throwable root = e;
				for(; root.getCause() != null; root = root.getCause()) {}

				ERROR = root.toString();
			} finally {
				FORWARD_EXCEPTION = false;
			}
		}

		private void init(ResourceManager manager) throws Exception {
			var binder = MinecraftClient.getInstance().getTextureManager();

			makeTexture(true, SKIN_ID, manager, binder);
			makeTexture(false, CAPE_ID, manager, binder);

			SELF_SKIN_ID = optional("skin-self.png", true, true, manager, binder);
			SELF_CAPE_ID = optional("cape-self.png", false, true, manager, binder);

			OTHER_SKIN_ID = optional("skin-others.png", true, false, manager, binder);
			OTHER_CAPE_ID = optional("cape-others.png", false, false, manager, binder);
		}

		private Identifier optional(String path,
		                            boolean skin, boolean self,
		                            ResourceManager manager, TextureManager binder)
				throws Exception {
			Identifier id = new Identifier("specialskin", path);

			try {
				makeTexture(skin, id, manager, binder);
				return id;
			} catch(FileNotFoundException ignored) {
				Identifier fallback = skin ? SKIN_ID : CAPE_ID;

				logNoCustom(id, fallback.getPath(), self);
				return fallback;
			}
		}

		private void logNoCustom(Identifier id, String type, boolean self) {
			LOGGER.info("No custom {} for {} ({}), defaulting to {}",
					type, self ? "own player" : "other players",
					id.getPath(), type);
		}

		private void makeTexture(boolean isSkin, Identifier id, ResourceManager manager,
		                         TextureManager binder) throws Exception {
			InputStream stream = manager.getResourceOrThrow(id).getInputStream();

			new PlayerSkinTexture(
					null,
					null,
					DefaultSkinHelper.getTexture(),
					isSkin,
					null) {

				{
					NativeImage image = loadTexture(stream);
					if(image != null)
						onTextureLoaded(image);

					binder.registerTexture(id, this);
				}

				@Override
				public void load(ResourceManager manager) {}
			};
		}
	}
}
