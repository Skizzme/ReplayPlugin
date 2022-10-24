package dev.skizzme.replayplugin.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {

    public static ItemStack createItem(Material material, int amount, String displayName, String... loreIn) {
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatUtil.chat(displayName));

        List<String> lore = new ArrayList<>();

        for (String ld : loreIn) {
            lore.add(ChatUtil.chat(ld));
        }

        meta.setLore(lore);

        itemStack.setItemMeta(meta);

        return itemStack;
    }

}
