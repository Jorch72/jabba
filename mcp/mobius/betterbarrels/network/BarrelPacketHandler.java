package mcp.mobius.betterbarrels.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import mcp.mobius.betterbarrels.common.LocalizedChat;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ChatMessageComponent;
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
				if (barrel != null)
					barrel.getStorage().setStoredItemType(packetCast.stack, packetCast.amount);
				//Minecraft.getMinecraft().theWorld.markBlockForRenderUpdate(packetCast.x, packetCast.y, packetCast.z);				
			}
			else if (header == 0x02){
				Packet0x02GhostUpdate packetCast = new Packet0x02GhostUpdate(packet);
				TileEntityBarrel barrel = (TileEntityBarrel)Minecraft.getMinecraft().theWorld.getBlockTileEntity(packetCast.x, packetCast.y, packetCast.z);
				if (barrel != null){
					barrel.getStorage().setGhosting(packetCast.locked);
					Minecraft.getMinecraft().theWorld.markBlockForRenderUpdate(packetCast.x, packetCast.y, packetCast.z);
				}
			}
			else if (header == 0x03){
				Packet0x03SideUpgradeUpdate packetCast = new Packet0x03SideUpgradeUpdate(packet);
				TileEntityBarrel barrel = (TileEntityBarrel)Minecraft.getMinecraft().theWorld.getBlockTileEntity(packetCast.x, packetCast.y, packetCast.z);
				if (barrel != null){
					barrel.sideUpgrades = packetCast.sideUpgrades;
					barrel.sideMetadata = packetCast.sideMetadata;
					Minecraft.getMinecraft().theWorld.markBlockForRenderUpdate(packetCast.x, packetCast.y, packetCast.z);
				}
			}
			else if (header == 0x04){
				Packet0x04StructuralUpdate packetCast = new Packet0x04StructuralUpdate(packet);
				TileEntityBarrel barrel = (TileEntityBarrel)Minecraft.getMinecraft().theWorld.getBlockTileEntity(packetCast.x, packetCast.y, packetCast.z);
				if (barrel != null){
					barrel.levelStructural = packetCast.level;
					Minecraft.getMinecraft().theWorld.markBlockForRenderUpdate(packetCast.x, packetCast.y, packetCast.z);
				}
			}	
			else if (header == 0x05){
				Packet0x05CoreUpdate packetCast = new Packet0x05CoreUpdate(packet);
				TileEntityBarrel barrel = (TileEntityBarrel)Minecraft.getMinecraft().theWorld.getBlockTileEntity(packetCast.x, packetCast.y, packetCast.z);
				if (barrel != null){
					barrel.coreUpgrades = packetCast.upgrades;
					barrel.hasRedstone  = packetCast.hasRedstone;
					barrel.hasHopper    = packetCast.hasHopper;
					barrel.hasEnder     = packetCast.hasEnder;
					barrel.nStorageUpg  = packetCast.nStorageUpg;
				}
			}	
			else if (header == 0x06){
				Packet0x06FullStorage packetCast = new Packet0x06FullStorage(packet);
				TileEntityBarrel barrel = (TileEntityBarrel)Minecraft.getMinecraft().theWorld.getBlockTileEntity(packetCast.x, packetCast.y, packetCast.z);
				if (barrel != null){
					barrel.setStorage(packetCast.storage);
				}
			}	
			else if (header == 0x07){
				Packet0x07ForceRender packetCast = new Packet0x07ForceRender(packet);
				Minecraft.getMinecraft().theWorld.markBlockForRenderUpdate(packetCast.x, packetCast.y, packetCast.z);
			}			
			
			else if (header == 0x08){
				Packet0x08LinkUpdate packetCast = new Packet0x08LinkUpdate(packet);
				TileEntityBarrel barrel = (TileEntityBarrel)Minecraft.getMinecraft().theWorld.getBlockTileEntity(packetCast.x, packetCast.y, packetCast.z);
				if (barrel != null){
					barrel.isLinked     = packetCast.isLinked;
					Minecraft.getMinecraft().theWorld.markBlockForRenderUpdate(packetCast.x, packetCast.y, packetCast.z);					
				}
			}			
         else if (header == 0x09){
            Packet0x09LocalizedChat packetCast = new Packet0x09LocalizedChat(packet);
            ChatMessageComponent packetMessage = new ChatMessageComponent();
            if (packetCast.count == 0) {
               packetMessage.addKey(LocalizedChat.values()[packetCast.messageID].localizationKey);            
            } else {
               packetMessage.addFormatted(LocalizedChat.values()[packetCast.messageID].localizationKey, (Object[])(packetCast.extraNumbers));
            }
            Minecraft.getMinecraft().thePlayer.sendChatToPlayer(packetMessage);
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

    public static void writeNBTTagCompound(NBTTagCompound par0NBTTagCompound, DataOutput par1DataOutput) throws IOException
    {
        if (par0NBTTagCompound == null)
        {
            par1DataOutput.writeShort(-1);
        }
        else
        {
            byte[] abyte = CompressedStreamTools.compress(par0NBTTagCompound);
            par1DataOutput.writeShort((short)abyte.length);
            par1DataOutput.write(abyte);
        }
    }		
	
    public static void sendLocalizedChat(EntityPlayer player, LocalizedChat message, Integer ... extraNumbers) {
       PacketDispatcher.sendPacketToPlayer(Packet0x09LocalizedChat.create(message, extraNumbers), (Player)player);
     }
}
