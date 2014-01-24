package mcp.mobius.betterbarrels.server;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import mcp.mobius.betterbarrels.mod_BetterBarrels;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class SaveHandler {

	public static void saveData(){
		WorldServer worldServer = DimensionManager.getWorld(0);
		if (worldServer == null){
			mod_BetterBarrels.log.log(Level.WARNING, "Error while trying to save bspace data. Dimension 0 is null");
			return;
		}
		
        try {
            File saveFile = new File(worldServer.getChunkSaveLocation(), "betterbarrel.dat");
            if(saveFile.exists()) {
               File backupFile = new File(worldServer.getChunkSaveLocation(), "betterbarrel_backup.dat");
               if(backupFile.exists()) {
                  if(backupFile.delete()) {
                     saveFile.renameTo(backupFile);
                  }
               } else {
                  saveFile.renameTo(backupFile);
               }
            }

            CompressedStreamTools.writeCompressed(BSpaceStorageHandler.instance.writeToNBT(), new FileOutputStream(saveFile));
         } catch (IOException e) {
             e.printStackTrace();
             throw new RuntimeException("Failed to save betterbarrel data");
         } catch (NullPointerException e) {
	         e.printStackTrace();
	         throw new RuntimeException("Error while trying to save bspace data. Something is null");        	 
         }
		
	};
	
	public static void loadData(){
		
		WorldServer worldServer = DimensionManager.getWorld(0);		
		if (worldServer == null){
			mod_BetterBarrels.log.log(Level.WARNING, "Error while trying to load bspace data. Dimension 0 is null");
			return;
		}		
		
		try {
			File saveFile = new File(worldServer.getChunkSaveLocation(), "betterbarrel.dat");
			if(!saveFile.exists()) {
				System.out.printf("BETTERBARRELS : Missing save file !\n");
			}

			if (!BSpaceStorageHandler.loaded){
				BSpaceStorageHandler.instance.readFromNBT(CompressedStreamTools.readCompressed(new FileInputStream(saveFile)));
				BSpaceStorageHandler.loaded = true;
			}
			
	       } catch (EOFException e) {
	    	   System.out.printf("BETTERBARRELS : Corrupted save file !\n");
	    	   /*
	    	   try {
	             File backupFile = new File(worldServer.getChunkSaveLocation(), "portalgun_backup.dat");
	             if(!backupFile.exists()) {
	                return;
	             }

	 			 BSpaceStorageHandler.instance.readFromNBT(CompressedStreamTools.readCompressed(new FileInputStream(backupFile)));
	             File var4 = new File(var1.getChunkSaveLocation(), "portalgun.dat");
	             var4.delete();
	             backupFile.renameTo(var4);
	             PortalGun.console("Restoring data from backup.");
	          } catch (Exception var5) {
	             this.data = new NBTTagCompound();
	             PortalGun.console("Even your backup data is corrupted. What have you been doing?!", true);
	          }
	          */
	       } catch (IOException var7) {
	    	   System.out.printf("BETTERBARRELS : Error reading save file !\n");	    	   
	       } catch (NullPointerException e) {
	    	   e.printStackTrace();
	    	   throw new RuntimeException("Error while trying to load bspace data. Something is null");        	 
	       }		
	}
	
}
