package de.luisagrether.idisguise.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.EntityTrackerEntry;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.PlayerChunkMap.EntityTracker;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.ServerPlayerConnection;

public class EntityTracker_v1_17_R1 extends EntityTracker {
    
    private static Field EntityTracker_tracker = null;
    private static Field EntityTracker_trackingDistance = null;
    private static Field EntityTracker_trackerEntry = null;
    private static Field EntityTrackerEntry_e = null;
    private static Field EntityTrackerEntry_f = null;
    static {
        try {
            EntityTracker_tracker = EntityTracker.class.getDeclaredField("c");
            EntityTracker_tracker.setAccessible(true);
            EntityTracker_trackingDistance = EntityTracker.class.getDeclaredField("d");
            EntityTracker_trackingDistance.setAccessible(true);
            EntityTracker_trackerEntry = EntityTracker.class.getDeclaredField("b");
            EntityTracker_trackerEntry.setAccessible(true);
            EntityTrackerEntry_e = EntityTrackerEntry.class.getDeclaredField("e");
            EntityTrackerEntry_e.setAccessible(true);
            EntityTrackerEntry_f = EntityTrackerEntry.class.getDeclaredField("f");
            EntityTrackerEntry_f.setAccessible(true);
        } catch(Exception e) {}
    }

    private final int targetId;
    private boolean intercept;

    EntityTracker_v1_17_R1(PlayerChunkMap chunkMap, EntityTracker original, int targetId) throws IllegalAccessException {
        chunkMap.super(
            (net.minecraft.world.entity.Entity)EntityTracker_tracker.get(original),
            EntityTracker_trackingDistance.getInt(original),
            EntityTrackerEntry_e.getInt(EntityTracker_trackerEntry.get(original)),
            EntityTrackerEntry_f.getBoolean(EntityTracker_trackerEntry.get(original))
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
        PlayerChunkMap chunkMap = ((WorldServer)((CraftEntity)entity).getHandle().t).getChunkProvider().a;
        EntityTracker original = chunkMap.G.get(entity.getEntityId());
        if(original instanceof EntityTracker_v1_17_R1) {
            return;
        } else {
            chunkMap.G.remove(entity.getEntityId());
            EntityTracker_v1_17_R1 intruder = new EntityTracker_v1_17_R1(chunkMap, original, player.getEntityId());
            chunkMap.G.put(entity.getEntityId(), intruder);
            for(ServerPlayerConnection observer : new ArrayList<>(original.f)) {
                original.clear(observer.d());
                intruder.updatePlayer(observer.d());
            }
        }
    }

    public static void toggleIntercept(Entity entity, Player player, boolean intercept) {
        PlayerChunkMap chunkMap = ((WorldServer)((CraftEntity)entity).getHandle().t).getChunkProvider().a;
        EntityTracker tracker = chunkMap.G.get(entity.getEntityId());
        if(!(tracker instanceof EntityTracker_v1_17_R1)) {
            throw new IllegalStateException();
        } else {
            EntityTracker_v1_17_R1 intruder = (EntityTracker_v1_17_R1)tracker;
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
