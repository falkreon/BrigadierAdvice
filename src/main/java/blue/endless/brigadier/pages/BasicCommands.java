package blue.endless.brigadier.pages;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.level.ServerWorldProperties;

public class BasicCommands {
	/*
	 *                       #### HOW TO BRIGADIER WITHOUT MAKING A MESS ####
	 * 
	 *              (This is a live / exectuable version of a gist with the same name.)
	 * 
	 * 
	 *                          #### So, let's talk about nodes. ####
	 * 
	 * 
	 *           "/"
	 *          /   \
	 *  test_kill   test_weather
	 *                /  |  \
	 *           clear rain  thunder
	 * 
	 * 
	 * 
	 * (we'll talk about the optional arguments for `kill` later.)
	 * 
	 * Most commands start with the slash (not part of brigadier) followed by the command name. This is
	 * a `LiteralCommandNode`. In fact, any bare token you see is probably a literal. The `clear`, `rain`,
	 * and `thunder` are also literal nodes, for example. So at least for now, we've got a whole tree that
	 * just contains the root node and some literal nodes.
	 * 
	 * 
	 * Some nodes on the tree have `Command` objects hung on them. You have a lot of flexibility on where
	 * to hang them. for `kill`, you'd definitely need to hang the KillCommand directly on the `kill` node,
	 * because it's the only one. But for `weather`, you could either hang one Command each onto `clear`,
	 * `rain`, and `thunder`, or you could hang a Command onto `weather` that could handle all three. If
	 * the "leaf" node doesn't contain an `executes` clause, Brigadier will walk one level back up the tree
	 * and check again, and keep doing so until it finds a command. If it walks all the way up to the root
	 * without finding one, it'll grab the "command usage" for that command, and print out an error with it.
	 * 
	 * 
	 *      It happens that vanilla puts the executes on `clear`, `rain`, and `thunder` for the
	 *      weather command,  so that's what I'll show here.
	 * 
	 * 
	 * 
	 */
	
	public static void register() {
		
		/*
		 *                          #### Building them cleanly ####
		 * 
		 * So, you've all seen how mojang puts them together, but how do you put them togehter in a way that's
		 * easy to read and understand?
		 * 
		 * 
		 * It is possible to put together a node all by itself. Just take your time with each node, build
		 * it clearly, and then stitch them together at the end:
		 */
		
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			//Make some new nodes
			LiteralCommandNode<ServerCommandSource> killNode = CommandManager
					.literal("test_kill")
					.executes(new CustomKillCommand())
					.build();

			LiteralCommandNode<ServerCommandSource> weatherNode = CommandManager
					.literal("test_weather")
					.build();

			LiteralCommandNode<ServerCommandSource> clearNode = CommandManager
					.literal("clear")
					.executes(CustomWeatherCommand::clear)
					.build();

			LiteralCommandNode<ServerCommandSource> rainNode = CommandManager
					.literal("rain")
					.executes(CustomWeatherCommand::rain)
					.build();

			LiteralCommandNode<ServerCommandSource> thunderNode = CommandManager
					.literal("thunder")
					.executes(CustomWeatherCommand::thunder)
					.build();

			//Now stitch them together

			//usage: /kill
			dispatcher.getRoot().addChild(killNode);

			//usage: /weather [clear|rain|thunder]
			dispatcher.getRoot().addChild(weatherNode);
			weatherNode.addChild(clearNode);
			weatherNode.addChild(rainNode);
			weatherNode.addChild(thunderNode);
		});
		
		/*
		 * So, in summary:
		 * 
		 * - addChild tells Brigadier how the command pieces are organized.
		 * 
		 * - When you type a slash in on the client to start a command, Brigadier starts at the slash at
		 *   the top of the tree, and as you keep typing, it takes different "branches" until it hits a part with
		 *   no more branches (we call this a "leaf"). At this point you can hit return and a valid command
		 *   will run.
		 * 
		 * - I didn't really show it here, but you can mix and match! Make a set of subnodes with the fluent
		 *   pattern, and then attach it to the main tree? Totally valid. Don't like how I organize commands
		 *   at all, and want to use Mojang's way? Also valid.
		 */
	}
	
	
	/*
	 * COMMAND IMPLEMENTATIONS
	 * 
	 * These aren't as important, they just give you something to test the system with, and show how the
	 * code you want to run connects up with the Brigadier system.
	 */
	
	public static class CustomKillCommand implements Command<ServerCommandSource> {
		@Override
		public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
			System.out.println(context.getSource().getDisplayName());
			//context.getPlayer().kill(); //Good enough for now!
			return 1; //positive numbers are success! Negative numbers are failure.
		}
	}
	
	public static class CustomWeatherCommand {
		public static int clear(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
			context.getSource().getWorld().setWeather(6000, 0, false, false);
			context.getSource().sendFeedback(Text.translatable("commands.weather.set.clear"), true);
			
			return 6000;
			//Often we return a positive number related to what we successfully did,
			//in this case the number of clear-weather ticks we set
		}

		public static int rain(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
			context.getSource().getWorld().setWeather(0, 6000, true, false);
			context.getSource().sendFeedback(Text.translatable("commands.weather.set.rain"), true);
			return 6000;
		}

		public static int thunder(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
			context.getSource().getWorld().setWeather(0, 6000, true, true);
			context.getSource().sendFeedback(Text.translatable("commands.weather.set.thunder"), true);
			return 6000;
		}
	}
}
