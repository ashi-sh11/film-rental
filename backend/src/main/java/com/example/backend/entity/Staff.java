package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "staff")
@Getter
@Setter
public class Staff {

    @Id
    @Column(name = "staff_id")
    private Integer staffId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "address_id")
    private Integer addressId;

    @Column(name = "email")
    private String email;

    @Column(name = "store_id")
    private Integer storeId;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", insertable = false, updatable = false)
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;
}