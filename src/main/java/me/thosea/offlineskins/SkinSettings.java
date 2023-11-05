package me.thosea.offlineskins;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.booleans.Boolean2BooleanFunction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.option.SimpleOption.PotentialValuesBasedCallbacks;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.util.SkinTextures.Model;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class SkinSettings extends GameOptionsScreen {
	public static final SimpleOption<Boolean> ENABLED =
			SimpleOption.ofBoolean("offlineskins.settings.enabled", true, SkinSettings::refresh);
	public static final SimpleOption<OverrideMode> GLOBAL_MODE =
			makeOverrideMode("globalMode", OverrideMode.ONLY_YOU);
	public static final SimpleOption<OverrideMode> SKIN_MODE =
			makeOverrideMode("skinMode", OverrideMode.EVERYBODY);
	public static final SimpleOption<OverrideMode> CAPE_MODE =
			makeOverrideMode("capeMode", OverrideMode.EVERYBODY);
	public static final SimpleOption<Model> MODEL_TYPE =
			new SimpleOption<>("offlineskins.settings.modelType",
					SimpleOption.emptyTooltip(),
					(text, model) -> Text.translatable(
							"offlineskins.settings.modelType." + model.ordinal()),
					new PotentialValuesBasedCallbacks<>(List.of(Model.values()),
							Codec.INT.xmap(id -> {
								return Model.values()[MathHelper.floorMod(id, 2)];
							}, Model::ordinal)),
					Model.WIDE,
					SkinSettings::refresh);
	public static final SimpleOption<Boolean> BUTTON_MODEL_PARTS =
			new SimpleOption<>("offlineskins.settings.modelParts",
					SimpleOption.emptyTooltip(),
					(text, dummy) -> Text
							.translatable("offlineskins.settings.modelParts.continued")
							.formatted(Formatting.GRAY, Formatting.ITALIC),
					SimpleOption.BOOLEAN,
					false,
					ignored -> {
						OfflineSkins.ENTERING_SKIN_SCREEN = true;

						MinecraftClient client = MinecraftClient.getInstance();
						client.setScreen(new SkinOptionsScreen(client.currentScreen, client.options));
					});
	public static final Set<PlayerModelPart> ENABLED_PARTS = EnumSet.allOf(PlayerModelPart.class);

	private static final OverrideMode[] values = OverrideMode.values();
	private static SimpleOption<OverrideMode> makeOverrideMode(String name, OverrideMode def) {
		return new SimpleOption<>("offlineskins.settings." + name,
				mode -> {
					String key = "offlineskins.settings.overrideMode." + mode.ordinal() + ".desc";
					return Tooltip.of(Text.translatable(key));
				},
				(text, model) -> Text.translatable(
						"offlineskins.settings.overrideMode." + model.ordinal()),
				new PotentialValuesBasedCallbacks<>(List.of(OverrideMode.values()),
						Codec.INT.xmap(id -> {
							return values[MathHelper.floorMod(id, values.length)];
						}, OverrideMode::ordinal)),
				def,
				SkinSettings::refresh);
	}

	public static void refresh(Object dummy) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayNetworkHandler network = client.getNetworkHandler();

		if(network != null) {
			for(PlayerListEntry entry : network.getListedPlayerListEntries()) {
				((PlayerListEntryAccessor) entry).refresh((PlayerAccessor)
						client.world.getPlayerByUuid(entry.getProfile().getId()));
			}
		}
	}

	private OptionListWidget list;

	public SkinSettings(Screen parent) {
		super(parent, null, Text.translatable("offlineskins.settings"));
	}

	@Override
	protected void init() {
		list = new OptionListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);

		list.addOptionEntry(ENABLED, GLOBAL_MODE);
		list.addOptionEntry(SKIN_MODE, CAPE_MODE);
		list.addOptionEntry(MODEL_TYPE, BUTTON_MODEL_PARTS);

		this.addSelectableChild(list);
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
			this.close();
		}).dimensions(this.width / 2 - 100, this.height - 27, 200, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.render(context, this.list, mouseX, mouseY, delta);
	}

	@Override
	public void removed() {}

	public enum OverrideMode {
		NOBODY(isLocal -> false),
		ONLY_YOU(isLocal -> isLocal),
		ONLY_OTHERS(isLocal -> !isLocal),
		EVERYBODY(isLocal -> true);

		OverrideMode(Boolean2BooleanFunction function) {
			this.allow = function;
		}

		public final Boolean2BooleanFunction allow;
	}
}
