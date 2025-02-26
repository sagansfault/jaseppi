package sh.sagan.jaseppi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import sh.sagan.jaseppi.Jaseppi;
import sh.sagan.jaseppi.JaseppiCommandHandler;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AudioCommands extends JaseppiCommandHandler {

    public AudioCommands(Jaseppi jaseppi) {
        super(jaseppi);

        registerPrefixCommand("play", this::handlePlay);
        registerPrefixCommand("leave", this::handleLeave);
        registerPrefixCommand("skip", this::handleSkip);
        registerPrefixCommand("repeat", this::handleRepeat);
    }

    private AudioChannelUnion getChannel(MessageReceivedEvent event) {
        Message message = event.getMessage();
        Member member = event.getMember();
        if (member == null) {
            return null;
        }
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null) {
            message.reply("Hop in vc").queue();
            return null;
        }
        AudioChannelUnion channel = voiceState.getChannel();
        if (channel == null) {
            message.reply("Hop in a channel").queue();
            return null;
        }
        return channel;
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        AudioChannelUnion channel = event.getChannelLeft();
        if (channel == null) {
            return;
        }
        AudioManager audioManager = event.getGuild().getAudioManager();
        AudioChannelUnion connectedChannel = audioManager.getConnectedChannel();
        if (connectedChannel == null) {
            return;
        }
        if (channel.getIdLong() != connectedChannel.getIdLong()) {
            return;
        }
        if (channel.asVoiceChannel().getMembers().size() != 1) {
            return;
        }
        jaseppi.getAudioManager().getTrackScheduler().setRepeat(false);
        jaseppi.getAudioManager().getTrackScheduler().clearQueue();
        audioManager.closeAudioConnection();
    }

    private void handlePlay(MessageReceivedEvent event, String args) {
        AudioChannelUnion channel = getChannel(event);
        if (channel == null) {
            return;
        }
        AudioManager audioManager = event.getGuild().getAudioManager();
        Message message = event.getMessage();

        AudioPlayerManager audioPlayerManager = jaseppi.getAudioManager().getAudioPlayerManager();
        String query = args.trim();
        boolean link = query.startsWith("http");
        if (!link) {
            query = String.format("ytsearch:%s", query);
        }
        audioPlayerManager.loadItem(query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                connectAndPlay(audioManager, channel, track);
                String reply = "```" + track.getInfo().title + "```";
                message.reply("Queued\n" + reply).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                if (link) {
                    connectAndPlay(audioManager, channel, tracks);
                    String reply = tracks.stream().map(t -> t.getInfo().title).collect(Collectors.joining("\n"));
                    reply = "```" + reply + "```";
                    message.reply("Queued\n" + reply).queue();
                } else {
                    AudioTrack first = tracks.get(0);
                    if (first == null) {
                        return;
                    }
                    trackLoaded(first);
                }
            }

            @Override
            public void noMatches() {
                message.reply("Nothing found").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                message.reply("Could not play: " + exception.getMessage()).queue();
                exception.printStackTrace();
            }
        });
    }

    private void connectAndPlay(AudioManager audioManager, AudioChannelUnion channel, AudioTrack track) {
        connectAndSetHandler(audioManager, channel);
        jaseppi.getAudioManager().getTrackScheduler().queue(track);
    }

    private void connectAndPlay(AudioManager audioManager, AudioChannelUnion channel, Collection<AudioTrack> tracks) {
        connectAndSetHandler(audioManager, channel);
        for (AudioTrack track : tracks) {
            jaseppi.getAudioManager().getTrackScheduler().queue(track);
        }
    }

    private void connectAndSetHandler(AudioManager audioManager, AudioChannelUnion channel) {
        AudioPlayerSendHandler handler = new AudioPlayerSendHandler(jaseppi);
        audioManager.setSendingHandler(handler);
        audioManager.setReceivingHandler(handler);
        AudioChannelUnion connected = audioManager.getConnectedChannel();
        if (connected == null || connected.getIdLong() != channel.getIdLong()) {
            audioManager.openAudioConnection(channel);
        }
    }

    private void handleLeave(MessageReceivedEvent event, String args) {
        AudioManager audioManager = event.getGuild().getAudioManager();
        jaseppi.getAudioManager().getTrackScheduler().setRepeat(false);
        jaseppi.getAudioManager().getTrackScheduler().clearQueue();
        audioManager.closeAudioConnection();
        event.getMessage().reply("Bye").queue();
    }

    private void handleSkip(MessageReceivedEvent event, String args) {
        jaseppi.getAudioManager().getTrackScheduler().setRepeat(false);
        jaseppi.getAudioManager().getTrackScheduler().nextTrack();
        event.getMessage().reply("Skipped").queue();
    }

    private void handleRepeat(MessageReceivedEvent event, String args) {
        jaseppi.getAudioManager().getTrackScheduler().setRepeat(true);
        event.getMessage().reply("Repeating").queue();
    }
}
