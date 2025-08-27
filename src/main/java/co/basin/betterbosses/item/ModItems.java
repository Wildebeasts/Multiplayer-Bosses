package co.basin.betterbosses.item;

import co.basin.betterbosses.MultiplayerBosses;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MultiplayerBosses.MODID);

    public static final DeferredHolder<Item, LootBagItem> LOOTBAG = ITEMS.register("lootbag", () -> new LootBagItem(new Item.Properties().rarity(Rarity.EPIC)));
}
