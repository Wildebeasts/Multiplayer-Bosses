package co.basin.betterbosses.client;

import co.basin.betterbosses.MultiplayerBosses;
import co.basin.betterbosses.item.LootBagItem;
import co.basin.betterbosses.item.ModItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

public class Events {
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, i) -> ((LootBagItem) stack.getItem()).getColor(stack, i), ModItems.LOOTBAG.get());
    }
}
