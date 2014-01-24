package mcp.mobius.betterbarrels.common.items.upgrades;

import mcp.mobius.betterbarrels.mod_BetterBarrels;
import net.minecraft.item.Item;

public class UpgradeSide {
	public static int NONE     = 0x0; 
	public static int FRONT    = 0x1;
	public static int STICKER  = 0x2;
	public static int HOPPER   = 0x3;
	public static int REDSTONE = 0x4;
	
	public static Item[] mapItem = {
		null,
		null,
		mod_BetterBarrels.itemUpgradeSide,
		mod_BetterBarrels.itemUpgradeSide,
		mod_BetterBarrels.itemUpgradeSide,
	};
	
	public static int[] mapMeta = {
		0,
		0,
		0,
		1,
		2
	};
	
	public static int[] mapRevMeta = {
		STICKER,
		HOPPER,
		REDSTONE,
	};		
}
