package net.villagerfix.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "villagerfix")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class VillagerFixConfig implements ConfigData {

    @Comment("Hero Of The Villager Discount 0.2 = 20%")
    public double baseVillagerOfTheHeroDiscount = 0.2D;
    public double extraVillagerOfTheHeroDiscount = 0.05D;

    public double maxReputationDiscount = 0.2D;
    public int maxReputation = 25;
}