/*
 * Copyright (C) 2016-2017 David Alejandro Rubio Escares / Kodehawa
 *
 * Mantaro is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Mantaro is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mantaro.  If not, see http://www.gnu.org/licenses/
 */

package net.kodehawa.mantarobot.options.opts;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.kodehawa.mantarobot.commands.OptsCmd;
import net.kodehawa.mantarobot.commands.game.core.GameLobby;
import net.kodehawa.mantarobot.commands.interaction.polls.Poll;
import net.kodehawa.mantarobot.core.listeners.operations.InteractiveOperations;
import net.kodehawa.mantarobot.data.MantaroData;
import net.kodehawa.mantarobot.db.entities.DBGuild;
import net.kodehawa.mantarobot.db.entities.helpers.GuildData;
import net.kodehawa.mantarobot.options.annotations.Option;
import net.kodehawa.mantarobot.options.event.OptionRegistryEvent;
import net.kodehawa.mantarobot.utils.DiscordUtils;
import net.kodehawa.mantarobot.utils.commands.EmoteReference;

import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Option
@Slf4j
public class GeneralOptions extends OptionHandler {

    @Subscribe
    public void onRegistry(OptionRegistryEvent e) {
        registerOption("lobby:reset", "Lobby reset", "Fixes stuck game/poll/operations session.", event -> {
            GameLobby.LOBBYS.remove(event.getChannel());
            Poll.getRunningPolls().remove(event.getChannel().getId());
            Future<Void> stuck = InteractiveOperations.get(event.getChannel());
            if(stuck != null) stuck.cancel(true);
            event.getChannel().sendMessage(EmoteReference.CORRECT + "Reset the lobby correctly.").queue();
        });

        registerOption("modlog:blacklist", "Modlog blacklist",
                "Prevents an user from appearing in modlogs.\n" +
                        "You need the user mention.\n" +
                        "Example: ~>opts modlog blacklist @user",
                "Prevents an user from appearing in modlogs", event -> {
                    List<User> mentioned = event.getMessage().getMentionedUsers();
                    if(mentioned.isEmpty()) {
                        event.getChannel().sendMessage(EmoteReference.ERROR + "**You need to specify the users to locally blacklist from mod logs.**").queue();
                        return;
                    }

                    DBGuild dbGuild = MantaroData.db().getGuild(event.getGuild());
                    GuildData guildData = dbGuild.getData();

                    List<String> toBlackList = mentioned.stream().map(ISnowflake::getId).collect(Collectors.toList());
                    String blacklisted = mentioned.stream().map(user -> user.getName() + "#" + user.getDiscriminator()).collect(Collectors.joining(","));

                    guildData.getModlogBlacklistedPeople().addAll(toBlackList);
                    dbGuild.save();

                    event.getChannel().sendMessage(EmoteReference.CORRECT + "Locally blacklisted users from mod-log: **" + blacklisted + "**").queue();
                });

        registerOption("modlog:whitelist", "Modlog whitelist",
                "Allows an user from appearing in modlogs.\n" +
                        "You need the user mention.\n" +
                        "Example: ~>opts modlog whitelist @user",
                "Allows an user from appearing in modlogs (everyone by default)", event -> {
                    List<User> mentioned = event.getMessage().getMentionedUsers();
                    if(mentioned.isEmpty()) {
                        event.getChannel().sendMessage(EmoteReference.ERROR + "**You need to specify the users to locally whitelist from mod logs.**").queue();
                        return;
                    }

                    DBGuild dbGuild = MantaroData.db().getGuild(event.getGuild());
                    GuildData guildData = dbGuild.getData();

                    List<String> toUnBlacklist = mentioned.stream().map(ISnowflake::getId).collect(Collectors.toList());
                    String unBlacklisted = mentioned.stream().map(user -> user.getName() + "#" + user.getDiscriminator()).collect(Collectors.joining(","));

                    guildData.getModlogBlacklistedPeople().removeAll(toUnBlacklist);
                    dbGuild.save();

                    event.getChannel().sendMessage(EmoteReference.CORRECT + "Locally un-blacklisted users from mod-log: **" + unBlacklisted + "**").queue();
                });

        registerOption("linkprotection:toggle", "Link-protection toggle", "Toggles anti-link protection.", event -> {
            DBGuild dbGuild = MantaroData.db().getGuild(event.getGuild());
            GuildData guildData = dbGuild.getData();
            boolean toggler = guildData.isLinkProtection();

            guildData.setLinkProtection(!toggler);
            event.getChannel().sendMessage(EmoteReference.CORRECT + "Set link protection to " + "`" + !toggler + "`").queue();
            dbGuild.save();
        });

        registerOption("linkprotection:channel:allow", "Link-protection channel allow",
                "Allows the posting of invites on a channel.\n" +
                        "You need the channel name.\n" +
                        "Example: ~>opts linkprotection channel allow promote-here",
                "Allows the posting of invites on a channel.", (event, args) -> {
                    if(args.length == 0) {
                        OptsCmd.onHelp(event);
                        return;
                    }

                    DBGuild dbGuild = MantaroData.db().getGuild(event.getGuild());
                    GuildData guildData = dbGuild.getData();
                    String channelName = args[0];
                    List<TextChannel> textChannels = event.getGuild().getTextChannels().stream()
                            .filter(textChannel -> textChannel.getName().contains(channelName))
                            .collect(Collectors.toList());

                    if(textChannels.isEmpty()) {
                        event.getChannel().sendMessage(EmoteReference.ERROR + "There were no channels matching your search.").queue();
                    }

                    if(textChannels.size() <= 1) {
                        guildData.getLinkProtectionAllowedChannels().add(textChannels.get(0).getId());
                        dbGuild.save();
                        event.getChannel().sendMessage(EmoteReference.CORRECT + textChannels.get(0).getAsMention() + " can now be used to post discord invites.").queue();
                        return;
                    }

                    DiscordUtils.selectList(event, textChannels,
                            textChannel -> String.format("%s (ID: %s)", textChannel.getName(), textChannel.getId()),
                            s -> OptsCmd.getOpts().baseEmbed(event, "Select the Channel:").setDescription(s).build(),
                            textChannel -> {
                                guildData.getLinkProtectionAllowedChannels().add(textChannel.getId());
                                dbGuild.save();
                                event.getChannel().sendMessage(EmoteReference.OK + textChannel.getAsMention() + " can now be used to send discord invites.").queue();
                            }
                    );
                });

        registerOption("linkprotection:channel:disallow", "Link-protection channel disallow",
                "Disallows the posting of invites on a channel.\n" +
                        "You need the channel name.\n" +
                        "Example: ~>opts linkprotection channel disallow general",
                "Disallows the posting of invites on a channel (every channel by default)", (event, args) -> {
                    if(args.length == 0) {
                        OptsCmd.onHelp(event);
                        return;
                    }

                    DBGuild dbGuild = MantaroData.db().getGuild(event.getGuild());
                    GuildData guildData = dbGuild.getData();
                    String channelName = args[0];
                    List<TextChannel> textChannels = event.getGuild().getTextChannels().stream()
                            .filter(textChannel -> textChannel.getName().contains(channelName))
                            .collect(Collectors.toList());

                    if(textChannels.isEmpty()) {
                        event.getChannel().sendMessage(EmoteReference.ERROR + "There were no channels matching your search.").queue();
                    }

                    if(textChannels.size() <= 1) {
                        guildData.getLinkProtectionAllowedChannels().remove(textChannels.get(0).getId());
                        dbGuild.save();
                        event.getChannel().sendMessage(EmoteReference.CORRECT + textChannels.get(0).getAsMention() + " cannot longer be used to post discord invites.").queue();
                        return;
                    }

                    DiscordUtils.selectList(event, textChannels,
                            textChannel -> String.format("%s (ID: %s)", textChannel.getName(), textChannel.getId()),
                            s -> OptsCmd.getOpts().baseEmbed(event, "Select the Channel:").setDescription(s).build(),
                            textChannel -> {
                                guildData.getLinkProtectionAllowedChannels().remove(textChannel.getId());
                                dbGuild.save();
                                event.getChannel().sendMessage(EmoteReference.OK + textChannel.getAsMention() + " cannot longer be used to send discord invites.").queue();
                            }
                    );
                });

        registerOption("imageboard:tags:blacklist:add", "Blacklist imageboard tags", "Blacklists the specified imageboard tag from being looked up.",
                "Blacklist imageboard tags", (event, args) -> {
            if(args.length == 0) {
                event.getChannel().sendMessage(EmoteReference.ERROR + "You need to specify at least a tag to blacklist!").queue();
                return;
            }

            DBGuild dbGuild = MantaroData.db().getGuild(event.getGuild());
            GuildData guildData = dbGuild.getData();

            for(String tag : args) {
                guildData.getBlackListedImageTags().add(tag.toLowerCase());
            }

            dbGuild.saveAsync();
            event.getChannel().sendMessage(EmoteReference.CORRECT + "Successfully blacklisted " + String.join(" ,", args) + " from image search.").queue();
        });

        registerOption("imageboard:tags:blacklist:remove", "Un-blacklist imageboard tags", "Un-blacklist the specified imageboard tag from being looked up.",
                "Un-blacklist imageboard tags", (event, args) -> {
                    if(args.length == 0) {
                        event.getChannel().sendMessage(EmoteReference.ERROR + "You need to specify at least a tag to un-blacklist!").queue();
                        return;
                    }

                    DBGuild dbGuild = MantaroData.db().getGuild(event.getGuild());
                    GuildData guildData = dbGuild.getData();

                    for(String tag : args) {
                        guildData.getBlackListedImageTags().remove(tag.toLowerCase());
                    }

                    dbGuild.saveAsync();
                    event.getChannel().sendMessage(EmoteReference.CORRECT + "Successfully un-blacklisted " + String.join(" ,", args) + " from image search.").queue();
        });
    }

    @Override
    public String description() {
        return "Everything that doesn't fit anywhere else.";
    }
}
