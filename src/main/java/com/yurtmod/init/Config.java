package com.yurtmod.init;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;

public class Config 
{
	public static boolean SUPER_MALLET_CREATIVE_ONLY;
	
	public static boolean ALLOW_REFUND;
	public static boolean ALLOW_PLAYER_COLLIDE;
	public static boolean ALLOW_NONPLAYER_COLLIDE;
	public static int TEPEE_DECORATED_CHANCE;
	
	public static boolean ALLOW_SLEEP_TENT_DIM;
	public static boolean ALLOW_TELEPORT_TENT_DIM;
	public static boolean ALLOW_RESPAWN_INTERCEPT;
	
	public static boolean IS_TENT_FIREPROOF;
	
	public static int DIM_ID;

	public static void mainRegistry(Configuration config)
	{
		config.load();

		SUPER_MALLET_CREATIVE_ONLY = config.getBoolean("Super Mallet Creative Only", Configuration.CATEGORY_GENERAL, false, 
				"When true, only Creative-mode players can use the Super Tent Mallet");
		TEPEE_DECORATED_CHANCE = config.getInt("Tepee Design Chance", Configuration.CATEGORY_GENERAL, 35, 0, 100, 
				"Percentage chance that a plain tepee block will randomly have a design");
		ALLOW_TELEPORT_TENT_DIM = !config.getBoolean("Restrict Teleporting", Configuration.CATEGORY_GENERAL, true, 
				"When true, only creative-mode players can teleport within the Tent Dimension");
		ALLOW_SLEEP_TENT_DIM = !config.getBoolean("Beds explode in Tent Dim", Configuration.CATEGORY_GENERAL, false, 
				"When true, beds used in the Tent Dimension will explode.");
		ALLOW_PLAYER_COLLIDE = config.getBoolean("Allow Player Walk-In", Configuration.CATEGORY_GENERAL, true, 
				"[Experimental] When true, players can enter the tent by walking through the door");
		ALLOW_NONPLAYER_COLLIDE = config.getBoolean("Allow Entity Walk-In", Configuration.CATEGORY_GENERAL, false, 
				"[Experimental] When true, non-player entities can enter the tent by walking through the door");
		ALLOW_RESPAWN_INTERCEPT = config.getBoolean("Allow Respawn Logic", Configuration.CATEGORY_GENERAL, true, 
				"When true, players who die in Tent Dimension will be sent to overworld IF they have no bed. Disable if buggy.");
		DIM_ID = config.getInt("Tent Dimension ID", Configuration.CATEGORY_GENERAL, DimensionManager.getNextFreeDimId(), -255, 255, 
				"The ID for the Tent Dimension. **Delete this field whenever you add/remove a dimension-adding mod**");
		IS_TENT_FIREPROOF = config.getBoolean("Is Tent Fireproof", Configuration.CATEGORY_GENERAL, false, 
				"When true, the tent item will not be destroyed if it is burned");
		ALLOW_REFUND = config.getBoolean("Allow tent refund", Configuration.CATEGORY_GENERAL, true, 
				"When true, using an invalid tent item makes it drop new crafting materials for that item");
		
		config.save();
	}
}
