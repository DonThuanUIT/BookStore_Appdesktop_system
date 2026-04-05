package com.bookstore.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User extends BaseEntity {
    private String username;

    @Column(name = "password_hash")
    private String password;

    private String email;

    @Column(name = "full_name")
    private String fullName;

    private String phone;
    private String address;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}