package mcp.mobius.betterbarrels.client.render;

import java.util.HashMap;

import mcp.mobius.betterbarrels.mod_BetterBarrels;
import mcp.mobius.betterbarrels.client.Coordinates;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import mcp.mobius.betterbarrels.common.items.ItemBarrelHammer;
import mcp.mobius.betterbarrels.common.items.upgrades.UpgradeSide;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

public class TileEntityBarrelRenderer extends TileEntityBaseRenderer {

	public static HashMap<TileEntityBarrel, Coordinates> postponedSigns = new HashMap<TileEntityBarrel, Coordinates>();
	public static TileEntityBarrelRenderer _instance = null;
	
    protected static ItemStack coreStorage  = new ItemStack(mod_BetterBarrels.itemUpgradeCore, 0, 0);
    protected static ItemStack coreEnder    = new ItemStack(mod_BetterBarrels.itemUpgradeCore, 0, 1);
    protected static ItemStack coreRedstone = new ItemStack(mod_BetterBarrels.itemUpgradeCore, 0, 2);
    protected static ItemStack coreHopper   = new ItemStack(mod_BetterBarrels.itemUpgradeCore, 0, 3);	
	
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
			//int blockOrientation = ((TileEntityBarrel) tileEntity).orientation.ordinal();
			ForgeDirection orientation    = ((TileEntityBarrel) tileEntity).orientation;
			TileEntityBarrel barrelEntity = (TileEntityBarrel)tileEntity;
			Coordinates barrelPos = new Coordinates(xpos, ypos, zpos);
			
	        GL11.glDisable(GL11.GL_BLEND);
	        GL11.glDisable(GL11.GL_LIGHTING);  			

	        int textureUpgrade = barrelEntity.levelStructural;
	        
	        if (!mod_BetterBarrels.fullBarrelTexture)
	        	textureUpgrade = 0;

        	boolean isHammer = this.mc.thePlayer.getHeldItem() != null ? this.mc.thePlayer.getHeldItem().getItem() instanceof ItemBarrelHammer ? true : false : false;
        	
	        for (ForgeDirection forgeSide: ForgeDirection.VALID_DIRECTIONS){					
				this.setLight(barrelEntity, forgeSide);
					
				if (barrelEntity.storage.hasItem() &&  this.isItemDisplaySide(barrelEntity, forgeSide))
				{
					if (forgeSide == ForgeDirection.DOWN || forgeSide == ForgeDirection.UP)
						this.renderStackOnBlock(barrelEntity.storage.getItem(), forgeSide, orientation, barrelPos, 8.0F, 65.0F, 64.0F);
					else
						this.renderStackOnBlock(barrelEntity.storage.getItem(), forgeSide, orientation, barrelPos, 8.0F, 65.0F, 75.0F);
					String barrelString = this.getBarrelString(barrelEntity);
					this.renderTextOnBlock(barrelString, forgeSide, orientation, barrelPos, 2.0F, 128.0F, 10.0F, 255, 255, 255, 0, true);
					
				}

				//TODO : Simplified version for speed
				if (barrelEntity.storage.isGhosting() && this.isItemDisplaySide(barrelEntity, forgeSide))
					this.renderIconOnBlock(8, forgeSide, orientation, barrelPos, 2F, 223F, 215F, -0.01F);				
				
				if (barrelEntity.sideUpgrades[forgeSide.ordinal()] == UpgradeSide.REDSTONE)
					this.renderIconOnBlock(10, forgeSide, orientation, barrelPos, 8F, 64F, 64F, -0.01F);

				if (barrelEntity.sideUpgrades[forgeSide.ordinal()] == UpgradeSide.HOPPER)
					this.renderIconOnBlock(11, forgeSide, orientation, barrelPos, 8F, 64F, 64F, -0.01F);				
				
				if (isHammer && this.isItemDisplaySide(barrelEntity, forgeSide)){
					int offsetY = 0;
					if (barrelEntity.nStorageUpg > 0){
						this.renderStackOnBlock(TileEntityBarrelRenderer.coreStorage, forgeSide, orientation, barrelPos, 2.0F, 0.0F, offsetY);
						this.renderTextOnBlock("x"+String.valueOf(barrelEntity.nStorageUpg), forgeSide, orientation, barrelPos, 2.0F, 35.0F, offsetY + 15.0F, 255, 255, 255, 0, false);
						offsetY += 35;
					}
						
					if (barrelEntity.hasRedstone){
						this.renderStackOnBlock(TileEntityBarrelRenderer.coreRedstone, forgeSide, orientation, barrelPos, 2.0F, 0.0F, offsetY);
						offsetY += 35;
					}
					
					if (barrelEntity.hasHopper){
						this.renderStackOnBlock(TileEntityBarrelRenderer.coreHopper, forgeSide, orientation, barrelPos, 2.0F, 0.0F, offsetY);
						offsetY += 35;
					}
					
					if (barrelEntity.hasEnder){
						this.renderStackOnBlock(TileEntityBarrelRenderer.coreEnder, forgeSide, orientation, barrelPos, 2.0F, 0.0F, offsetY);
						offsetY += 35;
					}					
				}
			}
        	
	        this.loadState();
        	
        }

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
	
	protected void renderBarrelSide(int index,  ForgeDirection side, ForgeDirection orientation, Coordinates barrelPos){
    	
        //float revert = reverted ? -0.9991F : 1F;
        //side = reverted ? side.getOpposite() : side;
        
        float size = 16.0F;
        
    	GL11.glPushMatrix();
    	
        this.alignRendering(side, orientation, barrelPos);
        this.moveRendering(16.0F, 0.0, 0.0, 0.0);
        
    	this.texManager.bindTexture(blocksSheetRes);
        this.drawTexturedModalRect(0, 0, 16*(index%16), 16*(index/16), 16, 16);
        
        GL11.glPopMatrix();    	           
    }    
    
	protected boolean isItemDisplaySide(TileEntityBarrel barrel, ForgeDirection forgeSide){
		if (barrel.sideUpgrades[forgeSide.ordinal()] == UpgradeSide.NONE)    return false;			
		if (barrel.sideUpgrades[forgeSide.ordinal()] == UpgradeSide.FRONT)   return true;		
		if (barrel.sideUpgrades[forgeSide.ordinal()] == UpgradeSide.STICKER) return true;
		return false;
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
