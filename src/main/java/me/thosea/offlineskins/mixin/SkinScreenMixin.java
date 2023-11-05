package me.thosea.offlineskins.mixin;

import me.thosea.offlineskins.OfflineSkins;
import me.thosea.offlineskins.PlayerListEntryAccessor;
import me.thosea.offlineskins.ScreenAccessor;
import me.thosea.offlineskins.SkinSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget.UpdateCallback;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkinOptionsScreen.class)
public abstract class SkinScreenMixin extends Screen {
	@Unique private boolean isCustom;

	protected SkinScreenMixin(Text title) {
		super(title);
		throw new AssertionError();
	}

	// Vanilla screen
	@ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;dimensions(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;"), index = 1)
	private int offsetDoneButton(int previous) {
		return isCustom ? previous : previous + 24;
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void addCustomButton(CallbackInfo ci) {
		if(isCustom) return;
		var button = addDrawableChild(ButtonWidget.builder(
						Text.translatable("offlineskins.settings"),
						ignored -> MinecraftClient.getInstance().setScreen(new SkinSettings(this)))
				.dimensions(this.width / 2 - 100, this.height / 6 + 96, 200, 20)
				.build());

		if(OfflineSkins.ERROR != null) {
			button.active = false;
			button.setTooltip(Tooltip.of(Text.translatable(
					"offlineskins.settings.error", OfflineSkins.ERROR)));
		}
	}

	// Custom screen
	@Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;isPlayerModelPartEnabled(Lnet/minecraft/client/render/entity/PlayerModelPart;)Z"))
	private boolean onIsEnabled(GameOptions instance, PlayerModelPart part) {
		return isCustom
				? SkinSettings.ENABLED_PARTS.contains(part)
				: instance.isPlayerModelPartEnabled(part);
	}

	// Very hacky but needed...
	@Unique private static final PlayerModelPart[] modelParts = PlayerModelPart.values();

	@Unique private int index;

	@Inject(method = "init", at = @At("HEAD"))
	private void preInit(CallbackInfo ci) {
		if(OfflineSkins.ENTERING_SKIN_SCREEN) {
			OfflineSkins.ENTERING_SKIN_SCREEN = false;
			isCustom = true;
			index = -1;
			((ScreenAccessor) this).setTitle(Text.translatable("offlineskins.settings.modelParts.title"));
		} else {
			isCustom = false;
		}

	}

	@ModifyArg(method = "init", index = 5, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/CyclingButtonWidget$Builder;build(IIIILnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/CyclingButtonWidget$UpdateCallback;)Lnet/minecraft/client/gui/widget/CyclingButtonWidget;"))
	private UpdateCallback<Boolean> onSetConsumer(UpdateCallback<Boolean> callback) {
		if(!isCustom) return callback;

		index++;

		int ourIndex = index;
		return (button, enabled) -> {
			PlayerModelPart part = modelParts[ourIndex];

			if(enabled) {
				SkinSettings.ENABLED_PARTS.add(part);
			} else {
				SkinSettings.ENABLED_PARTS.remove(part);
			}
		};
	}


	@Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getMainArm()Lnet/minecraft/client/option/SimpleOption;"))
	private SimpleOption<Arm> onGetArm(GameOptions instance) {
		return isCustom ? null : instance.getMainArm();
	}

	@Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;createWidget(Lnet/minecraft/client/option/GameOptions;III)Lnet/minecraft/client/gui/widget/ClickableWidget;"))
	private ClickableWidget onGetSkin(SimpleOption<Arm> instance,
	                                  GameOptions options, int x, int y, int width) {
		return isCustom ? null : instance.createWidget(options, x, y, width);
	}

	@Redirect(method = "init", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/gui/screen/option/SkinOptionsScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"))
	private Element onAddSkinWidget(SkinOptionsScreen instance, Element element) {
		if(!isCustom && client.player != null) {
			var entry = client.player.getPlayerListEntry();

			if(entry != null && ((PlayerListEntryAccessor) entry).isOverridden()) {
				((ClickableWidget) element).active = false;
			}
		}

		return addDrawableChild(cast(element));
	}

	@Redirect(method = "init", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/client/gui/screen/option/SkinOptionsScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"))
	private Element onAddHAndWidget(SkinOptionsScreen instance, Element element) {
		return isCustom
				? null // no hand option
				: this.addDrawableChild(cast(element));
	}

	@Unique
	private <T> T cast(Object obj) {
		return (T) obj;
	}
}
