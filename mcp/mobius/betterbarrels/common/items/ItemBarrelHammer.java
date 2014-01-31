package mcp.mobius.betterbarrels.common.items;

import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.network.BarrelPacketHandler;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class ItemBarrelHammer extends Item implements IOverlayItem{
   public static enum HammerMode {
      NORMAL("Your hammer returns to normal."),
      BSPACE("You feel your hammer begin to resonate."),
      REDSTONE("Your hammer begins emitting a red glow."),
      HOPPER("You begin to have trouble holding onto your hammer."),
      STORAGE("Your hammer begins to grow."),
      STRUCTURAL("Your hammer becomes a precision tool to destruct.");
      
      public final String message;
      public Icon icon;

      public static final HammerMode[] MODES = { NORMAL, BSPACE, REDSTONE, HOPPER, STORAGE, STRUCTURAL };

      private HammerMode(final String message) {
         this.message = message;
      }

      public static ItemStack setNextMode(ItemStack item) {
         int next_mode = item.getItemDamage() + 1;
         if (next_mode >= HammerMode.MODES.length) {
            next_mode = 0;
         }
         item.setItemDamage(next_mode);
         return item;
      }

      public static HammerMode getMode(final ItemStack item) {
         int mode = item.getItemDamage();
         if (mode >= HammerMode.MODES.length) {
            mode = 0;
         }
         return HammerMode.MODES[mode];
      }
   }
   
   public ItemBarrelHammer(int id){
      super(id);
        this.setMaxStackSize(1);    
        this.setUnlocalizedName("hammer");
   }
   
   @Override
    public boolean shouldPassSneakingClickToBlock(World par2World, int par4, int par5, int par6)
    {
      if (par2World.getBlockId(par4, par5, par6) == BetterBarrels.barrelID) {
        return true;
      }
      return false;
    }
   
    @Override    
    public void registerIcons(IconRegister par1IconRegister)
    {
       for (HammerMode mode: HammerMode.MODES) {
          mode.icon = par1IconRegister.registerIcon(BetterBarrels.modid + ":hammer_" + mode.name().toLowerCase());
       }
    }    
    
    @Override
    public Icon getIconFromDamage(int dmg)
    {
       if (dmg >= HammerMode.MODES.length) {
          dmg = 0;
       }
      return HammerMode.MODES[dmg].icon;
    }    

    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack)
    {
        return super.getUnlocalizedName() + "." + HammerMode.getMode(par1ItemStack).name().toLowerCase();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
       if (par3EntityPlayer.isSneaking()) {
          par3EntityPlayer.inventory.mainInventory[par3EntityPlayer.inventory.currentItem] = HammerMode.setNextMode(par1ItemStack);
   
          if (!par2World.isRemote) {
             BarrelPacketHandler.sendChat(par3EntityPlayer, HammerMode.getMode(par1ItemStack).message);
          }
       }
       
       return par1ItemStack;
   }
}
