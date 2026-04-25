package com.cpt202.security;

import com.cpt202.model.entity.User;

/**
 * Authenticated user context extracted from a validated JWT.
 */
public record AuthContext(Long userId, User.UserRole role) {
}
