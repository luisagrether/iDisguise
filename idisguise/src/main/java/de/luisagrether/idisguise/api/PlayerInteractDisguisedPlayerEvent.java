package de.luisagrether.idisguise.api;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import de.luisagrether.idisguise.iDisguise;

/**
 * This event is fired whenever someone right clicks an entity that represents a disguised player.<br>
 * This event was introduced to enable new plugin functionality (e.g. MobAbilities).<br>
 * <b>WARNING: Due to changes in the internal handling of disguises, this event does not extend <code>PlayerInteractEntityEvent</code> in the new API (6.0.1+).</b>
 * 
 * @since 6.0.1
 * @author LuisaGrether
 */
public class PlayerInteractDisguisedPlayerEvent extends PlayerEvent {
	
	private static final HandlerList handlers = new HandlerList();
    private final Player clicked;
    private final EntityType type;
	
	public PlayerInteractDisguisedPlayerEvent(Player who, Player clicked) {
		super(who);
        this.clicked = clicked;
        this.type = iDisguise.getInstance().getDisguise(clicked);
	}
	
	/**
	 * Gets the disguised player that was right-clicked by the player.
	 * 
	 * @since 6.0.1
	 * @return disguised player right-clicked by player
	 */
	public Player getRightClicked() {
		return clicked;
	}
	
	/**
	 * Gets the disguise type of the right-clicked player.
	 * 
	 * @since 6.0.1
	 * @return disguise type of the right-clicked player
	 */
	public EntityType getDisguise() {
		return type;
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}
