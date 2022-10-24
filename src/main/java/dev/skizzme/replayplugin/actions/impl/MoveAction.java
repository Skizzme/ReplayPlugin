package dev.skizzme.replayplugin.actions.impl;

import dev.skizzme.replayplugin.actions.ReplayAction;
import dev.skizzme.replayplugin.npc.PlayerNPC;
import org.bukkit.entity.Player;

public class MoveAction extends ReplayAction {

    public double x, y, z, yaw, pitch;
    public boolean onGround, rotating;

    public MoveAction(double x, double y, double z, boolean onGround, PlayerNPC npc, long delay, Player player, long replayTime) {
        super(npc, delay, player, replayTime);
        this.x = x;
        this.y = y;
        this.z = z;
        rotating = false;
    }

    public MoveAction(double x, double y, double z, double yaw, double pitch, boolean onGround, PlayerNPC npc, long delay, Player player, long replayTime) {
        super(npc, delay, player, replayTime);
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        rotating = true;
    }
}
