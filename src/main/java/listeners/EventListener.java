package listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import src.*;

public class EventListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event){
        User user = event.getUser();
        String emoji = event.getReaction().getEmoji().getAsReactionCode();
        String channelMention = event.getChannel().getAsMention();

        String message = user.getAsMention() + " reacted to a message with "+ emoji + " in the " + channelMention + " channel!";
        event.getChannel().sendMessage(message).queue();
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event){

        //JDA client = event.getJDA();
        Guild guild = event.getGuild();

        List<TextChannel> channels = guild.getTextChannelsByName("member", true);

        for(TextChannel ch : channels)
        {
            String name = event.getUser().getName();
            ch.sendMessage("**" + name + "** join the server!").queue();
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event){

        Guild guild = event.getGuild();
        List<TextChannel> channels = guild.getTextChannelsByName("member", true);

        for(TextChannel ch : channels)
        {
            String name = event.getUser().getName();
            ch.sendMessage("**" + name + "** left the server!").queue();
        }
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event){
        String name = event.getChannel().getName();
        //event.getGuild().getDefaultChannel().asStandardGuildMessageChannel().sendMessage(name).queue();
        JDA client = event.getJDA();
        //List<TextChannel> channels = client.getTextChannelsByName("Members Log", true);
        /*for(TextChannel ch : channels)
        {
            String name = event.getUser().getName();
            ch.sendMessage("**" + name + "** joined the server!");
        }*/
    }



    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event){
        String command = event.getName();
        switch (command) {
            case "calculate" -> {
                OptionMapping equation = event.getOption("input");
                if (equation == null) {
                    event.reply("You must provide an operation!").queue();
                    return;
                }
                SyntacticTree tree = new SyntacticTree(equation.getAsString());
                //event.reply(tree.generateTree()).queue();
                tree.generateTree(event);
            }
            case "image" -> {
                OptionMapping query = event.getOption("query");
                if (query == null) {
                    event.reply("You must provide a query!").queue();
                    return;
                }
                ArrayList<ArrayList<String>> results;
                try {
                    results = getImageURL.getURL(query.getAsString(), 1);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                EmbedBuilder msg = new EmbedBuilder();
                msg.setTitle("Image searcher");
                msg.setFooter("Image 1, Page 1");
                msg.setColor(new Color(109, 4, 125));

                if(results.isEmpty()) {
                    msg.setFooter("");
                    msg.setTitle("No image could be found with the provided query!");
                    event.replyEmbeds(msg.build()).setEphemeral(true).queue();
                    return;
                }
                ArrayList<String> hits = results.get(0);
                ArrayList<String> url = results.get(1);
                ArrayList<String> tags = results.get(2);

                if (hits.isEmpty()) {
                    msg.setFooter("");
                    msg.setTitle("No image could be found with the provided query!");
                    event.replyEmbeds(msg.build()).setEphemeral(true).queue();
                    return;
                } else {
                    List<Button> buttons = new ArrayList<>();
                    msg.addField("", "[Original image](" + url.get(1 % hits.size()) + ")", false);
                    msg.addField("Tags", tags.get(1 % hits.size()), false);
                    if (hits.size() > 1) {

                        int next = 2 % hits.size();
                        buttons.add(Button.primary("image_page" + "_" + (0) + "_" + query.getAsString(), Emoji.fromUnicode("U+23EA"))); //U+25C0

                        buttons.add(Button.primary("image_" + 0 + "_" + 1 + "_" + query.getAsString(), Emoji.fromUnicode("U+25C0"))); //U+23EA
                        buttons.add(Button.primary("image_" + 2 + "_" + 1 + "_" + query.getAsString(), Emoji.fromUnicode("U+25B6"))); //	U+23EA

                        buttons.add(Button.primary("image_page" + "_" + (2) + "_" + query.getAsString(), Emoji.fromUnicode("U+23E9"))); //U+25C0
                    }
                    msg.setImage(hits.get(1 % hits.size()));
                    event.replyEmbeds(msg.build()).addActionRow(buttons).setEphemeral(true).queue();
                }
            }
        }
    }


    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event){
        String[] args = event.getButton().getId().split("_");

        if(args[0].equals("image")) {

            int currPage = Integer.parseInt(args[2]);
            if (!args[1].equals("page")){

                ArrayList<ArrayList<String>> results;
                try {
                    results = getImageURL.getURL(args[3], Integer.parseInt(args[2]));
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                ArrayList<String> hits = results.get(0);
                ArrayList<String> url = results.get(1);
                ArrayList<String> tags = results.get(2);

                int pos = Integer.parseInt(args[1]) % hits.size();
                int prev_pos = pos - 1;
                if (prev_pos < 0)
                    prev_pos = hits.size() - 1;
                int next_pos = (pos + 1) % hits.size();

                if (hits.size() == 2) {
                    prev_pos = 0;
                    next_pos = 1;
                }

                EmbedBuilder msg = new EmbedBuilder();
                msg.setTitle("Image searcher");
                msg.setFooter("Image " + pos + ", Page " + currPage);
                msg.setColor(new Color(109, 4, 125));

                List<Button> buttons = new ArrayList<>();
                buttons.add(Button.primary("image_page" + "_" + (currPage-1) + "_" + args[3], Emoji.fromUnicode("U+23EA"))); //U+25C0

                buttons.add(Button.primary("image_" + prev_pos + "_" + currPage + "_" + args[3], Emoji.fromUnicode("U+25C0"))); //U+23EA
                buttons.add(Button.primary("image_" + next_pos + "_" + currPage + "_" + args[3], Emoji.fromUnicode("U+25B6"))); //	U+23EA

                buttons.add(Button.primary("image_page" + "_" + (currPage+1) + "_" + args[3], Emoji.fromUnicode("U+23E9"))); //U+25C0
                msg.setImage(hits.get(pos));
                msg.addField("", "[Original image](" + url.get(pos) + ")", false);
                msg.addField("Tags", tags.get(pos), false);
                //event.getMessage().editMessageEmbeds(msg.build()).setActionRow(buttons).queue();
                event.deferEdit().queue();
                event.getHook().editOriginalEmbeds(msg.build()).setActionRow(buttons).queue();

            } else {
                int newPage = currPage;

                if(newPage ==  0)
                    newPage = 1;
                System.out.println(newPage);
                ArrayList<ArrayList<String>> results = null;
                try {
                    results = getImageURL.getURL(args[3], newPage);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                newPage = Integer.parseInt(results.get(3).get(0));
                ArrayList<String> hits = results.get(0);
                ArrayList<String> url = results.get(1);
                ArrayList<String> tags = results.get(2);

                EmbedBuilder msg = new EmbedBuilder();
                msg.setTitle("Image searcher");
                msg.setFooter("Image " + 1 + ", Page " + newPage);
                msg.setColor(new Color(109, 4, 125));

                List<Button> buttons = new ArrayList<>();
                buttons.add(Button.primary("image_page" + "_" + (newPage-1) + "_" + args[3], Emoji.fromUnicode("U+23EA"))); //U+25C0

                buttons.add(Button.primary("image_" + 0 + "_" + newPage + "_" + args[3], Emoji.fromUnicode("U+25C0"))); //U+23EA
                buttons.add(Button.primary("image_" + 2 + "_" + newPage + "_" + args[3], Emoji.fromUnicode("U+25B6"))); //	U+23EA

                buttons.add(Button.primary("image_page" + "_" + (newPage+1) + "_" + args[3], Emoji.fromUnicode("U+23E9"))); //U+25C0
                msg.setImage(hits.get(1 % hits.size()));
                msg.addField("", "[Original image](" + url.get(1 % hits.size()) + ")", false);
                msg.addField("Tags", tags.get(1 % hits.size()), false);
                event.deferEdit().queue();
                event.getHook().editOriginalEmbeds(msg.build()).setActionRow(buttons).queue();

            }

        }

    }

    @Override
    public void onGuildReady(GuildReadyEvent event){
        List<CommandData> commandData = new ArrayList<>();
        //commandData.add(Commands.slash("vlad_e_f_urit", "adevarat sau nu"));
        //commandData.add(Commands.slash("calculate", "perform operation").addOption(OptionType.STRING, "input", "The operation you want to calculate", true));
        commandData.add(Commands.slash("image", "Finds an image.").addOption(OptionType.STRING, "query", "What image you want to find", true));
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event){
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("vlad_e_f_urit", "adevarat sau nu"));
        commandData.add(Commands.slash("calculate", "perform operation").addOption(OptionType.STRING, "input", "The operation you want to calculate", true));
        event.getJDA().updateCommands().addCommands(commandData).queue();
    }

}
