package mcp.mobius.betterbarrels.common.blocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;
import cpw.mods.fml.common.network.PacketDispatcher;
import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.common.blocks.logic.LogicHopper;
import mcp.mobius.betterbarrels.common.items.ItemBarrelHammer;
import mcp.mobius.betterbarrels.common.items.upgrades.ItemUpgradeCore;
import mcp.mobius.betterbarrels.common.items.upgrades.ItemUpgradeSide;
import mcp.mobius.betterbarrels.common.items.upgrades.ItemUpgradeStructural;
import mcp.mobius.betterbarrels.common.items.upgrades.UpgradeCore;
import mcp.mobius.betterbarrels.common.items.upgrades.UpgradeSide;
import mcp.mobius.betterbarrels.network.BarrelPacketHandler;
import mcp.mobius.betterbarrels.network.Packet0x01ContentUpdate;
import mcp.mobius.betterbarrels.network.Packet0x02GhostUpdate;
import mcp.mobius.betterbarrels.network.Packet0x03SideUpgradeUpdate;
import mcp.mobius.betterbarrels.network.Packet0x04StructuralUpdate;
import mcp.mobius.betterbarrels.network.Packet0x05CoreUpdate;
import mcp.mobius.betterbarrels.network.Packet0x06FullStorage;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.ForgeDirection;


public class TileEntityBarrel extends TileEntity implements ISidedInventory, IDeepStorageUnit {
	private static int version = 3;
	
    private long clickTime = -20; //Click timer for double click handling
	
    public IBarrelStorage storage     = new StorageLocal();
	public ForgeDirection orientation = ForgeDirection.UNKNOWN;
	public int levelStructural        = 0;
	public int[] sideUpgrades         = {UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE};
	public ArrayList<Integer> coreUpgrades = new ArrayList<Integer>();
	public boolean hasRedstone        = false;
	public boolean hasHopper          = false;
	public boolean hasEnder           = false;
	public boolean isTicking          = false;
	public byte    nStorageUpg        = 0;
	public byte    nTicks             = 0;
	
	public LogicHopper logicHopper    = LogicHopper.instance();
	
	/* UPDATE HANDLING */
	@Override
	public boolean canUpdate(){
		return this.isTicking;
	}
	
	@Override
	public void updateEntity() {
		this.nTicks += 1;
		if (this.nTicks % 8 == 0){
			if (this.logicHopper.run(this)){
				this.onInventoryChanged();
				PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);
			}
			this.nTicks = 0;
		}
	}
	
	private void startTicking(){
		this.isTicking = true;
		if (!this.worldObj.loadedTileEntityList.contains(this))
			this.worldObj.addTileEntity(this);
	}
	
	private void stopTicking(){
		this.isTicking = false;
		if (this.worldObj.loadedTileEntityList.contains(this))
			this.worldObj.loadedTileEntityList.remove(this);
	}
	
	/*
	private void setTicking(){
		if (this.worldObj == null) return;
		if (this.isTicking)
			this.startTicking();
		else
			this.stopTicking();
	}
	*/
	
	/* SLOT HANDLING */
	
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
	
	public boolean hasUpgrade(int upgrade){
		for (Integer i : this.coreUpgrades)
			if (i == upgrade) return true;
		return false;
	}
	
	public int getLastNoneStorageUpgradeIndex(){
		for (int i = coreUpgrades.size() - 1; i >= 0; i--)
			if ((this.coreUpgrades.get(i) != UpgradeCore.STORAGE) && (this.coreUpgrades.get(i) != UpgradeCore.NONE))
				return i;
		return -1;
	}
	
	/* REDSTONE HANDLING */
	public int getRedstonePower(int side){
		int[] sideSwitch = {1,0,3,2,5,4};
		side = sideSwitch[side];

		if (!this.hasRedstone) 
			return 0;
		else
			if (this.sideUpgrades[side] == UpgradeSide.REDSTONE && this.storage.getAmount() == this.storage.getMaxStoredCount())
				return 15;
			else
				return 0;
	}
	
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
        else if (player.isSneaking() && stack.getItem() instanceof ItemUpgradeSide)
        	this.applySideUpgrade(stack, player, ForgeDirection.getOrientation(side));
        else if (player.isSneaking() && stack.getItem() instanceof ItemUpgradeCore)
        	this.applyCoreUpgrade(stack, player);		
		else if (player.isSneaking() && (stack.getItem() instanceof ItemUpgradeStructural))
			this.applyUpgradeStructural(stack, player);
		else if (player.isSneaking() && (stack.getItem() instanceof ItemBarrelHammer))
			this.removeUpgrade(stack, player, ForgeDirection.getOrientation(side));
		else
			this.manualStackAdd(player);
	}

	
	/* UPGRADE ACTIONS */

	private void removeUpgrade(ItemStack stack, EntityPlayer player, ForgeDirection side){
		int type = this.sideUpgrades[side.ordinal()]; 
		
		if (type != UpgradeSide.NONE && type != UpgradeSide.FRONT){
			this.dropSideUpgrade(player, side);
		} else {
			int indexLastUpdate = this.getLastNoneStorageUpgradeIndex();
			if (indexLastUpdate != -1){

				int coreType = this.coreUpgrades.get(indexLastUpdate);
				this.coreUpgrades.remove(indexLastUpdate);
				ItemStack droppedStack = new ItemStack(UpgradeCore.mapItem[coreType], 1, UpgradeCore.mapMeta[coreType]);
				this.dropItemInWorld(player, droppedStack , 0.02);
				
				this.hasRedstone = this.hasUpgrade(UpgradeCore.REDSTONE);
				this.hasHopper   = this.hasUpgrade(UpgradeCore.HOPPER);
				this.hasEnder    = this.hasUpgrade(UpgradeCore.ENDER);
				
				if (this.hasHopper)
					this.startTicking();
				else
					this.stopTicking();
				
				for (ForgeDirection s : ForgeDirection.VALID_DIRECTIONS){
					int sideType = this.sideUpgrades[s.ordinal()];
					if ((UpgradeSide.mapReq[sideType] != UpgradeCore.NONE) && (!this.hasUpgrade(UpgradeSide.mapReq[sideType])))
						this.dropSideUpgrade(player, s);
				}

			} else if (this.coreUpgrades.size() > 0) {
				int newMaxStoredItems = (this.storage.getMaxStacks() - 64) * this.storage.getItem().getMaxStackSize();
				if (this.storage.getAmount() > newMaxStoredItems)
					
					BarrelPacketHandler.sendChat(player, "Please remove some stacks first.");
				
				else{
					this.coreUpgrades.remove(0);
					ItemStack droppedStack = new ItemStack(UpgradeCore.mapItem[UpgradeCore.STORAGE], 1, UpgradeCore.mapMeta[UpgradeCore.STORAGE]);
					this.dropItemInWorld(player, droppedStack , 0.02);
					this.storage.rmStorageUpgrade();
					this.nStorageUpg -= 1;
				}
				
			} else if (this.levelStructural > 0){
				
    			ItemStack droppedStack = new ItemStack(BetterBarrels.itemUpgradeStructural, 1, this.levelStructural-1);
				this.dropItemInWorld(player, droppedStack , 0.02);
				this.levelStructural -= 1;				
				
			} else {
				BarrelPacketHandler.sendChat(player, "Bonk !");				
			}
		}
		
		this.worldObj.notifyBlockChange(this.xCoord, this.yCoord, this.zCoord, this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord));
		PacketDispatcher.sendPacketToAllInDimension(Packet0x03SideUpgradeUpdate.create(this), this.worldObj.provider.dimensionId);
		PacketDispatcher.sendPacketToAllInDimension(Packet0x04StructuralUpdate.create(this), this.worldObj.provider.dimensionId);
		PacketDispatcher.sendPacketToAllInDimension(Packet0x05CoreUpdate.create(this), this.worldObj.provider.dimensionId);
		PacketDispatcher.sendPacketToAllInDimension(Packet0x06FullStorage.create(this), this.worldObj.provider.dimensionId);		
	}
	
	private void dropSideUpgrade(EntityPlayer player, ForgeDirection side){
		int type = this.sideUpgrades[side.ordinal()]; 		
		ItemStack droppedStack = new ItemStack(UpgradeSide.mapItem[type], 1, UpgradeSide.mapMeta[type]);
		this.dropItemInWorld(player, droppedStack , 0.02);
		this.sideUpgrades[side.ordinal()] = UpgradeSide.NONE;
	}
	
	private void applySideUpgrade(ItemStack stack, EntityPlayer player, ForgeDirection side){
		int type      = UpgradeSide.mapRevMeta[stack.getItemDamage()];
		if (this.sideUpgrades[side.ordinal()] != UpgradeSide.NONE) {return;}
		
		if (type == UpgradeSide.STICKER){
			//if ((side == ForgeDirection.UP) || (side == ForgeDirection.DOWN)) {return;}
			this.sideUpgrades[side.ordinal()] = UpgradeSide.STICKER;
		}

		else if (type == UpgradeSide.REDSTONE){
			if (this.hasUpgrade(UpgradeCore.REDSTONE))
				this.sideUpgrades[side.ordinal()] = UpgradeSide.REDSTONE;
			else{
				BarrelPacketHandler.sendChat(player, "This facade requires a redstone core update.");
				return;
			}
		}		
		
		else if (type == UpgradeSide.HOPPER){
			if (this.hasUpgrade(UpgradeCore.HOPPER))
				this.sideUpgrades[side.ordinal()] = UpgradeSide.HOPPER;
			else{
				BarrelPacketHandler.sendChat(player, "This facade requires a hopper core update.");
				return;
			}
		}			
		
		stack.stackSize -= 1;
		this.onInventoryChanged();
		PacketDispatcher.sendPacketToAllInDimension(Packet0x03SideUpgradeUpdate.create(this), this.worldObj.provider.dimensionId);
	}
	
	private void applyCoreUpgrade(ItemStack stack, EntityPlayer player){
		int slotsused = UpgradeCore.mapMetaSlots[stack.getItemDamage()]; 
		int type      = UpgradeCore.mapRevMeta[stack.getItemDamage()];

		if (!(type == UpgradeCore.STORAGE) && this.hasUpgrade(type)){
			BarrelPacketHandler.sendChat(player, "Core upgrade already installed.");
			return;			
		}		
		
		if (slotsused > this.getFreeSlots()){
			BarrelPacketHandler.sendChat(player, "Not enough upgrade slots for this upgrade. You need at least " + String.valueOf(slotsused) + " to apply this.");			
			return;
		}
	
		if (type == UpgradeCore.STORAGE){
			this.coreUpgrades.add(UpgradeCore.STORAGE);
			this.storage.addStorageUpgrade();
			this.nStorageUpg += 1;
			PacketDispatcher.sendPacketToAllInDimension(Packet0x06FullStorage.create(this), this.worldObj.provider.dimensionId);				
		}

		if (type == UpgradeCore.REDSTONE){
			this.coreUpgrades.add(UpgradeCore.REDSTONE);
			this.hasRedstone = true;
		}		

		if (type == UpgradeCore.HOPPER){
			this.coreUpgrades.add(UpgradeCore.HOPPER);
			this.hasHopper = true;
			this.startTicking();
		}
		
		if (type == UpgradeCore.ENDER){
			this.coreUpgrades.add(UpgradeCore.ENDER);
			this.hasEnder = true;
		}		
		
		stack.stackSize -= 1;
		this.onInventoryChanged();
		PacketDispatcher.sendPacketToAllInDimension(Packet0x05CoreUpdate.create(this), this.worldObj.provider.dimensionId);	
	}	
	
	private void applyUpgradeStructural(ItemStack stack, EntityPlayer player){
		if (stack.getItemDamage() == this.levelStructural){
			stack.stackSize      -= 1;
			this.levelStructural += 1;
		} else if ((player instanceof EntityPlayerMP) && (stack.getItemDamage() == (this.levelStructural - 1))) {
			BarrelPacketHandler.sendChat(player, "Upgrade already applied.");			
		} else if ((player instanceof EntityPlayerMP) && (stack.getItemDamage() < this.levelStructural)) {
			BarrelPacketHandler.sendChat(player, "You cannot downgrade a barrel.");			
		} else if ((player instanceof EntityPlayerMP) && (stack.getItemDamage() > this.levelStructural)) {
			BarrelPacketHandler.sendChat(player, "You need at least an upgrade Mark " + stack.getItemDamage() + " to apply this.");
		}

		this.onInventoryChanged();		
		PacketDispatcher.sendPacketToAllInDimension(Packet0x04StructuralUpdate.create(this), this.worldObj.provider.dimensionId);

	}	
	
	/* OTHER ACTIONS */
	
	private void switchLocked(){
		this.storage.switchGhosting();
		this.onInventoryChanged();
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);	
		PacketDispatcher.sendPacketToAllInDimension(Packet0x02GhostUpdate.create(this), this.worldObj.provider.dimensionId);		
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

		BetterBarrels.proxy.updatePlayerInventory(player);
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
        NBTTag.setBoolean("redstone",      this.hasRedstone);
        NBTTag.setBoolean("hopper",        this.hasHopper);
        NBTTag.setBoolean("ender",         this.hasEnder);
        NBTTag.setBoolean("ticking",       this.isTicking);
        NBTTag.setByte("nticks",           this.nTicks);
        NBTTag.setCompoundTag("storage",   this.storage.writeTagCompound());
        NBTTag.setByte("nStorageUpg",   this.nStorageUpg);
    }  	

	@Override	
    public void readFromNBT(NBTTagCompound NBTTag)
    {
    	super.readFromNBT(NBTTag);
    	
    	// Handling of backward compatibility
    	if(NBTTag.getInteger("version") == 2){
    		this.readFromNBT_v2(NBTTag);
    		return;
    	}
    	    	
    	this.orientation     = ForgeDirection.getOrientation(NBTTag.getInteger("orientation"));
    	this.sideUpgrades    = NBTTag.getIntArray("sideUpgrades");
    	this.coreUpgrades    = this.convertArrayList(NBTTag.getIntArray("coreUpgrades"));
    	this.levelStructural = NBTTag.getInteger("structural");
    	this.hasRedstone     = NBTTag.getBoolean("redstone");
    	this.hasHopper       = NBTTag.getBoolean("hopper");
    	this.hasEnder        = NBTTag.getBoolean("ender");
    	this.nStorageUpg     = NBTTag.getByte("nStorageUpg");
    	this.isTicking       = NBTTag.getBoolean("ticking");
    	this.nTicks          = NBTTag.getByte("nticks");
    	this.storage.readTagCompound(NBTTag.getCompoundTag("storage"));
    	
    	if (this.worldObj != null && this.isTicking)
    		this.startTicking();
    }	

	/* V2 COMPATIBILITY METHODS */
	
	private void readFromNBT_v2(NBTTagCompound NBTTag){
    	int     blockOrientation = NBTTag.getInteger("barrelOrient");
    	int     upgradeCapacity  = NBTTag.getInteger("upgradeCapacity");
    	int blockOriginalOrient  = NBTTag.hasKey("barrelOrigOrient") ? NBTTag.getInteger("barrelOrigOrient") : blockOrientation;
    	StorageLocal storage = new StorageLocal();
    	storage.readTagCompound(NBTTag.getCompoundTag("storage"));    	
    	
    	// We fix the labels and orientation
    	this.orientation = this.convertOrientationFlagToForge(blockOriginalOrient).get(0);
    	
    	ArrayList<ForgeDirection> stickers = this.convertOrientationFlagToForge(blockOrientation);
    	for (ForgeDirection s : stickers)
    		this.sideUpgrades[s.ordinal()] = UpgradeSide.STICKER;
    	this.sideUpgrades[this.orientation.ordinal()] = UpgradeSide.FRONT;
    	
    	// We fix the structural and core upgrades
    	this.levelStructural = upgradeCapacity;
    	int freeSlots = this.getFreeSlots();
    	for (int i = 0; i < freeSlots; i++){
			this.coreUpgrades.add(UpgradeCore.STORAGE);
			this.storage.addStorageUpgrade();
			this.nStorageUpg += 1;
    	}

    	// Fix for the content
    	this.storage.setGhosting(storage.isGhosting());
    	this.storage.setStoredItemType(storage.getItem(), storage.getAmount());

    	
    	//this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 1, 1 & 2);
	}
	
	private ArrayList<ForgeDirection> convertOrientationFlagToForge(int flags){
		ArrayList<ForgeDirection> directions = new ArrayList<ForgeDirection>();
		for (int i = 0; i<4; i++)
			if (((1 << i) & flags) != 0)
				directions.add(ForgeDirection.getOrientation(i+2));
		return directions;
	}	

	/* END OF V2 COMPATIBILITY METHODS */		
	
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
	
    /* OTHER */
    
	@Override
	public void onInventoryChanged(){
		super.onInventoryChanged();

		if (this.hasUpgrade(UpgradeCore.REDSTONE))
			this.worldObj.notifyBlockChange(this.xCoord, this.yCoord, this.zCoord, this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord));		
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
    
    /*/////////////////////////////////////*/
    /* IInventory Interface Implementation */
    /*/////////////////////////////////////*/
    
	@Override
	public int getSizeInventory() {return this.storage.getSizeInventory();}
	@Override
	public ItemStack getStackInSlot(int islot) {
		ItemStack stack = this.storage.getStackInSlot(islot);
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);	
		return stack; 
	}
	@Override
	public ItemStack decrStackSize(int islot, int quantity) {
		TileEntity ent = this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord - 1, this.zCoord);
		ItemStack stack;
		if (ent instanceof TileEntityHopper)
			stack = this.storage.decrStackSize_Hopper(islot, quantity);
		else
			stack = this.storage.decrStackSize(islot, quantity);
		
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);
		return stack;
	}
	@Override
	public void setInventorySlotContents(int islot, ItemStack stack) { 
		this.storage.setInventorySlotContents(islot, stack);
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);
	}
	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {return null;}	
	@Override
	public String getInvName() {return "mcp.mobius.betterbarrel";}
	@Override
	public int getInventoryStackLimit() {return 64;}
	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : var1.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;}
	@Override
	public void openChest() {}
	@Override
	public void closeChest() {}

	
	
	@Override
	public boolean isInvNameLocalized() {return false;}
	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {return this.storage.isItemValidForSlot(i, itemstack);}

	@Override
	public ItemStack getStoredItemType() {
		return this.storage.getStoredItemType();
	}

	@Override
	public void setStoredItemCount(int amount) {
		this.storage.setStoredItemCount(amount);
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);
	}

	@Override
	public void setStoredItemType(ItemStack type, int amount) {
		this.storage.setStoredItemType(type, amount);
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);
	}

	@Override
	public int getMaxStoredCount() {
		return this.storage.getMaxStoredCount();
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		// TODO : Prevent sides with an hopper upgrade to react as a valid slot.
		return this.storage.getAccessibleSlotsFromSide(var1);
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemstack, int side) {
		// TODO : Prevent sides with an hopper upgrade to react as a valid slot.
		return this.storage.canInsertItem(slot, itemstack, side);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemstack, int side) {
		// TODO : Prevent sides with an hopper upgrade to react as a valid slot.		
		return this.storage.canExtractItem(slot, itemstack, side);
	}    
}