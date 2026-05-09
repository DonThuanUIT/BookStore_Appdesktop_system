package com.bookstore.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public record ImportUpsertRequest(
        @NotEmpty(message = "Danh sach sach nhap khong duoc de trong")
        List<@Valid ImportDetailRequest> details
) {
}
