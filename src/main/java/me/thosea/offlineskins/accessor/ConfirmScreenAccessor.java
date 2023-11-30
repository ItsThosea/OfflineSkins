package me.thosea.offlineskins.accessor;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public interface ConfirmScreenAccessor {
	void setMessage(Text message);
	Text getMessage();
	void setCallback(BooleanConsumer callback);
	List<ButtonWidget> getButtons();
}
