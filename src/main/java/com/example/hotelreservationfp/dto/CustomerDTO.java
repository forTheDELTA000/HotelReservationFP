package com.example.hotelreservationfp.dto;

import com.example.hotelreservationfp.entity.Guest; // Make sure this matches your actual Guest entity import

public class CustomerDTO {
    public Integer id;
    public String firstName;
    public String lastName;
    public String email;
    public String phone;

    public CustomerDTO(Guest guest) {
        // Change getGuestId() to whatever your primary key getter is in your Guest entity!
        this.id = guest.getGuestId();
        this.firstName = guest.getFirstName();
        this.lastName = guest.getLastName();
        this.email = guest.getEmail();
        this.phone = guest.getPhone();
    }

    // Getters
    public Integer getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}