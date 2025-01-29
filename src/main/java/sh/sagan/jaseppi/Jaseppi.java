package sh.sagan.jaseppi;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import sh.sagan.jaseppi.audio.AudioCommands;
import sh.sagan.jaseppi.audio.JaseppiAudioManager;
import sh.sagan.sf6j.GameData;
import sh.sagan.sf6j.SF6J;

import java.net.http.HttpClient;
import java.util.stream.Stream;

public class Jaseppi {

    private final JDA jda;
    private final GameData sf6GameData;
    private final JaseppiAudioManager audioManager;
    private final HttpClient httpClient;

    private Jaseppi(JDA jda, GameData sf6GameData) {
        this.jda = jda;
        this.sf6GameData = sf6GameData;
        this.audioManager = new JaseppiAudioManager();
        this.httpClient = HttpClient.newBuilder().build();
    }

    public static Jaseppi create(JDA jda) {
        GameData sf6GameData = SF6J.load().join();

        Jaseppi jaseppi = new Jaseppi(jda, sf6GameData);

        CommandListUpdateAction commands = jda.getGuildById("466452910197440514").updateCommands();
        Stream.of(
                new SF6Commands(jaseppi),
                new AudioCommands(jaseppi),
                new AICommands(jaseppi)
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

    public HttpClient getHttpClient() {
        return httpClient;
    }
}
