package dev.eduzavarce.pets.users.domain;


import dev.eduzavarce.pets.shared.core.domain.StringValueObject;

import java.util.Objects;
import java.util.regex.Pattern;

public class UserEmail extends StringValueObject {
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile(
          "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");

  private static final int MAX_EMAIL_LENGTH = 254; // RFC 5321 limit
  private static final int MAX_LOCAL_PART_LENGTH = 64; // RFC 5321 limit
  private static final int MAX_DOMAIN_LENGTH = 253; // RFC 5321 limit

  private static final Pattern DISPOSABLE_EMAIL_DOMAINS =
      Pattern.compile("(?i)(10minutemail|guerrillamail|mailinator|tempmail|throwaway)\\..*");

  protected UserEmail(String value) {
    super(normalizeEmail(value));
    validateEmail(value);
  }

  private static String normalizeEmail(String email) {
    if (email == null) {
      return null;
    }
    return email.trim().toLowerCase();
  }

  private void validateEmail(String email) {
    validateNotNull(email);
    validateNotEmpty(email);
    validateLength(email);
    validateFormat(email);
    validateLocalPartLength(email);
    validateDomainLength(email);
    validateNotDisposable(email);
    validateDomainFormat(email);
  }

  private void validateNotNull(String email) {
    Objects.requireNonNull(email, "Email cannot be null");
  }

  private void validateNotEmpty(String email) {
    if (email.isBlank()) {
      throw new InvalidUserEmailException("Email cannot be empty or blank");
    }
  }

  private void validateLength(String email) {
    if (email.length() > MAX_EMAIL_LENGTH) {
      throw new InvalidUserEmailException(
          String.format(
              "Email exceeds maximum length of %d characters: %s", MAX_EMAIL_LENGTH, email));
    }
  }

  private void validateFormat(String email) {
    if (!EMAIL_PATTERN.matcher(email).matches()) {
      throw new InvalidUserEmailException("Invalid email format: " + email);
    }
  }

  private void validateLocalPartLength(String email) {
    String localPart = extractLocalPart(email);
    if (localPart.length() > MAX_LOCAL_PART_LENGTH) {
      throw new InvalidUserEmailException(
          String.format(
              "Email local part exceeds maximum length of %d characters: %s",
              MAX_LOCAL_PART_LENGTH, localPart));
    }
  }

  private void validateDomainLength(String email) {
    String domain = extractDomain(email);
    if (domain.length() > MAX_DOMAIN_LENGTH) {
      throw new InvalidUserEmailException(
          String.format(
              "Email domain exceeds maximum length of %d characters: %s",
              MAX_DOMAIN_LENGTH, domain));
    }
  }

  private void validateNotDisposable(String email) {
    String domain = extractDomain(email);
    if (DISPOSABLE_EMAIL_DOMAINS.matcher(domain).matches()) {
      throw new InvalidUserEmailException("Disposable email addresses are not allowed: " + email);
    }
  }

  private void validateDomainFormat(String email) {
    String domain = extractDomain(email);

    if (!domain.contains(".")) {
      throw new InvalidUserEmailException("Email domain must contain at least one dot: " + email);
    }

    if (domain.startsWith(".")
        || domain.endsWith(".")
        || domain.startsWith("-")
        || domain.endsWith("-")) {
      throw new InvalidUserEmailException("Invalid domain format: " + domain);
    }

    if (domain.contains("..")) {
      throw new InvalidUserEmailException("Domain cannot contain consecutive dots: " + domain);
    }
  }

  private String extractLocalPart(String email) {
    int atIndex = email.lastIndexOf('@');
    return atIndex > 0 ? email.substring(0, atIndex) : email;
  }

  private String extractDomain(String email) {
    int atIndex = email.lastIndexOf('@');
    return atIndex >= 0 && atIndex < email.length() - 1 ? email.substring(atIndex + 1) : "";
  }

  public String getLocalPart() {
    return extractLocalPart(value());
  }

  public String getDomain() {
    return extractDomain(value());
  }

  public boolean isFromDomain(String domain) {
    return getDomain().equalsIgnoreCase(domain);
  }

  public boolean isPersonalEmail() {
    String domain = getDomain().toLowerCase();
    return domain.equals("gmail.com")
        || domain.equals("yahoo.com")
        || domain.equals("outlook.com")
        || domain.equals("hotmail.com")
        || domain.equals("icloud.com");
  }

  public boolean isCorporateEmail() {
    return !isPersonalEmail() && !getDomain().isEmpty();
  }

  @Override
  public String value() {
    return super.value();
  }

  @Override
  public String toString() {
    return "UserEmail{value='" + value() + "'}";
  }
}
