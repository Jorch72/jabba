package mcp.mobius.betterbarrels.common.items;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.common.JabbaCreativeTab;
import mcp.mobius.betterbarrels.common.LocalizedChat;
import mcp.mobius.betterbarrels.common.blocks.BlockBarrel;
import mcp.mobius.betterbarrels.network.BarrelPacketHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class ItemBarrelHammer extends Item implements IOverlayItem{
   public static enum HammerMode {
      NORMAL, BSPACE, REDSTONE, HOPPER, STORAGE, STRUCTURAL, VOID, CREATIVE;
      
      public final LocalizedChat message;
      public Icon icon;

      private HammerMode() {
         this.message = LocalizedChat.valueOf("HAMMER_" + this.name().toUpperCase());
      }

      public static ItemStack setNextMode(ItemStack item, EntityPlayer player) {
         int next_mode = item.getItemDamage() + 1;
         if (!player.capabilities.isCreativeMode && next_mode == HammerMode.CREATIVE.ordinal()) {
            next_mode++;
         }
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
   
   public ItemBarrelHammer(int id){
      super(id);
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setUnlocalizedName("hammer");
        this.setCreativeTab(JabbaCreativeTab.tab);
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
       for (HammerMode mode: HammerMode.values()) {
          mode.icon = par1IconRegister.registerIcon(BetterBarrels.modid + ":hammer_" + mode.name().toLowerCase());
       }
    }    
    
    @Override
    public Icon getIconFromDamage(int dmg)
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
          par3EntityPlayer.inventory.mainInventory[par3EntityPlayer.inventory.currentItem] = HammerMode.setNextMode(par1ItemStack, par3EntityPlayer);
   
          if (!par2World.isRemote) {
             BarrelPacketHandler.sendLocalizedChat(par3EntityPlayer, HammerMode.getMode(par1ItemStack).message);
          }
       }
       
       return par1ItemStack;
   }

	@Override
	public boolean canHarvestBlock(Block blockHit) {
		if (blockHit instanceof BlockBarrel) {
			return true;
		} else {
			return super.canHarvestBlock(blockHit);
		}
	}

 	@Override
 	public float getStrVsBlock(ItemStack hammerStack, Block blockHit) {
 		if (hammerStack.getItem() instanceof ItemBarrelHammer && (blockHit instanceof BlockBarrel)) {
 			return EnumToolMaterial.IRON.getEfficiencyOnProperMaterial();
 		} else {
 			return super.getStrVsBlock(hammerStack, blockHit);
 		}
 	}

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @SideOnly(Side.CLIENT)
    public void getSubItems(int itemID, CreativeTabs tabs, List list) {
       for(HammerMode mode: HammerMode.values()) {
          list.add(new ItemStack(itemID, 1, mode.ordinal()));
       }
    }
}
