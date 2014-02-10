package mcp.mobius.betterbarrels.common.items.upgrades;

import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public enum UpgradeCore {
   STORAGE(Type.STORAGE, 1),
   ENDER(Type.ENDER, 2),
   REDSTONE(Type.REDSTONE, 1),
   HOPPER(Type.HOPPER, 1),
   STORAGE3(Type.STORAGE, 3),
   STORAGE9(Type.STORAGE, 9),
   STORAGE27(Type.STORAGE, 27),
   VOID(Type.VOID, 2);

   public static enum Type {
      STORAGE, ENDER, REDSTONE, HOPPER, VOID
   }

   public final Type type;
   public final int slotsUsed;
   public final String translationKey;

   @SideOnly(Side.CLIENT)
   public IIcon icon;

   private UpgradeCore(final Type type, final int slots) {
      this.type = type;
      this.slotsUsed = slots;
      translationKey = "item.upgrade.core." + this.name().toLowerCase();
   }

   @SideOnly(Side.CLIENT)
   public String description() {
      String key = "text.jabba.ubgrade.core." + this.type.name().toLowerCase();

      if (this.type == Type.STORAGE) {
         return StatCollector.translateToLocalFormatted(key, this.slotsUsed * 64);
      } else {
         return StatCollector.translateToLocal(key);
      }
   }
}
