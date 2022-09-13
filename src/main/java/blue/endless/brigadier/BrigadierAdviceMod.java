package blue.endless.brigadier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blue.endless.brigadier.pages.Arguments;
import blue.endless.brigadier.pages.BasicCommands;
import blue.endless.brigadier.pages.ModifyACommand;
import net.fabricmc.api.ModInitializer;

/*
 * HOW TO NAVIGATE THIS REPO
 * 
 * Start with BasicCommands.java - this is the original "How to Brigadier Without Making a Mess"
 * gist, except the commands are exectuable and updooted for minecraft 1.19 and recent Yarn shenanigans.
 * 
 * Implements test_kill and test_weather commands.
 * 
 * 
 * Arguments.java implements kill2 and tell2 commands, and demonstrates how to accept entity, player, and message
 * arguments, and discusses builder pattern issues which arise when constructing commands that have them.
 * 
 * 
 */

public class BrigadierAdviceMod implements ModInitializer {
	public static final String MOD_ID = "brigadier_advice";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	@Override
	public void onInitialize() {
		BasicCommands.register();
		Arguments.register();
		ModifyACommand.register();
	}
	
	
	
	
	
	
}
