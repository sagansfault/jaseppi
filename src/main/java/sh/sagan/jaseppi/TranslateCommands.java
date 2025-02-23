package sh.sagan.jaseppi;

import com.deepl.api.DeepLClient;
import com.deepl.api.DeepLException;
import com.google.gson.JsonObject;
import com.moji4j.MojiConverter;
import com.moji4j.MojiDetector;
import fr.free.nrw.jakaroma.Jakaroma;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TranslateCommands extends JaseppiCommandHandler {

    //    private final TranslationServiceClient client;
    private final DeepLClient client;
    private final Jakaroma jakaroma;
    private final MojiConverter mojiConverter;
    private final MojiDetector mojiDetector;

    public TranslateCommands(Jaseppi jaseppi) {
        super(jaseppi);
//        try {
//            this.client = TranslationServiceClient.create();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        this.client = new DeepLClient(System.getenv("DEEPL_API_KEY"));
        this.jakaroma = new Jakaroma();
        this.mojiConverter = new MojiConverter();
        this.mojiDetector = new MojiDetector();
    }

    @Override
    public void register(CommandListUpdateAction commands) {
        commands.addCommands(
                Commands.slash("te", "Translate English into Japanese")
                        .addOption(OptionType.STRING, "text", "Text", true)
                        .setGuildOnly(true),
                Commands.slash("tj", "Translate Japanese into English")
                        .addOption(OptionType.STRING, "text", "Text", true)
                        .setGuildOnly(true)
        );
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            return;
        }
        boolean te = event.getName().equalsIgnoreCase("te");
        boolean tj = event.getName().equalsIgnoreCase("tj");
        if (!te && !tj) {
            return;
        }
        event.deferReply().queue();

        String text = event.getOption("text").getAsString().trim();
        String source = te ? "en" : "ja";
        String target = te ? "ja" : "en-US";

        if (tj) {
            if (mojiDetector.hasKana(text) || mojiDetector.hasKanji(text)) {
                text = mojiConverter.convertRomajiToHiragana(text);
            }
        }

//        if (tj) {
//            HttpRequest req = HttpRequest.newBuilder()
//                    .GET()
//                    .uri(URI.create(String.format("https://api.romaji2kana.com/v1/to/kana?q=%s", text.trim().replaceAll(" ", "%20"))))
//                    .build();
//            try {
//                text = jaseppi.getHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).body();
//            } catch (IOException | InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            text = Main.GSON.fromJson(text, JsonObject.class).get("a").getAsString();
//        }

        try {
            text = client.translateText(text, source, target).getText();
        } catch (DeepLException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (te) {
            String romaji = new Jakaroma().convert(text, false, false);
            text += " (" + romaji + ")";
        }

        event.getHook().editOriginal(text).queue();
    }
}
