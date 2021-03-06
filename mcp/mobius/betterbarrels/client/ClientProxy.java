package mcp.mobius.betterbarrels.client;

import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;
import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.bspace.BBEventHandler;
import mcp.mobius.betterbarrels.client.render.BlockBarrelRenderer;
import mcp.mobius.betterbarrels.client.render.TileEntityBarrelRenderer;
import mcp.mobius.betterbarrels.common.BaseProxy;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;

public class ClientProxy extends BaseProxy {
	
	@Override
	public void registerRenderers() {
		//MinecraftForgeClient.preloadTexture(BLOCK_PNG);
		//MinecraftForgeClient.preloadTexture(ITEMS_PNG);		
		
		BetterBarrels.blockBarrelRendererID = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(BetterBarrels.blockBarrelRendererID, new BlockBarrelRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrel.class, new TileEntityBarrelRenderer());
		
		
		
		//ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMiniBarrel.class,  new TileEntityMiniBarrelRenderer());
		//ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrelShelf.class,  new TileEntityBarrelShelfRenderer());
		//mod_BetterBarrels.RENDER_SHELF = RenderingRegistry.getNextAvailableRenderId();
	}
	
	@Override
	public void registerEventHandler(){
		//TODO : Turned off registering end of rendering event to check for fps drop
		
		MinecraftForge.EVENT_BUS.register(new BBEventHandler());
	
	}
	
	public void postInit(){
	}	
}

