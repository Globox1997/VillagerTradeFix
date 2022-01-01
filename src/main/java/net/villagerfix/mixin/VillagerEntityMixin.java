package net.villagerfix.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;
import net.villagerfix.access.VillagerAccess;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity implements VillagerAccess {

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
            if (nbt.contains(jobString, 10)) {
                offerList.add(new TradeOfferList(nbt.getCompound(jobString)));
            }
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
}
