package com.github.wolfiewaffle.hardcore_torches.loot;

import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.JSONUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class FatModifier extends LootModifier {
    private final int[] choices;

    public FatModifier(ILootCondition[] conditions, int[] choices) {
        super(conditions);
        this.choices = choices;
    }

    @Nonnull
    @Override
    public List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        if (!Config.animalsDropFat.get()) return generatedLoot;

        Random random = new Random();
        int num = choices[random.nextInt(choices.length)];
        if (num > 0) generatedLoot.add(new ItemStack(ItemInit.ANIMAL_FAT.get(), num));

        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<FatModifier> {

        @Override
        public FatModifier read(ResourceLocation name, JsonObject object, ILootCondition[] conditions) {
            JsonArray array = JSONUtils.getAsJsonArray(object, "choices");
            int[] choices = new int[array.size()];

            for (int i = 0; i < array.size(); i++) {
                choices[i] = array.get(i).getAsInt();
            }

            return new FatModifier(conditions, choices);
        }

        @Override
        public JsonObject write(FatModifier instance) {
            JsonObject json = makeConditions(instance.conditions);
            JsonArray array = new JsonArray();

            for (int i = 0; i < instance.choices.length; i++) {
                array.add(instance.choices[i]);
            }

            json.add("choices", array);
            return json;
        }
    }
}
