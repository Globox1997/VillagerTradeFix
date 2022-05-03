package net.villagerfix;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.villagerfix.config.VillagerFixConfig;

public class VillagerFixMain implements ModInitializer {

    public static VillagerFixConfig CONFIG = new VillagerFixConfig();

    @Override
    public void onInitialize() {
        AutoConfig.register(VillagerFixConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(VillagerFixConfig.class).getConfig();

    }

}
