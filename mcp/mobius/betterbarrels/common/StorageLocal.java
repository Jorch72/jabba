package mcp.mobius.betterbarrels.common;

import java.util.logging.Level;

import mcp.mobius.betterbarrels.mod_BetterBarrels;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

public class StorageLocal implements IBarrelStorage {

	private ItemStack item = null;			//Current stored item
	//private ItemStack transaction = null;	//Transaction stack for automatic handling
	//private int amount      = 0;	//Quantity of items in the barrel
	private int totalAmount   = 0;	//Total number of items
	private int stackAmount   = 0;	//Number of items in a stack
	//private int freeSpace     = 0;	//Free space
	//private int totalCapacity = 0;	//Total capacity taking into account upgrades
	
	private int baseslots   = 32;	    		//Number of accessible slots from outside (for automatic mods like BC & LP). Base value (cst)
	private int accslots    = 32;				//Number of accessible slots from outside (for automatic mods like BC & LP). Computed value on upgrade
	private int basestacks  = 64;				//Base amount of stacks in the barrel, before upgrades
	private int maxstacks   = 64;				//Maximum amount of stacks in the barrel
	private int upgCapacity = 0;				//Current capacity upgrade level
	private int storageID   = -1;
	
	private boolean keepLastItem = false;	//Ghosting mod. If true, we don't reset the item type when the barrel is empty
	private boolean isPrivate    = false;
	private String  username     = "";
	private boolean dirty        = false;
	private boolean wasEmpty     = false;
	
	private ItemStack[] slots = new ItemStack[128];

	public StorageLocal(){}	
	public StorageLocal(int ID){ this.storageID = ID; }
	public StorageLocal(NBTTagCompound tag){ this.readTagCompound(tag);	}
	public StorageLocal(int basestacks, int baseslots){
		this.basestacks = basestacks;
		this.baseslots  = baseslots;
		this.maxstacks  = this.basestacks;
		this.accslots   = this.baseslots;
	}

	/* ITEM MANIPULATION */
    @Override	
	public boolean   hasItem(){ return this.item == null ? false : true; }
    @Override    
	public ItemStack getItem(){ return this.item; }
    @Override	
	public void      setItem(ItemStack stack){
    	this.wasEmpty = false;
    	
    	if (stack != null){
    		this.item = stack.copy();
    		this.item.stackSize = 0;
    	} else {
    		this.item = null;
    	}
		this.setAllSlots(stack);
	}
	
	private void     setAllSlots(ItemStack stack){
		if (stack != null)
			for (int i = 0; i < this.baseslots; i++){
				this.slots[i] = stack.copy();
				this.slots[i].stackSize = 0;
			}
		else
			for (int i = 0; i < this.baseslots; i++)
				this.slots[i] = null;
	}
	
    @Override
	public boolean sameItem(ItemStack stack){
    	if (!this.hasItem()) return true;
    	if (stack == null)   return false;
		//if ((stack == null) || !this.hasItem()) return false;

		int oreIDBarrel = OreDictionary.getOreID(this.getItem());
		int oreIDStack  = OreDictionary.getOreID(stack);
		
		boolean stackIsMetal = OreDictionary.getOreName(oreIDBarrel).startsWith("ingot") ||
							   OreDictionary.getOreName(oreIDBarrel).startsWith("ore")   ||
							   OreDictionary.getOreName(oreIDBarrel).startsWith("dust")  ||
							   OreDictionary.getOreName(oreIDBarrel).startsWith("block") ||
							   OreDictionary.getOreName(oreIDBarrel).startsWith("nugget") ;
		
		boolean oreDictEquals = (oreIDStack != -1) && (oreIDBarrel != -1) && (oreIDBarrel == oreIDStack);
		boolean rawEquals     = this.getItem().isItemEqual(stack) && ItemStack.areItemStackTagsEqual(this.getItem(), stack); 
		
		return rawEquals || (stackIsMetal && oreDictEquals);
	}
	
	/* NBT MANIPULATION */
	@Override
	public NBTTagCompound writeTagCompound() {
		NBTTagCompound retTag = new NBTTagCompound();

		retTag.setInteger("amount",       this.totalAmount);
		retTag.setBoolean("keepLastItem", this.keepLastItem);
		retTag.setInteger("maxstacks",    this.maxstacks);
		retTag.setInteger("upgCapacity",  this.upgCapacity);
		retTag.setInteger("StorageID",    this.storageID);
		retTag.setBoolean("private", this.isPrivate);
		retTag.setString("username", this.username);
        
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
		this.storageID        = tag.getInteger("StorageID");
    	this.item             = tag.hasKey("current_item") ? ItemStack.loadItemStackFromNBT(tag.getCompoundTag("current_item")) : null;		
    	this.keepLastItem     = tag.hasKey("keepLastItem") ? tag.getBoolean("keepLastItem") : false;
    	this.isPrivate        = tag.hasKey("private") ? tag.getBoolean("private") : false;
    	this.username         = tag.hasKey("username") ? tag.getString("username") : "";
    	this.setItem(this.item);
	}
	
    /* STATUS MANIPULATION */	
	@Override
	public boolean switchGhosting() { this.keepLastItem = !this.keepLastItem; this.setDirty(); return this.keepLastItem; }
	@Override
	public boolean isGhosting() { return this.keepLastItem; }
	@Override
	public boolean isPrivate() { return this.isPrivate; }
	@Override
	public void    setPrivate(boolean status) { this.isPrivate = status; }
	@Override
	public void    setUsername(String username) { this.username = username; }
	@Override
	public String  getUsername() { return this.username; }
	@Override
	public boolean canInteractStrong(String username) {	return true; }	//TODO : Not urgent. Required for access rights.
	@Override
	public boolean canInteract(String username) { return true;	}		//TODO : Not urgent. Required for access rights.
	
	/* AMOUNT HANDLING */
	@Override
	public int   getAmount() { return this.totalAmount; }
	@Override
	public void  setAmount(int amount) { 
		this.totalAmount  = amount; 
		this.setDirty(); 
		this.updateStacks();
	}
	@Override
	public void  modAmount(int amount) { 
		this.totalAmount += amount; 
		this.setDirty(); 
		this.updateStacks();
	}

	@Override
	public int getMaxStacks(){
		return this.maxstacks;
	}
	
	private void cleanBarrel() {
		this.setItem(null);
		this.totalAmount = 0;
		this.stackAmount = 0;
		this.setDirty();
	}
	
	private void updateStacks() {
		
		//for (int i = 0; i < this.getSizeInventory(); i++)
		//	System.out.printf("%s ", this.slots[i]);
		//System.out.printf("\n");
		
		// 1. We check the current amount stored inside stacks
		//    On top of that, we also keep a copy of the stack if we are empty 
		int currentStackAmount = 0;
		ItemStack foundStack = null;
		for (int i = 0; i < this.getSizeInventory(); i++)
			if (this.slots[i] != null){
				currentStackAmount += this.slots[i].stackSize;
				if (!this.hasItem())
					foundStack = this.slots[i].copy(); 
			}
		
		// 2. If we are empty, setup the current item type to the found type (if it exists)
		if (!this.hasItem() && (foundStack != null))
			this.setItem(foundStack);
		
		// 3. We compute from how much the amount stored in stacks changed and we update the total amount
        //    If deltaAmount is not null, we have a change of internal values, so make it dirty 
		int  deltaAmount  = currentStackAmount - this.stackAmount;
		this.totalAmount += deltaAmount;
		if (deltaAmount != 0) this.setDirty();
		
		//this.logInt("delta ", deltaAmount);

		// 4. If the totalAmount is now 0 and we are not ghosting, clean up the barrel and return
		// TODO : This will always make the barrel dirty for empty barrels !
		if ((this.totalAmount <= 0) && !this.isGhosting()){
			if (!this.wasEmpty){
				this.cleanBarrel();
				this.wasEmpty = true;
			}
			return;
		}
		
		if (this.getItem() == null)
			return;
		
		for (int i = 0; i < this.getSizeInventory(); i++){
			if (this.slots[i] == null){
				this.slots[i] = this.getItem().copy();
				this.slots[i].stackSize = 0;
			}
				
		}
		
		
		// 5. We handle the slot 0 (transit slot).
		//    If it contains a stack and the stack is none empty, we empty it and remove the value from currentStackAmount
		//    We assume that the item is set, that way, if it is not (and it should), the code will crash		
		currentStackAmount -= this.slots[0].stackSize;
		this.slots[0].stackSize = 0;
		int stack0Size = Math.min(this.getAmount() - (this.maxstacks - 1)*this.getItem().getMaxStackSize(), this.getItem().getMaxStackSize());
		stack0Size     = Math.max(stack0Size, 0);
		this.slots[0].stackSize = stack0Size;
		currentStackAmount += this.slots[0].stackSize;
				

		// 6. We loop over all the slots, checking the stack in it, and pushing the missing part
		//    We assume that the item is set, that way, if it is not (and it should), the code will crash
		//    We also start at slot 1 as slot 0 is supposed to remain empty for optimisation		
		int remainingAmount = this.totalAmount;
		this.stackAmount = 0;
		this.stackAmount += this.slots[0].stackSize;
		
		for (int i = 1; i < this.getSizeInventory(); i++){
			if (this.slots[i] == null)
				this.slots[i]= this.getItem().copy();
			this.slots[i].stackSize = 0;
		}

		for (int i = 1; i < this.getSizeInventory(); i++){
			int toAdd = Math.min(this.slots[i].getMaxStackSize(), remainingAmount);
			this.slots[i].stackSize = toAdd;
			remainingAmount  -= toAdd;
			this.stackAmount += toAdd;
		}

		// 7. We invert slot 0 and 1 so the first slot is alway full (fix for hoopers)
		int s0s = this.slots[0].stackSize;
		int s1s = this.slots[1].stackSize;
		
		this.slots[0].stackSize = s1s;
		this.slots[1].stackSize = s0s;
	}
	
	/* INTERNAL METHODS */
	@Override
	public boolean update() {
		this.updateStacks();
		return false; 
	};
	
	@Override
	public void    upgCapacity(int level) {
		this.maxstacks   = Math.max(this.basestacks * (int)Math.pow(2, level), this.maxstacks); 
		this.upgCapacity = Math.max(level, this.upgCapacity);		
		this.accslots    = Math.min(this.baseslots * (int)Math.pow(2, this.upgCapacity), 128);		
	};
	
	public void    setDirty()   { this.dirty = true; };
	public void    clearDirty() { this.dirty = false; };
	public boolean isDirty()    { return this.dirty; };
	
	/* MANUAL STACK */
	@Override	
    public int       addStack(ItemStack stack) {
		if ((stack == null) || !this.sameItem(stack)) return 0;
		
		int deposit;
		if (!this.hasItem()){
			this.setItem(stack);
			this.setAmount(stack.stackSize);
			deposit = stack.stackSize;
			stack.stackSize -= deposit;
		} else {
			int totalCapacity = this.getItem().getMaxStackSize() * this.maxstacks;
			int freeSpace     = totalCapacity - this.getAmount();
			deposit       = Math.min(stack.stackSize, freeSpace);
			stack.stackSize  -= deposit;
			this.modAmount(deposit);
		}
		this.setDirty();		
		return deposit;
	} 	
	
	@Override	
    public ItemStack getStack() {
		if (this.getItem() != null)
			return this.getStack(this.getItem().getMaxStackSize());
		else
			return null;    
    }
	
	@Override	
    public ItemStack getStack(int amount) {

		ItemStack retStack = null;
		if (this.hasItem()){
			amount = Math.min(amount, this.getItem().getMaxStackSize());
			amount = Math.min(amount, this.getAmount());

			retStack = this.getItem().copy();			
			this.modAmount(-amount);
			retStack.stackSize = amount;
		}
		this.setDirty();		
		return retStack;
	}	
	
    /* REMOTE STORAGE HANDLING */
	@Override
    public int getStorageID() { return this.storageID; }
	
    /* IInventory PARTIAL INTERFACE */
	@Override
	public int getSizeInventory() { return this.accslots; } 
	@Override
	
	public ItemStack getStackInSlot(int islot) {
		return this.slots[islot]; 
	}
	
	@Override
	public ItemStack decrStackSize(int islot, int quantity) {
		ItemStack returnStack = this.item.copy();
		int stackSize = Math.min(quantity, this.slots[islot].stackSize);
		returnStack.stackSize = stackSize;
		this.slots[islot].stackSize -= stackSize;
		//this.modAmount(-stackSize);
		return returnStack;
	}
	
	@Override
	public void setInventorySlotContents(int islot, ItemStack stack) { this.slots[islot] = stack; }

	@Override
	public boolean isStackValidForSlot(int i, ItemStack itemstack) {
		if (!this.hasItem() && this.keepLastItem) return false;
		if (!this.hasItem())          return true;
		if (this.sameItem(itemstack)) return true;
		return false;		
	};
	
	/* LOGGING OUTPUTS */
	private void logStack(String msg, ItemStack stack){ mod_BetterBarrels.log.log(Level.INFO, String.format("%s %s", msg, stack)); }
	private void   logMsg(String msg)                 { mod_BetterBarrels.log.log(Level.INFO, String.format("%s", msg)); }
	private void   logInt(String msg, int value)      { mod_BetterBarrels.log.log(Level.INFO, String.format("%s %s", msg, value)); }
	
}