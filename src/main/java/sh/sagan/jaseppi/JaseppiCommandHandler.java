package sh.sagan.jaseppi;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public abstract class JaseppiCommandHandler extends ListenerAdapter {

    protected final Jaseppi jaseppi;

    public JaseppiCommandHandler(Jaseppi jaseppi) {
        this.jaseppi = jaseppi;
    }

    public abstract void register(CommandListUpdateAction commands);
}
