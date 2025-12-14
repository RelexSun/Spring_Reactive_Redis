package com.kshrd.reactiveredis.base;

public record PagedResponse<T>(
        T items,
        PaginationInfo pagination
) {
}
