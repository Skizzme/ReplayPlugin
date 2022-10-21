package dev.skizzme.replayplugin.actions.impl;

import dev.skizzme.replayplugin.actions.ReplayAction;
import dev.skizzme.replayplugin.ncp.PlayerNPC;
import org.bukkit.entity.Player;

public class HealthUpdateAction extends ReplayAction {

    public double health;
    public boolean isDamage;

    public HealthUpdateAction(double health, boolean isDamage, PlayerNPC npc, long delay, Player player, long replayTime) {
        super(npc, delay, player, replayTime);
        this.health = health;
        this.isDamage = isDamage;
    }
}
