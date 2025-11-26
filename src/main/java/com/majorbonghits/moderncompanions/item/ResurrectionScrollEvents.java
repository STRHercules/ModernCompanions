package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.core.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Hardens Resurrection Scroll item entities so they cannot be destroyed by fire, explosions, or the void.
 */
@EventBusSubscriber(modid = ModernCompanions.MOD_ID)
public final class ResurrectionScrollEvents {
    private ResurrectionScrollEvents() {}

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ItemEntity item)) {
            return;
        }

        ItemStack stack = item.getItem();
        if (!stack.is(ModItems.RESURRECTION_SCROLL.get())) {
            return;
        }

        // One-time reinforcement whenever a scroll item enters a level (drop, load, or transfer).
        stack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE); // native fire immunity for the stack itself
        item.setInvulnerable(true); // blocks explosions and all normal damage sources
        item.setNoGravity(true); // stops sinking through lava or falling into the void
        item.setDeltaMovement(Vec3.ZERO); // freeze any initial toss momentum so it stays put
        item.setUnlimitedLifetime(); // never despawn
        item.clearFire();

        double floor = event.getLevel().getMinBuildHeight() + 0.25D;
        if (item.getY() < floor) {
            item.setPos(item.getX(), floor, item.getZ()); // lift out of the void before the below-world discard fires
        }
    }
}
