package io.github.flemmli97.flan.player;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ParticleIndicators;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClaimDisplay {

    private int displayTime;
    private final int height;
    private final Claim toDisplay;
    public final EnumDisplayType type;
    private int[][] poss;

    private int[][] middlePoss;

    private int[] prevDims;

    private final DustParticleEffect corner, middle;

    public ClaimDisplay(Claim claim, EnumDisplayType type, int y) {
        this.toDisplay = claim;
        this.displayTime = ConfigHandler.config.claimDisplayTime;
        this.prevDims = claim.getDimensions();
        this.type = type;
        this.height = y;
        switch (type) {
            case SUB -> {
                this.corner = ParticleIndicators.SUBCLAIMCORNER;
                this.middle = ParticleIndicators.SUBCLAIMMIDDLE;
            }
            case CONFLICT -> {
                this.corner = ParticleIndicators.OVERLAPCLAIM;
                this.middle = ParticleIndicators.OVERLAPCLAIM;
            }
            case EDIT -> {
                this.corner = ParticleIndicators.EDITCLAIMCORNER;
                this.middle = ParticleIndicators.EDITCLAIMMIDDLE;
            }
            default -> {
                this.corner = ParticleIndicators.CLAIMCORNER;
                this.middle = ParticleIndicators.CLAIMMIDDLE;
            }
        }
    }

    public boolean display(ServerPlayerEntity player, boolean remove) {
        if (--this.displayTime % 2 == 0)
            return this.toDisplay.isRemoved();
        int[] dims = this.toDisplay.getDimensions();
        if (this.poss == null || this.changed(dims)) {
            this.middlePoss = calculateDisplayPos(player.getServerWorld(), dims, this.height);
            this.poss = new int[][]{
                    getPosFrom(player.getServerWorld(), dims[0], dims[2], this.height),
                    getPosFrom(player.getServerWorld(), dims[1], dims[2], this.height),
                    getPosFrom(player.getServerWorld(), dims[0], dims[3], this.height),
                    getPosFrom(player.getServerWorld(), dims[1], dims[3], this.height),
            };
        }
        for (int[] pos : this.poss) {
            if (pos[1] != pos[2])
                player.networkHandler.sendPacket(new ParticleS2CPacket(this.corner, true, pos[0] + 0.5, pos[2] + 0.25, pos[3] + 0.5, 0, 0.5f, 0, 0, 1));
            player.networkHandler.sendPacket(new ParticleS2CPacket(this.corner, true, pos[0] + 0.5, pos[1] + 0.25, pos[3] + 0.5, 0, 0.5f, 0, 0, 1));
        }
        if (this.middlePoss != null)
            for (int[] pos : this.middlePoss) {
                if (pos[1] != pos[2])
                    player.networkHandler.sendPacket(new ParticleS2CPacket(this.middle, true, pos[0] + 0.5, pos[2] + 0.25, pos[3] + 0.5, 0, 0.5f, 0, 0, 1));
                player.networkHandler.sendPacket(new ParticleS2CPacket(this.middle, true, pos[0] + 0.5, pos[1] + 0.25, pos[3] + 0.5, 0, 0.5f, 0, 0, 1));
            }
        this.prevDims = dims;
        return this.toDisplay.isRemoved() || (remove && this.displayTime < 0);
    }

    private boolean changed(int[] dims) {
        for (int i = 0; i < dims.length; i++)
            if (dims[i] != this.prevDims[i])
                return true;
        return false;
    }

    public static int[][] calculateDisplayPos(ServerWorld world, int[] from, int height) {
        List<int[]> l = new ArrayList<>();
        Set<Integer> xs = new HashSet<>();
        addEvenly(from[0], from[1], 10, xs);
        xs.add(from[0] + 1);
        xs.add(from[1] - 1);
        Set<Integer> zs = new HashSet<>();
        addEvenly(from[2], from[3], 10, zs);
        zs.add(from[2] + 1);
        zs.add(from[3] - 1);
        for (int x : xs) {
            l.add(getPosFrom(world, x, from[2], height));
            l.add(getPosFrom(world, x, from[3], height));

        }
        for (int z : zs) {
            l.add(getPosFrom(world, from[0], z, height));
            l.add(getPosFrom(world, from[1], z, height));
        }

        return l.toArray(new int[0][]);
    }

    private static void addEvenly(int min, int max, int step, Set<Integer> l) {
        if (max - min < step * 1.5)
            return;
        if (max - min > 0 && max - min <= step * 0.5) {
            l.add(max - step + 1);
            l.add(min + step - 1);
            return;
        }
        l.add(max - step);
        l.add(min + step);
        addEvenly(min + step, max - step, step, l);
    }

    /**
     * Returns an array of of form [x,y1,y2,z] where y1 = height of the lowest replaceable block and y2 = height of the
     * lowest air block above water (if possible)
     */
    public static int[] getPosFrom(ServerWorld world, int x, int z, int maxY) {
        int[] y = nextAirAndWaterBlockFrom(world, x, maxY, z);
        return new int[]{x, y[0], y[1], z};
    }

    private static int[] nextAirAndWaterBlockFrom(ServerWorld world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = world.getBlockState(pos);
        if (state.getMaterial().isReplaceable()) {
            pos = pos.down();
            state = world.getBlockState(pos);
            while (state.getMaterial().isReplaceable() && !world.isOutOfHeightLimit(pos)) {
                pos = pos.down();
                state = world.getBlockState(pos);
            }
            pos = pos.up();
            state = world.getBlockState(pos);
        } else {
            pos = pos.up();
            state = world.getBlockState(pos);
            while (!state.getMaterial().isReplaceable()) {
                pos = pos.up();
                state = world.getBlockState(pos);
            }
        }
        int[] yRet = new int[]{pos.getY(), pos.getY()};
        if (state.getMaterial().isLiquid()) {
            pos = pos.up();
            state = world.getBlockState(pos);
            while (state.getMaterial().isLiquid()) {
                pos = pos.up();
                state = world.getBlockState(pos);
            }
            if (state.getMaterial().isReplaceable())
                yRet[1] = pos.getY();
        }

        return yRet;
    }

    @Override
    public int hashCode() {
        return this.toDisplay.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof ClaimDisplay)
            return this.toDisplay.equals(((ClaimDisplay) obj).toDisplay);
        return false;
    }
}
