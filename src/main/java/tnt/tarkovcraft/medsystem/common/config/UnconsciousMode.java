package tnt.tarkovcraft.medsystem.common.config;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;

import java.util.function.Predicate;

public enum UnconsciousMode {

    ALLOW(level -> true),
    ONLY_MULTIPLAYER(UnconsciousMode::isMultiplayer),
    DISABLE(level -> false);

    private final Predicate<ServerLevel> filter;

    UnconsciousMode(Predicate<ServerLevel> filter) {
        this.filter = filter;
    }

    public boolean allowsUnconsciousState(ServerLevel level) {
        return filter.test(level);
    }

    private static boolean isMultiplayer(ServerLevel level) {
        MinecraftServer server = level.getServer();
        PlayerList playerList = server.getPlayerList();
        return playerList.getPlayerCount() > 1;
    }
}
