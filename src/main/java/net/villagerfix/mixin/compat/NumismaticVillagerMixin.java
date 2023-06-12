package net.villagerfix.mixin.compat;

import com.glisco.numismaticoverhaul.villagers.data.NumismaticTradeOfferExtensions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.villagerfix.VillagerFixMain;

@Mixin(value = VillagerEntity.class, priority = 1001)
public abstract class NumismaticVillagerMixin extends MerchantEntity {

    public NumismaticVillagerMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public int getReputation(PlayerEntity player) {
        return 0;
    }

    @Inject(method = "prepareOffersFor", at = @At("TAIL"))
    private void captureReputation(PlayerEntity player, CallbackInfo info) {
        int reputation = this.getReputation(player);

        int adjustedReputation = (reputation
                + (player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE) ? ((player.getStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE).getAmplifier() + 1) * 10) : 0));

        if (adjustedReputation > VillagerFixMain.CONFIG.maxReputation) {
            adjustedReputation = VillagerFixMain.CONFIG.maxReputation;
        }
        final int newAdjustedReputation = adjustedReputation;

        this.getOffers().forEach(offer -> ((NumismaticTradeOfferExtensions) offer).numismatic$setReputation(newAdjustedReputation));
    }
}
