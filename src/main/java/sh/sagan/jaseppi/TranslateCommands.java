package sh.sagan.jaseppi;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslateCommands extends JaseppiCommandHandler {

    private static final Pattern PATTERN = Pattern.compile("c2aHje\">(.*)</span>");

    public TranslateCommands(Jaseppi jaseppi) {
        super(jaseppi);
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

        String uri = String.format("https://translate.google.ca/?sl=%s&tl=%s&text=%s&op=translate", source, target, text);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();
        jaseppi.getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(t -> "Error sending http request: " + t)
                .thenAccept(response -> event.getHook().editOriginal(getResponseFromBody(response)).queue());
    }

    private String getResponseFromBody(String body) {
        Matcher matcher = PATTERN.matcher(body);
        if (matcher.groupCount() < 2) {
            return "";
        }
        return matcher.group(1);
    }
}
