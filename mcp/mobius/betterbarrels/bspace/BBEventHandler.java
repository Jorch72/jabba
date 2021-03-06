package mcp.mobius.betterbarrels.bspace;

import mcp.mobius.betterbarrels.common.items.dolly.ItemBarrelMover;
import mcp.mobius.betterbarrels.common.items.upgrades.ItemUpgradeCore;
import mcp.mobius.betterbarrels.common.items.upgrades.ItemUpgradeStructural;
import mcp.mobius.betterbarrels.common.items.upgrades.UpgradeCore;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class BBEventHandler {

	@ForgeSubscribe
	public void onWorldLoad(WorldEvent.Load event) {
		if (!event.world.isRemote && event.world.provider.dimensionId == 0)
			BSpaceStorageHandler.instance().loadFromFile();
	}
	
	@ForgeSubscribe
	public void onItemTooltip(ItemTooltipEvent event){
		if (event.itemStack.getItem() instanceof ItemUpgradeCore){
			event.toolTip.add(1, "Slots : " + UpgradeCore.mapMetaSlots[event.itemStack.getItemDamage()]);
		}
		
		if (event.itemStack.getItem() instanceof ItemUpgradeStructural){
			int nslots = 0;
			for (int i = 0; i < event.itemStack.getItemDamage() + 1; i++)
				nslots += MathHelper.floor_double(Math.pow(2, i));			
			
			event.toolTip.add(1, "Slots : " + nslots);
		}

		if (event.itemStack.getItem() instanceof ItemBarrelMover){
			if (event.itemStack.hasTagCompound() && event.itemStack.getTagCompound().hasKey("Container")){
				NBTTagCompound tag = event.itemStack.getTagCompound().getCompoundTag("Container");
				int id = tag.getInteger("ID");
				int meta = tag.getInteger("Meta");
				ItemStack stack = new ItemStack(id, 0, meta);
				event.toolTip.add(1, stack.getDisplayName());				
			} else {
				event.toolTip.add(1, "< Empty >");
			}
		}
	}	
}
