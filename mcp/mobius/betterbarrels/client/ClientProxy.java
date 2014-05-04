package mcp.mobius.betterbarrels.client;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ReloadableResourceManager;
import net.minecraft.client.resources.ResourceManager;
import net.minecraft.client.resources.ResourceManagerReloadListener;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringTranslate;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.bspace.BBEventHandler;
import mcp.mobius.betterbarrels.client.render.BlockBarrelRenderer;
import mcp.mobius.betterbarrels.client.render.TileEntityBarrelRenderer;
import mcp.mobius.betterbarrels.common.BaseProxy;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import mcp.mobius.betterbarrels.common.items.upgrades.StructuralLevel;

public class ClientProxy extends BaseProxy {
	
	public static Map<Integer, ISimpleBlockRenderingHandler> blockRenderers;
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	@Override
	public void registerRenderers() {
		// Grab a static reference to the block renderers list for later use
		try {
			Field blockRendererField = RenderingRegistry.class.getDeclaredField("blockRenderers");
			blockRendererField.setAccessible(true);
			ClientProxy.blockRenderers = (Map<Integer, ISimpleBlockRenderingHandler>)blockRendererField.get(RenderingRegistry.instance());
		} catch (Throwable t) {}

		// Get the next "available" ID, and make sure it's really available
		BetterBarrels.blockBarrelRendererID = RenderingRegistry.getNextAvailableRenderId();
		while(blockRenderers.containsKey(BetterBarrels.blockBarrelRendererID)) {
			BetterBarrels.blockBarrelRendererID = RenderingRegistry.getNextAvailableRenderId();
		}

		RenderingRegistry.registerBlockHandler(BetterBarrels.blockBarrelRendererID, new BlockBarrelRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrel.class, new TileEntityBarrelRenderer());

		//ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMiniBarrel.class,  new TileEntityMiniBarrelRenderer());
		//ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrelShelf.class,  new TileEntityBarrelShelfRenderer());
		//mod_BetterBarrels.RENDER_SHELF = RenderingRegistry.getNextAvailableRenderId();
	}

	@Override
	public void checkRenderers() {
		ISimpleBlockRenderingHandler renderer = ClientProxy.blockRenderers.get(BetterBarrels.blockBarrelRendererID);

		if(!(renderer instanceof BlockBarrelRenderer)) {
			throw new RuntimeException(String.format("Wrong renderer found ! %s found while looking up the Jabba Barrel renderer.",  renderer.getClass().getCanonicalName()));
		}
	}

	@Override
	public void registerEventHandler(){
		//TODO : Turned off registering end of rendering event to check for fps drop
		
		MinecraftForge.EVENT_BUS.register(new BBEventHandler());
	
	}
	
   @Override
   public void postInit(){
      BetterBarrels.debug("06 - Registering resource manager reload listener");
      ((ReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new ResourceManagerReloadListener() {
         private boolean ranOnce = false;

         @Override
         public void onResourceManagerReload(ResourceManager resourcemanager) {
            if (!ranOnce) { // FML reloads the resources at the end of the MC loading cycle, we want to run then and afterwards
               ranOnce = true;
               return;
            }
            BetterBarrels.debug("07 - Resource pack is reloading, rechecking names and regenerating textures");
            StructuralLevel.loadBaseTextureData();
            if (StructuralLevel.LEVELS != null) {
               for (int level = 1; level < StructuralLevel.LEVELS.length; level++) {
                  StructuralLevel.LEVELS[level].discoverMaterialName();
                  StringTranslate.inject(new ByteArrayInputStream(("item.upgrade.structural." + String.valueOf(level) + ".name=" + StatCollector.translateToLocal("item.upgrade.structural") + " " +  StructuralLevel.romanNumeral(level) + " (" +  StructuralLevel.LEVELS[level].name + ")").getBytes()));
                  StructuralLevel.LEVELS[level].generateIcons();
               }
            }
            StructuralLevel.unloadBaseTextureData();
         }
      });
	}
}

