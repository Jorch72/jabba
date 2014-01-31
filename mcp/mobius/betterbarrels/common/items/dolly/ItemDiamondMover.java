package mcp.mobius.betterbarrels.common.items.dolly;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemDiamondMover extends ItemBarrelMover {

    static {
    	classExtensionsNames.add("net.minecraft.tileentity.TileEntityMobSpawner");    	
    	
    	for (String s : classExtensionsNames){
    		try {
    			classExtensions.add(Class.forName(s));
    			classMap.put(s, Class.forName(s));
    		}
    		catch (ClassNotFoundException e){
    			classExtensions.add(null);
    		}
    	}
    }	
	
	public ItemDiamondMover(int id){
		super(id);
		this.setUnlocalizedName("dolly.diamond.empty");
        this.setMaxDamage(6); 
	}
	
	@Override	
    public String getUnlocalizedName(ItemStack stack)
    {   
		if (stack.getItemDamage() == 0)
			return "item.dolly.diamond.empty";
		else
			return "item.dolly.diamond.full";
    }	
}
