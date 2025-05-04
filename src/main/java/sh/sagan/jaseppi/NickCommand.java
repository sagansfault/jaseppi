package sh.sagan.jaseppi;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class NickCommand extends JaseppiCommandHandler {

    public NickCommand(Jaseppi jaseppi) {
        super(jaseppi);

        registerPrefixCommand("nick", (event, args) -> {
            Message message = event.getMessage();
            List<Member> members = message.getMentions().getMembers();
            if (members.isEmpty()) {
                message.reply("mention someone: .nick @quiv Dancing King").queue();
                return;
            }
            Member member = members.get(0);
            String nick = args.substring(args.indexOf(" "));
            member.modifyNickname(nick).queue();
        });
    }
}
