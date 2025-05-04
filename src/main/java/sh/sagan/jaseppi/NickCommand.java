package sh.sagan.jaseppi;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class NickCommand extends JaseppiCommandHandler {

    public NickCommand(Jaseppi jaseppi) {
        super(jaseppi);

        registerPrefixCommand("nick", (event, args) -> {
            Message message = event.getMessage();
            List<User> users = message.getMentions().getUsers();
            if (users.isEmpty()) {
                message.reply("mention someone: .nick @quiv Dancing King").queue();
                return;
            }
            User user = users.get(0);
            Member member = event.getGuild().getMember(user);
            if (member == null) {
                message.reply("could not find member " + user + " in this server").queue();
                return;
            }
            String nick = args.substring(args.indexOf(" "));
            member.modifyNickname(nick).queue();
        });
    }
}
