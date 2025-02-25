package sh.sagan.jaseppi.jisho;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Sense {

    @SerializedName("english_definitions")
    private List<String> englishDefinitions;
    @SerializedName("parts_of_speech")
    private List<String> partsOfSpeech;

    public List<String> getEnglishDefinitions() {
        return englishDefinitions;
    }

    public List<String> getPartsOfSpeech() {
        return partsOfSpeech;
    }
}
