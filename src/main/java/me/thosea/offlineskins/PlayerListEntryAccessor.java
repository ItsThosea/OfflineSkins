package me.thosea.offlineskins;

public interface PlayerListEntryAccessor {
	void refresh(PlayerAccessor player);
	boolean isOverridden();
}
