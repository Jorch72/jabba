package mcp.mobius.betterbarrels.common.items;

import mcp.mobius.betterbarrels.mod_BetterBarrels;
import mcp.mobius.betterbarrels.common.BaseProxy;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBSpaceInterface extends ItemUpgrade {
	public ItemBSpaceInterface(int id){
		super(id);
        this.setMaxStackSize(16);
        this.setUnlocalizedName("B-Space Interface (WIP)");
        //this.setIconIndex(20);		
	}
	
	@Override
    public boolean shouldPassSneakingClickToBlock(World par2World, int par4, int par5, int par6)
    {
        return true;
    }

    @Override    
    public void registerIcons(IconRegister par1IconRegister)
    {
    	this.itemIcon  = par1IconRegister.registerIcon(mod_BetterBarrels.modid + ":" + "bspace_interface");
    }	
}
