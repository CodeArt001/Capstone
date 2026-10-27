package com.example.demo.service;

public record PaystackTransaction(String reference, String status, long amount, String currency) {
}
