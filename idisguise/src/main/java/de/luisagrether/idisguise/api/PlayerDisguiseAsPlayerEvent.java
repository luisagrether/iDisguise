package de.luisagrether.idisguise.api;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * This event is fired when a player is to be disguised as another player (either by command or via the API).
 * 
 * @since 6.0.1
 * @author LuisaGrether
 */
public class PlayerDisguiseAsPlayerEvent extends PlayerDisguiseEvent {
    
    private static final HandlerList handlers = new HandlerList();
	private final String targetSkin;

    public PlayerDisguiseAsPlayerEvent(Player player, String targetSkin) {
        super(player, EntityType.PLAYER);
        this.targetSkin = targetSkin;
    }

    /**
     * Gets the target skin that is to be applied.
     * 
     * @since 6.0.1
     * @return the target skin
     */
    public String getTargetSkin() {
        return targetSkin;
    }

    public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
