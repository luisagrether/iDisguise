package de.luisagrether.idisguise.api;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * The API to hook into iDisguise. The following code returns an instance:
 * <code>Bukkit.getServicesManager().getRegistration(DisguiseAPI.class).getProvider();</code>
 * 
 * @since 6.0.1
 * @author LuisaGrether
 */
public interface DisguiseAPI {
	
    /**
	 * Get a player's current disguise type.
	 * 
	 * @since 6.0.1
     * @param player the player
	 * @return the player's current disguise type, or <code>null</code> if the player is not disguised
	 */
	public EntityType getDisguise(Player player);

    
	/**
	 * Indicates whether a player is disguised.
	 * 
	 * @since 6.0.1
     * @param player the player
     * @return <code>true</code> if and only if the player is currently disguised
	 */
	public boolean isDisguised(Player player);

	/**
	 * Disguise a player as some non-human entity.
	 * 
	 * @since 6.0.1
     * @param player the player
     * @param type the disguise type
	 * @return the disguise entity, may be edited to alter the disguise appearance
	 * @throws UnsupportedOperationException if the given type is not supported
	 */
	public Entity disguise(Player player, EntityType type);

    /**
     * Disguise a player as another player.
     * 
     * @since 6.0.1
	 * @param player the player
     * @param targetSkin the target skin
	 * @param callback is called after the operation is finished. <code>true</code> indicates success, <code>false</code> indicates that some error occurred.
     * @throws IllegalArgumentException if the given target skin is not a valid Minecraft username
	 * @throws UnsupportedOperationException if player disguise is not supported
     */
    public void disguiseAsPlayer(Player player, String targetSkin, @Nullable Consumer<Boolean> callback);
	
	/**
	 * Undisguise a player.
	 * 
	 * @since 6.0.1
	 * @param player the player
	 * @return the disguise type before undisguising, or <code>null</code> if the player was not disguised
	 */
	public EntityType undisguise(Player player);
	
}
