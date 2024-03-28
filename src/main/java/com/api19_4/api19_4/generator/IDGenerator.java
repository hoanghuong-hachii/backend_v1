package com.api19_4.api19_4.generator;

public class IDGenerator {
    private String prefix;
    private int counter;

    public IDGenerator(String prefix, int counter) {
        this.prefix = prefix;
        this.counter = counter; // Bắt đầu từ 1
    }

    public String generateNextID() {
        String formattedCounter = String.format("%06d", counter);
        String nextID = prefix + formattedCounter;
        counter++;
        return nextID;
    }

    // Reset the counter (useful for testing or starting anew)
    public void resetCounter() {
        counter = 1;
    }
}
