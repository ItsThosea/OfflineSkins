package me.thosea.specialskin;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.booleans.Boolean2BooleanFunction;
import me.thosea.specialskin.screen.SkinPackScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.option.SimpleOption.PotentialValuesBasedCallbacks;
import net.minecraft.client.option.SimpleOption.TooltipFactory;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class SkinSettings {
	private SkinSettings() {}

	public static final String PREFIX = "specialskin.settings.";

	public static final SimpleOption<Boolean> ENABLED =
			SimpleOption.ofBoolean(PREFIX + "enabled", true, SkinSettings::refresh);
	public static final SimpleOption<OverrideMode> GLOBAL_MODE =
			makeOverrideMode("globalMode", OverrideMode.ONLY_YOU);
	public static final SimpleOption<OverrideMode> SKIN_MODE =
			makeOverrideMode("skinMode", OverrideMode.EVERYBODY);
	public static final SimpleOption<OverrideMode> CAPE_MODE =
			makeOverrideMode("capeMode", OverrideMode.EVERYBODY);
	public static final SimpleOption<OverrideMode> TAB_MODE =
			makeOverrideMode("tabMode", OverrideMode.EVERYBODY);
	public static final SimpleOption<Model> MODEL_TYPE =
			new SimpleOption<>(PREFIX + "modelType",
					SimpleOption.emptyTooltip(),
					(text, model) -> Text.translatable(
							PREFIX + "modelType." + model.ordinal()),
					new PotentialValuesBasedCallbacks<>(List.of(Model.values()),
							Codec.INT.xmap(id -> {
								return Model.values()[MathHelper.floorMod(id, 2)];
							}, Model::ordinal)),
					Model.WIDE,
					SkinSettings::refresh);


	public enum Model {
		SLIM("slim"),
		WIDE("default");

		public final String name;

		Model(String name) {
			this.name = name;
		}
	}

	public static final SimpleOption<?> BUTTON_MODEL_PARTS = makeButtonOption(
			PREFIX + "modelParts",
			() -> {
				SpecialSkin.ENTERING_SKIN_SCREEN = true;

				MinecraftClient client = MinecraftClient.getInstance();
				client.setScreen(new SkinOptionsScreen(client.currentScreen, client.options));
			}
	);
	public static final SimpleOption<?> BUTTON_MAKE_SKIN_PACK = makeButtonOption(
			PREFIX + "skinPacks", () -> {
				MinecraftClient client = MinecraftClient.getInstance();
				client.setScreen(new SkinPackScreen(client.currentScreen));
			});
	public static final Set<PlayerModelPart> ENABLED_PARTS = EnumSet.allOf(PlayerModelPart.class);
	private static final OverrideMode[] values = OverrideMode.values();
	private static SimpleOption<OverrideMode> makeOverrideMode(String name, OverrideMode def) {
		return new SimpleOption<>(PREFIX + name,
				mode -> {
					String key = PREFIX + "overrideMode." + mode.ordinal() + ".desc";
					return Tooltip.of(Text.translatable(key));
				},
				(text, model) -> Text.translatable(
						PREFIX + "overrideMode." + model.ordinal()),
				new PotentialValuesBasedCallbacks<>(List.of(OverrideMode.values()),
						Codec.INT.xmap(id -> {
							return values[MathHelper.floorMod(id, values.length)];
						}, OverrideMode::ordinal)),
				def,
				SkinSettings::refresh);
	}

	public static SimpleOption<?> makeButtonOption(String key, Runnable onClick) {
		return makeButtonOption(key, SimpleOption.emptyTooltip(), onClick);
	}

	public static SimpleOption<?> makeButtonOption(String key,
	                                               TooltipFactory<Boolean> tooltip,
	                                               Runnable onClick) {
		return new SimpleOption<>(key,
				tooltip,
				(text, value) -> Text
						.translatable(PREFIX + "common.continued")
						.formatted(Formatting.GRAY, Formatting.ITALIC),
				SimpleOption.BOOLEAN,
				false,
				ignored -> {
					onClick.run();
				});
	}

	private static void refresh(Object dummy) {
		SpecialSkin.refresh();
	}

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
