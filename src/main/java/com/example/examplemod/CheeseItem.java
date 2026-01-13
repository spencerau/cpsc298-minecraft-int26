package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;

public class CheeseItem extends Item {
    public CheeseItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof Player player) {

            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 300, 3));
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 1));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 300, 5));
        }

        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            BlockPos pos = player.blockPosition();

            LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
            bolt.setPos(pos.getX(), pos.getY(), pos.getZ());
            serverLevel.addFreshEntity(bolt);
        }

        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }
}