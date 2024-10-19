package sh.sagan.jaseppi;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.EnumSet;

public class Main {

    public static void main(String[] args) {
        String token = System.getenv("DISCORD_TOKEN");
        JDA jda = JDABuilder.createLight(token, EnumSet.of(GatewayIntent.GUILD_VOICE_STATES))
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .enableCache(CacheFlag.VOICE_STATE)
                .build();
        Jaseppi jaseppi = Jaseppi.create(jda);
    }
}