package sh.sagan.jaseppi;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import sh.sagan.jaseppi.jisho.JishoResponse;
import sh.sagan.jaseppi.jisho.JishoResponseData;
import sh.sagan.jaseppi.jisho.Reading;
import sh.sagan.jaseppi.jisho.Sense;

import java.util.ArrayList;
import java.util.List;

public class JishoCommands extends JaseppiCommandHandler {

    public JishoCommands(Jaseppi jaseppi) {
        super(jaseppi);
    }

    @Override
    public void register(CommandListUpdateAction commands) {

    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String raw = event.getMessage().getContentRaw();
        if (!raw.startsWith("j")) {
            return;
        }
        String text = raw.substring(3);
        String word = text.split(" ")[0];
        jaseppi.getJisho().search(word).thenAccept(response -> {
            event.getMessage().replyEmbeds(buildEmbed(word, response)).queue();
        });
    }

    private MessageEmbed buildEmbed(String query, JishoResponse response) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("\"" + query + "\"", "https://jisho.org/search/" + query);
        List<JishoResponseData> data = response.getData();
        for (int i = 0; i < Math.min(3, data.size()); i++) {
            JishoResponseData datum = data.get(i);
            Reading reading = datum.getJapanese().get(0);
            String fieldName = reading.getWord() + " (" + reading.getReading() + ")";
            List<String> fieldValues = new ArrayList<>();
            List<Sense> senses = datum.getSenses();
            for (int j = 0; j < Math.min(3, senses.size()); j++) {
                Sense sens = senses.get(j);
                String text = String.join("; ", sens.getEnglishDefinitions());
                String sub = String.join(", ", sens.getPartsOfSpeech());
                fieldValues.add((j + 1) + ". " + text + "\n    - " + sub);
            }
            String fieldValue = String.join("\n", fieldValues);
            builder.addField(fieldName, fieldValue, false);
        }
        return builder.build();
    }
}
