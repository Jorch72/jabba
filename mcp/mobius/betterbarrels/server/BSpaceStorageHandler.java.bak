package mcp.mobius.betterbarrels.server;

import java.util.Collection;
import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;

import mcp.mobius.betterbarrels.mod_BetterBarrels;
import mcp.mobius.betterbarrels.common.IBarrelStorage;
import mcp.mobius.betterbarrels.common.StorageLocal;

public class BSpaceStorageHandler {

	HashMap<Integer, IBarrelStorage>  storageMap = null;
	public static BSpaceStorageHandler instance;
	public static int currentID;
	public static boolean loaded = false;
	
	public BSpaceStorageHandler(){
		this.instance = this;
		this.storageMap = new HashMap<Integer, IBarrelStorage>();
		this.currentID = 0;
	}
	
	public int getNextID(){
		//return this.storageMap.size() + 1;
		return this.currentID + 1;
	}
	
	public boolean hasStorage(int ID){
		return this.storageMap.containsKey(ID);
	}
	
	public IBarrelStorage getStorage(int ID){
		//if (!this.storageMap.containsKey(ID))
		//	this.storageMap.put(ID, new StorageLocal(ID));
		return this.storageMap.get(ID);
	}
	
	public IBarrelStorage getNewStorage(){
		IBarrelStorage storage = new StorageLocal(this.getNextID());
		this.storageMap.put(this.getNextID(), storage);
		this.currentID += 1;
		return storage;
	}
	
	public IBarrelStorage setStorage (int ID, IBarrelStorage storage){
		this.storageMap.put(ID, storage);
		return this.getStorage(ID);
	}
	
	public NBTTagCompound writeToNBT(){
		NBTTagCompound retTag = new NBTTagCompound();
		
		retTag.setCompoundTag("params", new NBTTagCompound());
		retTag.getCompoundTag("params").setInteger("currentID", this.currentID);
		retTag.setCompoundTag("data", new NBTTagCompound());
		
		for (int storageID : storageMap.keySet()){
			retTag.getCompoundTag("data").setCompoundTag(String.valueOf(storageID), this.storageMap.get(storageID).writeTagCompound());
		}
		
		return retTag;
	}
	
	public void readFromNBT(NBTTagCompound loadTag){
		
		this.storageMap = new HashMap<Integer, IBarrelStorage>();
		this.currentID  = 0;
		
		//COMPATIBILITY : We only load the tags if they are present
		if (!loadTag.hasKey("params")){return;}
			
		this.currentID = loadTag.getCompoundTag("params").getInteger("currentID");
		
		for (NBTTagCompound tag : (Collection<NBTTagCompound>)loadTag.getCompoundTag("data").getTags()){
			int storageID = tag.getInteger("StorageID");
			StorageLocal storage = new StorageLocal(tag);
			this.setStorage(storageID, storage);
		}
		
	}
}
