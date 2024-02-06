package com.github.khanshoaib3.minecraft_access.utils;

import com.github.khanshoaib3.minecraft_access.features.point_of_interest.BlockPos3d;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Objects;

/**
 * This class provides delegate calls to {@link ClientPlayerEntity}.
 * The main reason for this class is that {@link ClientPlayerEntity} cannot be mocked by Mockito. <p>
 * ({@link ClientPlayerEntity} constructor requires -> {@link ClientWorld} constructor -> {@link World} static init block -> {@link RegistryKeys},
 * somehow the {@link RegistryKeys} cannot finish its static assignments in class loading.
 * We can replace Mockito with more powerful PowerMock to resolve this problem, but PowerMock is sticking on Junit 4,
 * we can't go back to Junit 4 from 5 since some of the mechanisms currently used for unit testing have no alternatives in 4.
 * Forgive me for doing this, but it's the most economical way.)
 */
public class PlayerUtils {

    public static void playSoundOnPlayer(RegistryEntry.Reference<SoundEvent> sound, float volume, float pitch) {
        WorldUtils.getClientPlayer().orElseThrow().playSound(sound.value(), volume, pitch);
    }

    public static void lookAt(Vec3d position) {
        WorldUtils.getClientPlayer().orElseThrow().lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, position);
    }

    /**
     * Let player looks at entity even the entity exposes a very small part of its body
     * TODO untested
     */
    public static void lookAt(Entity entity) {
        Vec3d playerEyePos = WorldUtils.getClientPlayer().orElseThrow().getEyePos();

        // Try to look at entity's eyes or Enderman's stomach first.
        Vec3d firstPos = entity instanceof EndermanEntity ? entity.getBlockPos().toCenterPos() : entity.getEyePos();
        if (isPlayerCanSee(playerEyePos, firstPos, entity)) {
            lookAt(firstPos);
            return;
        }

        // Then start to find a possible position to target at the entity.
        // This part of code is copied from Explosion.getExposure()
        Box box = entity.getBoundingBox();
        double stepX = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double stepY = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double stepZ = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        // Don't know how these two lengths are determined
        double initX = (1.0 - Math.floor(1.0 / stepX) * stepX) / 2.0;
        double initZ = (1.0 - Math.floor(1.0 / stepZ) * stepZ) / 2.0;
        if (stepX < 0.0 || stepY < 0.0 || stepZ < 0.0) {
            lookAt(firstPos);
            return;
        }

        for (double i = 0.0; i <= 1.0; i += stepX) {
            for (double j = 0.0; j <= 1.0; j += stepY) {
                for (double k = 0.0; k <= 1.0; k += stepZ) {
                    double px = MathHelper.lerp(i, box.minX, box.maxX);
                    double py = MathHelper.lerp(j, box.minY, box.maxY);
                    double pz = MathHelper.lerp(k, box.minZ, box.maxZ);
                    Vec3d vec3d = new Vec3d(px + initX, py, pz + initZ);
                    if (isPlayerCanSee(playerEyePos, vec3d, entity)) {
                        lookAt(vec3d);
                        return;
                    }
                }
            }
        }

        // Make sure to look at entity even the player can't see it.
        lookAt(firstPos);
    }

    private static boolean isPlayerCanSee(Vec3d playerEyePos, Vec3d somewhereOnEntity, Entity entity) {
        BlockHitResult hitResult = entity.getWorld().raycast(
                new RaycastContext(somewhereOnEntity, playerEyePos,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE, entity));
        return hitResult.getType() == HitResult.Type.MISS;
    }

    @SuppressWarnings("unused")
    public static void lookAt(BlockPos position) {
        lookAt(position.toCenterPos());
    }

    public static void lookAt(BlockPos3d position) {
        lookAt(position.getAccuratePosition());
    }

    public static int getExperienceLevel() {
        return WorldUtils.getClientPlayer().map(p -> p.experienceLevel).orElse(-999);
    }

    /**
     * @return percentage-based number
     */
    public static int getExperienceProgress() {
        return WorldUtils.getClientPlayer().map(p -> (int) (p.experienceProgress * 100)).orElse(-999);
    }

    public static boolean isInFluid() {
        ClientPlayerEntity player = WorldUtils.getClientPlayer().orElseThrow();
        return player.isSwimming()
                || player.isSubmergedInWater()
                || player.isInsideWaterOrBubbleColumn()
                || player.isInLava();
    }

    /**
     * The value of MinecraftClient.crosshairTarget field is ray cast result that not including the fluid blocks.
     * So use this method to get what fluid the player might be looking at.
     */
    public static BlockHitResult crosshairFluidTarget(double rayCastDistance) {
        Entity camera = Objects.requireNonNull(MinecraftClient.getInstance().getCameraEntity());
        HitResult hit = camera.raycast(rayCastDistance, 0.0F, true);
        // Whatever the inner values are, they are not used.
        BlockHitResult missed = BlockHitResult.createMissed(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN);

        if (!HitResult.Type.BLOCK.equals(hit.getType())) return missed;

        BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
        ClientWorld world = WorldUtils.getClientWorld().orElseThrow();

        BlockState blockState = world.getBlockState(blockPos);
        boolean thisBlockIsFluidBlock = blockState.isOf(Blocks.WATER) || blockState.isOf(Blocks.LAVA);
        if (!thisBlockIsFluidBlock) return missed;

        FluidState fluidState = world.getFluidState(blockPos);
        if (fluidState.isEmpty()) return missed;

        return (BlockHitResult) hit;
    }
}
