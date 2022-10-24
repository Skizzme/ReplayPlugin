package dev.skizzme.replayplugin.actions.impl;

import dev.skizzme.replayplugin.actions.ReplayAction;
import dev.skizzme.replayplugin.npc.PlayerNPC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HeldItemAction extends ReplayAction {

    public ItemStack item;

    public HeldItemAction(ItemStack item, PlayerNPC npc, long delay, Player player, long replayTime) {
        super(npc, delay, player, replayTime);
        this.item = item;
    }
}
