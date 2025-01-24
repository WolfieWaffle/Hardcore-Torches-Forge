package com.github.wolfiewaffle.hardcore_torches.compat.farmersdelight;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import vectorwing.farmersdelight.client.renderer.StoveRenderer;

public class FarmersClientCompat {
    public FarmersClientCompat() {
    }

    public static void loadClientCompat() {
        BlockEntityRenderers.register(FarmersCommonCompat.STOVE_BLOCK_ENTITY.get(), StoveRenderer::new);
    }
}
