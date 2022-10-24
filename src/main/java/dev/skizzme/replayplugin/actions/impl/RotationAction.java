package dev.skizzme.replayplugin.actions.impl;

import dev.skizzme.replayplugin.actions.ReplayAction;
import dev.skizzme.replayplugin.npc.PlayerNPC;
import org.bukkit.entity.Player;

public class RotationAction extends ReplayAction {

    public double yaw, pitch;

    public RotationAction(double yaw, double pitch, PlayerNPC npc, long delay, Player player, long replayTime) {
        super(npc, delay, player, replayTime);
        this.yaw = yaw;
        this.pitch = pitch;
    }

}
