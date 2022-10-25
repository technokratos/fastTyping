package com.training.apparatus.data.service;

import com.training.apparatus.data.dto.UserDto;
import com.training.apparatus.data.entity.Group;
import com.training.apparatus.data.entity.User;
import com.training.apparatus.data.repo.UserRepository;
import com.training.apparatus.secutiy.SecurityService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    SecurityService securityService;

    @Autowired
    EntityManager em;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public void saveUserWithEncodePassword(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name()).build();
    }

    @Transactional
    public void resetResult(String email) {
        Optional<User> user = Optional.ofNullable(userRepository.findByEmail(email));
        if (user.isPresent()) {
            user.get().getResults().clear();
            userRepository.save(user.get());
        }

    }

    public Group getGroup(String email) {
        return userRepository.findByEmail(email).getGroup();
    }


    public List<User> findUsersByGroup() {
        User user = securityService.getAuthUser();
        return updateUsers(user.getGroup().getLink());
    }

    public List<UserDto> findUsersDtoByGroup() {
        User user = securityService.getAuthUser();
        long groupId = user.getGroup().getId();
        return findUsersDtoByGroup(groupId);
    }

    public List<UserDto> findUsersDtoByGroup(long groupId) {
        return userRepository.findUserDtoByGroup(groupId);
    }

    public List<User> updateUsers(String code) {
        return userRepository.findByGroup(code);
    }

    @Transactional
    public void moveCursor(User auth, int cursor) {
        Map<User.Settings, String> settings = getUserSettings(auth);
        settings.put(User.Settings.CursorInExternalText, Integer.toString(cursor));
        userRepository.save(auth);
    }

    @Transactional
    public void setUserText(User auth, String url) {
        Map<User.Settings, String> settings = getUserSettings(auth);

        settings.put(User.Settings.ExternalTextLink, URLEncoder.encode(url, StandardCharsets.UTF_8));
        //settings.put(User.Settings.CursorInExternalText, Integer.toString(0));
        userRepository.saveAndFlush(auth);
    }

    private static Map<User.Settings, String> getUserSettings(User auth) {
        Map<User.Settings, String> settings = auth.getSettings();
        if (settings == null) {
            settings = new HashMap<>();
            auth.setSettings(settings);
        }
        return settings;
    }

    public User getUser() {
        UserDetails auth = securityService.getAuthenticatedUser();
        return userRepository.findByEmail(auth.getUsername());
    }

    public User findByEmail(String emailValue) {
        return userRepository.findByEmail(emailValue);
    }
}
