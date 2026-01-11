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

public class FTBTeamsHelper {

    public static boolean canSeeMarks(UUID playerId1, UUID playerId2) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            return true;
        }

        TeamManager manager = FTBTeamsAPI.api().getManager();

        Optional<Team> team1Opt = manager.getTeamForPlayerID(playerId1);
        Optional<Team> team2Opt = manager.getTeamForPlayerID(playerId2);

        if (team1Opt.isEmpty() || team2Opt.isEmpty()) {
            return true;
        }

        Team team1 = team1Opt.get();
        Team team2 = team2Opt.get();

        boolean player1InParty = team1.isPartyTeam();
        boolean player2InParty = team2.isPartyTeam();

        if (!player1InParty && !player2InParty) {
            return true;
        }

        if (player1InParty != player2InParty) {
            return false;
        }

        return team1.getTeamId().equals(team2.getTeamId());
    }

    public static boolean canSeeMarksClient(UUID ownerUUID, @Nullable UUID ownerTeamId, boolean ownerInParty, Player viewer) {
        if (!FTBTeamsAPI.api().isClientManagerLoaded()) {
            return true;
        }

        ClientTeamManager clientManager = FTBTeamsAPI.api().getClientManager();

        Optional<Team> viewerTeamOpt = clientManager.getTeamForPlayer(viewer);
        if (viewerTeamOpt.isEmpty()) {
            return !ownerInParty;
        }

        Team viewerTeam = viewerTeamOpt.get();
        boolean viewerInParty = viewerTeam.isPartyTeam();

        if (!ownerInParty && !viewerInParty) {
            return true;
        }

        if (ownerInParty != viewerInParty) {
            return false;
        }

        if (ownerTeamId == null) {
            return false;
        }
        return ownerTeamId.equals(viewerTeam.getTeamId());
    }

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

        if (team.isPartyTeam()) {
            return team.getTeamId();
        }

        return null;
    }

    public static boolean isInParty(UUID playerId) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            return false;
        }

        TeamManager manager = FTBTeamsAPI.api().getManager();
        Optional<Team> teamOpt = manager.getTeamForPlayerID(playerId);

        return teamOpt.map(Team::isPartyTeam).orElse(false);
    }

    public static boolean areInSameTeam(UUID playerId1, UUID playerId2) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            return false;
        }

        TeamManager manager = FTBTeamsAPI.api().getManager();
        return manager.arePlayersInSameTeam(playerId1, playerId2);
    }
}
