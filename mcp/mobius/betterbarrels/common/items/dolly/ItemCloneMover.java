package mcp.mobius.betterbarrels.common.items.dolly;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Facing;
import net.minecraft.world.World;

public class ItemCloneMover extends ItemBarrelMover {
   /*
    * Admin Clone Dolly: Picks up any valid TE(As determined by the base dolly code) and placing it down will place a copy, leaving the stored info still on the dolly
    * 
    * Pickup = Shift Right-CLick TE
    *  Clone = Right-CLick
    *  Clear = Shift Right-Click while TE is stored
    * 
    * Easter-Egg: Spawns a sheep named "Dolly" when no TE stored and right clicked on ground
    * 
    * Things of concern: BSpace barrels, will have to regenerate the link: Feb 08 09:46:46 <ProfMobius> Be VERY careful with the clone thing Feb 08 09:47:30 <ProfMobius> I'm keeping a fixed index for every barrel. It is stored in the BSpaceHandler and you will have to reset it when you are creating
    * the clone Feb 08 09:47:49 <ProfMobius> otherwise, you are going to trigger really weird behaviors with ender barrels and surely break things Feb 08 09:48:31 <taelnia> i saw that index, and thank you for the tip, I wasn't quite sure yet how to handle linked barrels Feb 08 09:49:07 <taelnia>
    * void upgrade appears to be working fine though, but I only did limited testing on it Feb 08 09:49:22 <ProfMobius> Setting it to -1 in the NBT when storing the barrel in the dolly should be enough to regenerate an index when you put it back down, but it does require checking Feb 08 09:49:39
    * <taelnia> noted, I'll attempt to be careful :)
    */
   NBTTagCompound savedTileTag;

   public ItemCloneMover(int id) {
      super(id);
      this.type = DollyType.CLONE;
   }

   @Override
   protected boolean canPickSpawners() {
      return true;
   }

   @Override
   public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {}

   @Override
   public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
      if (world.isRemote) {
         return false;
      }

      if (player.isSneaking() && (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("Container"))) {
         boolean ret = this.pickupContainer(stack, player, world, x, y, z);

         if (!ret) {
            EntitySheep dolly = new EntitySheep(world);
            dolly.setPosition(x + Facing.offsetsXForSide[side], y + Facing.offsetsYForSide[side], z + Facing.offsetsZForSide[side]);
            dolly.setCustomNameTag("Dolly");
            dolly.setHealth(20);
            dolly.setAlwaysRenderNameTag(true);
            world.spawnEntityInWorld(dolly);

            return true;
         } else {
            savedTileTag = stack.getTagCompound().getCompoundTag("Container");
         }

         return ret;
      }

      if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Container")) {
         if (player.isSneaking()) {
            stack.getTagCompound().removeTag("Container");

            return true;
         }
         boolean ret = this.placeContainer(stack, player, world, x, y, z, side);

         if (!stack.getTagCompound().hasKey("Container")) {
            stack.getTagCompound().setCompoundTag("Container", savedTileTag);
         }

         return ret;
      }

      return false;
   }
}
