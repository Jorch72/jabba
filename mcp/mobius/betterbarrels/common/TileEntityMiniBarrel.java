package mcp.mobius.betterbarrels.common;

import net.minecraft.nbt.NBTTagCompound;

public class TileEntityMiniBarrel extends TileEntityBarrel {

    public TileEntityMiniBarrel(){
    	this.storage = new StorageLocal();
    }

    public TileEntityMiniBarrel(int stacks, int slots){
    	this.storage = new StorageLocal(stacks, slots);
    } 	
}
