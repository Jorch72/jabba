package mcp.mobius.betterbarrels.common.items;

import mcp.mobius.betterbarrels.mod_BetterBarrels;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class ItemBarrelLocker extends Item {
	
	public ItemBarrelLocker(int id){
		super(id);
        this.setMaxDamage(16);
        this.setMaxStackSize(1);
        this.setUnlocalizedName("Locking planks (WIP)");        
	}
	
	@Override
    public boolean shouldPassSneakingClickToBlock(World par2World, int par4, int par5, int par6)
    {
        return true;
    }

    @Override    
    public void registerIcons(IconRegister par1IconRegister)
    {
    	this.itemIcon  = par1IconRegister.registerIcon(mod_BetterBarrels.modid + ":" + "locking_planks");
    }	
}