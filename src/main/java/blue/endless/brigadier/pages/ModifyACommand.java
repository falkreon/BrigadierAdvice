package blue.endless.brigadier.pages;

import java.util.List;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class ModifyACommand {
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			
			CommandNode<ServerCommandSource> effectNode = dispatcher.getRoot().getChild("effect");
			CommandNode<ServerCommandSource> listNode = CommandManager.literal("list").executes(ModifyACommand::listEffects).build();
			
			effectNode.addChild(listNode);
		});
	}
	
	public static int listEffects(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayer();
		if (player==null) {
			context.getSource().sendError(Text.literal("You are not a Player, so you cannot have any potion effects."));
			return 0;
		} else {
			for(StatusEffectInstance effect : player.getStatusEffects()) {
				Style effectStyle = Style.EMPTY.withColor(0xFF_FFFFFF);
				if (effect.isAmbient()) {
					effectStyle = effectStyle.withColor(0xFF_52B985);
				}
				if (effect.isPermanent()) {
					effectStyle = effectStyle.withUnderline(true);
				}
				Text name = effect.getEffectType().getName();
				List<Text> withStyle = name.getWithStyle(effectStyle);
				MutableText message = MutableText.of(new LiteralTextContent(""));
				for(Text t : withStyle) {
					message.append(t);
				}
				message.append(" x"+effect.getAmplifier());
				message.append(" "+effect.getDuration()+"ticks");
				
				context.getSource().sendFeedback(message, false);
			}
			
		}
		
		
		return 0;
	}
}
