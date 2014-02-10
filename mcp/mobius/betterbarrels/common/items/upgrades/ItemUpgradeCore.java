package mcp.mobius.betterbarrels.common.items.upgrades;

import java.util.List;

import mcp.mobius.betterbarrels.BetterBarrels;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemUpgradeCore extends ItemUpgrade {
   public ItemUpgradeCore() {
      super();
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setMaxStackSize(16);
      this.setUnlocalizedName("upgrade.core.generic");
   }

   @Override
   public String getUnlocalizedName(ItemStack stack) {
      return UpgradeCore.values()[stack.getItemDamage()].translationKey;
   }

   @Override
   public void registerIcons(IIconRegister par1IconRegister) {
      for (UpgradeCore upgrade: UpgradeCore.values())
         upgrade.icon = par1IconRegister.registerIcon(BetterBarrels.modid + ":coreupg_" + upgrade.ordinal());
   }

   @Override
   public IIcon getIconFromDamage(int i) {
      return UpgradeCore.values()[i].icon;
   }

   @Override
   public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
      super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
      
      par3List.add(UpgradeCore.values()[par1ItemStack.getItemDamage()].description());
   }
   
   @Override
   @SideOnly(Side.CLIENT)
   public void getSubItems(Item item, CreativeTabs tabs, List list) {
      for (UpgradeCore upgrade: UpgradeCore.values()) {
         list.add(new ItemStack(item, 1, upgrade.ordinal()));
      }
   }
}
