package mcp.mobius.betterbarrels.client.render;

import java.util.HashMap;

import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.client.Coordinates;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import mcp.mobius.betterbarrels.common.items.ItemBarrelHammer;
import mcp.mobius.betterbarrels.common.items.upgrades.ItemUpgradeStructural;
import mcp.mobius.betterbarrels.common.items.upgrades.UpgradeSide;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

public class TileEntityBarrelRenderer extends TileEntityBaseRenderer {

	public static HashMap<TileEntityBarrel, Coordinates> postponedSigns = new HashMap<TileEntityBarrel, Coordinates>();
	public static TileEntityBarrelRenderer _instance = null;
	
    protected static ItemStack coreStorage  = new ItemStack(BetterBarrels.itemUpgradeCore, 0, 0);
    protected static ItemStack coreEnder    = new ItemStack(BetterBarrels.itemUpgradeCore, 0, 1);
    protected static ItemStack coreRedstone = new ItemStack(BetterBarrels.itemUpgradeCore, 0, 2);
    protected static ItemStack coreHopper   = new ItemStack(BetterBarrels.itemUpgradeCore, 0, 3);	
	
    //protected int textureSideRef = Minecraft.getMinecraft().renderEngine.getTexture("/mcp/mobius/betterbarrels/textures/block.png");
    //protected int textureIconRef = Minecraft.getMinecraft().renderEngine.getTexture("/mcp/mobius/betterbarrels/textures/items.png");

	public static TileEntityBarrelRenderer instance(){
		if (_instance == null)
			_instance = new TileEntityBarrelRenderer();
		return _instance;
	}
	
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

        	boolean isHammer = this.mc.thePlayer.getHeldItem() != null ? this.mc.thePlayer.getHeldItem().getItem() instanceof ItemBarrelHammer ? true : false : false;
        	boolean hasItem  = barrelEntity.storage.hasItem();
        	
        	int color = ItemUpgradeStructural.textColor[barrelEntity.levelStructural];

	        for (ForgeDirection forgeSide: ForgeDirection.VALID_DIRECTIONS){					
				this.setLight(barrelEntity, forgeSide);
				
				if (hasItem &&  this.isItemDisplaySide(barrelEntity, forgeSide))
				{
					if (forgeSide == ForgeDirection.DOWN || forgeSide == ForgeDirection.UP)
						this.renderStackOnBlock(barrelEntity.storage.getItem(), forgeSide, orientation, barrelPos, 8.0F, 65.0F, 64.0F);
					else
						this.renderStackOnBlock(barrelEntity.storage.getItem(), forgeSide, orientation, barrelPos, 8.0F, 65.0F, 75.0F);
					String barrelString = this.getBarrelString(barrelEntity);
					this.renderTextOnBlock(barrelString, forgeSide, orientation, barrelPos, 2.0F, 128.0F, 10.0F, color, TileEntityBaseRenderer.ALIGNCENTER);
					
				}
	        }
				
			if (isHammer){
		        for (ForgeDirection forgeSide: ForgeDirection.VALID_DIRECTIONS){
					this.setLight(barrelEntity, forgeSide);
		        	if (this.isItemDisplaySide(barrelEntity, forgeSide)){
						int offsetY = 256 - 32;
						
						if (barrelEntity.levelStructural > 0){
							this.renderIconOnBlock(0, forgeSide, orientation, barrelPos, 2F, 0.0F, 0, -0.01F);
							this.renderTextOnBlock("x"+String.valueOf(barrelEntity.levelStructural), forgeSide, orientation, barrelPos, 2.0F, 37.0F, 0 + 15.0F, color, TileEntityBaseRenderer.ALIGNLEFT);
						}						
						
						if (barrelEntity.nStorageUpg > 0){
							this.renderStackOnBlock(TileEntityBarrelRenderer.coreStorage, forgeSide, orientation, barrelPos, 2.0F, 256.0F - 32F, 0);
							this.renderTextOnBlock(String.valueOf(barrelEntity.nStorageUpg) + "x", forgeSide, orientation, barrelPos, 2.0F, 256.0F - 32F , 15.0F, color, TileEntityBaseRenderer.ALIGNRIGHT);
						}
							
						if (barrelEntity.hasRedstone){
							this.renderStackOnBlock(TileEntityBarrelRenderer.coreRedstone, forgeSide, orientation, barrelPos, 2.0F, 0.0F, offsetY);
							offsetY -= 35;
						}
						
						if (barrelEntity.hasHopper){
							this.renderStackOnBlock(TileEntityBarrelRenderer.coreHopper, forgeSide, orientation, barrelPos, 2.0F, 0.0F, offsetY);
							offsetY -= 35;
						}
						
						if (barrelEntity.hasEnder){
							this.renderStackOnBlock(TileEntityBarrelRenderer.coreEnder, forgeSide, orientation, barrelPos, 2.0F, 0.0F, offsetY);
							offsetY -= 35;
						}	
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
   
}
