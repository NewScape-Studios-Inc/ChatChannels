package shadows.channels;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/**
 * A chat channel.  Holds a list of uuids that messages will be dispatched to, and dispatches messages to them accordingly.
 */
public class Channel {

	public static final HashMap<String, Channel> REGISTRY = new HashMap<>();

	protected final Set<UUID> listeners = new HashSet<>();

	protected final String id;
	protected String prefix;
	protected TextFormatting color;

	public Channel(String id, String prefix, TextFormatting color) {
		this.id = id;
		this.prefix = prefix;
		this.color = color;
	}

	/**
	 * Sends a message to this channel, according to it's rules.
	 * @param source The player who originally sent the message.
	 * @param message The message to be sent.
	 */
	public void sendMessage(ServerPlayerEntity source, ITextComponent message) {
		ITextComponent msg = formatMsg(source, message);
		for (UUID u : listeners) {
			ServerPlayerEntity p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(u);
			if (p != null && canSendTo(source, p)) p.sendMessage(msg);
		}
	}

	protected ITextComponent formatMsg(ServerPlayerEntity source, ITextComponent message) {
		return new TranslationTextComponent("%s <%s> %s", prefix, source.getName(), message).applyTextStyle(color);
	}

	protected boolean canSendTo(ServerPlayerEntity source, ServerPlayerEntity dest) {
		return true;
	}

	public void addListener(UUID id) {
		listeners.add(id);
	}

	public void removeListener(UUID id) {
		listeners.remove(id);
	}

	public boolean isListening(UUID id) {
		return listeners.contains(id);
	}

}
