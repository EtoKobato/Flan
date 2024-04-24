package io.github.flemmli97.flan.forge.platform;

import io.github.flemmli97.flan.api.forge.PermissionCheckEvent;
import io.github.flemmli97.flan.platform.ClaimPermissionCheck;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;

public class ClaimPermissionCheckImpl implements ClaimPermissionCheck {

    @Override
    public InteractionResult check(ServerPlayer player, ResourceLocation permission, BlockPos pos) {
        PermissionCheckEvent event = new PermissionCheckEvent(player, permission, pos);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getActionResult();
    }
}
