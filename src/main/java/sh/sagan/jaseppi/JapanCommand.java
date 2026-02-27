package sh.sagan.jaseppi;

public class JapanCommand extends JaseppiCommandHandler {

    private static final long TAKEOFF_MILLIS = 1788975000000L;

    public JapanCommand(Jaseppi jaseppi) {
        super(jaseppi);

        registerPrefixCommand("japan", (event, args) -> {
            long millis = TAKEOFF_MILLIS - System.currentTimeMillis();
            long days = millis / (24 * 60 * 60 * 1000);
            long hours = (millis / (60 * 60 * 1000)) % 24;
            long minutes = (millis / (60 * 1000)) % 60;
            long seconds = (millis / 1000) % 60;
            String message = "";
            if (days > 0) {
                message += days + "d ";
            }
            if (hours > 0) {
                message += hours + "h ";
            }
            if (minutes > 0) {
                message += minutes + "m ";
            }
            if (seconds > 0) {
                message += seconds + "s";
            }
            event.getMessage().reply(message).queue();
        });
    }
}
