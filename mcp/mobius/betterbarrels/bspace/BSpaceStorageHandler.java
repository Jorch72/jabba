package mcp.mobius.betterbarrels.bspace;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.DimensionManager;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import mcp.mobius.betterbarrels.common.blocks.logic.Coordinates;

public class BSpaceStorageHandler {
	private int version = 1;
	
	public static BSpaceStorageHandler _instance = new BSpaceStorageHandler();
	private BSpaceStorageHandler(){}
	public static BSpaceStorageHandler instance() { return BSpaceStorageHandler._instance; }		

	public HashMap<Integer, Coordinates> registeredStorages = new HashMap<Integer, Coordinates>();
	public HashMap<Integer, HashSet<Integer>> links          = new HashMap<Integer, HashSet<Integer>>(); 
	
	private int maxID   = 0;
	
	public int getNextID(){
		this.maxID += 1;
		return this.maxID;
	}
	
	/*
	public int registerStorage(int dim, int x, int y, int z){
		this.registeredStorages.put(++id, new Coordinates(dim,x,y,z));
		System.out.printf("Registered new storage at %d %d %d %d with id %d\n", dim, x, y, z, id);
		return id;
	}
	*/
	
	public void updateStorage(int id, int dim, int x, int y, int z){
		this.registeredStorages.put(id, new Coordinates(dim,x,y,z));
		this.writeToFile();
	}	
	
	public void removeStorage(int id){
		/*
		this.registeredStorages.remove(id);
		this.writeToFile();
		*/		
	}

	public void linkStorages(int source, int target){

		if (!links.containsKey(source))
			links.put(source, new HashSet<Integer>());
		
		// We create a new set for this barrel
		links.put(target, new HashSet<Integer>());

		// We remove all references of this target in the current link table
		for (HashSet<Integer> set : links.values())
			set.remove(target);
		
		// We add this target to the source
		links.get(source).add(target);
		
		// We add the source, target and all previous targets to a tempo hashset
		HashSet<Integer> transferSet = new HashSet<Integer>();
		transferSet.add(source);
		transferSet.add(target);
		transferSet.addAll(links.get(source));
		
		// We update the hashset of all the elements in the source hashset and remove self referencing.
		for (Integer i : links.get(source)){
			links.get(i).clear();
			links.get(i).addAll(transferSet);
			links.get(i).remove(i);
			
			TileEntityBarrel b = BSpaceStorageHandler.instance().getBarrel(i);
			if (b != null)
				b.isLinked = true;
		}
		
		// Finally, we cleanup the mess by removing barrels without link data anymore
		for (Integer i : links.keySet()){
			if (links.get(i).size() == 0)
				links.remove(i);
		}
		
		this.writeToFile();		
	}
	
	public TileEntityBarrel getBarrel(int id){
		if (this.registeredStorages.containsKey(id)){
			Coordinates coord = this.registeredStorages.get(id);
			IBlockAccess world = DimensionManager.getWorld(coord.dim);
			if (world == null) return null;
			TileEntity   te    = world.getBlockTileEntity(MathHelper.floor_double(coord.x), MathHelper.floor_double(coord.y), MathHelper.floor_double(coord.z));
			if (!(te instanceof TileEntityBarrel)) return null;
			TileEntityBarrel barrel = (TileEntityBarrel)te;
			if (barrel.id != id) return null;
			return barrel;
		}
		return null;
	}
	
	private void writeToNBT(NBTTagCompound nbt){
		nbt.setInteger("maxID",   this.maxID);
		nbt.setInteger("version", this.version);
		
		NBTTagCompound   list = new NBTTagCompound();
		for (Integer key : this.links.keySet())
			list.setIntArray(String.valueOf(key), this.convertInts(this.links.get(key)));

		NBTTagCompound   coords = new NBTTagCompound();
		for (Integer key : this.registeredStorages.keySet())
			coords.setCompoundTag(String.valueOf(key), this.registeredStorages.get(key).writeToNBT());		
		
		nbt.setTag("links", list);
		nbt.setTag("IDs",   coords);
	}
	
	private void readFromNBT(NBTTagCompound nbt){
		this.maxID = nbt.hasKey("maxID") ? nbt.getInteger("maxID") : 0;
		this.links = new HashMap<Integer, HashSet<Integer>>();
		
		if (nbt.hasKey("links"))
			for (Object obj : nbt.getCompoundTag("links").getTags()){
				NBTTagIntArray tag = (NBTTagIntArray)obj;
				int key = Integer.parseInt(tag.getName());
				this.links.put(key, this.convertHashSet(tag.intArray));
			}
		
		if (nbt.hasKey("IDs")){
			for (Object obj : nbt.getCompoundTag("IDs").getTags()){
				NBTTagCompound tag = (NBTTagCompound)obj;
				int key = Integer.parseInt(tag.getName());
				this.registeredStorages.put(key, new Coordinates(tag));
			}			
		}
	}
	
	/* FILE HANDLING */
	
    private File saveDir;
    private File[] saveFiles;
    private int saveTo;    
    private NBTTagCompound saveTag;
    
	public void writeToFile(){
        try
        {
        	this.writeToNBT(saveTag);
        	
            File saveFile = saveFiles[saveTo];
            if(!saveFile.exists())
                saveFile.createNewFile();
            DataOutputStream dout = new DataOutputStream(new FileOutputStream(saveFile));
            CompressedStreamTools.writeCompressed(saveTag, dout);
            dout.close();
            FileOutputStream fout = new FileOutputStream(saveFiles[2]);
            fout.write(saveTo);
            fout.close();
            saveTo^=1;
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
	}
	
    public void loadFromFile()
    {
    	System.out.printf("Attemping to load JABBA data.\n");
    	
        saveDir = new File(DimensionManager.getCurrentSaveRootDirectory(), "JABBA");
        try
        {
            if(!saveDir.exists())
                saveDir.mkdirs();
            saveFiles = new File[]{new File(saveDir, "data1.dat"), new File(saveDir, "data2.dat"), new File(saveDir, "lock.dat")};
            if(saveFiles[2].exists() && saveFiles[2].length() > 0)
            {
                FileInputStream fin = new FileInputStream(saveFiles[2]);
                saveTo = fin.read()^1;
                fin.close();
                
                if(saveFiles[saveTo^1].exists())
                {
                    DataInputStream din = new DataInputStream(new FileInputStream(saveFiles[saveTo^1]));
                    saveTag = CompressedStreamTools.readCompressed(din);
                    din.close();
                }
                else
                {
                    saveTag = new NBTTagCompound();
                }
            }
            else
                saveTag = new NBTTagCompound();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
        
        this.readFromNBT(saveTag);
    }
    
    
    private int[] convertInts(Set<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }
    
    private HashSet<Integer> convertHashSet(int[] list)
    {
    	HashSet<Integer> ret = new HashSet<Integer>();
    	for (int i = 0; i < list.length; i++)
    		ret.add(list[i]);
    	return ret;

    }    
}
