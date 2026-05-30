package com.amit.skillup.camunda.dto;

public record Person(
        String name,
        int age,
        String gender,
        String email,
        String mobile,
        String address,
        String city,
        String state,
        String country
) {}
