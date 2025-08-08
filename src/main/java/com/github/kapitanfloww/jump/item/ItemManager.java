package com.github.kapitanfloww.jump.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemManager {

    public static ItemStack getCheckpointItem() {
        final var stack = ItemStack.of(Material.CLOCK, 1);
        final var itemMeta = stack.getItemMeta();
        itemMeta.itemName(Component.text("To Checkpoint", NamedTextColor.GOLD));
        stack.setItemMeta(itemMeta);
        return stack;
    }
}
