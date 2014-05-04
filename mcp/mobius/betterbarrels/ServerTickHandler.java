package mcp.mobius.betterbarrels;

import java.util.EnumSet;
import java.util.WeakHashMap;

import mcp.mobius.betterbarrels.bspace.BSpaceStorageHandler;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public enum ServerTickHandler implements ITickHandler {
	INSTANCE;
	
	class Timer{
		private long interval;
		private long lastTick = System.nanoTime();
		
		public Timer(long interval){
			this.interval = interval * 1000L * 1000L; //Interval is passed in millisecond but stored in nanosecond.
		}
		
		public boolean isDone(){
			long    time  = System.nanoTime();
			long    delta = (time - this.lastTick) - this.interval;
			boolean done  = delta >= 0;
			if (!done) return false;
			
			this.lastTick = time - delta;
			return true;
		}
	}
	
	// Hash map of dirty barrels for automatic cleanup
	// The boolean is never used and is just there to be able to have a WeakHashMap with automatic key handling
	private WeakHashMap<TileEntityBarrel, Boolean> dirtyBarrels = new WeakHashMap<TileEntityBarrel, Boolean>();
	public Timer timer = new Timer(BetterBarrels.limiterDelay);
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if(type.contains(TickType.SERVER)){
			if (timer.isDone()){
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
