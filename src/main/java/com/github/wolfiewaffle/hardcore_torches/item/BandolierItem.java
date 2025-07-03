package com.github.wolfiewaffle.hardcore_torches.item;

import com.github.wolfiewaffle.hardcore_torches.component.DataTypes;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import java.util.List;

public class BandolierItem extends Item {

    public BandolierItem(Properties properties) {
        super(properties);
    }

    public boolean isBarVisible(ItemStack stack) {
        return getTorchCount(stack) < getMaxCount() || getFuel(stack) < getMaxFuel();
    }

    public int getBarWidth(ItemStack stack) {
        //int fuel = getFuel(stack);
        //int max = this.getMaxFuel();
        //return max != 0 ? Math.round(13.0F - (float)(max - fuel) * 13.0F / (float)max) : 0;
        int count = getTorchCount(stack);
        int max = getMaxCount();
        return max != 0 ? Math.round(13.0F - (float)(max - count) * 13.0F / (float)max) : 0;
    }

    public int getBarColor(ItemStack stack) {
        //return Color.HSBtoRGB(0.5F, 1.0F, 1.0F);

        float maxFuel = (float) getTorchCount(stack) * Config.defaultTorchFuel.get();
        float f = Math.max(0.0F, (maxFuel - (maxFuel - getFuel(stack))) / maxFuel);

        if (getTorchItemOrAir(stack) instanceof TorchItem torch) {
            if (torch.burnState == ETorchState.BURNT) return Mth.hsvToRgb(0.5F, 0.5F, 0.5F);
        }

        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    public void appendHoverText(ItemStack stack, Level level, List<net.minecraft.network.chat.Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.literal("Right-click to add"));
        tooltipComponents.add(Component.literal("or remove torches"));

        Item item = getTorchItemOrAir(stack);
        if (item instanceof TorchItem) {
            tooltipComponents.add(Component.literal(getTorchCount(stack) + " * ").append(Component.translatable(item.getDescriptionId())).withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.literal((int) Math.ceil((getFuel(stack) / ((float) getTorchCount(stack) * Config.defaultTorchFuel.get())) * 100f) + "% Fuel").withStyle(ChatFormatting.GRAY));
        }
    }

    public static ItemStack getNextTorchOrEmpty(ItemStack bandolierStack, boolean justOne) {
        ItemStack resultStack = ItemStack.EMPTY;

        if (bandolierStack.has(DataTypes.TORCH_TYPE) && bandolierStack.has(DataTypes.TORCH_COUNT)) {

            int count = justOne ? 1 : Math.min(bandolierStack.get(DataTypes.TORCH_COUNT), getTorchItemOrAir(bandolierStack).getDefaultMaxStackSize());
            resultStack = new ItemStack(getTorchItemOrAir(bandolierStack), count);

            if (resultStack.getItem() instanceof TorchItem) {
                // Set fuel
                resultStack = TorchItem.setFuel(resultStack, (bandolierStack.getOrDefault(DataTypes.FUEL, 0)) / bandolierStack.get(DataTypes.TORCH_COUNT));
            }
        }

        return resultStack;
    }

    public static int getFuel(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof BandolierItem)) {
            return 0;
        } else {
            return stack.getOrDefault(DataTypes.FUEL, 0);
        }
    }

    public static Item getTorchItemOrAir(ItemStack stack) {
        Item item = stack.getItem();
        Item output = Items.AIR;

        if (item instanceof BandolierItem) {
            if (BandolierItem.getTorchCount(stack) == 0) return output;

            if (stack.has(DataTypes.TORCH_TYPE)) return stack.get(DataTypes.TORCH_TYPE).value();
        }

        return output;
    }

    private static int getMaxFuel() {
        return Config.bandolierMaxTorches.get() * Config.defaultTorchFuel.get();
    }

    private static int getMaxCount() {
        return Config.bandolierMaxTorches.get();
    }

    private static int getTorchCount(ItemStack stack) {
        return stack.getOrDefault(DataTypes.TORCH_COUNT, 0);
    }

    public boolean overrideOtherStackedOnMe(ItemStack selfStack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
            if (otherStack.isEmpty()) {
                ItemStack takenTorch = getNextTorchOrEmpty(selfStack, false);
                if (takenTorch != ItemStack.EMPTY) {
                    access.set(takenTorch);
                    deleteTorches(selfStack, takenTorch.getCount());
                    return true;
                }
            } else if (otherStack.getItem() instanceof TorchItem otherTorch) {
                boolean compatible = false;

                // See if torch is defined
                if (BandolierItem.getTorchItemOrAir(selfStack) instanceof TorchItem bandoTorch) {

                    // If burnt, only allow burnt
                    if (bandoTorch.burnState == ETorchState.BURNT) {
                        if (otherTorch.burnState == ETorchState.BURNT) compatible = true;
                    }

                    // if lit or unlit, disallow burnt
                    if (bandoTorch.burnState != ETorchState.BURNT) {
                        if (otherTorch.burnState != ETorchState.BURNT) compatible = true;
                    }
                } else {
                    compatible = true;
                }

                // Add torch if it's allowed
                if (compatible) {
                    int i = add(selfStack, otherStack);
                    if (i > 0) {
                        this.playInsertSound(player);
                        otherStack.shrink(i);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    public static void replaceStack(ItemStack bandolierStack, ItemStack insertedStack) {
        if (insertedStack.getItem() instanceof TorchItem torch && bandolierStack.getItem() instanceof BandolierItem bandolier) {
            if (!insertedStack.isEmpty()) {
                bandolierStack.set(DataTypes.TORCH_TYPE, torch.builtInRegistryHolder());
                bandolierStack.set(DataTypes.TORCH_COUNT, insertedStack.getCount());
                bandolierStack.set(DataTypes.FUEL, TorchItem.getFuel(insertedStack) * insertedStack.getCount());
            }
        }
    }

    public static ItemStack getTickedBandolier(ItemStack stack) {
        int count;

        // Set count. If no count, no torches, so return self.
        if (stack.has(DataTypes.TORCH_COUNT)) {
            count = stack.getOrDefault(DataTypes.TORCH_COUNT, 0);
        } else {
            return stack;
        }

        // Containing torches
        ItemStack heldStack = BandolierItem.getNextTorchOrEmpty(stack, true);
        heldStack = heldStack.copyWithCount(count);

        if (!heldStack.isEmpty()) {
            if (heldStack.getItem() instanceof TorchItem torch) {
                heldStack = TorchItem.getTickedStack(heldStack);
                BandolierItem.replaceStack(stack, heldStack);
            }
        }

        return stack;
    }

    private static int add(ItemStack bandolierStack, ItemStack insertedStack) {
        if (insertedStack.getItem() instanceof TorchItem torchItem) {

            if (!insertedStack.isEmpty() && torchItem.canFitInsideContainerItems()) {

                // Set this bandolier's torch type
                if (!bandolierStack.has(DataTypes.TORCH_TYPE)) {
                    bandolierStack.set(DataTypes.TORCH_TYPE, torchItem.builtInRegistryHolder());
                }

                // Ensure space for items
                int existingCount = getTorchCount(bandolierStack);
                int insertCount = Math.min(insertedStack.getCount(), (getMaxCount() - existingCount));
                if (insertCount == 0) {
                    return 0;
                } else {

                    // If space, get fuel
                    int fuel = TorchItem.getFuel(insertedStack);

                    // If space, add items
                    bandolierStack.set(DataTypes.TORCH_COUNT, insertCount + existingCount);
                    bandolierStack.set(DataTypes.FUEL, getFuel(bandolierStack) + (insertCount * fuel));
                    return insertCount;
                }
            }
        }

        return 0;
    }

    public static void empty(ItemStack bandolierStack) {
        bandolierStack.remove(DataTypes.FUEL);
        bandolierStack.remove(DataTypes.TORCH_TYPE);
        bandolierStack.remove(DataTypes.TORCH_COUNT);
    }

    public static void deleteOneTorch(ItemStack bandolierStack, ItemStack torchStack) {
        if (torchStack.getItem() instanceof TorchItem) {
            int fuel = TorchItem.getFuel(torchStack);

            int count = getTorchCount(bandolierStack) - 1;

            if (count > 0) {
                bandolierStack.set(DataTypes.TORCH_COUNT, count);
                bandolierStack.set(DataTypes.FUEL, getFuel(bandolierStack) - fuel);
            } else {
                empty(bandolierStack);
            }
        }
    }

    public static void deleteTorches(ItemStack bandolierStack, int removeCount) {
        ItemStack removeStack = BandolierItem.getNextTorchOrEmpty(bandolierStack, true);

        if (removeStack.isEmpty()) {
            return;
        } else if (removeStack.getItem() instanceof TorchItem) {
            int fuel = TorchItem.getFuel(removeStack) * removeCount;

            int newCount = getTorchCount(bandolierStack) - removeCount;

            if (newCount <= 0) {
                empty(bandolierStack);
            } else {
                bandolierStack.set(DataTypes.FUEL, getFuel(bandolierStack) - fuel);
                bandolierStack.set(DataTypes.TORCH_COUNT, newCount);
            }
        }
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        ItemStack stack1 = oldStack.copy();
        ItemStack stack2 = newStack.copy();

        stack1.remove(DataTypes.FUEL);
        stack2.remove(DataTypes.FUEL);

        return super.shouldCauseReequipAnimation(stack1, stack2, slotChanged);
    }
}
