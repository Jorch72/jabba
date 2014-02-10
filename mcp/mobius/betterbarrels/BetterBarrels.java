package mcp.mobius.betterbarrels;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import mcp.mobius.betterbarrels.bspace.BSpaceStorageHandler;
import mcp.mobius.betterbarrels.common.BaseProxy;
import mcp.mobius.betterbarrels.common.blocks.BlockBarrel;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import mcp.mobius.betterbarrels.common.items.ItemBarrelHammer;
import mcp.mobius.betterbarrels.common.items.ItemTuningFork;
import mcp.mobius.betterbarrels.common.items.dolly.ItemBarrelMover;
import mcp.mobius.betterbarrels.common.items.dolly.ItemDiamondMover;
import mcp.mobius.betterbarrels.common.items.upgrades.ItemUpgradeCore;
import mcp.mobius.betterbarrels.common.items.upgrades.ItemUpgradeSide;
import mcp.mobius.betterbarrels.common.items.upgrades.ItemUpgradeStructural;
import mcp.mobius.betterbarrels.network.BarrelPacketHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid=BetterBarrels.modid, name="JABBA", version="1.1.1", dependencies="after:Waila;after:NotEnoughItems")
@NetworkMod(channels = {"JABBA"}, clientSideRequired=true, serverSideRequired=false, packetHandler=BarrelPacketHandler.class)

public class BetterBarrels {

	public static final String modid = "JABBA";
	
	public static Logger log = Logger.getLogger("BetterBarrels");	
	
	
    // The instance of your mod that Forge uses.
	@Instance("ProfMobius_BetterBarrels")
	public static BetterBarrels instance;
	
	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide="mcp.mobius.betterbarrels.client.ClientProxy", serverSide="mcp.mobius.betterbarrels.common.BaseProxy")
	public static BaseProxy proxy;

	/* CONFIG PARAMS */
	private static Configuration config = null;
	
	public static int barrelID = -1;
	public static int miniBarrelID = -1;
	public static int barrelShelfID = -1;
	
	private static int itemUpgradeStructuralID = -1;
	private static int itemUpgradeCoreID = -1;	
	private static int itemUpgradeSideID = -1;
	private static int itemMoverID = -1;
	private static int itemMoverDiamondID = -1;
	private static int itemTuningForkID = -1;	
	private static int itemLockingPlanksID = -1;
	private static int itemHammerID = -1;
	
	public static boolean  fullBarrelTexture  = true;
	public static boolean  highRezTexture     = true;
	public static boolean  showUpgradeSymbols = true;
	public static boolean  diamondDollyActive = true;	
	public static String[] materialList       = new String[]{"Ore.plankWood", "Ore.ingotIron", "Ore.ingotGold", "Ore.gemDiamond", "Ore.obsidian", "Ore.whiteStone", "Ore.gemEmerald"};
	
	public static Block blockBarrel      = null;
	public static Block blockMiniBarrel  = null;
	public static Block blockBarrelShelf = null;	
	public static Item itemUpgradeStructural = null;
	public static Item itemUpgradeCore   = null;
	public static Item itemUpgradeSide   = null;
	public static Item itemMover         = null;
	public static Item itemMoverDiamond  = null;
	public static Item itemTuningFork    = null;
	public static Item itemLockingPlanks = null;
	public static Item itemHammer = null;
	
	public static int blockBarrelRendererID = -1;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
		
		try {
			config.load();
			barrelID     = config.get("block",  "BetterBarrel",    3500).getInt();
			miniBarrelID = config.get("block",  "MiniBarrel",      3501).getInt();
			barrelShelfID= config.get("block",  "BarrelShelf",     3502).getInt();
			
			itemUpgradeStructuralID = config.get("item",   "UpgradeCapacity", 3501).getInt();
			itemUpgradeSideID       = config.get("item",   "Sticker",         3502).getInt();
			itemMoverID         = config.get("item",   "Mover",           3503).getInt();
			itemTuningForkID    = config.get("item",   "TuningFork",      3504).getInt();
			itemLockingPlanksID = config.get("item",   "LockingPlanks",   3506).getInt();	
			itemUpgradeCoreID   = config.get("item",   "UpgradeCore",     3507).getInt();
			itemHammerID        = config.get("item",   "Hammer",          3508).getInt();
			itemMoverDiamondID  = config.get("item",   "DiamondMover",    3509).getInt();				
			
			diamondDollyActive  = config.get(Configuration.CATEGORY_GENERAL, "diamondDollyActive", true).getBoolean(true);
			materialList        = config.get(Configuration.CATEGORY_GENERAL, "materialList", materialList).getStringList();
			
			
			//fullBarrelTexture  = config.get(Configuration.CATEGORY_GENERAL, "fullBarrelTexture", true).getBoolean(true);
			//highRezTexture     = config.get(Configuration.CATEGORY_GENERAL, "highRezTexture", false).getBoolean(false);
			//showUpgradeSymbols = config.get(Configuration.CATEGORY_GENERAL, "showUpgradeSymbols", false).getBoolean(false);
			
		} catch (Exception e) {
			FMLLog.log(Level.SEVERE, e, "BlockBarrel has a problem loading it's configuration");
			FMLLog.severe(e.getMessage());	
			
		} finally {
		   if (config.hasChanged())
		      config.save();
		}
		
		RecipeHandler.instance().registerOres();
		proxy.registerEventHandler();	
		
		//log.setLevel(Level.FINEST);
      blockBarrel           = new BlockBarrel(barrelID);
      itemUpgradeStructural = new ItemUpgradeStructural(itemUpgradeStructuralID);
      itemUpgradeCore       = new ItemUpgradeCore(itemUpgradeCoreID);
      itemUpgradeSide       = new ItemUpgradeSide(itemUpgradeSideID);
      itemMover             = new ItemBarrelMover(itemMoverID);
      itemMoverDiamond      = new ItemDiamondMover(itemMoverDiamondID);
      itemHammer            = new ItemBarrelHammer(itemHammerID);
      itemTuningFork        = new ItemTuningFork(itemTuningForkID);
      
      GameRegistry.registerBlock(blockBarrel, "barrel");
      //GameRegistry.registerBlock(blockMiniBarrel);
      //GameRegistry.registerBlock(blockBarrelShelf);    
      //GameRegistry.registerTileEntity(TileEntityMiniBarrel.class,  "TileEntityMiniBarrel");
      //GameRegistry.registerTileEntity(TileEntityBarrelShelf.class, "TileEntityBarrelShelf");

      GameRegistry.registerItem(itemUpgradeStructural, "upgradeStructural");
      GameRegistry.registerItem(itemUpgradeCore, "upgradeCore");
      GameRegistry.registerItem(itemUpgradeSide, "upgradeSide");
      GameRegistry.registerItem(itemMover, "mover");
      GameRegistry.registerItem(itemMoverDiamond, "moverDiamond");
      GameRegistry.registerItem(itemHammer, "hammer");
      GameRegistry.registerItem(itemTuningFork, "tuningFork");
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event) {
		RecipeHandler.instance().registerRecipes();

		GameRegistry.registerTileEntity(TileEntityBarrel.class, "TileEntityBarrel");

		proxy.registerRenderers();

		FMLInterModComms.sendMessage("Waila", "register", "mcp.mobius.betterbarrels.BBWailaProvider.callbackRegister");        
	}
 
	
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit();
	}	

	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event) {
		BSpaceStorageHandler.instance().writeToFile();
	}
}
