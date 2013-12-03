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
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;


//public class TileEntityBarrel extends TileEntity implements ISidedInventory {
public class TileEntityBarrel extends TileEntity implements IInventory, IDeepStorageUnit {

	public int blockOrientation    = -1;		//Faces with an item display (int to byte)
	public int blockOriginalOrient = -1;		//Original orientation of the barrel (int to byte)
	private boolean syncAmount     = true;		//Should the amount be synced on entity update ?
	private boolean syncAspect     = true;
	public boolean orientDirty     = false; 	//Has the barrel orientation changed ?
	private int tickSinceLastUpdate = 0;			//Timer for forced barrel updates
	
	public int upgradeCapacity    = 0;			//Capacity upgrade level
	//public int upgradeOutput      = 0;
	
	public boolean storageRemote   = false;				    //Type of storage. For now, 0 => local, 1 => remote.
	public int     storageRemoteID = -1;				//Storage ID for none local storages.
	
	private int prevAmount = 0;
	private ItemStack prevItem = null;
	
    private long clickTime = -20;				//Click timer for double click handling
	
    private int version = 2;						//Version of the NBT Tag format
    
    public IBarrelStorage storage = null;
    
    public TileEntityBarrel(){
    	this.storage = new StorageLocal();
    }

    public TileEntityBarrel(int stacks, int slots){
    	this.storage = new StorageLocal(stacks, slots);
    }    
    
	void switchGhosting(World world){
		this.storage.switchGhosting();
		this.updateEntity();
		this.onInventoryChanged();
		world.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);		
	}
    
	void setUsername(String username){
		this.storage.setUsername(username);
	}
	
	@Override
	public void updateEntity() {
		/*
		this.syncAmount = this.storage.update() || this.syncAmount;
		//this.syncAmount = this.storage.update();

		if ((!this.storage.hasItem()) && (this.prevItem == null)){
			this.syncAmount = false;			
		}		
		else if ((this.prevAmount != this.storage.getAmount()) || (!this.storage.sameItem(this.prevItem))){
			this.syncAmount = true;
			this.prevAmount = this.storage.getAmount();
			this.prevItem = this.storage.getItem();
		} else {
			this.syncAmount = false;
		}
		
		//We send a forced update every 5 seconds.
		this.tickSinceLastUpdate += 1;
		if (this.tickSinceLastUpdate > 100){
			this.syncAmount = true;
			this.tickSinceLastUpdate = 0;
		}
		 */
		if (!(this.worldObj.isRemote)){
			this.tickSinceLastUpdate += 1;
			this.storage.update();
			this.syncAmount = this.storage.isDirty();
			
			if (this.orientDirty){
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, this.blockOrientation, 1 + 2);
				this.orientDirty = false;
			}
			
			if (this.blockOrientation == -1){
				this.blockOrientation    = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
				this.blockOriginalOrient = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
			}

			if (((this.syncAmount) || (this.syncAspect)) && (this.tickSinceLastUpdate > 5)) {
			//if (((this.syncAmount) || (this.syncAspect))) {			
				
				PacketDispatcher.sendPacketToAllAround(this.xCoord, this.yCoord, this.zCoord, 50.0, this.worldObj.provider.dimensionId, this.getDescriptionPacket());
				this.syncAmount = false;
				this.syncAspect = false;
				this.storage.clearDirty();
				this.tickSinceLastUpdate = 0;
			}
		}
	}

	/*/////////////////////*/
	/* USER INTERACTIONS   */
	/*/////////////////////*/
	void leftClick(EntityPlayer player){
		this.storage.update();
		
		ItemStack droppedStack = null;
		if (player.isSneaking())
			droppedStack = this.storage.getStack(1); 
		else
			droppedStack = this.storage.getStack();

		if ((droppedStack != null) && (droppedStack.stackSize > 0))
			this.dropItemInWorld(player, droppedStack, 0.02);
		
		this.syncAmount = true;
		this.updateEntity();
	}
	
	void rightClick(EntityPlayer player){
		this.storage.update();

		// First, we try to add the current held item
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
		
		this.syncAmount = true;
		this.updateEntity();
	}

	void applyUpgrade(World world, ItemStack upgradeStack, EntityPlayer player){

		// Structural upgrades //
		if (upgradeStack.getItem() instanceof ItemCapaUpg){ 
			if (upgradeStack.getItemDamage() == this.upgradeCapacity){
				upgradeStack.stackSize -= 1;
				this.upgradeCapacity += 1;
				this.syncAspect = true;
				this.storage.upgCapacity(this.upgradeCapacity);
			} else if ((player instanceof EntityPlayerMP) && (upgradeStack.getItemDamage() == (this.upgradeCapacity - 1))) {
				((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(new Packet3Chat(
						ChatMessageComponent.createFromText("Upgrade already applied."), false));				
			} else if ((player instanceof EntityPlayerMP) && (upgradeStack.getItemDamage() < this.upgradeCapacity)) {
				((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(new Packet3Chat(
						ChatMessageComponent.createFromText("You cannot downgrade a barrel."), false));
			} else if ((player instanceof EntityPlayerMP) && (upgradeStack.getItemDamage() > this.upgradeCapacity)) {
				((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(new Packet3Chat(
						ChatMessageComponent.createFromText("You need at least an upgrade Mark " + upgradeStack.getItemDamage() + " to apply this."), false));
			}
		}
		
		// BSpace Interface upgrade //
		else if ((upgradeStack.getItem() instanceof ItemBSpaceInterface) && !this.storageRemote && this.upgradeCapacity >= 4 && !this.storage.hasItem()){
			upgradeStack.stackSize -= 1;
			this.storageRemote   = true;
			this.storageRemoteID = -1;
		}
		else if ((upgradeStack.getItem() instanceof ItemBSpaceInterface) && !this.storageRemote && this.upgradeCapacity < 4){
			((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(new Packet3Chat(
					ChatMessageComponent.createFromText("You need at least an upgrade Mark 4 to apply this."), false));			
		}
		else if ((upgradeStack.getItem() instanceof ItemBSpaceInterface) && !this.storageRemote && this.storage.hasItem()){
			((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(new Packet3Chat(
					ChatMessageComponent.createFromText("You can only apply this on empty and unlocked barrels."), false));
		}
		
		// Update the block/entity //
		this.updateEntity();		
		world.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);		
	}
	
	void applySticker(World world, int x, int y, int z, ItemStack stack, ForgeDirection side){
		if ((side == ForgeDirection.UP) || (side == ForgeDirection.DOWN)) {return;}
		if ((side == ForgeDirection.getOrientation(this.blockOrientation))) {return;}
    	if (((1 << (side.ordinal() - 2)) & this.blockOrientation) != 0) {return;}
		
		this.blockOrientation = this.blockOrientation | ((1 << (side.ordinal() - 2)));
		world.setBlockMetadataWithNotify(x, y, z, this.blockOrientation | ((1 << (side.ordinal() - 2))), 1 + 2);
		
		stack.stackSize -= 1;
		
		this.syncAspect = true;
		this.updateEntity();
		world.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}
	
	void tuneBarrel(World world, ItemStack stack, EntityPlayer player){
		if(!this.storageRemote) {
			((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(new Packet3Chat(
					ChatMessageComponent.createFromText("Barrel requires a BSpace interface to be tunable"), false));
			return;
		}
		
		boolean forkTuned     = false;
		boolean barrelTuned   = this.storageRemoteID != -1 ? true : false;
		int     forkFrequency = -1;
		
		NBTTagCompound nbtStack = stack.getTagCompound();		
		if (stack.getTagCompound() != null){
			forkTuned     = true;
			forkFrequency = nbtStack.getInteger("frequency"); 
		} else {
			nbtStack = new NBTTagCompound();
			nbtStack.setInteger("frequency", -1);
		}

		// We tune both
		if (!forkTuned && !barrelTuned){
			IBarrelStorage storage = BSpaceStorageHandler.instance.getNewStorage();
			forkFrequency        = storage.getStorageID();
			this.storageRemoteID = storage.getStorageID();
			this.storage         = storage;
			storage.upgCapacity(this.upgradeCapacity);
			((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(new Packet3Chat(
					ChatMessageComponent.createFromText("Barrel and fork are now tuned to " + String.valueOf(storage.getStorageID())), false));			
		}
		
		// We tune the barrel
		else if (forkTuned && !barrelTuned){
			IBarrelStorage storage = BSpaceStorageHandler.instance.getStorage(forkFrequency);
			this.storageRemoteID = storage.getStorageID();
			this.storage = storage;
			storage.upgCapacity(this.upgradeCapacity);			
			((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(new Packet3Chat(
					ChatMessageComponent.createFromText("Barrel is now tuned to " + String.valueOf(storage.getStorageID())), false));			
		}
		
		// We tune the fork
		else if (!forkTuned && barrelTuned){
			forkFrequency = this.storage.getStorageID();
			((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(new Packet3Chat(
					ChatMessageComponent.createFromText("Fork is now tuned to " + String.valueOf(this.storage.getStorageID())), false));		
		}		

		else if (forkTuned && barrelTuned){
			((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(new Packet3Chat(
					ChatMessageComponent.createFromText("Both fork and barrel are already tuned"), false));
		}
		
		if (forkFrequency != -1){
			nbtStack.setInteger("frequency", forkFrequency);
			stack.setTagCompound(nbtStack);		
			stack.setItemName("Tuned fork " + String.valueOf(forkFrequency));
		} else {
			stack.setTagCompound(null);
		}
		this.updateEntity();
		SaveHandler.saveData();

	}
	
	void lockBarrel(World world, ItemStack stack, EntityPlayer player){
		if (!this.storage.isPrivate()){
			stack.stackSize -= 1;
			this.storage.setPrivate(true);
		}

		this.updateEntity();
		world.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);			
		
	}
	
	private void dropItemInWorld(EntityPlayer player, ItemStack stack, double speedfactor){
        EntityItem droppedEntity = new EntityItem(this.worldObj, (double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D, stack);

        if (player != null)
        {
            Vec3 motion = Vec3.createVectorHelper(player.posX - (double)this.xCoord, player.posY - (double)this.yCoord, player.posZ - (double)this.zCoord);
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

    /*////////////////*/
    /* Data load/save */
    /*////////////////*/	
	
    @Override	
    public void writeToNBT(NBTTagCompound NBTTag)
    {
    	this.writeToNBT(NBTTag, true);
    }
	
    private void writeToNBT(NBTTagCompound NBTTag, boolean full)
    {
        if (full){
        	super.writeToNBT(NBTTag);
        }
        
        NBTTag.setInteger("version",          this.version);        
        NBTTag.setInteger("barrelOrient",     this.blockOrientation);
        NBTTag.setInteger("barrelOrigOrient", this.blockOriginalOrient);          
        NBTTag.setInteger("upgradeCapacity",  this.upgradeCapacity);     
        
        NBTTag.setBoolean("storageRemote",    this.storageRemote);
        NBTTag.setInteger("storageRemoteID",  this.storageRemoteID);
    	NBTTag.setCompoundTag("storage",      this.storage.writeTagCompound());
		this.syncAmount = true;        
    }

    @Override
    public void readFromNBT(NBTTagCompound NBTTag){
    	this.readFromNBT(NBTTag, true);
    }
    
    private void readFromNBT(NBTTagCompound NBTTag, boolean full)
    {
    	if (full)
    		super.readFromNBT(NBTTag);

    	this.blockOrientation = NBTTag.getInteger("barrelOrient");
    	this.upgradeCapacity  = NBTTag.getInteger("upgradeCapacity");
    	
    	
    	this.storageRemote     = NBTTag.getBoolean("storageRemote");
    	this.storageRemoteID   = NBTTag.getInteger("storageRemoteID");
    	this.storage.readTagCompound(NBTTag.getCompoundTag("storage"));
    	
    	this.blockOriginalOrient = NBTTag.hasKey("barrelOrigOrient") ? NBTTag.getInteger("barrelOrigOrient") : this.blockOrientation;
		this.orientDirty = true;    
		this.syncAmount = true;
		
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && (this.storageRemote) && (this.storageRemoteID != -1)){
			if(!BSpaceStorageHandler.loaded)
				SaveHandler.loadData();
			if (this.storage != BSpaceStorageHandler.instance.getStorage(this.storageRemoteID))
				this.storage = BSpaceStorageHandler.instance.getStorage(this.storageRemoteID);
		}
    }	

    @Override
    public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt) {
        this.readFromNBT(pkt.data, false);
    }    

    @Override
    public Packet132TileEntityData getDescriptionPacket()
    {
        NBTTagCompound var1 = new NBTTagCompound();
        this.writeToNBT(var1, false);
        return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, var1);
    }

    
    /*/////////////////////////////////////*/
    /* IInventory Interface Implementation */
    /*/////////////////////////////////////*/
    
	@Override
	public int getSizeInventory() {return this.storage.getSizeInventory();}
	@Override
	public ItemStack getStackInSlot(int islot) { return this.storage.getStackInSlot(islot); }
	@Override
	public ItemStack decrStackSize(int islot, int quantity) { return this.storage.decrStackSize(islot, quantity);}
	@Override
	public void setInventorySlotContents(int islot, ItemStack stack) { this.storage.setInventorySlotContents(islot, stack); this.updateEntity(); }
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
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {return this.storage.isStackValidForSlot(i, itemstack);}

	@Override
	public ItemStack getStoredItemType() {
		return this.storage.getStoredItemType();
	}

	@Override
	public void setStoredItemCount(int amount) {
		this.storage.setStoredItemCount(amount);
	}

	@Override
	public void setStoredItemType(ItemStack type, int amount) {
		this.storage.setStoredItemType(type, amount);
	}

	@Override
	public int getMaxStoredCount() {
		return this.storage.getMaxStoredCount();
	}

	/*
	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return this.storage.getAccessibleSlotsFromSide(var1);
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemstack, int side) {
		return this.storage.canInsertItem(slot, itemstack, side);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemstack, int side) {
		return this.storage.canExtractItem(slot, itemstack, side);
	}
	*/

	
}
