package de.luisagrether.idisguise.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.EntityTrackerEntry;
import net.minecraft.server.v1_16_R2.PlayerChunkMap;
import net.minecraft.server.v1_16_R2.PlayerChunkMap.EntityTracker;
import net.minecraft.server.v1_16_R2.WorldServer;

public class EntityTracker_v1_16_R2 extends EntityTracker {
    
    private static Field EntityTracker_tracker = null;
    private static Field EntityTracker_trackingDistance = null;
    private static Field EntityTracker_trackerEntry = null;
    private static Field EntityTrackerEntry_d = null;
    private static Field EntityTrackerEntry_e = null;
    static {
        try {
            EntityTracker_tracker = EntityTracker.class.getDeclaredField("tracker");
            EntityTracker_tracker.setAccessible(true);
            EntityTracker_trackingDistance = EntityTracker.class.getDeclaredField("trackingDistance");
            EntityTracker_trackingDistance.setAccessible(true);
            EntityTracker_trackerEntry = EntityTracker.class.getDeclaredField("trackerEntry");
            EntityTracker_trackerEntry.setAccessible(true);
            EntityTrackerEntry_d = EntityTrackerEntry.class.getDeclaredField("d");
            EntityTrackerEntry_d.setAccessible(true);
            EntityTrackerEntry_e = EntityTrackerEntry.class.getDeclaredField("e");
            EntityTrackerEntry_e.setAccessible(true);
        } catch(Exception e) {}
    }

    private final int targetId;
    private boolean intercept;

    EntityTracker_v1_16_R2(PlayerChunkMap chunkMap, EntityTracker original, int targetId) throws IllegalAccessException {
        chunkMap.super(
            (net.minecraft.server.v1_16_R2.Entity)EntityTracker_tracker.get(original),
            EntityTracker_trackingDistance.getInt(original),
            EntityTrackerEntry_d.getInt(EntityTracker_trackerEntry.get(original)),
            EntityTrackerEntry_e.getBoolean(EntityTracker_trackerEntry.get(original))
        );
        this.targetId = targetId;
        this.intercept = false;
    }

    public void toggleIntercept(boolean intercept) {
        this.intercept = intercept;
    }

    @Override
    public void updatePlayer(EntityPlayer entityPlayer) {
        if(intercept && entityPlayer.getId() == targetId) {
            return;
        }
        super.updatePlayer(entityPlayer);
    }

    public static void inject(Entity entity, Player player) throws IllegalAccessException {
        PlayerChunkMap chunkMap = ((WorldServer)((CraftEntity)entity).getHandle().world).getChunkProvider().playerChunkMap;
        EntityTracker original = chunkMap.trackedEntities.get(entity.getEntityId());
        if(original instanceof EntityTracker_v1_16_R2) {
            return;
        } else {
            chunkMap.trackedEntities.remove(entity.getEntityId());
            EntityTracker_v1_16_R2 intruder = new EntityTracker_v1_16_R2(chunkMap, original, player.getEntityId());
            chunkMap.trackedEntities.put(entity.getEntityId(), intruder);
            for(EntityPlayer observer : new ArrayList<>(original.trackedPlayers)) {
                original.clear(observer);
                intruder.updatePlayer(observer);
            }
        }
    }

    public static void toggleIntercept(Entity entity, Player player, boolean intercept) {
        PlayerChunkMap chunkMap = ((WorldServer)((CraftEntity)entity).getHandle().world).getChunkProvider().playerChunkMap;
        EntityTracker tracker = chunkMap.trackedEntities.get(entity.getEntityId());
        if(!(tracker instanceof EntityTracker_v1_16_R2)) {
            throw new IllegalStateException();
        } else {
            EntityTracker_v1_16_R2 intruder = (EntityTracker_v1_16_R2)tracker;
            if(intruder.intercept == intercept) {
                return;
            } else {
                intruder.toggleIntercept(intercept);
                if(intercept) {
                    intruder.clear(((CraftPlayer)player).getHandle());
                } else {
                    intruder.updatePlayer(((CraftPlayer)player).getHandle());
                }
            }
        }
    }

}
