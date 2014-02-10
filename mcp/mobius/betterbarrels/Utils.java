package mcp.mobius.betterbarrels;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class Utils {

   public static void dropItemInWorld(TileEntity source, EntityPlayer player, ItemStack stack, double speedfactor) {

      int hitOrientation = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
      double stackCoordX = 0.0D, stackCoordY = 0.0D, stackCoordZ = 0.0D;

      switch (hitOrientation) {
         case 0:
            stackCoordX = source.xCoord + 0.5D;
            stackCoordY = source.yCoord + 0.5D;
            stackCoordZ = source.zCoord - 0.25D;
            break;
         case 1:
            stackCoordX = source.xCoord + 1.25D;
            stackCoordY = source.yCoord + 0.5D;
            stackCoordZ = source.zCoord + 0.5D;
            break;
         case 2:
            stackCoordX = source.xCoord + 0.5D;
            stackCoordY = source.yCoord + 0.5D;
            stackCoordZ = source.zCoord + 1.25D;
            break;
         case 3:
            stackCoordX = source.xCoord - 0.25D;
            stackCoordY = source.yCoord + 0.5D;
            stackCoordZ = source.zCoord + 0.5D;
            break;
      }

      EntityItem droppedEntity = new EntityItem(source.getWorldObj(), stackCoordX, stackCoordY, stackCoordZ, stack);

      if (player != null) {
         Vec3 motion = Vec3.createVectorHelper(player.posX - stackCoordX, player.posY - stackCoordY, player.posZ - stackCoordZ);
         motion.normalize();
         droppedEntity.motionX = motion.xCoord;
         droppedEntity.motionY = motion.yCoord;
         droppedEntity.motionZ = motion.zCoord;
         double offset = 0.25D;
         droppedEntity.moveEntity(motion.xCoord * offset, motion.yCoord * offset, motion.zCoord * offset);
      }

      droppedEntity.motionX *= speedfactor;
      droppedEntity.motionY *= speedfactor;
      droppedEntity.motionZ *= speedfactor;

      source.getWorldObj().spawnEntityInWorld(droppedEntity);
   }

}
