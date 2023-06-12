package net.villagerfix.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;
import net.villagerfix.VillagerFixMain;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {

    private List<TradeOfferList> offerList = new ArrayList<TradeOfferList>();
    private List<String> jobList = new ArrayList<String>();

    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo info) {
        for (int i = 0; i < nbt.getInt("JobCount"); ++i) {
            String jobString = "OldOffer" + i;
            jobList.add(nbt.getString(jobString + "OldWork"));
            if (nbt.contains(jobString, 10))
                offerList.add(new TradeOfferList(nbt.getCompound(jobString)));
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo info) {
        for (int i = 0; i < this.jobList.size(); ++i) {
            String jobString = "OldOffer" + i;
            nbt.put(jobString, this.offerList.get(i).toNbt());
            nbt.putString(jobString + "OldWork", this.jobList.get(i));
        }
        nbt.putInt("JobCount", this.jobList.size());
    }

    @Redirect(method = "prepareOffersFor", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/TradeOffer;increaseSpecialPrice(I)V", ordinal = 0))
    private void prepareOffersForRedirect(TradeOffer tradeOffer, int amount) {
        if (tradeOffer.getOriginalFirstBuyItem().getCount() * VillagerFixMain.CONFIG.maxReputationDiscount < MathHelper.abs(amount)) {
            tradeOffer.increaseSpecialPrice(-MathHelper.floor((float) VillagerFixMain.CONFIG.maxReputationDiscount * tradeOffer.getOriginalFirstBuyItem().getCount()));
        } else
            tradeOffer.increaseSpecialPrice(amount);
    }

    @Redirect(method = "prepareOffersFor", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/TradeOffer;increaseSpecialPrice(I)V", ordinal = 1))
    private void prepareOffersForRedirect(TradeOffer tradeOffer, int amount, PlayerEntity playerEntity) {
        double d = VillagerFixMain.CONFIG.baseVillagerOfTheHeroDiscount
                + VillagerFixMain.CONFIG.extraVillagerOfTheHeroDiscount * playerEntity.getStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE).getAmplifier();
        int k = (int) Math.floor(d * (double) tradeOffer.getOriginalFirstBuyItem().getCount());
        tradeOffer.increaseSpecialPrice(-Math.max(k, 0));
    }

    @Inject(method = "setVillagerData", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/passive/VillagerEntity;getVillagerData()Lnet/minecraft/village/VillagerData;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void setVillagerDataMixin(VillagerData villagerData, CallbackInfo info, VillagerData villagerData2) {
        if (!offerList.contains(this.getOffers())) {
            offerList.add(this.getOffers());
            jobList.add(villagerData2.getProfession().toString());
        }
    }

    @ModifyVariable(method = "prepareOffersFor", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/passive/VillagerEntity;getReputation(Lnet/minecraft/entity/player/PlayerEntity;)I"), ordinal = 0)
    private int prepareOffersForReputationMixin(int i) {
        if (i > VillagerFixMain.CONFIG.maxReputation) {
            i = VillagerFixMain.CONFIG.maxReputation;
        }
        return i;
    }

    @Override
    public TradeOfferList getOffers() {
        if (this.offers == null) {
            if (this.jobList != null && this.jobList.contains(((VillagerEntity) (Object) this).getVillagerData().getProfession().toString())) {
                this.offers = this.offerList.get(this.jobList.indexOf(((VillagerEntity) (Object) this).getVillagerData().getProfession().toString()));
            } else {
                this.offers = new TradeOfferList();
                this.fillRecipes();
            }
        }
        return this.offers;
    }

}
