package me.thosea.specialskin.accessor;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public interface ConfirmScreenAccessor {
	void sskin$setMessage(Text message);
	void sskin$setCallback(BooleanConsumer callback);
	List<ButtonWidget> sskin$getWidgets();
}
