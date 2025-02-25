package sh.sagan.jaseppi.jisho;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JishoResponseData {

    private String slug;
    @SerializedName("is_common")
    private boolean common;
    private List<Reading> japanese;
    private List<Sense> senses;

    public String getSlug() {
        return slug;
    }

    public boolean isCommon() {
        return common;
    }

    public List<Reading> getJapanese() {
        return japanese;
    }

    public List<Sense> getSenses() {
        return senses;
    }
}
