package mcp.mobius.betterbarrels.common.items.upgrades;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class ItemUpgradeStructural extends ItemUpgrade {
   public ItemUpgradeStructural(int par1) {
      super(par1);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setMaxStackSize(16);
      this.setUnlocalizedName("item.upgrade.structural.generic");
   }

   @Override
   public String getUnlocalizedName(ItemStack stack) {
      return "item.upgrade.structural." + String.valueOf(stack.getItemDamage() + 1);
   }

   @Override
   public void registerIcons(IconRegister par1IconRegister) {
      StructuralLevel.registerItemIconPieces(par1IconRegister);
      if (StructuralLevel.LEVELS == null) StructuralLevel.createLevelArray();
      for (int i = 1; i < StructuralLevel.LEVELS.length; i++)
         StructuralLevel.LEVELS[i].registerItemIcon(par1IconRegister, i);
   }

   @Override
   public Icon getIconFromDamage(int i) {
      return StructuralLevel.LEVELS[Math.min(i+1, StructuralLevel.LEVELS.length-1)].getIconItem();
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Override
   @SideOnly(Side.CLIENT)
   public void getSubItems(int itemID, CreativeTabs tabs, List list) {
      for (int i = 1; i < StructuralLevel.LEVELS.length; ++i) {
         list.add(new ItemStack(itemID, 1, i - 1));
      }
   }
}
