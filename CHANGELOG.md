# Modern Companions Changelog

## v1.1.2
* Can now set custom companion skins using /companionskin "NAME" URL | Example: `/companionskin "Daniel George" https://i.imgur.com/FWADR65.png`

## v1.1.3
* Attack/Cast/Eat/Drink Animations fixed

## v1.1.4
* Extended Companion is hungry messages
* Resurrection Scrolls now invulnerable - They resist gravity/fire/lava/explosions and will avoid the void.

## v1.2.0
* Realigned player inventory in Companion GUI
* Added Optional Curio support for Companions
    * Curio visiblity is toggleable
* Added Optional Sophisticated Backpack support/feature
    * If a Companion is wearing a Sophisticated Backpack, they will fill it before their inventory.
* Added better preferred weapon and fallback support.
    * Companions using a preferred weapon type will recieve a +2 bonus to their damage.
    * Companions will now use whatever weapon is available if they do not have their preferred weapon.
    * Hopefully increased Shield support for Vanguards - should equip modded shields now.
    * Fixed bug where Vanguard equipped 2 shields.
* Companion Traits, Backgrounds, Morale, Bonds and additional Stats, and more!
    * Companions now spawn with 1-2 random traits which will grant small bonuses.
    * Companions will now spawn with a small backstory.
    * Companions now have a 'Morale' level - taking care of them raises this while neglecting them will drain it.
    * The more you travel, heal, and revive you companion the stronger their Bond will grow. This will grant benefits to the Companion.
    * There is now an additional button in the Companion GUI that will lead you to a new Biography page.
    * Companions now have ages assigned to them, and will age 1 year every 3 in-game months.
        * Aging is only a visual string in the Bio page.
    * Existing Companions will have missing values (Backstory, Age, Traits) assigned to them.

# v1.3.0
* Introduced Jobs
    * Implemented Jobs
        * Fisher
            * Fisher will path to nearest water, and begin fishing until taken off patrol.
            * Fisher will produce random fishing loot, as if the player were fishing.
        * Miner
            * Miners will catalogue all ores in their work radius, path to them and retrieve them. (Modded ores included!)
            * Miners will break all stone-like blocks, dirt, grass and gravel in their way, collecting the blocks along the way.
        * Lumberjack
            * Lumberjack will path around their work radius and chop down trees, replanting saplings in their place when done (if saplings available in inventory).
            * Lumberjacks will break leaves to access logs.
        * Chef
            * Chefs will take any raw meats placed in their inventory to the nearest Furnace -> Campfire in that order, preferring furnaces if campfires are available and falling back to campfire only when there is no furnace present in their work radius.
            * Chefs will place fuel into the furnace if needed (and present in inventory) such as coal, charcoal, wood.
        * Hunter
            * Hunters will seek out and kill passive mobs within their work radius, collecting their loot.
            * This functions the same as setting them to hunt passive mobs, except while on patrol they have a much larger radius in which they hunt.
    * Courier Functionality
        * Companions with Jobs will deliver their inventory (minus their equipped gear) to their assigned chest.
        * If the assigned chest is not chunk loaded, the Companion will alert you that they cannot reach the area.
        * Optional config to enable all assigned chests to chunk load to prevent misdelivery.
* Improved Patrol Logic
    * Pathing Radius now extends to 128 blocks
    * Shift-Clicking now advances radius by 10
    * Companions with Jobs will only work when on patrol
    * Companions will switch to their desired tool when set to patrol, and back to their weapons when taken off patrol.




BUGS: 

Lumberjacks spin in place when no trees available to chop.