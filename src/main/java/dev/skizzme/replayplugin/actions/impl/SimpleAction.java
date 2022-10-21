package dev.skizzme.replayplugin.actions.impl;

import dev.skizzme.replayplugin.actions.ReplayAction;
import dev.skizzme.replayplugin.ncp.PlayerNPC;
import org.bukkit.entity.Player;

public class SimpleAction extends ReplayAction {

    public ActionType actionType;

    public SimpleAction(ActionType actionType, PlayerNPC npc, long delay, Player player, long replayTime) {
        super(npc, delay, player, replayTime);
        this.actionType = actionType;
    }

    public enum ActionType {
        SWING,
        RESPAWN,
        START_SNEAK,
        STOP_SNEAK,
        FILLER;
    }

}
