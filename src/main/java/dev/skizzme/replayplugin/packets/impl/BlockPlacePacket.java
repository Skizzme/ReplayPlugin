package dev.skizzme.replayplugin.packets.impl;

import dev.skizzme.replayplugin.Packet;
import dev.skizzme.replayplugin.packets.IReplayPacket;
import io.github.retrooper.packetevents.utils.player.Direction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockPlacePacket extends IReplayPacket {

    public Material material;
    public Location location;
    public Direction direction;

    public BlockPlacePacket(Packet packet, long delay, Player player, Material material, Location location, Direction direction) {
        super(packet, delay, player);
        this.material = material;
        this.location = location;
        this.direction = direction;
    }
}
