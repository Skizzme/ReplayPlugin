package dev.skizzme.replayplugin.actions;

import dev.skizzme.replayplugin.ncp.PlayerNPC;
import org.bukkit.entity.Player;

public abstract class ReplayAction {

    public Player player;
    public long delay;
    public PlayerNPC npc;
    public long replayTime;

    public ReplayAction(PlayerNPC npc, long delay, Player player, long replayTime) {
        this.npc = npc;
        this.delay = delay;
        this.player = player;
        this.replayTime = replayTime;
    }

}
