package mcp.mobius.betterbarrels.common.items.upgrades;

import mcp.mobius.betterbarrels.mod_BetterBarrels;
import net.minecraft.item.Item;

public class UpgradeCore {
	public static int NONE     = 0x00;
	public static int STORAGE  = 0x01;
	public static int ENDER    = 0x02;
	public static int REDSTONE = 0x03;
	public static int HOPPER   = 0x04;
	
	public static Item[] mapItem = {
		null,
		mod_BetterBarrels.itemUpgradeCore,
		mod_BetterBarrels.itemUpgradeCore,
		mod_BetterBarrels.itemUpgradeCore,
		mod_BetterBarrels.itemUpgradeCore,		
	};
	
	public static int[] mapMeta = {
		0,
		0,
		1,
		2,
		3,
	};		
}
