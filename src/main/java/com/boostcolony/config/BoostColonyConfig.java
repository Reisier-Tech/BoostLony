package com.boostcolony.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class BoostColonyConfig {
    public static class Server {
        public final ForgeConfigSpec.DoubleValue placeSpeed;
        public final ForgeConfigSpec.DoubleValue breakSpeed;
        public final ForgeConfigSpec.IntValue placeBlockRange;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.push("BoostColony");

            builder.comment("Speed multiplier for placing blocks (1.0 = vanilla, 5.0 = 5x faster, up to 100x).");
            placeSpeed = builder.defineInRange("placeSpeed", 5.0D, 1.0D, 100.0D);

            builder.comment("Speed multiplier for breaking/destroying blocks (1.0 = vanilla, 10.0 = 10x faster, up to 100x).");
            breakSpeed = builder.defineInRange("breakSpeed", 10.0D, 1.0D, 100.0D);

            builder.comment("Maximum interaction/reach range in blocks for workers (vanilla is 5-12 blocks, up to 64).");
            placeBlockRange = builder.defineInRange("placeBlockRange", 16, 5, 64);

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
