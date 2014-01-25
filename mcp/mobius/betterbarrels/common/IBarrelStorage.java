package mcp.mobius.betterbarrels.common;

import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IBarrelStorage extends ISidedInventory, IDeepStorageUnit{
	
	public boolean   hasItem();
	public ItemStack getItem();
	public void      setItem(ItemStack stack);
    public boolean   sameItem(ItemStack stack);
    
    /* STORAGE HANDLING */
	public int  getAmount();
	public void setAmount(int amount);
	public int  getMaxStacks();
    public void setBaseStacks(int basestacks);
    
	/* NBT MANIPULATION */
    public NBTTagCompound writeTagCompound();
    public void readTagCompound(NBTTagCompound tag);
 
	/* MANUAL STACK */
    public int       addStack(ItemStack stack);
    public ItemStack getStack();
    public ItemStack getStack(int amount);	
    
    /* STATUS MANIPULATION */
    public boolean switchGhosting();
    public boolean isGhosting();
    public void    setGhosting(boolean locked);
    public boolean isPrivate();
    public void    setPrivate(boolean status);
    public void    setUsername(String username);
    public String  getUsername();
    public boolean canInteractStrong(String username);
    public boolean canInteract(String username); 
    
    /* REMOTE STORAGE HANDLING */
    public int getStorageID(); 
    
	public void    setDirty();
	public void    clearDirty();
	public boolean isDirty();
	//public void    upgCapacity(int level);
	public void    addStorageUpgrade();
	public void    rmStorageUpgrade();	
	
	public ItemStack decrStackSize_Hopper(int slot, int quantity);
}
