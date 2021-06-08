package io.github.flemmli97.flan.claim;

import io.github.flemmli97.flan.api.ClaimPermission;
import io.github.flemmli97.flan.api.PermissionRegistry;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class GlobalClaim implements IPermissionContainer {

    private final ServerWorld world;

    public GlobalClaim(ServerWorld world) {
        this.world = world;
    }

    @Override
    public boolean canInteract(ServerPlayerEntity player, ClaimPermission perm, BlockPos pos, boolean message) {
        Boolean global = ConfigHandler.config.getGlobal(this.world, perm);
        if (global != null) {
            if (global)
                return true;
            if (message)
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.noPermissionSimple, Formatting.DARK_RED), true);
            return false;
        }
        if (perm == PermissionRegistry.MOBSPAWN)
            return false;
        return true;
    }
}
