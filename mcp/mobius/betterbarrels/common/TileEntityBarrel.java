package mcp.mobius.betterbarrels.common;

import java.util.Random;

import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.server.FMLServerHandler;
import mcp.mobius.betterbarrels.mod_BetterBarrels;
import mcp.mobius.betterbarrels.common.items.ItemBSpaceInterface;
import mcp.mobius.betterbarrels.common.items.ItemCapaUpg;
import mcp.mobius.betterbarrels.server.BSpaceStorageHandler;
import mcp.mobius.betterbarrels.server.SaveHandler;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;


//public class TileEntityBarrel extends TileEntity implements ISidedInventory {
public class TileEntityBarrel extends TileEntity{
	private static int version = 3;
	
    public IBarrelStorage storage     = new StorageLocal();
	public ForgeDirection orientation = ForgeDirection.UNKNOWN;
	public int levelStructural;	
	
	
	/* SAVING AND LOADING OF DATA */
	
	@Override
    public void writeToNBT(NBTTagCompound NBTTag)
    {
        super.writeToNBT(NBTTag);
        NBTTag.setInteger("version",     this.version);        
        NBTTag.setInteger("orientation", this.orientation.ordinal());
    }  	

	@Override	
    public void readFromNBT(NBTTagCompound NBTTag)
    {
    	super.readFromNBT(NBTTag);
    	this.orientation = ForgeDirection.getOrientation(NBTTag.getInteger("orientation"));
    }	
	
    @Override
    public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt) {
        this.readFromNBT(pkt.data);
    }    

    @Override
    public Packet132TileEntityData getDescriptionPacket()
    {
        NBTTagCompound var1 = new NBTTagCompound();
        this.writeToNBT(var1);
        return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, var1);
    }	
	
}
