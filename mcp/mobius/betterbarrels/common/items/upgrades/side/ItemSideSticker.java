package mcp.mobius.betterbarrels.common.items.upgrades.side;

import mcp.mobius.betterbarrels.mod_BetterBarrels;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;

public class ItemSideSticker extends Item {
	public ItemSideSticker(int id){
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
