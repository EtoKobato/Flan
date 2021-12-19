package io.github.flemmli97.flan.player;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.function.BiFunction;

public class TeleportUtils {

    public static BlockPos roundedBlockPos(Vec3d pos) {
        return new BlockPos(pos);
    }

    public static Vec3d getTeleportPos(ServerPlayerEntity player, Vec3d playerPos, ClaimStorage storage, int[] dim, BlockPos.Mutable bPos, BiFunction<Claim, BlockPos, Boolean> check) {
        return getTeleportPos(player, playerPos, storage, dim, false, bPos, check);
    }

    public static Vec3d getTeleportPos(ServerPlayerEntity player, Vec3d playerPos, ClaimStorage storage, int[] dim, boolean checkSub, BlockPos.Mutable bPos, BiFunction<Claim, BlockPos, Boolean> check) {
        Pair<Direction, Vec3d> pos = nearestOutside(dim, playerPos);
        bPos.set(pos.getRight().getX(), pos.getRight().getY(), pos.getRight().getZ());
        Claim claim = storage.getClaimAt(bPos);
        if (checkSub) {
            Claim sub = claim != null ? claim.getSubClaim(bPos) : null;
            if (sub != null)
                claim = sub;
        }
        if (claim == null || check.apply(claim, bPos)) {
            Vec3d ret = pos.getRight();
            BlockPos rounded = roundedBlockPos(ret);
            int y = player.getServerWorld().getChunk(rounded.getX() >> 4, rounded.getZ() >> 4, ChunkStatus.HEIGHTMAPS)
                    .sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, rounded.getX() & 15, rounded.getZ() & 15);
            Vec3d dest = new Vec3d(ret.x, y + 1, ret.z);
            if (player.world.getBlockCollisions(player, player.getBoundingBox().offset(dest.subtract(player.getPos())), (state, p) -> true).allMatch(VoxelShape::isEmpty))
                return dest;
            return new Vec3d(rounded.getX() + 0.5, y + 1, rounded.getZ() + 0.5);
        }
        int[] newDim = claim.getDimensions();
        switch (pos.getLeft()) {
            case NORTH -> dim[2] = newDim[2];
            case SOUTH -> dim[3] = newDim[3];
            case EAST -> dim[1] = newDim[1];
            default -> dim[0] = newDim[0];
        }
        return getTeleportPos(player, playerPos, storage, dim, bPos, check);
    }

    private static Pair<Direction, Vec3d> nearestOutside(int[] dim, Vec3d from) {
        double northDist = Math.abs(from.getZ() - dim[2]);
        double southDist = Math.abs(dim[3] - from.getZ());
        double westDist = Math.abs(from.getX() - dim[0]);
        double eastDist = Math.abs(dim[1] - from.getX());
        if (northDist > southDist) {
            if (eastDist > westDist) {
                if (southDist > westDist)
                    return new Pair<>(Direction.WEST, new Vec3d(dim[0] - 1.5, from.getY(), from.getZ()));
                return new Pair<>(Direction.SOUTH, new Vec3d(from.getX(), from.getY(), dim[3] + 1.5));
            }
            if (southDist > eastDist)
                return new Pair<>(Direction.EAST, new Vec3d(dim[1] + 1.5, from.getY(), from.getZ()));
            return new Pair<>(Direction.SOUTH, new Vec3d(from.getX(), from.getY(), dim[3] + 1.5));
        }
        if (eastDist > westDist) {
            if (northDist > westDist)
                return new Pair<>(Direction.WEST, new Vec3d(dim[0] - 1.5, from.getY(), from.getZ()));
            return new Pair<>(Direction.NORTH, new Vec3d(from.getX(), from.getY(), dim[2] - 1.5));
        }
        if (northDist > eastDist)
            return new Pair<>(Direction.EAST, new Vec3d(dim[1] + 1.5, from.getY(), from.getZ()));
        return new Pair<>(Direction.NORTH, new Vec3d(from.getX(), from.getY(), dim[2] - 1.5));
    }
}
