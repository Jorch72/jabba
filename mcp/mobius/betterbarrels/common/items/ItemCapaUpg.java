package mcp.mobius.betterbarrels.common.items;

import java.util.List;

import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.betterbarrels.mod_BetterBarrels;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class ItemCapaUpg extends ItemUpgrade{

	public Icon[] upgradeIcons = new Icon[7];
	
    public String[] upgradeNames = { "Capacity MKI (Wood)",		
			 						 "Capacity MKII (Iron)",
			 						 "Capacity MKIII (Gold)",
			 						 "Capacity MKIV (Diamond)",
			 						 "Capacity MKV (Obsidian)",
			 						 "Capacity MKVI (End stone)",
			 						 "Capacity MKVII (Emerald)" };
	
	
    public ItemCapaUpg(int par1)
    {
        super(par1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(16);
        this.setUnlocalizedName("Generic capacity upgrade");        
    }

    /*
	@Override	
    public String getLocalizedName(ItemStack par1ItemStack)
    {    	return "Upgrade Test";    }

	@Override	
    public String getUnlocalizedName()
    {    	return "Upgrade Test";    }
    */

	@Override	
    public String getUnlocalizedName(ItemStack stack)
    {   
		if (stack.getItemDamage() >= this.upgradeNames.length)
			return "Capacity Upgrade [ERROR]";
		
		return this.upgradeNames[stack.getItemDamage()];
    }	    
    
    @Override    
    public void registerIcons(IconRegister par1IconRegister)
    {
    	for(int i=0 ; i < 7; i++)
    		this.upgradeIcons[i]  = par1IconRegister.registerIcon(mod_BetterBarrels.modid + ":" + "capaupg_mk" + String.valueOf(i+1));
    }	
	
    @Override
    public Icon getIconFromDamage(int i){
        return this.upgradeIcons[i];
    }
    
    /*
    @Override
    public String getItemName(){
    	return "Test";
    }
    
    @Override
    public String getItemNameIS(ItemStack stack){
            return upgradeNames[stack.getItemDamage()];
    } 
    */
    
	@Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(int itemID, CreativeTabs tabs, List list){
            for(int i = 0; i < this.upgradeIcons.length; ++i){
                    list.add(new ItemStack(itemID, 1, i));
             }
     }    
}