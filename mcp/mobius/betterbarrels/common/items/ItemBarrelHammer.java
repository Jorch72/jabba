package mcp.mobius.betterbarrels.common.items;

import mcp.mobius.betterbarrels.common.BaseProxy;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class ItemBarrelHammer extends Item {
	public ItemBarrelHammer(int id){
		super(id);
	}
	
	@Override
    public boolean shouldPassSneakingClickToBlock(World par2World, int par4, int par5, int par6)
    {
        return true;
    }
}
