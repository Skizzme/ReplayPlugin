package dev.skizzme.replayplugin.replay;

import com.google.gson.JsonElement;
import dev.skizzme.replayplugin.Packet;
import dev.skizzme.replayplugin.actions.ReplayAction;
import dev.skizzme.replayplugin.actions.impl.*;
import dev.skizzme.replayplugin.npc.PlayerNPC;
import dev.skizzme.replayplugin.packets.IReplayPacket;
import dev.skizzme.replayplugin.packets.impl.BlockPlacePacket;
import dev.skizzme.replayplugin.packets.impl.HeldItemChangePacket;
import dev.skizzme.replayplugin.replay.replayer.Replayer;
import io.github.retrooper.packetevents.packetwrappers.play.in.entityaction.WrappedPacketInEntityAction;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.packetwrappers.play.out.updatehealth.WrappedPacketOutUpdateHealth;
import org.bukkit.Location;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class ActionParser {

    public static ArrayList<ReplayAction> parsePackets(ArrayDeque<IReplayPacket> packets, Replayer r) {
        ArrayList<ReplayAction> actions = new ArrayList<>();
        int i = 0;
        long replayTime = 0;
        for (IReplayPacket replayPacket : packets) {
            Packet packet = replayPacket.packet;
            PlayerNPC npc = r.getNpcMap().get(replayPacket.player);
            if (replayPacket instanceof HeldItemChangePacket) {
                actions.add(new HeldItemAction(((HeldItemChangePacket) replayPacket).heldItem, npc, replayPacket.delay, replayPacket.player, replayTime));
                continue;
            }

            if (replayPacket instanceof BlockPlacePacket) {
                BlockPlacePacket blockPlacePacket = (BlockPlacePacket) replayPacket;
                Location loc = blockPlacePacket.location;
                loc.setWorld(r.getWorld());
                actions.add(new BlockPlaceAction(blockPlacePacket.material, loc, blockPlacePacket.direction, npc, replayPacket.delay, replayPacket.player, replayTime));
            }

            boolean isFillerAction = true;
            if (packet.isC03()) {
                WrappedPacketInFlying wrapper = new WrappedPacketInFlying(packet.getRawPacket());
                System.out.println(wrapper.getPosition().toString());
                if (wrapper.isRotating() && !wrapper.isMoving()) {
                    actions.add(new RotationAction(wrapper.getYaw(), wrapper.getPitch(), npc, replayPacket.delay, replayPacket.player, replayTime));
                    isFillerAction = false;
                }
                if (wrapper.isRotating() && wrapper.isMoving()) {
                    actions.add(new MoveAction(wrapper.getPosition().x, wrapper.getPosition().y, wrapper.getPosition().z, wrapper.getYaw(), wrapper.getPitch(), wrapper.isOnGround(), npc, replayPacket.delay, replayPacket.player, replayTime));
                    isFillerAction = false;
                }
                if (!wrapper.isRotating() && wrapper.isMoving()) {
                    actions.add(new MoveAction(wrapper.getPosition().x, wrapper.getPosition().y, wrapper.getPosition().z, wrapper.isOnGround(), npc, replayPacket.delay, replayPacket.player, replayTime));
                    isFillerAction = false;
                }
            }
            if (packet.isUpdateHealth()) {
                WrappedPacketOutUpdateHealth wrapper = new WrappedPacketOutUpdateHealth(packet.getRawPacket());
                if (wrapper.getHealth() < npc.lastHealth) {
                    actions.add(new HealthUpdateAction(wrapper.getHealth(), true, npc, replayPacket.delay, replayPacket.player, replayTime));
                }
                else{
                    actions.add(new HealthUpdateAction(wrapper.getHealth(), false, npc, replayPacket.delay, replayPacket.player, replayTime));
                }
                isFillerAction = false;
                npc.lastHealth = wrapper.getHealth();
            }
            if (packet.isEntityAction()) {
                WrappedPacketInEntityAction wrapper = new WrappedPacketInEntityAction(packet.getRawPacket());

                if (wrapper.getAction() == WrappedPacketInEntityAction.PlayerAction.START_SNEAKING) {
                    actions.add(new SimpleAction(SimpleAction.ActionType.START_SNEAK, npc, replayPacket.delay, replayPacket.player, replayTime));
                    isFillerAction = false;
                }
                if (wrapper.getAction() == WrappedPacketInEntityAction.PlayerAction.STOP_SNEAKING) {
                    actions.add(new SimpleAction(SimpleAction.ActionType.STOP_SNEAK, npc, replayPacket.delay, replayPacket.player, replayTime));
                    isFillerAction = false;
                }
            }
            if (packet.isRespawn()) {
                actions.add(new SimpleAction(SimpleAction.ActionType.RESPAWN, npc, replayPacket.delay, replayPacket.player, replayTime));
            }
            if (packet.isArmAnimation()) {
                actions.add(new SimpleAction(SimpleAction.ActionType.SWING, npc, replayPacket.delay, replayPacket.player, replayTime));
                isFillerAction = false;
            }

            if (isFillerAction) {
                actions.add(new SimpleAction(SimpleAction.ActionType.FILLER, npc, replayPacket.delay, replayPacket.player, replayTime));
            }
            replayTime+=replayPacket.delay;
            i++;
        }
        return actions;
    }

    public static void parseJson(JsonElement json, Replayer r) {

    }

}
