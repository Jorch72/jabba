package mcp.mobius.betterbarrels.client;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ReloadableResourceManager;
import net.minecraft.client.resources.ResourceManager;
import net.minecraft.client.resources.ResourceManagerReloadListener;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringTranslate;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.bspace.BBEventHandler;
import mcp.mobius.betterbarrels.client.render.BlockBarrelRenderer;
import mcp.mobius.betterbarrels.client.render.TileEntityBarrelRenderer;
import mcp.mobius.betterbarrels.common.BaseProxy;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import mcp.mobius.betterbarrels.common.items.upgrades.StructuralLevel;

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
	
   public static ReloadableResourceManager rm = null;

   public void postInit(){
	   // Reflect (1.6.4: Vanilla, SRG, and DeObf)
      for (String field: new String[]{ "ao", "field_110451_am", "mcResourceManager" }) {
         try {
            Field fResourceManager = Minecraft.getMinecraft().getClass().getDeclaredField(field);
            if (fResourceManager != null) {
               fResourceManager.setAccessible(true);
               rm = (ReloadableResourceManager)fResourceManager.get(Minecraft.getMinecraft());
               break;
            }
         } catch (Exception e) {
         }
      }
      if (rm != null) {
         rm.registerReloadListener(new ResourceManagerReloadListener() {
            private boolean ranOnce = false;

            @Override
            public void onResourceManagerReload(ResourceManager resourcemanager) {
               if (!ranOnce) { // The MC engine calls this several times, ignore the first
                  ranOnce = true;
                  return;
               }
               if (StructuralLevel.LEVELS != null) {
                  for (int level = 1; level < StructuralLevel.LEVELS.length; level++) {
                     StringTranslate.inject(new ByteArrayInputStream(("item.upgrade.structural." + String.valueOf(level) + ".name=" + StatCollector.translateToLocal("item.upgrade.structural") + " " +  StructuralLevel.romanNumeral(level) + " (" +  StructuralLevel.LEVELS[level].name + ")").getBytes()));
                     StructuralLevel.LEVELS[level].generateIcons();
                  }
               }
            }
         });
      }
	}
}

