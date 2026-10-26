package com.treasuryhub.payment.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.treasuryhub.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
