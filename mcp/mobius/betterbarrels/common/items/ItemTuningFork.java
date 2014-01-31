package mcp.mobius.betterbarrels.common.items;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.common.BaseProxy;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemTuningFork extends Item implements IOverlayItem{
	
	public ItemTuningFork(int id){
		super(id);
        this.setMaxDamage(30); // Time it stays tuned, in sec.
        this.setMaxStackSize(1);
        this.setUnlocalizedName("fork");  
	}
	
	@Override
    public boolean shouldPassSneakingClickToBlock(World par2World, int par4, int par5, int par6)
    {
        return true;
    }

    @Override    
    public void registerIcons(IconRegister par1IconRegister)
    {
    	this.itemIcon  = par1IconRegister.registerIcon(BetterBarrels.modid + ":" + "bspace_fork");
    }		
	
    @Override
    public void onUpdate(ItemStack stack, World world, Entity player, int par4, boolean par5) {
    	if (world.getTotalWorldTime() % 20 == 0){
    	
	    	if (stack.getItemDamage() != 0)
	    		stack.setItemDamage(stack.getItemDamage() + 1);
	    	
	    	if (stack.getItemDamage() == this.getMaxDamage()){
	    		stack.setTagCompound(new NBTTagCompound());
	    		stack.setItemDamage(0);
	    	}
    	}
    }
}
