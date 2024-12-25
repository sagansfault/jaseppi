package sh.sagan.jaseppi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.Web;

public class JaseppiAudioManager {

    private final AudioPlayerManager audioPlayerManager;
    private final AudioPlayer player;
    private final TrackScheduler trackScheduler;

    public JaseppiAudioManager(String poToken, String visitorData) {
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        Web.setPoTokenAndVisitorData(poToken, visitorData);
        YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager(true);
        audioPlayerManager.registerSourceManager(youtubeAudioSourceManager);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager, com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class);

        this.player = audioPlayerManager.createPlayer();
        this.trackScheduler = new TrackScheduler(player);
        player.addListener(trackScheduler);
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }
}
