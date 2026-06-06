package com.bookstore.backend.validation;

import com.bookstore.backend.dto.request.BookUpsertRequest;
import com.bookstore.backend.dto.request.CreateOrderRequest;
import com.bookstore.backend.dto.request.UpdateOrderStatusRequest;
import com.bookstore.backend.dto.request.UserCreateRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldRejectInvalidBookRequest() {
        BookUpsertRequest request = new BookUpsertRequest(
                " ",
                null,
                BigDecimal.valueOf(-1),
                "x".repeat(501),
                "x".repeat(2001),
                -1L,
                List.of(-2L),
                List.of()
        );

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void shouldAcceptValidBookRequest() {
        BookUpsertRequest request = new BookUpsertRequest(
                "Clean Code",
                2008,
                BigDecimal.valueOf(100000),
                "https://example.com/clean-code.png",
                "A book about clean code",
                1L,
                List.of(1L),
                List.of(1L)
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectEmptyOrderItems() {
        CreateOrderRequest request = new CreateOrderRequest(List.of(), "BANK_TRANSFER");;

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectInvalidOrderStatus() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("DONE");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectInvalidUserRole() {
        UserCreateRequest request = new UserCreateRequest(
                "admin01",
                "Secret123",
                "admin@example.com",
                "Admin",
                "0909000000",
                "Ho Chi Minh",
                "SUPER_ADMIN"
        );

        assertFalse(validator.validate(request).isEmpty());
    }
}
