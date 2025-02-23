package sh.sagan.jaseppi;

public class Config {

    private String discordToken;
    private String deeplAPIKey;
    private String youtubeRefreshToken;

    public Config() {

    }

    public String getDeeplAPIKey() {
        return deeplAPIKey;
    }

    public String getDiscordToken() {
        return discordToken;
    }

    public String getYoutubeRefreshToken() {
        return youtubeRefreshToken;
    }
}
