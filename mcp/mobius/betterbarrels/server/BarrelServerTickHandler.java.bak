package mcp.mobius.betterbarrels.server;

import java.util.EnumSet;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class BarrelServerTickHandler implements ITickHandler {

	int tickSinceLastSave = 0;
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if(type.contains(TickType.WORLD)){
			if (this.tickSinceLastSave >= 1200){
				this.tickSinceLastSave = 0;
				SaveHandler.saveData();
			}
			this.tickSinceLastSave += 1;
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel() {
		return "betterbarrels.server.tickhandler";
	}

}
