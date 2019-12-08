package shadows.channels;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import shadows.placebo.config.Configuration;

@Mod(ChatChannels.MODID)
public class ChatChannels {

	public static final String MODID = "chatchannels";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static int localChatDist = 50;

	public ChatChannels() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::setup);
	}

	@SubscribeEvent
	public void setup(FMLCommonSetupEvent e) {
		Channels.init();
		MinecraftForge.EVENT_BUS.addListener(this::starting);
		Configuration c = new Configuration(new File(FMLPaths.CONFIGDIR.get().toFile(), MODID + ".cfg"));
		localChatDist = c.getInt("Local Chat Radius", "general", localChatDist, 0, (int) Math.sqrt(Integer.MAX_VALUE), "How close (in blocks) a player must be to receive local messages from another player.");
		if (c.hasChanged()) c.save();
	}

	@SubscribeEvent
	public void starting(FMLServerStartingEvent e) {
		CommandDispatcher<CommandSource> disp = e.getServer().getCommandManager().getDispatcher();
		for (String s : Channel.REGISTRY.keySet()) {
			disp.register(Commands.literal(s).then(Commands.argument("msg", MessageArgument.message()).executes(c -> {
				Channel channel = Channel.REGISTRY.get(s);
				ITextComponent msg = MessageArgument.getMessage(c, "msg");
				if (channel.isListening(c.getSource().asPlayer().getUniqueID())) channel.sendMessage(c.getSource().asPlayer(), msg);
				else c.getSource().sendErrorMessage(new StringTextComponent("You must join this channel to send messages."));
				return 1;
			})));
		}

		disp.register(Commands.literal("join").then(Commands.argument("channel", StringArgumentType.string()).suggests((c, b) -> ISuggestionProvider.suggest(Channel.REGISTRY.keySet(), b)).executes(c -> {
			Channel channel = Channel.REGISTRY.get(c.getArgument("channel", String.class));
			if (channel != null) {
				channel.addListener(c.getSource().asPlayer().getUniqueID());
				c.getSource().sendFeedback(new StringTextComponent("Joined channel " + channel.id), false);
			} else c.getSource().sendErrorMessage(new StringTextComponent("Channel " + c.getArgument("channel", String.class) + " does not exist."));
			return 1;
		})));

		disp.register(Commands.literal("leave").then(Commands.argument("channel", StringArgumentType.string()).suggests((c, b) -> ISuggestionProvider.suggest(Channel.REGISTRY.keySet(), b)).executes(c -> {
			Channel channel = Channel.REGISTRY.get(c.getArgument("channel", String.class));
			if (channel != null) {
				channel.removeListener(c.getSource().asPlayer().getUniqueID());
				c.getSource().sendFeedback(new StringTextComponent("Left channel " + channel.id), false);
			} else c.getSource().sendErrorMessage(new StringTextComponent("Channel " + c.getArgument("channel", String.class) + " does not exist."));
			return 1;
		})));

		disp.register(Commands.literal("channels").executes(c -> {
			StringBuilder build = new StringBuilder("Channels: ");
			for (String s : Channel.REGISTRY.keySet())
				build.append(s + ", ");
			c.getSource().sendFeedback(new StringTextComponent(build.substring(0, build.length() - 2)), false);
			return 1;
		}));
	}

}
