package mcp.mobius.betterbarrels.common.items;

import java.util.List;

import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.common.JabbaCreativeTab;
import mcp.mobius.betterbarrels.common.LocalizedChat;
import mcp.mobius.betterbarrels.network.BarrelPacketHandler;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBarrelHammer extends Item implements IOverlayItem{
   public static enum HammerMode {
      NORMAL(LocalizedChat.HAMMER_NORMAL),
      BSPACE(LocalizedChat.HAMMER_BSPACE),
      REDSTONE(LocalizedChat.HAMMER_REDSTONE),
      HOPPER(LocalizedChat.HAMMER_HOPPER),
      STORAGE(LocalizedChat.HAMMER_STORAGE),
      STRUCTURAL(LocalizedChat.HAMMER_STRUCTURAL),
      VOID(LocalizedChat.HAMMER_VOID);
      
      public final LocalizedChat message;
      public IIcon icon;

      private HammerMode(final LocalizedChat message) {
         this.message = message;
      }

      public static ItemStack setNextMode(ItemStack item) {
         int next_mode = item.getItemDamage() + 1;
         if (next_mode >= HammerMode.values().length) {
            next_mode = 0;
         }
         item.setItemDamage(next_mode);
         return item;
      }

      public static HammerMode getMode(final ItemStack item) {
         int mode = item.getItemDamage();
         if (mode >= HammerMode.values().length) {
            mode = 0;
         }
         return HammerMode.values()[mode];
      }
   }
   
   public ItemBarrelHammer(){
      super();
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setUnlocalizedName("hammer");
        this.setCreativeTab(JabbaCreativeTab.tab);
   }
   
   @Override
    public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
    {
      if (world.getBlock(x, y, z) == BetterBarrels.blockBarrel) {
        return true;
      }
      return false;
    }
   
    @Override    
    public void registerIcons(IIconRegister par1IconRegister)
    {
       for (HammerMode mode: HammerMode.values()) {
          mode.icon = par1IconRegister.registerIcon(BetterBarrels.modid + ":hammer_" + mode.name().toLowerCase());
       }
    }    
    
    @Override
    public IIcon getIconFromDamage(int dmg)
    {
       if (dmg >= HammerMode.values().length) {
          dmg = 0;
       }
      return HammerMode.values()[dmg].icon;
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
             BarrelPacketHandler.sendLocalizedChat(par3EntityPlayer, HammerMode.getMode(par1ItemStack).message);
          }
       }
       
       return par1ItemStack;
   }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tabs, List list) {
       for(HammerMode mode: HammerMode.values()) {
          list.add(new ItemStack(item, 1, mode.ordinal()));
       }
    }
}
