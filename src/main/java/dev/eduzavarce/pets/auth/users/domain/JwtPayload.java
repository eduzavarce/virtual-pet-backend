package dev.eduzavarce.pets.auth.users.domain;

public class JwtPayload {
    private final String userId;
    private final String role;
    private final String username;
    private final String iat;
    private final String exp;

    public JwtPayload(String userId, String role, String username, String iat, String exp) {
        this.userId = userId;
        this.role = role;
        this.username = username;
        this.iat = iat;
        this.exp = exp;
    }
}
