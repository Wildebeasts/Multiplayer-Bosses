package co.basin.betterbosses;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.event.config.ModConfigEvent;

import java.util.*;
import java.util.stream.Collectors;
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue SHOULD_SCALE_BOSS_HEALTH = BUILDER
            .comment("If boss health should be scaled")
            .define("Should Scale Boss Health", true);

    private static final ModConfigSpec.BooleanValue SHOULD_SCALE_BOSS_DROPS = BUILDER
            .comment("If boss drops should be rolled an additional time per player")
            .define("Should Scale Boss Drops", true);

    private static final ModConfigSpec.DoubleValue HEALTH_MULTIPLIER_PER_PLAYER = BUILDER
            .comment("The amount to scale boss health per player after the first")
            .defineInRange("Health Multiplier Per Player", 1.0, 0.0, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue FLAT_HEALTH_MULTIPLIER = BUILDER
            .comment("A flat value to increase boss health. Will override \"Health Multiplier Per Player\" if it is not 0")
            .defineInRange("Flat Health Multiplier", 0.0, 0.0, Double.MAX_VALUE);

    private static final ModConfigSpec.IntValue FLAT_DROPS_MULTIPLIER = BUILDER
            .comment("A flat value to increase boss. If this value is not 0 player count will be ignored")
            .defineInRange("Flat Drop Multiplier", 0, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue SHOULD_USE_FORGE_TAGS = BUILDER
            .comment("Whether to use the forge tag \"forge:bosses\" to detect bosses")
            .define("Should Use Forge Tags", true);

    private static final ModConfigSpec.BooleanValue SHOULD_DROP_LOOT_BAGS = BUILDER
            .comment("Whether to drop loot bags filled with boss items or just drop the plain items")
            .define("Should Drop Loot Bags", true);

    private static final ModConfigSpec.BooleanValue USE_PROXIMITY_SCALING = BUILDER
            .comment("Whether to scale health and loot off of the number of players in the configured range or use the global player count")
            .define("Use Proximity Scaling", true);

    private static final ModConfigSpec.IntValue PROXIMITY_SCALING_RANGE = BUILDER
            .comment("How far from a boss spawn position to detect players")
            .defineInRange("Proximity Scaling Range", 100, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> BOSS_NAMES = BUILDER
            .comment("All bosses that should be affected by this mod. Most are usually in the \"forge:bosses\" tag")
            .defineList("Boss Entities", List.of(
                    "minecraft:wither",
                    "minecraft:ender_dragon",
                    "cataclysm:ancient_remnant",
                    "cataclysm:ignis",
                    "cataclysm:maledictus",
                    "cataclysm:ender_golem",
                    "cataclysm:ender_guardian",
                    "cataclysm:the_leviathan",
                    "cataclysm:the_harbinger",
                    "cataclysm:netherite_monstrosity",
                    "bosses_of_mass_destruction:void_blossom",
                    "bosses_of_mass_destruction:gauntlet",
                    "bosses_of_mass_destruction:lich",
                    "bosses_of_mass_destruction:obsidilith"
            ), Config::validateString);

    private static final ModConfigSpec.ConfigValue<List<? extends List<? extends String>>> LOOT_BAG_TINTS = BUILDER
            .comment("Tint colors used for loot bags dropped from bosses. Uses minecrafts integer encoded rgb format. Use -1 for no tint")
            .defineList("Loot Bag Tints", List.of(
                    List.of("minecraft:wither", "7561558", "13882367"),
                    List.of("minecraft:ender_dragon", "2171169", "9830655"),
                    List.of("cataclysm:ancient_remnant", "15789718", "16760084"),
                    List.of("cataclysm:ignis", "5384240", "4766957"),
                    List.of("cataclysm:maledictus", "10387251", "2220943"),
                    List.of("cataclysm:ender_golem", "2363210", "7217407"),
                    List.of("cataclysm:ender_guardian", "12905869", "1049638"),
                    List.of("cataclysm:the_leviathan", "1050911", "6690047"),
                    List.of("cataclysm:the_harbinger", "13617352", "12523030"),
                    List.of("cataclysm:netherite_monstrosity", "3092275", "7995392"),
                    List.of("bosses_of_mass_destruction:void_blossom", "2511372", "4653074"),
                    List.of("bosses_of_mass_destruction:lich", "2700357", "6984916"),
                    List.of("bosses_of_mass_destruction:obsidilith", "328201", "3089731")
            ), Config::validateLootbagTints);

    private static boolean validateLootbagTints(final Object obj) {
        return obj instanceof final List<?> list && list.size() == 3;
    }

    private static boolean validateString(final Object obj) {
        return obj instanceof String;
    }

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean shouldScaleBossHealth;
    public static boolean shouldScaleBossDrops;
    public static double multiplierPerPlayer;
    public static double flatHealthMultiplier;
    public static int flatDropsMultiplier;
    public static boolean shouldUseForgeTags;
    public static boolean shouldDropLootBags;
    public static boolean useProximityScaling;
    public static int proximityScalingRange;
    public static List<EntityType<?>> bosses;

    public static Map<EntityType<?>, Tuple<Integer, Integer>> lootBagTints;
    public static Map<String, Tuple<Integer, Integer>> lootBagTintsById;

    public static void bake()
    {
        shouldScaleBossHealth = SHOULD_SCALE_BOSS_HEALTH.get();
        multiplierPerPlayer = HEALTH_MULTIPLIER_PER_PLAYER.get();
        shouldScaleBossDrops = SHOULD_SCALE_BOSS_DROPS.get();
        flatHealthMultiplier = FLAT_HEALTH_MULTIPLIER.get();
        flatDropsMultiplier = FLAT_DROPS_MULTIPLIER.get();
        shouldUseForgeTags = SHOULD_USE_FORGE_TAGS.get();
        shouldDropLootBags = SHOULD_DROP_LOOT_BAGS.get();
        useProximityScaling = USE_PROXIMITY_SCALING.get();
        proximityScalingRange = PROXIMITY_SCALING_RANGE.get();

        bosses = BOSS_NAMES.get().stream()
                .map(ResourceLocation::parse)
                .map(BuiltInRegistries.ENTITY_TYPE::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        lootBagTints = LOOT_BAG_TINTS.get().stream()
                .filter((list) -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse((String) list.get(0))) != null)
                .collect(Collectors.toMap(
                        (list) -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse((String) list.get(0))),
                        (list) -> new Tuple<>(Integer.parseInt((String) list.get(1)), Integer.parseInt((String) list.get(2))),
                        (existing, replacement) -> replacement
                ));

        // Also keep a string-keyed map for client tint lookup without registry access timing issues
        lootBagTintsById = LOOT_BAG_TINTS.get().stream()
                .collect(Collectors.toMap(
                        (list) -> (String) list.get(0),
                        (list) -> new Tuple<>(Integer.parseInt((String) list.get(1)), Integer.parseInt((String) list.get(2))),
                        (existing, replacement) -> replacement
                ));
    }
}
