package me.thosea.specialskin.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.thosea.specialskin.SkinSettings;
import me.thosea.specialskin.SpecialSkin;
import me.thosea.specialskin.accessor.PlayerEntryAccessor;
import me.thosea.specialskin.screen.SettingsScreen;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkinOptionsScreen.class)
public abstract class MixinSkinScreen extends Screen {
	@Unique private boolean isCustomModelParts;

	protected MixinSkinScreen(Text title) {
		super(title);
		throw new AssertionError();
	}

	@ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;dimensions(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;"), index = 1)
	private int offsetDoneButton(int previous) {
		return isCustomModelParts ? previous : previous + 24;
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void addCustomButton(CallbackInfo ci) {
		if(isCustomModelParts) return;
		var button = addDrawableChild(ButtonWidget.builder(
						Text.translatable("specialskin.settings"),
						ignored -> client.setScreen(new SettingsScreen(this)))
				.dimensions(this.width / 2 - 100, this.height / 6 + 96, 200, 20)
				.build());

		if(SpecialSkin.ERROR != null) {
			button.active = false;
			button.setTooltip(Tooltip.of(Text.translatable(
					"specialskin.settings.error", SpecialSkin.ERROR)));
		}
	}

	// Custom screen
	@WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;isPlayerModelPartEnabled(Lnet/minecraft/client/render/entity/PlayerModelPart;)Z"))
	private boolean onIsEnabled(GameOptions instance, PlayerModelPart part, Operation<Boolean> original) {
		return isCustomModelParts
				? SkinSettings.ENABLED_PARTS.contains(part)
				: original.call(instance, part);
	}

	@Unique private static final PlayerModelPart[] modelParts = PlayerModelPart.values();
	@Unique private int buttonIndex;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void preInit(CallbackInfo ci) {
		if(SpecialSkin.ENTERING_CUSTOM_MODEL_PARTS) {
			SpecialSkin.ENTERING_CUSTOM_MODEL_PARTS = false;
			this.isCustomModelParts = true;
			this.title = Text.translatable("specialskin.settings.modelParts.title");
		}
	}

	@Inject(method = "init", at = @At("HEAD"))
	private void onInit(CallbackInfo ci) {
		if(this.isCustomModelParts) {
			this.buttonIndex = -1;
		}
	}

	@ModifyArg(method = "init", index = 5, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/CyclingButtonWidget$Builder;build(IIIILnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/CyclingButtonWidget$UpdateCallback;)Lnet/minecraft/client/gui/widget/CyclingButtonWidget;"))
	private UpdateCallback<Boolean> onSetConsumer(UpdateCallback<Boolean> callback) {
		if(!isCustomModelParts) return callback;

		buttonIndex++;

		int ourIndex = buttonIndex;
		return (button, enabled) -> {
			PlayerModelPart part = modelParts[ourIndex];

			if(enabled) {
				SkinSettings.ENABLED_PARTS.add(part);
			} else {
				SkinSettings.ENABLED_PARTS.remove(part);
			}
		};
	}

	@WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getMainArm()Lnet/minecraft/client/option/SimpleOption;"))
	private SimpleOption<?> onAddArmButton(GameOptions options, Operation<SimpleOption<Arm>> original) {
		if(isCustomModelParts) {
			return SkinSettings.ENABLED_PARTS_MODE;
		} else {
			return original.call(options);
		}
	}

	@WrapOperation(method = "init", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/gui/screen/option/SkinOptionsScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"))
	private Element onAddSkinWidget(SkinOptionsScreen instance, Element element, Operation<Element> original) {
		if(!isCustomModelParts && client.player != null) {
			var entry = client.player.getPlayerListEntry();

			if(entry != null && ((PlayerEntryAccessor) entry).sskin$isOverridden()) {
				ClickableWidget widget = (ClickableWidget) element;

				widget.active = false;
				widget.setTooltip(Tooltip.of(Text.translatable("specialskin.SCDisabled")));
			}
		}

		return original.call(instance, element);
	}
}
