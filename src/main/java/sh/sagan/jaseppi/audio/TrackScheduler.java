package sh.sagan.jaseppi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.List;
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

    public String getQueueMessage(int newTracksFromEnd) {
        StringBuilder builder = new StringBuilder("```\n");
        int size = queue.size();
        int start = size - newTracksFromEnd;
        int index = 0;
        List<AudioTrack> tracks = new ArrayList<>(queue);
        AudioTrack playingTrack = player.getPlayingTrack();
        if (playingTrack != null) {
            tracks.add(0, playingTrack);
        }
        for (AudioTrack audioTrack : tracks) {
            if (index == 0) {
                builder.append("> ");
            } else {
                builder.append(index).append(". ");
            }
            builder.append(audioTrack.getInfo().title);
            if (index > start) {
                builder.append(" *");
            }
            builder.append("\n");
            index += 1;
        }
        builder.append("```");
        return builder.toString();
    }
}
