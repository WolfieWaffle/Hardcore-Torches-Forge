package com.github.wolfiewaffle.hardcore_torches.loot;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FatModifier extends LootModifier {
    private final List<Integer> choices;

    public FatModifier(LootItemCondition[] conditions, List<Integer> choices) {
        super(conditions);
        this.choices = choices;
    }

    @Nonnull
    @Override
    public ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!Config.animalsDropFat.get()) return generatedLoot;

        Random random = new Random();
        int num = choices.get(random.nextInt(choices.size()));
        if (num > 0) generatedLoot.add(new ItemStack(ItemInit.ANIMAL_FAT.get(), num));

        return generatedLoot;
    }

    public List<Integer> getChoices() {
        List<Integer> list = new ArrayList<>();
        Lists.newArrayList(choices);
        return list;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return MainMod.FAT_MOD_CODEC.get();
    }

    public static final Codec<FatModifier> codec = RecordCodecBuilder.create(instance -> instance.group(
            IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(glm -> glm.conditions),
            Codec.INT.listOf().fieldOf("choices").forGetter(FatModifier::getChoices)
    ).apply(instance, FatModifier::new));
}
