package io.github.flemmli97.flan.forgeevent;

import io.github.flemmli97.flan.event.BlockInteractEvents;
import io.github.flemmli97.flan.event.ItemInteractEvents;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;

public class BlockInteractEventsForge {

    public static void startBreakBlocks(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getWorld() instanceof ServerWorld))
            return;
        if (BlockInteractEvents.startBreakBlocks(event.getPlayer(), event.getWorld(), event.getHand(), event.getPos(), event.getFace()) == ActionResult.FAIL)
            event.setCanceled(true);
    }

    public static void breakBlocks(BlockEvent.BreakEvent event) {
        if (!(event.getWorld() instanceof ServerWorld))
            return;
        if (!BlockInteractEvents.breakBlocks((World) event.getWorld(), event.getPlayer(), event.getPos(), event.getState(), event.getWorld().getBlockEntity(event.getPos())))
            event.setCanceled(true);
    }

    public static void useBlocks(PlayerInteractEvent.RightClickBlock event) {
        ActionResult res = BlockInteractEvents.useBlocks(event.getPlayer(), event.getWorld(), event.getHand(), event.getHitVec());
        if (res == ActionResult.SUCCESS) {
            event.setCancellationResult(res);
            event.setCanceled(true);
            return;
        }
        if (res == ActionResult.FAIL)
            event.setUseBlock(Event.Result.DENY);
        res = ItemInteractEvents.onItemUseBlock(new ItemUsageContext(event.getPlayer(), event.getHand(), event.getHitVec()));
        if (res == ActionResult.FAIL)
            event.setUseItem(Event.Result.DENY);
    }
}
