package mcp.mobius.betterbarrels.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import mcp.mobius.betterbarrels.mod_BetterBarrels;
import mcp.mobius.betterbarrels.client.render.TileEntityBarrelRenderer;
import mcp.mobius.betterbarrels.common.BaseProxy;
import mcp.mobius.betterbarrels.common.TileEntityBarrel;
//import mcp.mobius.betterbarrels.common.TileEntityBarrelShelf;
//import mcp.mobius.betterbarrels.common.TileEntityMiniBarrel;

public class ClientProxy extends BaseProxy {
	
	@Override
	public void registerRenderers() {
		//MinecraftForgeClient.preloadTexture(BLOCK_PNG);
		//MinecraftForgeClient.preloadTexture(ITEMS_PNG);		
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrel.class, new TileEntityBarrelRenderer());
		
		//ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMiniBarrel.class,  new TileEntityMiniBarrelRenderer());
		//ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrelShelf.class,  new TileEntityBarrelShelfRenderer());
		
		mod_BetterBarrels.RENDER_SHELF = RenderingRegistry.getNextAvailableRenderId();
	}
	
	@Override
	public void registerEventHandler(){
		//TODO : Turned off registering end of rendering event to check for fps drop
		
		//MinecraftForge.EVENT_BUS.register(new ClientEventHandler());		
	}	
}
