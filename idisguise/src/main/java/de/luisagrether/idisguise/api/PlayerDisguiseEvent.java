package de.luisagrether.idisguise.api;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * This event is fired when a player is to be disguised (either by command or via the API).
 * 
 * @since 6.0.1
 * @author LuisaGrether
 */
public class PlayerDisguiseEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
	private boolean cancel = false;
	private final EntityType type;
	
	public PlayerDisguiseEvent(Player player, EntityType type) {
		super(player);
		this.type = type;
	}
	
	/**
	 * Gets the disguise that is to be applied.
	 * 
	 * @since 6.0.1
	 * @return the disguise
	 */
	public EntityType getDisguise() {
		return type;
	}
	
	public boolean isCancelled() {
		return cancel;
	}
	
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
