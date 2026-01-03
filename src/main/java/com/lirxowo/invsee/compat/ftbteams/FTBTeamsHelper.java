package com.lirxowo.invsee.compat.ftbteams;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import dev.ftb.mods.ftbteams.api.client.ClientTeamManager;
import dev.ftb.mods.ftbteams.api.client.KnownClientPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * Helper class that directly interacts with FTB Teams API.
 * This class is only loaded when FTB Teams is present.
 */
public class FTBTeamsHelper {

    /**
     * Check if two players can see each other's marks based on team membership.
     * Rules:
     * - If both players are in the same party team: visible
     * - If both players are NOT in any party team: visible
     * - If one is in a party and one is not: not visible
     * - If in different party teams: not visible
     *
     * @param playerId1 UUID of the first player
     * @param playerId2 UUID of the second player
     * @return true if marks should be visible between these players
     */
    public static boolean canSeeMarks(UUID playerId1, UUID playerId2) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            return true;
        }

        TeamManager manager = FTBTeamsAPI.api().getManager();

        Optional<Team> team1Opt = manager.getTeamForPlayerID(playerId1);
        Optional<Team> team2Opt = manager.getTeamForPlayerID(playerId2);

        // If we can't find team info for either player, allow visibility
        if (team1Opt.isEmpty() || team2Opt.isEmpty()) {
            return true;
        }

        Team team1 = team1Opt.get();
        Team team2 = team2Opt.get();

        boolean player1InParty = team1.isPartyTeam();
        boolean player2InParty = team2.isPartyTeam();

        // If neither is in a party, they can see each other
        if (!player1InParty && !player2InParty) {
            return true;
        }

        // If one is in a party and the other is not, they cannot see each other
        if (player1InParty != player2InParty) {
            return false;
        }

        // Both are in parties - check if same party
        return team1.getTeamId().equals(team2.getTeamId());
    }

    /**
     * Client-side check if two players can see each other's marks.
     * This uses the client team manager which has sync'd data from server.
     *
     * @param ownerUUID UUID of the mark owner
     * @param ownerTeamId Team ID of the mark owner (can be null if not in party)
     * @param ownerInParty Whether the owner was in a party when they created the mark
     * @param viewer The viewing player
     * @return true if marks should be visible
     */
    public static boolean canSeeMarksClient(UUID ownerUUID, @Nullable UUID ownerTeamId, boolean ownerInParty, Player viewer) {
        if (!FTBTeamsAPI.api().isClientManagerLoaded()) {
            return true;
        }

        ClientTeamManager clientManager = FTBTeamsAPI.api().getClientManager();

        // Get viewer's team info
        Optional<Team> viewerTeamOpt = clientManager.getTeamForPlayer(viewer);
        if (viewerTeamOpt.isEmpty()) {
            // Can't determine viewer's team - allow visibility if owner not in party
            return !ownerInParty;
        }

        Team viewerTeam = viewerTeamOpt.get();
        boolean viewerInParty = viewerTeam.isPartyTeam();

        // If neither is in a party, they can see each other
        if (!ownerInParty && !viewerInParty) {
            return true;
        }

        // If one is in a party and the other is not, they cannot see each other
        if (ownerInParty != viewerInParty) {
            return false;
        }

        // Both are in parties - check if same party
        if (ownerTeamId == null) {
            return false;
        }
        return ownerTeamId.equals(viewerTeam.getTeamId());
    }

    /**
     * Get the effective team ID for a player.
     * Returns the party team ID if the player is in a party,
     * or null if they're only in their personal player team.
     *
     * @param playerId the player's UUID
     * @return the party team ID, or null if not in a party
     */
    @Nullable
    public static UUID getPlayerTeamId(UUID playerId) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            return null;
        }

        TeamManager manager = FTBTeamsAPI.api().getManager();
        Optional<Team> teamOpt = manager.getTeamForPlayerID(playerId);

        if (teamOpt.isEmpty()) {
            return null;
        }

        Team team = teamOpt.get();

        // Only return team ID if player is in a party team
        if (team.isPartyTeam()) {
            return team.getTeamId();
        }

        return null;
    }

    /**
     * Check if a player is currently in a party team.
     *
     * @param playerId the player's UUID
     * @return true if the player is in a party team
     */
    public static boolean isInParty(UUID playerId) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            return false;
        }

        TeamManager manager = FTBTeamsAPI.api().getManager();
        Optional<Team> teamOpt = manager.getTeamForPlayerID(playerId);

        return teamOpt.map(Team::isPartyTeam).orElse(false);
    }

    /**
     * Check if two players are in the same team using the built-in API method.
     *
     * @param playerId1 UUID of the first player
     * @param playerId2 UUID of the second player
     * @return true if players are in the same team
     */
    public static boolean areInSameTeam(UUID playerId1, UUID playerId2) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            return false;
        }

        TeamManager manager = FTBTeamsAPI.api().getManager();
        return manager.arePlayersInSameTeam(playerId1, playerId2);
    }
}
