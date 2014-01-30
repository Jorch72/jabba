package mcp.mobius.betterbarrels.common.blocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.bspace.BSpaceStorageHandler;
import mcp.mobius.betterbarrels.common.blocks.logic.LogicHopper;
import mcp.mobius.betterbarrels.common.items.ItemBarrelHammer;
import mcp.mobius.betterbarrels.common.items.ItemTuningFork;
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
	private static int version = 3;
	
    private long clickTime = -20; //Click timer for double click handling
	
    private IBarrelStorage storage     = new StorageLocal();
	public  ForgeDirection orientation = ForgeDirection.UNKNOWN;
	public  int levelStructural        = 0;
	public  int[] sideUpgrades         = {UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE, UpgradeSide.NONE};
	public  int[] sideMetadata         = {0, 0, 0, 0, 0, 0};
	public  ArrayList<Integer> coreUpgrades = new ArrayList<Integer>();
	public  boolean hasRedstone        = false;
	public  boolean hasHopper          = false;
	public  boolean hasEnder           = false;
	public  boolean isTicking          = false;
	public  boolean isLinked           = false;
	public  byte    nStorageUpg        = 0;
	public  byte    nTicks             = 0;
	public  int     id                 = -1;
	
	public LogicHopper logicHopper    = LogicHopper.instance();
	
	public void setLinked(boolean linked){
		this.isLinked = linked;
		PacketDispatcher.sendPacketToAllInDimension(Packet0x08LinkUpdate.create(this), this.worldObj.provider.dimensionId);
	}
	
	public boolean getLinked(){
		return this.isLinked;
	}
	
	public IBarrelStorage getStorage(){
		// If I'm enderish, I should request the storage from the Manager. Otherwise, do the usual stuff
		if (this.hasEnder && !this.worldObj.isRemote)
			return BSpaceStorageHandler.instance().getStorage(this.id);
		else
			return this.storage;
	}
	
	public void setStorage(IBarrelStorage storage){
		this.storage = storage;
	}
	
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
			this.dropItemInWorld(player, droppedStack, 0.02);
		
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
        	this.applyCoreUpgrade(stack, player);		
		else if (player.isSneaking() && (stack.getItem() instanceof ItemUpgradeStructural))
			this.applyUpgradeStructural(stack, player);
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
		if (!this.hasEnder){
			BarrelPacketHandler.sendChat(player, "This barrel is not reacting to the fork.");
			return;
		}

		//if (this.getStorage().hasItem()){
		//	BarrelPacketHandler.sendChat(player, "Barrel content is preventing it from resonating.");
		//	return;
		//}
		
		// Here we sync the fork to the original barrel frequency if the fork is not already tuned.
		//stack.setItemDamage(stack.getMaxDamage());
		BarrelPacketHandler.sendChat(player, "The fork starts resonating.");
		stack.setItemDamage(1);
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("tuneID",     this.id);
		stack.getTagCompound().setInteger("structural", this.levelStructural);
		stack.getTagCompound().setByte("storage",       this.nStorageUpg);
		
	}
	
	private void tuneBarrel(ItemStack stack, EntityPlayer player, ForgeDirection side){
		if (!this.hasEnder){
			BarrelPacketHandler.sendChat(player, "This barrel is not reacting to the fork.");
			return;
		}

		if (this.getStorage().hasItem()){
			BarrelPacketHandler.sendChat(player, "Barrel content is preventing it from resonating.");
			return;
		}
		
		int  structural = stack.getTagCompound().getInteger("structural");
		byte storage    = stack.getTagCompound().getByte("storage");
		int  barrelID   = stack.getTagCompound().getInteger("tuneID");
		
		if (this.levelStructural != structural || this.nStorageUpg != storage){
			BarrelPacketHandler.sendChat(player, "Structure too different to find a common frequency.");
			return;			
		}

		if (this.id == barrelID){
			stack.setItemDamage(1);
			return;
		}		

		if (BSpaceStorageHandler.instance().getBarrel(barrelID) == null){
			BarrelPacketHandler.sendChat(player, "The fork has lost the original source.");
			stack.setItemDamage(0);
			stack.setTagCompound(new NBTTagCompound());			
			return;			
		}
		
		BarrelPacketHandler.sendChat(player, "Barrels are resonating together.");	
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
				
				if (coreType == UpgradeCore.ENDER){
					if (BSpaceStorageHandler.instance().hasLinks(this.id)){
						BarrelPacketHandler.sendChat(player, "The resonance vanishes...");
					}
					this.storage = BSpaceStorageHandler.instance().unregisterEnderBarrel(this.id);
				}
				
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
				int newMaxStoredItems = (this.getStorage().getMaxStacks() - 64) * this.getStorage().getItem().getMaxStackSize();
				if (this.getStorage().getAmount() > newMaxStoredItems)
					
					BarrelPacketHandler.sendChat(player, "Please remove some stacks first.");
				
				else{
					this.coreUpgrades.remove(0);
					ItemStack droppedStack = new ItemStack(UpgradeCore.mapItem[UpgradeCore.STORAGE], 1, UpgradeCore.mapMeta[UpgradeCore.STORAGE]);
					this.dropItemInWorld(player, droppedStack , 0.02);
					this.getStorage().rmStorageUpgrade();
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
			if (this.hasUpgrade(UpgradeCore.REDSTONE)){
				this.sideUpgrades[side.ordinal()] = UpgradeSide.REDSTONE;
				this.sideMetadata[side.ordinal()] = UpgradeSide.RS_FULL;
			}
			else{
				BarrelPacketHandler.sendChat(player, "This facade requires a redstone core update.");
				return;
			}
		}		
		
		else if (type == UpgradeSide.HOPPER){
			if (this.hasUpgrade(UpgradeCore.HOPPER)){
				this.sideUpgrades[side.ordinal()] = UpgradeSide.HOPPER;
				this.sideMetadata[side.ordinal()] = UpgradeSide.NONE;
			}
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
			if (BSpaceStorageHandler.instance().hasLinks(this.id)){
				BarrelPacketHandler.sendChat(player, "The resonance prevents you from applying this upgrade.");
				return;
			}			
			
			this.coreUpgrades.add(UpgradeCore.STORAGE);
			this.getStorage().addStorageUpgrade();
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
			BSpaceStorageHandler.instance().registerEnderBarrel(this.id, this.storage);
		}		
		
		stack.stackSize -= 1;
		this.onInventoryChanged();
		PacketDispatcher.sendPacketToAllInDimension(Packet0x05CoreUpdate.create(this), this.worldObj.provider.dimensionId);	
	}	
	
	private void applyUpgradeStructural(ItemStack stack, EntityPlayer player){
		if (BSpaceStorageHandler.instance().hasLinks(this.id)){
			BarrelPacketHandler.sendChat(player, "The resonance prevents you from applying this upgrade.");
			return;
		}
		
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
		
		if (this.worldObj.getWorldTime() - this.clickTime < 10L){
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
		if (this.id == -1)
			 this.id = BSpaceStorageHandler.instance().getNextBarrelID();
		
		BSpaceStorageHandler.instance().updateBarrel(this.id, this.worldObj.provider.dimensionId, this.xCoord, this.yCoord, this.zCoord);
		
        super.writeToNBT(NBTTag);
        NBTTag.setInteger("version",       TileEntityBarrel.version);        
        NBTTag.setInteger("orientation",   this.orientation.ordinal());
        NBTTag.setIntArray("sideUpgrades", this.sideUpgrades);
        NBTTag.setIntArray("sideMeta",     this.sideMetadata);
        NBTTag.setIntArray("coreUpgrades", this.convertInts(this.coreUpgrades));
        NBTTag.setInteger("structural",    this.levelStructural);
        NBTTag.setBoolean("redstone",      this.hasRedstone);
        NBTTag.setBoolean("hopper",        this.hasHopper);
        NBTTag.setBoolean("ender",         this.hasEnder);
        NBTTag.setBoolean("ticking",       this.isTicking);
        NBTTag.setBoolean("linked",        this.isLinked);
        NBTTag.setByte("nticks",           this.nTicks);
        NBTTag.setCompoundTag("storage",   this.getStorage().writeTagCompound());
        NBTTag.setByte("nStorageUpg",      this.nStorageUpg);
        NBTTag.setInteger("bspaceid", 	   this.id);
        
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
    	this.sideMetadata    = NBTTag.getIntArray("sideMeta");
    	this.coreUpgrades    = this.convertArrayList(NBTTag.getIntArray("coreUpgrades"));
    	this.levelStructural = NBTTag.getInteger("structural");
    	this.hasRedstone     = NBTTag.getBoolean("redstone");
    	this.hasHopper       = NBTTag.getBoolean("hopper");
    	this.hasEnder        = NBTTag.getBoolean("ender");
    	this.nStorageUpg     = NBTTag.getByte("nStorageUpg");
    	this.isTicking       = NBTTag.getBoolean("ticking");
    	this.isLinked        = NBTTag.hasKey("linked") ? NBTTag.getBoolean("linked") : false;
    	this.nTicks          = NBTTag.getByte("nticks");
    	this.id              = NBTTag.getInteger("bspaceid");
    	
    	
    	if (this.hasEnder && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
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
    	this.levelStructural = upgradeCapacity;
    	int freeSlots = this.getFreeSlots();
    	for (int i = 0; i < freeSlots; i++){
			this.coreUpgrades.add(UpgradeCore.STORAGE);
			this.getStorage().addStorageUpgrade();
			this.nStorageUpg += 1;
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
	public void onInventoryChanged(){
		super.onInventoryChanged();

		if (this.hasUpgrade(UpgradeCore.REDSTONE) || this.hasUpgrade(UpgradeCore.HOPPER))
			this.worldObj.notifyBlockChange(this.xCoord, this.yCoord, this.zCoord, this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord));
		
		if (this.hasUpgrade(UpgradeCore.ENDER) && !this.worldObj.isRemote)
			BSpaceStorageHandler.instance().updateAllBarrels(this.id);
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
	public int getSizeInventory() {return this.getStorage().getSizeInventory();}
	@Override
	public ItemStack getStackInSlot(int islot) {
		ItemStack stack = this.getStorage().getStackInSlot(islot);
		this.onInventoryChanged();
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);
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
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);
		return stack;
	}
	@Override
	public void setInventorySlotContents(int islot, ItemStack stack) { 
		this.getStorage().setInventorySlotContents(islot, stack);
		this.onInventoryChanged();
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
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {return this.getStorage().isItemValidForSlot(i, itemstack);}

	@Override
	public ItemStack getStoredItemType() {
		return this.getStorage().getStoredItemType();
	}

	@Override
	public void setStoredItemCount(int amount) {
		this.getStorage().setStoredItemCount(amount);
		this.onInventoryChanged();
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);
	}

	@Override
	public void setStoredItemType(ItemStack type, int amount) {
		this.getStorage().setStoredItemType(type, amount);
		this.onInventoryChanged();
		PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(this), this.worldObj.provider.dimensionId);
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
