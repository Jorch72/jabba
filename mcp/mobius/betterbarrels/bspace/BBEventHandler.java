package mcp.mobius.betterbarrels.bspace;

import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class BBEventHandler {

	@ForgeSubscribe
	public void onWorldLoad(WorldEvent.Load event) {
		if (!event.world.isRemote && event.world.provider.dimensionId == 0)
			BSpaceStorageHandler.instance().loadFromFile();
	}
}
