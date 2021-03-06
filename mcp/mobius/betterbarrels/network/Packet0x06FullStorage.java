package mcp.mobius.betterbarrels.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import mcp.mobius.betterbarrels.common.blocks.StorageLocal;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;

public class Packet0x06FullStorage{
	public byte header;
	public int  x,y,z;
	public StorageLocal storage = new StorageLocal();
	
	public Packet0x06FullStorage(Packet250CustomPayload packet){
		DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
		
		try{
			this.header    = inputStream.readByte();
			this.x         = inputStream.readInt();
			this.y         = inputStream.readInt();
			this.z         = inputStream.readInt();
			this.storage.readTagCompound(Packet.readNBTTagCompound(inputStream));
			
		} catch (IOException e){}
	}
	
	public static Packet250CustomPayload create(TileEntityBarrel barrel){
		
		Packet250CustomPayload packet = new Packet250CustomPayload();
		ByteArrayOutputStream bos     = new ByteArrayOutputStream();
		DataOutputStream outputStream = new DataOutputStream(bos);		
		
		try{
			outputStream.writeByte(0x06);
			outputStream.writeInt(barrel.xCoord);
			outputStream.writeInt(barrel.yCoord);
			outputStream.writeInt(barrel.zCoord);
			BarrelPacketHandler.writeNBTTagCompound(barrel.getStorage().writeTagCompound(), outputStream);
			
		}catch(IOException e){}
		
		packet.channel = "JABBA";
		packet.data    = bos.toByteArray();
		packet.length  = bos.size();
		
		return packet;
	}
	

}
