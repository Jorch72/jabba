package mcp.mobius.betterbarrels.common.items.upgrades;

import mcp.mobius.betterbarrels.BetterBarrels;
import net.minecraft.item.Item;

public class UpgradeCore {
	public static int NONE     = 0x00;
	public static int STORAGE  = 0x01;
	public static int ENDER    = 0x02;
	public static int REDSTONE = 0x03;
	public static int HOPPER   = 0x04;
	
	public static Item[] mapItem = {
		null,
		BetterBarrels.itemUpgradeCore,
		BetterBarrels.itemUpgradeCore,
		BetterBarrels.itemUpgradeCore,
		BetterBarrels.itemUpgradeCore,		
	};
	
	public static int[] mapMeta = {	//Map index to metadata
		-1,
		0,
		1,
		2,
		3,
	};
	
	public static int[] mapRevMeta = {	//Map meta to index
		STORAGE,
		ENDER,
		REDSTONE,
		HOPPER
	};
	
    public static int[] mapMetaSlots = { 	//Map meta to slots
    	1,
		2,
		1,
		1
	};
    
    public static int[] mapSlots = {	// Map index to slots
    	-1,
    	1,
		2,
		1,
		1
	};	 
    
    public static String[] mapMetaDescript = {
    	"Increases the storage capacity by 64 stacks",
    	"Turns the barrel into a BSpace barrel",
    	"Allows the barrel to emit redstone",
    	"Allows the barrel to automatically output"
    };
}
