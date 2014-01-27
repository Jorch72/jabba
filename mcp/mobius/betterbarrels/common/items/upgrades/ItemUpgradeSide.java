package mcp.mobius.betterbarrels.common.items.upgrades;

import java.util.List;

import mcp.mobius.betterbarrels.BetterBarrels;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class ItemUpgradeSide extends ItemUpgrade {
	public static Icon[] upgradeIcons = new Icon[3];
	
    public static String[] upgradeNames = { "Sticker",
    										"Hopper facade",
    										"Redstone facade"
    									};	
	
	public ItemUpgradeSide(int id){
		super(id);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(16);
        this.setUnlocalizedName("Generic core upgrade");    		
	}
	
	@Override	
    public String getUnlocalizedName(ItemStack stack)
    {   
		if (stack.getItemDamage() >= ItemUpgradeSide.upgradeNames.length)
			return "Core Upgrade [ERROR]";
		
		return ItemUpgradeSide.upgradeNames[stack.getItemDamage()];
    }	    
    
    @Override    
    public void registerIcons(IconRegister par1IconRegister)
    {
    	for(int i=0 ; i < ItemUpgradeSide.upgradeNames.length; i++)
    		ItemUpgradeSide.upgradeIcons[i]  = par1IconRegister.registerIcon(BetterBarrels.modid + ":" + "sideupg_" + String.valueOf(i));
    }	
	
    @Override
    public Icon getIconFromDamage(int i){
        return ItemUpgradeSide.upgradeIcons[i];
    }	
	
	@Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(int itemID, CreativeTabs tabs, List list){
            for(int i = 0; i < ItemUpgradeSide.upgradeIcons.length; ++i){
                    list.add(new ItemStack(itemID, 1, i));
             }
     }      
    
}
