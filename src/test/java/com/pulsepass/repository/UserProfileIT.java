package com.pulsepass.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.BaseIntegrationTest;
import com.pulsepass.domain.model.User;
import com.pulsepass.domain.model.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

class UserProfileIT extends BaseIntegrationTest {
    @Autowired private UserRepository users;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void shouldPersistUserWithSingleProfile() {
        User user = new User();
        user.setUsername("andrea");
        user.setEmail("andrea@x.com");
        user.setActive(true);

        UserProfile profile = new UserProfile();
        profile.setFirstName("Andrea");
        profile.setLastName("Perez");
        profile.setCity("Bogota");
        user.setProfile(profile);

        users.saveAndFlush(user);

        User found = users.findByEmailIgnoreCase("ANDREA@X.COM").orElseThrow();
        assertThat(found.getProfile().getFirstName()).isEqualTo("Andrea");
    }

    @Test
    void shouldRejectSecondProfileForSameUser() {
        User user = new User();
        user.setUsername("carlos");
        user.setEmail("c@x.com");
        user.setActive(true);

        UserProfile profile = new UserProfile();
        profile.setFirstName("Carlos");
        profile.setLastName("Lopez");
        user.setProfile(profile);
        users.saveAndFlush(user);

assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO user_profiles(user_id, first_name, last_name) VALUES (?, 'Otro', 'Perfil')",
                user.getId()))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldRejectDuplicateUsername() {
        User first = new User();
        first.setUsername("dup-user");
        first.setEmail("du1@x.com");
        first.setActive(true);
        users.saveAndFlush(first);

        User second = new User();
        second.setUsername("dup-user");
        second.setEmail("du2@x.com");
        second.setActive(true);

        assertThatThrownBy(() -> users.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        User first = new User();
        first.setUsername("em-1");
        first.setEmail("same@x.com");
        first.setActive(true);
        users.saveAndFlush(first);

        User second = new User();
        second.setUsername("em-2");
        second.setEmail("same@x.com");
        second.setActive(true);

        assertThatThrownBy(() -> users.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

