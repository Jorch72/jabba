package mcp.mobius.betterbarrels.bspace;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;
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

	
    private File saveDir;
    private File[] saveFiles;
    private int saveTo;    
    private NBTTagCompound saveTag;
    
	public void writeToFile(){
        try
        {
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
    }	
}
