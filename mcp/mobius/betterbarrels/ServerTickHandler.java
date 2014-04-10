package mcp.mobius.betterbarrels;

import java.util.EnumSet;
import java.util.WeakHashMap;

import mcp.mobius.betterbarrels.bspace.BSpaceStorageHandler;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public enum ServerTickHandler implements ITickHandler {
	INSTANCE;
	
	// Hash map of dirty barrels for automatic cleanup
	// The boolean is never used and is just there to be able to have a WeakHashMap with automatic key handling
	private WeakHashMap<TileEntityBarrel, Boolean> dirtyBarrels = new WeakHashMap<TileEntityBarrel, Boolean>();
	public long timer1000 = System.nanoTime();
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if(type.contains(TickType.SERVER)){
			// One second timer
			if (System.nanoTime() - timer1000 > 1000000000L){
				timer1000 = System.nanoTime();
				
				for (TileEntityBarrel barrel : dirtyBarrels.keySet()){
					barrel.onInventoryChangedExec();
				}
				dirtyBarrels.clear();
			}
			
		}
	}

	public void markDirty(TileEntityBarrel barrel){
		this.markDirty(barrel, true);
	}
	public void markDirty(TileEntityBarrel barrel, boolean bspace){
		this.dirtyBarrels.put(barrel, true);
		if (bspace)
			if (barrel.coreUpgrades.hasEnder && !barrel.worldObj.isRemote) BSpaceStorageHandler.instance().markAllDirty(barrel.id);		
	}
	
	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.SERVER);
	}

	@Override
	public String getLabel() {
		return "Jabba sync handler";
	}

}
