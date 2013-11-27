package mcp.mobius.betterbarrels.client;

import mcp.mobius.betterbarrels.common.TileEntityBarrel;
import mcp.mobius.betterbarrels.client.render.TileEntityBarrelRenderer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

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
