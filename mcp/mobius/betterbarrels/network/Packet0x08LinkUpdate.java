package mcp.mobius.betterbarrels.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import net.minecraft.network.packet.Packet250CustomPayload;

public class Packet0x08LinkUpdate {
	public byte header;
	public int  x,y,z;
	public boolean isLinked;
	
	public Packet0x08LinkUpdate(Packet250CustomPayload packet){
		DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
		
		try{
			this.header    = inputStream.readByte();
			this.x         = inputStream.readInt();
			this.y         = inputStream.readInt();
			this.z         = inputStream.readInt();
			this.isLinked  = inputStream.readBoolean();
			
		} catch (IOException e){}
	}
	
	public static Packet250CustomPayload create(TileEntityBarrel barrel){
		
		Packet250CustomPayload packet = new Packet250CustomPayload();
		ByteArrayOutputStream bos     = new ByteArrayOutputStream();
		DataOutputStream outputStream = new DataOutputStream(bos);		
		
		try{
			outputStream.writeByte(0x08);
			outputStream.writeInt(barrel.xCoord);
			outputStream.writeInt(barrel.yCoord);
			outputStream.writeInt(barrel.zCoord);
			outputStream.writeBoolean(barrel.isLinked);
			
		}catch(IOException e){}
		
		packet.channel = "JABBA";
		packet.data    = bos.toByteArray();
		packet.length  = bos.size();
		
		return packet;
	}
}
