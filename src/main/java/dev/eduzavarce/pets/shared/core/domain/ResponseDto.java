package dev.eduzavarce.pets.shared.core.domain;

public record ResponseDto<T>(String status, T data) {
}
