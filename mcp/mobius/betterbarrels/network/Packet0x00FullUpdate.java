package mcp.mobius.betterbarrels.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;

public class Packet0x00FullUpdate {

	public NBTTagCompound data = null;
	
	private Packet0x00FullUpdate(){}
	
	public static Packet250CustomPayload create(TileEntity entity){
		
		Packet250CustomPayload packet = new Packet250CustomPayload();
		ByteArrayOutputStream bos     = new ByteArrayOutputStream();
		DataOutputStream outputStream = new DataOutputStream(bos);		
		
		try{
			outputStream.writeByte(0x00);
		}catch(IOException e){}
		
		packet.channel = "JABBA";
		packet.data    = bos.toByteArray();
		packet.length  = bos.size();
		
		return packet;
	};
	
	public static Packet0x00FullUpdate read(Packet250CustomPayload packet){
		return null;
	};
	
}
