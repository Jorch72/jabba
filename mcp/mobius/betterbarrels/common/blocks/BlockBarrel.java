package mcp.mobius.betterbarrels.common.blocks;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.betterbarrels.mod_BetterBarrels;
import mcp.mobius.betterbarrels.common.items.upgrades.UpgradeCore;
import mcp.mobius.betterbarrels.common.items.upgrades.UpgradeSide;

public class BlockBarrel extends BlockContainer{

	public static Icon[] text_side     = new Icon[16];
	public static Icon[] text_top      = new Icon[16];
	public static Icon[] text_label    = new Icon[16];
	public static Icon[] text_labeltop = new Icon[16];
	
    public BlockBarrel(int par1){
        super(par1, Material.wood);
        this.setHardness(2.0F);
        this.setResistance(5.0F);
        this.setUnlocalizedName("jabba.blockbarrel");
		this.setCreativeTab(CreativeTabs.tabBlock);
    }

	@Override
	public TileEntity createNewTileEntity(World world) {
        return new TileEntityBarrel();
	}
	
    @Override    
    public void registerIcons(IconRegister iconRegister){
    	for (int i=0; i<16; i++)
    	{
    		BlockBarrel.text_side[i]     = iconRegister.registerIcon(mod_BetterBarrels.modid + ":" + "barrel_side_"     + String.valueOf(i));
    		BlockBarrel.text_top[i]      = iconRegister.registerIcon(mod_BetterBarrels.modid + ":" + "barrel_top_"      + String.valueOf(i));
    		BlockBarrel.text_label[i]    = iconRegister.registerIcon(mod_BetterBarrels.modid + ":" + "barrel_label_"    + String.valueOf(i));
    		BlockBarrel.text_labeltop[i] = iconRegister.registerIcon(mod_BetterBarrels.modid + ":" + "barrel_labeltop_" + String.valueOf(i));
    	}
    }
    
    /*
    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int metadata){
    	// Meta is not equal to 0 if the TE is properly set and we don't want normal block rendering
    	if (metadata != 0)
    		return text_blank;
    	
    	// This whole part exists only to get proper inventory rendering of the block
    	ForgeDirection forgeSide = ForgeDirection.getOrientation(side);
    	
    	if ((forgeSide == ForgeDirection.UP) || (forgeSide == ForgeDirection.DOWN))
    		return text_top;    	
    	else if (forgeSide == ForgeDirection.WEST)
    		return text_label;
    	else
    		return text_side;    		
    }
    */
    
    /*
    @Override
	public void onBlockAdded(World world, int x, int y, int z) {
    	world.setBlockMetadataWithNotify(x, y, z, 1, 1 & 2);    	
    }
    */
    
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack par6ItemStack){
    	// We get the orientation and check if the TE is already properly created.
    	// If so we set the entity value to the correct orientation and set the block meta to 1 to kill the normal block rendering.
    	
        int barrelOrientation = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        TileEntityBarrel barrelEntity = (TileEntityBarrel)world.getBlockTileEntity(x, y, z);
        
        if (barrelEntity != null){
	        switch (barrelOrientation){
	        	case 0:
	        		barrelEntity.orientation = ForgeDirection.NORTH;
	        		break;
	        	case 1:
	        		barrelEntity.orientation = ForgeDirection.EAST;	        		
	        		break;
	        	case 2:
	        		barrelEntity.orientation = ForgeDirection.SOUTH;	        		
	        		break;
	        	case 3:
	        		barrelEntity.orientation = ForgeDirection.WEST;	        		
	        		break;        		
	        }
	        barrelEntity.sideUpgrades[barrelEntity.orientation.ordinal()] = UpgradeSide.FRONT;
        }
    }
    
    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player){
        if (!world.isRemote){
            TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
            ((TileEntityBarrel)tileEntity).leftClick(player);
        }
    }     
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float var7, float var8, float var9){
        if (!world.isRemote){
            TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
            ((TileEntityBarrel)tileEntity).rightClick(player, side);        	
        }
        return true;
    }
    
	private void dropStack(World world, ItemStack stack, int x, int y, int z){
    	Random random = new Random();
        float var10 = random.nextFloat() * 0.8F + 0.1F;
        float var11 = random.nextFloat() * 0.8F + 0.1F;
        EntityItem items;

        
        for (float var12 = random.nextFloat() * 0.8F + 0.1F; stack.stackSize > 0; world.spawnEntityInWorld(items))
        {
            int var13 = random.nextInt(21) + 10;

            if (var13 > stack.stackSize)
            {
                var13 = stack.stackSize;
            }

            stack.stackSize -= var13;
            items = new EntityItem(world, x + var10, y + var11, z + var12, new ItemStack(stack.itemID, var13, stack.getItemDamage()));
            float var15 = 0.05F;
            items.motionX = (float)random.nextGaussian() * var15;
            items.motionY = (float)random.nextGaussian() * var15 + 0.2F;
            items.motionZ = (float)random.nextGaussian() * var15;

            if (stack.hasTagCompound())
            {
            	items.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
            }
        }  		
	}
	
	@Override
    public void breakBlock(World world, int x, int y, int z, int par5, int par6){

    	TileEntityBarrel barrelEntity = (TileEntityBarrel)world.getBlockTileEntity(x, y, z);
    	
    	// We drop the structural upgrades
    	if ((barrelEntity != null) && (barrelEntity.levelStructural > 0)){
    		int currentUpgrade = barrelEntity.levelStructural;
    		while (currentUpgrade > 0){
    			ItemStack droppedStack = new ItemStack(mod_BetterBarrels.itemUpgradeStructural, 1, currentUpgrade-1);
    			this.dropStack(world, droppedStack, x, y, z);
    			currentUpgrade -= 1;
    		}
    	}    	
    	
    	// We drop the core upgrades
    	if (barrelEntity != null){
    		for (Integer i : barrelEntity.coreUpgrades){
    			Item upgrade = UpgradeCore.mapItem[i];
    			if (upgrade != null){
    				ItemStack droppedStack = new ItemStack(upgrade, 1, UpgradeCore.mapMeta[i]);
    				this.dropStack(world, droppedStack, x, y, z);
    			}
    		}
    	}     	
    	
    	// We drop the side upgrades
    	if (barrelEntity != null){
    		for (int i = 0; i < 6; i++){
    			Item upgrade = UpgradeSide.mapItem[barrelEntity.sideUpgrades[i]];
    			if (upgrade != null){
    				ItemStack droppedStack = new ItemStack(upgrade, 1, UpgradeSide.mapMeta[barrelEntity.sideUpgrades[i]]);
    				this.dropStack(world, droppedStack, x, y, z);
    			}
    		}
    	}    	
    	
    	// We drop the stacks
        if ((barrelEntity != null) && (barrelEntity.storage.hasItem()))
        {
        	barrelEntity.updateEntity();
        	int ndroppedstacks = 0;
        	ItemStack droppedstack = new ItemStack(0,0,0);
        	while ((droppedstack != null) && (ndroppedstacks <= 64)){
        		droppedstack    = barrelEntity.storage.getStack();
        		ndroppedstacks += 1;
        		
        		if (droppedstack != null)
        			this.dropStack(world, droppedstack, x, y, z);
        	}
        }

        super.breakBlock(world, x, y, z, par5, par6);        
        
    }

	@Override
    public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z){
    	TileEntityBarrel barrelEntity = (TileEntityBarrel)world.getBlockTileEntity(x, y, z);
    	if (player != null && barrelEntity != null && !barrelEntity.storage.canInteract(player.username)){
    		return false;
    	}
    	return super.removeBlockByPlayer(world, player, x, y, z);
		
    }
	
	/* REDSTONE HANDLING */

	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side){
		return this.isProvidingWeakPower(world, x, y, z, side);
    }

    @Override
	public boolean canProvidePower(){
    	return true;
    }

    @Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side){
    	TileEntityBarrel barrel = (TileEntityBarrel)world.getBlockTileEntity(x, y, z);
    	return barrel.getRedstonePower(side);
    }

	@Override
	public int getRenderType(){
		return mod_BetterBarrels.blockBarrelRendererID;
	}    
    
    /*
    @Override
    public boolean isBlockNormalCube(World world, int x, int y, int z){
    	return false;
    }
    */
    
    /*
    private boolean isIndirectlyPowered(World par1World, int par2, int par3, int par4)
    {
        int l = par1World.getBlockMetadata(par2, par3, par4);
        return l == 5 && par1World.getIndirectPowerOutput(par2, par3 - 1, par4, 0) ? true : (l == 3 && par1World.getIndirectPowerOutput(par2, par3, par4 - 1, 2) ? true : (l == 4 && par1World.getIndirectPowerOutput(par2, par3, par4 + 1, 3) ? true : (l == 1 && par1World.getIndirectPowerOutput(par2 - 1, par3, par4, 4) ? true : l == 2 && par1World.getIndirectPowerOutput(par2 + 1, par3, par4, 5))));
    }
    */    
}
