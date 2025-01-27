package com.smartcore.coursework.service;

import com.smartcore.coursework.dto.TeamWithMembersDTO;
import com.smartcore.coursework.exception.EntityNotFoundException;
import com.smartcore.coursework.model.AppUser;
import com.smartcore.coursework.model.Team;
import com.smartcore.coursework.repository.AppUserRepository;
import com.smartcore.coursework.repository.TeamRepository;
import com.smartcore.coursework.util.ClassUtils;
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
        List<Team> teams = teamRepository.findAll();
        if (teams.isEmpty()) {
            throw new EntityNotFoundException("No teams found in " + ClassUtils.getClassAndMethodName());
        }
        return teams;
    }

    public Team save(Team team) {
        if (team == null) {
            throw new IllegalArgumentException("Team cannot be null in " + ClassUtils.getClassAndMethodName());
        }
        return teamRepository.save(team);
    }

    public List<TeamWithMembersDTO> getAllTeamsWithMembers() {
        List<Team> teams = getAllTeams();
        List<AppUser> appUsers = appUserRepository.findAll();
        if (appUsers.isEmpty()) {
            throw new EntityNotFoundException("No users found in " + ClassUtils.getClassAndMethodName());
        }

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
        validateTeamId(id);

        return teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with ID: " + id + " in " + ClassUtils.getClassAndMethodName()));
    }

    public void deleteTeamById(String id) {
        validateTeamId(id);

        if (!teamRepository.existsById(id)) {
            throw new EntityNotFoundException("Team with ID " + id + " not found in " + ClassUtils.getClassAndMethodName());
        }

        teamRepository.deleteById(id);
    }

    private void validateTeamId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Team ID cannot be null or empty in " + ClassUtils.getClassAndMethodName());
        }
    }
}