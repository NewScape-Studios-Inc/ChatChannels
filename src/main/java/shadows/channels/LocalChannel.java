package shadows.channels;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TextFormatting;

public class LocalChannel extends Channel {

	public LocalChannel(String id, String prefix, TextFormatting color) {
		super(id, prefix, color);
	}

	@Override
	protected boolean canSendTo(ServerPlayerEntity source, ServerPlayerEntity dest) {
		return dest.world == source.world && dest.getDistanceSq(source) <= ChatChannels.localChatDist * ChatChannels.localChatDist;
	}

}
