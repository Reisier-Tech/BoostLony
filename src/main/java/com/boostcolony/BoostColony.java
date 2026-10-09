package com.boostcolony;

import com.boostcolony.commands.BoostColonyCommands;
import com.boostcolony.config.BoostColonyConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BoostColony.MOD_ID)
public class BoostColony {
    public static final String MOD_ID = "boostcolony";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public BoostColony() {
        LOGGER.info("Initializing BoostColony Addon for MineColonies...");

        // Register Server Config
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, BoostColonyConfig.SERVER_SPEC, "boostcolony-server.toml");

        // Register Event Listeners
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(BoostColonyCommands::onRegisterCommands);

        LOGGER.info("BoostColony initialized successfully!");
    }
}
