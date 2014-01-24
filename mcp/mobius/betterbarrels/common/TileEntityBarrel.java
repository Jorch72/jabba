package mcp.mobius.betterbarrels.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import cpw.mods.fml.common.network.PacketDispatcher;
import mcp.mobius.betterbarrels.mod_BetterBarrels;
import mcp.mobius.betterbarrels.common.items.upgrades.ItemUpgradeCore;
import mcp.mobius.betterbarrels.common.items.upgrades.ItemUpgradeSide;
import mcp.mobius.betterbarrels.common.items.upgrades.ItemUpgradeStructural;
import mcp.mobius.betterbarrels.common.items.upgrades.UpgradeCore;
import mcp.mobius.betterbarrels.common.items.upgrades.UpgradeSide;
import mcp.mobius.betterbarrels.network.Packet0x01ContentUpdate;
import mcp.mobius.betterbarrels.network.Packet0x02GhostUpdate;
import mcp.mobius.betterbarrels.network.Packet0x03SideUpgradeUpdate;
import mcp.mobius.betterbarrels.network.Packet0x04StructuralUpdate;
import mcp.mobius.betterbarrels.network.Packet0x05CoreUpdate;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.ForgeDirection;


//public class TileEntityBarrel extends TileEntity implements ISidedInventory {
public class TileEntityBarrel extends TileEntity{
	private static int version = 3;
	
    private long clickTime = -20; //Click timer for double click handling
	
    public IBarrelStorage storage     = new StorageLocal();
	public ForgeDirection orientation = ForgeDirection.UNKNOWN;
	public int levelStructural        = 0;
	public int[] sideUpgrades = {UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE};
	public ArrayList<Integer> coreUpgrades = new ArrayList<Integer>();
	
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
	
	public void rightClick(EntityPlayer player, int side){
		ItemStack stack = player.getHeldItem();
		
		if (!player.isSneaking())
			this.manualStackAdd(player);		
		else if (player.isSneaking() && stack == null)
			this.switchLocked();
        else if (player.isSneaking() && stack.getItem() instanceof ItemUpgradeSide && UpgradeSide.mapRevMeta[stack.getItemDamage()] == UpgradeSide.STICKER)
        	this.applySticker(stack, ForgeDirection.getOrientation(side));
        else if (player.isSneaking() && stack.getItem() instanceof ItemUpgradeCore)
        	this.applyCoreUpgrade(stack, player);		
		else if (player.isSneaking() && (stack.getItem() instanceof ItemUpgradeStructural))
			this.applyUpgradeStructural(stack, player);		
		else
			this.manualStackAdd(player);
	}

	private void applyUpgradeStructural(ItemStack stack, EntityPlayer player){
		if (stack.getItemDamage() == this.levelStructural){
			stack.stackSize      -= 1;
			this.levelStructural += 1;
		} else if ((player instanceof EntityPlayerMP) && (stack.getItemDamage() == (this.levelStructural - 1))) {
			((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(new Packet3Chat(
					ChatMessageComponent.createFromText("Upgrade already applied."), false));				
		} else if ((player instanceof EntityPlayerMP) && (stack.getItemDamage() < this.levelStructural)) {
			((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(new Packet3Chat(
					ChatMessageComponent.createFromText("You cannot downgrade a barrel."), false));
		} else if ((player instanceof EntityPlayerMP) && (stack.getItemDamage() > this.levelStructural)) {
			((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(new Packet3Chat(
					ChatMessageComponent.createFromText("You need at least an upgrade Mark " + stack.getItemDamage() + " to apply this."), false));
		}

		PacketDispatcher.sendPacketToAllInDimension(Packet0x04StructuralUpdate.create(this), this.worldObj.provider.dimensionId);
		this.onInventoryChanged();
	}	
	
	public int getMaxUpgradeSlots(){
		int nslots = 0;
		for (int i = 0; i < this.levelStructural; i++)
			nslots += MathHelper.floor_double(Math.pow(2, i));
		return  nslots;
	}
	
	public int getUsedSlots(){
		int nslots = 0;
		for (Integer i : this.coreUpgrades)
			nslots += UpgradeCore.mapSlots[i];
		return nslots;
	}
	
	public int getFreeSlots(){
		return getMaxUpgradeSlots() - getUsedSlots();
	}	
	
	private void switchLocked(){
		this.storage.switchGhosting();
		this.onInventoryChanged();
		PacketDispatcher.sendPacketToAllInDimension(Packet0x02GhostUpdate.create(this), this.worldObj.provider.dimensionId);		
	}
	
	private void applySticker(ItemStack stack, ForgeDirection side){
		if ((side == ForgeDirection.UP) || (side == ForgeDirection.DOWN)) {return;}
		if (this.sideUpgrades[side.ordinal()] != UpgradeSide.NONE) {return;}

		this.sideUpgrades[side.ordinal()] = UpgradeSide.STICKER;
		stack.stackSize -= 1;
		
		PacketDispatcher.sendPacketToAllInDimension(Packet0x03SideUpgradeUpdate.create(this), this.worldObj.provider.dimensionId);
	}
	
	private void applyCoreUpgrade(ItemStack stack, EntityPlayer player){
		int slotsused = UpgradeCore.mapMetaSlots[stack.getItemDamage()]; 

		if (slotsused > this.getFreeSlots()){
			((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(new Packet3Chat(
					ChatMessageComponent.createFromText("Not enough upgrade slots for this upgrade. You need at least " + String.valueOf(slotsused) + " to apply this."), false));
			return;
		}
		
		System.out.printf("%s\n", this.getFreeSlots());
		
		if (UpgradeCore.mapRevMeta[stack.getItemDamage()] == UpgradeCore.STORAGE){
			this.coreUpgrades.add(UpgradeCore.STORAGE);
		}

		stack.stackSize -= 1;
		this.onInventoryChanged();
		PacketDispatcher.sendPacketToAllInDimension(Packet0x05CoreUpdate.create(this), this.worldObj.provider.dimensionId);	
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
		
        int hitOrientation = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        double stackCoordX = 0.0D, stackCoordY = 0.0D, stackCoordZ = 0.0D;
        
        switch (hitOrientation){
        	case 0:
        		stackCoordX = this.xCoord + 0.5D;
        		stackCoordY = this.yCoord + 0.5D;
        		stackCoordZ = this.zCoord - 0.25D;
        		break;
        	case 1:
        		stackCoordX = this.xCoord + 1.25D;
        		stackCoordY = this.yCoord + 0.5D;
        		stackCoordZ = this.zCoord + 0.5D;	        		
        		break;
        	case 2:
        		stackCoordX = this.xCoord + 0.5D;
        		stackCoordY = this.yCoord + 0.5D;
        		stackCoordZ = this.zCoord + 1.25D;	        		
        		break;
        	case 3:
        		stackCoordX = this.xCoord - 0.25D;
        		stackCoordY = this.yCoord + 0.5D;
        		stackCoordZ = this.zCoord + 0.5D;        		
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
        NBTTag.setInteger("version",       TileEntityBarrel.version);        
        NBTTag.setInteger("orientation",   this.orientation.ordinal());
        NBTTag.setIntArray("sideUpgrades", this.sideUpgrades);
        NBTTag.setIntArray("coreUpgrades", this.convertInts(this.coreUpgrades));
        NBTTag.setInteger("structural",    this.levelStructural);
        NBTTag.setCompoundTag("storage",   this.storage.writeTagCompound());
    }  	

	@Override	
    public void readFromNBT(NBTTagCompound NBTTag)
    {
    	super.readFromNBT(NBTTag);
    	this.orientation     = ForgeDirection.getOrientation(NBTTag.getInteger("orientation"));
    	this.sideUpgrades    = NBTTag.getIntArray("sideUpgrades");
    	this.coreUpgrades    = this.convertArrayList(NBTTag.getIntArray("coreUpgrades"));
    	this.levelStructural = NBTTag.getInteger("structural");
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
	
    private int[] convertInts(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }
    
    private ArrayList<Integer> convertArrayList(int[] list)
    {
    	ArrayList<Integer> ret = new ArrayList<Integer>();
    	for (int i = 0; i < list.length; i++)
    		ret.add(list[i]);
    	return ret;

    }    
}
