package com.smartcore.coursework.service;

import com.smartcore.coursework.dto.TeamWithMembersDTO;
import com.smartcore.coursework.model.AppUser;
import com.smartcore.coursework.model.Team;
import com.smartcore.coursework.repository.AppUserRepository;
import com.smartcore.coursework.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final AppUserRepository appUserRepository;

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team save(Team team) {
        return teamRepository.save(team);
    }

    public List<TeamWithMembersDTO> getAllTeamsWithMembers() {
        List<Team> teams = getAllTeams();
        List<AppUser> appUsers = appUserRepository.findAll();

        return teams.stream()
                .map(team -> new TeamWithMembersDTO(
                        team,
                        appUsers.stream()
                                .filter(appUser -> team.equals(appUser.getTeam()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    public Team getTeamById(String id) {
        return teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Team not found"));
    }

    public void deleteTeamById(String id) {
        teamRepository.deleteById(id);
    }
}
