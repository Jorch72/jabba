package mcp.mobius.betterbarrels.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import mcp.mobius.betterbarrels.common.LocalizedChat;
import net.minecraft.network.packet.Packet250CustomPayload;

public class Packet0x09LocalizedChat {
   public byte header;
   public byte messageID;
   public int count;
   public Integer[] extraNumbers;

   public Packet0x09LocalizedChat(Packet250CustomPayload packet) {
      DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));

      try {
         this.header = inputStream.readByte();
         this.messageID = inputStream.readByte();
         this.count = inputStream.readInt();

         this.extraNumbers = new Integer[this.count];

         for (int i = 0; i < this.count; i++) {
            this.extraNumbers[i] = inputStream.readInt();
         }
      } catch (IOException e) {
      }
   }

   public static Packet250CustomPayload create(LocalizedChat message, Integer ... extraNumbers) {
      Packet250CustomPayload packet = new Packet250CustomPayload();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream outputStream = new DataOutputStream(bos);

      try {
         outputStream.writeByte(0x09);
         outputStream.writeByte(message.ordinal());

         outputStream.writeInt(extraNumbers.length);

         for (int i = 0; i < extraNumbers.length; i++) {
            outputStream.writeInt(extraNumbers[i]);
         }
      } catch (IOException e) {
      }

      packet.channel = "JABBA";
      packet.data = bos.toByteArray();
      packet.length = bos.size();

      return packet;
   }
}
