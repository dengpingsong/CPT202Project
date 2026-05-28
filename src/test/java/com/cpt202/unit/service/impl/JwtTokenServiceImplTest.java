package com.cpt202.unit.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.UnauthorizedAccessException;
import com.cpt202.model.entity.User;
import com.cpt202.properties.JwtProperties;
import com.cpt202.security.AuthContext;
import com.cpt202.security.UserAuthState;
import com.cpt202.service.UserAuthStateService;
import com.cpt202.service.impl.JwtTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/** Unit tests for JWT token generation and bearer-token parsing rules. */
@ExtendWith(MockitoExtension.class)
class JwtTokenServiceImplTest {

    private static final String TEST_SECRET = "0123456789abcdef0123456789abcdef";

    @Mock
    private UserAuthStateService userAuthStateService;

    private JwtTokenServiceImpl jwtTokenService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(TEST_SECRET);
        jwtProperties.setExpirationSeconds(3600);
        jwtTokenService = new JwtTokenServiceImpl(userAuthStateService, jwtProperties);
        jwtTokenService.init();
    }

    /** Missing Authorization headers are rejected before token parsing. */
    @Test
    void parseBearerTokenShouldRejectMissingHeader() {
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> jwtTokenService.parseBearerToken(null));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.INVALID_BEARER_TOKEN);
    }

    /** Token generation requires a persisted user id and role claim source. */
    @Test
    void generateTokenShouldRejectIncompleteUser() {
        User user = new User();
        user.setUsername("alice");

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class,
                () -> jwtTokenService.generateToken(user));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.INVALID_LOGIN_STATE);
    }

    /** A valid Bearer token resolves to the cached auth state for the same active role. */
    @Test
    void parseBearerTokenShouldReturnAuthContextForActiveUser() {
        User user = activeUser();
        String token = jwtTokenService.generateToken(user);
        when(userAuthStateService.getUserAuthState(user.getUserId()))
                .thenReturn(new UserAuthState(user.getUserId(), user.getRole(), "ACTIVE"));

        AuthContext authContext = jwtTokenService.parseBearerToken("Bearer " + token);

        assertThat(authContext.userId()).isEqualTo(user.getUserId());
        assertThat(authContext.role()).isEqualTo(user.getRole());
    }

    private User activeUser() {
        User user = new User();
        user.setUserId(10L);
        user.setUsername("alice");
        user.setRole(User.UserRole.STUDENT);
        user.setAccountStatus("ACTIVE");
        return user;
    }
}
