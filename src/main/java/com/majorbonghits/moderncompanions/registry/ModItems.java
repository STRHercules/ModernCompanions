package com.majorbonghits.moderncompanions.registry;

import com.majorbonghits.moderncompanions.Constants;
import com.majorbonghits.moderncompanions.struct.WeaponType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Dynamically registers every material/weapon permutation the same way BasicWeapons does.
 */
public final class ModItems {
    private ModItems() {
    }

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);

    private static final Map<WeaponType, List<Supplier<Item>>> ITEMS_BY_TYPE = new EnumMap<>(WeaponType.class);

    private record MaterialEntry(Tier tier, String prefix, UnaryOperator<Item.Properties> settingsModifier) {
        MaterialEntry(Tier tier, String prefix) {
            this(tier, prefix, UnaryOperator.identity());
        }
    }

    private static final Tier BRONZE_TIER = new Tier() {
        @Override
        public int getUses() {
            return 350;
        }

        @Override
        public float getSpeed() {
            return 7.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 2.5F;
        }

        @Override
        public int getEnchantmentValue() {
            return 13;
        }

        @Override
        public net.minecraft.world.item.crafting.Ingredient getRepairIngredient() {
            return net.minecraft.world.item.crafting.Ingredient.EMPTY;
        }

        @Override
        public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
            return net.minecraft.tags.BlockTags.NEEDS_IRON_TOOL;
        }
    };

    private static final List<MaterialEntry> MATERIALS = new ArrayList<>(
        List.of(
            new MaterialEntry(Tiers.WOOD, "wooden"),
            new MaterialEntry(Tiers.STONE, "stone"),
            new MaterialEntry(Tiers.IRON, "iron"),
            new MaterialEntry(Tiers.GOLD, "golden"),
            new MaterialEntry(Tiers.DIAMOND, "diamond"),
            new MaterialEntry(Tiers.NETHERITE, "netherite", props -> props.fireResistant())
        )
    );

    static {
        // Optional bronze support — only registered when the bronze mod is present.
        if (ModList.get().isLoaded("bronze")) {
            MATERIALS.add(new MaterialEntry(BRONZE_TIER, "bronze"));
        }

        for (MaterialEntry material : MATERIALS) {
            registerAllWeaponsForMaterial(material);
        }
    }

    private static void registerAllWeaponsForMaterial(MaterialEntry material) {
        for (WeaponType type : WeaponType.values()) {
            String itemId = material.prefix() + "_" + type.getId();
            Item.Properties properties = material.settingsModifier().apply(new Item.Properties());

            float damageModifier = WeaponType.getDamageModifier(type, material.tier());
            float speedModifier = WeaponType.getSpeedModifier(type, material.tier());
            float reachModifier = WeaponType.getReachModifier(type, material.tier());

            DeferredItem<Item> registered = ITEMS.register(itemId, () -> type.create(material.tier(), damageModifier, speedModifier, reachModifier, properties));
            ITEMS_BY_TYPE.computeIfAbsent(type, k -> new ArrayList<>()).add(registered);
        }
    }

    public static List<Item> getItemsByType(WeaponType type) {
        return ITEMS_BY_TYPE.getOrDefault(type, Collections.emptyList())
            .stream()
            .map(Supplier::get)
            .filter(Objects::nonNull)
            .toList();
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
