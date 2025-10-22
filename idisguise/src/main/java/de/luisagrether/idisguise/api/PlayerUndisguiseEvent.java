package de.luisagrether.idisguise.api;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import de.luisagrether.idisguise.iDisguise;

/**
 * This event is fired when a player is to be undisguised (either by command or via the API).
 * <b>WARNING: Players are also undisguised when they leave the server. No event is called in that case.</b>
 * 
 * @since 6.0.1
 * @author LuisaGrether
 */
public class PlayerUndisguiseEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
	private boolean cancel = false;
	private final EntityType type;
	
	public PlayerUndisguiseEvent(Player player) {
		super(player);
		this.type = iDisguise.getInstance().getDisguise(player);
	}
	
	/**
	 * Gets the disguise that is to be removed.
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
