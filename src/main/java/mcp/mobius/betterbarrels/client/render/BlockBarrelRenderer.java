package mcp.mobius.betterbarrels.client.render;

import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import mcp.mobius.betterbarrels.common.items.upgrades.StructuralLevel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class BlockBarrelRenderer implements ISimpleBlockRenderingHandler {

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
		Tessellator tessellator = Tessellator.instance;
		
		IIcon iconSide, iconTop, iconLabel;
		iconSide     = StructuralLevel.LEVELS[0].getIconSide();
		iconTop      = StructuralLevel.LEVELS[0].getIconTop();
		iconLabel    = StructuralLevel.LEVELS[0].getIconLabel();

		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, iconTop);
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, iconTop);
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, iconSide);
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, iconSide);
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, iconSide);
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, iconLabel);
		tessellator.draw();
	}
	
	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block tile, int modelId, RenderBlocks renderer) {
		TileEntityBarrel barrel = (TileEntityBarrel) world.getTileEntity(x, y, z);

		barrel.overlaying = false;
		boolean renderedBarrel = renderer.renderStandardBlock(tile, x, y, z);
		barrel.overlaying = true;
		boolean renderedOverlay = renderer.renderStandardBlock(tile, x, y, z);
		barrel.overlaying = false;
		
		return renderedBarrel || renderedOverlay;
	}
	
	@Override
	public boolean shouldRender3DInInventory(int modelID) {
		return true;
	}

	@Override
	public int getRenderId() {
		return BetterBarrels.blockBarrelRendererID;
	}

}
