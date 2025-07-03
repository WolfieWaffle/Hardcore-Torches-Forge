package com.github.wolfiewaffle.hardcore_torches.item;

import com.github.wolfiewaffle.hardcore_torches.block.AbstractHardcoreTorchBlock;
import com.github.wolfiewaffle.hardcore_torches.block.AbstractLanternBlock;
import com.github.wolfiewaffle.hardcore_torches.block.HardcoreCampfire;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class FireStarterItem extends Item {
    private static final int USE_DURATION = 72000;

    public FireStarterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
//        BlockPos pos = context.getClickedPos();
//        Level world = context.getLevel();
//        BlockState state = world.getBlockState(pos);
//
//        if (state.getBlock() instanceof HardcoreCampfire campfire) {
//            if (campfire.getFuel(world, pos) <= 0) {
//                if (context.getPlayer() != null) return campfire.needsFuel(context.getPlayer());
//            }
//        } else if (state.getBlock() == Blocks.CAMPFIRE) {
//            if (state.getValue(BlockStateProperties.LIT)) {
//                world.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, true));
//            }
//        }

        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Has a chance to fail").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int number) {
        if (world.isClientSide) return;

        BlockHitResult hit = world.clip(new ClipContext(entity.getEyePosition(), entity.getEyePosition().add(entity.getLookAngle().scale(entity.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE))), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
        BlockPos pos = hit.getBlockPos();
        Block block = world.getBlockState(pos).getBlock();
        boolean attempt = false;
        boolean success;

        // Random chance to fail
        Random random = new Random();
        success = random.nextDouble() < Config.starterSuccessChance.get();

        // Attempt to light
        if (number <= USE_DURATION - 15 && entity instanceof Player) {
            boolean simulateFlintAndSteel = false;

            if (block instanceof HardcoreCampfire campfire && Config.starterLightCampfires.get()) {
                if (campfire.getFuel(world, pos) <= 0) {
                    if (entity != null && entity instanceof Player player) campfire.needsFuel(player);
                } else {
                    attempt = true;
                    if (success) campfire.light(world, pos, world.getBlockState(pos));
                }
            } else if (block instanceof CampfireBlock && Config.starterLightCampfires.get()) {
                attempt = true;
                if (success) simulateFlintAndSteel = true;
            } else if (block instanceof AbstractHardcoreTorchBlock && Config.starterLightTorches.get()) {
                if (((AbstractHardcoreTorchBlock) block).burnState != ETorchState.LIT) {
                    attempt = true;
                    if (success) ((AbstractHardcoreTorchBlock) block).light(world, pos);
                }
            } else if (block instanceof AbstractLanternBlock && Config.starterLightLanterns.get()) {
                if (((AbstractLanternBlock) block).canLight(world, pos)) {
                    attempt = true;
                    if (success) ((AbstractLanternBlock) block).light(world, pos);
                }
            } else if (Config.starterStartFires.get()) {
                attempt = true;
                if (success) simulateFlintAndSteel = true;
            }

            if (simulateFlintAndSteel) Items.FLINT_AND_STEEL.useOn(new UseOnContext((Player) entity, entity.getUsedItemHand(), hit));
        }

        if (attempt) stack.grow(-1);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        player.startUsingItem(hand);

        return super.use(world, player, hand);
    }
}
