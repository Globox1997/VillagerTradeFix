package net.villagerfix.access;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOfferList;

import java.util.List;

public interface VillagerAccess {

    void saveLastJob(VillagerEntity villagerEntity);

    void setJobList(List<String> jobList);

    void setOffersList(List<TradeOfferList> offersList);

    List<String> getJobList();

    List<TradeOfferList> getOffersList();
}
