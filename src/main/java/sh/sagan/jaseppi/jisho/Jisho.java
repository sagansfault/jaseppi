package sh.sagan.jaseppi.jisho;

import sh.sagan.jaseppi.Jaseppi;
import sh.sagan.jaseppi.Main;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class Jisho {

    private final Jaseppi jaseppi;

    public Jisho(Jaseppi jaseppi) {
        this.jaseppi = jaseppi;
    }

    public CompletableFuture<JishoResponse> search(String word) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(String.format("https://jisho.org/api/v1/search/words?keyword=%s", word)))
                .build();
        return jaseppi.getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> Main.GSON.fromJson(resp.body(), JishoResponse.class));
    }
}
