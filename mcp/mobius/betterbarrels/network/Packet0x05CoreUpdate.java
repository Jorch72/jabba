package mcp.mobius.betterbarrels.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import mcp.mobius.betterbarrels.common.items.upgrades.UpgradeCore;
import net.minecraft.network.packet.Packet250CustomPayload;

public class Packet0x05CoreUpdate {
	public byte header;
	public int  x,y,z;
	public byte  nStorageUpg = 0;
	public boolean hasRedstone = false;
	public boolean hasHopper   = false;
	public boolean hasEnder    = false;
	public ArrayList<Integer> upgrades = new ArrayList<Integer>();

	public Packet0x05CoreUpdate(Packet250CustomPayload packet){
		DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
		
		try{
			this.header      = inputStream.readByte();
			this.x           = inputStream.readInt();
			this.y           = inputStream.readInt();
			this.z           = inputStream.readInt();
			int size         = inputStream.readInt();

			for (int i = 0; i < size; i++)
				this.upgrades.add(inputStream.readInt());
			
		} catch (IOException e){}
		
		for (Integer i : this.upgrades){
			if (i == UpgradeCore.STORAGE)
				this.nStorageUpg += 1;
			else if (i == UpgradeCore.ENDER)
				this.hasEnder = true;
			else if (i == UpgradeCore.HOPPER)
				this.hasHopper = true;
			else if (i == UpgradeCore.REDSTONE)
				this.hasRedstone = true;
		}
	}
	
	public static Packet250CustomPayload create(TileEntityBarrel barrel){
		
		Packet250CustomPayload packet = new Packet250CustomPayload();
		ByteArrayOutputStream bos     = new ByteArrayOutputStream();
		DataOutputStream outputStream = new DataOutputStream(bos);		
		
		try{
			outputStream.writeByte(0x05);
			outputStream.writeInt(barrel.xCoord);
			outputStream.writeInt(barrel.yCoord);
			outputStream.writeInt(barrel.zCoord);
			outputStream.writeInt(barrel.coreUpgrades.size());
			for (Integer i : barrel.coreUpgrades)
				outputStream.writeInt(i.intValue());
			
		}catch(IOException e){}
		
		packet.channel = "JABBA";
		packet.data    = bos.toByteArray();
		packet.length  = bos.size();
		
		return packet;
	}
}