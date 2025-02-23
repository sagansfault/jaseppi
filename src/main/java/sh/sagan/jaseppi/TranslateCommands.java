package sh.sagan.jaseppi;

import com.deepl.api.DeepLClient;
import com.deepl.api.DeepLException;
import com.moji4j.MojiConverter;
import com.moji4j.MojiDetector;
import fr.free.nrw.jakaroma.Jakaroma;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

public class TranslateCommands extends JaseppiCommandHandler {

    private final DeepLClient client;
    private final Jakaroma jakaroma;
    private final MojiConverter mojiConverter;
    private final MojiDetector mojiDetector;

    public TranslateCommands(Jaseppi jaseppi) {
        super(jaseppi);
        this.client = new DeepLClient(System.getenv("DEEPL_API_KEY"));
        this.jakaroma = new Jakaroma();
        this.mojiConverter = new MojiConverter();
        this.mojiDetector = new MojiDetector();
    }

    @Override
    public void register(CommandListUpdateAction commands) {
//        commands.addCommands(
//                Commands.slash("te", "Translate English into Japanese")
//                        .addOption(OptionType.STRING, "text", "Text", true)
//                        .setGuildOnly(true),
//                Commands.slash("tj", "Translate Japanese into English")
//                        .addOption(OptionType.STRING, "text", "Text", true)
//                        .setGuildOnly(true)
//        );
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String raw = event.getMessage().getContentRaw();
        boolean te = raw.startsWith(".te");
        boolean tj = raw.startsWith(".tj");
        if (!te && !tj) {
            return;
        }
        String text = raw.substring(4);
        String source = te ? "en" : "ja";
        String target = te ? "ja" : "en-US";

        if (tj) {
            if (!mojiDetector.hasKana(text) && !mojiDetector.hasKanji(text)) {
                text = mojiConverter.convertRomajiToHiragana(text);
            }
        }

        try {
            text = client.translateText(text, source, target).getText();
        } catch (DeepLException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (te) {
            String romaji = jakaroma.convert(text, false, false);
            text += " (" + romaji + ")";
        }

        event.getMessage().reply(text).queue();
    }
}
