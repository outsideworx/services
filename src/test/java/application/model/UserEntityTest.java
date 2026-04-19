package application.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {
    @Test
    void getAuthorities_alwaysReturnsSingleRoleUser() {
        assertThat(new UserEntity().getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_USER");
    }

    @Test
    void userDetailsFlags_defaultToFalse() {
        UserEntity user = new UserEntity();
        assertThat(user.isAccountNonExpired()).isFalse();
        assertThat(user.isAccountNonLocked()).isFalse();
        assertThat(user.isCredentialsNonExpired()).isFalse();
        assertThat(user.isEnabled()).isFalse();
    }

}