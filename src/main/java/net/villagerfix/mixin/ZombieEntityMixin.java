package net.villagerfix.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.villagerfix.access.VillagerAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin {

    @Inject(method = "onKilledOther", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/ZombieVillagerEntity;setOfferData(Lnet/minecraft/nbt/NbtCompound;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void villagerTradeFix$saveDataToZombieVillagerEntity(ServerWorld world, LivingEntity other, CallbackInfoReturnable<Boolean> cir, boolean bl, VillagerEntity villagerEntity, ZombieVillagerEntity zombieVillagerEntity) {
        ((VillagerAccess)zombieVillagerEntity).setOffersList(((VillagerAccess)other).getOffersList());
        ((VillagerAccess)zombieVillagerEntity).setJobList(((VillagerAccess)other).getJobList());
    }
}
