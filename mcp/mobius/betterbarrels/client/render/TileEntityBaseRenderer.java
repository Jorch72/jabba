package mcp.mobius.betterbarrels.client.render;

import org.lwjgl.opengl.GL11;

import mcp.mobius.betterbarrels.client.Coordinates;
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
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeDirection;

public abstract class TileEntityBaseRenderer extends TileEntitySpecialRenderer {
	
	protected float scale = 1f/256f;
    protected RenderBlocks renderBlocks = new RenderBlocks();	
    protected RenderItem   renderItem   = new RenderItem();

    protected TextureManager texManager    = Minecraft.getMinecraft().renderEngine;
    protected FontRenderer   renderFont    = Minecraft.getMinecraft().fontRenderer;
	
    protected static ResourceLocation itemsSheetRes    = new ResourceLocation("jabba", "textures/sheets/items.png");
    protected static ResourceLocation blocksSheetRes   = new ResourceLocation("jabba", "textures/sheets/blocks.png");
    protected static ResourceLocation blocks32SheetRes = new ResourceLocation("jabba", "textures/sheets/blocks32.png");    
    protected static ResourceLocation blankRes         = new ResourceLocation("jabba", "blank.png");
    
	protected boolean hasBlending;
	protected boolean hasLight;
	protected int   boundTexIndex;    
    
	protected void setLight(TileEntity tileEntity, ForgeDirection side){
		int xOffset = 0;
		int zOffset = 0;
		int yOffset = 0;
	
		switch(side.ordinal()){
			case 0:	xOffset =  0; zOffset =  0; yOffset = -1; break;
			case 1:	xOffset =  0; zOffset =  0; yOffset = 1; break;		
			case 2:	xOffset =  0; zOffset = -1; yOffset = 0; break;
			case 3:	xOffset =  0; zOffset =  1; yOffset = 0; break;
			case 4:	xOffset = -1; zOffset =  0; yOffset = 0; break;
			case 5:	xOffset =  1; zOffset =  0; yOffset = 0; break;
		}
		
        int ambientLight = tileEntity.worldObj.getLightBrightnessForSkyBlocks(tileEntity.xCoord + xOffset, tileEntity.yCoord + yOffset, tileEntity.zCoord + zOffset, 0);
        int var6 = ambientLight % 65536;
        int var7 = ambientLight / 65536;
        float var8 = 0.8F;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var6 * var8, var7 * var8);		
	}    
    
	protected void renderTextOnBlock(String renderString, ForgeDirection side, Coordinates barrelPos, float size, double posx, double posy, int red, int green, int blue, int alpha, boolean centered){

    	if (renderString == null || renderString.equals("")){return;}
        	
    	int stringWidth = this.getFontRenderer().getStringWidth(renderString);

    	GL11.glPushMatrix();

    	this.alignRendering(side, barrelPos);
        this.moveRendering(size, posx, posy, -0.01);
        
        GL11.glDepthMask(false);            	
        GL11.glDisable(GL11.GL_LIGHTING);

        int color = (alpha << 24) | (red << 16) | (blue << 8) | green;
        
        if (centered){
        	this.getFontRenderer().drawString(renderString, -stringWidth / 2, 0, color);
        } else {
        	this.getFontRenderer().drawString(renderString, 0, 0, color);
        }
        
        GL11.glDepthMask(true);
        GL11.glPopMatrix();       
    }

	protected void renderStackOnBlock(ItemStack stack, ForgeDirection side, Coordinates barrelPos, float size, double posx, double posy){
    	
    	if (stack == null){return;}

        
    	GL11.glPushMatrix();

    	this.alignRendering(side, barrelPos);
        this.moveRendering(size, posx, posy, -0.01, 0.1f);
        
        if (!ForgeHooksClient.renderInventoryItem(this.renderBlocks, this.texManager, stack, true, 0.0F, 0.0F, 0.0F))
        {
            this.renderItem.renderItemIntoGUI(this.renderFont, this.texManager, stack, 0, 0);
        }        
        
        GL11.glPopMatrix();    	 
    }

	protected void renderIconOnBlock(int index,  ForgeDirection side, Coordinates barrelPos, float size, double posx, double posy, double zdepth){
    	GL11.glPushMatrix();
        
    	this.alignRendering(side, barrelPos);
        this.moveRendering(size, posx, posy, zdepth);
        
        this.texManager.bindTexture(itemsSheetRes);
        this.drawTexturedModalRect(0, 0, 16*(index%16), 16*(index/16), 16, 16);
        
        GL11.glPopMatrix();        
    }	
	
	protected void alignRendering(ForgeDirection side, Coordinates position){
        GL11.glTranslated(position.x + 0.5F, position.y + 0.5F, position.z + 0.5F);     // We align the rendering on the center of the block
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(this.getRotationYForSide(side), 0.0F, 1.0F, 0.0F); // We rotate it so it face the right face
        GL11.glRotatef(this.getRotationXForSide(side), 1.0F, 0.0F, 0.0F);
        GL11.glTranslated(-0.5F, -0.5F, -0.5f);		
	}
	
	protected void moveRendering(float size, double posX, double posY, double posz){
		this.moveRendering(size, posX, posY, posz, 0.0f);
	}	

	protected void moveRendering(float size, double posX, double posY, double posz, float flatenning){
		GL11.glTranslated(0, 0, posz);
        GL11.glScalef(scale, scale, flatenning);			  // We flatten the rendering and scale it to the right size
        GL11.glTranslated(posX, posY, 0);		  // Finally, we translate the icon itself to the correct position
        GL11.glScalef(size, size, 0);		
	}		
	
    protected float getRotationYForSide(ForgeDirection side){
    	int sideRotation[]  = {0,0,0,2,3,1};
    	return sideRotation[side.ordinal()] * 90F;
    }

    protected float getRotationXForSide(ForgeDirection side){
    	int sideRotation[]  = {1,3,0,0,0,0};
    	return sideRotation[side.ordinal()] * 90F;
    }  
    
    protected void drawTexturedModalRect(int posX, int posY, int textureX, int textureY, int sizeX, int sizeY)
    {
        float scaleX = 0.00390625F;
        float scaleY = 0.00390625F;
        float zLevel = 0.0F;
        Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(posX + 0,     posY + sizeY, zLevel, (textureX + 0) * scaleX,     (textureY + sizeY) * scaleY);
        var9.addVertexWithUV(posX + sizeX, posY + sizeY, zLevel, (textureX + sizeX) * scaleX, (textureY + sizeY) * scaleY);
        var9.addVertexWithUV(posX + sizeX, posY + 0,     zLevel, (textureX + sizeX) * scaleX, (textureY + 0) * scaleY);
        var9.addVertexWithUV(posX + 0,     posY + 0,     zLevel, (textureX + 0) * scaleX,     (textureY + 0) * scaleY);
        var9.draw();
    }		
	
    protected void saveState(){
		hasBlending   = GL11.glGetBoolean(GL11.GL_BLEND);
		hasLight      = GL11.glGetBoolean(GL11.GL_LIGHTING);
    	boundTexIndex = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);    	
    }
    
    protected void loadState(){
    	if (hasBlending)
    		GL11.glEnable(GL11.GL_BLEND);
    	else
    		GL11.glDisable(GL11.GL_BLEND);

    	if (hasLight)
    		GL11.glEnable(GL11.GL_LIGHTING);
    	else
    		GL11.glDisable(GL11.GL_LIGHTING);
    	
    	GL11.glBindTexture(GL11.GL_TEXTURE_2D, boundTexIndex);    	
    }
    
}
