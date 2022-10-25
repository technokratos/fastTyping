package com.training.apparatus.data.service;

import com.training.apparatus.data.entity.Group;
import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.repo.GroupRepository;
import com.training.apparatus.data.repo.UserRepository;
import com.training.apparatus.secutiy.SecurityService;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void addUser(String email, String link) {
        Optional<Group> group = groupRepository.findByLink(link);
        if(group.isPresent()) {
            User user = userRepository.findByEmail(email);
            group.get().addUser(user);
            groupRepository.save(group.get());
        }
    }

    public void removeUser(User user) {
        Optional<Group> group = groupRepository.findById(user.getGroup().getId());
        if(group.isPresent()) {
            group.get().removeUser(user);
            groupRepository.save(group.get());
        }
    }

    @Transactional
    public Group save(Group group) {
        return groupRepository.saveAndFlush(group);
    }

    public boolean notExistGroupForTheUser(String name, User manager) {
        return !groupRepository.existsByManagerAndName(manager, name);
    }

    public List<Group> getGroupsByManager(User user) {
        return groupRepository.findByManager(user);
    }

    public Optional<Group> getGroupById(long id) {
        return groupRepository.findById(id);
    }

    public boolean existGroupsForTheManager(User manager) {
        return groupRepository.existsByManager(manager);
    }
}
