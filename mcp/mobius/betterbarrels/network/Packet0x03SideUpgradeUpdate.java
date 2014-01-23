package mcp.mobius.betterbarrels.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import mcp.mobius.betterbarrels.common.TileEntityBarrel;
import net.minecraft.network.packet.Packet250CustomPayload;

public class Packet0x03SideUpgradeUpdate {

	public byte header;
	public int  x,y,z;
	public int[] sideUpgrades = new int[6];

	public Packet0x03SideUpgradeUpdate(Packet250CustomPayload packet){
		DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
		
		try{
			this.header    = inputStream.readByte();
			this.x         = inputStream.readInt();
			this.y         = inputStream.readInt();
			this.z         = inputStream.readInt();

			for (int i = 0; i < 6; i++)
				this.sideUpgrades[i] = inputStream.readInt();			
			
		} catch (IOException e){}
	}
	
	public static Packet250CustomPayload create(TileEntityBarrel barrel){
		
		Packet250CustomPayload packet = new Packet250CustomPayload();
		ByteArrayOutputStream bos     = new ByteArrayOutputStream();
		DataOutputStream outputStream = new DataOutputStream(bos);		
		
		try{
			outputStream.writeByte(0x03);
			outputStream.writeInt(barrel.xCoord);
			outputStream.writeInt(barrel.yCoord);
			outputStream.writeInt(barrel.zCoord);
			
			for (int i = 0; i < 6; i++)
				outputStream.writeInt(barrel.sideUpgrades[i]);
			
		}catch(IOException e){}
		
		packet.channel = "JABBA";
		packet.data    = bos.toByteArray();
		packet.length  = bos.size();
		
		return packet;
	}	
	
}
