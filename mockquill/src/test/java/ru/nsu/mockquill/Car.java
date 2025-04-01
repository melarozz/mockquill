package ru.nsu.mockquill;

// Car.java
public class Car {
    // Fields (Attributes)
    private String brand;
    private String model;
    private int year;
    private double speed;

    // Constructor
    public Car(String brand, String model, int year) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.speed = 0; // Default speed
    }

    // Method to accelerate
    public void accelerate(double amount) {
        if (amount > 0) {
            speed += amount;
            System.out.println(brand + " " + model + " accelerated to " + speed + " km/h.");
        } else {
            System.out.println("Acceleration amount must be positive.");
        }
    }

    // Method to brake
    public void brake(double amount) {
        if (amount > 0 && speed - amount >= 0) {
            speed -= amount;
            System.out.println(brand + " " + model + " slowed down to " + speed + " km/h.");
        } else {
            System.out.println("Invalid braking amount.");
        }
    }

    // Method to display car info
    public void displayInfo() {
        System.out.println("Car Info: " + brand + " " + model + " (" + year + ")");
    }

    public double getSpeed() {
        return speed;
    }
}