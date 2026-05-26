package com.bookstore.backend.controller;

import com.bookstore.backend.service.RevenueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/revenue")
@Tag(name = "Revenue")
@Validated
public class RevenueController {

    private final RevenueService revenueService;

    public RevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getRevenue(
            @RequestParam
            @Min(value = 2000, message = "year must be at least 2000")
            @Max(value = 2100, message = "year must not exceed 2100")
            int year,

            @RequestParam(required = false)
            @Min(value = 1, message = "month must be between 1 and 12")
            @Max(value = 12, message = "month must be between 1 and 12")
            Integer month
    ) {
        return ResponseEntity.ok(revenueService.getRevenue(year, month));
    }
}
