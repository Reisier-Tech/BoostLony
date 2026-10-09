package com.boostcolony.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class BoostColonyConfig {
    public static class Server {
        public final ForgeConfigSpec.BooleanValue maxCitizenSkills;
        public final ForgeConfigSpec.BooleanValue autoMaxSkillsOnUpdate;
        public final ForgeConfigSpec.IntValue maxSkillLevel;
        public final ForgeConfigSpec.IntValue buildingDelay;
        public final ForgeConfigSpec.IntValue miningDelay;
        public final ForgeConfigSpec.IntValue placeBlockRange;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.push("BoostColony");

            builder.comment("If true, all citizen skills (Strength, Agility, Athletics, etc.) will act as max level.");
            maxCitizenSkills = builder.define("maxCitizenSkills", true);

            builder.comment("If true, automatically updates citizens' actual saved skill levels to max level so the GUI displays 100.");
            autoMaxSkillsOnUpdate = builder.define("autoMaxSkillsOnUpdate", true);

            builder.comment("The max skill level value to set/return (MineColonies max is 100).");
            maxSkillLevel = builder.defineInRange("maxSkillLevel", 100, 1, 100);

            builder.comment("Delay ticks between placing blocks for Builders (MineColonies default is 150 ticks!). Lower = faster building.");
            buildingDelay = builder.defineInRange("buildingDelay", 1, 0, 150);

            builder.comment("Delay ticks between breaking/destroying blocks for Workers (MineColonies default is 40 ticks!). Lower = faster clearing.");
            miningDelay = builder.defineInRange("miningDelay", 1, 0, 40);

            builder.comment("The range (in blocks) from which builders can place blocks without having to walk next to them (MineColonies default is 5 blocks!).");
            placeBlockRange = builder.defineInRange("placeBlockRange", 32, 5, 128);

            builder.pop();
        }
    }

    public static final ForgeConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }
}
