package mcp.mobius.betterbarrels.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import mcp.mobius.betterbarrels.common.TileEntityBarrel;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class BarrelPacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {

		if (packet.channel.equals("JABBA")) {
			byte header = this.getHeader(packet);

			if (header == 0x01){
				Packet0x01ContentUpdate packetCast = new Packet0x01ContentUpdate(packet);
				TileEntityBarrel barrel = (TileEntityBarrel)Minecraft.getMinecraft().theWorld.getBlockTileEntity(packetCast.x, packetCast.y, packetCast.z);
				barrel.storage.setStoredItemType(packetCast.stack, packetCast.amount);
			}
			else if (header == 0x02){
				Packet0x02GhostUpdate packetCast = new Packet0x02GhostUpdate(packet);
				TileEntityBarrel barrel = (TileEntityBarrel)Minecraft.getMinecraft().theWorld.getBlockTileEntity(packetCast.x, packetCast.y, packetCast.z);
				barrel.storage.setGhosting(packetCast.locked);
			}
			else if (header == 0x03){
				Packet0x03SideUpgradeUpdate packetCast = new Packet0x03SideUpgradeUpdate(packet);
				TileEntityBarrel barrel = (TileEntityBarrel)Minecraft.getMinecraft().theWorld.getBlockTileEntity(packetCast.x, packetCast.y, packetCast.z);
				barrel.sideUpgrades = packetCast.sideUpgrades;
			}			
		}				
	}

	public byte getHeader(Packet250CustomPayload packet){
		DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
		try{
			return inputStream.readByte();
		} catch (IOException e){
			return -1;
		}
	}	
	
}
