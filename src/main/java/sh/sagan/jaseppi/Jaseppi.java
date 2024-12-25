package sh.sagan.jaseppi;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import sh.sagan.jaseppi.audio.AudioCommands;
import sh.sagan.jaseppi.audio.JaseppiAudioManager;
import sh.sagan.sf6j.GameData;
import sh.sagan.sf6j.SF6J;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

public class Jaseppi {

    private final JDA jda;
    private final GameData sf6GameData;
    private final JaseppiAudioManager audioManager;

    private Jaseppi(JDA jda, GameData sf6GameData, String poToken, String visitorData) {
        this.jda = jda;
        this.sf6GameData = sf6GameData;
        this.audioManager = new JaseppiAudioManager(poToken, visitorData);
    }

    public static Jaseppi create(JDA jda) {
        GameData sf6GameData = SF6J.load().join();
        String[] poTokenAndVisitorData = getPoTokenAndVisitorData();

        Jaseppi jaseppi = new Jaseppi(jda, sf6GameData, poTokenAndVisitorData[0], poTokenAndVisitorData[1]);

        CommandListUpdateAction commands = jda.getGuildById("466452910197440514").updateCommands();
        Stream.of(
                new SF6Commands(jaseppi),
                new AudioCommands(jaseppi)
        ).forEach(handler -> {
            handler.register(commands);
            jda.addEventListener(handler);
        });
        commands.queue();

        return jaseppi;
    }

    public JDA getJDA() {
        return jda;
    }

    public GameData getSF6GameData() {
        return sf6GameData;
    }

    public JaseppiAudioManager getAudioManager() {
        return audioManager;
    }

    private static String[] getPoTokenAndVisitorData() {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:8080/token")).GET().build();
        String body = httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString()).join().body();
        JsonObject jsonObject = Main.GSON.fromJson(body, JsonObject.class);
        String potoken = jsonObject.get("potoken").getAsString();
        String visitorData = jsonObject.get("visitor_data").getAsString();
        return new String[]{potoken, visitorData};
    }
}
