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
import java.util.logging.Level;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.DimensionManager;
import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.common.blocks.IBarrelStorage;
import mcp.mobius.betterbarrels.common.blocks.StorageLocal;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import mcp.mobius.betterbarrels.common.blocks.logic.Coordinates;
import mcp.mobius.betterbarrels.network.Packet0x01ContentUpdate;
import mcp.mobius.betterbarrels.network.Packet0x02GhostUpdate;

public class BSpaceStorageHandler {
	private int version = 1;
	
	public static BSpaceStorageHandler _instance = new BSpaceStorageHandler();
	private BSpaceStorageHandler(){}
	public static BSpaceStorageHandler instance() { return BSpaceStorageHandler._instance; }		

	private HashMap<Integer, Coordinates>     barrels = new HashMap<Integer, Coordinates>();
	
	// This variable should store a map between barrels and what storage to access.
	private HashMap<Integer, IBarrelStorage>  storageMap         = new HashMap<Integer, IBarrelStorage>();
	
	// This is the original storage map, established prior to linkage (used to restore barrels when the upgrade is removed)
	private HashMap<Integer, IBarrelStorage>  storageMapOriginal = new HashMap<Integer, IBarrelStorage>();	
	
	// Table of links to restore proper object sharing on load and transmit signals between barrels
	private HashMap<Integer, HashSet<Integer>>         links    = new HashMap<Integer, HashSet<Integer>>(); 
	
	private int maxBarrelID   = 0;

	public int getNextBarrelID(){
		this.maxBarrelID += 1;
		return this.maxBarrelID;
	}
	
	public void updateBarrel(int id, int dim, int x, int y, int z){
		this.barrels.put(id, new Coordinates(dim,x,y,z));
		this.writeToFile();
	}	

	public void registerEnderBarrel(int id, IBarrelStorage storage){
		this.storageMap.put(id, storage);
		this.storageMapOriginal.put(id, storage);
		this.writeToFile();		
	}
	
	public IBarrelStorage unregisterEnderBarrel(int id){
		IBarrelStorage storage = this.storageMapOriginal.get(id);
		this.storageMap.remove(id);
		this.storageMapOriginal.remove(id);
		this.unlinkStorage(id);
		
		this.writeToFile();
		return storage;
	}
	
	public IBarrelStorage getStorage(int id){
		return this.storageMap.get(id);
	}
	
	public IBarrelStorage getStorageOriginal(int id){
		return this.storageMapOriginal.get(id);
	}	
	
	public TileEntityBarrel getBarrel(int id){
		if (this.barrels.containsKey(id)){
			Coordinates coord = this.barrels.get(id);
			IBlockAccess world = DimensionManager.getWorld(coord.dim);
			if (world == null) return null;
			TileEntity te = world.getBlockTileEntity(MathHelper.floor_double(coord.x), MathHelper.floor_double(coord.y), MathHelper.floor_double(coord.z));
			if (!(te instanceof TileEntityBarrel)) return null;
			TileEntityBarrel barrel = (TileEntityBarrel)te;
			if (barrel.id != id) return null;
			return barrel;
		}
		return null;
	}

	// Need a way to get a stored inventory
	
	// Need a way to handle a request for a new inventory
	
	public void linkStorages(int sourceID, int targetID){
		this.unlinkStorage(targetID);
		
		this.storageMap.put(targetID, this.storageMap.get(sourceID));
		
		if (!links.containsKey(sourceID))
			links.put(sourceID, new HashSet<Integer>());
		
		// We create a new set for this barrel
		links.put(targetID, new HashSet<Integer>());

		// We remove all references of this target in the current link table
		for (HashSet<Integer> set : links.values())
			set.remove(targetID);
		
		// We add this target to the source
		links.get(sourceID).add(targetID);
		
		// We add the source, target and all previous targets to a tempo hashset
		HashSet<Integer> transferSet = new HashSet<Integer>();
		transferSet.add(sourceID);
		transferSet.add(targetID);
		transferSet.addAll(links.get(sourceID));
		
		// We update the hashset of all the elements in the source hashset and remove self referencing.
		for (Integer i : links.get(sourceID)){
			links.get(i).clear();
			links.get(i).addAll(transferSet);
			links.get(i).remove(i);
			
			TileEntityBarrel barrel = this.getBarrel(i);
			if (barrel != null)
				barrel.setLinked(true);
		}
		
		TileEntityBarrel source = this.getBarrel(sourceID);
		if (source != null)
			source.setLinked(true);			
		
		this.cleanUpLinks();
		this.writeToFile();			
	}
	
	private void cleanUpLinks(){
		// Finally, we cleanup the mess by removing barrels without link data anymore
		HashSet<Integer> keys = new HashSet<Integer>(links.keySet());
		for (Integer i : keys){
			if (links.get(i).size() == 0){
				links.remove(i);
				
				TileEntityBarrel barrel = this.getBarrel(i);
				if (barrel != null)
					barrel.setLinked(false);				
			}
		}		
	}
	
	public IBarrelStorage unlinkStorage(int sourceID){
		if (!this.links.containsKey(sourceID)) 
			return this.storageMapOriginal.get(sourceID);
		
		HashSet<Integer> copy = new HashSet<Integer>(this.links.get(sourceID));
		for (Integer targetID : copy) 
			this.links.get(targetID).remove(sourceID);
		
		this.links.remove(sourceID);

		TileEntityBarrel barrel = this.getBarrel(sourceID);
		if (barrel != null)
			barrel.setLinked(false);		
		
		this.cleanUpLinks();
		this.writeToFile();	
		
		return this.storageMapOriginal.get(sourceID);
	}
	
	private void relinkStorages(){
		for (Integer source : this.links.keySet())
			for (Integer target : this.links.get(source)){
				this.storageMap.put(target, this.storageMap.get(source));
			}
	}
	
	public boolean hasLinks(int sourceID){
		return this.links.containsKey(sourceID);
	}
	
	public void updateAllBarrels(int sourceID){
		if (!this.links.containsKey(sourceID)) return;
		
		TileEntityBarrel source = this.getBarrel(sourceID);
		if (source == null) return;
		
		for (Integer targetID : this.links.get(sourceID)){
			TileEntityBarrel target = this.getBarrel(targetID);
			if (target != null){
				target.getStorage().setGhosting(source.getStorage().isGhosting());
				PacketDispatcher.sendPacketToAllInDimension(Packet0x01ContentUpdate.create(target), target.worldObj.provider.dimensionId);
				PacketDispatcher.sendPacketToAllInDimension(Packet0x02GhostUpdate.create(target), target.worldObj.provider.dimensionId);
			}
		}
	}
	
	/*====================================*/
	/*            NBT HANDLING            */
	/*====================================*/
	
	private void writeToNBT(NBTTagCompound nbt){
		nbt.setInteger("version", this.version);
		nbt.setInteger("maxBarrelID",   this.maxBarrelID);

		NBTTagCompound   coords = new NBTTagCompound();
		for (Integer key : this.barrels.keySet())
			coords.setCompoundTag(String.valueOf(key), this.barrels.get(key).writeToNBT());		
		nbt.setTag("barrelCoords",   coords);

		NBTTagCompound   stores = new NBTTagCompound();
		for (Integer key : this.storageMap.keySet())
			stores.setCompoundTag(String.valueOf(key), this.storageMap.get(key).writeTagCompound());		
		nbt.setTag("storages",   stores);		

		NBTTagCompound   storesOriginal = new NBTTagCompound();
		for (Integer key : this.storageMapOriginal.keySet())
			storesOriginal.setCompoundTag(String.valueOf(key), this.storageMapOriginal.get(key).writeTagCompound());		
		nbt.setTag("storagesOriginal", storesOriginal);

		NBTTagCompound   list = new NBTTagCompound();
		for (Integer key : this.links.keySet())
			list.setIntArray(String.valueOf(key), this.convertInts(this.links.get(key)));
		nbt.setTag("links", list);
		
		
	}
	
	private void readFromNBT(NBTTagCompound nbt){
		this.maxBarrelID  = nbt.hasKey("maxBarrelID")  ? nbt.getInteger("maxBarrelID")  : 0;
		this.links = new HashMap<Integer, HashSet<Integer>>();
 		
		if (nbt.hasKey("barrelCoords")){
			for (Object obj : nbt.getCompoundTag("barrelCoords").getTags()){
				NBTTagCompound tag = (NBTTagCompound)obj;
				int key = Integer.parseInt(tag.getName());
				this.barrels.put(key, new Coordinates(tag));
			}			
		}
		
		if (nbt.hasKey("storages")){
			for (Object obj : nbt.getCompoundTag("storages").getTags()){
				NBTTagCompound tag = (NBTTagCompound)obj;
				int key = Integer.parseInt(tag.getName());
				this.storageMap.put(key, new StorageLocal(tag));
			}			
		}		
		
		if (nbt.hasKey("storagesOriginal")){
			for (Object obj : nbt.getCompoundTag("storagesOriginal").getTags()){
				NBTTagCompound tag = (NBTTagCompound)obj;
				int key = Integer.parseInt(tag.getName());
				this.storageMapOriginal.put(key, new StorageLocal(tag));
			}			
		}			
		
 		if (nbt.hasKey("links")){
 			for (Object obj : nbt.getCompoundTag("links").getTags()){
 				NBTTagIntArray tag = (NBTTagIntArray)obj;
 				int key = Integer.parseInt(tag.getName());
				this.links.put(key, this.convertHashSet(tag.intArray));
 			}
 			
 			this.relinkStorages();
 		}		
	}
	
	/*====================================*/
	/*            FILE HANDLING           */
	/*====================================*/
	
    private File saveDir;
    private File[] saveFiles;
    private int saveTo;    
    private NBTTagCompound saveTag;
    
	public void writeToFile(){
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;
		
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
        	/*
			for (StackTraceElement elem : e.getStackTrace()){
				BetterBarrels.log.log(Level.WARNING, String.format("%s.%s:%s",elem.getClassName(), elem.getMethodName(), elem.getLineNumber()));
			}
			BetterBarrels.log.log(Level.WARNING, String.format("Catched unhandled exception : %s",e));
			*/        	
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
        	/*
			for (StackTraceElement elem : e.getStackTrace()){
				BetterBarrels.log.log(Level.WARNING, String.format("%s.%s:%s",elem.getClassName(), elem.getMethodName(), elem.getLineNumber()));
			}
			BetterBarrels.log.log(Level.WARNING, String.format("Catched unhandled exception : %s",e));
			*/            	
            throw new RuntimeException(e);
        }
        
        this.readFromNBT(saveTag);
    }
    
	/*====================================*/
	/*          TYPE CONVERSION           */
	/*====================================*/    
    
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
