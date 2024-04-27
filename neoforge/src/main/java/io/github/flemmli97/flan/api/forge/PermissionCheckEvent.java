package io.github.flemmli97.flan.api.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.Event;

public class PermissionCheckEvent extends Event {

    public final ServerPlayer player;
    public final ResourceLocation permission;
    public final BlockPos pos;
    private InteractionResult result = InteractionResult.PASS;

    /**
     * Event for when permissions are checked
     *
     * @param player     The corresponding player. Can be null if the check is e.g. caused by tnt explosions
     * @param permission The permission to check
     * @param pos        The block pos where the action is occuring
     */
    public PermissionCheckEvent(ServerPlayer player, ResourceLocation permission, BlockPos pos) {
        this.player = player;
        this.permission = permission;
        this.pos = pos;
    }

    /**
     * @return ActionResult#PASS to do nothing. ActionResult#FAIL to prevent the action. Else to allow the action
     */
    public InteractionResult getActionResult() {
        return this.result;
    }

    public void setResult(InteractionResult result) {
        this.result = result;
    }
}
