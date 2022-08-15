package net.villagerfix.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
import net.minecraft.world.World;
import net.villagerfix.VillagerFixMain;
import net.villagerfix.access.VillagerAccess;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity implements VillagerAccess {

    private List<TradeOfferList> offerList = new ArrayList<>();
    private List<String> jobList = new ArrayList<>();

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

    // Won't get applied since bad injection point
    // Other injection would change reputation integer for other trades too
    // @ModifyVariable(method = "prepareOffersFor", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/TradeOffer;increaseSpecialPrice(I)V", ordinal = 0), ordinal = 0)
    // private int prepareOffersForMixin(int original) {
    // if (buyItemCount * VillagerFixMain.CONFIG.maxReputationDiscount < original * priceMultiplier) {
    // // return (int) (buyItemCount * VillagerFixMain.CONFIG.maxReputationDiscount / priceMultiplier);
    // } else
    // return original;
    // }
    // @Inject(method = "prepareOffersFor", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/TradeOffer;increaseSpecialPrice(I)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    // private void prepareOffersForMixin(PlayerEntity player, CallbackInfo info, int i, Iterator<TradeOfferList> var3, TradeOffer tradeOffer) {
    // priceMultiplier = tradeOffer.getPriceMultiplier();
    // buyItemCount = tradeOffer.getOriginalFirstBuyItem().getCount();
    // }
    // @Inject(method = "prepareOffersFor", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/effect/StatusEffectInstance;getAmplifier()I"), locals = LocalCapture.CAPTURE_FAILSOFT)
    // private void prepareOffersForMixin(PlayerEntity player, CallbackInfo info, int i, StatusEffectInstance statusEffectInstance, int j) {
    // amplifierVOTH = j;
    // }
    // @ModifyVariable(method = "prepareOffersFor", at = @At(value = "INVOKE", target = "Ljava/lang/Math;floor(D)D", shift = Shift.BEFORE), ordinal = 0)
    // private double prepareOffersForMixin(double original) {
    // return VillagerFixMain.CONFIG.baseVillagerOfTheHeroDiscount + VillagerFixMain.CONFIG.extraVillagerOfTheHeroDiscount * amplifierVOTH;
    // }

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

    @Override
    public void saveLastJob(VillagerEntity villagerEntity) {
        if (!offerList.contains(villagerEntity.getOffers())) {
            offerList.add(villagerEntity.getOffers());
            jobList.add(villagerEntity.getVillagerData().getProfession().toString());
        }
    }

    @Override
    public void setJobList(List<String> jobList) {
        this.jobList = jobList;
    }

    @Override
    public void setOffersList(List<TradeOfferList> offersList) {
        this.offerList = offersList;
    }

    @Override
    public List<String> getJobList() {
        return this.jobList;
    }

    @Override
    public List<TradeOfferList> getOffersList() {
        return this.offerList;
    }
}
