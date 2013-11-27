package mcp.mobius.betterbarrels.common.items;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.betterbarrels.mod_BetterBarrels;
import mcp.mobius.betterbarrels.common.BaseProxy;
import mcp.mobius.betterbarrels.common.TileEntityBarrel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.IShearable;

public class ItemBarrelMover extends Item {
	private static Icon   text_empty = null;
	private static Icon   text_filled = null;
    
    private static ArrayList<Class>  classExtensions      = new ArrayList<Class>();
    private static ArrayList<String> classExtensionsNames = new ArrayList<String>();
    
    static {
    	classExtensionsNames.add("cpw.mods.ironchest.TileEntityIronChest");
    	classExtensionsNames.add("buildcraft.energy.TileEngine");
    	classExtensionsNames.add("buildcraft.factory.TileTank");
    	classExtensionsNames.add("ic2.api.energy.tile.IEnergySink");
    	classExtensionsNames.add("ic2.api.energy.tile.IEnergySource");
    	classExtensionsNames.add("mods.railcraft.common.blocks.machine.beta.TileEngine");
    	classExtensionsNames.add("forestry.core.gadgets.Engine");
    	classExtensionsNames.add("bluedart.tile.TileEntityForceEngine");
    	classExtensionsNames.add("thermalexpansion.block.engine.TileEngineRoot");
    	//classExtensionsNames.add("factorization.common.TileEntityBarrel");
    	classExtensionsNames.add("thermalexpansion.block.machine.TileMachineRoot");
    	
    	for (String s : classExtensionsNames){
    		try {
    			classExtensions.add(Class.forName(s));
    		}
    		catch (ClassNotFoundException e){
    			classExtensions.add(null);
    		}
    	}
    }
    
	public ItemBarrelMover(int id){
		super(id);
        this.setMaxStackSize(1); 
        this.setHasSubtypes(true);
        this.setMaxDamage(0);     
        this.setUnlocalizedName("Dolly");
	}

    @Override	
    public String getItemDisplayName(ItemStack par1ItemStack)
    {    	return "Dolly";    }

    @Override    
    public String getUnlocalizedName()
    {    	return "Dolly";    }

    @Override    
    public String getUnlocalizedName(ItemStack par1ItemStack)
    {    	return "Dolly";    }

    @Override    
    public void registerIcons(IconRegister par1IconRegister)
    {
    	this.itemIcon    = par1IconRegister.registerIcon(mod_BetterBarrels.modid + ":" + "dolly_empty");
    	this.text_empty  = this.itemIcon;
    	this.text_filled = par1IconRegister.registerIcon(mod_BetterBarrels.modid + ":" + "dolly_filled");
    }    
    
    @Override
    public Icon getIconFromDamage(int dmg)
    {
        if (dmg == 0)
        	return this.text_empty;
        else
        	return this.text_filled;
    }    
    
	@Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
		if (world.isRemote){return false;}
		
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("Container")){
			return this.pickupContainer(stack, player, world, x, y, z);
		}
		
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Container")){
			return this.placeContainer(stack, player, world, x, y, z, side);
		}
		
        return false;
    }
	
	private boolean placeContainer(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side){
		NBTTagCompound nbtStack = stack.getTagCompound();
		int blockID        = nbtStack.getCompoundTag("Container").getInteger("ID");
		int blockMeta      = nbtStack.getCompoundTag("Container").getInteger("Meta");
		String TEClassName = nbtStack.getCompoundTag("Container").getString("TEClass");
		NBTTagCompound nbtContainer = nbtStack.getCompoundTag("Container").getCompoundTag("NBT");
		
		ForgeDirection targSide = ForgeDirection.getOrientation(side);			
		//if (world.isBlockSolidOnSide(x, y, z, targSide)){return false;}

		int targX = x;
		int targY = y;
		int targZ = z;
		
		int targID = world.getBlockId(targX, targY, targZ);
		
        if (targID == Block.snow.blockID)
            targSide = ForgeDirection.UP;
		
		if (targID != Block.vine.blockID && targID != Block.tallGrass.blockID && targID != Block.deadBush.blockID
                && (Block.blocksList[targID] == null || !Block.blocksList[targID].isBlockReplaceable(world, targX, targY, targZ))){
			if (targSide.equals(ForgeDirection.NORTH))
				targZ -= 1;
			if (targSide.equals(ForgeDirection.SOUTH))
				targZ += 1;			
			if (targSide.equals(ForgeDirection.WEST))
				targX -= 1;
			if (targSide.equals(ForgeDirection.EAST))
				targX += 1;			
			if (targSide.equals(ForgeDirection.UP))
				targY += 1;			
			if (targSide.equals(ForgeDirection.DOWN))
				targY += 1;
		}
		
		if(!(world.canPlaceEntityOnSide(blockID, targX, targY, targZ, false, side, (Entity)null, stack))){return false;}
		
		nbtContainer.setInteger("x", targX);
		nbtContainer.setInteger("y", targY);
		nbtContainer.setInteger("z", targZ);

		/* Vanilla chest */
		if (TEClassName.contains("net.minecraft.tileentity.TileEntityChest"))
			blockMeta = this.getBarrelOrientationOnPlacement(player).ordinal(); 
		
		/* Buildcraft engines orientation correction */
		if (TEClassName.contains("buildcraft.energy.TileEngine") && nbtContainer.hasKey("orientation"))
			nbtContainer.setInteger("orientation", 1);		

		/* Railcraft engines orientation correction */
		if (TEClassName.contains("mods.railcraft.common.blocks.machine.beta") && nbtContainer.hasKey("direction"))
			nbtContainer.setByte("direction", (byte)1);		

		/* Forestry engines orientation correction */
		if (TEClassName.contains("forestry.energy.gadgets") && nbtContainer.hasKey("Orientation"))
			nbtContainer.setInteger("Orientation", 1);			
		
		/* Dartcraft engines orientation correction */
		if (TEClassName.contains("bluedart.tile.TileEntityForceEngine") && nbtContainer.hasKey("facing"))
			nbtContainer.setByte("facing", (byte)1);

		/* Thermal Expansion engines */
		if (TEClassName.contains("thermalexpansion.block.engine") && nbtContainer.hasKey("side.facing"))
			nbtContainer.setByte("side.facing", (byte)1);		
		
		/* Iron chest orientation correction */
		if (TEClassName.contains("cpw.mods.ironchest") && nbtContainer.hasKey("facing"))
			nbtContainer.setByte("facing", (byte)this.getBarrelOrientationOnPlacement(player).ordinal());		
		
		/* IC2 Orientation correction */
		if (TEClassName.contains("ic2.core.block") && nbtContainer.hasKey("facing"))
			nbtContainer.setShort("facing", (short)this.getBarrelOrientationOnPlacement(player).ordinal());
		
		/* Gregtech Orientation Correction */
		if (TEClassName.contains("gregtechmod") && nbtContainer.hasKey("mFacing"))
			nbtContainer.setShort("mFacing", (short)this.getBarrelOrientationOnPlacement(player).ordinal());		
		
		/* Factorization barrel */
		//if (TEClassName.contains("factorization.common.TileEntityBarrel") && nbtContainer.hasKey("facing"))
		//	nbtContainer.setByte("facing", (byte)this.getBarrelOrientationOnPlacement(player).ordinal());

		/* Thermal Expension */
		if (TEClassName.contains("thermalexpansion.block.machine") && nbtContainer.hasKey("side.facing")){
			ForgeDirection side_facing  = ForgeDirection.getOrientation(nbtContainer.getByte("side.facing"));
			ForgeDirection new_facing   = this.getBarrelOrientationOnPlacement(player);
			byte[] side_array_old       = nbtContainer.getByteArray("side.array");
			byte[] side_array_new       = side_array_old.clone();

			int rotations = 0;
			while (side_facing != new_facing){
				rotations += 1;
				side_facing = side_facing.getRotation(ForgeDirection.UP);
			}			
			
			for (int i = 2; i < 6; i++){
				ForgeDirection new_direction = ForgeDirection.getOrientation(i);
				for (int j = 0; j < rotations; j++)
					new_direction = new_direction.getRotation(ForgeDirection.DOWN);
				side_array_new[i] = side_array_old[new_direction.ordinal()];
			}
			
			nbtContainer.setByteArray("side.array", side_array_new);
			nbtContainer.setByte("side.facing", (byte)new_facing.ordinal());
		}
		
		
		/* Better barrel craziness */
		if (nbtContainer.getString("id").equals("TileEntityBarrel")){
			ForgeDirection newBarrelOrientation         = this.getBarrelOrientationOnPlacement(player);
			ForgeDirection oldBarrelOrientation         = this.convertOrientationFlagToForge(nbtContainer.getInteger("barrelOrigOrient")).get(0); 
			ArrayList<ForgeDirection> oldBarrelStickers = this.convertOrientationFlagToForge(nbtContainer.getInteger("barrelOrient"));
			
			int numberRotations = 0;
			while (newBarrelOrientation != oldBarrelOrientation){
				numberRotations += 1;
				oldBarrelOrientation = oldBarrelOrientation.getRotation(ForgeDirection.UP);
			}
			
			for (int i = 0; i < numberRotations; i++)
				for (int j = 0; j < oldBarrelStickers.size(); j++)
					oldBarrelStickers.set(j, oldBarrelStickers.get(j).getRotation(ForgeDirection.UP));
			
			nbtContainer.setInteger("barrelOrient", this.convertForgeToOrientationFlag(oldBarrelStickers));
			nbtContainer.setInteger("barrelOrigOrient", (1 << (newBarrelOrientation.ordinal() - 2)));
			blockMeta = this.convertForgeToOrientationFlag(oldBarrelStickers);
		}
		
		world.setBlock(targX, targY, targZ, blockID, blockMeta, 1 + 2);
		world.getBlockTileEntity(targX, targY, targZ).readFromNBT(nbtContainer);

		if (TEClassName.contains("net.minecraft.tileentity.TileEntityChest"))	
			world.setBlockMetadataWithNotify(targX, targY, targZ, blockMeta, 1 + 2);
		
		stack.setItemDamage(0);		
		stack.getTagCompound().removeTag("Container");
		try{ stack.getTagCompound().getCompoundTag("display").removeTag("Name");
		} catch (Exception e){}
		return true;
		
		
	}
	
	private boolean isTEMovable(TileEntity te){
		if (te instanceof TileEntityBarrel)
			return true;
		if (te instanceof TileEntityChest)
			return true;		
		for (Class c : classExtensions){
			if (c!=null && c.isInstance(te))
			return true;
		}
		return false;
	}
	
	private boolean pickupContainer(ItemStack stack, EntityPlayer player, World world, int x, int y, int z){
		int blockID            = world.getBlockId(x, y, z);
		int blockMeta          = world.getBlockMetadata(x, y, z);
		TileEntity containerTE = world.getBlockTileEntity(x, y, z);
		NBTTagCompound nbtContainer = new NBTTagCompound();
		NBTTagCompound nbtTarget    = new NBTTagCompound();
		
		if (!isTEMovable(containerTE))
			return false;
		
		if (containerTE instanceof TileEntityBarrel && !((TileEntityBarrel) containerTE).storage.canInteract(player.username))
			return false;
		
		containerTE.writeToNBT(nbtContainer);
		
		/*   */
		//System.out.printf("%s\n", nbtContainer.tagMap);
		//System.out.printf("%s\n", blockMeta);
		
		nbtTarget.setInteger("ID",   blockID);
		nbtTarget.setInteger("Meta", blockMeta);
		nbtTarget.setString("TEClass", containerTE.getClass().getName());
		nbtTarget.setCompoundTag("NBT", nbtContainer);
		
		//this.dumpTag(nbtTarget, 0);
		
		//System.out.printf("%s\n", nbtTarget.tagMap);
		//System.out.printf("%s\n", nbtTarget.getTags());
		
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("Container"))
			stack.getTagCompound().removeTag("Container");
		
		stack.getTagCompound().setCompoundTag("Container", nbtTarget);

		//String moverName = this.getBlockName(containerTE);
		//String moverName = containerTE.getClass().getName();
		
		world.removeBlockTileEntity(x, y, z);
		world.setBlock(x, y, z, 0, 0, 1 + 2);
		
		stack.setItemDamage(1);
		//stack.setItemName(mod_BetterBarrels.moverName + " (" + moverName + ")");
		stack.setItemName(mod_BetterBarrels.moverName + " (Full)");
		
		return true;		
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
		if (world.isRemote){return;}
		
		if ((stack.hasTagCompound()) && stack.getTagCompound().hasKey("Container") && (entity instanceof EntityPlayer)){
			
			int amplifier = 1;
			if (stack.getTagCompound().hasKey("amount")){
				amplifier = Math.min(4, stack.getTagCompound().getInteger("amount") / 2048);
			}
			
			((EntityPlayer)entity).addPotionEffect(new PotionEffect(Potion.digSlowdown.id,  10, amplifier));
			((EntityPlayer)entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 10, amplifier));
		}
	}
	
	private ForgeDirection getBarrelOrientationOnPlacement(EntityPlayer player){
		
		ForgeDirection barrelOrientation = ForgeDirection.UNKNOWN;
		Vec3 playerLook = player.getLookVec();
		if (Math.abs(playerLook.xCoord) > Math.abs(playerLook.zCoord)){
			if (playerLook.xCoord > 0)
				barrelOrientation = ForgeDirection.WEST;
			else
				barrelOrientation = ForgeDirection.EAST;
		} else {
			if (playerLook.zCoord > 0)
				barrelOrientation = ForgeDirection.NORTH;
			else
				barrelOrientation = ForgeDirection.SOUTH;			
		}
		
        return barrelOrientation;
        
	}
	
	private ArrayList<ForgeDirection> convertOrientationFlagToForge(int flags){
		ArrayList<ForgeDirection> directions = new ArrayList<ForgeDirection>();
		
		for (int i = 0; i<4; i++)
			if (((1 << i) & flags) != 0)
				directions.add(ForgeDirection.getOrientation(i+2));
		
		return directions;
	}
	
	private int convertForgeToOrientationFlag(ArrayList<ForgeDirection> directions){
		int flags = 0;
		for (ForgeDirection direction:directions){
			flags += (1 << (direction.ordinal() - 2));
		}
		return flags;
	}
	
	private String getBlockName(TileEntity tileEntity){

		Block teBlock = Block.blocksList[tileEntity.getWorldObj().getBlockId(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord)];
		
		ItemStack pick = null;
		try{
			pick = teBlock.getPickBlock(null, tileEntity.getWorldObj(), tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
	        if(pick != null)
	        	return pick.getDisplayName();
		} catch (Throwable e){}

        return "<Unknown>";
	}
}
