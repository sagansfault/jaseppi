package sh.sagan.jaseppi;

import com.deepl.api.DeepLClient;
import com.deepl.api.DeepLException;
import com.deepl.api.Usage;
import com.moji4j.MojiConverter;
import com.moji4j.MojiDetector;
import fr.free.nrw.jakaroma.Jakaroma;

public class TranslateCommands extends JaseppiCommandHandler {

    private final DeepLClient client;
    private final Jakaroma jakaroma;
    private final MojiConverter mojiConverter;
    private final MojiDetector mojiDetector;

    public TranslateCommands(Jaseppi jaseppi) {
        super(jaseppi);
        this.client = new DeepLClient(jaseppi.getConfig().getDeeplAPIKey());
        this.jakaroma = new Jakaroma();
        this.mojiConverter = new MojiConverter();
        this.mojiDetector = new MojiDetector();

        registerPrefixCommand("te", (event, args) -> {
            String text;
            try {
                Usage.Detail character = client.getUsage().getCharacter();
                if (character != null && (double) character.getCount() / 500_000 > 0.9) {
                    text = "Character limit reached for this month";
                } else {
                    text = client.translateText(args, "en", "ja").getText();
                }
            } catch (DeepLException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            String romaji = jakaroma.convert(text, false, false);
            text += " (" + romaji + ")";
            event.getMessage().reply(text).queue();
        });

        registerPrefixCommand("tj", (event, args) -> {
            if (!mojiDetector.hasKana(args) && !mojiDetector.hasKanji(args)) {
                args = mojiConverter.convertRomajiToHiragana(args);
            }
            try {
                Usage.Detail character = client.getUsage().getCharacter();
                if (character != null && (double) character.getCount() / 500_000 > 0.9) {
                    args = "Character limit reached for this month";
                } else {
                    args = client.translateText(args, "ja", "en-US").getText();
                }
            } catch (DeepLException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            event.getMessage().reply(args).queue();
        });
    }
}
