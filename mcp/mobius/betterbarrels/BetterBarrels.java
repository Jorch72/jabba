package mcp.mobius.betterbarrels;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import mcp.mobius.betterbarrels.common.BaseProxy;
import mcp.mobius.betterbarrels.common.blocks.BlockBarrel;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import mcp.mobius.betterbarrels.common.items.ItemBarrelHammer;
import mcp.mobius.betterbarrels.common.items.ItemBarrelMover;
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
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid=BetterBarrels.modid, name="JABBA", version="1.1.0", dependencies="after:Waila;after:NotEnoughItems")
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
	private static int itemTuningForkID = -1;	
	private static int itemBSpaceUpgID = -1;
	private static int itemLockingPlanksID = -1;
	private static int itemHammerID = -1;
	
	public static boolean fullBarrelTexture  = true;
	public static boolean highRezTexture     = true;
	public static boolean showUpgradeSymbols = true;
	
	
	public static Block blockBarrel      = null;
	public static Block blockMiniBarrel  = null;
	public static Block blockBarrelShelf = null;	
	public static Item itemUpgradeStructural = null;
	public static Item itemUpgradeCore   = null;
	public static Item itemUpgradeSide   = null;
	public static Item itemMover         = null;
	public static Item itemTuningFork    = null;
	public static Item itemBSpaceUpg     = null;	
	public static Item itemLockingPlanks = null;
	public static Item itemHammer = null;
	
	public static int blockBarrelRendererID = -1;
	
	public static int RENDER_SHELF = -1;
	
	public static String moverName = "Dolly";
	
	//public static BSpaceStorageHandler storageHandler = new BSpaceStorageHandler();
	
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
			itemBSpaceUpgID     = config.get("item",   "BSpaceUpg",       3505).getInt();
			itemLockingPlanksID = config.get("item",   "LockingPlanks",   3506).getInt();	
			itemUpgradeCoreID   = config.get("item",   "UpgradeCore",     3507).getInt();
			itemHammerID        = config.get("item",   "Hammer",          3508).getInt();			
			
			fullBarrelTexture  = config.get(Configuration.CATEGORY_GENERAL, "fullBarrelTexture", true).getBoolean(true);
			highRezTexture     = config.get(Configuration.CATEGORY_GENERAL, "highRezTexture", false).getBoolean(false);
			showUpgradeSymbols = config.get(Configuration.CATEGORY_GENERAL, "showUpgradeSymbols", false).getBoolean(false);
			
		} catch (Exception e) {
			FMLLog.log(Level.SEVERE, e, "BlockBarrel has a problem loading it's configuration");
			FMLLog.severe(e.getMessage());	
			
		} finally {
			config.save();
		}
		
		OreDictionary.registerOre("ingotIron",  Item.ingotIron);
		OreDictionary.registerOre("ingotGold",  Item.ingotGold);
		OreDictionary.registerOre("slimeball",  Item.slimeBall);
		OreDictionary.registerOre("gemDiamond", Item.diamond);
		OreDictionary.registerOre("gemEmerald", Item.emerald);
		OreDictionary.registerOre("chestWood",  Block.chest);
		
		//PlasmaTexture.precomputeTables();
		//PlasmaTexture.generateTexture();
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event) {
		blockBarrel           = new BlockBarrel(barrelID);
		itemUpgradeStructural = new ItemUpgradeStructural(itemUpgradeStructuralID);
		itemUpgradeCore       = new ItemUpgradeCore(itemUpgradeCoreID);
		itemUpgradeSide       = new ItemUpgradeSide(itemUpgradeSideID);
		itemMover             = new ItemBarrelMover(itemMoverID);
		itemHammer            = new ItemBarrelHammer(itemHammerID);
		
		LanguageRegistry.addName(blockBarrel, "Better Barrel");
		LanguageRegistry.addName(new ItemStack(itemMover,0,0),    moverName);
		LanguageRegistry.addName(new ItemStack(itemHammer,0,0),   "Barrel Hammer");

		//blockMiniBarrel     = new BlockMiniBarrel(miniBarrelID);
		//blockBarrelShelf    = new BlockBarrelShelf(barrelShelfID);		
		//itemTuningFork      = new ItemTuningFork(itemTuningForkID);
		//itemBSpaceUpg       = new ItemBSpaceInterface(itemBSpaceUpgID);
		
		//LanguageRegistry.addName(blockMiniBarrel, "Mini Barrel (WIP)");
		//LanguageRegistry.addName(blockBarrelShelf, "Barrel shelf (WIP)");
		//LanguageRegistry.addName(itemTuningFork,   "B-Space Tuning Fork (WIP)");
		//LanguageRegistry.addName(itemBSpaceUpg,   "B-Space Interface (WIP)");
		
		for(int i=0; i<ItemUpgradeStructural.upgradeNames.length; i++){
			ItemStack upgrade = new ItemStack(itemUpgradeStructural, 1, i);
			LanguageRegistry.addName(upgrade, ItemUpgradeStructural.upgradeNames[i]);
		}

		for(int i=0; i<ItemUpgradeCore.upgradeNames.length; i++){
			ItemStack upgrade = new ItemStack(itemUpgradeCore, 1, i);
			LanguageRegistry.addName(upgrade, ItemUpgradeCore.upgradeNames[i]);
		}		
		
		for(int i=0; i<ItemUpgradeSide.upgradeNames.length; i++){
			ItemStack upgrade = new ItemStack(itemUpgradeSide, 1, i);
			LanguageRegistry.addName(upgrade, ItemUpgradeSide.upgradeNames[i]);
		}			
		
		GameRegistry.registerBlock(blockBarrel, "jabba.blockbarrel");
		GameRegistry.registerTileEntity(TileEntityBarrel.class, "TileEntityBarrel");

		//GameRegistry.registerBlock(blockMiniBarrel);
		//GameRegistry.registerBlock(blockBarrelShelf);		
		//GameRegistry.registerTileEntity(TileEntityMiniBarrel.class,  "TileEntityMiniBarrel");
		//GameRegistry.registerTileEntity(TileEntityBarrelShelf.class, "TileEntityBarrelShelf");
		proxy.registerRenderers();
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockBarrel), new Object[]
				 {"W-W", "WCW", "WWW", 
			     Character.valueOf('C'), "chestWood", 
				 Character.valueOf('W'), "logWood",
				 Character.valueOf('-'), "slabWood"}));

		this.addStructuralUpgrade(0, "plankWood");
		this.addStructuralUpgrade(1, "ingotIron");
		this.addStructuralUpgrade(2, "ingotGold");
		this.addStructuralUpgrade(3, "gemDiamond");
		this.addStructuralUpgrade(4, Block.obsidian);
		this.addStructuralUpgrade(5, Block.whiteStone);
		this.addStructuralUpgrade(6, "gemEmerald");		
		
		this.addCoreUpgrade(0, blockBarrel);
		this.addCoreUpgrade(1, Block.enderChest);
		this.addCoreUpgrade(2, Block.blockRedstone);
		this.addCoreUpgrade(3, Block.hopperBlock);		
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemMover,1,0), new Object[] 
				{"  X", " PX", "XXX",
		     Character.valueOf('X'), "ingotIron", 
			 Character.valueOf('P'), "plankWood"}));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemUpgradeSide, 4, 0), new Object[]
				{" P ","PXP", " P ", 
			 Character.valueOf('P'), Item.paper, 
			 Character.valueOf('X'), "slimeball"}));

		//GameRegistry.addRecipe(new ItemStack(itemTuningFork, 1, 0), new Object[]
		//		{" P "," PP", "P  ", 'P', Item.ingotIron});		
		
		proxy.registerEventHandler();
		
        //TickRegistry.registerTickHandler(new BarrelServerTickHandler(), Side.SERVER);
        FMLInterModComms.sendMessage("Waila", "register", "mcp.mobius.betterbarrels.BBWailaProvider.callbackRegister");        
	}
 
	private void addStructuralUpgrade(int level, Item variableComponent){
		GameRegistry.addRecipe(new ItemStack(itemUpgradeStructural,1,level), new Object[] 
				{"PBP", "B B", "PBP",
				'P', Block.fence, 
				'B', variableComponent});		
	}

	private void addStructuralUpgrade(int level, Block variableComponent){
		GameRegistry.addRecipe(new ItemStack(itemUpgradeStructural,1,level), new Object[] 
				{"PBP", "B B", "PBP",
				'P', Block.fence, 
				'B', new ItemStack(variableComponent,0)});		
	}	
	
	private void addStructuralUpgrade(int level, String variableComponent){
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemUpgradeStructural,1,level), new Object[] 
				{"PBP", "B B", "PBP",
				Character.valueOf('P'), Block.fence, 
				Character.valueOf('B'), variableComponent}));		
	}	
	
	private void addCoreUpgrade(int meta, Item variableComponent){
		GameRegistry.addRecipe(new ItemStack(itemUpgradeCore,1,meta), new Object[] 
				{" P ", " B ", " P ",
				'P', Block.pistonBase, 
				'B', variableComponent});		
	}

	private void addCoreUpgrade(int meta, Block variableComponent){
		GameRegistry.addRecipe(new ItemStack(itemUpgradeCore,1,meta), new Object[] 
				{" P ", " B ", " P ",
				'P', Block.pistonBase, 
				'B', new ItemStack(variableComponent,0)});		
	}	
	
	private void addCoreUpgrade(int meta, String variableComponent){
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemUpgradeCore,1,meta), new Object[] 
				{" P ", " B ", " P ",
				Character.valueOf('P'), Block.pistonBase,
				Character.valueOf('B'), variableComponent}));		
	}	
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (Loader.isModLoaded("NotEnoughItems"))
			BBNeiTooltip.registerHandler();;
	}	

	//@ServerStopping
	//public void serverStopping(FMLServerStoppingEvent var1) {
	//	SaveHandler.saveData();
	//}
}