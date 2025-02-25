package sh.sagan.jaseppi;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RestartCommands extends JaseppiCommandHandler {

    public RestartCommands(Jaseppi jaseppi) {
        super(jaseppi);
    }

    @Override
    public void register(CommandListUpdateAction commands) {

    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String raw = event.getMessage().getContentRaw();
        if (!raw.startsWith(".restart")) {
            return;
        }
        String text = raw.substring(3);
        if (event.getMessage().getAuthor().getIdLong() != 203347457944322048L) {
            return;
        }
        try {
            new ProcessBuilder("git pull; mvn clean package; java -jar target/jaseppi-0.1.0.jar").start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.exit(0);
    }
}
