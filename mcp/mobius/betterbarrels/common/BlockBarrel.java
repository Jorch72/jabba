package mcp.mobius.betterbarrels.common;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.betterbarrels.mod_BetterBarrels;
import mcp.mobius.betterbarrels.client.ClientProxy;
import mcp.mobius.betterbarrels.common.items.ItemBarrelLocker;
import mcp.mobius.betterbarrels.common.items.ItemBarrelSticker;
import mcp.mobius.betterbarrels.common.items.ItemTuningFork;
import mcp.mobius.betterbarrels.common.items.ItemUpgrade;
import mcp.mobius.betterbarrels.common.items.SideUpgrade;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaBlock;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class BlockBarrel extends BlockContainer{

	private static Icon text_side  = null;
	private static Icon text_top   = null;
	private static Icon text_label = null;
	private static Icon text_blank = null;	
	
    public BlockBarrel(int par1)
    {
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
    public void registerIcons(IconRegister par1IconRegister)
    {
    	this.text_side  = par1IconRegister.registerIcon(mod_BetterBarrels.modid + ":" + "barrel_side");
    	this.text_top   = par1IconRegister.registerIcon(mod_BetterBarrels.modid + ":" + "barrel_top");
    	this.text_label = par1IconRegister.registerIcon(mod_BetterBarrels.modid + ":" + "barrel_label");
    	this.text_blank = par1IconRegister.registerIcon(mod_BetterBarrels.modid + ":" + "blank");
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int metadata)
    {
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
    
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack par6ItemStack) 
    {
    	// We get the orientation and check if the TE is already properly created.
    	// If so we set the entity value to the correct orientation and set the block meta to 1 to kill the normal block rendering.
    	
        int barrelOrientation = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        TileEntityBarrel barrelEntity = (TileEntityBarrel)world.getBlockTileEntity(x, y, z);
        
        if (barrelEntity != null){
        	world.setBlockMetadataWithNotify(x, y, z, 1, 1 & 2);
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
	        barrelEntity.sideUpgrades[barrelEntity.orientation.ordinal()] = SideUpgrade.FRONT;
        }
    }
    
    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
    {
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
            items = new EntityItem(world, (double)((float)x + var10), (double)((float)y + var11), (double)((float)z + var12), new ItemStack(stack.itemID, var13, stack.getItemDamage()));
            float var15 = 0.05F;
            items.motionX = (double)((float)random.nextGaussian() * var15);
            items.motionY = (double)((float)random.nextGaussian() * var15 + 0.2F);
            items.motionZ = (double)((float)random.nextGaussian() * var15);

            if (stack.hasTagCompound())
            {
            	items.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
            }
        }  		
	}
	
	@Override
    public void breakBlock(World world, int x, int y, int z, int par5, int par6)
    {

    	TileEntityBarrel barrelEntity = (TileEntityBarrel)world.getBlockTileEntity(x, y, z);
    	
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
    public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z)
    {
    	TileEntityBarrel barrelEntity = (TileEntityBarrel)world.getBlockTileEntity(x, y, z);
    	if (player != null && barrelEntity != null && !barrelEntity.storage.canInteract(player.username)){
    		return false;
    	}
    	return super.removeBlockByPlayer(world, player, x, y, z);
		
    } 
}
