package dev.skizzme.replayplugin.packets;

import dev.skizzme.replayplugin.Packet;
import org.bukkit.entity.Player;

public abstract class IReplayPacket {

    public Packet packet;
    public long delay;
    public long createTime;
    public Player player;

    public IReplayPacket(Packet packet, long delay, Player player) {
        this.packet = packet;
        this.delay = delay;
        this.createTime = System.currentTimeMillis();
        this.player = player;
    }
}
