package mcp.mobius.betterbarrels.common.items.upgrades;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.betterbarrels.mod_BetterBarrels;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;


public class ItemUpgradeCore extends ItemUpgrade {
	public static Icon[] upgradeIcons = new Icon[4];
	
	/*
    public static String[] upgradeNames = { "upgrade.core.storage",
    										"upgrade.core.ender",
    										"upgrade.core.redstone",
    										"upgrade.core.hopper"
    									};	
    */
    
    public static String[] upgradeNames = { "Storage upgrade",
											"BSpace upgrade",
											"Redstone upgrade",
											"Hopper upgrade"
										};		
	
	public ItemUpgradeCore(int id){
		super(id);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(16);
        this.setUnlocalizedName("upgrade.core.generic");    		
	}
	
	@Override	
    public String getUnlocalizedName(ItemStack stack)
    {   
		if (stack.getItemDamage() >= ItemUpgradeCore.upgradeNames.length)
			return "Core Upgrade [ERROR]";
		
		return ItemUpgradeCore.upgradeNames[stack.getItemDamage()];
    }	    
    
    @Override    
    public void registerIcons(IconRegister par1IconRegister)
    {
    	for(int i=0 ; i < ItemUpgradeCore.upgradeNames.length; i++)
    		ItemUpgradeCore.upgradeIcons[i]  = par1IconRegister.registerIcon(mod_BetterBarrels.modid + ":" + "coreupg_" + String.valueOf(i));
    }	
	
    @Override
    public Icon getIconFromDamage(int i){
        return ItemUpgradeCore.upgradeIcons[i];
    }	
	
	@Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(int itemID, CreativeTabs tabs, List list){
            for(int i = 0; i < ItemUpgradeCore.upgradeIcons.length; ++i){
                    list.add(new ItemStack(itemID, 1, i));
             }
     }      
    
}
