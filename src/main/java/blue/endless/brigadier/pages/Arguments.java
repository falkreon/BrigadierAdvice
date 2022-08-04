package blue.endless.brigadier.pages;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class Arguments {
	
	public static void register() {
		/*
		 *                       #### Accepting Arguments ####
		 * 
		 * Most of the non-literal nodes in Brigadier are different kinds of arguments. Arguments go in the
		 * tree in the order they're accepted, so that the library knows what to suggest and can turn things
		 * red if you're doing it wrong. Each argument is given a name (which is visible in the usage
		 * description). When you're executing the command, you can retrieve those arguments by the names
		 * you gave their nodes.
		 * 
		 * 
		 * We're going to start by implementing a kill2 command that can take an entity argument:
		 * Usage: /kill2 <entity>
		 * 
		 * 
		 *           "/"
		 *            |
		 *          kill2
		 *            |
		 *         <target>
		 * 
		 * 
		 * I'm also going to show off more of a hybrid approach for building the commands.
		 */
		
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			
			var kill2Node = CommandManager
					.literal("kill2")
					.executes(Arguments::kill2);
			
			var arg = CommandManager.argument("target", EntityArgumentType.entity());
			
			dispatcher.register(
					kill2Node
					.then(arg)
					);
			
		});
		
		/*
		 * (see below for how we unpack the Entity at the other end)
		 * 
		 * 
		 * Non-exhaustive list of other argument types available:
		 * Angle, Block, BlockMirror, BlockPos, BlockPredicate, BlockRotation, BlockState, Color, ColumnPos,
		 * CommandFunction, Coordinate, Dimension, Enchantment, EntityAnchor, Entity, EntitySummon, Enum, GameProfile,
		 * Identifier, ItemPredicate, ItemSlot, ItemStack, LookingPos, Message, NbtCompound, NbtElement, NumberRange,
		 * Operation, ParticleEffect, Pos, RegistryKey, RegistryPredicate, Rotation, ScoreboardCriterion,
		 * ScoreboardObjective, ScoreboardSlot, ScoreHolder, StatusEffect, Swizzle, Team, TextFunction, Text, Time,
		 * Uuid, Vec2, Vec3.
		 * 
		 * Each one has a FooArgumentType that you can use like I just did above with Entity.
		 * 
		 * You can add custom argument types too! It's tedious; look at Identifier.fromCommandInput() to see how these
		 * things typically get parsed in from the command.
		 * 
		 * 
		 * Text is useful if you want to do some manual parsing, or if you just need "whatever is on the rest of the line".
		 * 
		 * Now we're going to implement a tell2 command that will tell another player some text:
		 * Usage: /tell2 <player> <message>
		 * 
		 * 
		 *           "/"
		 *            |
		 *          tell2
		 *            |
		 *         <player>
		 *            |
		 *        <message>
		 * 
		 */
		

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			
			var tell2Node = CommandManager
					.literal("tell2")
					.build();
			
			var player = CommandManager.argument("player", EntityArgumentType.player()).build();
			
			var message = CommandManager
					.argument("message", MessageArgumentType.message())
					.executes(Arguments::tell2)
					.build();
			
			
			dispatcher.getRoot().addChild(tell2Node);
			tell2Node.addChild(player);
			player.addChild(message);
			
			/*
			 * A note on fluent construction:
			 * 
			 * This command is *much* easier to construct manually, because the fluent construction calls return
			 * the wrong result. Intuitively, one might think that:
			 * 
			 * a.then(b).then(c)
			 * 
			 * would construct the tree:
			 * 
			 *     a
			 *      \
			 *       b
			 *        \
			 *         c
			 * 
			 * whereas it actually constructs the tree:
			 * 
			 *     a
			 *    / \
			 *   b   c
			 * 
			 * In order to construct the above tree, you need to say:
			 * 
			 *    a.then(b.then(c))
			 * 
			 * but of course, these are builders, not nodes, so you need to do everything together:
			 * 
			 *    a.then(b.then(c.executes(command)))
			 * 
			 * which is possible to keep organized through diligent indenting, but I wouldn't recommend it.
			 */
			
		});
		
	}
	
	
	/*
	 * Note: We throw a CommandSyntaxException here; Mojang's first party code silently eats these in their lambdas.
	 */
	public static int kill2(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		Entity target = EntityArgumentType.getEntity(context, "target");
			
		target.kill();
		context.getSource().sendFeedback(Text.translatable("commands.kill.success.single", target.getDisplayName()), true);
		
		return 1;
	}
	
	
	public static int tell2(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
		Text message = MessageArgumentType.getMessage(context, "message");
		
		//Hack the message together from all the Text bits
		MutableText finalMessage = Text.literal("[")
				.append(context.getSource().getDisplayName())
				.append(Text.literal("]: "))
				.append(message);
		
		//The "right" way to do this is "message arguments", but we're not going to get into that. This isn't a Text
		//tutorial, it's a Brigadier tutorial.
		
		player.sendMessageToClient(finalMessage, false);
		
		return 1;
	}
	
}
