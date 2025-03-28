package sh.sagan.jaseppi;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import sh.sagan.sf6j.CharacterId;
import sh.sagan.sf6j.GameData;
import sh.sagan.sf6j.Gif;
import sh.sagan.sf6j.Move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SF6Commands extends JaseppiCommandHandler {

    private final GameData sf6GameData;

    public SF6Commands(Jaseppi jaseppi) {
        super(jaseppi);
        this.sf6GameData = jaseppi.getSF6GameData();
    }

    @Override
    public void registerSlashCommands(CommandListUpdateAction commands) {
        commands.addCommands(
                Commands.slash("sf6", "Print SF6 frame data")
                        .addOption(OptionType.STRING, "character", "The character of the move's frame data.", true, true)
                        .addOption(OptionType.STRING, "move", "The move of the character.", true, true)
                        .setGuildOnly(true)
        );
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            return;
        }
        if (!event.getName().equalsIgnoreCase("sf6")) {
            return;
        }
        String character = event.getOption("character").getAsString();
        String moveIdentifier = event.getOption("move").getAsString();
        CharacterId characterId = CharacterId.getByName(character);
        if (characterId == null) {
            event.reply("Could not find character").queue();
            return;
        }
        Move move = sf6GameData.getCharacterData().get(characterId).getMove(moveIdentifier);
        if (move == null) {
            event.reply("Could not find move").queue();
            return;
        }
        List<MessageEmbed> embeds = new ArrayList<>();
        embeds.add(new EmbedBuilder()
                .setTitle(move.getIdentifier())
                .setUrl("https://wiki.supercombo.gg/w/Street_Fighter_6")
                .addField("Damage", move.getDamage(), true)
                .addField("Guard", move.getGuard(), true)
                .addField("Cancel", move.getCancel(), true)
                .addField("Startup", move.getStartup(), true)
                .addField("Active", move.getActive(), true)
                .addField("Recovery", move.getRecovery(), true)
                .addField("On Block", move.getBlockAdvantage(), true)
                .addField("Armor", move.getArmor(), true)
                .addField("Invuln", move.getInvuln(), true)
                .setImage(move.getImage())
                .setFooter(move.getNotes())
                .build()
        );
        Gif gif = getGif(characterId, move);
        if (gif != null) {
            embeds.add(new EmbedBuilder()
                    .setUrl("https://wiki.supercombo.gg/w/Street_Fighter_6")
                    .setImage(gif.getGifURL())
                    .build()
            );
        }
        event.replyEmbeds(embeds).queue();
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equalsIgnoreCase("sf6")) {
            return;
        }
        AutoCompleteQuery focusedOption = event.getFocusedOption();
        String focusedOptionName = focusedOption.getName();
        if (focusedOptionName.equalsIgnoreCase("character")) {
            String currentValue = focusedOption.getValue().toLowerCase();
            List<Command.Choice> choices;
            if (currentValue.isBlank()) {
                choices = Arrays.stream(CharacterId.VALUES)
                        .map(CharacterId::getName)
                        .map(name -> new Command.Choice(name, name))
                        .limit(25)
                        .collect(Collectors.toList());
            } else {
                choices = Arrays.stream(CharacterId.VALUES)
                        .map(CharacterId::getName)
                        .filter(name -> name.toLowerCase().contains(currentValue.toLowerCase()))
                        .map(name -> new Command.Choice(name, name))
                        .limit(25)
                        .collect(Collectors.toList());
            }
            event.replyChoices(choices).queue();
        } else if (focusedOptionName.equalsIgnoreCase("move")) {
            OptionMapping characterOption = event.getOption("character");
            CharacterId characterId = CharacterId.getByName(characterOption.getAsString());
            if (characterId == null) {
                return;
            }
            String currentValue = focusedOption.getValue().toLowerCase();
            List<Command.Choice> choices;
            if (currentValue.isBlank()) {
                choices = sf6GameData.getCharacterData().get(characterId).getMoves().stream()
                        .map(Move::getIdentifier)
                        .map(identifier -> new Command.Choice(identifier, identifier))
                        .limit(25)
                        .collect(Collectors.toList());
            } else {
                choices = sf6GameData.getCharacterData().get(characterId).getMoves().stream()
                        .filter(move -> move.getIdentifier().toLowerCase().contains(currentValue) || move.getName().toLowerCase().contains(currentValue))
                        .map(move -> new Command.Choice(String.format("%s (%s)", move.getIdentifier(), move.getName()), move.getIdentifier()))
                        .limit(25)
                        .collect(Collectors.toList());
            }
            event.replyChoices(choices).queue();
        }
    }

    private Gif getGif(CharacterId characterId, Move move) {
        String moveName = move.getName()
                .replaceAll("Stand\\b", "Standing")
                .replaceAll("Crouch\\b", "Crouching")
                .replaceAll("HP", "Heavy Punch")
                .replaceAll("MP", "Medium Punch")
                .replaceAll("LP", "Light Punch")
                .replaceAll("HK", "Heavy Kick")
                .replaceAll("MK", "Medium Kick")
                .replaceAll("LK", "Light Kick")
                .toLowerCase();
        for (Gif gif : sf6GameData.getCharacterData().get(characterId).getGifs()) {
            String gifName = gif.getMoveName().toLowerCase();
            if (gifName.contains(moveName) || moveName.contains(gifName)) {
                return gif;
            }
        }
        return null;
    }
}
