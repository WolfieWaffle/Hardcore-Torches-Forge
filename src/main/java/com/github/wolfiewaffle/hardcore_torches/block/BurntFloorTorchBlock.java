package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleTypes;

public class BurntFloorTorchBlock extends HardcoreFloorTorchBlock {
    public static final MapCodec<BurntFloorTorchBlock> CODEC = simpleCodec(BurntFloorTorchBlock::new);

    public BurntFloorTorchBlock(Properties prop) {
        super(prop, null, ETorchState.BURNT, MainMod.basicTorches, () -> Config.defaultTorchFuel.get());
    }

    public MapCodec<BurntFloorTorchBlock> codec() {
        return CODEC;
    }
}
