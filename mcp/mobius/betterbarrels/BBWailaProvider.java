package mcp.mobius.betterbarrels;

import java.util.List;

import net.minecraft.item.ItemStack;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

public class BBWailaProvider implements IWailaDataProvider {
	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor,	IWailaConfigHandler config) { return null; }

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,	IWailaConfigHandler config) {
		TileEntityBarrel tebarrel = (TileEntityBarrel)accessor.getTileEntity();
		ItemStack barrelStack = tebarrel.storage.getItem();
		
		currenttip.add(String.format("Structural level : %d", tebarrel.levelStructural));
		currenttip.add(String.format("Upgrade slots : %d / %d", tebarrel.getFreeSlots(), tebarrel.getMaxUpgradeSlots()));		
		
		if (barrelStack != null){
			if(config.getConfig("bb.itemtype"))
				currenttip.add(barrelStack.getDisplayName());
			if(config.getConfig("bb.itemnumb"))
					currenttip.add(String.format("%d / %d items", tebarrel.storage.getAmount(), tebarrel.storage.getItem().getMaxStackSize() * tebarrel.storage.getMaxStacks()));
			if(config.getConfig("bb.space"))
					currenttip.add(String.format("%d stacks max", tebarrel.storage.getMaxStacks()));			
		} else {
			if(config.getConfig("bb.itemtype"))
				currenttip.add("<Empty>");
			if(config.getConfig("bb.space"))
				currenttip.add(String.format("%d stacks max", tebarrel.storage.getMaxStacks()));			
		}
		
		return currenttip;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,	IWailaConfigHandler config) {
		return currenttip;
	}	
	
	public static void callbackRegister(IWailaRegistrar registrar){
		registrar.addConfig("Better barrels", "bb.itemtype", "Barrel content");
		registrar.addConfig("Better barrels", "bb.itemnumb", "Items quantity");
		registrar.addConfig("Better barrels", "bb.space", "Max stacks");
		registrar.registerBodyProvider(new BBWailaProvider(), TileEntityBarrel.class);
	}
	
}


//registrar.addConfig("Better barrels", "bb.itemtype", "Barrel content");
//registrar.addConfig("Better barrels", "bb.itemnumb", "Items quantity");
//registrar.addConfig("Better barrels", "bb.space", "Max stacks");