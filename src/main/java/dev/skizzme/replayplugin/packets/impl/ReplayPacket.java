package dev.skizzme.replayplugin.packets.impl;

import dev.skizzme.replayplugin.Packet;
import dev.skizzme.replayplugin.packets.IReplayPacket;
import org.bukkit.entity.Player;

public class ReplayPacket extends IReplayPacket {

    public ReplayPacket(Packet packet, long delay, Player player) {
        super(packet, delay, player);
    }

}
