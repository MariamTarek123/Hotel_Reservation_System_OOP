package models;

import enums.genders;
import exceptions.InvalidCredentialsException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a person in the hotel reservation system.
 * This is an abstract base class that holds common properties and behaviors
 * (such as login functionality and validation) for all human entities like Guests, Admin, and Staff.
 */
public abstract class Person {
    private String username;
    private String password;
    private LocalDate dateOfBirth;
    private String address;
    private genders gender;

    //no args constructor
    protected Person() {
    }

    //parameterized constructor
    protected Person(String username, String password, LocalDate dateOfBirth, String address, genders gender) {
        setUsername(username);
        setPassword(password);
        setDateOfBirth(dateOfBirth);
        setAddress(address);
        setGender(gender);
    }

    //login method
    public boolean login(String username, String password) throws InvalidCredentialsException {
        validateLoginInput(username, password);
        if (!Objects.equals(this.username, username.trim()) || !Objects.equals(this.password, password)) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }
        return true;
    }

    //validation methods
    protected static void validateLoginInput(String username, String password) throws InvalidCredentialsException {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new InvalidCredentialsException("Username and password are required.");
        }
    }

    protected static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be blank.");
        }
        if (username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long.");
        }
    }

    protected static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank.");
        }
        if (password.length() < 7) {
            throw new IllegalArgumentException("Password must be at least 7 characters long.");
        }
        if (!password.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException("Password must include at least one letter.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must include at least one digit.");
        }
    }

    protected static void validateDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be null.");
        }
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future.");
        }
    }

    protected static void validateAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address cannot be blank.");
        }
    }

    protected static void validateGender(genders gender) {
        if (gender == null) {
            throw new IllegalArgumentException("Gender cannot be null.");
        }
    }

    //getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        validateUsername(username);
        this.username = username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        validatePassword(password);
        this.password = password;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        validateDateOfBirth(dateOfBirth);
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        validateAddress(address);
        this.address = address.trim();
    }

    public genders getGender() {
        return gender;
    }

    public void setGender(genders gender) {
        validateGender(gender);
        this.gender = gender;
    }
}
