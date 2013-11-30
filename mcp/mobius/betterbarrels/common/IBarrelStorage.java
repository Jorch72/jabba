package mcp.mobius.betterbarrels.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IBarrelStorage {
	
	/* ITEM MANIPULATION */
	public boolean   hasItem();
	public ItemStack getItem();
	public void      setItem(ItemStack stack);
    public boolean   sameItem(ItemStack stack);
	
	/* NBT MANIPULATION */
    public NBTTagCompound writeTagCompound();
    public void readTagCompound(NBTTagCompound tag);
	
    /* STATUS MANIPULATION */
    public boolean switchGhosting();
    public boolean isGhosting();
    public boolean isPrivate();
    public void    setPrivate(boolean status);
    public void    setUsername(String username);
    public String  getUsername();
    public boolean canInteractStrong(String username);
    public boolean canInteract(String username);    
    
    /* STORAGE HANDLING */
	public int  getAmount();
	public void setAmount(int amount);
	public void modAmount(int amount);
	public int  getMaxStacks();
   
	/* INTERNAL METHODS */
	public boolean update();
	public void    upgCapacity(int level);
	public void    setDirty();
	public void    clearDirty();
	public boolean isDirty();	
	
	/* MANUAL STACK */
    public int       addStack(ItemStack stack);
    public ItemStack getStack();
    public ItemStack getStack(int amount);	
	
    /* REMOTE STORAGE HANDLING */
    public int       getStorageID();
    
    /* IInventory PARTIAL INTERFACE */
    public int getSizeInventory();
    public ItemStack getStackInSlot(int islot);
    public ItemStack decrStackSize(int islot, int quantity);
    public void setInventorySlotContents(int islot, ItemStack stack);
	public boolean isStackValidForSlot(int i, ItemStack itemstack);    
    
	public int[] getAccessibleSlotsFromSide(int var1);
	public boolean  canInsertItem(int slot, ItemStack itemstack, int side);
	public boolean canExtractItem(int slot, ItemStack itemstack, int side);
	
	/*
	public int       getAmount();
	public void      upgCapacity(int level);
	public int       getStorageID();
	public int       getBaseStacks();
	*/
	
    /** Manually added stacks **/
	/*
    public int       addStack(ItemStack stack);
    public ItemStack getStack();
    public ItemStack getStack(int amount);
    */
    
    /** Internal state methods **/
	/*
    public boolean update();
    */
    
    /* IInventory partial interface */
	/*
    public int getSizeInventory();
    public ItemStack getStackInSlot(int islot);
    public ItemStack decrStackSize(int islot, int quantity);
    public void setInventorySlotContents(int islot, ItemStack stack);
	public boolean isStackValidForSlot(int i, ItemStack itemstack);
	*/     
}
