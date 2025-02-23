package sh.sagan.jaseppi;

import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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

        String text = event.getOption("text").getAsString().replaceAll(" ", "%20");
        String source = te ? "en" : "ja";
        String target = te ? "ja" : "en";

        String projectId = "jaseppi-451803";

        LocationName parent = LocationName.of(projectId, "global");
        TranslateTextRequest request = TranslateTextRequest.newBuilder()
                .setParent(parent.toString())
                .setMimeType("text/plain")
                .setSourceLanguageCode(source)
                .setTargetLanguageCode(target)
                .addContents(text)
                .build();

        TranslateTextResponse response = client.translateText(request);
        event.getHook().editOriginal(response.getTranslations(0).getTranslatedText()).queue();
    }
}
