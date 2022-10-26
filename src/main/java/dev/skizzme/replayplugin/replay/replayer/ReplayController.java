package dev.skizzme.replayplugin.replay.replayer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.skizzme.replayplugin.ReplayPlugin;
import dev.skizzme.replayplugin.actions.ReplayAction;
import dev.skizzme.replayplugin.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ReplayController {

    private Replayer replay;
    protected int skipPoint = -1;

    private ItemStack[] viewerInv, viewerArmour;

    public ReplayController(Replayer replay) {
        this.replay = replay;
    }

    public long getCurrentTime(long offset) {
        long time = replay.actions.get(replay.replayIndex).replayTime+offset;
        if (time < 0) time = 0;
        if (time > 10000) time = 10000;
        return time;
    }

    public void play() {
        replay.state = ReplayState.PLAY;
        replay.isPaused = false;
    }

    public void pause() {
        replay.state = ReplayState.PAUSED;
        replay.isPaused = true;
    }

    public void end() {
        replay.state = ReplayState.ENDED;
        replay.runThread.stop();
        replay.queueThread.stop();
        restoreInventory();
        for (Player player : replay.npcMap.keySet()) {
            replay.npcMap.get(player).hideFromPlayers();
            replay.npcMap.remove(player);
        }
        replay.worldLoader.deleteWorld();
    }

    public void handleControl(String itemName) {

        if (itemName.equals("\247cDecrease Speed")) {
            replay.replaySpeed *= 1.25;
        }
        if (itemName.equals("\2479Increase Speed")) {
            replay.replaySpeed /= 1.25;
        }

        replay.viewer.sendMessage("" + getCurrentTime(-2000) + ", " + replay.replayIndex);

        if (itemName.equals("\247aSkip backward (2s)")) {
            ReplayAction closestAction = getActionAtTime(getCurrentTime(-2000));
            skipPoint = replay.actions.indexOf(closestAction);
            replay.state = ReplayState.SKIP_BACKWARD;
        }
        if (itemName.equals("\247aSkip forward (2s)")) {
            ReplayAction closestAction = getActionAtTime(getCurrentTime(2000));
            skipPoint = replay.actions.indexOf(closestAction);
            replay.state = ReplayState.SKIP_FORWARD;
        }

        if (itemName.equals("\247bPause")) {
            this.pause();
            ItemStack stateController = new ItemStack(Material.LIME_DYE, 1, (short) 0);
            ItemMeta meta = stateController.getItemMeta();
            meta.setDisplayName("\247aPlay");
//            replay.state =
            stateController.setItemMeta(meta);
            replay.viewer.getInventory().setItem(4, stateController);
        }
        if (itemName.equals("\247aPlay")) {
            this.play();
            ItemStack stateController = new ItemStack(Material.GRAY_DYE, 1, (short) 0);
            ItemMeta meta = stateController.getItemMeta();
            meta.setDisplayName("\247bPause");
            stateController.setItemMeta(meta);
            replay.viewer.getInventory().setItem(4, stateController);
        }
        System.out.println(itemName + ", " + itemName.equals("\247cExit"));
        if (itemName.equals("\247cExit")) {
            replay.replayController.end();
            ReplayPlugin.INSTANCE.replayManager.removePlayer(replay.viewer);
            this.end();
        }
    }

    public void copyInventory() {
        PlayerInventory inventory = replay.viewer.getInventory();

        viewerInv = inventory.getContents();
        viewerArmour = inventory.getArmorContents();
    }

    public void restoreInventory() {
        PlayerInventory inventory = replay.viewer.getInventory();

        inventory.setContents(viewerInv);
        inventory.setArmorContents(viewerArmour);
    }

    public void giveControlsToViewer() {
        Inventory inventory = replay.viewer.getInventory();
        inventory.clear();

        inventory.setItem(2, getSkull("\247cDecrease Speed", "http://textures.minecraft.net/texture/dcd7c14b92cb37909208a0d204780493f9c9cc5f56d1019b7363417909f1d956"));
        inventory.setItem(3, getSkull("\247aSkip backward (2s)", "http://textures.minecraft.net/texture/a6e1cd0067855b67e0fd5b7eb7457281c41d13bd5bc9158c4a82f518198a1d22"));
        ItemStack stateController = new ItemStack(Material.LIME_DYE, 1, (short) 0);
        ItemMeta meta = stateController.getItemMeta();
        meta.setDisplayName("\247aPlay");
        stateController.setItemMeta(meta);
        inventory.setItem(4, stateController);
        inventory.setItem(5, getSkull("\247aSkip forward (2s)", "http://textures.minecraft.net/texture/db2f30502a8fe4c80e883d23b47389b03a7818d9bbad2ba4dc10d653d3eb52b2"));
        inventory.setItem(6, getSkull("\2479Increase Speed", "http://textures.minecraft.net/texture/cf3821aab0a5abfe7f4937ac28ec8e31a3360cb515c11046ff750ae2a0a391af"));
        inventory.setItem(8, ItemUtil.createItem(Material.BARRIER, 1, "\247cExit"));
    }

    private ItemStack getSkull(String name, String url) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1, (short) 3);

        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try
        {
            profileField = itemMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(itemMeta, profile);
        }
        catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e)
        {
            e.printStackTrace();
        }

        itemMeta.setDisplayName(name);
        item.setItemMeta(itemMeta);
        return item;
    }

    public ReplayAction getActionAtTime(long time) {
        List<ReplayAction> sortedAction = replay.actions.stream().sorted(Comparator.comparingLong(action -> Math.abs(action.replayTime-time))).collect(Collectors.toList());
        return sortedAction.get(0);
    }

    public void handleIndex(int index) {
        if (replay.state == ReplayState.SKIP_BACKWARD || replay.state == ReplayState.SKIP_FORWARD) {
            if (index == skipPoint) {
                replay.state = replay.isPaused ? ReplayState.PAUSED : ReplayState.PLAY;
                skipPoint = -1;
            }
        }
    }

}
