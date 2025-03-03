package sh.sagan.jaseppi;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import sh.sagan.jaseppi.audio.AudioCommands;
import sh.sagan.jaseppi.audio.JaseppiAudioManager;
import sh.sagan.jaseppi.jisho.Jisho;
import sh.sagan.sf6j.GameData;
import sh.sagan.sf6j.SF6J;

import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Jaseppi {

    private static final String PREFIX = ".";

    private final JDA jda;
    private final Config config;
    private final GameData sf6GameData;
    private final JaseppiAudioManager audioManager;
    private final HttpClient httpClient;
    private final Jisho jisho;

    private Jaseppi(JDA jda, Config config, GameData sf6GameData) {
        this.jda = jda;
        this.config = config;
        this.sf6GameData = sf6GameData;
        this.audioManager = new JaseppiAudioManager(this);
        this.httpClient = HttpClient.newBuilder().build();
        this.jisho = new Jisho(this);
    }

    public static Jaseppi create(JDA jda, Config config) {
        GameData sf6GameData = SF6J.load().join();

        Jaseppi jaseppi = new Jaseppi(jda, config, sf6GameData);

        CommandListUpdateAction commands = jda.getGuildById("466452910197440514").updateCommands();
        List<JaseppiCommandHandler> commandHandlers = Arrays.asList(
                new SF6Commands(jaseppi),
                new AudioCommands(jaseppi),
                new TranslateCommands(jaseppi),
                new JishoCommands(jaseppi)
        );
        commandHandlers.forEach(handler -> {
            jda.addEventListener(handler);
            handler.registerSlashCommands(commands);
        });
        jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                Message message = event.getMessage();
                String raw = message.getContentRaw();
                commandHandlers.stream()
                        .flatMap(h -> h.getCommands().entrySet().stream())
                        .filter(e -> raw.startsWith(PREFIX + e.getKey()))
                        .forEach(e -> {
                            String args = "";
                            if (raw.length() > e.getKey().length() + 1) {
                                args = raw.substring(e.getKey().length() + 2);
                            }
                            e.getValue().accept(event, args);
                        });
            }
        });
        commands.queue();

        return jaseppi;
    }

    public JDA getJDA() {
        return jda;
    }

    public Config getConfig() {
        return config;
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

    public Jisho getJisho() {
        return jisho;
    }
}
