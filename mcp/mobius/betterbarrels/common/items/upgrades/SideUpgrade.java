package mcp.mobius.betterbarrels.common.items.upgrades;

import mcp.mobius.betterbarrels.mod_BetterBarrels;
import net.minecraft.item.Item;

public class SideUpgrade {
	public static int NONE    = 0x0; 
	public static int FRONT   = 0x1;
	public static int STICKER = 0x2;
	
	public static Item[] mapItem = {
		null,
		null,
		mod_BetterBarrels.itemSticker
	};
	
	public static int[] mapMeta = {
		0,
		0,
		0
	};	
}
