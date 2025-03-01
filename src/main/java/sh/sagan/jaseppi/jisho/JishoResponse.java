package sh.sagan.jaseppi.jisho;

import java.util.List;

public class JishoResponse {

    private List<JishoResponseData> data;

    public List<JishoResponseData> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "JishoResponse{" +
                "data=" + data +
                '}';
    }
}