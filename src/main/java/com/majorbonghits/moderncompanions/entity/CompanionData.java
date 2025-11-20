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
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Shared data tables (names, skins, foods) brought forward from the original Companions mod.
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
            Items.COOKED_RABBIT
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

    // Male (0) / female (1) skins
    public static final ResourceLocation[][] skins = new ResourceLocation[][]{
            new ResourceLocation[]{
                    tex("textures/entities/male/medieval-man-hugh.png"),
                    tex("textures/entities/male/alexandros.png"),
                    tex("textures/entities/male/cyrus.png"),
                    tex("textures/entities/male/diokles.png"),
                    tex("textures/entities/male/dion.png"),
                    tex("textures/entities/male/georgios.png"),
                    tex("textures/entities/male/ioannis.png"),
                    tex("textures/entities/male/medieval-peasant-schwaechlich.png"),
                    tex("textures/entities/male/medieval-peasant-without-vest.png"),
                    tex("textures/entities/male/medieval-peasant-with-vest-on.png"),
                    tex("textures/entities/male/panos.png"),
                    tex("textures/entities/male/viking-blue-tunic.png"),
                    tex("textures/entities/male/cronos-jojo.png"),
                    tex("textures/entities/male/medieval-man-alard.png"),
                    tex("textures/entities/male/peasant-ginger.png"),
                    tex("textures/entities/male/townsman-green-tunic.png"),
                    tex("textures/entities/male/polish-farmer.png"),
                    tex("textures/entities/male/peasant.png"),
                    tex("textures/entities/male/rustic-farmer.png"),
                    tex("textures/entities/male/medieval-villager.png")
            },
            new ResourceLocation[]{
                    tex("textures/entities/female/a-rogue-i-guess.png"),
                    tex("textures/entities/female/deidre-gramville.png"),
                    tex("textures/entities/female/deidre-gramville2.png"),
                    tex("textures/entities/female/eleora-halle.png"),
                    tex("textures/entities/female/fantastic-blue.png"),
                    tex("textures/entities/female/ftu-emma.png"),
                    tex("textures/entities/female/girl-medieval-peasant.png"),
                    tex("textures/entities/female/medieval-barmaid.png"),
                    tex("textures/entities/female/runaway.png"),
                    tex("textures/entities/female/shannon-flux.png"),
                    tex("textures/entities/female/the-traveller.png"),
                    tex("textures/entities/female/x-ayesha.png")
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

    public static int getHealthModifier() {
        float healthFloat = rand.nextFloat();
        if (healthFloat <= 0.03) return -4;
        if (healthFloat <= 0.1) return -3;
        if (healthFloat <= 0.2) return -2;
        if (healthFloat <= 0.35) return -1;
        if (healthFloat <= 0.65) return 0;
        if (healthFloat <= 0.8) return 1;
        if (healthFloat <= 0.9) return 2;
        if (healthFloat <= 0.97) return 3;
        return 4;
    }

    public static ItemStack getSpawnArmor(EquipmentSlot armorType) {
        float materialFloat = rand.nextFloat();
        if (materialFloat <= 0.40F) {
            return ItemStack.EMPTY;
        } else if (materialFloat <= 0.70F) {
            return switch (armorType) {
                case HEAD -> Items.LEATHER_HELMET.getDefaultInstance();
                case CHEST -> Items.LEATHER_CHESTPLATE.getDefaultInstance();
                case LEGS -> Items.LEATHER_LEGGINGS.getDefaultInstance();
                case FEET -> Items.LEATHER_BOOTS.getDefaultInstance();
                default -> ItemStack.EMPTY;
            };
        } else if (materialFloat <= 0.90F) {
            return switch (armorType) {
                case HEAD -> Items.CHAINMAIL_HELMET.getDefaultInstance();
                case CHEST -> Items.CHAINMAIL_CHESTPLATE.getDefaultInstance();
                case LEGS -> Items.CHAINMAIL_LEGGINGS.getDefaultInstance();
                case FEET -> Items.CHAINMAIL_BOOTS.getDefaultInstance();
                default -> ItemStack.EMPTY;
            };
        } else {
            return switch (armorType) {
                case HEAD -> Items.IRON_HELMET.getDefaultInstance();
                case CHEST -> Items.IRON_CHESTPLATE.getDefaultInstance();
                case LEGS -> Items.IRON_LEGGINGS.getDefaultInstance();
                case FEET -> Items.IRON_BOOTS.getDefaultInstance();
                default -> ItemStack.EMPTY;
            };
        }
    }

    public static String getRandomName(int sex) {
        String firstName = firstNames[sex][rand.nextInt(firstNames[sex].length)];
        String lastName = lastNames[rand.nextInt(lastNames.length)];
        return firstName + " " + lastName;
    }

    public static boolean isArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
    }

    public static boolean isArmorSlot(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            return isArmorSlot(armor.getEquipmentSlot());
        }
        return false;
    }

    public static boolean isBetterArmor(ItemStack candidate, ItemStack current) {
        if (!(candidate.getItem() instanceof ArmorItem candArmor) || !(current.getItem() instanceof ArmorItem curArmor)) {
            return current.isEmpty();
        }
        if (candArmor.getEquipmentSlot() != curArmor.getEquipmentSlot()) {
            return current.isEmpty();
        }
        if (candArmor.getMaterial() == ArmorMaterials.NETHERITE && curArmor.getMaterial() != ArmorMaterials.NETHERITE) {
            return true;
        }
        return candArmor.getDefense() > curArmor.getDefense();
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

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, path);
    }

    // Names source: https://github.com/ironarachne/namegen/blob/main/swedishnames.go
    // male=0, female=1
    public static final String[][] firstNames = new String[][]{
            new String[]{
                    "Abraham", "Adam", "Acke", "Adolf", "Albert", "Albin", "Albrecht", "Alexander", "Alf", "Alfred", "Algot",
                    "Alvar", "Anders", "Andreas", "Arne", "Aron", "Arthur", "Arvid", "Axel", "Bengt", "Bernhard", "Bernt",
                    "Bertil", "Birger", "Bjarne", "Bo", "Bosse", "Bror", "Cai", "Caj", "Carl", "Christer", "Christoffer",
                    "Claes", "Dag", "Daniel", "Danne", "Ebbe", "Eilert", "Einar", "Elias", "Elis", "Elmar", "Elof", "Elov",
                    "Emil", "Emrik", "Enok", "Eric", "Erik", "Erland", "Erling", "Eskil", "Evert", "Folke", "Frans",
                    "Fredrik", "Frej", "Fritiof", "Fritjof", "Gerhard", "Gottfrid", "Greger", "Gunnar", "Gunne", "Gustaf",
                    "Gustav", "Halsten", "Halvar", "Hampus", "Hans", "Harald", "Hasse", "Henrik", "Hilding", "Hjalmar",
                    "Holger", "Inge", "Ingemar", "Ingmar", "Ingvar", "Isac", "Isak", "Ivar", "Jakob", "Jan", "Janne", "Jarl",
                    "Jens", "Jerk", "Jerker", "Joakim", "Johan", "John", "Jon", "Jonas", "Kalle", "Karl", "Kasper", "Kennet",
                    "Kettil", "Kjell", "Klas", "Knut", "Krister", "Kristian", "Kristofer", "Lage", "Lars", "Lasse", "Leif",
                    "Lelle", "Lennart", "Lias", "Loke", "Lorens", "Loui", "Love", "Ludde", "Ludvig", "Magnus", "Markus",
                    "Martin", "Matheo", "Mats", "Matteus", "Mattias", "Mattis", "Matts", "Melker", "Micael", "Mikael",
                    "Milian", "Nicklas", "Niklas", "Nils", "Njord", "Noak", "Ola", "Oliver", "Olle", "Olaf", "Olof", "Olov",
                    "Orvar", "Osvald", "Otto", "Ove", "Patrik", "Peder", "Pehr", "Pelle", "Per", "Peter", "Petter", "Pontus",
                    "Ragnar", "Ragnvald", "Rickard", "Rikard", "Robert", "Roffe", "Samuel", "Sigfrid", "Sigge", "Sigvard",
                    "Sivert", "Sixten", "Staffan", "Stefan", "Stellan", "Stig", "Sune", "Svante", "Sven", "Tage", "Thor",
                    "Thore", "Thorsten", "Thorvald", "Tomas", "Tor", "Tore", "Torgny", "Torkel", "Torsten", "Torvald", "Truls",
                    "Tryggve", "Ture", "Ulf", "Ulrik", "Uno", "Urban", "Valdemar", "Valter", "Verg", "Verner", "Victor",
                    "Vidar", "Vide", "Viggo", "Viktor", "Vilhelm", "Ville", "Vilmar", "Yngve"
            },
            new String[]{
                    "Agda", "Agneta", "Agnetha", "Aina", "Alfhild", "Alicia", "Alva", "Anette", "Anja", "Anneli", "Annika",
                    "Asta", "Astrid", "Barbro", "Bengta", "Berit", "Birgit", "Birgitta", "Bodil", "Brita", "Britt", "Britta",
                    "Cajsa", "Carin", "Carina", "Carita", "Catharina", "Cathrine", "Catrine", "Charlotta", "Christin", "Cilla",
                    "Dagny", "Ebba", "Eira", "Eleonor", "Elin", "Elina", "Ellinor", "Elna", "Elsa", "Elsie", "Embla", "Emelie",
                    "Erica", "Erika", "Erna", "Evy", "Fredrika", "Freja", "Frida", "Gabriella", "Gerd", "Gerda", "Gertrud",
                    "Gittan", "Greta", "Gry", "Gudrun", "Gull", "Gunborg", "Gunda", "Gunhild", "Gunhilda", "Gunilla", "Gunn",
                    "Gunnel", "Gunvor", "Hanna", "Hanne", "Hedda", "Hedvig", "Helga", "Henrika", "Hillevi", "Hilma", "Hulda",
                    "Idun", "Ingeborg", "Ingegerd", "Inger", "Ingrid", "Jannike", "Jennie", "Joline", "Jonna", "Josefin",
                    "Josefina", "Josefine", "Juni", "Kaja", "Kajsa", "Kamilla", "Karin", "Karita", "Karla", "Katja", "Katrin",
                    "Kersti", "Kerstin", "Kia", "Kjerstin", "Klara", "Kristin", "Kristine", "Laila", "Linn", "Linnea", "Lis",
                    "Lisbet", "Lisbeth", "Liselott", "Liselotte", "Liv", "Lo", "Lotta", "Lottie", "Lova", "Lovis", "Lovisa",
                    "Maj", "Maja", "Majken", "Malena", "Malin", "Margaretha", "Margit", "Mari", "Mariann", "Marit", "Marita",
                    "Mathilda", "Meja", "Merit", "Meta", "Mikaela", "Milla", "Milly", "Mimmi", "Minna", "Moa", "Mona", "Nanna",
                    "Nea", "Nellie", "Nelly", "Ottilia", "Pernilla", "Petronella", "Ragna", "Ragnhild", "Rakel", "Rebecka",
                    "Rigmor", "Rika", "Ronja", "Runa", "Rut", "Saga", "Sanna", "Sassa", "Signe", "Sigrid", "Siri", "Siv", "Sofie",
                    "Solveig", "Solvig", "Stina", "Susann", "Susanne", "Svea", "Sylvi", "Tanja", "Tekla", "Terese", "Teresia",
                    "Tessan", "Thea", "Therese", "Thorborg", "Thyra", "Tilde", "Tindra", "Tora", "Torborg", "Tova", "Tove",
                    "Tuva", "Tyra", "Ulla", "Ulrica", "Ulrika", "Vanja", "Vendela", "Vilhelmina", "Viveka", "Vivi", "Ylva"
            }
    };

    public static final String[] lastNames = new String[]{
            "Abrahamsson", "Abramsson", "Adamsson", "Adolfsson", "Adolvsson", "Ahlberg", "Ahlgren", "Albertsson",
            "Albinsson", "Albrechtsson", "Albrecktsson", "Albrektson", "Albrektsson", "Alexanderson", "Alexandersson",
            "Alfredsson", "Alfson", "Alfsson", "Almstedt", "Alvarsson", "Andersson", "Andreasson", "Arthursson",
            "Arvidsson", "Axelsson", "Beck", "Bengtsdotter", "Bengtsson", "Berg", "Berge", "Bergfalk", "Berggren",
            "Berglund", "Bergman", "Bernhardsson", "Berntsson", "Blom", "Blomgren", "Blomqvist", "Borg", "Breiner",
            "Byquist", "Byqvist", "Carlson", "Carlsson", "Claesson", "Dahl", "Dahlman", "Danielsson", "Einarsson", "Ek",
            "Eklund", "Eld", "Eliasson", "Elmersson", "Engberg", "Engman", "Ericson", "Ericsson", "Eriksson", "Falk",
            "Feldt", "Forsberg", "Fransson", "Fredriksson", "Frisk", "Gerhardsson", "Grahn", "Gunnarsson", "Gustafsson",
            "Gustavsson", "Hall", "Hallman", "Hansson", "Haraldsson", "Haroldson", "Henriksson", "Herbertsson",
            "Hermansson", "Hjort", "Holgersson", "Holm", "Holmberg", "Hult", "Ingesson", "Isaksson", "Ivarsson", "Jakobsson",
            "Janson", "Jansson", "Johansson", "Johnsson", "Jonasson", "Jonsson", "Karlsson", "Kjellsson", "Klasson",
            "Knutson", "Knutsson", "Kron", "Lager", "Larson", "Larsson", "Leifsson", "Lennartsson", "Leonardsson", "Lind",
            "Lindbeck", "Lindberg", "Lindgren", "Lindholm", "Lindquist", "Lindqvist", "Ljung", "Ljungborg", "Ljunggren",
            "Ljungman", "Ljungstrand", "Lund", "Lundberg", "Lundgren", "Lundin", "Lundquist", "Lundqvist", "Magnusson",
            "Markusson", "Martin", "Martinsson", "Matsson", "Mattsson", "Mikaelsson", "Niklasson", "Nilsson", "Norling",
            "Nyberg", "Nykvist", "Nylund", "Nyquist", "Nyqvist", "Olander", "Oliversson", "Olofsdotter", "Olofsson", "Olson",
            "Olsson", "Ottosson", "Patriksson", "Persson", "Petersson", "Pettersson", "Pilkvist", "Ragnvaldsson", "Rapp",
            "Rask", "Robertsson", "Rosenberg", "Samuelsson", "Sandberg", "Sigurdsson", "Simonsson", "Solberg", "Sorenson",
            "Stefansson", "Stenberg", "Stendahl", "Stigsson", "Strand", "Sundberg", "Svenson", "Svensson", "Tomasson",
            "Ulfsson", "Victorsson", "Vinter", "Waltersson", "Wang", "Westerberg", "Winter", "Winther", "Wuopio"
    };
}
