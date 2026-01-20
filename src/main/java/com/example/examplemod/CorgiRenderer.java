package com.example.examplemod;

import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.ResourceLocation;

public final class CorgiRenderer extends WolfRenderer {
    private static final ResourceLocation CORGI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "textures/entity/corgi.png");

    public CorgiRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.layers.removeIf(layer -> layer instanceof WolfCollarLayer);
    }

    @Override
    public ResourceLocation getTextureLocation(WolfRenderState state) {
        return CORGI_TEXTURE;
    }
}


