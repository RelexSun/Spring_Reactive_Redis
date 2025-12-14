package com.kshrd.reactiveredis.utils;

import com.kshrd.reactiveredis.base.APIResponse;
import com.kshrd.reactiveredis.base.PagedResponse;
import com.kshrd.reactiveredis.base.PaginationInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

public class ResponseUtil {
    public static <T> ResponseEntity<APIResponse<T>> buildResponse(String messsage, T payload, HttpStatus status) {
        APIResponse<T> response = new APIResponse<>(messsage, status, payload, Instant.now());
        return ResponseEntity.status(status).body(response);
    }

    public static <T> PagedResponse<T> pagedResponse(T content, Long totalCount, Integer page, Integer size, Integer totalPages) {
        PaginationInfo paginationInfo = new PaginationInfo(totalCount, page, size, totalPages);
        return new PagedResponse<>(content, paginationInfo);
    }
}
