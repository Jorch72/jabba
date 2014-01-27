package mcp.mobius.betterbarrels;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class RecipeHandler {

	public static RecipeHandler _instance = new RecipeHandler();
	private RecipeHandler(){}
	public static RecipeHandler instance() { return RecipeHandler._instance; }	
	
	public void registerRecipes(){
		this.addStructuralUpgrade(0, "plankWood");
		this.addStructuralUpgrade(1, "ingotIron");
		this.addStructuralUpgrade(2, "ingotGold");
		this.addStructuralUpgrade(3, "gemDiamond");
		this.addStructuralUpgrade(4, Block.obsidian);
		this.addStructuralUpgrade(5, Block.whiteStone);
		this.addStructuralUpgrade(6, "gemEmerald");		
		
		this.addCoreUpgrade(0, BetterBarrels.blockBarrel);
		this.addCoreUpgrade(1, Block.enderChest);
		this.addCoreUpgrade(2, Block.blockRedstone);
		this.addCoreUpgrade(3, Block.hopperBlock);
		
		this.addSideUpgrade(1, Block.hopperBlock);
		this.addSideUpgrade(2, Item.redstone);
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BetterBarrels.blockBarrel), new Object[]
				 {"W-W", "WCW", "WWW", 
			     Character.valueOf('C'), "chestWood", 
				 Character.valueOf('W'), "logWood",
				 Character.valueOf('-'), "slabWood"}));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BetterBarrels.itemMover,1,0), new Object[] 
				{"  X", " PX", "XXX",
		     Character.valueOf('X'), "ingotIron", 
			 Character.valueOf('P'), "plankWood"}));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BetterBarrels.itemUpgradeSide, 4, 0), new Object[]
				{" P ","PXP", " P ", 
			 Character.valueOf('P'), Item.paper, 
			 Character.valueOf('X'), "slimeball"}));		
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BetterBarrels.itemHammer, 1, 0), new Object[]
				{"III","ISI", " S ", 
			 Character.valueOf('I'), "ingotIron", 
			 Character.valueOf('S'), Item.stick}));	
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BetterBarrels.itemTuningFork, 1, 0), new Object[]
				{" P "," EP", "P  ", 
				'P', "ingotIron",
				'E', Item.enderPearl
				}));		
	}
	
	private void addStructuralUpgrade(int level, Item variableComponent){
		GameRegistry.addRecipe(new ItemStack(BetterBarrels.itemUpgradeStructural,1,level), new Object[] 
				{"PBP", "B B", "PBP",
				'P', Block.fence, 
				'B', variableComponent});		
	}

	private void addStructuralUpgrade(int level, Block variableComponent){
		GameRegistry.addRecipe(new ItemStack(BetterBarrels.itemUpgradeStructural,1,level), new Object[] 
				{"PBP", "B B", "PBP",
				'P', Block.fence, 
				'B', new ItemStack(variableComponent,0)});		
	}	
	
	private void addStructuralUpgrade(int level, String variableComponent){
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BetterBarrels.itemUpgradeStructural,1,level), new Object[] 
				{"PBP", "B B", "PBP",
				Character.valueOf('P'), Block.fence, 
				Character.valueOf('B'), variableComponent}));		
	}	
	
	private void addCoreUpgrade(int meta, Item variableComponent){
		GameRegistry.addRecipe(new ItemStack(BetterBarrels.itemUpgradeCore,1,meta), new Object[] 
				{" P ", " B ", " P ",
				'P', Block.pistonBase, 
				'B', variableComponent});		
	}

	private void addCoreUpgrade(int meta, Block variableComponent){
		GameRegistry.addRecipe(new ItemStack(BetterBarrels.itemUpgradeCore,1,meta), new Object[] 
				{" P ", " B ", " P ",
				'P', Block.pistonBase, 
				'B', new ItemStack(variableComponent,0)});		
	}	
	
	private void addCoreUpgrade(int meta, String variableComponent){
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BetterBarrels.itemUpgradeCore,1,meta), new Object[] 
				{" P ", " B ", " P ",
				Character.valueOf('P'), Block.pistonBase,
				Character.valueOf('B'), variableComponent}));		
	}	
	
	private void addSideUpgrade(int meta, Item variableComponent){
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BetterBarrels.itemUpgradeSide,4,meta), new Object[] 
				{" P ", "PBP", " P ",
				Character.valueOf('P'), "plankWood", 
				Character.valueOf('B'), variableComponent}));		
	}

	private void addSideUpgrade(int meta, Block variableComponent){
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BetterBarrels.itemUpgradeSide,4,meta), new Object[] 
				{" P ", "PBP", " P ",
				Character.valueOf('P'), "plankWood",
				Character.valueOf('B'), new ItemStack(variableComponent,0)}));		
	}	
	
	private void addSideUpgrade(int meta, String variableComponent){
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BetterBarrels.itemUpgradeSide,4,meta), new Object[] 
				{" P ", "PBP", " P ",
				Character.valueOf('P'), "plankWood",
				Character.valueOf('B'), variableComponent}));		
	}	
}
