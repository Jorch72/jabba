package mcp.mobius.betterbarrels.common.blocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.Utils;
import mcp.mobius.betterbarrels.bspace.BSpaceStorageHandler;
import mcp.mobius.betterbarrels.common.LocalizedChat;
import mcp.mobius.betterbarrels.common.blocks.logic.LogicHopper;
import mcp.mobius.betterbarrels.common.items.ItemBarrelHammer;
import mcp.mobius.betterbarrels.common.items.ItemTuningFork;
import mcp.mobius.betterbarrels.common.items.ItemBarrelHammer.HammerMode;
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
import mcp.mobius.betterbarrels.network.Packet0x08LinkUpdate;
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
	private static int version = 4;
	
    private long clickTime = -20; //Click timer for double click handling
	
    IBarrelStorage storage     = new StorageLocal();
	public  ForgeDirection orientation = ForgeDirection.UNKNOWN;
	public  int[] sideUpgrades         = {UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE};
	public  int[] sideMetadata         = {0, 0, 0, 0, 0, 0};
	public  boolean isTicking          = false;
	public  boolean isLinked           = false;
	public  byte    nTicks             = 0;
	public  int     id                 = -1;
	public  long    timeSinceLastUpd   = System.currentTimeMillis();
	
	
	public BarrelCoreUpgrades coreUpgrades;

	public LogicHopper logicHopper    = LogicHopper.instance();
	
	public TileEntityBarrel() {
	   coreUpgrades = new BarrelCoreUpgrades(this);
	}
	
	public void setLinked(boolean linked){
		this.isLinked = linked;
		PacketDispatcher.sendPacketToAllInDimension(Packet0x08LinkUpdate.create(this), this.worldObj.provider.dimensionId);
	}
	
	public boolean getLinked(){
		return this.isLinked;
	}
	
	public IBarrelStorage getStorage(){
		// If I'm enderish, I should request the storage from the Manager. Otherwise, do the usual stuff
		if (this.coreUpgrades.hasEnder && !this.worldObj.isRemote)
			return BSpaceStorageHandler.instance().getStorage(this.id);
		else
			return this.storage;
	}
	
	public void setStorage(IBarrelStorage storage){
		this.storage = storage;
	}

	public void setVoid(boolean delete) {
      this.coreUpgrades.hasVoid = delete;
      this.storage.setVoid(delete);
	}

	/* UPDATE HANDLING */
	@Override
	public boolean canUpdate(){
		if (this.worldObj != null && this.worldObj.isRemote)
			return false;
		else
			return this.isTicking;
	}
	
	@Override
	public void updateEntity() {
	   if (this.worldObj.isRemote) return;

	   this.nTicks += 1;
		if (this.nTicks % 8 == 0){
			if (this.logicHopper.run(this)){
				this.onInventoryChanged();
				PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);
			}
			this.nTicks = 0;
		}
	}
	
	void startTicking(){
		this.isTicking = true;
		if (!this.worldObj.loadedTileEntityList.contains(this))
			this.worldObj.addTileEntity(this);
	}
	
	void stopTicking(){
		this.isTicking = false;
		if (this.worldObj.loadedTileEntityList.contains(this))
			this.worldObj.loadedTileEntityList.remove(this);
	}
	
	/* REDSTONE HANDLING */
	public int getRedstonePower(int side){
		int[] sideSwitch = {1,0,3,2,5,4};
		side = sideSwitch[side];

		if (!this.coreUpgrades.hasRedstone) 
			return 0;
		else
			if (this.sideUpgrades[side] == UpgradeSide.REDSTONE && this.sideMetadata[side] == UpgradeSide.RS_FULL && this.getStorage().getAmount() == this.getStorage().getMaxStoredCount())
				return 15;
			else if (this.sideUpgrades[side] == UpgradeSide.REDSTONE && this.sideMetadata[side] == UpgradeSide.RS_EMPT && this.getStorage().getAmount() == 0)
				return 15;
			else if  (this.sideUpgrades[side] == UpgradeSide.REDSTONE && this.sideMetadata[side] == UpgradeSide.RS_PROP)
				
				if (this.getStorage().getAmount() == 0)
					return 0;
				else if (this.getStorage().getAmount() == this.getStorage().getMaxStoredCount())
					return 15;
				else
					return MathHelper.floor_float(((float)this.getStorage().getAmount() / (float)this.getStorage().getMaxStoredCount()) * 14) + 1;
			else
				return 0;
	}
	
	/* PLAYER INTERACTIONS */
	
	public void leftClick(EntityPlayer player){
		if (this.worldObj.isRemote) return;		
		
		ItemStack droppedStack = null;
		if (player.isSneaking())
			droppedStack = this.getStorage().getStack(1); 
		else
			droppedStack = this.getStorage().getStack();

		if ((droppedStack != null) && (droppedStack.stackSize > 0))
			Utils.dropItemInWorld(this, player, droppedStack, 0.02);
		
		this.onInventoryChanged();
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);		
	}
	
	public void rightClick(EntityPlayer player, int side){
		if (this.worldObj.isRemote) return;
		
		ItemStack stack = player.getHeldItem();

		if (!player.isSneaking() && stack != null && (stack.getItem() instanceof ItemBarrelHammer))
			this.configSide(stack, player, ForgeDirection.getOrientation(side));		
		else if (!player.isSneaking())
			this.manualStackAdd(player);		
		else if (player.isSneaking() && stack == null)
			this.switchLocked();
        else if (player.isSneaking() && stack.getItem() instanceof ItemUpgradeSide)
        	this.applySideUpgrade(stack, player, ForgeDirection.getOrientation(side));
        else if (player.isSneaking() && stack.getItem() instanceof ItemUpgradeCore)
           coreUpgrades.applyUpgrade(stack, player);		
		else if (player.isSneaking() && (stack.getItem() instanceof ItemUpgradeStructural))
		   coreUpgrades.applyStructural(stack, player);
		else if (player.isSneaking() && (stack.getItem() instanceof ItemBarrelHammer))
			this.removeUpgrade(stack, player, ForgeDirection.getOrientation(side));
		else if (player.isSneaking() && (stack.getItem() instanceof ItemTuningFork) && (stack.getItemDamage() == 0))
			this.tuneFork(stack, player, ForgeDirection.getOrientation(side));	
		else if (player.isSneaking() && (stack.getItem() instanceof ItemTuningFork) && (stack.getItemDamage() != 0))
			this.tuneBarrel(stack, player, ForgeDirection.getOrientation(side));			
		else
			this.manualStackAdd(player);
	}

	/* THE TUNING FORK */
	private void tuneFork(ItemStack stack, EntityPlayer player, ForgeDirection side){
		if (!this.coreUpgrades.hasEnder){
			BarrelPacketHandler.sendLocalizedChat(player, LocalizedChat.BSPACE_NOREACT);
			return;
		}

		//if (this.getStorage().hasItem()){
		//	BarrelPacketHandler.sendChat(player, "Barrel content is preventing it from resonating.");
		//	return;
		//}
		
		// Here we sync the fork to the original barrel frequency if the fork is not already tuned.
		//stack.setItemDamage(stack.getMaxDamage());
		BarrelPacketHandler.sendLocalizedChat(player, LocalizedChat.BSPACE_FORK_RESONATING);
		stack.setItemDamage(1);
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("tuneID",     this.id);
		stack.getTagCompound().setInteger("structural", coreUpgrades.levelStructural);
		stack.getTagCompound().setByte("storage",       coreUpgrades.nStorageUpg);
		stack.getTagCompound().setBoolean("void",       coreUpgrades.hasVoid);
	}
	
	private void tuneBarrel(ItemStack stack, EntityPlayer player, ForgeDirection side){
		if (!this.coreUpgrades.hasEnder){
	      BarrelPacketHandler.sendLocalizedChat(player, LocalizedChat.BSPACE_NOREACT);
			return;
		}

		if (this.getStorage().hasItem()){
	      BarrelPacketHandler.sendLocalizedChat(player, LocalizedChat.BSPACE_CONTENT);
			return;
		}
		
		int  structural = stack.getTagCompound().getInteger("structural");
		byte storage    = stack.getTagCompound().getByte("storage");
		int  barrelID   = stack.getTagCompound().getInteger("tuneID");
		boolean hasVoid = stack.getTagCompound().getBoolean("void");
		
		if (coreUpgrades.levelStructural != structural || coreUpgrades.nStorageUpg != storage || coreUpgrades.hasVoid != hasVoid){
	      BarrelPacketHandler.sendLocalizedChat(player, LocalizedChat.BSAPCE_STRUCTURE);
			return;			
		}

		if (this.id == barrelID){
			stack.setItemDamage(1);
			return;
		}		

		if (BSpaceStorageHandler.instance().getBarrel(barrelID) == null || !BSpaceStorageHandler.instance().getBarrel(barrelID).coreUpgrades.hasEnder){
	      BarrelPacketHandler.sendLocalizedChat(player, LocalizedChat.BSPACE_FORK_LOST);
			stack.setItemDamage(0);
			stack.setTagCompound(new NBTTagCompound());			
			return;			
		}
		
      BarrelPacketHandler.sendLocalizedChat(player, LocalizedChat.BSPACE_RESONATING);
		stack.setItemDamage(0);
		stack.setTagCompound(new NBTTagCompound());
		
		BSpaceStorageHandler.instance().linkStorages(barrelID, this.id);
		PacketDispatcher.sendPacketToAllInDimension(Packet0x02GhostUpdate.create(this), this.worldObj.provider.dimensionId);	
		PacketDispatcher.sendPacketToAllInDimension(Packet0x06FullStorage.create(this), this.worldObj.provider.dimensionId);		
	}	
	
	/* UPGRADE ACTIONS */

	private void configSide(ItemStack stack, EntityPlayer player, ForgeDirection side){
		int type = this.sideUpgrades[side.ordinal()];
		
		if (type == UpgradeSide.REDSTONE){
			this.sideMetadata[side.ordinal()] = this.sideMetadata[side.ordinal()] + 1;
			if (this.sideMetadata[side.ordinal()] > UpgradeSide.RS_PROP)
				this.sideMetadata[side.ordinal()] = UpgradeSide.RS_FULL;
			
			this.onInventoryChanged();
			PacketDispatcher.sendPacketToAllInDimension(Packet0x03SideUpgradeUpdate.create(this), this.worldObj.provider.dimensionId);			
		}
	}
	
	void removeUpgradeFacades(EntityPlayer player) {
      for (ForgeDirection s : ForgeDirection.VALID_DIRECTIONS){
         int sideType = this.sideUpgrades[s.ordinal()];
         if ((UpgradeSide.mapReq[sideType] != -1) && (!coreUpgrades.hasUpgrade(UpgradeCore.values()[UpgradeSide.mapReq[sideType]])))
            this.dropSideUpgrade(player, s);
      }
	}
	
	private void removeUpgrade(ItemStack stack, EntityPlayer player, ForgeDirection side){
		int type = this.sideUpgrades[side.ordinal()]; 
		
		if (type != UpgradeSide.NONE && type != UpgradeSide.FRONT){
			this.dropSideUpgrade(player, side);
		} else {
		   coreUpgrades.removeUpgrade(stack, player, side);
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
		Utils.dropItemInWorld(this, player, droppedStack , 0.02);
		this.sideUpgrades[side.ordinal()] = UpgradeSide.NONE;
		this.sideMetadata[side.ordinal()] = UpgradeSide.NONE;
	}
	
	private void applySideUpgrade(ItemStack stack, EntityPlayer player, ForgeDirection side){
		int type      = UpgradeSide.mapRevMeta[stack.getItemDamage()];
		if (this.sideUpgrades[side.ordinal()] != UpgradeSide.NONE) {return;}
		
		if (type == UpgradeSide.STICKER){
			this.sideUpgrades[side.ordinal()] = UpgradeSide.STICKER;
			this.sideMetadata[side.ordinal()] = UpgradeSide.NONE;
		}

		else if (type == UpgradeSide.REDSTONE){
			if (coreUpgrades.hasRedstone){
				this.sideUpgrades[side.ordinal()] = UpgradeSide.REDSTONE;
				this.sideMetadata[side.ordinal()] = UpgradeSide.RS_FULL;
			}
			else{
            BarrelPacketHandler.sendLocalizedChat(player, LocalizedChat.FACADE_REDSTONE);
				return;
			}
		}		
		
		else if (type == UpgradeSide.HOPPER){
			if (coreUpgrades.hasHopper){
				this.sideUpgrades[side.ordinal()] = UpgradeSide.HOPPER;
				this.sideMetadata[side.ordinal()] = UpgradeSide.NONE;
			}
			else{
            BarrelPacketHandler.sendLocalizedChat(player, LocalizedChat.FACADE_HOPPER);
				return;
			}
		}			
		
		stack.stackSize -= 1;
		this.onInventoryChanged();
		PacketDispatcher.sendPacketToAllInDimension(Packet0x03SideUpgradeUpdate.create(this), this.worldObj.provider.dimensionId);
	}
	
	/*
	private void unlinkBarrel(EntityPlayer player){
		if (BSpaceStorageHandler.instance().hasLinks(this.id)){
			BarrelPacketHandler.sendChat(player, "The resonance vanishes...");
			this.storage = BSpaceStorageHandler.instance().unlinkStorage(this.id);
			PacketDispatcher.sendPacketToAllInDimension(Packet0x06FullStorage.create(this), this.worldObj.provider.dimensionId);			
		}
		
	}
	*/	
	
	/* OTHER ACTIONS */
	
	private void switchLocked(){
		this.getStorage().switchGhosting();
		this.onInventoryChanged();
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);	
		PacketDispatcher.sendPacketToAllInDimension(Packet0x02GhostUpdate.create(this), this.worldObj.provider.dimensionId);		
	}
	
	public void setLocked(boolean locked){
		this.getStorage().setGhosting(locked);
		this.onInventoryChanged();
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);	
		PacketDispatcher.sendPacketToAllInDimension(Packet0x02GhostUpdate.create(this), this.worldObj.provider.dimensionId);		
	}
	
	private void manualStackAdd(EntityPlayer player){
		ItemStack heldStack = player.inventory.getCurrentItem();
		this.getStorage().addStack(heldStack);
		
		if (this.worldObj.getTotalWorldTime() - this.clickTime < 10L){
			InventoryPlayer playerInv = player.inventory;
            for (int invSlot = 0; invSlot < playerInv.getSizeInventory(); ++invSlot)
            {
            	ItemStack slotStack = playerInv.getStackInSlot(invSlot);
            	
            	// We add the items to the barrel and update player inventory
            	if (this.getStorage().addStack(slotStack) > 0){
                	if (slotStack.stackSize == 0)
                		playerInv.setInventorySlotContents(invSlot, (ItemStack)null);
            	}
            }
		}

		BetterBarrels.proxy.updatePlayerInventory(player);
		this.clickTime = this.worldObj.getTotalWorldTime();	
		
		this.onInventoryChanged();
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);
	}
	
	/* SAVING AND LOADING OF DATA */
	
	@Override
    public void writeToNBT(NBTTagCompound NBTTag)
    {
		if (this.id == -1)
			 this.id = BSpaceStorageHandler.instance().getNextBarrelID();
		
		BSpaceStorageHandler.instance().updateBarrel(this.id, this.worldObj.provider.dimensionId, this.xCoord, this.yCoord, this.zCoord);
		
        super.writeToNBT(NBTTag);
        NBTTag.setInteger("version",       TileEntityBarrel.version);        
        NBTTag.setInteger("orientation",   this.orientation.ordinal());
        NBTTag.setIntArray("sideUpgrades", this.sideUpgrades);
        this.coreUpgrades.writeToNBT(NBTTag);
        NBTTag.setIntArray("sideMeta",     this.sideMetadata);
        NBTTag.setBoolean("ticking",       this.isTicking);
        NBTTag.setBoolean("linked",        this.isLinked);
        NBTTag.setByte("nticks",           this.nTicks);
        NBTTag.setCompoundTag("storage",   this.getStorage().writeTagCompound());
        NBTTag.setInteger("bspaceid", 	   this.id);
        
    }  	

	@Override	
    public void readFromNBT(NBTTagCompound NBTTag)
    {
    	super.readFromNBT(NBTTag);
    	
      // Handling of backward compatibility
    	int saveVersion = NBTTag.getInteger("version");
      if(saveVersion == 2) {
         this.readFromNBT_v2(NBTTag);
         return;
      }
    	    	
    	this.orientation     = ForgeDirection.getOrientation(NBTTag.getInteger("orientation"));
    	this.sideUpgrades    = NBTTag.getIntArray("sideUpgrades");
    	this.sideMetadata    = NBTTag.getIntArray("sideMeta");
    	this.coreUpgrades    = new BarrelCoreUpgrades(this);
    	this.coreUpgrades.readFromNBT(NBTTag, saveVersion);
    	this.isTicking       = NBTTag.getBoolean("ticking");
    	this.isLinked        = NBTTag.hasKey("linked") ? NBTTag.getBoolean("linked") : false;
    	this.nTicks          = NBTTag.getByte("nticks");
    	this.id              = NBTTag.getInteger("bspaceid");
    	
    	
    	if (this.coreUpgrades.hasEnder && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
    		this.storage = BSpaceStorageHandler.instance().getStorage(this.id);
    	else
    		this.getStorage().readTagCompound(NBTTag.getCompoundTag("storage"));
    	
    	if (this.worldObj != null){
        	this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
        	if (this.isTicking)
        		this.startTicking();
    	}

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
    	coreUpgrades.levelStructural = upgradeCapacity;
    	int freeSlots = coreUpgrades.getFreeSlots();
    	for (int i = 0; i < freeSlots; i++){
			this.coreUpgrades.upgradeList.add(UpgradeCore.STORAGE);
			this.getStorage().addStorageUpgrade();
			coreUpgrades.nStorageUpg += 1;
    	}

    	// Fix for the content
    	this.getStorage().setGhosting(storage.isGhosting());
    	this.getStorage().setStoredItemType(storage.getItem(), storage.getAmount());

    	// We get a new id
    	this.id = BSpaceStorageHandler.instance().getNextBarrelID();
    	
    	// We update the rendering if possible
    	if (this.worldObj != null){
        	this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
    	}    	
    	
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
    public void onInventoryChanged() {
       super.onInventoryChanged();

       if (coreUpgrades.hasRedstone || coreUpgrades.hasHopper) this.worldObj.notifyBlockChange(this.xCoord, this.yCoord, this.zCoord, this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord));

       if (coreUpgrades.hasEnder && !this.worldObj.isRemote) BSpaceStorageHandler.instance().updateAllBarrels(this.id);
    }

    /*/////////////////////////////////////*/
    /* IInventory Interface Implementation */
    /*/////////////////////////////////////*/
    
    private void sendContentSyncPacket(){
		long currTime = System.currentTimeMillis();
		if (currTime - this.timeSinceLastUpd > 1000){
			PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);
			//PacketDispatcher.sendPacketToAllAround(this.xCoord, this.yCoord, this.zCoord, 200, this.worldObj.provider.dimensionId, Packet0x01ContentUpdate.create(this));
			this.timeSinceLastUpd = currTime;
		}    	
    }
    
	@Override
	public int getSizeInventory() {return this.getStorage().getSizeInventory();}
	@Override
	public ItemStack getStackInSlot(int islot) {
		ItemStack stack = this.getStorage().getStackInSlot(islot);
		this.onInventoryChanged();
		this.sendContentSyncPacket();
		return stack;
	}
	
	@Override
	public ItemStack decrStackSize(int islot, int quantity) {
		TileEntity ent = this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord - 1, this.zCoord);
		ItemStack stack;
		if (ent instanceof TileEntityHopper)
			stack = this.getStorage().decrStackSize_Hopper(islot, quantity);
		else
			stack = this.getStorage().decrStackSize(islot, quantity);
		
		this.onInventoryChanged();
		this.sendContentSyncPacket();
		return stack;
	}
	
	@Override
	public void setInventorySlotContents(int islot, ItemStack stack) { 
		this.getStorage().setInventorySlotContents(islot, stack);
		this.onInventoryChanged();
		this.sendContentSyncPacket();
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
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {return this.getStorage().isItemValidForSlot(i, itemstack);}

	@Override
	public ItemStack getStoredItemType() {
		return this.getStorage().getStoredItemType();
	}

	@Override
	public void setStoredItemCount(int amount) {
		this.getStorage().setStoredItemCount(amount);
		this.onInventoryChanged();
		this.sendContentSyncPacket();
	}

	@Override
	public void setStoredItemType(ItemStack type, int amount) {
		this.getStorage().setStoredItemType(type, amount);
		this.onInventoryChanged();
		this.sendContentSyncPacket();
	}

	@Override
	public int getMaxStoredCount() {
		return this.getStorage().getMaxStoredCount();
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		// TODO : Prevent sides with an hopper upgrade to react as a valid slot.
		if (this.sideUpgrades[side] == UpgradeSide.HOPPER)
			return new int[]{1};
		else
			return this.getStorage().getAccessibleSlotsFromSide(side);
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemstack, int side) {
		// TODO : Prevent sides with an hopper upgrade to react as a valid slot.
		if (this.sideUpgrades[side] == UpgradeSide.HOPPER)
			return false;
		else
			return this.getStorage().canInsertItem(slot, itemstack, side);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemstack, int side) {
		// TODO : Prevent sides with an hopper upgrade to react as a valid slot.		
		return this.getStorage().canExtractItem(slot, itemstack, side);
	}    
}
