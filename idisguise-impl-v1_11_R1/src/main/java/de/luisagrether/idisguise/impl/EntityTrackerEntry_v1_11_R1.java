package de.luisagrether.idisguise.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;

import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.EntityTracker;
import net.minecraft.server.v1_11_R1.EntityTrackerEntry;
import net.minecraft.server.v1_11_R1.WorldServer;

public class EntityTrackerEntry_v1_11_R1 extends EntityTrackerEntry {
    
    private static Field EntityTrackerEntry_e = null;
    private static Field EntityTrackerEntry_f = null;
    private static Field EntityTrackerEntry_g = null;
    private static Field EntityTrackerEntry_u = null;
    private static Field EntityTracker_trackerSet = null;
    static {
        try {
            EntityTrackerEntry_e = EntityTrackerEntry.class.getDeclaredField("e");
            EntityTrackerEntry_e.setAccessible(true);
            EntityTrackerEntry_f = EntityTrackerEntry.class.getDeclaredField("f");
            EntityTrackerEntry_f.setAccessible(true);
            EntityTrackerEntry_g = EntityTrackerEntry.class.getDeclaredField("g");
            EntityTrackerEntry_g.setAccessible(true);
            EntityTrackerEntry_u = EntityTrackerEntry.class.getDeclaredField("u");
            EntityTrackerEntry_u.setAccessible(true);
            EntityTracker_trackerSet = EntityTracker.class.getDeclaredField("c");
            EntityTracker_trackerSet.setAccessible(true);
        } catch(Exception e) {}
    }

    private final int targetId;
    private boolean intercept;

    EntityTrackerEntry_v1_11_R1(EntityTrackerEntry original, int targetId) throws IllegalAccessException {
        super(
            original.b(),
            EntityTrackerEntry_e.getInt(original),
            EntityTrackerEntry_f.getInt(original),
            EntityTrackerEntry_g.getInt(original),
            EntityTrackerEntry_u.getBoolean(original)
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
        EntityTracker tracker = ((WorldServer)((CraftEntity)entity).getHandle().world).tracker;
        EntityTrackerEntry original = tracker.trackedEntities.get(entity.getEntityId());
        if(original instanceof EntityTrackerEntry_v1_11_R1) {
            return;
        } else {
            tracker.trackedEntities.d(entity.getEntityId());
            Set<EntityTrackerEntry> entrySet = (Set<EntityTrackerEntry>)EntityTracker_trackerSet.get(tracker);
            entrySet.remove(original);
            EntityTrackerEntry_v1_11_R1 intruder = new EntityTrackerEntry_v1_11_R1(original, player.getEntityId());
            tracker.trackedEntities.a(entity.getEntityId(), intruder);
            entrySet.add(intruder);
            for(EntityPlayer observer : new ArrayList<>(original.trackedPlayers)) {
                original.clear(observer);
                intruder.updatePlayer(observer);
            }
        }
    }

    public static void toggleIntercept(Entity entity, Player player, boolean intercept) {
        EntityTracker tracker = ((WorldServer)((CraftEntity)entity).getHandle().world).tracker;
        EntityTrackerEntry entry = tracker.trackedEntities.get(entity.getEntityId());
        if(!(entry instanceof EntityTrackerEntry_v1_11_R1)) {
            throw new IllegalStateException();
        } else {
            EntityTrackerEntry_v1_11_R1 intruder = (EntityTrackerEntry_v1_11_R1)entry;
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
