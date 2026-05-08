package com.bookstore.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public record BookUpsertRequest(
        @NotBlank(message = "Ten sach khong duoc de trong")
        @Size(max = 200, message = "Ten sach toi da 200 ky tu")
        String title,

        @Min(value = 0, message = "Nam xuat ban khong hop le")
        @Max(value = 2100, message = "Nam xuat ban khong hop le")
        Integer publishYear,

        @Size(max = 500, message = "URL anh toi da 500 ky tu")
        String imageUrl,

        @Positive(message = "publisherId khong hop le")
        Long publisherId,

        List<Long> authorIds,
        List<Long> categoryIds
) {
}
