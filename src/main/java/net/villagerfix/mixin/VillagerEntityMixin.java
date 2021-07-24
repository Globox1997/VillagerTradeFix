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
import net.minecraft.nbt.NbtList;
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
            jobList.add(nbt.getString("JobFix" + i));
            if (nbt.contains("JobFix" + i, 10)) {
                offerList.add(new TradeOfferList(nbt.getCompound("JobFix" + i)));
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo info) {
        for (int i = 0; i < this.jobList.size(); ++i) {
            String jobString = "JobFix" + i;
            NbtList nbtJobList = new NbtList();
            for (int u = 0; u < this.offerList.get(i).size(); ++u) {
                nbtJobList.add(this.offerList.get(i).get(u).toNbt());
            }
            nbt.put(jobString, nbtJobList);
            nbt.putString(jobString, this.jobList.get(i));
        }
        nbt.putInt("JobCount", this.jobList.size());

    }

    @Inject(method = "fillRecipes", at = @At("HEAD"), cancellable = true)
    protected void fillRecipesMixin(CallbackInfo info) {
        if (this.jobList != null && this.jobList.contains(((VillagerEntity) (Object) this).getVillagerData().getProfession().toString())) {
            this.offers = this.offerList.get(this.jobList.indexOf(((VillagerEntity) (Object) this).getVillagerData().getProfession().toString()));
            info.cancel();
        }
    }

    @Override
    public void saveLastJob(VillagerEntity villagerEntity) {
        if (!offerList.contains(villagerEntity.getOffers())) {
            offerList.add(villagerEntity.getOffers());
            jobList.add(villagerEntity.getVillagerData().getProfession().toString());
        }
    }
}
