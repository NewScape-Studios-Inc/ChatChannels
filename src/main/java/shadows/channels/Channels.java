package shadows.channels;

import net.minecraft.util.text.TextFormatting;

public class Channels {

	public static Channel register(String id, String prefix, TextFormatting color) {
		return register(new Channel(id, prefix, color));
	}

	public static Channel register(Channel c) {
		Channel.REGISTRY.put(c.id, c);
		return c;
	}

	public static void init() {
		register("mature", "[Mature]", TextFormatting.RED);
		register(new LocalChannel("local", "[Local]", TextFormatting.GREEN));
	}
}
