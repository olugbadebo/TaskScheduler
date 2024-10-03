package edu.cwru.ooa21.taskscheduler.models;

import java.io.Serializable;
import java.math.BigInteger;


public final class Duration implements Comparable<Duration>, Serializable
{
    private final BigInteger milliseconds;

    private Duration(BigInteger milliseconds)
    {
        this.milliseconds = milliseconds;
    }

    // method for adding durations
    public Duration add(Duration other)
    {
        return new Duration(this.milliseconds.add(other.milliseconds));
    }

    // method for subtracting durations
    public Duration subtract(Duration other)
    {
        return new Duration(this.milliseconds.subtract(other.milliseconds));
    }

    // comparism method for comparing two durations, overriden by the Comparable class
    @Override
    public int compareTo(Duration other)
    {
        return this.milliseconds.compareTo(other.milliseconds);
    }

    // returns the milliseconds value
    public BigInteger toMillis()
    {
        return this.milliseconds;
    }

    // returns new instance of millis
    public static Duration ofMillis(long millis) {
        return new Duration(BigInteger.valueOf(millis));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Duration other = (Duration) obj;
        return milliseconds.equals(other.milliseconds);  // Compare based on milliseconds value
    }

    @Override
    public int hashCode() {
        return milliseconds.hashCode();  // Use milliseconds' hash code
    }

}
