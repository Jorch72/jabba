package mcp.mobius.betterbarrels.common.items.upgrades;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.betterbarrels.BetterBarrels;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class ItemUpgradeCore extends ItemUpgrade {
   public ItemUpgradeCore(int id) {
      super(id);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setMaxStackSize(16);
      this.setUnlocalizedName("upgrade.core.generic");
   }

   @Override
   public String getUnlocalizedName(ItemStack stack) {
      return UpgradeCore.values()[Math.min(stack.getItemDamage(),UpgradeCore.values().length-1)].translationKey;
   }

   @Override
   public void registerIcons(IconRegister par1IconRegister) {
      for (UpgradeCore upgrade: UpgradeCore.values())
         upgrade.icon = par1IconRegister.registerIcon(BetterBarrels.modid + ":coreupg_" + upgrade.ordinal());
   }

   @Override
   public Icon getIconFromDamage(int i) {
      return UpgradeCore.values()[Math.min(i,UpgradeCore.values().length-1)].icon;
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Override
   public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
      super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
      
      par3List.add(UpgradeCore.values()[Math.min(par1ItemStack.getItemDamage(),UpgradeCore.values().length-1)].description());
   }
   
   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Override
   @SideOnly(Side.CLIENT)
   public void getSubItems(int itemID, CreativeTabs tabs, List list) {
      for (UpgradeCore upgrade: UpgradeCore.values()) {
         list.add(new ItemStack(itemID, 1, upgrade.ordinal()));
      }
   }
}
