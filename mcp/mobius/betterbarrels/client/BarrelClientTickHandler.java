package mcp.mobius.betterbarrels.client;

import java.util.ArrayList;
import java.util.EnumSet;

import mcp.mobius.betterbarrels.common.TileEntityBarrel;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class BarrelClientTickHandler implements ITickHandler {

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		/*
		if(type.contains(TickType.RENDER)){
			//Render the blended signs
			
			for (TileEntityBarrel te : TileEntityBarrelRenderer.postponedSigns.keySet())
				TileEntityBarrelRenderer.instance().secondPassRendering(te, TileEntityBarrelRenderer.postponedSigns.get(te));
			
			TileEntityBarrelRenderer.postponedSigns.clear();
			
		}
		*/

	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.RENDER);
	}

	@Override
	public String getLabel() {
		return "betterbarrels.client.tickhandler";
	}

}
