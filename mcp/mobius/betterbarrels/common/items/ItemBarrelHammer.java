package mcp.mobius.betterbarrels.common.items;

import mcp.mobius.betterbarrels.BetterBarrels;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class ItemBarrelHammer extends Item {
	
	public ItemBarrelHammer(int id){
		super(id);
        this.setMaxStackSize(1); 		
	}
	
	@Override
    public boolean shouldPassSneakingClickToBlock(World par2World, int par4, int par5, int par6)
    {
        return true;
    }
	
    @Override    
    public void registerIcons(IconRegister par1IconRegister)
    {
    	this.itemIcon    = par1IconRegister.registerIcon(BetterBarrels.modid + ":" + "hammer");
    }    
    
    @Override
    public Icon getIconFromDamage(int dmg)
    {
    	return this.itemIcon;
    }  	
}
