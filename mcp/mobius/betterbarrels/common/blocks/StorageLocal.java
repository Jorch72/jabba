package mcp.mobius.betterbarrels.common.blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.common.blocks.logic.Coordinates;
import mcp.mobius.betterbarrels.common.blocks.logic.ItemImmut;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;
import mcp.mobius.betterbarrels.common.blocks.logic.OreDictPair;

public class StorageLocal implements IBarrelStorage{

	private ItemStack inputStack      = null;	// Slot 0
	private ItemStack prevInputStack  = null;
	private ItemStack outputStack     = null;	// Slot 1
	private ItemStack prevOutputStack = null;
	private ItemStack itemTemplate      = null;
	private ItemStack renderingTemplate = null;
	
	private int totalAmount   = 0;	//Total number of items
	private int stackAmount   = 0;	//Number of items in a stack
	
	private int basestacks    = 64;				//Base amount of stacks in the barrel, before upgrades
	private int maxstacks     = 64;				//Maximum amount of stacks in the barrel (post upgrade)
	private int upgCapacity   = 0;				//Current capacity upgrade level
	private boolean keepLastItem = false;	//Ghosting mod. If true, we don't reset the item type when the barrel is empty
	
	private Set<Coordinates> linkedStorages = new HashSet<Coordinates>();
	
    private static HashMap<OreDictPair, Boolean> oreDictCache = new HashMap<OreDictPair, Boolean>(); 	
	
	public StorageLocal(){ this.onInventoryChanged(); }	
	public StorageLocal(NBTTagCompound tag){ this.readTagCompound(tag); this.onInventoryChanged(); }
	public StorageLocal(int nupgrades) {
		for (int i = 0; i < nupgrades; i++)
			this.addStorageUpgrade();
		this.onInventoryChanged();
	}
	
	private ItemStack getStackFromSlot(int slot){ return slot == 0 ? this.inputStack : this.outputStack; }
	
	private int getFreeSpace(){ 
		if (this.hasItem())
			return (this.itemTemplate.getMaxStackSize() * this.getMaxStacks()) - this.totalAmount;
		else
			return 64;
	}
	
	// IBarrelStorage Interface //
    @Override	
	public boolean   hasItem(){ return this.itemTemplate == null ? false : true; }
    @Override    
	public ItemStack getItem(){ return this.itemTemplate; }
    @Override
    public ItemStack getItemForRender(){
    	if (this.renderingTemplate == null){
			this.renderingTemplate = this.itemTemplate.copy();    		
    		if (this.renderingTemplate.hasTagCompound() && this.renderingTemplate.getTagCompound().hasKey("ench"))
    			this.renderingTemplate.getTagCompound().removeTag("ench");
    		if (this.renderingTemplate.hasTagCompound() && this.renderingTemplate.getTagCompound().hasKey("CustomPotionEffects"))
    			this.renderingTemplate.getTagCompound().removeTag("CustomPotionEffects");    		
    		if (this.renderingTemplate.itemID == Item.potion.itemID)
    			this.renderingTemplate.setItemDamage(0);
    	}
    	return this.renderingTemplate;
    }
    
    
    @Override	
	public void      setItem(ItemStack stack){
    	if (stack != null){
    		this.itemTemplate = stack.copy();
    		this.itemTemplate.stackSize = 0;
    	} else {
    		this.itemTemplate = null;
    		this.renderingTemplate = null;
    	}
	}    
    

    
    @Override
	public boolean sameItem(ItemStack stack){
    	if (!this.hasItem() && this.isGhosting()) return false;
    	if (!this.hasItem()) return true;
    	if (stack == null)   return false;

		if (this.getItem().isItemEqual(stack) && ItemStack.areItemStackTagsEqual(this.getItem(), stack))
			return true;     	
    	
    	OreDictPair orePair  = new OreDictPair(
    			new ItemImmut(this.getItem().itemID,   this.getItem().getItemDamage()),
    			new ItemImmut(stack.itemID, stack.getItemDamage())
    	);
    	
    	if (!oreDictCache.containsKey(orePair)){
	    	int oreIDBarrel = OreDictionary.getOreID(this.getItem());
	    	int oreIDStack  = OreDictionary.getOreID(stack);
			boolean stackIsMetal =  OreDictionary.getOreName(oreIDBarrel).startsWith("ingot") ||
									OreDictionary.getOreName(oreIDBarrel).startsWith("ore")   ||
									OreDictionary.getOreName(oreIDBarrel).startsWith("dust")  ||
									OreDictionary.getOreName(oreIDBarrel).startsWith("block") ||
									OreDictionary.getOreName(oreIDBarrel).startsWith("nugget") ;
			
			oreDictCache.put(orePair, (oreIDStack != -1) && (oreIDBarrel != -1) && (oreIDBarrel == oreIDStack) && (stackIsMetal));
			//System.out.printf("Added ore pair for %d:%d | %d:%d = %s\n", this.getItem().itemID, this.getItem().getItemDamage(), stack.itemID, stack.getItemDamage(), oreDictCache.get(orePair));
    	}
		return oreDictCache.get(orePair);
	}    
    
	/* NBT MANIPULATION */
	@Override
	public NBTTagCompound writeTagCompound() {
		NBTTagCompound retTag = new NBTTagCompound();

		retTag.setInteger("amount",       this.totalAmount);
		retTag.setBoolean("keepLastItem", this.keepLastItem);
		retTag.setInteger("maxstacks",    this.maxstacks);
		retTag.setInteger("upgCapacity",  this.upgCapacity);
        
        if (this.getItem() != null){
            NBTTagCompound var3 = new NBTTagCompound();
            this.getItem().writeToNBT(var3);
            retTag.setCompoundTag("current_item", var3);
        }
		return retTag;
	}
	@Override
	public void readTagCompound(NBTTagCompound tag) {
		this.totalAmount      = tag.getInteger("amount");
		this.maxstacks        = tag.getInteger("maxstacks");
		this.upgCapacity      = tag.getInteger("upgCapacity");
    	this.itemTemplate     = tag.hasKey("current_item") ? ItemStack.loadItemStackFromNBT(tag.getCompoundTag("current_item")) : null;		
    	this.keepLastItem     = tag.hasKey("keepLastItem") ? tag.getBoolean("keepLastItem") : false;
    	this.setItem(this.itemTemplate);
	}    
    
	/* MANUAL STACK */
	@Override	
    public int addStack(ItemStack stack){
		boolean skip = stack == null || !this.sameItem(stack);
		if (!this.hasItem() && this.isGhosting() && stack != null) skip = false;
		
		if (skip) return 0;
		
		int deposit;
		if (!this.hasItem()){
			this.setItem(stack);
			this.totalAmount = stack.stackSize;
			deposit = stack.stackSize;
			stack.stackSize -= deposit;
		} else {
			int totalCapacity = this.getItem().getMaxStackSize() * this.maxstacks;
			int freeSpace     = totalCapacity - this.totalAmount;
			deposit       = Math.min(stack.stackSize, freeSpace);
			stack.stackSize  -= deposit;
			this.totalAmount += deposit;
		}
		this.onInventoryChanged();
		return deposit;    	
    }
	
	@Override
    public ItemStack getStack(){
		if (this.hasItem())
			return this.getStack(this.getItem().getMaxStackSize());
		else
			return null;      	
    }
    
	@Override	
    public ItemStack getStack(int amount){
		this.onInventoryChanged();
		
		ItemStack retStack = null;
		if (this.hasItem()){
			amount = Math.min(amount, this.getItem().getMaxStackSize());
			amount = Math.min(amount, this.totalAmount);

			retStack = this.getItem().copy();			
			this.totalAmount  -= amount;
			retStack.stackSize = amount;
		}
		
		this.onInventoryChanged();		
		return retStack;    	
    }
	
    /* STATUS MANIPULATION */	
	@Override
	public boolean switchGhosting() { this.keepLastItem = !this.keepLastItem; this.onInventoryChanged(); return this.keepLastItem; }
	@Override
	public boolean isGhosting() { return this.keepLastItem; }
	@Override
	public void setGhosting(boolean locked) { this.keepLastItem = locked; }	

	/* AMOUNT HANDLING */
	@Override
	public int   getAmount() { return this.totalAmount; }	
	
	@Override
	public void setAmount(int amount) { this.totalAmount = amount; }
	
	@Override	
	public void setBaseStacks(int basestacks){ 
		this.basestacks = basestacks;
		this.maxstacks  = basestacks;
	}	
	
	@Override
	public int getMaxStacks(){ return this.maxstacks; }
	
	/*
	@Override
	public void upgCapacity(int level) {
		this.maxstacks   = Math.max(this.basestacks * (int)Math.pow(2, level), this.maxstacks); 
		this.upgCapacity = Math.max(level, this.upgCapacity);
	}
	*/	
	
	@Override
	public void addStorageUpgrade(){
		this.upgCapacity += 1;
		this.maxstacks    = this.basestacks * (this.upgCapacity + 1);
	}
	
	@Override
	public void rmStorageUpgrade(){
		this.upgCapacity -= 1;
		this.maxstacks    = this.basestacks * (this.upgCapacity + 1);
	}	
	
	// ISidedInventory Interface //
	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return new int[]{0,1};
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemstack, int side) {
		if (slot == 1) return false;
		if (this.getFreeSpace() <= 0) return false;
		return this.sameItem(itemstack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemstack, int side) {
		if (slot == 0)         return false;
		if (!this.hasItem())   return false;
		if (itemstack == null) return true;
		return this.sameItem(itemstack);
	}

	
	// IInventory Interface //
	@Override
	public int getSizeInventory() {	return 2; }
	@Override
	public ItemStack getStackInSlot(int slot) {
		this.onInventoryChanged();
		return this.getStackFromSlot(slot); 
	}

	@Override
	public ItemStack decrStackSize(int slot, int quantity) {
		if (slot == 0)
			throw new RuntimeException("[JABBA] Tried to decr the stack size of the input slot");

		ItemStack stack = this.getStackFromSlot(slot).copy();
		int stackSize = Math.min(quantity, stack.stackSize);
		stack.stackSize = stackSize;
		this.getStackFromSlot(slot).stackSize -= stackSize;

		this.onInventoryChanged();
		return stack;		
	}

	@Override
	public ItemStack decrStackSize_Hopper(int slot, int quantity) {
		if (slot == 0)
			throw new RuntimeException("[JABBA] Tried to decr the stack size of the input slot");

		ItemStack stack = this.getStackFromSlot(slot).copy();
		int stackSize = Math.min(quantity, stack.stackSize);
		stack.stackSize = stackSize;
		this.getStackFromSlot(slot).stackSize -= stackSize;

		//this.onInventoryChanged();
		return stack;		
	}	
	
	@Override
	public ItemStack getStackInSlotOnClosing(int slot) { return this.getStackFromSlot(slot); }

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack) { 
		if (slot == 0)
			this.inputStack = itemstack;
		else
			this.outputStack = itemstack;
		
		this.onInventoryChanged();
	}

	@Override
	public String getInvName() { return "jabba.localstorage"; }

	@Override
	public boolean isInvNameLocalized() { return false; }

	@Override
	public int getInventoryStackLimit() { return 64; }

	@Override
	public void onInventoryChanged() {
		// TODO : Might need to do some cleanup here
		
		if (this.inputStack != null){
			if (!this.hasItem())
				this.setItem(this.inputStack);
			
			if (this.prevInputStack != null){
				this.totalAmount += this.inputStack.stackSize - this.prevInputStack.stackSize;
				this.inputStack   = null;
			} else {
				this.totalAmount += this.inputStack.stackSize;
				this.inputStack   = null;						
			}
		}
		
		if (this.hasItem() && this.getFreeSpace() < this.itemTemplate.getMaxStackSize()){
			this.inputStack           = this.itemTemplate.copy();
			this.inputStack.stackSize = this.itemTemplate.getMaxStackSize() - this.getFreeSpace();
		}
		
		if (this.hasItem() && outputStack == null && this.prevOutputStack != null)
			this.totalAmount -= prevOutputStack.stackSize;

		if (this.prevOutputStack != null && this.outputStack != null){
			int delta = this.prevOutputStack.stackSize - this.outputStack.stackSize;
			this.totalAmount -= delta;
			this.outputStack.stackSize = Math.min(this.outputStack.getMaxStackSize(), this.totalAmount);
		}		
		
		if (this.hasItem() && outputStack == null){
			this.outputStack = this.itemTemplate.copy();
			this.outputStack.stackSize = Math.min(this.outputStack.getMaxStackSize(), this.totalAmount);			
		}
		
		if (this.totalAmount == 0 && !this.isGhosting()){
			this.itemTemplate = null;
			this.outputStack  = null;
			this.inputStack   = null;
			this.renderingTemplate = null;			
		}
		
		this.prevOutputStack = this.outputStack != null ? this.outputStack.copy() : null;
		this.prevInputStack  = this.inputStack  != null ? this.inputStack.copy()  : null;		
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) { return true; }	// This is not handled here but rather in the TE itself

	@Override
	public void openChest() {} // Unused

	@Override
	public void closeChest() {} // Unused

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		return this.sameItem(itemstack);
	}

	
	// IDeepStorageUnit Interface //
	@Override
	public ItemStack getStoredItemType() {
		if (this.hasItem()){
			ItemStack stack = this.getItem().copy();
			stack.stackSize = this.totalAmount;
			return stack;
		} 
		else if (!this.hasItem() && this.isGhosting()){
			return new ItemStack(Block.endPortal, 0);
		}
		else{
			return null;
		}
	}
	
	@Override
	public void setStoredItemCount(int amount) {
		this.totalAmount = amount;
		this.onInventoryChanged();
	}
		
	@Override
	public void setStoredItemType(ItemStack type, int amount) {
		this.setItem(type);
		this.totalAmount = amount;
		this.onInventoryChanged();
	}
		
	@Override
	public int getMaxStoredCount() {
		if (this.hasItem())
			return this.maxstacks * this.getItem().getMaxStackSize();
		else
			return this.maxstacks * 64;
	}	
}