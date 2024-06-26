package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleTypes;

public class BurntWallTorchBlock extends HardcoreWallTorchBlock {
    public static final MapCodec<BurntWallTorchBlock> CODEC = simpleCodec(BurntWallTorchBlock::new);

    public BurntWallTorchBlock(Properties prop) {
        super(prop, null, ETorchState.BURNT, MainMod.basicTorches, () -> Config.defaultTorchFuel.get());
    }

    public MapCodec<BurntWallTorchBlock> codec() {
        return CODEC;
    }
}
