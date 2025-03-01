package sh.sagan.jaseppi.jisho;

import java.util.Objects;

public class Reading {

    private String word;
    private String reading;

    public String getWord() {
        return word;
    }

    public String getReading() {
        return reading;
    }

    @Override
    public String toString() {
        if (word == null) {
            return Objects.requireNonNullElse(reading, "N/A");
        } else {
            if (reading == null) {
                return word;
            } else {
                return word + " (" + reading + ")";
            }
        }
    }
}
