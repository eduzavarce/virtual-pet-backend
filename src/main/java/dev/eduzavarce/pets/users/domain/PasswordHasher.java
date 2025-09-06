package dev.eduzavarce.pets.users.domain;

public interface PasswordHasher {
    String hash(String plainText);

    boolean matches(String plainText, String hashedText);
}
