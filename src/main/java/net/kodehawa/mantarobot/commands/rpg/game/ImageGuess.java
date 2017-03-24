package net.kodehawa.mantarobot.commands.rpg.game;

import br.com.brjdevs.java.utils.extensions.Async;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.kodehawa.mantarobot.commands.AnimeCmds;
import net.kodehawa.mantarobot.commands.rpg.entity.player.EntityPlayer;
import net.kodehawa.mantarobot.commands.rpg.game.core.Game;
import net.kodehawa.mantarobot.commands.rpg.game.core.GameReference;
import net.kodehawa.mantarobot.commands.rpg.world.TextChannelWorld;
import net.kodehawa.mantarobot.commands.utils.data.CharacterData;
import net.kodehawa.mantarobot.data.MantaroData;
import net.kodehawa.mantarobot.utils.Utils;
import net.kodehawa.mantarobot.utils.commands.EmoteReference;
import net.kodehawa.mantarobot.utils.data.GsonDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.Random;

import static net.kodehawa.mantarobot.utils.Utils.toByteArray;

public class ImageGuess extends Game {

	private static final Logger LOGGER = LoggerFactory.getLogger("Game[ImageGuess]");
	private int attempts = 0;
	private String authToken = AnimeCmds.authToken;
	private String characterName = null;
	private int maxAttempts = 10;
	private String[] search = {"Mato Kuroi", "Kotori Kanbe", "Kotarou Tennouji", "Akane Senri", "Misaki Mei", "Tomoe Mami"
		, "Shintaro Kisaragi", "Momo Kisaragi", "Takane Enomoto", "Ruuko Kominato", "Homura Akemi", "Madoka Kaname"};

	@Override
	public void call(GuildMessageReceivedEvent event, EntityPlayer player) {
		if (event.getAuthor().isFake() || !(EntityPlayer.getPlayer(event.getAuthor().getId()).getId() == player.getId() &&
				player.getGame() == type()
			&& !event.getMessage().getContent().startsWith(MantaroData.getData().get().getPrefix(event.getGuild())))) {
			return;
		}

		if (attempts > maxAttempts) {
			event.getChannel().sendMessage(EmoteReference.SAD + "You used all of your attempts, game is ending.").queue();
			endGame(event, player, false);
			return;
		}

		if (event.getMessage().getContent().equalsIgnoreCase(characterName)) {
			onSuccess(player, event);
			return;
		}

		if (event.getMessage().getContent().equalsIgnoreCase("end")) {
			endGame(event, player, false);
			return;
		}

		event.getChannel().sendMessage(EmoteReference.SAD + "That wasn't it! "
			+ EmoteReference.STOPWATCH + "You have " + (maxAttempts - attempts) + " attempts remaning").queue();

		attempts++;
	}

	@Override
	public boolean onStart(GuildMessageReceivedEvent event, GameReference type, EntityPlayer player) {
		player.setCurrentGame(type, event.getChannel());
		TextChannelWorld.of(event.getChannel()).addGame(player, this);
		int random = new Random().nextInt(search.length);
		try {
			String url = String.format("https://anilist.co/api/character/search/%1s?access_token=%2s", URLEncoder.encode(search[random], "UTF-8"), authToken);
			String json = Utils.wget(url, event);
			CharacterData[] character = GsonDataManager.GSON_PRETTY.fromJson(json, CharacterData[].class);

			String imageUrl = character[0].getImage_url_med();
			characterName = character[0].getName_first();
			if (characterName.equals("Takane")) characterName = "Ene";

			event.getChannel().sendMessage(new EmbedBuilder().setTitle("Guess the character", event.getJDA().getSelfUser().getAvatarUrl())
											.setImage(imageUrl).setFooter("You have 120 seconds to answer. (Type end to end the game)", null).build()).queue();
			super.onStart(TextChannelWorld.of(event.getChannel()), event, player);
			return true;
		} catch (Exception e) {
			onError(LOGGER, event, player, e);
			return false;
		}
	}

	@Override
	public GameReference type() {
		return GameReference.IMAGEGUESS;
	}

	public String getCharacterName() {
		return characterName;
	}
}