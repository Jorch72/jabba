package mcp.mobius.betterbarrels.common.blocks.logic;

import java.util.ArrayList;

import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import mcp.mobius.betterbarrels.common.items.upgrades.UpgradeSide;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class LogicHopper {

	public static LogicHopper _instance = new LogicHopper();
	private LogicHopper(){}
	public static LogicHopper instance() { return LogicHopper._instance; }

	private int[] sideConvert = {0, 1, 4, 5, 3, 2, -1};	
	
	public boolean run(TileEntityBarrel barrel){
		boolean transaction = false;
		ItemStack  stack = barrel.storage.getStackInSlot(1);
		if (stack == null || stack.stackSize == 0) return false;
		
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS){
			if (barrel.sideUpgrades[side.ordinal()] == UpgradeSide.HOPPER){
				//System.out.printf("%s\n", side);
				
				TileEntity targetEntity = barrel.worldObj.getBlockTileEntity(barrel.xCoord + side.offsetX, barrel.yCoord + side.offsetY, barrel.zCoord + side.offsetZ);
				if ((targetEntity instanceof IInventory) && !this.isFull((IInventory)targetEntity, side.getOpposite())){
					
					stack = barrel.storage.getStackInSlot(1);
					if(stack != null && stack.stackSize > 0 && this.pushItemToInventory((IInventory)targetEntity, side.getOpposite(), stack)){
						barrel.storage.onInventoryChanged();
						transaction = true;
					}

				}
			}
		}
		return transaction;		
	}


	
	private boolean isFull(IInventory inventory, ForgeDirection side){
		int mcSide = sideConvert[side.ordinal()];
		
    	if (inventory instanceof ISidedInventory  && mcSide > -1){
    	    ISidedInventory sinv = (ISidedInventory)inventory;
    	    int[] islots = sinv.getAccessibleSlotsFromSide(mcSide);
    	    
    	    for (int index : islots){
    	    	ItemStack is = sinv.getStackInSlot(index); 
    	    	if ( is == null || is.stackSize != is.getMaxStackSize()) return false;
    	    }
    	    return true;
    	    
    	} else {
    		for (int index = 0; index < inventory.getSizeInventory(); index++){
    			ItemStack is = inventory.getStackInSlot(index);
    	    	if ( is == null || is.stackSize != is.getMaxStackSize()) return false;    			
    		}
    		return true;
    	}
	}
	
	private boolean pushItemToInventory(IInventory inventory, ForgeDirection side, ItemStack stack){
		int mcSide = sideConvert[side.ordinal()];
	
    	if (inventory instanceof ISidedInventory  && mcSide > -1){
    	    ISidedInventory sinv = (ISidedInventory)inventory;
    	    int[] islots = sinv.getAccessibleSlotsFromSide(mcSide); 
    	    
    	    for (int slot : islots){
    	    	if (!sinv.canInsertItem(slot, stack, mcSide)) continue;
    	    	ItemStack targetStack = sinv.getStackInSlot(slot);
    	    	
    	    	if (targetStack == null){
    	    		targetStack = stack.copy();
    	    		targetStack.stackSize = 1;
    	    		sinv.setInventorySlotContents(slot, targetStack);
    	    		stack.stackSize -= 1;
    	    		return true;
    	    		
    	    	} else if (targetStack.isItemEqual(stack)) {
    	    		targetStack.stackSize += 1;
    	    		stack.stackSize -= 1;
    	    		return true;
    	    	}
    	    }
    	    
    	    
    	} else {
    		
    		
    	}

    	return false;
	}
}
