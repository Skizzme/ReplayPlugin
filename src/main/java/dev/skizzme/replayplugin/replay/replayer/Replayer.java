package dev.skizzme.replayplugin.replay.replayer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.skizzme.replayplugin.packets.IReplayPacket;
import dev.skizzme.replayplugin.actions.*;
import dev.skizzme.replayplugin.actions.impl.*;
import dev.skizzme.replayplugin.npc.PlayerNPC;
import dev.skizzme.replayplugin.replay.ActionParser;
import dev.skizzme.replayplugin.util.RandomUtil;
import dev.skizzme.replayplugin.util.Timer;
import dev.skizzme.replayplugin.util.Util;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Replayer {

    protected ArrayDeque<IReplayPacket> packets;
    protected Player viewer;
    protected HashMap<Player, PlayerNPC> npcMap = new HashMap<>();
    public ArrayList<ReplayAction> actions = new ArrayList<>();
    protected BlockingQueue<ReplayAction> actionQueue = new LinkedBlockingQueue<>();

    protected World world;
    protected Location viewerLocation;

    protected Thread queueThread, runThread;
    protected int replayIndex;
    protected double replaySpeed = 1;
    protected boolean isPaused;
    public ReplayState state;

    protected WorldLoader worldLoader = new WorldLoader(this);
    public ReplayController replayController = new ReplayController(this);

    public Replayer(ArrayDeque<IReplayPacket> packets, Player viewer) {
        this.packets = (ArrayDeque<IReplayPacket>) packets.clone();
        this.viewer = viewer;
        this.viewerLocation = viewer.getLocation();

        this.state = ReplayState.PLAY;

        replayController.copyInventory();

        viewer.sendMessage("Creating world...");
        worldLoader.createWorld();
        viewer.sendMessage("Created world and starting replay...");
    }

    protected void worldCreated() {
        Location tpLoc = viewer.getLocation();
        tpLoc.setWorld(world);
        viewer.teleport(tpLoc);
        replayController.giveControlsToViewer();
        createNPCs(this.packets);
        this.actions = ActionParser.parsePackets(this.packets, this);
    }

    private void createNPCs(ArrayDeque<IReplayPacket> packets) {
        for (IReplayPacket replayPacket : packets) {
            Player p = replayPacket.player;
            //find a good way to get gameprofile
            if (!npcMap.containsKey(p)) {
                PlayerNPC npc = new PlayerNPC(new GameProfile(UUID.randomUUID(), p.getName() + "_" + RandomUtil.randomString(4)), ((CraftWorld)world).getHandle());
                npcMap.put(p, npc);
                npc.addPlayer(viewer);
                npc.setLocation(viewer.getLocation());

                String[] skin = Util.getSkinProperties(p.getUniqueId().toString());
                if (skin != null)
                    npc.entityNPC.getProfile().getProperties().put("textures", new Property("textures", skin[0], skin[1]));
            }
        }
    }

    public void showNPCs() {
        for (PlayerNPC npc : npcMap.values()) {
            npc.showToPlayers();
            npc.setLocation(viewer.getLocation());
        }
    }

    public void startQueue() {
        this.queueThread = new Thread(() -> {
            int index = 0;
            Timer timer = new Timer();
            while (true) {
                if (index < 0) {
                    continue;
                }
                long delay = (long) ((actions.get(index).delay*replaySpeed)-1);
//                viewer.sendMessage("" + index);
                if (state == ReplayState.SKIP_BACKWARD || state == ReplayState.SKIP_FORWARD) {
                    delay = 0;
                }
                if (state != ReplayState.PAUSED && timer.hasTimeElapsed(delay, true)) {
                    actionQueue.add(actions.get(index));
                    index += state != ReplayState.SKIP_BACKWARD ? 1 : -1;
                    if (index >= actions.size()) {
                        index = actions.size()-1;
                    }
                    this.replayIndex = index;
                }
            }
        });
        this.queueThread.setName("ReplayQueueThread");
        this.queueThread.start();
    }

    public void runReplay() {
        this.runThread = new Thread(() -> {
            int index = 0;
            while (true) {
                try {
                    ReplayAction a = actionQueue.take();
                    EntityPlayer npc = a.npc.entityNPC;
                    if (a instanceof MoveAction) {
                        MoveAction action = (MoveAction)a;
//                        action.npc.moveEntity(new Location(viewer.getWorld(), action.x, action.y, action.z, npc.getHeadRotation(), (float) npc.getHeadY()), action.onGround);
                        if (action.rotating) {
                            System.out.println("MOVELOOK");
                            action.npc.moveAndRotateEntity(new Location(viewer.getWorld(), action.x, action.y, action.z, (float) action.yaw, (float) action.pitch), action.onGround);
                        } else {
                            System.out.println("MOVE");
                            action.npc.moveEntity(new Location(viewer.getWorld(), action.x, action.y, action.z, npc.getHeadRotation(), (float) npc.getHeadY()), action.onGround);
                        }
                    }
                    if (a instanceof RotationAction) {
                        System.out.println("LOOK");
                        RotationAction action = (RotationAction)a;
                        action.npc.rotateEntity(action.yaw, action.pitch);
                    }
                    if (a instanceof HeldItemAction) {
                        HeldItemAction action = (HeldItemAction)a;
                        CraftPlayer player = (CraftPlayer) viewer;
                        npc.getBukkitEntity().getPlayer().setItemInHand(action.item);
                        player.getHandle().b.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata(npc.getId(), npc.getDataWatcher(), false));
                    }
                    if (a instanceof SimpleAction) {
                        SimpleAction action = (SimpleAction)a;
                        if (action.actionType == SimpleAction.ActionType.SWING) {
                            a.npc.swingArm();
                        }
                        if (action.actionType == SimpleAction.ActionType.RESPAWN) {
                            a.npc.showToPlayers();
                        }
                        if (action.actionType == SimpleAction.ActionType.START_SNEAK || action.actionType == SimpleAction.ActionType.STOP_SNEAK) {
                            a.npc.setSneaking(action.actionType == SimpleAction.ActionType.START_SNEAK);
                        }
                    }
                    if (a instanceof BlockPlaceAction) {
                        BlockPlaceAction action = (BlockPlaceAction)a;
//                        world.getBlockAt(action.location).setType(action.material);
                        CraftPlayer player = (CraftPlayer) viewer;
//                        Location
//                        BlockPosition position = new Location(new Vec3D(action.location.getBlockX(), action.location.getBlockY(), action.location.getBlockZ()));
//                        player.getHandle().playerConnection.sendPacket(new PacketPlayOutBlockChange(position, (IBlockData) world.getBlockAt(action.location).getBlockData().));
                    }
                    if (a instanceof HealthUpdateAction) {
                        if (((HealthUpdateAction) a).isDamage) {
                            a.npc.damageEntity(((HealthUpdateAction) a).health);
                        }else{
                            a.npc.setHealth(((HealthUpdateAction) a).health);
                        }
                    }
                    index += state != ReplayState.SKIP_BACKWARD ? 1 : -1;
                    replayController.handleIndex(index);
                    this.replayIndex = index;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();;
                }
            }
        });
        this.runThread.setName("ReplayRunThread");
        this.runThread.start();
    }

    public HashMap<Player, PlayerNPC> getNpcMap() {
        return npcMap;
    }

    public World getWorld() {
        return world;
    }
}