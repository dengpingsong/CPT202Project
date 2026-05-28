package com.cpt202.service;

import com.cpt202.model.entity.User;
import com.cpt202.security.AuthContext;

/**
 * Handles JWT creation and parsing for authenticated users.
 */
public interface JwtTokenService {

    /**
     * Generates a signed JWT for an authenticated user.
     *
     * @param user authenticated user snapshot
     * @return signed JWT string
     */
    String generateToken(User user);

    /**
     * Parses and validates a raw JWT value.
     *
     * @param token JWT without the Bearer prefix
     * @return authenticated user context
     */
    AuthContext parseToken(String token);

    /**
     * Extracts, parses and validates an Authorization header value.
     *
     * @param authorizationHeader full Authorization header value
     * @return authenticated user context
     */
    AuthContext parseBearerToken(String authorizationHeader);
}
