package sh.sagan.jaseppi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final ConcurrentLinkedDeque<AudioTrack> queue;
    private final AtomicBoolean repeat = new AtomicBoolean(false);

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new ConcurrentLinkedDeque<>();
    }

    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public void clearQueue() {
        player.startTrack(null, false);
        queue.clear();
    }

    public void nextTrack() {
        player.startTrack(queue.poll(), false);
    }

    public void setRepeat(boolean repeat) {
        this.repeat.set(repeat);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (repeat.get()) {
                queue.offerFirst(track.makeClone());
            }
            nextTrack();
        }
    }
}
