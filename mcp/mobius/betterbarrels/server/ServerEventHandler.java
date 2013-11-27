package mcp.mobius.betterbarrels.server;

import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class ServerEventHandler {

	@ForgeSubscribe
	public void onWorldLoad(WorldEvent.Load var1) {
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && var1.world.provider.dimensionId == 0) {
			SaveHandler.loadData();
		}	
	}
}
