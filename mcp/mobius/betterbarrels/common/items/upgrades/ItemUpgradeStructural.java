package mcp.mobius.betterbarrels.common.items.upgrades;

import java.util.List;

import mcp.mobius.betterbarrels.BetterBarrels;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemUpgradeStructural extends ItemUpgrade{

	public static IIcon[] upgradeIcons = new IIcon[7];
	
    public static int[] textColor = {
    		0x00FFFFFF,
    		0x00FFFFFF,
	 		0x00000000,
	 		0x00000000,
	 		0x00000000,
	 		0x00FFFFFF,
	 		0x00000000,
	 		0x00000000,};    
	
    public ItemUpgradeStructural()
    {
        super();
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(16);
        this.setUnlocalizedName("item.upgrade.structural.generic");        
    }

	@Override	
    public String getUnlocalizedName(ItemStack stack)
    {   
		return "item.upgrade.structural." + String.valueOf(stack.getItemDamage());
    }	    
    
    @Override    
    public void registerIcons(IIconRegister par1IconRegister)
    {
    	for(int i=0 ; i < ItemUpgradeStructural.upgradeIcons.length; i++)
    		ItemUpgradeStructural.upgradeIcons[i]  = par1IconRegister.registerIcon(BetterBarrels.modid + ":" + "capaupg_mk" + String.valueOf(i+1));
    }	
	
    @Override
    public IIcon getIconFromDamage(int i){
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
    public void getSubItems(Item item, CreativeTabs tabs, List list){
            for(int i = 0; i < ItemUpgradeStructural.upgradeIcons.length; ++i){
                    list.add(new ItemStack(item, 1, i));
             }
     }    
}
