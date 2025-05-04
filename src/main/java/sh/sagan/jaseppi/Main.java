package sh.sagan.jaseppi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

public class Main {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static void main(String[] args) {

        String read;
        try {
            read = new String(Files.readAllBytes(Path.of("config.json")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Config config = GSON.fromJson(read, Config.class);

        String token = config.getDiscordToken();
        JDA jda = JDABuilder.createLight(token, EnumSet.of(GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS))
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .enableCache(CacheFlag.VOICE_STATE)
                .build();
        Jaseppi jaseppi = Jaseppi.create(jda, config);
    }
}