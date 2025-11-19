package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Data tables mirrored from the original mod (dialog, skin pools, food lists).
 */
public class CompanionData {
    public static final Random rand = new Random();

    public static final Item[] ALL_FOODS = new Item[]{
            Items.COOKIE,
            Items.BREAD,
            Items.MELON_SLICE,
            Items.APPLE,
            Items.SWEET_BERRIES,
            Items.CARROT,
            Items.POTATO,
            Items.BAKED_POTATO,
            Items.COOKED_SALMON,
            Items.COOKED_COD,
            Items.SALMON,
            Items.COD,
            Items.COOKED_MUTTON,
            Items.COOKED_PORKCHOP,
            Items.COOKED_BEEF,
            Items.COOKED_CHICKEN,
            Items.COOKED_RABBIT,
    };

    public static final MutableComponent[] tameFail = new MutableComponent[]{
            Component.literal("I need more food."),
            Component.literal("Is that all you got?"),
            Component.literal("I'm still hungry."),
            Component.literal("Can I have some more?"),
            Component.literal("I'm going to need a bit more."),
            Component.literal("That's not enough."),
    };

    public static final MutableComponent[] notTamed = new MutableComponent[]{
            Component.literal("Do you have any food?"),
            Component.literal("I'm hungry."),
            Component.literal("Have you seen any food around here?"),
            Component.literal("I could use some food."),
            Component.literal("I wish I had some food."),
            Component.literal("I'm starving."),
    };

    public static final MutableComponent[] WRONG_FOOD = new MutableComponent[]{
            Component.literal("That's not what I asked for."),
            Component.literal("I didn't ask for that."),
            Component.literal("Looks like you didn't understand my request."),
            Component.literal("Did you forget what I asked for?"),
            Component.literal("I don't remember asking for that")
    };

    public static final MutableComponent[] ENOUGH_FOOD = new MutableComponent[]{
        Component.literal("I have enough of that."),
        Component.literal("I don't want that anymore."),
        Component.literal("I want something else now."),
    };

    public static final Class<?>[] alertMobs = new Class<?>[]{
            Blaze.class,
            EnderMan.class,
            Endermite.class,
            Ghast.class,
            Giant.class,
            Guardian.class,
            Hoglin.class,
            MagmaCube.class,
            Phantom.class,
            Shulker.class,
            Silverfish.class,
            Slime.class,
            Spider.class,
            Vex.class,
            AbstractSkeleton.class,
            Zoglin.class,
            Zombie.class,
            Raider.class
    };

    public static final Class<?>[] huntMobs = new Class<?>[]{
            Chicken.class,
            Cow.class,
            MushroomCow.class,
            Pig.class,
            Rabbit.class,
            Sheep.class
    };

    // skins[0] == male, skins[1] == female
    public static final ResourceLocation[][] skins = new ResourceLocation[][]{
            new ResourceLocation[]{
                    tex("textures/entities/male/medieval-man-hugh.png"),
                    tex("textures/entities/male/alexandros.png"),
                    tex("textures/entities/male/cyrus.png"),
                    tex("textures/entities/male/diokles.png"),
                    tex("textures/entities/male/dion.png"),
                    tex("textures/entities/male/henry.png"),
                    tex("textures/entities/male/maharbal.png"),
                    tex("textures/entities/male/sigurd.png"),
                    tex("textures/entities/male/man3.png"),
                    tex("textures/entities/male/man4.png"),
                    tex("textures/entities/male/man5.png"),
                    tex("textures/entities/male/man6.png"),
                    tex("textures/entities/male/man7.png"),
            },
            new ResourceLocation[]{
                    tex("textures/entities/female/ariss.png"),
                    tex("textures/entities/female/gema.png"),
                    tex("textures/entities/female/kiwi.png"),
                    tex("textures/entities/female/lacy.png"),
                    tex("textures/entities/female/medieval-woman-ava.png"),
                    tex("textures/entities/female/metella.png"),
                    tex("textures/entities/female/muriel.png"),
                    tex("textures/entities/female/narcissa.png"),
                    tex("textures/entities/female/silvia.png")
            }
    };

    public static final ResourceLocation[][] maleArmor = new ResourceLocation[][]{
            new ResourceLocation[]{
                    tex("textures/entities/armor/chainmail_arms_layer_2.png"),
                    tex("textures/entities/armor/chainmail_arms_layer_1.png")
            },
            new ResourceLocation[]{
                    tex("textures/entities/armor/iron_arms_layer_2.png"),
                    tex("textures/entities/armor/iron_arms_layer_1.png")
            },
            new ResourceLocation[]{
                    tex("textures/entities/armor/steel_layer_2.png"),
                    tex("textures/entities/armor/steel_layer_1.png")
            }
    };

    public static final ResourceLocation[][] femaleArmor = new ResourceLocation[][]{
            new ResourceLocation[]{
                    tex("textures/entities/armor/chainmail_layer_2.png"),
                    tex("textures/entities/armor/chainmail_layer_1.png")
            },
            new ResourceLocation[]{
                    tex("textures/entities/armor/iron_layer_2.png"),
                    tex("textures/entities/armor/iron_layer_1.png")
            },
            new ResourceLocation[]{
                    tex("textures/entities/armor/steel_layer_2.png"),
                    tex("textures/entities/armor/steel_layer_1.png")
            }
    };

    public static boolean isArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
    }

    public static boolean isArmorSlot(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            return isArmorSlot(armor.getEquipmentSlot());
        }
        return false;
    }

    public static Map<Item, Integer> getRandomFoodRequirement(Random random) {
        Map<Item, Integer> food = new HashMap<>();
        Item food1 = ALL_FOODS[random.nextInt(ALL_FOODS.length)];
        Item food2 = ALL_FOODS[random.nextInt(ALL_FOODS.length)];
        while (food1.equals(food2)) {
            food2 = ALL_FOODS[random.nextInt(ALL_FOODS.length)];
        }
        food.put(food1, random.nextInt(5) + 1);
        food.put(food2, random.nextInt(5) + 1);
        return food;
    }

    public static boolean isFood(Item item) {
        for (Item food : ALL_FOODS) {
            if (food.equals(item)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBetterArmor(ItemStack itemStack, ItemStack currentArmor) {
        return currentArmor.isEmpty() || itemStack.getMaxDamage() > currentArmor.getMaxDamage();
    }

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, path);
    }
}
