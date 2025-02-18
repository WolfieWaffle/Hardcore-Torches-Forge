package com.github.wolfiewaffle.hardcore_torches.item;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import java.util.List;

public class BandolierItem extends Item {
    private static final String TAG_TORCH = "Torch";
    private static final String TAG_COUNT = "Count";
    private static final String TAG_FUEL = "Fuel";

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
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    public void appendHoverText(ItemStack stack, Level level, List<net.minecraft.network.chat.Component> tooltipComponents, TooltipFlag isAdvanced) {
        Item item = getTorchItemOrAir(stack);
        if (item instanceof TorchItem) tooltipComponents.add(Component.literal(getTorchCount(stack) + " * ").append(Component.translatable(item.getDescriptionId())).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal((int) ((getFuel(stack) / ((float) getTorchCount(stack) * Config.defaultTorchFuel.get())) * 100f) + "% Fuel").withStyle(ChatFormatting.GRAY));
    }

    public static ItemStack getNextTorchOrEmpty(ItemStack bandolierStack) {
        CompoundTag compoundtag = bandolierStack.getOrCreateTag();
        ItemStack stack = ItemStack.EMPTY;

        if (compoundtag.contains(TAG_TORCH)) {
            stack = new ItemStack(getTorchItemOrAir(bandolierStack));
            if (stack.getItem() instanceof TorchItem) {

                // Set fuel
                if (compoundtag.contains(TAG_COUNT)) {
                    stack = TorchItem.setFuel(stack, compoundtag.getInt(TAG_FUEL) / compoundtag.getInt(TAG_COUNT));
                }
            }
        }

        return stack;
    }

    public static int getFuel(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof BandolierItem)) {
            return 0;
        } else {
            CompoundTag nbt = stack.getTag();
            if (nbt != null && nbt.contains(TAG_FUEL)) {
                return nbt.getInt(TAG_FUEL);
            } else {
                return 0;
            }
        }
    }

    public static Item getTorchItemOrAir(ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof BandolierItem) {
            CompoundTag nbt = stack.getTag();
            if (nbt != null && nbt.contains(TAG_TORCH)) {
                return BuiltInRegistries.ITEM.get(new ResourceLocation(nbt.getString(TAG_TORCH)));
            }
        }

        return null;
    }

    private static int getMaxFuel() {
        return Config.bandolierMaxTorches.get() * Config.defaultTorchFuel.get();
    }

    private static int getMaxCount() {
        return Config.bandolierMaxTorches.get();
    }

    private static int getTorchCount(ItemStack stack) {
        CompoundTag compoundtag = stack.getTag();
        if (compoundtag != null) {
            return compoundtag.getInt(TAG_COUNT);
        }
        return 0;
    }

    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
            if (other.isEmpty()) {
                ItemStack takenTorch = getNextTorchOrEmpty(stack);
                if (takenTorch != ItemStack.EMPTY) {
                    access.set(takenTorch.copyWithCount(getTorchCount(stack)));
                    empty(stack);
                    return true;
                }
            } else if (other.getItem() instanceof TorchItem) {
                boolean compatible = false;

                // See if torch is defined
                CompoundTag nbt = stack.getTag();
                if (nbt != null && nbt.contains(TAG_TORCH)) {
                    if (BandolierItem.getTorchItemOrAir(stack) == TorchItem.stateStack(other, ETorchState.UNLIT).getItem()) {
                        compatible = true;
                    }
                } else {
                    compatible = true;
                }

                // Add torch if it's allowed
                if (compatible) {
                    int i = add(stack, other);
                    if (i > 0) {
                        this.playInsertSound(player);
                        other.shrink(i);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    private static int add(ItemStack bandolierStack, ItemStack insertedStack) {
        if (insertedStack.getItem() instanceof TorchItem torchItem) {

            if (!insertedStack.isEmpty() && torchItem.canFitInsideContainerItems()) {
                CompoundTag compoundtag = bandolierStack.getOrCreateTag();

                // Set this bandolier's torch type
                if (!compoundtag.contains(TAG_TORCH)) {
                    compoundtag.putString(TAG_TORCH, torchItem.toString());
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
                    compoundtag.putInt(TAG_COUNT, insertCount + existingCount);
                    compoundtag.putInt(TAG_FUEL, getFuel(bandolierStack) + (insertCount * fuel));
                    return insertCount;
                }
            }
        }

        return 0;
    }

    public static void empty(ItemStack bandolierStack) {
        CompoundTag compoundtag = bandolierStack.getOrCreateTag();

        compoundtag.remove(TAG_TORCH);
        compoundtag.remove(TAG_FUEL);
        compoundtag.remove(TAG_COUNT);
        bandolierStack.setTag(compoundtag);
    }

    public static void deleteOneTorch(ItemStack bandolierStack, ItemStack torchStack) {
        if (torchStack.getItem() instanceof TorchItem) {
            int fuel = TorchItem.getFuel(torchStack);

            CompoundTag compoundtag = bandolierStack.getOrCreateTag();

            int existingCount = getTorchCount(bandolierStack);
            compoundtag.putInt(TAG_COUNT, existingCount - 1);
            compoundtag.putInt(TAG_FUEL, getFuel(bandolierStack) - fuel);
            bandolierStack.setTag(compoundtag);
        }
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }
}
