package com.bookstore.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public record ImportUpsertRequest(
        @NotEmpty(message = "Import details must not be empty")
        @Size(max = 100, message = "Import cannot contain more than 100 details")
        List<@Valid ImportDetailRequest> details
) {
}
