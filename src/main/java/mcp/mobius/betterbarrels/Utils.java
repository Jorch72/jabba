package mcp.mobius.betterbarrels;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.logging.log4j.Level;

public class Utils {
	public static void dropItemInWorld(TileEntity source, EntityPlayer player, ItemStack stack, double speedfactor) {
		int hitOrientation = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		double stackCoordX = 0.0D, stackCoordY = 0.0D, stackCoordZ = 0.0D;

		switch (hitOrientation) {
		case 0:
			stackCoordX = source.xCoord + 0.5D;
			stackCoordY = source.yCoord + 0.5D;
			stackCoordZ = source.zCoord - 0.25D;
			break;
		case 1:
			stackCoordX = source.xCoord + 1.25D;
			stackCoordY = source.yCoord + 0.5D;
			stackCoordZ = source.zCoord + 0.5D;
			break;
		case 2:
			stackCoordX = source.xCoord + 0.5D;
			stackCoordY = source.yCoord + 0.5D;
			stackCoordZ = source.zCoord + 1.25D;
			break;
		case 3:
			stackCoordX = source.xCoord - 0.25D;
			stackCoordY = source.yCoord + 0.5D;
			stackCoordZ = source.zCoord + 0.5D;
			break;
		}

		EntityItem droppedEntity = new EntityItem(source.getWorldObj(), stackCoordX, stackCoordY, stackCoordZ, stack);

		if (player != null) {
			Vec3 motion = Vec3.createVectorHelper(player.posX - stackCoordX, player.posY - stackCoordY, player.posZ - stackCoordZ);
			motion.normalize();
			droppedEntity.motionX = motion.xCoord;
			droppedEntity.motionY = motion.yCoord;
			droppedEntity.motionZ = motion.zCoord;
			double offset = 0.25D;
			droppedEntity.moveEntity(motion.xCoord * offset, motion.yCoord * offset, motion.zCoord * offset);
		}

		droppedEntity.motionX *= speedfactor;
		droppedEntity.motionY *= speedfactor;
		droppedEntity.motionZ *= speedfactor;

		source.getWorldObj().spawnEntityInWorld(droppedEntity);
	}

	public static ForgeDirection getDirectionFacingEntity(EntityLivingBase player, boolean allowVertical) {
		Vec3 playerLook = player.getLookVec();

		if (allowVertical) {
			if (playerLook.yCoord <=  -BetterBarrels.verticalPlacementRange) {
				return ForgeDirection.UP;
			} else if (playerLook.yCoord >=  BetterBarrels.verticalPlacementRange) {
				return ForgeDirection.DOWN;
			}
		}

		if (Math.abs(playerLook.xCoord) >= Math.abs(playerLook.zCoord)) {
			if (playerLook.xCoord > 0)
				return ForgeDirection.WEST;
			else
				return ForgeDirection.EAST;
		} else {
			if (playerLook.zCoord > 0)
				return ForgeDirection.NORTH;
			else
				return ForgeDirection.SOUTH;			
		}
	}

	public static class Material {
		public String name;
		public String modDomain;
		public int meta = -1;

		public Material(String in) {
			if (in.contains("Ore.")) {
				name = in.split("\\.")[1];
			} else if (in.contains(":")) {
				int splitCh = in.indexOf(':');

				modDomain = in.substring(0, splitCh);
				String itemStr = in.substring(splitCh + 1, in.length());

				int metaCh = itemStr.indexOf(':');
				int wildCh = itemStr.indexOf('*');

				if (metaCh >= 0) {
					if (wildCh == metaCh + 1) {
						meta = OreDictionary.WILDCARD_VALUE;
					} else {
						meta = Integer.parseInt(itemStr.substring(metaCh + 1, itemStr.length()));
					}

					name = itemStr.substring(0, metaCh);
				} else {
					name = itemStr;
					meta = 0;
				}
			} else {
				BetterBarrels.log.error("Unable to parse input string into oreDict or item:" + in);
			}
		}

		public boolean isOreDict() {
			return this.name != null && this.modDomain == null;
		}

		public ItemStack getStack() {
			ItemStack ret = new ItemStack(Blocks.portal);
			if (this.isOreDict()) {
				ArrayList<ItemStack> ores = OreDictionary.getOres(this.name);

				if (ores.size() > 0) {
					ret = ores.get(0);
				}
				BetterBarrels.debug("05 - Looking up [" + this.name + "] and found: " + ret.getDisplayName());
			} else {
				try {
					ret = new ItemStack((Item)Item.itemRegistry.getObject(modDomain + ":" + name), 1, this.meta);
					BetterBarrels.debug("05 - Looking up [" + (this.modDomain + ":" + this.name + ":" + this.meta) + "] and found: " + ret.getDisplayName());
				} catch (Throwable t) {
					BetterBarrels.log.error("Error while trying to initialize material with name " + (this.modDomain + ":" + this.name + ":" + this.meta));
				}
			}
			return ret;
		}
	}

	public static class ReflectionHelper {
		static public Method getMethod(Class targetClass, String[] targetNames, Class[] targetParams) {
			return getMethod(targetClass, targetNames, targetParams, Level.ERROR, "Unable to reflect requested method[" + targetNames.toString() + "] with a paramter signature of [" + targetParams.toString() + "] in class[" + targetClass.getCanonicalName() + "]");
		}
		static public Method getMethod(Class targetClass, String[] targetNames, Class[] targetParams, Level errorLevel, String errorMessage) {
			Method foundMethod = null;
			for (String methodName : targetNames) {
				try {
					foundMethod = targetClass.getDeclaredMethod(methodName, targetParams);
					if (foundMethod != null) {
						foundMethod.setAccessible(true);
						break;
					}
				} catch (Throwable t) {
				}
			}
			if (foundMethod == null && errorMessage != null) {
				BetterBarrels.log.log(errorLevel, errorMessage);
			}
			return foundMethod;
		}

		static public Field getField(Class targetClass, String[] targetNames) {
			return getField(targetClass, targetNames, Level.ERROR, "Unable to reflect requested field[" + targetNames.toString() + "] in class[" + targetClass.getCanonicalName() + "]");
		}
		static public Field getField(Class targetClass, String[] targetNames, Level errorLevel, String errorMessage) {
			Field foundField = null;
			for (String fieldName : targetNames) {
				try {
					foundField = targetClass.getDeclaredField(fieldName);
					if (foundField != null) {
						foundField.setAccessible(true);
						break;
					}
				} catch (Throwable t) {
				}
			}
			if (foundField == null && errorMessage != null) {
				BetterBarrels.log.log(errorLevel, errorMessage);
			}
			return foundField;
		}

		static public <T> T getFieldValue(Class<T> returnType, Object targetObject, Class targetClass, String[] targetNames) {
			if (!returnType.isPrimitive()) {
				return getFieldValue(returnType, null, targetObject, targetClass, targetNames, Level.ERROR, "Unable to reflect and return value for requested field[" + targetNames.toString() + "] in class[" + targetClass.getCanonicalName() + "], defaulting to null or 0");
			} else {
				return getFieldValue(returnType, returnType.cast(0), targetObject, targetClass, targetNames, Level.ERROR, "Unable to reflect and return value for requested field[" + targetNames.toString() + "] in class[" + targetClass.getCanonicalName() + "], defaulting to null or 0");
			}
		}
		static public <T> T getFieldValue(Class<T> returnType, T errorValue, Object targetObject, Class targetClass, String[] targetNames, Level errorLevel, String errorMessage) {
			T returnValue = errorValue;
			Field foundField = getField(targetClass, targetNames, errorLevel, errorMessage);
			if (foundField != null) {
				try {
					returnValue = returnType.cast(foundField.get(targetObject));
					BetterBarrels.debug("Reflected field [" + foundField.getName() + "] and found value [" + returnValue + "], had a backup value of " + errorValue);
				} catch (Throwable t) {
					BetterBarrels.log.error("Unable to cast found field [" + foundField.getName() + "] to return type [" + returnType.getName() + "]. Defaulting to provided error value.");
				}
			}
			return returnValue;
		}
	}
}
