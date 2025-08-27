package co.basin.betterbosses.item;

import co.basin.betterbosses.Config;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.core.registries.BuiltInRegistries;

public class LootBagItem extends Item {
    public LootBagItem(Properties properties) {
        super(properties);
    }

    public static void setBoss(ItemStack stack, LivingEntity livingEntity) {
        String encodeId = livingEntity.getEncodeId();
        net.minecraft.world.item.component.CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = custom != null ? custom.copyTag() : new CompoundTag();
        tag.putString("mob", encodeId != null ? encodeId : "");
        stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        CompoundTag nbt = itemStack.get(DataComponents.CUSTOM_DATA) != null ? itemStack.get(DataComponents.CUSTOM_DATA).copyTag() : new CompoundTag();

        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(nbt.getString("mob")));
        if (entityType == null) { return InteractionResultHolder.fail(itemStack); }

        if (!level.isClientSide()) {
            LivingEntity livingEntity = (LivingEntity) entityType.create(level);
            ServerLevel serverLevel = (ServerLevel) level;
            Inventory inventory = player.getInventory();
            for (ItemStack lootStack : createLoot(player, livingEntity, serverLevel)) {
                inventory.placeItemBackInInventory(lootStack);
            }
        }

        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    private ObjectArrayList<ItemStack> createLoot(Player player, LivingEntity livingEntity, ServerLevel serverLevel) {
        if (livingEntity instanceof WitherBoss) {
            // Ensure Wither bag contains a Nether Star at minimum
            return ObjectArrayList.of(new ItemStack(Items.NETHER_STAR));
        }
        ResourceKey<LootTable> resourcelocation = livingEntity.getLootTable();
        LootTable loottable = serverLevel.getServer().reloadableRegistries().getLootTable(resourcelocation);
        LootParams.Builder lootparams$builder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, livingEntity)
                .withParameter(LootContextParams.DAMAGE_SOURCE, serverLevel.damageSources().generic())
                .withParameter(LootContextParams.ORIGIN, player.position());
        return loottable.getRandomItems(lootparams$builder.create(LootContextParamSets.ENTITY), livingEntity.getLootTableSeed());
    }

    @Override
    public Component getName(ItemStack itemStack) {
        CompoundTag nbt = itemStack.get(DataComponents.CUSTOM_DATA) != null ? itemStack.get(DataComponents.CUSTOM_DATA).copyTag() : null;
        if (nbt == null) { return super.getName(itemStack); }
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(nbt.getString("mob")));
        if (entityType == null) { return super.getName(itemStack); }
        return entityType.getDescription().copy().append(" ").append(Component.translatable(this.getDescriptionId(itemStack)));
    }

    public int getColor(ItemStack itemStack, int tintIndex) {
        net.minecraft.world.item.component.CustomData custom = itemStack.get(DataComponents.CUSTOM_DATA);
        if (custom == null) { return -1; }
        CompoundTag nbt = custom.copyTag();
        if (nbt == null) { return -1; }
        String id = nbt.getString("mob");
        if (id == null || id.isEmpty()) { return -1; }
        Tuple<?, ?> colors = Config.lootBagTintsById != null ? Config.lootBagTintsById.get(id) : null;
        int rgb;
        if (colors == null) {
            if ("minecraft:wither".equals(id)) {
                rgb = 0x734F96;
            } else if ("minecraft:ender_dragon".equals(id)) {
                rgb = 0x2E0854;
            } else {
                return -1;
            }
        } else {
            rgb = (int) (tintIndex == 0 ? colors.getA() : colors.getB());
        }
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }
}
