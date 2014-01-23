package mcp.mobius.betterbarrels.common;

import java.util.Random;

import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.server.FMLServerHandler;
import mcp.mobius.betterbarrels.mod_BetterBarrels;
import mcp.mobius.betterbarrels.common.items.ItemBSpaceInterface;
import mcp.mobius.betterbarrels.common.items.ItemCapaUpg;
import mcp.mobius.betterbarrels.network.Packet0x01ContentUpdate;
import mcp.mobius.betterbarrels.server.BSpaceStorageHandler;
import mcp.mobius.betterbarrels.server.SaveHandler;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;


//public class TileEntityBarrel extends TileEntity implements ISidedInventory {
public class TileEntityBarrel extends TileEntity{
	private static int version = 3;
	
    private long clickTime = -20; //Click timer for double click handling
	
    public IBarrelStorage storage     = new StorageLocal();
	public ForgeDirection orientation = ForgeDirection.UNKNOWN;
	public int levelStructural        = 0;	
	
	/* PLAYER INTERACTIONS */
	
	public void leftClick(EntityPlayer player){
		ItemStack droppedStack = null;
		if (player.isSneaking())
			droppedStack = this.storage.getStack(1); 
		else
			droppedStack = this.storage.getStack();

		if ((droppedStack != null) && (droppedStack.stackSize > 0))
			this.dropItemInWorld(player, droppedStack, 0.02);
		
		this.onInventoryChanged();
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);		
	}
	
	public void rightClick(EntityPlayer player){
		this.manualStackAdd(player);
	}

	private void manualStackAdd(EntityPlayer player){
		ItemStack heldStack = player.inventory.getCurrentItem();
		this.storage.addStack(heldStack);
		
		if (this.worldObj.getWorldTime() - this.clickTime < 10L){
			InventoryPlayer playerInv = player.inventory;
            for (int invSlot = 0; invSlot < playerInv.getSizeInventory(); ++invSlot)
            {
            	ItemStack slotStack = playerInv.getStackInSlot(invSlot);
            	
            	// We add the items to the barrel and update player inventory
            	if (this.storage.addStack(slotStack) > 0){
                	if (slotStack.stackSize == 0)
                		playerInv.setInventorySlotContents(invSlot, (ItemStack)null);
            	}
            }
		}

		mod_BetterBarrels.proxy.updatePlayerInventory(player);
		this.clickTime = this.worldObj.getWorldTime();	
		
		this.onInventoryChanged();		
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);
	}
	
	private void dropItemInWorld(EntityPlayer player, ItemStack stack, double speedfactor){
		
        int hitOrientation = MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        double stackCoordX = 0.0D, stackCoordY = 0.0D, stackCoordZ = 0.0D;
        
        System.out.printf("%d\n", hitOrientation);
        
        switch (hitOrientation){
        	case 0:
        		stackCoordX = (double)this.xCoord + 0.5D;
        		stackCoordY = (double)this.yCoord + 0.5D;
        		stackCoordZ = (double)this.zCoord - 0.25D;
        		break;
        	case 1:
        		stackCoordX = (double)this.xCoord + 1.25D;
        		stackCoordY = (double)this.yCoord + 0.5D;
        		stackCoordZ = (double)this.zCoord + 0.5D;	        		
        		break;
        	case 2:
        		stackCoordX = (double)this.xCoord + 0.5D;
        		stackCoordY = (double)this.yCoord + 0.5D;
        		stackCoordZ = (double)this.zCoord + 1.25D;	        		
        		break;
        	case 3:
        		stackCoordX = (double)this.xCoord - 0.25D;
        		stackCoordY = (double)this.yCoord + 0.5D;
        		stackCoordZ = (double)this.zCoord + 0.5D;        		
        		break;        		
        }
       
		EntityItem droppedEntity = new EntityItem(this.worldObj, stackCoordX, stackCoordY, stackCoordZ, stack);

        if (player != null)
        {
            Vec3 motion = Vec3.createVectorHelper(player.posX - stackCoordX, player.posY - stackCoordY, player.posZ - stackCoordZ);
            motion.normalize();
            droppedEntity.motionX = motion.xCoord;
            droppedEntity.motionY = motion.yCoord;
            droppedEntity.motionZ = motion.zCoord;
            double offset = 0.25D;
            droppedEntity.moveEntity(motion.xCoord * offset, motion.yCoord * offset, motion.zCoord * offset);
        }

        droppedEntity.motionX *= speedfactor;
        droppedEntity.motionY *= speedfactor;
        droppedEntity.motionZ *= speedfactor;        
        
        this.worldObj.spawnEntityInWorld(droppedEntity);		
	}	
	
	/* SAVING AND LOADING OF DATA */
	
	@Override
    public void writeToNBT(NBTTagCompound NBTTag)
    {
        super.writeToNBT(NBTTag);
        NBTTag.setInteger("version",     this.version);        
        NBTTag.setInteger("orientation", this.orientation.ordinal());
        NBTTag.setCompoundTag("storage", this.storage.writeTagCompound());
    }  	

	@Override	
    public void readFromNBT(NBTTagCompound NBTTag)
    {
    	super.readFromNBT(NBTTag);
    	this.orientation = ForgeDirection.getOrientation(NBTTag.getInteger("orientation"));
    	this.storage.readTagCompound(NBTTag.getCompoundTag("storage"));
    }	
	
    @Override
    public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt) {
        this.readFromNBT(pkt.data);
    }    

    @Override
    public Packet132TileEntityData getDescriptionPacket()
    {
        NBTTagCompound var1 = new NBTTagCompound();
        this.writeToNBT(var1);
        return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, var1);
    }	
	
}
