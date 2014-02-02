package mcp.mobius.betterbarrels;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class RecipeHandler {

	public static RecipeHandler _instance = new RecipeHandler();
	private RecipeHandler(){}
	public static RecipeHandler instance() { return RecipeHandler._instance; }	
	
	public void registerOres(){
		OreDictionary.registerOre("ingotIron",  Item.ingotIron);
		OreDictionary.registerOre("ingotGold",  Item.ingotGold);
		OreDictionary.registerOre("slimeball",  Item.slimeBall);
		OreDictionary.registerOre("gemDiamond", Item.diamond);
		OreDictionary.registerOre("gemEmerald", Item.emerald);
		OreDictionary.registerOre("chestWood",  Block.chest);
		OreDictionary.registerOre("stickWood",  Item.stick);
		OreDictionary.registerOre("obsidian",   Block.obsidian);
		OreDictionary.registerOre("whiteStone", Block.whiteStone);
	}
	
	public void registerRecipes(){
		
		//for (int i = 0; i < BetterBarrels.materialList.length; i++)
		for (int i = 0; i < Math.min(7, BetterBarrels.materialList.length); i++)
			this.addStructuralUpgrade(i, BetterBarrels.materialList[i]);
		
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
		
		if (BetterBarrels.diamondDollyActive){
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BetterBarrels.itemMoverDiamond,1,0), new Object[] 
					{"   ", " P ", "XXX",
			     Character.valueOf('X'), "gemDiamond", 
				 Character.valueOf('P'), BetterBarrels.itemMover}));
		}
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BetterBarrels.itemUpgradeSide, 4, 0), new Object[]
				{" P ","PXP", " P ", 
			 Character.valueOf('P'), Item.paper, 
			 Character.valueOf('X'), "slimeball"}));		
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BetterBarrels.itemHammer, 1, 0), new Object[]
				{"III","ISI", " S ", 
			 Character.valueOf('I'), "ingotIron", 
			 Character.valueOf('S'), "stickWood"}));	
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BetterBarrels.itemTuningFork, 1, 0), new Object[]
				{" P "," EP", "P  ", 
			Character.valueOf('P'), "ingotIron",
			Character.valueOf('E'), Item.enderPearl
				}));		
	}
	
	private void addStructuralUpgrade(int level, String variableComponent){
		String type     = variableComponent.split("\\.")[0];
		String compoStr = variableComponent.split("\\.")[1];
		this.addStructuralUpgrade_(level, compoStr);
		
		/*
		if (type.equals("Ore"))
			this.addStructuralUpgrade_(level, compoStr);

		if (type.equals("Block")){
			try {
				Block compo = (Block)Class.forName("net.minecraft.block.Block").getField(compoStr).get(null);
				this.addStructuralUpgrade_(level, compo);
			} catch (Exception e) {
				BetterBarrels.log.severe("Error while trying to register recipe with material " + variableComponent);
			}
		}

		if (type.equals("Item")){
			try {
				Item compo = (Item)Class.forName("net.minecraft.item.Item").getField(compoStr).get(null);
				this.addStructuralUpgrade_(level, compo);
			} catch (Exception e) {
				BetterBarrels.log.severe("Error while trying to register recipe with material " + variableComponent);
			}
		}
		*/		
		
	}
	
	private void addStructuralUpgrade_(int level, Item variableComponent){
		GameRegistry.addRecipe(new ItemStack(BetterBarrels.itemUpgradeStructural,1,level), new Object[] 
				{"PBP", "B B", "PBP",
				'P', Block.fence, 
				'B', variableComponent});		
	}

	private void addStructuralUpgrade_(int level, Block variableComponent){
		GameRegistry.addRecipe(new ItemStack(BetterBarrels.itemUpgradeStructural,1,level), new Object[] 
				{"PBP", "B B", "PBP",
				'P', Block.fence, 
				'B', new ItemStack(variableComponent,1)});		
	}	
	
	private void addStructuralUpgrade_(int level, String variableComponent){
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
				'B', new ItemStack(variableComponent,1)});		
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
				Character.valueOf('B'), new ItemStack(variableComponent,1)}));		
	}	
	
	private void addSideUpgrade(int meta, String variableComponent){
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BetterBarrels.itemUpgradeSide,4,meta), new Object[] 
				{" P ", "PBP", " P ",
				Character.valueOf('P'), "plankWood",
				Character.valueOf('B'), variableComponent}));		
	}	
}
