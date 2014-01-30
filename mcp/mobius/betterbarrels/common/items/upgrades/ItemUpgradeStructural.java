package mcp.mobius.betterbarrels.common.items.upgrades;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.betterbarrels.BetterBarrels;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class ItemUpgradeStructural extends ItemUpgrade{

	public static Icon[] upgradeIcons = new Icon[7];
	
    public static String[] upgradeNames = { "Structural MKI (Wood)",		
			 						 		"Structural MKII (Iron)",
			 						 		"Structural MKIII (Gold)",
			 						 		"Structural MKIV (Diamond)",
			 						 		"Structural MKV (Obsidian)",
			 						 		"Structural MKVI (End stone)",
			 						 		"Structural MKVII (Emerald)" };
	
    public static int[] textColor = {
    		0x00FFFFFF,
    		0x00FFFFFF,
	 		0x00000000,
	 		0x00000000,
	 		0x00000000,
	 		0x00FFFFFF,
	 		0x00000000,
	 		0x00000000,};    
	
    public ItemUpgradeStructural(int par1)
    {
        super(par1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(16);
        this.setUnlocalizedName("Generic structural upgrade");        
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
		if (stack.getItemDamage() >= ItemUpgradeStructural.upgradeNames.length)
			return "Capacity Upgrade [ERROR]";
		
		return ItemUpgradeStructural.upgradeNames[stack.getItemDamage()];
    }	    
    
    @Override    
    public void registerIcons(IconRegister par1IconRegister)
    {
    	for(int i=0 ; i < ItemUpgradeStructural.upgradeNames.length; i++)
    		ItemUpgradeStructural.upgradeIcons[i]  = par1IconRegister.registerIcon(BetterBarrels.modid + ":" + "capaupg_mk" + String.valueOf(i+1));
    }	
	
    @Override
    public Icon getIconFromDamage(int i){
        return ItemUpgradeStructural.upgradeIcons[i];
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
            for(int i = 0; i < ItemUpgradeStructural.upgradeIcons.length; ++i){
                    list.add(new ItemStack(itemID, 1, i));
             }
     }    
}
