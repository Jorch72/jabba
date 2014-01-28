package mcp.mobius.betterbarrels.server;

import java.util.HashSet;
import java.util.Set;

public class BSpaceStorageHandler {

	public final class Coordinates{
		public final int dim, x, y, z;
		public Coordinates(int dim, int x, int y, int z){
			this.dim = dim; this.x = x; this.y = y; this.z = z;
		}
		
		public boolean equals(Object o)  {
			Coordinates c = (Coordinates)o;
			return (this.dim == c.dim) && (this.x == c.x) && (this.y == c.y) && (this.z == c.z);
		};
		
		public int hashCode() {
			return this.dim + 31 * this.x + 877 * this.y + 3187 * this.z;
		}
	}
	
	public static BSpaceStorageHandler _instance = new BSpaceStorageHandler();
	private BSpaceStorageHandler(){}
	public static BSpaceStorageHandler instance() { return BSpaceStorageHandler._instance; }		

	public Set<Coordinates> registeredStorages = new HashSet<Coordinates>();
	
	public void registerStorage(int dim, int x, int y, int z){
		this.registeredStorages.add(new Coordinates(dim,x,y,z));
		System.out.printf("Registered new storage at %d %d %d %d", dim, x, y, z);
	}
	
	public void removeStorage(int dim, int x, int y, int z){
		this.registeredStorages.remove(new Coordinates(dim,x,y,z));
		System.out.printf("Removed storage at %d %d %d %d", dim, x, y, z);		
	}
}
