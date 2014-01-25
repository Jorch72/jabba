package mcp.mobius.betterbarrels.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import mcp.mobius.betterbarrels.common.TileEntityBarrel;
import net.minecraft.network.packet.Packet250CustomPayload;

public class Packet0x05CoreUpdate {
	public byte header;
	public int  x,y,z;
	public byte  nStorageUpg;
	public boolean hasRedstone;
	public ArrayList<Integer> upgrades = new ArrayList<Integer>();

	public Packet0x05CoreUpdate(Packet250CustomPayload packet){
		DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
		
		try{
			this.header      = inputStream.readByte();
			this.x           = inputStream.readInt();
			this.y           = inputStream.readInt();
			this.z           = inputStream.readInt();
			this.nStorageUpg = inputStream.readByte();
			this.hasRedstone = inputStream.readBoolean();
			int size         = inputStream.readInt();

			for (int i = 0; i < size; i++)
				this.upgrades.add(inputStream.readInt());
			
		} catch (IOException e){}
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
			outputStream.writeByte(barrel.nStorageUpg);
			outputStream.writeBoolean(barrel.hasRedstone);
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