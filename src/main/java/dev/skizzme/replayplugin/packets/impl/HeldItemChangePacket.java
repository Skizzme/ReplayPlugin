package dev.skizzme.replayplugin.packets.impl;

import dev.skizzme.replayplugin.Packet;
import dev.skizzme.replayplugin.packets.IReplayPacket;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HeldItemChangePacket extends IReplayPacket {

    public ItemStack heldItem;

    public HeldItemChangePacket(Packet packet, long delay, Player player, ItemStack heldItem) {
        super(packet, delay, player);
        this.heldItem = heldItem;
    }
}
