package sh.sagan.jaseppi;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AICommands extends JaseppiCommandHandler {

    private static final Pattern REGEX = Pattern.compile("content\": ?\"(.+)\"\n  },");

    private static final String MODEL = "deepseek-r1:1.5b";
    private static final String CHAT_ADDRESS = "http://localhost:8888/api/chat";

    public AICommands(Jaseppi jaseppi) {
        super(jaseppi);
    }

    @Override
    public void register(CommandListUpdateAction commands) {
        commands.addCommands(
                Commands.slash("talk", "Ask me anything")
                        .addOption(OptionType.STRING, "query", "Query", true)
                        .setGuildOnly(true)
        );
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Get outa my dms").queue();
            return;
        }
        Member member = event.getInteraction().getMember();
        if (member == null) {
            event.reply("?").queue();
            return;
        }
        switch (event.getName()) {
            case "talk":
                handleTalk(event);
                break;
        }
    }

    private void handleTalk(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        String prompt = event.getOption("query").getAsString().trim();
        String data = String.format("{\"model\": \"%s\",\"stream\": false,\"messages\": [{\"role\": \"user\", \"content\": \"%s\"}]}", MODEL, prompt);
        sendRequest(event, CHAT_ADDRESS, data);
    }

    private void sendRequest(SlashCommandInteractionEvent event, String address, String data) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(address))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();
        jaseppi.getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    JsonObject json = Main.GSON.fromJson(response, JsonObject.class);
                    response = json.get("message").getAsJsonObject().get("content").getAsString();
                    response = response.replaceAll("\u003cthink\u003e\n\n\u003c/think\u003e\n\n", "");
                    event.getHook().editOriginal(response).queue();
                });
    }
}
