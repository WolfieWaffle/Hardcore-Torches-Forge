package com.github.wolfiewaffle.hardcore_torches.compat.amendments;

import com.github.wolfiewaffle.hardcore_torches.init.ItemInit;
import net.mehvahdjukaar.amendments.client.renderers.LanternRendererExtension;
import net.mehvahdjukaar.amendments.client.renderers.TorchRendererExtension;
import net.mehvahdjukaar.amendments.client.renderers.WallLanternBlockTileRenderer;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonSpecialItemRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class AmendmentsClientCompat {
    public AmendmentsClientCompat() {
    }

    public static void loadClientCompat() {
        LanternRendererExtension lantern_anim = new LanternRendererExtension();
        TorchRendererExtension torch_anim = new TorchRendererExtension();
        IThirdPersonSpecialItemRenderer.attachToItem(ItemInit.LIT_LANTERN.get(), lantern_anim);
        IThirdPersonSpecialItemRenderer.attachToItem(ItemInit.UNLIT_LANTERN.get(), lantern_anim);
        IThirdPersonSpecialItemRenderer.attachToItem(ItemInit.LIT_SOUL_LANTERN.get(), lantern_anim);
        IThirdPersonSpecialItemRenderer.attachToItem(ItemInit.UNLIT_SOUL_LANTERN.get(), lantern_anim);
        IThirdPersonSpecialItemRenderer.attachToItem(ItemInit.LIT_TORCH.get(), torch_anim);
        IThirdPersonSpecialItemRenderer.attachToItem(ItemInit.UNLIT_TORCH.get(), torch_anim);
        IThirdPersonSpecialItemRenderer.attachToItem(ItemInit.SMOLDERING_TORCH.get(), torch_anim);
        IThirdPersonSpecialItemRenderer.attachToItem(ItemInit.BURNT_TORCH.get(), torch_anim);
        IThirdPersonSpecialItemRenderer.attachToItem(ItemInit.LIT_SOUL_TORCH.get(), torch_anim);
        IThirdPersonSpecialItemRenderer.attachToItem(ItemInit.UNLIT_SOUL_TORCH.get(), torch_anim);
        ItemBlockRenderTypes.setRenderLayer(AmendmentsCommonCompat.LIT_WALL_LANTERN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(AmendmentsCommonCompat.UNLIT_WALL_LANTERN.get(), RenderType.cutout());
        BlockEntityRenderers.register(AmendmentsCommonCompat.WALL_LANTERN_BLOCK_ENTITY.get(), WallLanternBlockTileRenderer::new);
    }
}
