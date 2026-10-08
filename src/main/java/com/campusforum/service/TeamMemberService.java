package com.campusforum.service;

import com.campusforum.model.TeamMember;
import com.campusforum.repository.TeamMemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;

    public TeamMemberService(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }

    public List<TeamMember> getAllMembers() {
        return teamMemberRepository.findAllByOrderByCreatedAtAsc();
    }

    public TeamMember getMemberById(Long id) {
        return teamMemberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team member not found with ID: " + id));
    }

    public TeamMember createMember(String name, String role, String bio, String image, String email, String linkedin, String github) {
        TeamMember member = new TeamMember(name.trim(), role.trim(), bio != null ? bio.trim() : "", image, email, linkedin, github);
        return teamMemberRepository.save(member);
    }

    public TeamMember updateMember(Long id, String name, String role, String bio, String image, String email, String linkedin, String github) {
        TeamMember member = getMemberById(id);
        member.setName(name.trim());
        member.setRole(role.trim());
        if (bio != null) member.setBio(bio.trim());
        if (image != null) member.setImage(image);
        if (email != null) member.setEmail(email);
        if (linkedin != null) member.setLinkedin(linkedin);
        if (github != null) member.setGithub(github);
        return teamMemberRepository.save(member);
    }

    public void deleteMember(Long id) {
        if (!teamMemberRepository.existsById(id)) {
            throw new IllegalArgumentException("Team member not found with ID: " + id);
        }
        teamMemberRepository.deleteById(id);
    }
}
