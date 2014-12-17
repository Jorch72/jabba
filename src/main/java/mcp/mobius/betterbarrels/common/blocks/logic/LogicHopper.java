package mcp.mobius.betterbarrels.common.blocks.logic;

import mcp.mobius.betterbarrels.common.blocks.IBarrelStorage;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import mcp.mobius.betterbarrels.common.items.upgrades.UpgradeSide;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;

public enum LogicHopper {
	INSTANCE;

	private boolean isStorage(TileEntity inventory) {
		if (inventory instanceof IDeepStorageUnit) return true;
		if (inventory instanceof IInventory) return true;
		return false;
	}

	public boolean run(TileEntityBarrel barrel) {
		boolean transaction = false;
		IBarrelStorage store = barrel.getStorage();

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (barrel.sideUpgrades[side.ordinal()] == UpgradeSide.HOPPER) {
				//shortcut out if no work
				if (barrel.sideMetadata[side.ordinal()] == UpgradeSide.RS_FULL && (!store.hasItem() || store.getAmount() <= 0)) continue;
				if (barrel.sideMetadata[side.ordinal()] == UpgradeSide.RS_EMPT && (store.getAmount() >= store.getMaxStoredCount())) continue;

				TileEntity targetEntity = barrel.getWorldObj().getTileEntity(barrel.xCoord + side.offsetX, barrel.yCoord + side.offsetY, barrel.zCoord + side.offsetZ);
				if (isStorage(targetEntity)) {
					if (barrel.sideMetadata[side.ordinal()] == UpgradeSide.RS_FULL) {
						// Output mode
						ItemStack stack = store.getStackInSlot(1);
						if (!this.isFull(targetEntity, side.getOpposite())) {
							stack = barrel.getStorage().getStackInSlot(1);
							if(stack != null && stack.stackSize > 0 && this.pushItemToInventory(targetEntity, side.getOpposite(), stack)) {
								barrel.getStorage().markDirty();
								transaction = true;
								targetEntity.markDirty();
							}
						}
					} else {
						// Input mode
						if (store.getAmount() != store.getMaxStoredCount()) {
							ItemStack pulledStack = pullMatchingItemFromInventory(store, targetEntity, side.getOpposite());
							if (pulledStack != null) {
								if (store.hasItem())
									store.setStoredItemCount(store.getAmount() + 1);
								else
									store.setStoredItemType(pulledStack, 1);
								transaction = true;
								targetEntity.markDirty();
							}
						}
					}
				}
			}
		}
		return transaction;
	}

	private boolean isFull(TileEntity inventory, ForgeDirection side) {
		if (inventory instanceof IDeepStorageUnit) {
			IDeepStorageUnit dsu = (IDeepStorageUnit)inventory;
			ItemStack is = dsu.getStoredItemType();
			if (is == null || is.stackSize != dsu.getMaxStoredCount())
				return false;
			return true;
		} else if (inventory instanceof ISidedInventory  && side.ordinal() > -1) {
			ISidedInventory sinv = (ISidedInventory)inventory;
			int[] islots = sinv.getAccessibleSlotsFromSide(side.ordinal());

			for (int index : islots) {
				ItemStack is = sinv.getStackInSlot(index);
				if (is == null || is.stackSize != is.getMaxStackSize())
					return false;
			}
			return true;
		} else if (inventory instanceof IInventory) {
			IInventory inv = (IInventory)inventory;
			for (int index = 0; index < inv.getSizeInventory(); index++) {
				ItemStack is = inv.getStackInSlot(index);
				if (is == null || is.stackSize != is.getMaxStackSize())
					return false;
			}
			return true;
		}
		return true;
	}

	private boolean pushItemToInventory(TileEntity inventory, ForgeDirection side, ItemStack stack) {
		if (inventory instanceof IDeepStorageUnit) {
			IDeepStorageUnit dsu = (IDeepStorageUnit)inventory;
			ItemStack is = dsu.getStoredItemType();

			if (is == null) {
				is = stack.copy();
				dsu.setStoredItemType(is, 1);
				stack.stackSize--;
				return true;
			} else if (is.isItemEqual(stack) && is.stackSize < dsu.getMaxStoredCount()) {
				dsu.setStoredItemCount(is.stackSize + 1);
				stack.stackSize--;
				return true;
			}
		} else if (inventory instanceof ISidedInventory  && side.ordinal() > -1) {
			ISidedInventory sinv = (ISidedInventory)inventory;
			int[] islots = sinv.getAccessibleSlotsFromSide(side.ordinal());
			for (int slot : islots) {
				if (!sinv.canInsertItem(slot, stack, side.ordinal())) continue;
				ItemStack targetStack = sinv.getStackInSlot(slot);

				if (targetStack == null) {
					targetStack = stack.copy();
					targetStack.stackSize = 1;
					sinv.setInventorySlotContents(slot, targetStack);
					stack.stackSize--;
					return true;
				} else if (targetStack.isItemEqual(stack) && targetStack.stackSize < targetStack.getMaxStackSize()) {
					targetStack.stackSize++;
					stack.stackSize--;
					return true;
				}
			}
		} else if (inventory instanceof IInventory) {
			IInventory inv = (IInventory)inventory;
			int nslots = inv.getSizeInventory();
			for (int slot = 0; slot < nslots; slot++) {
				ItemStack targetStack = inv.getStackInSlot(slot);

				if (targetStack == null) {
					targetStack = stack.copy();
					targetStack.stackSize = 1;
					inv.setInventorySlotContents(slot, targetStack);
					stack.stackSize--;
					return true;
				} else if (targetStack.isItemEqual(stack) && targetStack.stackSize < targetStack.getMaxStackSize()) {
					targetStack.stackSize++;
					stack.stackSize--;
					return true;
				}
			}
		}

		return false;
	}

	private ItemStack pullMatchingItemFromInventory(IBarrelStorage barrel, TileEntity source, ForgeDirection side) {
		if (source instanceof IDeepStorageUnit) {
			IDeepStorageUnit dsu = (IDeepStorageUnit)source;
			ItemStack stack = dsu.getStoredItemType();
			if (stack != null && barrel.sameItem(stack) && stack.stackSize > 0) {
				dsu.setStoredItemCount(stack.stackSize - 1);
				stack = stack.copy();
				stack.stackSize = 1;
				return stack;
			}
		} else if (source instanceof ISidedInventory  && side.ordinal() > -1) {
			ISidedInventory sinv = (ISidedInventory)source;
			int[] islots = sinv.getAccessibleSlotsFromSide(side.ordinal());
			for (int slot : islots) {
				if (!sinv.canExtractItem(slot, barrel.getItem(), side.ordinal())) continue;
				ItemStack stack = sinv.getStackInSlot(slot);
				if (stack != null && barrel.sameItem(stack) && stack.stackSize > 0) {
					return sinv.decrStackSize(slot, 1);
				}
			}
		} else if (source instanceof IInventory) {
			IInventory inv = (IInventory)source;
			int nslots = inv.getSizeInventory();
			for (int slot = 0; slot < nslots; slot++) {
				ItemStack stack = inv.getStackInSlot(slot);
				if (stack != null && barrel.sameItem(stack) && stack.stackSize > 0) {
					return inv.decrStackSize(slot, 1);
				}
			}
		}

		return null;
	}
}
