package dev.eduzavarce.pets.shared.core.domain;

public record PaginatedResponseDto<T>(String status, T data, int currentPage, int totalPages, long totalItems,
                                      int itemsPerPage) {
}
