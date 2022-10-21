package dev.skizzme.replayplugin.ncp;

import com.mojang.authlib.GameProfile;
import dev.skizzme.replayplugin.ReplayPlugin;
import dev.skizzme.replayplugin.util.Timer;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
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
    public EntityPlayer entityNCP;
    private ArrayList<Player> players = new ArrayList<>();
    private double lastX, lastY, lastZ;

    public double lastHealth = 20;

    private Timer timer = new Timer();

    public PlayerNPC(GameProfile gamePofile, WorldServer world) {
        this.gameProfile = gamePofile;
        this.entityNCP = new EntityPlayer(((CraftServer) Bukkit.getServer()).getServer(), world, gameProfile);
        this.playerNCP = entityNCP.getBukkitEntity().getPlayer();
        this.playerNCP.setPlayerListName(gameProfile.getName());
    }

    public void setLocation(Location loc) {
        lastX = entityNCP.locX();
        lastY = entityNCP.locY();
        lastZ = entityNCP.locZ();
        entityNCP.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
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
            cp.getHandle().b.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport(entityNCP));
            cp.getHandle().b.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation(entityNCP, (byte) ((loc.getYaw()*256)/360)));
        }
    }

    public void setSneaking(boolean sneaking) {
        entityNCP.setSneaking(sneaking);
        DataWatcher dw = entityNCP.getDataWatcher();
        for (Player p : players) {
            ((CraftPlayer)p).getHandle().b.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata(entityNCP.getId(), dw, false));
        }
    }

    public void moveEntity(Location loc, boolean isOnGround) {
        setLocation(loc);
        for (Player p : players) {
            CraftPlayer cp = (CraftPlayer)p;
            if (Math.abs(entityNCP.locX() - lastX) > 7 || Math.abs(entityNCP.locY() - lastY) > 7 || Math.abs(entityNCP.locZ() - lastZ) > 7) {
                teleportToLocation(loc);
            }else{
                short deltaX = getDelta(entityNCP.locX(), lastX);
                short deltaY = getDelta(entityNCP.locY(), lastY);
                short deltaZ = getDelta(entityNCP.locZ(), lastZ);
                cp.getHandle().b.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutEntity.PacketPlayOutRelEntityMove(entityNCP.getId(), deltaX, deltaY, deltaZ, entityNCP.isOnGround()));
            }
        }
        timer.reset();
    }

    public void rotateEntity(double yaw, double pitch) {
//        entityNCP.setYRot((float) pitch);
        for (Player p : players) {
            CraftPlayer cp = (CraftPlayer)p;
            cp.getHandle().b.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutEntity.PacketPlayOutEntityLook(entityNCP.getId(), (byte) ((yaw*256)/360), (byte) ((pitch*256)/360), entityNCP.isOnGround()));
            cp.getHandle().b.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation(entityNCP, (byte) ((yaw*256)/360)));
//            }
        }
    }

    public void damageEntity(double health) {
        entityNCP.setHealth((float) health);
        DataWatcher dw = entityNCP.getDataWatcher();
        for (Player p : players) {
            CraftPlayer cp = (CraftPlayer)p;
            cp.getHandle().b.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutAnimation(entityNCP, 1));
        }
//        entityNCP.playSound(Sound.ENTITY_PLAYER_HURT, 1, 1);
    }

    public void setHealth(double health) {
        entityNCP.setHealth((float) health);
        DataWatcher dw = entityNCP.getDataWatcher();
        for (Player p : players) {
            CraftPlayer cp = (CraftPlayer)p;
            ((CraftPlayer)p).getHandle().b.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata(entityNCP.getId(), dw, false));
        }
    }

    public short getDelta(double current, double previous) {
        //128
        return (short) ((current * 32 - previous * 32) * 128);
    }

    public void showToPlayers() {
        PacketPlayOutPlayerInfo pInfPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, this.entityNCP);
        PacketPlayOutNamedEntitySpawn pNamePacket = new PacketPlayOutNamedEntitySpawn(this.entityNCP);
        for (Player p : players) {
            ((CraftPlayer) p).getHandle().b.sendPacket(pInfPacket);
            ((CraftPlayer) p).getHandle().b.sendPacket(pNamePacket);
        }
    }

    public void hideFromPlayers() {
        for (Player p : players) {
            CraftPlayer cp = (CraftPlayer)p;
            cp.getHandle().b.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy(this.entityNCP.getId()));
        }
    }

    public void swingArm() {
        for (Player p : players) {
            CraftPlayer cp = (CraftPlayer)p;
            cp.getHandle().b.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutAnimation(entityNCP, 0));
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
