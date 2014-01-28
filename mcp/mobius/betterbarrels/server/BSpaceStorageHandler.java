package mcp.mobius.betterbarrels.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import mcp.mobius.betterbarrels.common.blocks.logic.Coordinates;

public class BSpaceStorageHandler {
	
	public static BSpaceStorageHandler _instance = new BSpaceStorageHandler();
	private BSpaceStorageHandler(){}
	public static BSpaceStorageHandler instance() { return BSpaceStorageHandler._instance; }		

	public HashMap<Integer, Coordinates> registeredStorages = new HashMap<Integer, Coordinates>();
	
	private int topID = 0;
	
	public int getNextID(){
		this.topID += 1;
		return this.topID;
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
	}	
	
	public void removeStorage(int id){
		this.registeredStorages.remove(id);
		System.out.printf("Removed storage with id %d\n", id);		
	}
}
