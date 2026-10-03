package com.example.tasteebackend.services;

import com.example.tasteebackend.model.Order;

public interface EmailService {
    void sendOrderReceipt(Order order);
}