package mcp.mobius.betterbarrels.client;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class ClientEventHandler {

	@ForgeSubscribe
	public void onRenderWorldLastEvent(RenderWorldLastEvent e){
		{
			//TODO : Desactivated for speed
			/*
			for (TileEntityBarrel te : TileEntityBarrelRenderer.postponedSigns.keySet())
				TileEntityBarrelRenderer.instance().secondPassRendering(te, TileEntityBarrelRenderer.postponedSigns.get(te));
		
			TileEntityBarrelRenderer.postponedSigns.clear();
			*/
		}
	}
}
