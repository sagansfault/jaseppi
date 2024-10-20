package sh.sagan.jaseppi.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import sh.sagan.jaseppi.Jaseppi;
import sh.sagan.jaseppi.JaseppiCommandHandler;

public class AudioCommands extends JaseppiCommandHandler {

    public AudioCommands(Jaseppi jaseppi) {
        super(jaseppi);
    }

    @Override
    public void register(CommandListUpdateAction commands) {
        commands.addCommands(
                Commands.slash("play", "Play audio")
                        .addOption(OptionType.STRING, "query", "Search query or link.", true)
                        .setGuildOnly(true),
                Commands.slash("leave", "Leave")
                        .setGuildOnly(true),
                Commands.slash("skip", "Skip a track")
                        .setGuildOnly(true),
                Commands.slash("repeat", "Repeat the current track")
                        .setGuildOnly(true)
        );
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

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Get outa my dms").queue();
            return;
        }
        Member member = event.getInteraction().getMember();
        if (member == null) {
            event.reply("?").queue();
            return;
        }
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null) {
            event.reply("Hop in vc").queue();
            return;
        }
        AudioChannelUnion channel = voiceState.getChannel();
        if (channel == null) {
            event.reply("Hop in a channel").queue();
            return;
        }
        AudioManager audioManager = guild.getAudioManager();
        switch (event.getName()) {
            case "play":
                handlePlay(event, channel, audioManager);
                break;
            case "leave":
                handleLeave(event, channel, audioManager);
                break;
            case "skip":
                handleSkip(event, channel, audioManager);
                break;
            case "repeat":
                handleRepeat(event, channel, audioManager);
                break;
        }
    }

    private void handlePlay(SlashCommandInteractionEvent event, @NotNull AudioChannelUnion channel, AudioManager audioManager) {
        event.deferReply().queue();
        AudioPlayerManager audioPlayerManager = jaseppi.getAudioManager().getAudioPlayerManager();
        String query = event.getOption("query").getAsString().trim();
        if (!query.startsWith("http")) {
            query = String.format("ytsearch:%s", query);
        }
        audioPlayerManager.loadItem(query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                connectAndPlay(audioManager, channel, track);
                String message = jaseppi.getAudioManager().getTrackScheduler().getQueueMessage(1);
                event.getHook().editOriginal("Queued\n" + message).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack track = playlist.getSelectedTrack();
                if (track == null) {
                    track = playlist.getTracks().get(0);
                }
                connectAndPlay(audioManager, channel, track);
                String message = jaseppi.getAudioManager().getTrackScheduler().getQueueMessage(1);
                event.getHook().editOriginal("Queued\n" + message).queue();
            }

            @Override
            public void noMatches() {
                event.getHook().editOriginal("Nothing found").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                event.getHook().editOriginal("Could not play: " + exception.getMessage()).queue();
                exception.printStackTrace();
            }
        });
    }

    private void connectAndPlay(AudioManager audioManager, AudioChannelUnion channel, AudioTrack track) {
        connectAndSetHandler(audioManager, channel);
        jaseppi.getAudioManager().getTrackScheduler().queue(track);
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

    private void handleLeave(SlashCommandInteractionEvent event, @NotNull AudioChannelUnion channel, AudioManager audioManager) {
        jaseppi.getAudioManager().getTrackScheduler().setRepeat(false);
        jaseppi.getAudioManager().getTrackScheduler().clearQueue();
        audioManager.closeAudioConnection();
        event.reply("Bye").queue();
    }

    private void handleSkip(SlashCommandInteractionEvent event, @NotNull AudioChannelUnion channel, AudioManager audioManager) {
        jaseppi.getAudioManager().getTrackScheduler().setRepeat(false);
        jaseppi.getAudioManager().getTrackScheduler().nextTrack();
        event.reply("Skipped").queue();
    }

    private void handleRepeat(SlashCommandInteractionEvent event, @NotNull AudioChannelUnion channel, AudioManager audioManager) {
        jaseppi.getAudioManager().getTrackScheduler().setRepeat(true);
        event.reply("Repeating").queue();
    }
}
