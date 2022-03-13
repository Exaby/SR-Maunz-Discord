package com.vauff.maunzdiscord.commands.slash;

import com.vauff.maunzdiscord.commands.templates.AbstractSlashCommand;
import com.vauff.maunzdiscord.core.Util;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class Minecraft extends AbstractSlashCommand<ChatInputInteractionEvent>
{
	@Override
	public void exe(ChatInputInteractionEvent event, Guild guild, MessageChannel channel, User author) throws Exception
	{
		String argument = event.getInteraction().getCommandInteraction().get().getOption("account").get().getValue().get().asString();
		BufferedReader uuidReader = new BufferedReader(new InputStreamReader(new URL("http://axis.iaero.me/uuidapi?uuid=" + argument + "&format=plain").openStream()));
		String uuidStatus = uuidReader.readLine();
		String username;

		if (uuidStatus.equals("") || uuidStatus.equals("Please input a valid 32-character UUID. (You may use Java format or plain text format, the 32 char limit does not count \"-\")") || uuidStatus.equals("Please input a valid format: (plain, json, xml)"))
		{
			username = argument;
		}
		else
		{
			username = uuidStatus;
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("http://axis.iaero.me/accinfo?username=" + username + "&format=csv").openStream()));
		String statusRaw = reader.readLine();
		String[] status = statusRaw.split(",");

		if (username.contains("#") || username.contains("&"))
		{
			event.editReply("The Minecraft account name **" + username + "** must be alphanumerical or contain an underscore.").block();
		}
		else
		{
			if (statusRaw.equalsIgnoreCase("unknown username"))
			{
				event.editReply("The Minecraft account name **" + username + "** is free and does not belong to any account!").block();
			}

			else if (statusRaw.equalsIgnoreCase("Username must be 16 characters or less."))
			{
				event.editReply("The Minecraft account name **" + username + "** must be 16 characters or less.").block();
			}

			else if (statusRaw.equalsIgnoreCase("Username must be alphanumerical (or contain '_')."))
			{
				event.editReply("The Minecraft account name **" + username + "** must be alphanumerical or contain an underscore.").block();
			}

			else if (statusRaw.contains(","))
			{
				String uuid = status[0];

				uuid = new StringBuilder(uuid).insert(uuid.length() - 24, "-").toString();
				uuid = new StringBuilder(uuid).insert(uuid.length() - 20, "-").toString();
				uuid = new StringBuilder(uuid).insert(uuid.length() - 16, "-").toString();
				uuid = new StringBuilder(uuid).insert(uuid.length() - 12, "-").toString();

				String headURL = "https://cravatar.eu/helmavatar/" + status[1] + "/120";
				URL constructedURL = new URL(headURL);

				EmbedCreateSpec embed = EmbedCreateSpec.builder()
					.color(Util.averageColorFromURL(constructedURL, true))
					.thumbnail(headURL)
					.footer("Powered by axis.iaero.me", "https://i.imgur.com/4o6K42Z.png")
					.addField("Name", status[1], true)
					.addField("Account Status", "Premium", true)
					.addField("Migrated", StringUtils.capitalize(status[2]), true)
					.addField("UUID", uuid, true)
					.addField("Skin", "https://minotar.net/body/" + status[1] + "/500.png", true)
					.addField("Raw Skin", "https://minotar.net/skin/" + status[1], true)
					.build();

				event.editReply().withEmbeds(embed).block();
			}
		}
	}

	@Override
	public ApplicationCommandRequest getCommand()
	{
		return ApplicationCommandRequest.builder()
			.name(getName())
			.description("Gives you full information about a Minecraft account")
			.addOption(ApplicationCommandOptionData.builder()
				.name("account")
				.description("Account username or UUID")
				.type(ApplicationCommandOption.Type.STRING.getValue())
				.required(true)
				.build())
			.build();
	}

	@Override
	public String getName()
	{
		return "minecraft";
	}

	@Override
	public BotPermission getPermissionLevel()
	{
		return BotPermission.EVERYONE;
	}
}
