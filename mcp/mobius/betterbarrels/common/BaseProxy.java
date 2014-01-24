package mcp.mobius.betterbarrels.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class BaseProxy {
	
	public void registerEventHandler(){
		//MinecraftForge.EVENT_BUS.register(new ServerEventHandler());		
	}
	
	public void registerRenderers() {
		// Nothing here as this is the server side proxy
	}

    void updatePlayerInventory(EntityPlayer player)
    {
        if (player instanceof EntityPlayerMP)
        {
            EntityPlayerMP playerMP = (EntityPlayerMP)player;
            playerMP.sendContainerToPlayer(playerMP.inventoryContainer);
        }
    }	
	
}
