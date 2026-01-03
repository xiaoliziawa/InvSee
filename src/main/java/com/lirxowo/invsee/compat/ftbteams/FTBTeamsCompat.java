package com.lirxowo.invsee.compat.ftbteams;

import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * FTB Teams compatibility layer.
 * Provides safe methods to interact with FTB Teams when it's loaded.
 */
public class FTBTeamsCompat {
    private static final String FTB_TEAMS_MODID = "ftbteams";

    /**
     * Check if FTB Teams mod is loaded.
     *
     * @return true if FTB Teams is available
     */
    public static boolean isLoaded() {
        return ModList.get().isLoaded(FTB_TEAMS_MODID);
    }

    /**
     * Check if two players are in the same team.
     * If FTB Teams is not loaded, returns true (all players can see each other).
     * If one or both players are not in a party team, follows special rules:
     * - Players not in a party can see marks from other players not in a party
     * - Players in a party cannot see marks from players not in a party
     * - Players not in a party cannot see marks from players in a party
     *
     * @param playerId1 UUID of the first player
     * @param playerId2 UUID of the second player
     * @return true if marks should be visible between these players
     */
    public static boolean canSeeMarks(UUID playerId1, UUID playerId2) {
        if (!isLoaded()) {
            return true;
        }
        try {
            return FTBTeamsHelper.canSeeMarks(playerId1, playerId2);
        } catch (Throwable e) {
            // If anything goes wrong, fall back to allowing visibility
            return true;
        }
    }

    /**
     * Client-side check if a mark should be visible to the viewer.
     * Uses synced team data on client.
     *
     * @param ownerUUID UUID of the mark owner
     * @param ownerTeamId Team ID of the mark owner (can be null if not in party)
     * @param ownerInParty Whether the owner was in a party when they created the mark
     * @param viewer The viewing player
     * @return true if marks should be visible
     */
    public static boolean canSeeMarksClient(UUID ownerUUID, @Nullable UUID ownerTeamId, boolean ownerInParty, Player viewer) {
        if (!isLoaded()) {
            return true;
        }
        try {
            return FTBTeamsHelper.canSeeMarksClient(ownerUUID, ownerTeamId, ownerInParty, viewer);
        } catch (Throwable e) {
            // If anything goes wrong, fall back to allowing visibility
            return true;
        }
    }

    /**
     * Get the effective team ID for a player.
     * Returns the player's party team ID if they're in a party,
     * or null if they're not in a party (individual player team).
     *
     * @param playerId the player's UUID
     * @return the team ID, or null if not in a party or FTB Teams not loaded
     */
    @Nullable
    public static UUID getPlayerTeamId(UUID playerId) {
        if (!isLoaded()) {
            return null;
        }
        try {
            return FTBTeamsHelper.getPlayerTeamId(playerId);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * Check if a player is in a party team.
     *
     * @param playerId the player's UUID
     * @return true if the player is in a party team
     */
    public static boolean isInParty(UUID playerId) {
        if (!isLoaded()) {
            return false;
        }
        try {
            return FTBTeamsHelper.isInParty(playerId);
        } catch (Throwable e) {
            return false;
        }
    }
}
