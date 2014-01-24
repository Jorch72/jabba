package mcp.mobius.betterbarrels.common.items.upgrades.side;

import mcp.mobius.betterbarrels.mod_BetterBarrels;
import mcp.mobius.betterbarrels.common.BaseProxy;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBarrelSticker extends Item {
	public ItemBarrelSticker(int id){
		super(id);
        //this.setIconIndex(16);
		this.setUnlocalizedName("ItemBarrelSticker");
        //this.setItemName("ItemBarrelSticker");
	}

    @Override    
    public void registerIcons(IconRegister par1IconRegister)
    {
    	this.itemIcon  = par1IconRegister.registerIcon(mod_BetterBarrels.modid + ":" + "label");
    }	
	
}
