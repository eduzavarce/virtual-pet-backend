package dev.eduzavarce.pets.shared.exceptions;

public class AlreadyExistsException extends CustomException {
  public AlreadyExistsException(String message) {
    super(message);
  }
}
