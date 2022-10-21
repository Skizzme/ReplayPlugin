package dev.skizzme.replayplugin;

import dev.skizzme.replayplugin.replayer.Replayer;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class ReplayManager {

    private HashMap<Player, Replayer> replayers = new HashMap<>();

    public void addPlayer(Player player, Replayer replayer) {
        if (!replayers.containsKey(player)) {
            replayers.put(player, replayer);
        }
    }

    public void removePlayer(Player player) {
        replayers.remove(player);
    }

    public Replayer getReplay(Player player) {
        return replayers.get(player);
    }

    public boolean hasReplay(Player player) {
        return replayers.containsKey(player);
    }

}
