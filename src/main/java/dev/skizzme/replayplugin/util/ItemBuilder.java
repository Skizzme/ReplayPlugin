package dev.skizzme.replayplugin.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Use this to easily create a custom item with enchantments, lore, etc.
 * @apiNote Every method will return the item builder, so you can do everything you need to in one line.
 */
public class ItemBuilder {

    private final ItemStack stack;

    public ItemBuilder(Material material, int amount) {
        this.stack = new ItemStack(material, amount);
    }

    public ItemBuilder(Material material, int amount, String displayName) {
        this.stack = new ItemStack(material, amount);
        this.setDisplayName(displayName);
    }

    /**
     * @return The ItemStack that you have created.
     */
    public ItemStack build() {
        return this.stack;
    }

    public ItemBuilder setDisplayName(String displayName) {
        this.setMeta(meta -> meta.setDisplayName(ChatUtil.chat(displayName)));

        return this;
    }

    public ItemBuilder setLore(String... lore) {
        List<String> loreFormatted = new ArrayList<>();
        for (String line : lore) {
            loreFormatted.add(ChatUtil.chat("&r&f" + line));
        }
        this.setMeta(meta -> meta.setLore(loreFormatted));

        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        this.setMeta(meta -> meta.addEnchant(enchantment, level, true));

        return this;
    }

    public ItemBuilder hideEnchantments() {
        this.setMeta(meta -> meta.addItemFlags(ItemFlag.HIDE_ENCHANTS));

        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag flag) {
        this.setMeta(meta -> meta.addItemFlags(flag));

        return this;
    }

    /**
     * Don't use if item will have other enchantments.
     */
    public ItemBuilder setGlowing() {
        this.setMeta(meta -> meta.addEnchant(this.stack.getType().toString().toLowerCase().contains("helmet") ? Enchantment.ARROW_INFINITE : Enchantment.WATER_WORKER, 1, true));
        this.hideEnchantments();

        return this;
    }

    /**
     * Used to easily set the MetaData of the item.
     * @param editor Example: meta -> meta.setDisplayName("example")
     */
    public ItemBuilder setMeta(Consumer<ItemMeta> editor) {
        ItemMeta meta = this.stack.getItemMeta();
        editor.accept(meta);
        this.stack.setItemMeta(meta);

        return this;
    }

}
