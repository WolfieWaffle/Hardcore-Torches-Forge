package com.github.wolfiewaffle.hardcore_torches.component;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.UnaryOperator;

public class DataTypes {
    public static final DataComponentType<Integer> FUEL = create(
            builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
    );

    public static final DataComponentType<Integer> TORCH_COUNT = create(
            builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
    );

    public static final DataComponentType<Holder<Item>> TORCH_TYPE = create(
            builder -> builder.persistent(ItemStack.ITEM_NON_AIR_CODEC).networkSynchronized(ByteBufCodecs.fromCodec(ItemStack.ITEM_NON_AIR_CODEC))
    );

    private static <T> DataComponentType<T> create(UnaryOperator<DataComponentType.Builder<T>> builder) {
        return builder.apply(DataComponentType.builder()).build();
    }
}
