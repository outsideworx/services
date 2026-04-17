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
}