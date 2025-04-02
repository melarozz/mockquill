package ru.nsu.mockquill;

public class Car {
    private String brand;
    private String model;
    private int year;
    private double speed;

    public Car(String brand, String model, int year) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.speed = 0;
    }

    public void accelerate(double amount) {
        if (amount > 0) {
            speed += amount;
            System.out.println(brand + " " + model + " accelerated to " + speed + " km/h.");
        } else {
            System.out.println("Acceleration amount must be positive.");
        }
    }

    public void brake(double amount) {
        if (amount > 0 && speed - amount >= 0) {
            speed -= amount;
            System.out.println(brand + " " + model + " slowed down to " + speed + " km/h.");
        } else {
            System.out.println("Invalid braking amount.");
        }
    }

    public void displayInfo() {
        System.out.println("Car Info: " + brand + " " + model + " (" + year + ")");
    }

    public double getSpeed() {
        return speed;
    }
}