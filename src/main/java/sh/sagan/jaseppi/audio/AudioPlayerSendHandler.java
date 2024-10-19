package sh.sagan.jaseppi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import sh.sagan.jaseppi.Jaseppi;

import java.nio.ByteBuffer;

public class AudioPlayerSendHandler implements AudioSendHandler, AudioReceiveHandler {

    private final Jaseppi jaseppi;
    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;

    public AudioPlayerSendHandler(Jaseppi jaseppi) {
        this.jaseppi = jaseppi;
        this.audioPlayer = jaseppi.getAudioManager().getPlayer();
    }

    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lastFrame.getData());
    }

    @Override
    public boolean isOpus() {
        return true;
    }

}