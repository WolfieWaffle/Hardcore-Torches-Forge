package com.github.wolfiewaffle.hardcore_torches.loot;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import javax.annotation.Nonnull;

public class ReplaceTorchModifier extends LootModifier {

    public ReplaceTorchModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Nonnull
    @Override
    public ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!Config.replaceInLootTables.get()) return generatedLoot;

        if (!context.getQueriedLootTableId().getPath().substring(0, 7).contains("blocks/")) {
            for (int i = 0; i < generatedLoot.size(); i++) {
                if (generatedLoot.get(i).getItem() == Items.TORCH) {
                    generatedLoot.set(i, new ItemStack(ItemInit.UNLIT_TORCH.get(), generatedLoot.get(i).getCount()));
                } else if (generatedLoot.get(i).getItem() == Items.SOUL_TORCH) {
                    generatedLoot.set(i, new ItemStack(ItemInit.UNLIT_SOUL_TORCH.get(), generatedLoot.get(i).getCount()));
                } else if (generatedLoot.get(i).getItem() == Items.LANTERN) {
                    generatedLoot.set(i, new ItemStack(ItemInit.UNLIT_LANTERN.get(), generatedLoot.get(i).getCount()));
                }
            }
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    public static final MapCodec<ReplaceTorchModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(glm -> glm.conditions)
    ).apply(instance, ReplaceTorchModifier::new));
}
