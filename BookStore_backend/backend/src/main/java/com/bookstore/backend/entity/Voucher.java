package com.bookstore.backend.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "vouchers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Voucher extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String code;
    private Integer discountPercent;
    private BigDecimal discountAmount;
    private LocalDate expiryDate;
    private Integer usageLimit;
    private Integer usedCount = 0;
}
