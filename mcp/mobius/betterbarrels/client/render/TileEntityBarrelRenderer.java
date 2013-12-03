package mcp.mobius.betterbarrels.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import mcp.mobius.betterbarrels.mod_BetterBarrels;
import mcp.mobius.betterbarrels.client.Coordinates;
import mcp.mobius.betterbarrels.common.TileEntityBarrel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class TileEntityBarrelRenderer extends TileEntityBaseRenderer {

	public static HashMap<TileEntityBarrel, Coordinates> postponedSigns = new HashMap<TileEntityBarrel, Coordinates>();
	public static TileEntityBarrelRenderer _instance = null;
	
    //protected int textureSideRef = Minecraft.getMinecraft().renderEngine.getTexture("/mcp/mobius/betterbarrels/textures/block.png");
    //protected int textureIconRef = Minecraft.getMinecraft().renderEngine.getTexture("/mcp/mobius/betterbarrels/textures/items.png");

	public static TileEntityBarrelRenderer instance(){
		if (_instance == null)
			_instance = new TileEntityBarrelRenderer();
		return _instance;
	}
	
	//TODO : Removed 2nd pass to test rendering speed
	/*
	public void secondPassRendering(TileEntityBarrel barrelEntity, Coordinates barrelPos) {
		int blockOrientation = barrelEntity.blockOrientation;

        String textureFile = BLOCK_FILE;
        
        if (mod_BetterBarrels.highRezTexture)
        	textureFile = BLOCK32_FILE;
		
    	for (ForgeDirection forgeSide: ForgeDirection.VALID_DIRECTIONS){	        
        	//Here we render the sign itself (and so, we can control it in funky ways !)
        	if (this.isItemDisplaySide(forgeSide, blockOrientation)){

    	        GL11.glEnable(GL11.GL_BLEND);
    	        //GL11.glDisable(GL11.GL_CULL_FACE);
    	        //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_SRC_ALPHA);
    	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
    	        //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_SRC_ALPHA);
    	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
    	        GL11.glDepthMask(false);     
    	        
    	        this.renderBarrelSide(textureFile, 3, forgeSide, barrelPos, false, true);
    	        
    			GL11.glDepthMask(true);
     
        		
    			GL11.glDisable(GL11.GL_BLEND);
        	}
    	}		
		
	}
	*/
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double xpos, double ypos, double zpos, float var8) {
		if (tileEntity instanceof TileEntityBarrel)
        {
			this.saveState();
			
			// First, we get the associated block metadata for orientation
			int blockOrientation = ((TileEntityBarrel) tileEntity).blockOrientation;
			TileEntityBarrel barrelEntity = (TileEntityBarrel)tileEntity;
			Coordinates barrelPos = new Coordinates(xpos, ypos, zpos);
			
	        GL11.glDisable(GL11.GL_BLEND);
	        GL11.glDisable(GL11.GL_LIGHTING);  			

	        int textureUpgrade = barrelEntity.upgradeCapacity;
	        
	        if (!mod_BetterBarrels.fullBarrelTexture)
	        	textureUpgrade = 0;
	        
        	for (ForgeDirection forgeSide: ForgeDirection.VALID_DIRECTIONS){
        		int textureIndex = 0;
            	if (this.isItemDisplaySide(forgeSide, blockOrientation))
            		textureIndex = 16*textureUpgrade + 1;  		
            	else if ((forgeSide == ForgeDirection.UP) || (forgeSide == ForgeDirection.DOWN))
            		textureIndex = 16*textureUpgrade;
            	else
            		textureIndex = 16*textureUpgrade + 2;
            	
            	this.setLight(barrelEntity, forgeSide);
            	this.renderBarrelSide(textureIndex, forgeSide, barrelPos);            	
            	
            	if (this.isItemDisplaySide(forgeSide, blockOrientation))
            		this.renderBarrelSide(3, forgeSide, barrelPos);

            	
            	//TODO : Desactivated for speed testing
            	//Here we render inside the barrel if we have a transparent barrel.
            	/*
            	if (barrelEntity.storageRemote)
                	this.renderBarrelSide(textureFile, textureIndex, forgeSide, barrelPos, true, false);
                */

        	}	        

        	//this.renderPlasma(ForgeDirection.SOUTH, barrelPos);	         	

	        for (ForgeDirection forgeSide: ForgeDirection.VALID_DIRECTIONS){					
				this.setLight(barrelEntity, forgeSide);
					
				if (barrelEntity.storage.hasItem() &&  this.isItemDisplaySide(forgeSide, blockOrientation))
				{
					this.renderStackOnBlock(barrelEntity.storage.getItem(), forgeSide, barrelPos, 8.0F, 65.0F, 75.0F);
					String barrelString = this.getBarrelString(barrelEntity);
					this.renderTextOnBlock(barrelString, forgeSide, barrelPos, 2.0F, 128.0F, 10.0F, 255, 255, 255, 0, true);
					
				}

				//TODO : Simplified version for speed
				if (barrelEntity.storage.isGhosting() && this.isItemDisplaySide(forgeSide, blockOrientation))
					this.renderIconOnBlock(8, forgeSide, barrelPos, 2F, 223F, 215F, -0.01F);				
				
				//TODO : Desactivated for speed
				/*
				if (this.isItemDisplaySide(forgeSide, blockOrientation))
				{	
					if ((barrelEntity.upgradeCapacity > 0) && (mod_BetterBarrels.showUpgradeSymbols)) 
						this.renderIconOnBlock(ITEM_FILE,  barrelEntity.upgradeCapacity, forgeSide, barrelPos, 0.5F, 0.5F, 52.0F, 0.01F);
					
					if(barrelEntity.storage.isGhosting())
						this.renderIconOnBlock(ITEM_FILE, 8, forgeSide, barrelPos, 0.5F, 27.5F, 52.0F, 0.01F);
					
					if(barrelEntity.storage.isPrivate())
						this.renderIconOnBlock(ITEM_FILE, 9, forgeSide, barrelPos, 0.5F, 0.0F, 52.0F, 0.01F);
				}
				*/
			}

     
	        /*
        	for (ForgeDirection forgeSide: ForgeDirection.VALID_DIRECTIONS){	        
            	//Here we render the sign itself (and so, we can control it in funky ways !)
				this.setLight(barrelEntity, forgeSide);        		
        		
            	if (this.isItemDisplaySide(forgeSide, blockOrientation)){

            		//TODO : Removed alpha blending rendering of remote storage to test for fps drop
            		// We render the sign half translucent.
            		//if(barrelEntity.storageRemote){
            		//	postponedSigns.put(barrelEntity, barrelPos);
            		//}
            		//else
        			this.renderBarrelSide(textureFile, 3, forgeSide, barrelPos, false, false);
            	}
        	}
        	*/
        	
	        this.loadState();
        	
        }

	}

	protected boolean isItemDisplaySide(ForgeDirection forgeSide, int blockOrientation){
		return ( ( (1 << (forgeSide.ordinal() - 2)) & blockOrientation) != 0);
	}
	
	protected String getBarrelString(TileEntityBarrel barrel){
        String outstring = null;
        if (!barrel.storage.hasItem()) return "";

    	int maxstacksize = barrel.storage.getItem().getMaxStackSize();
    	//int amount  = Math.min(barrel.storage.getAmount(), (int)Math.pow(2, barrel.upgradeCapacity) * barrel.storage.getBaseStacks() * barrel.storage.getItem().stackSize);
    	int amount = barrel.storage.getAmount(); 
        
        if (maxstacksize != 1){
        	int nstacks = amount/maxstacksize;
        	int remains = amount%maxstacksize;
        	
        	if ((nstacks > 0) && (remains > 0)){
        		outstring = String.format("%s*%s + %s", nstacks, maxstacksize, remains);
        	} else if ((nstacks == 0) && (remains > 0)) {
        		outstring = String.format("%s", remains);
        	} else if ((nstacks > 0) && (remains == 0)) {
        		outstring = String.format("%s*%s", nstacks, maxstacksize);
        	} else if (amount == 0){
        		outstring = "0";
        	}
        }
        else if (maxstacksize == 1){
        	outstring = String.format("%s", amount);
        } else {
        	outstring = "";
        }
        
        //if (amount < barrel.storage.getAmount())
        //	outstring += " [...]";
        
        return outstring;
	}
	
	protected void renderBarrelSide(int index,  ForgeDirection side, Coordinates barrelPos){
    	
        //float revert = reverted ? -0.9991F : 1F;
        //side = reverted ? side.getOpposite() : side;
        
        float size = 16.0F;
        
    	GL11.glPushMatrix();
    	
        this.alignRendering(side, barrelPos);
        this.moveRendering(16.0F, 0.0, 0.0, 0.0);
        
    	this.texManager.bindTexture(blocksSheetRes);
        this.drawTexturedModalRect(0, 0, 16*(index%16), 16*(index/16), 16, 16);
        
        GL11.glPopMatrix();    	           
    }    
    

	
    //TODO : Desactivated to test rendering speed
    /*
    private void renderPlasma(ForgeDirection side, Coordinates barrelPos){
    	GL11.glPushMatrix();

    	//GL11.glBindTexture(GL11.GL_TEXTURE_2D, PlasmaTexture.getTextureIndex());
    	
    	PlasmaTexture.bindTexture();

        GL11.glTranslated(barrelPos.x + 0.5F, barrelPos.y + 0.5F, barrelPos.z + 0.5F);     // We align the rendering on the center of the block
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(this.getRotationYForSide(side), 0.0F, 1.0F, 0.0F); // We rotate it so it face the right face
        GL11.glRotatef(this.getRotationXForSide(side), 1.0F, 0.0F, 0.0F);
        GL11.glTranslated(-0.5F, -0.5F, -0.5F);
        GL11.glScalef(scale*4.0F, scale*4.0F, 0.0000F);

        this.drawTexturedModalRect(0, 0, 16, 16, 16, 16);    	
    	
        GL11.glPopMatrix();    	
    }
    */
    
    
}