package dev.skizzme.replayplugin.actions.impl;

import dev.skizzme.replayplugin.actions.ReplayAction;
import dev.skizzme.replayplugin.npc.PlayerNPC;
import io.github.retrooper.packetevents.utils.player.Direction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BlockPlaceAction extends ReplayAction {

    public Material material;
    public Location location;
    public Direction direction;

    public BlockPlaceAction(Material material, Location location, Direction direction, PlayerNPC npc, long delay, Player player, long replayTime) {
        super(npc, delay, player, replayTime);
        this.material = material;
        this.location = location;
        this.direction = direction;
    }
}
