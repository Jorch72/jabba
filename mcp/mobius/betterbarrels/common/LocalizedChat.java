package mcp.mobius.betterbarrels.common;

public enum LocalizedChat {
   BONK("text.jabba.bonk"),
   DOWNGRADE("text.jabba.downgrade"),
   COREUPGRADE_EXISTS("text.jabba.coreupgrade_exists"),
   UPGRADE_EXISTS("text.jabba.upgrade_exists"),
   UPGRADE_REMOVE("text.jabba.upgrade_remove"),
   UPGRADE_INSUFFICIENT("text.jabba.upgrade_insufficient"),
   UPGRADE_REQUIRED("text.jabba.upgrade_required"),
   FACADE_REDSTONE("text.jabba.facade_redstone"),
   FACADE_HOPPER("text.jabba.facade_hopper"),
   STACK_REMOVE("text.jabba.stack_remove"),
   BSPACE_PREVENT("text.jabba.bspace_prevent"),
   BSPACE_REMOVE("text.jabba.bspace_remove"),
   BSPACE_NOREACT("text.jabba.bspace_noreact"),
   BSPACE_CONTENT("text.jabba.bspace_content"),
   BSAPCE_STRUCTURE("text.jabba.bsapce_structure"),
   BSPACE_FORK_RESONATING("text.jabba.bspace_fork_resonating"),
   BSPACE_FORK_LOST("text.jabba.bspace_fork_lost"),
   BSPACE_RESONATING("text.jabba.bspace_resonating"),
   HAMMER_NORMAL("text.jabba.hammer_normal"),
   HAMMER_BSPACE("text.jabba.hammer_bspace"),
   HAMMER_REDSTONE("text.jabba.hammer_redstone"),
   HAMMER_HOPPER("text.jabba.hammer_hopper"),
   HAMMER_STORAGE("text.jabba.hammer_storage"),
   HAMMER_STRUCTURAL("text.jabba.hammer_structural"),
   HAMMER_VOID("text.jabba.hammer_void");

   public final String localizationKey;

   private LocalizedChat(final String key) {
      this.localizationKey = key;
   }
}
