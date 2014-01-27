package mcp.mobius.betterbarrels.common.items;

import java.util.Random;

import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.common.BaseProxy;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemTuningFork extends Item {
	
	public ItemTuningFork(int id){
		super(id);
        this.setMaxDamage(16);
        this.setMaxStackSize(1);
        this.setUnlocalizedName("B-Space Tuning Fork (WIP)");        
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
	
	/*
    @Override
    public String getItemName(){
    	return "B-Space Tuning Fork";
    }	
    
    @Override
    public String getItemNameIS(ItemStack stack){
    	//if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Frequency"))
    	//	return "B-Space Tuning Fork Tuned to ";// + stack.getTagCompound().getInteger("Frequency");
    	
    	return "B-Space Tuning Fork";
    }     

    @Override
    public int getIconFromDamage(int i){
        return 19;
    }
    */
    
    /*
	@Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
		TileEntity targetTE = world.getBlockTileEntity(x, y, z);
		if (!(targetTE instanceof TileEntityBarrel)) return false;
		if (!((TileEntityBarrel)targetTE).storageRemote) return false;
		
		TileEntityBarrel barrelTE = (TileEntityBarrel)targetTE; 
		
		NBTTagCompound nbtStack   = stack.getTagCompound();
		
		if(nbtStack == null){
			nbtStack = new NBTTagCompound();
			nbtStack.setInteger("Frequency", 0);
		}
		
		int forkFrequency    = nbtStack.getInteger("Frequency");
		int barrelFrequency  = barrelTE.storageRemoteID; 
		
		System.out.printf("Barrel freq : %s\n", barrelFrequency);
		
		// We tune the fork
		if ((forkFrequency <= 0) && (barrelFrequency > 0)){
			forkFrequency = barrelFrequency;
			System.out.printf("Fork is now tuned to %s\n", forkFrequency);
		}
		
		// We tune the barrel
		else if ((forkFrequency > 0) && (barrelFrequency <= 0)){
			barrelTE.storageRemoteID = forkFrequency;
			barrelTE.storage = BSpaceStorageHandler.instance.getStorage(forkFrequency);
			System.out.printf("Barrel is now tuned to %s\n", forkFrequency);			
		}

		// We tune both
		else if ((forkFrequency <= 0) && (barrelFrequency <= 0)){
			barrelTE.storageRemoteID = BSpaceStorageHandler.instance.getNextID();
			forkFrequency    = BSpaceStorageHandler.instance.getNextID();
			barrelTE.storage = BSpaceStorageHandler.instance.getStorage(barrelTE.storageRemoteID);
			System.out.printf("Barrel and fork are now tuned to %s\n", forkFrequency);				
		}

		else if ((forkFrequency > 0) && (barrelFrequency > 0)){
			System.out.printf("Fork and barrel are both already tuned.\n");			
		}
		
		nbtStack.setInteger("Frequency", forkFrequency);
		stack.setTagCompound(nbtStack);
		
		return true;
    }
    */
}
