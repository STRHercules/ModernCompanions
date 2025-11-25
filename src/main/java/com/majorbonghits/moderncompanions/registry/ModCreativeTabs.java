package com.majorbonghits.moderncompanions.registry;

import com.majorbonghits.moderncompanions.Constants;
import com.majorbonghits.moderncompanions.core.ModEnchantments;
import com.majorbonghits.moderncompanions.core.ModItems;
import com.majorbonghits.moderncompanions.struct.WeaponType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Dedicated creative tab for Modern Companions spawn eggs + weapons.
 */
public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("modern_companions",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modern_companions"))
                    .icon(() -> new ItemStack(com.majorbonghits.moderncompanions.core.ModItems.KNIGHT_SPAWN_EGG.get()))
                    .displayItems((params, output) -> {
                        // Spawn eggs first
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.KNIGHT_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.ARCHER_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.ARBALIST_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.AXEGUARD_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.VANGUARD_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.BERSERKER_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.BEASTMASTER_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.CLERIC_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.ALCHEMIST_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.SCOUT_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.STORMCALLER_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.FIRE_MAGE_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.LIGHTNING_MAGE_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.NECROMANCER_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.RESURRECTION_SCROLL.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.COMPANION_MOVER.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.STORED_COMPANION.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.SUMMONING_WAND.get());

                        // Attribute enchantment books (I–III)
                        addEnchantBooks(params, output, ModEnchantments.EMPOWER);
                        addEnchantBooks(params, output, ModEnchantments.NIMBILITY);
                        addEnchantBooks(params, output, ModEnchantments.ENLIGHTENMENT);
                        addEnchantBooks(params, output, ModEnchantments.VITALITY);

                        // Weapon variants grouped by type
                        for (WeaponType type : WeaponType.values()) {
                            com.majorbonghits.moderncompanions.registry.ModItems.getItemsByType(type).forEach(output::accept);
                        }
                    })
                    .build());

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }

    private static void addEnchantBooks(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output,
                                        ResourceKey<net.minecraft.world.item.enchantment.Enchantment> enchantKey) {
        var lookupOpt = params.holders().lookup(Registries.ENCHANTMENT);
        lookupOpt.ifPresent(lookup -> lookup.get(enchantKey).ifPresent(holder -> addBookLevels(output, holder)));
    }

    private static void addBookLevels(CreativeModeTab.Output output, net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> holder) {
        for (int level = 1; level <= 3; level++) {
            output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, level)));
        }
    }
}
