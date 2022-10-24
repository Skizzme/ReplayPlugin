package dev.skizzme.replayplugin.ncp;

import com.mojang.authlib.GameProfile;
import dev.skizzme.replayplugin.ReplayPlugin;
import dev.skizzme.replayplugin.util.Timer;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.entity.EntityPose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class PlayerNPC {

    public GameProfile gameProfile;
    private Player playerNCP;
    public EntityPlayer entityNPC;
    private ArrayList<Player> players = new ArrayList<>();
    private double lastX, lastY, lastZ;
    public boolean exists = true;

    public double lastHealth = 20;

    private Timer timer = new Timer();

    public PlayerNPC(GameProfile gamePofile, WorldServer world) {
        this.gameProfile = gamePofile;
        this.entityNPC = new EntityPlayer(((CraftServer) Bukkit.getServer()).getServer(), world, gameProfile);
        this.playerNCP = entityNPC.getBukkitEntity().getPlayer();
        this.playerNCP.setPlayerListName(gameProfile.getName());
    }

    public void setLocation(Location loc) {
        lastX = entityNPC.locX();
        lastY = entityNPC.locY();
        lastZ = entityNPC.locZ();
        entityNPC.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public void addPlayer(Player player) {
        if (!this.players.contains(player)) {
            this.players.add(player);
        }
    }

    public void removePlayer(Player player) {
        if (this.players.contains(player)) {
            this.players.remove(player);
        }
    }

    public void teleportToLocation(Location loc) {
        setLocation(loc);
        for (Player p : players) {
            CraftPlayer cp = (CraftPlayer)p;
            cp.getHandle().b.sendPacket(new PacketPlayOutEntityTeleport(entityNPC));
            cp.getHandle().b.sendPacket(new PacketPlayOutEntityHeadRotation(entityNPC, (byte) ((loc.getYaw()*256)/360)));
        }
    }

    public void setSneaking(boolean sneaking) {
        entityNPC.setSneaking(sneaking);
        DataWatcher dw = entityNPC.getDataWatcher();
        for (Player p : players) {
            ((CraftPlayer)p).getHandle().b.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata(entityNPC.getId(), dw, false));
        }
    }

    public void moveAndRotateEntity(Location loc, boolean isOnGround) {
        setLocation(loc);
        for (Player p : players) {
            CraftPlayer cp = (CraftPlayer)p;
            if (Math.abs(entityNPC.locX() - lastX) > 7 || Math.abs(entityNPC.locY() - lastY) > 7 || Math.abs(entityNPC.locZ() - lastZ) > 7) {
                teleportToLocation(loc);
            }else{
                short deltaX = getDelta(entityNPC.locX(), lastX);
                short deltaY = getDelta(entityNPC.locY(), lastY);
                short deltaZ = getDelta(entityNPC.locZ(), lastZ);
                cp.getHandle().b.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(entityNPC.getId(), deltaX, deltaY, deltaZ, (byte) ((loc.getYaw()*256)/360), (byte) ((loc.getPitch()*256)/360), entityNPC.isOnGround()));
                cp.getHandle().b.sendPacket(new PacketPlayOutEntityHeadRotation(entityNPC, (byte) ((loc.getYaw()*256)/360)));
            }
        }
        timer.reset();
    }

    public void moveEntity(Location loc, boolean isOnGround) {
        setLocation(loc);
        for (Player p : players) {
            CraftPlayer cp = (CraftPlayer)p;
            if (Math.abs(entityNPC.locX() - lastX) > 7 || Math.abs(entityNPC.locY() - lastY) > 7 || Math.abs(entityNPC.locZ() - lastZ) > 7) {
                teleportToLocation(loc);
            }else{
                short deltaX = getDelta(entityNPC.locX(), lastX);
                short deltaY = getDelta(entityNPC.locY(), lastY);
                short deltaZ = getDelta(entityNPC.locZ(), lastZ);
                cp.getHandle().b.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMove(entityNPC.getId(), deltaX, deltaY, deltaZ, entityNPC.isOnGround()));
            }
        }
        timer.reset();
    }

    public void rotateEntity(double yaw, double pitch) {
//        entityNCP.setYRot((float) pitch);
        for (Player p : players) {
            CraftPlayer cp = (CraftPlayer)p;
            cp.getHandle().b.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(entityNPC.getId(), (byte) ((yaw*256)/360), (byte) ((pitch*256)/360), entityNPC.isOnGround()));
            cp.getHandle().b.sendPacket(new PacketPlayOutEntityHeadRotation(entityNPC, (byte) ((yaw*256)/360)));
//            }
        }
    }

    public void damageEntity(double health) {
        entityNPC.setHealth((float) health);
        for (Player p : players) {
            CraftPlayer cp = (CraftPlayer)p;
            cp.getHandle().b.sendPacket(new PacketPlayOutAnimation(entityNPC, 1));
        }
        entityNPC.playSound(SoundEffects.ot, 1, 1);
    }

    public void setHealth(double health) {
        entityNPC.setHealth((float) health);
        if (health <= 0) {
            entityNPC.setPose(EntityPose.h);
        }
        DataWatcher dw = entityNPC.getDataWatcher();
        for (Player p : players) {
            CraftPlayer cp = (CraftPlayer)p;
            cp.getHandle().b.sendPacket(new PacketPlayOutEntityMetadata(entityNPC.getId(), dw, false));
        }
    }

    public short getDelta(double current, double previous) {
        //128
        return (short) ((current * 32 - previous * 32) * 128);
    }

    public void showToPlayers() {
        PacketPlayOutPlayerInfo pInfPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, this.entityNPC);
        PacketPlayOutNamedEntitySpawn pNamePacket = new PacketPlayOutNamedEntitySpawn(this.entityNPC);
        for (Player p : players) {
            ((CraftPlayer) p).getHandle().b.sendPacket(pInfPacket);
            ((CraftPlayer) p).getHandle().b.sendPacket(pNamePacket);
        }
    }

    public void hideFromPlayers() {
        for (Player p : players) {
            CraftPlayer cp = (CraftPlayer)p;
            cp.getHandle().b.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy(this.entityNPC.getId()));
        }
    }

    public void swingArm() {
        for (Player p : players) {
            CraftPlayer cp = (CraftPlayer)p;
            cp.getHandle().b.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutAnimation(entityNPC, 0));
        }
    }

    public void followPlayer(Player player) {
        new BukkitRunnable() {
            public void run() {
                moveEntity(player.getLocation(), false);
            }
        }.runTaskTimerAsynchronously(ReplayPlugin.INSTANCE, 0, 1);
    }

}
