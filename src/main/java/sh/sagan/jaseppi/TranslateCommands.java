package sh.sagan.jaseppi;

import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.RomanizeTextRequest;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.Translation;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.cloud.translate.v3.TransliterationConfig;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;

public class TranslateCommands extends JaseppiCommandHandler {

    private final TranslationServiceClient client;

    public TranslateCommands(Jaseppi jaseppi) {
        super(jaseppi);
        try {
            this.client = TranslationServiceClient.create();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        String target = te ? "ja" : "en";

//        HttpRequest req = HttpRequest.newBuilder()
//                .GET()
//                .uri(URI.create(String.format("https://api.romaji2kana.com/v1/to/kana?q=%s", text.trim().replaceAll(" ", "%20"))))
//                .build();
//        try {
//            text = jaseppi.getHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).body();
//        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        text = Main.GSON.fromJson(text, JsonObject.class).get("a").getAsString();

        LocationName parent = LocationName.of("jaseppi-451803", "global");
        TranslateTextRequest request = TranslateTextRequest.newBuilder()
                .setParent(parent.toString())
                .setMimeType("text/plain")
                .setSourceLanguageCode("ja")
                .setTargetLanguageCode("en")
                .addContents(text)
                .setTransliterationConfig(TransliterationConfig.newBuilder().setEnableTransliteration(true))
                .build();

        TranslateTextResponse response = client.translateText(request);
        event.getHook().editOriginal(response.getTranslationsList().stream().map(Translation::getTranslatedText).collect(Collectors.joining(", "))).queue();
    }
}
