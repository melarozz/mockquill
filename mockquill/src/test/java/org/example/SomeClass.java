package org.example;

public class SomeClass implements SomeService {

    @Override
    public String getData() {
        return "Real Data";
    }

    @Override
    public String complexMethod(String input, int number, String pattern) {
        return input + number + pattern;
    }

}
