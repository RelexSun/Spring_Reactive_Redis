package com.kshrd.reactiveredis.base;

public record PaginationInfo(
        long totalElements,
        int currentPage,
        int pageSize,
        int totalPages
) {
}
