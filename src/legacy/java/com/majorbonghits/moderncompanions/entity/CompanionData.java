package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.*;

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
            Items.BAKED_POTATO,
            Items.COOKED_SALMON,
            Items.COOKED_COD,
            Items.COOKED_MUTTON,
            Items.COOKED_PORKCHOP,
            Items.COOKED_BEEF,
            Items.COOKED_CHICKEN,
            Items.COOKED_RABBIT
    };

    /** Higher-tier foods/drinks companions can consume for healing but will not request while taming. */
    public static final Item[] EXTRA_HEAL_CONSUMABLES = new Item[]{
            Items.GOLDEN_APPLE,
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.GOLDEN_CARROT,
            Items.HONEY_BOTTLE
    };

    /** Non-food resources companions might demand during taming. */
    public static final Item[] RESOURCE_ITEMS = new Item[] {
            Items.COAL,
            Items.CHARCOAL,
            Items.IRON_INGOT,
            Items.GOLD_INGOT,
            Items.COPPER_INGOT,
            Items.DIAMOND,
            Items.EMERALD,
            Items.LAPIS_LAZULI,
            Items.REDSTONE,
            Items.QUARTZ,
            Items.AMETHYST_SHARD
    };

    private static final Set<Item> DISALLOWED_FOODS = Set.of(
            Items.SPIDER_EYE,
            Items.ROTTEN_FLESH,
            Items.BEEF,
            Items.PORKCHOP,
            Items.CHICKEN,
            Items.MUTTON,
            Items.RABBIT,
            Items.COD,
            Items.SALMON
    );

     public static final MutableComponent[] tameFail = new MutableComponent[]{
            Component.literal("I need more food."),
            Component.literal("Is that all you got?"),
            Component.literal("I'm still hungry."),
            Component.literal("Can I have some more?"),
            Component.literal("I'm going to need a bit more."),
            Component.literal("That's not enough."),
            // extra lines
            Component.literal("You call that a meal?"),
            Component.literal("My stomach didn't even notice that."),
            Component.literal("Nope. Still hungry."),
            Component.literal("I'm going to pretend that never happened. Try again."),
            Component.literal("Nice start. Now add about ten more of those."),
            Component.literal("I appreciate the effort, not the portion size."),
            Component.literal("You're going to have to commit harder than that."),
            Component.literal("That was a snack, not a meal."),
            Component.literal("I'm going to need a lot more if you want my loyalty."),
            Component.literal("My hunger bar barely moved.")
    };

    public static final MutableComponent[] notTamed = new MutableComponent[]{
            Component.literal("Do you have any food?"),
            Component.literal("I'm hungry."),
            Component.literal("Have you seen any food around here?"),
            Component.literal("I could use some food."),
            Component.literal("I wish I had some food."),
            Component.literal("I'm starving."),
            // extra lines
            Component.literal("Got any snacks on you? Asking for a friend. I'm the friend."),
            Component.literal("We could be best friends... if you had food."),
            Component.literal("I'll listen when the food starts talking."),
            Component.literal("You look like someone who carries snacks. Prove me right."),
            Component.literal("No food, no deal."),
            Component.literal("We can talk taming after we talk feeding."),
            Component.literal("Is there a delivery service around here? Preferably you."),
            Component.literal("I'm interviewing humans. Requirement: must bring food."),
            Component.literal("If you had food, this conversation would be going better."),
            Component.literal("Step one: food. Step two: maybe I'll like you.")
    };

    public static final MutableComponent[] WRONG_FOOD = new MutableComponent[]{
            Component.literal("That's not what I asked for."),
            Component.literal("I didn't ask for that."),
            Component.literal("Looks like you didn't understand my request."),
            Component.literal("Did you forget what I asked for?"),
            Component.literal("I don't remember asking for that"),
            // extra lines
            Component.literal("That's… boldly incorrect."),
            Component.literal("Are you even listening to me?"),
            Component.literal("Points for effort, not for accuracy."),
            Component.literal("Close. But also not close at all."),
            Component.literal("This is the opposite of what I wanted."),
            Component.literal("Creative choice. Still wrong, though."),
            Component.literal("Did your inventory slip or was that on purpose?"),
            Component.literal("I'm picky, not desperate."),
            Component.literal("I asked for food, not whatever that is."),
            Component.literal("Try again, but this time use your memory.")
    };

    public static final MutableComponent[] ENOUGH_FOOD = new MutableComponent[]{
            Component.literal("I have enough of that."),
            Component.literal("I don't want that anymore."),
            Component.literal("I want something else now."),
            // extra lines
            Component.literal("If I eat one more of those, I'll explode."),
            Component.literal("Variety would be nice, you know."),
            Component.literal("I am officially bored of that flavor."),
            Component.literal("No more of that, please. My taste buds are on strike."),
            Component.literal("I'm full on that. Emotionally and physically."),
            Component.literal("Do you have literally anything else?"),
            Component.literal("Thanks, but I'm good on those for the next century."),
            Component.literal("My stomach says no. My soul also says no."),
            Component.literal("I get it, you like that item. I don't anymore."),
            Component.literal("Try something new. Surprise me—in a good way.")
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
        Item foodItem = pickAllowedFood(random);
        Item resourceItem = pickResource(random);
        // 2–5 food, 2–6 resource
        food.put(foodItem, random.nextInt(4) + 2);
        food.put(resourceItem, random.nextInt(5) + 2);
        return food;
    }

    public static boolean isFood(ItemStack stack) {
        Item item = stack.getItem();
        if (DISALLOWED_FOODS.contains(item)) return false;
        for (Item food : ALL_FOODS) {
            if (food.equals(item)) return true;
        }
        for (Item bonus : EXTRA_HEAL_CONSUMABLES) {
            if (bonus.equals(item)) return true;
        }
        return isHealingPotion(stack);
    }

    /** Allow only regen/healing potions (no splash/harmful mixes) as valid consumables. */
    public static boolean isHealingPotion(ItemStack stack) {
        if (!(stack.getItem() instanceof PotionItem)) return false;

        boolean hasHealingEffect = false;
        for (MobEffectInstance effect : PotionUtils.getMobEffects(stack)) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                return false;
            }
            if (effect.getEffect() == MobEffects.HEAL || effect.getEffect() == MobEffects.REGENERATION) {
                hasHealingEffect = true;
            }
        }
        return hasHealingEffect;
    }

    private static Item pickAllowedFood(Random random) {
        Item candidate;
        do {
            candidate = ALL_FOODS[random.nextInt(ALL_FOODS.length)];
        } while (DISALLOWED_FOODS.contains(candidate));
        return candidate;
    }

    private static Item pickResource(Random random) {
        return RESOURCE_ITEMS[random.nextInt(RESOURCE_ITEMS.length)];
    }

    private static ResourceLocation tex(String path) {
        return new ResourceLocation(ModernCompanions.MOD_ID, path);
    }

    // English names (American/British)
    // male=0, female=1
    public static final String[][] firstNames = new String[][]{
            new String[]{
                    "Aaron", "Abel", "Abraham", "Adam", "Adrian", "Aidan", "Aiden", "Albert",
                    "Alfred", "Andrew", "Anthony", "Arthur", "Asher", "Austin", "Barrett", "Barry",
                    "Beau", "Benjamin", "Blake", "Bobby", "Brad", "Bradley", "Brandon", "Brent",
                    "Brett", "Brian", "Brody", "Bryan", "Caleb", "Calvin", "Cameron", "Carl",
                    "Carlos", "Casey", "Carter", "Cedric", "Chad", "Charles", "Charlie", "Christian",
                    "Christopher", "Clark", "Clayton", "Clifford", "Cody", "Colby", "Cole", "Colin",
                    "Collin", "Connor", "Conrad", "Corey", "Craig", "Damian", "Damien", "Damon",
                    "Daniel", "Darren", "Darryl", "David", "Dean", "Declan", "Dennis", "Derek",
                    "Derrick", "Desmond", "Devin", "Diego", "Dominic", "Donald", "Donovan", "Douglas",
                    "Drew", "Dustin", "Dylan", "Edward", "Edwin", "Eli", "Elias", "Elijah", "Elliot",
                    "Elliott", "Ethan", "Eugene", "Evan", "Everett", "Felix", "Fernando", "Finley",
                    "Finn", "Francis", "Francisco", "Frank", "Franklin", "Gabriel", "Gage", "Gareth",
                    "Gavin", "George", "Gerald", "Gilbert", "Glen", "Glenn", "Gordon", "Graham",
                    "Grant", "Grayson", "Greg", "Gregory", "Harley", "Harold", "Harrison", "Harry",
                    "Harvey", "Hayden", "Heath", "Hector", "Henry", "Hudson", "Hugh", "Hugo",
                    "Hunter", "Ian", "Isaac", "Isaiah", "Israel", "Jack", "Jackson", "Jacob",
                    "Jaden", "Jake", "James", "Jamie", "Jared", "Jason", "Jasper", "Javier",
                    "Jeff", "Jeffrey", "Jeremiah", "Jeremy", "Jerome", "Jesse", "Jesus", "Joel",
                    "John", "Johnny", "Jonah", "Jonathan", "Jordan", "Jorge", "Jose", "Joseph",
                    "Joshua", "Josiah", "Juan", "Jude", "Julian", "Julio", "Justin", "Kaden",
                    "Kai", "Kaleb", "Karl", "Kayden", "Keith", "Kelvin", "Kenneth", "Kevin",
                    "Kieran", "Kyle", "Landon", "Larry", "Lawrence", "Lee", "Leo", "Leon",
                    "Leonard", "Leroy", "Liam", "Logan", "Lonnie", "Louis", "Luca", "Lucas",
                    "Luis", "Luke", "Malcolm", "Manuel", "Marcus", "Mario", "Mark", "Marshall",
                    "Martin", "Mason", "Mateo", "Matthew", "Maurice", "Max", "Maximilian", "Maxwell",
                    "Micah", "Michael", "Miguel", "Miles", "Mitchell", "Morgan", "Nate", "Nathan",
                    "Nathaniel", "Neil", "Nelson", "Nicholas", "Nico", "Nolan", "Noah", "Norman",
                    "Oliver", "Omar", "Oscar", "Owen", "Parker", "Patrick", "Paul", "Peter",
                    "Philip", "Phillip", "Preston", "Quentin", "Quinn", "Rafael", "Ralph", "Ramon",
                    "Randall", "Randy", "Raphael", "Ray", "Raymond", "Reece", "Reed", "Reid",
                    "Rhys", "Ricardo", "Richard", "Rick", "Ricky", "Riley", "Roberto", "Robert",
                    "Rodney", "Roger", "Roland", "Roman", "Ronald", "Ronnie", "Ross", "Roy",
                    "Russell", "Ryan", "Samuel", "Scott", "Sean", "Sebastian", "Sergio", "Seth",
                    "Shane", "Shaun", "Shawn", "Silas", "Simon", "Spencer", "Stanley", "Stephen",
                    "Steven", "Stuart", "Terrence", "Theodore", "Thomas", "Timothy", "Todd", "Tom",
                    "Travis", "Trevor", "Tristan", "Troy", "Tyler", "Tyrone", "Victor", "Vincent",
                    "Warren", "Wayne", "Wesley", "Weston", "Wilfred", "Will", "William", "Wyatt",
                    "Xavier", "Zach", "Zachariah", "Zachary"
            },
            new String[]{
                    "Abigail", "Ada", "Adelaide", "Adeline", "Aimee", "Alexa", "Alexandra", "Alexis",
                    "Alice", "Alicia", "Alison", "Allison", "Alyssa", "Amelia", "Amelie", "Amy",
                    "Anastasia", "Andrea", "Angela", "Angelica", "Angelina", "Anna", "Annabelle", "Anne",
                    "Annie", "April", "Ariana", "Arianna", "Aria", "Ashley", "Aubrey", "Audrey",
                    "Autumn", "Ava", "Bailey", "Barbara", "Beatrice", "Belinda", "Bella", "Beth",
                    "Bethany", "Bianca", "Brenda", "Brianna", "Bridget", "Britney", "Brooke", "Caitlin",
                    "Camila", "Camille", "Candice", "Cara", "Carla", "Carlie", "Carmen", "Caroline",
                    "Carolyn", "Cassandra", "Catherine", "Cathy", "Cecilia", "Celeste", "Chanel", "Charlotte",
                    "Chelsea", "Chloe", "Christina", "Christine", "Claire", "Clara", "Clarissa", "Courtney",
                    "Crystal", "Cynthia", "Daisy", "Dakota", "Daniella", "Danielle", "Darlene", "Dawn",
                    "Deborah", "Debra", "Delilah", "Diana", "Diane", "Donna", "Dorothy", "Eden",
                    "Edith", "Eileen", "Eleanor", "Elena", "Eliana", "Elinor", "Elisa", "Elise",
                    "Eliza", "Elizabeth", "Ella", "Ellen", "Ellie", "Eloise", "Elsa", "Emily",
                    "Emma", "Erica", "Erin", "Esme", "Estelle", "Esther", "Eva", "Evelyn",
                    "Faith", "Faye", "Felicity", "Fern", "Fiona", "Florence", "Frances", "Francesca",
                    "Freya", "Gabriela", "Gabriella", "Gail", "Georgia", "Georgina", "Gillian", "Gloria",
                    "Grace", "Gwen", "Gwendolyn", "Hailey", "Hannah", "Harper", "Hazel", "Heather",
                    "Heidi", "Helen", "Helena", "Holly", "Hope", "Imogen", "Ingrid", "Irene",
                    "Iris", "Isabel", "Isabella", "Isla", "Ivy", "Jacqueline", "Jade", "Jamie",
                    "Jane", "Janet", "Janice", "Jasmine", "Jean", "Jenna", "Jennifer", "Jessica",
                    "Jillian", "Joan", "Joanna", "Jodie", "Jordan", "Josephine", "Josie", "Joy",
                    "Judith", "Judy", "Julia", "Juliana", "Julie", "Juliet", "June", "Justine",
                    "Karen", "Katherine", "Kathleen", "Katrina", "Kayla", "Keira", "Kelly", "Kelsey",
                    "Kimberly", "Kirsten", "Kristen", "Kristin", "Lacey", "Lana", "Lara", "Laura",
                    "Lauren", "Leah", "Leanne", "Lena", "Lesley", "Lila", "Lillian", "Lily",
                    "Linda", "Lindsey", "Lisa", "Lola", "Loretta", "Lottie", "Louisa", "Louise",
                    "Lucia", "Lucille", "Lucy", "Luna", "Lydia", "Mackenzie", "Macy", "Madeline",
                    "Madison", "Mae", "Maeve", "Maggie", "Maisie", "Mandy", "Margaret", "Margot",
                    "Maria", "Mariah", "Mariam", "Marian", "Marilyn", "Marina", "Martha", "Mary",
                    "Matilda", "Maya", "Megan", "Melanie", "Melissa", "Mia", "Michelle", "Mila",
                    "Molly", "Monica", "Morgan", "Naomi", "Natalia", "Natalie", "Natasha", "Niamh",
                    "Nicole", "Nicola", "Nina", "Noelle", "Nora", "Norah", "Olivia", "Paige",
                    "Pamela", "Patricia", "Paula", "Penelope", "Phoebe", "Poppy", "Priscilla", "Rachel",
                    "Rebecca", "Reese", "Riley", "Rita", "Robyn", "Rosa", "Rosalie", "Rose",
                    "Rosie", "Ruby", "Ruth", "Sabrina", "Samantha", "Sandra", "Sara", "Sarah",
                    "Savannah", "Scarlett", "Selena", "Serena", "Shannon", "Sharon", "Sheila", "Shelby",
                    "Sienna", "Sierra", "Simone", "Sofia", "Sophia", "Sophie", "Stacey", "Stella",
                    "Stephanie", "Summer", "Susan", "Suzanne", "Sydney", "Tara", "Tessa", "Theresa",
                    "Tiffany", "Tracy", "Trinity", "Valentina", "Valerie", "Vanessa", "Vera", "Veronica",
                    "Victoria", "Violet", "Vivian", "Wendy", "Whitney", "Willow", "Yasmin", "Yvonne",
                    "Zara", "Zoe", "Zoey"
            }
    };

    public static final String[] lastNames = new String[]{
            "Adams", "Ainsworth", "Alexander", "Allen", "Anderson", "Andrews", "Armstrong", "Arnold",
            "Atkins", "Atkinson", "Austin", "Bailey", "Baker", "Ball", "Banks", "Barber",
            "Barker", "Barnes", "Barnett", "Barrett", "Barry", "Bates", "Baxter", "Beck",
            "Bell", "Bennett", "Benson", "Bentley", "Berry", "Black", "Blake", "Booth",
            "Bowen", "Boyd", "Bradley", "Brady", "Brewer", "Bridges", "Briggs", "Brooks",
            "Brown", "Bryant", "Buckley", "Bullock", "Burke", "Burnett", "Burns", "Burton",
            "Bush", "Butler", "Byrne", "Campbell", "Carlson", "Carpenter", "Carr", "Carroll",
            "Carter", "Casey", "Chambers", "Chapman", "Chandler", "Christensen", "Clark", "Clarke",
            "Clayton", "Cobb", "Cohen", "Cole", "Coleman", "Collins", "Conner", "Cook",
            "Cooper", "Curtis", "Cox", "Craig", "Crawford", "Cross", "Cruz", "Cunningham",
            "Curtis", "Dalton", "Daniel", "Daniels", "Davidson", "Davis", "Dawson", "Day",
            "Dean", "Delaney", "Dennis", "Dixon", "Douglas", "Doyle", "Duncan", "Dunn",
            "Edwards", "Elliott", "Ellis", "Erickson", "Eriksen", "Evans", "Farrell", "Ferguson",
            "Fernandez", "Fisher", "Fitzgerald", "Fleming", "Fletcher", "Flores", "Ford", "Foster",
            "Fowler", "Fox", "Francis", "Franklin", "Freeman", "Gallagher", "Gardner", "Garner",
            "Garcia", "Garrison", "George", "Gibbs", "Gibson", "Gilbert", "Gill", "Glover",
            "Gonzalez", "Goodman", "Gordon", "Graham", "Grant", "Graves", "Gray", "Green",
            "Greene", "Gregory", "Griffin", "Griffiths", "Hall", "Hamilton", "Hansen", "Hanson",
            "Harper", "Harris", "Harrison", "Hart", "Harvey", "Hawkins", "Hayes", "Haynes",
            "Henderson", "Henry", "Hernandez", "Hicks", "Hill", "Hines", "Hodges", "Hoffman",
            "Holland", "Holmes", "Holt", "Hopkins", "Horton", "Howard", "Howe", "Hudson",
            "Hughes", "Hunt", "Hunter", "Ingram", "Jackson", "Jacobs", "James", "Jarvis",
            "Jenkins", "Jennings", "Jensen", "Johnson", "Johnston", "Jones", "Jordan", "Kane",
            "Keller", "Kelley", "Kelly", "Kennedy", "Khan", "King", "Kirk", "Klein",
            "Knight", "Lambert", "Lane", "Lang", "Lawrence", "Lawson", "Leach", "Lee",
            "Lewis", "Little", "Lloyd", "Logan", "Long", "Lopez", "Lowe", "Lucas",
            "Lynch", "Lyons", "MacDonald", "Madden", "Manning", "Marks", "Marsh", "Marshall",
            "Martin", "Martinez", "Mason", "Matthews", "Maxwell", "May", "McBride", "McCarthy",
            "McCormick", "McDonald", "McGee", "McGrath", "McGregor", "McKenzie", "McLean", "McMillan",
            "Medina", "Mendez", "Meyer", "Miller", "Mills", "Mitchell", "Moody", "Moore",
            "Morales", "Morgan", "Morris", "Morrison", "Morton", "Moss", "Murphy", "Murray",
            "Myers", "Nelson", "Newman", "Newton", "Nichols", "Nicholson", "Nixon", "Nolan",
            "Norman", "Norris", "O'Brien", "O'Connor", "O'Neill", "Oliver", "Olson", "Ortiz",
            "Owens", " Page", "Palmer", "Parker", "Patel", "Patrick", "Patterson", "Payne",
            "Pearce", "Pearson", "Pena", "Perez", "Perkins", "Perry", "Peters", "Peterson",
            "Phillips", "Pierce", "Poole", "Porter", "Potter", "Powell", "Powers", "Price",
            "Quinn", "Ramirez", "Ramos", "Randall", "Ray", "Reed", "Rees", "Reese",
            "Reid", "Reyes", "Reynolds", "Rhodes", "Rice", "Richards", "Richardson", "Riley",
            "Rivers", "Robbins", "Roberts", "Robertson", "Robinson", "Rodgers", "Rodriguez", "Rogers",
            "Rose", "Ross", "Rowe", "Ruiz", "Russell", "Ryan", "Salazar", "Sanders",
            "Sanderson", "Sandoval", "Santiago", "Saunders", "Schmidt", "Scott", "Sharp", "Shaw",
            "Sheffield", "Shelton", "Short", "Silva", "Simmons", "Simpson", "Singh", "Sloan",
            "Smith", "Snyder", "Spencer", "Stanley", "Stephens", "Stevens", "Stewart", "Stone",
            "Sullivan", "Summers", "Sutton", "Taylor", "Terry", "Thomas", "Thompson", "Thornton",
            "Todd", "Torres", "Townsend", "Tran", "Tucker", "Turner", "Tyler", "Vasquez",
            "Vaughn", "Vazquez", "Wade", "Wagner", "Walker", "Wallace", "Walsh", "Walters",
            "Ward", "Warren", "Washington", "Waters", "Watkins", "Watson", "Watts", "Weaver",
            "Webb", "Weber", "Welch", "Wells", "West", "Wheeler", "White", "Whitaker",
            "Whitehead", "Whitfield", "Williams", "Williamson", "Willis", "Wilson", "Wise", "Wolfe",
            "Wong", "Wood", "Woods", "Wright", "Wyatt", "Young", "Zimmerman"
    };
}
