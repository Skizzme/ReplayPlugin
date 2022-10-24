package dev.skizzme.replayplugin.handlers;

import dev.skizzme.replayplugin.Packet;
import dev.skizzme.replayplugin.packets.impl.BlockPlacePacket;
import dev.skizzme.replayplugin.packets.impl.HeldItemChangePacket;
import dev.skizzme.replayplugin.packets.IReplayPacket;
import dev.skizzme.replayplugin.ReplayPlugin;
import dev.skizzme.replayplugin.packets.impl.ReplayPacket;
import io.github.retrooper.packetevents.event.impl.PacketPlayReceiveEvent;
import io.github.retrooper.packetevents.event.impl.PacketPlaySendEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.play.in.blockplace.WrappedPacketInBlockPlace;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayDeque;

public class PacketHandler {

    public static ArrayDeque<IReplayPacket> packets = new ArrayDeque<>();
    private static long lastTime = System.currentTimeMillis();
    public static void processIncoming(PacketPlayReceiveEvent event) {

        Packet packet = new Packet(Packet.Direction.RECEIVE, event.getNMSPacket(), event.getPacketId());

        if (!ReplayPlugin.INSTANCE.replayManager.hasReplay(event.getPlayer())) {
            try{
                if (packets.size() > 0) {
                    while (System.currentTimeMillis() - packets.getFirst().createTime > 10000) {
                        packets.removeFirst();
                    }
                }
            }catch (Exception ignored) { }

            if (packet.isBlockPlace()) {
                WrappedPacketInBlockPlace wrapper = new WrappedPacketInBlockPlace(packet.getRawPacket());
                Material material = event.getPlayer().getItemInHand().getType();
                if (material.isBlock()) {
                    packets.add(new BlockPlacePacket(packet, System.currentTimeMillis()-lastTime, event.getPlayer(), material, new Location(null, wrapper.getBlockPosition().x, wrapper.getBlockPosition().y, wrapper.getBlockPosition().z), wrapper.getDirection()));
                }
            }
            else if (packet.isHeldItemSlot()) {
                packets.add(new HeldItemChangePacket(packet, System.currentTimeMillis()-lastTime, event.getPlayer(), event.getPlayer().getItemInHand()));
            }else{
                packets.add(new ReplayPacket(packet, System.currentTimeMillis()-lastTime, event.getPlayer()));
            }

            lastTime = System.currentTimeMillis();
        }

        if (ReplayPlugin.INSTANCE.replayManager.hasReplay(event.getPlayer())) {
            if (packet.isUseItem()) {
                WrappedPacketInBlockPlace wrapper = new WrappedPacketInBlockPlace(packet.getRawPacket());
                Material itemMaterial = event.getPlayer().getItemInHand().getType();
                if ((itemMaterial == Material.PLAYER_HEAD || itemMaterial == Material.GRAY_DYE || itemMaterial == Material.LIME_DYE || itemMaterial == Material.BARRIER) && ReplayPlugin.INSTANCE.replayManager.hasReplay(event.getPlayer())) {
                    ReplayPlugin.INSTANCE.replayManager.getReplay(event.getPlayer()).replayController.handleControl(event.getPlayer().getItemInHand().getItemMeta().getDisplayName());
                }
            }
        }
    }

    public static void processOutgoing(PacketPlaySendEvent event) {
        Packet packet = new Packet(Packet.Direction.SEND, event.getNMSPacket(), event.getPacketId());

        if (!ReplayPlugin.INSTANCE.replayManager.hasReplay(event.getPlayer())) {
            if (packet.isUpdateHealth() || packet.isRespawn()) {
                try {
                    if (packets.size() > 0) {
                        while (System.currentTimeMillis() - packets.getFirst().createTime > 10000) {
                            packets.removeFirst();
                        }
                    }
                } catch (Exception ignored) {
                }
                packets.add(new ReplayPacket(packet, System.currentTimeMillis() - lastTime, event.getPlayer()));
                lastTime = System.currentTimeMillis();
            }
        }
    }

}
