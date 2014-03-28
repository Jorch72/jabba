package mcp.mobius.betterbarrels.client;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;

import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.bspace.BBEventHandler;
import mcp.mobius.betterbarrels.client.render.BlockBarrelRenderer;
import mcp.mobius.betterbarrels.client.render.TileEntityBarrelRenderer;
import mcp.mobius.betterbarrels.common.BaseProxy;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import mcp.mobius.betterbarrels.common.items.upgrades.StructuralLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringTranslate;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

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
		((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new IResourceManagerReloadListener() {
			private boolean ranOnce = false;

			@Override
			public void onResourceManagerReload(IResourceManager resourcemanager) {
				// FML forces a TExturePack reload after MC has finished initialising, allowing for mod icons to be registered at any point in init
				// we only want to run on the second and later icon reloads
				if (!ranOnce) {
					ranOnce = true;
					return;
				}
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
