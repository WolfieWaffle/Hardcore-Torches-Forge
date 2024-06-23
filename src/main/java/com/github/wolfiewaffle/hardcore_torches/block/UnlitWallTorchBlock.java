package com.github.wolfiewaffle.hardcore_torches.block;

import com.github.wolfiewaffle.hardcore_torches.MainMod;
import com.github.wolfiewaffle.hardcore_torches.config.Config;
import com.github.wolfiewaffle.hardcore_torches.util.ETorchState;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleTypes;

public class UnlitWallTorchBlock extends HardcoreWallTorchBlock {
    public static final MapCodec<UnlitWallTorchBlock> CODEC = simpleCodec(UnlitWallTorchBlock::new);

    public UnlitWallTorchBlock(Properties prop) {
        super(prop, null, ETorchState.UNLIT, MainMod.basicTorches, () -> Config.defaultTorchFuel.get());
    }

    public MapCodec<UnlitWallTorchBlock> codec() {
        return CODEC;
    }
}
