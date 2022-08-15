package net.villagerfix.mixin;

import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.TradeOfferList;
import net.villagerfix.access.VillagerAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(ZombieVillagerEntity.class)
public abstract class ZombieVillagerEntityMixin implements VillagerAccess {

    private List<TradeOfferList> offerList;
    private List<String> jobList;

    @Inject(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;setExperience(I)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void villagerTradeFix$setVillagerData(ServerWorld world, CallbackInfo ci, VillagerEntity villagerEntity) {
        ((VillagerAccess)villagerEntity).setOffersList(this.offerList);
        ((VillagerAccess)villagerEntity).setJobList(this.jobList);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void villagerTradeFix$saveDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        for (int i = 0; i < this.jobList.size(); ++i) {
            String jobString = "OldOffer" + i;
            nbt.put(jobString, this.offerList.get(i).toNbt());
            nbt.putString(jobString + "OldWork", this.jobList.get(i));
        }
        nbt.putInt("JobCount", this.jobList.size());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void villagerTradeFix$readDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        for (int i = 0; i < nbt.getInt("JobCount"); ++i) {
            String jobString = "OldOffer" + i;
            this.jobList.add(nbt.getString(jobString + "OldWork"));
            if (nbt.contains(jobString, 10))
                this.offerList.add(new TradeOfferList(nbt.getCompound(jobString)));
        }
    }

    @Override
    public void setOffersList(List<TradeOfferList> offerList) {
        this.offerList = offerList;
    }

    @Override
    public void setJobList(List<String> jobList) {
        this.jobList = jobList;
    }
}
