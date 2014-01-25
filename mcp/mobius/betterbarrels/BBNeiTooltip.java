package mcp.mobius.betterbarrels;

import java.util.List;

import mcp.mobius.betterbarrels.common.items.upgrades.ItemUpgradeCore;
import mcp.mobius.betterbarrels.common.items.upgrades.UpgradeCore;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import codechicken.nei.forge.GuiContainerManager;
import codechicken.nei.forge.IContainerTooltipHandler;

public class BBNeiTooltip implements IContainerTooltipHandler {

	@Override
	public List<String> handleTooltipFirst(GuiContainer gui, int mousex, int mousey, List<String> currenttip) {
		return currenttip;
	}

	@Override
	public List<String> handleItemTooltip(GuiContainer gui, ItemStack itemstack, List<String> currenttip) {
		if (itemstack.getItem() instanceof ItemUpgradeCore){
			currenttip.add(1, "Slots : " + UpgradeCore.mapMetaSlots[itemstack.getItemDamage()]);
			//currenttip.add(1, UpgradeCore.mapMetaDescript[itemstack.getItemDamage()]);
		}
		return currenttip;
	}

	public static void registerHandler(){
		GuiContainerManager.addTooltipHandler(new BBNeiTooltip());
	}
}
