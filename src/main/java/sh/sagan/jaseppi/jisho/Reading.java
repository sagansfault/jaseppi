package sh.sagan.jaseppi.jisho;

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
        return "Reading{" +
                "reading='" + reading + '\'' +
                ", word='" + word + '\'' +
                '}';
    }
}
