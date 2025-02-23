package com.smartcore.coursework.service;

import com.smartcore.coursework.dto.TeamWithMembersDTO;
import com.smartcore.coursework.dto.UserDTO;
import com.smartcore.coursework.exception.EntityNotFoundException;
import com.smartcore.coursework.model.AppUser;
import com.smartcore.coursework.model.Team;
import com.smartcore.coursework.repository.AppUserRepository;
import com.smartcore.coursework.repository.TeamRepository;
import com.smartcore.coursework.util.ClassUtils;
import jakarta.transaction.Transactional;
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

    public void addUserToTeam(String username, String teamId) {
        if (username == null || username.isEmpty() || teamId == null || teamId.isEmpty()) {
            throw new IllegalArgumentException("Username and Team ID cannot be null or empty in " + ClassUtils.getClassAndMethodName());
        }

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username + " in " + ClassUtils.getClassAndMethodName()));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with ID: " + teamId + " in " + ClassUtils.getClassAndMethodName()));

        user.setTeam(team);
        appUserRepository.save(user);
    }

    public void removeUserFromTeam(String username, String teamId) {
        validateTeamId(teamId);

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username + " in " + ClassUtils.getClassAndMethodName()));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with ID: " + teamId + " in " + ClassUtils.getClassAndMethodName()));

        if (!team.equals(user.getTeam())) {
            throw new IllegalArgumentException("User " + username + " is not in team " + teamId);
        }

        user.setTeam(null);
        appUserRepository.save(user);
    }


    public Team save(Team team) {
        if (team == null) {
            throw new IllegalArgumentException("Team cannot be null in " + ClassUtils.getClassAndMethodName());
        }
        return teamRepository.save(team);
    }

    @Transactional
    public Team createTeamWithLeader(String name, AppUser leader) {
        if (leader == null) {
            throw new IllegalArgumentException("Команда должна иметь лидера");
        }

        Team team = Team.builder().name(name).leader(leader).build();
        Team createdTeam = teamRepository.save(team);

        addUserToTeam(leader.getUsername(), createdTeam.getId());

        return createdTeam;
    }

    public List<TeamWithMembersDTO> getAllTeamsWithMembers() {
        List<Team> teams = getAllTeams();
        List<AppUser> appUsers = appUserRepository.findAll();

        return teams.stream().map(team -> {
            UserDTO leaderDTO = team.getLeader() != null
                    ? new UserDTO(team.getLeader().getId(), team.getLeader().getUsername(), team.getLeader().getLvl(), team.getLeader().getXp())
                    : null;

            List<UserDTO> membersDTO = appUsers.stream()
                    .filter(user -> team.equals(user.getTeam()))
                    .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getLvl(), user.getXp()))
                    .collect(Collectors.toList());

            return new TeamWithMembersDTO(leaderDTO, team, membersDTO);
        }).collect(Collectors.toList());
    }


    public Team getTeamById(String id) {
        validateTeamId(id);

        return teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with ID: " + id + " in " + ClassUtils.getClassAndMethodName()));
    }

    @Transactional
    public void deleteTeamById(String id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        List<AppUser> usersInTeam = appUserRepository.findByTeamId(id);

        for (AppUser user : usersInTeam) {
            user.setTeam(null);
            appUserRepository.save(user);
        }

        teamRepository.delete(team);
    }

    private void validateTeamId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Team ID cannot be null or empty in " + ClassUtils.getClassAndMethodName());
        }
    }
}