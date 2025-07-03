package com.github.wolfiewaffle.hardcore_torches.client;

import com.github.wolfiewaffle.hardcore_torches.HardcoreTorches;
import com.github.wolfiewaffle.hardcore_torches.compat.amendments.AmendmentsClientCompat;
import com.github.wolfiewaffle.hardcore_torches.compat.farmersdelight.FarmersClientCompat;
import com.github.wolfiewaffle.hardcore_torches.init.BlockEntityInit;
import com.github.wolfiewaffle.hardcore_torches.init.BlockInit;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = HardcoreTorches.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistry {

    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(BlockInit.LIT_LANTERN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.UNLIT_LANTERN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.LIT_SOUL_LANTERN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.UNLIT_SOUL_LANTERN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.LIT_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.LIT_WALL_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.UNLIT_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.UNLIT_WALL_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.LIT_SOUL_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.LIT_WALL_SOUL_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.UNLIT_SOUL_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.UNLIT_WALL_SOUL_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.SMOLDERING_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.SMOLDERING_WALL_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.BURNT_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.BURNT_WALL_TORCH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.HARDCORE_CAMPFIRE.get(), RenderType.cutout());
        BlockEntityRenderers.register(BlockEntityInit.CAMPFIRE_BLOCK_ENTITY.get(), CampfireRenderer::new);

        if (ModList.get().isLoaded("amendments")) {
            AmendmentsClientCompat.loadClientCompat();
        }

        if (ModList.get().isLoaded("farmersdelight")) {
            FarmersClientCompat.loadClientCompat();
        }
    }
}