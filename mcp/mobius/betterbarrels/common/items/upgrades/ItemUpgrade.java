package mcp.mobius.betterbarrels.common.items.upgrades;

import mcp.mobius.betterbarrels.common.items.IOverlayItem;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class ItemUpgrade extends Item implements IOverlayItem{
	public ItemUpgrade(int id){
		super(id);
	}
	
	@Override
    public boolean shouldPassSneakingClickToBlock(World par2World, int par4, int par5, int par6)
    {
        return true;
    }	
}
