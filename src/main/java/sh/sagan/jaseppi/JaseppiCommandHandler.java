package sh.sagan.jaseppi;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class JaseppiCommandHandler extends ListenerAdapter {

    protected final Jaseppi jaseppi;
    private final Map<String, BiConsumer<MessageReceivedEvent, String>> commands = new HashMap<>();

    public JaseppiCommandHandler(Jaseppi jaseppi) {
        this.jaseppi = jaseppi;
    }

    public void registerSlashCommands(CommandListUpdateAction commands) {}

    protected void registerPrefixCommand(String command, BiConsumer<MessageReceivedEvent, String> function) {
        commands.put(command, function);
    }

    public Map<String, BiConsumer<MessageReceivedEvent, String>> getCommands() {
        return commands;
    }
}
