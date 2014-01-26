package mcp.mobius.betterbarrels.client.render;

import org.lwjgl.opengl.GL11;

import mcp.mobius.betterbarrels.mod_BetterBarrels;
import mcp.mobius.betterbarrels.common.blocks.BlockBarrel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class BlockBarrelRenderer implements ISimpleBlockRenderingHandler {

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
		BlockBarrel barrel      = (BlockBarrel)block;
		Tessellator tessellator = Tessellator.instance;
		
		Icon iconSide, iconTop, iconLabel;
		iconSide  = BlockBarrel.text_side;
		iconTop   = BlockBarrel.text_top;
		iconLabel = BlockBarrel.text_label;
		
		double minXSide = iconSide.getMinU();
		double maxXSide = iconSide.getMaxU();
		double minYSide = iconSide.getMinV();
		double maxYSide = iconSide.getMaxV();

		double minXTop = iconTop.getMinU();
		double maxXTop = iconTop.getMaxU();
		double minYTop = iconTop.getMinV();
		double maxYTop = iconTop.getMaxV();

		double minXLabel = iconLabel.getMinU();
		double maxXLabel = iconLabel.getMaxU();
		double minYLabel = iconLabel.getMinV();
		double maxYLabel = iconLabel.getMaxV();	
		
		double xMin = 0, xMax = 1;
		double yMin = 0, yMax = 1;
		double zMin = 0, zMax = 1;
		
		GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		
		/* BOTTOM */
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		tessellator.addVertexWithUV(xMin, yMin, zMax, minXTop, minYTop);
		tessellator.addVertexWithUV(xMin, yMin, zMin, minXTop, maxYTop);
		tessellator.addVertexWithUV(xMax, yMin, zMin, maxXTop, maxYTop);
		tessellator.addVertexWithUV(xMax, yMin, zMax, maxXTop, minYTop);
		tessellator.draw();	
		
		/* TOP */
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV(xMin, yMax, zMin, minXTop, minYTop);
		tessellator.addVertexWithUV(xMin, yMax, zMax, minXTop, maxYTop);
		tessellator.addVertexWithUV(xMax, yMax, zMax, maxXTop, maxYTop);
		tessellator.addVertexWithUV(xMax, yMax, zMin, maxXTop, minYTop);
		tessellator.draw();	
		
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		tessellator.addVertexWithUV(xMax, yMax, zMin, minXSide, minYSide);
		tessellator.addVertexWithUV(xMax, yMin, zMin, minXSide, maxYSide);
		tessellator.addVertexWithUV(xMin, yMin, zMin, maxXSide, maxYSide);
		tessellator.addVertexWithUV(xMin, yMax, zMin, maxXSide, minYSide);
		tessellator.draw();		
		
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addVertexWithUV(xMin, yMax, zMax, minXSide, minYSide);
		tessellator.addVertexWithUV(xMin, yMin, zMax, minXSide, maxYSide);
		tessellator.addVertexWithUV(xMax, yMin, zMax, maxXSide, maxYSide);
		tessellator.addVertexWithUV(xMax, yMax, zMax, maxXSide, minYSide);
		tessellator.draw();
		
		/* FRONT */
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		tessellator.addVertexWithUV(xMin, yMin, zMax, minXLabel, maxYLabel);
		tessellator.addVertexWithUV(xMin, yMax, zMax, minXLabel, minYLabel);
		tessellator.addVertexWithUV(xMin, yMax, zMin, maxXLabel, minYLabel);
		tessellator.addVertexWithUV(xMin, yMin, zMin, maxXLabel, maxYLabel);
		tessellator.draw();	
		
		/* BACK */
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		tessellator.addVertexWithUV(xMax, yMin, zMin, minXSide, minYSide);
		tessellator.addVertexWithUV(xMax, yMax, zMin, minXSide, maxYSide);
		tessellator.addVertexWithUV(xMax, yMax, zMax, maxXSide, maxYSide);
		tessellator.addVertexWithUV(xMax, yMin, zMax, maxXSide, minYSide);
		tessellator.draw();			
	}
	
	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block tile, int modelId, RenderBlocks renderer) {
		if (renderer.hasOverrideBlockTexture())
		{ // usually: block is being broken
			renderer.renderFaceYNeg(tile, x, y, z, null);
			renderer.renderFaceYPos(tile, x, y, z, null);
			renderer.renderFaceZNeg(tile, x, y, z, null);
			renderer.renderFaceZPos(tile, x, y, z, null);
			renderer.renderFaceXNeg(tile, x, y, z, null);
			renderer.renderFaceXPos(tile, x, y, z, null);
			return true;
		}		
		
		int worldHeight = world.getHeight();
		BlockBarrel block      = (BlockBarrel)tile;		
		Tessellator tessellator = Tessellator.instance;
		//tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x-1, y, z));
		//tessellator.setBrightness(world.getLightBrightnessForSkyBlocks(x, y, z, 15));
		//tessellator.setBrightness(world.getLightBrightnessForSkyBlocks(x,y,z, 7));
		//System.out.printf("%s\n", block.getLightValue(world, x, y, z));
		//tessellator.setBrightness(0xFFFFFF);
		
		Icon iconSide, iconTop, iconLabel;
		iconSide  = BlockBarrel.text_side;
		iconTop   = BlockBarrel.text_top;
		iconLabel = BlockBarrel.text_label;		
		
		double minXSide = iconSide.getMinU();
		double maxXSide = iconSide.getMaxU();
		double minYSide = iconSide.getMinV();
		double maxYSide = iconSide.getMaxV();

		double minXTop = iconTop.getMinU();
		double maxXTop = iconTop.getMaxU();
		double minYTop = iconTop.getMinV();
		double maxYTop = iconTop.getMaxV();

		double minXLabel = iconLabel.getMinU();
		double maxXLabel = iconLabel.getMaxU();
		double minYLabel = iconLabel.getMinV();
		double maxYLabel = iconLabel.getMaxV();			
		
		double xMin = x, xMax = x + 1;
		double yMin = y, yMax = y + 1;
		double zMin = z, zMax = z + 1;		
		
		boolean renderAll = renderer.renderAllFaces;

		boolean[] renderSide = {
				renderAll || y <= 0 || block.shouldSideBeRendered(world, x, y - 1, z, 0),
						renderAll || y >= worldHeight || block.shouldSideBeRendered(world, x, y + 1, z, 1),
						renderAll || block.shouldSideBeRendered(world, x, y, z - 1, 2),
						renderAll || block.shouldSideBeRendered(world, x, y, z + 1, 3),
						renderAll || block.shouldSideBeRendered(world, x - 1, y, z, 4),
						renderAll || block.shouldSideBeRendered(world, x + 1, y, z, 5),
		};		
		
		tessellator.setColorOpaque_F(1, 1, 1);
		
		if (renderSide[0])
		{ // DOWN
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y - 1, z));
			tessellator.addVertexWithUV(xMin, yMin, zMax, minXTop, minYTop);
			tessellator.addVertexWithUV(xMin, yMin, zMin, minXTop, maxYTop);
			tessellator.addVertexWithUV(xMax, yMin, zMin, maxXTop, maxYTop);
			tessellator.addVertexWithUV(xMax, yMin, zMax, maxXTop, minYTop);
		}
		
		if (renderSide[1])
		{ // UP
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y + 1, z));
			tessellator.addVertexWithUV(xMin, yMax, zMin, minXTop, minYTop);
			tessellator.addVertexWithUV(xMin, yMax, zMax, minXTop, maxYTop);
			tessellator.addVertexWithUV(xMax, yMax, zMax, maxXTop, maxYTop);
			tessellator.addVertexWithUV(xMax, yMax, zMin, maxXTop, minYTop);
		}
		
		if (renderSide[2])
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z - 1));
			tessellator.addVertexWithUV(xMax, yMax, zMin, minXSide, minYSide);
			tessellator.addVertexWithUV(xMax, yMin, zMin, minXSide, maxYSide);
			tessellator.addVertexWithUV(xMin, yMin, zMin, maxXSide, maxYSide);
			tessellator.addVertexWithUV(xMin, yMax, zMin, maxXSide, minYSide);
		}
		
		if (renderSide[3])
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z - 1));			
			tessellator.addVertexWithUV(xMin, yMax, zMax, minXSide, minYSide);
			tessellator.addVertexWithUV(xMin, yMin, zMax, minXSide, maxYSide);
			tessellator.addVertexWithUV(xMax, yMin, zMax, maxXSide, maxYSide);
			tessellator.addVertexWithUV(xMax, yMax, zMax, maxXSide, minYSide);
		}
		
		if (renderSide[4])
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x - 1 , y, z));			
			tessellator.addVertexWithUV(xMin, yMin, zMax, minXLabel, maxYLabel);
			tessellator.addVertexWithUV(xMin, yMax, zMax, minXLabel, minYLabel);
			tessellator.addVertexWithUV(xMin, yMax, zMin, maxXLabel, minYLabel);
			tessellator.addVertexWithUV(xMin, yMin, zMin, maxXLabel, maxYLabel);
		}
		
		if (renderSide[5])
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x + 1 , y, z));			
			tessellator.addVertexWithUV(xMax, yMin, zMin, minXSide, minYSide);
			tessellator.addVertexWithUV(xMax, yMax, zMin, minXSide, maxYSide);
			tessellator.addVertexWithUV(xMax, yMax, zMax, maxXSide, maxYSide);
			tessellator.addVertexWithUV(xMax, yMin, zMax, maxXSide, minYSide);
		}
		
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return true;
	}

	@Override
	public int getRenderId() {
		return mod_BetterBarrels.blockBarrelRendererID;
	}

}
