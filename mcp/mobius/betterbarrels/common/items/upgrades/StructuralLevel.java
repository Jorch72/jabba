package mcp.mobius.betterbarrels.common.items.upgrades;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import mcp.mobius.betterbarrels.BetterBarrels;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.ReloadableResourceManager;
import net.minecraft.client.resources.ResourceManager;
import net.minecraft.client.resources.ResourceManagerReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringTranslate;
import net.minecraftforge.oredict.OreDictionary;

public class StructuralLevel {
   public static String[] upgradeMaterialsList = new String[]{ "Ore.plankWood", "Ore.ingotIron", "Ore.ingotGold", "Ore.gemDiamond", "Ore.obsidian", "Ore.whiteStone", "Ore.gemEmerald" };
   public static int maxCraftableTier = upgradeMaterialsList.length;
   public static StructuralLevel[] LEVELS;

   private static boolean initialized = false;

   private String name;
   private ItemStack materialStack;
   private TextureAtlasSprite iconBlockSide;
   private TextureAtlasSprite iconBlockLabel;
   private TextureAtlasSprite iconBlockTop;
   private TextureAtlasSprite iconBlockTopLabel;
   private TextureAtlasSprite iconItem;
   private int textColor;
   private int maxCoreSlots;

   // Define field we are refelcting for
   private static ReloadableResourceManager rm = null;
   // Reflect (1.6.4: Vanilla, SRG, and DeObf)
   static {
      for (String field: new String[]{ "ao", "field_110451_am", "mcResourceManager" }) {
         try {
            Field fResourceManager = Minecraft.getMinecraft().getClass().getDeclaredField(field);
            if (fResourceManager != null) {
               fResourceManager.setAccessible(true);
               StructuralLevel.rm = (ReloadableResourceManager)fResourceManager.get(Minecraft.getMinecraft());
               break;
            }
         } catch (Exception e) {
         }
      }
   }

   private StructuralLevel() {
      // Special case for base barrel with no upgrade
      this.textColor = 0xFFFFFFFF;
      this.maxCoreSlots = 0;
   }

   private StructuralLevel(String oreDictMaterial, final int level) {
      ArrayList<ItemStack> ores = OreDictionary.getOres(oreDictMaterial.split("\\.")[1]);
      ItemStack firstOreItem = ores.get(0);

      this.materialStack = firstOreItem;
      this.name = materialStack.getDisplayName();

      this.maxCoreSlots = 0;
      for (int i = 0; i < level; i++)
         this.maxCoreSlots += MathHelper.floor_double(Math.pow(2, i));

      // Use Reflected field found earlier
      if (StructuralLevel.rm != null) {
         StructuralLevel.rm.registerReloadListener(new ResourceManagerReloadListener() {
            private boolean ranOnce = false;

            @Override
            public void onResourceManagerReload(ResourceManager resourcemanager) {
               if (!ranOnce) { // The MC engine calls this several times, ignore the first
                  ranOnce = true;
                  return;
               }
               StringTranslate.inject(new ByteArrayInputStream(("item.upgrade.structural." + String.valueOf(level) + ".name=" + StatCollector.translateToLocal("item.upgrade.structural") + " " + romanNumeral(level) + " (" + name + ")").getBytes()));
               generateIcons();
            }
         });
      }
   }

   public static void createAndRegister() {
      if (initialized) return;
      LEVELS = new StructuralLevel[Math.min(24, upgradeMaterialsList.length) + 1];
      LEVELS[0] = new StructuralLevel();
      for (int i = 1; i < LEVELS.length; i++) {
         LEVELS[i] = new StructuralLevel(upgradeMaterialsList[i - 1], i);
      }
      initialized = true;
   }

   public Icon getIconSide() {
      return this.iconBlockSide;
   }

   public Icon getIconTop() {
      return this.iconBlockTop;
   }

   public Icon getIconLabel() {
      return this.iconBlockLabel;
   }

   public Icon getIconLabelTop() {
      return this.iconBlockTopLabel;
   }

   public Icon getIconItem() {
      return this.iconItem;
   }

   public int getTextColor() {
      return this.textColor;
   }

   public int getMaxCoreSlots() {
      return this.maxCoreSlots;
   }

   // Begin the crazy icon stuff
   /* Basic process:
    * 
    * register dummy icons, this is to get entries and values(position offsets, etc..) into the texture sheet
    * use opengl functions to access the texture sheet and read the base pieces
    * manipulate as desired
    * use opengl to replace modified array into original texture sheet since or registered Icons just store offsets to the texture sheet
    * End result: final icon used is dynamically generated at runtime, at every resource manager reload
    */
   private static TextureAtlasSprite iconLabelBackground;
   private static TextureAtlasSprite iconLabelBorder;
   private static TextureAtlasSprite iconTopBackground;
   private static TextureAtlasSprite iconTopBorder;
   private static TextureAtlasSprite iconTopLabel;
   private static TextureAtlasSprite iconSideBackground;
   private static TextureAtlasSprite iconSideBorder;
   private static TextureAtlasSprite iconItemBase;
   private static TextureAtlasSprite iconItemArrow;

   private static class AccessibleTextureAtlasSprite extends TextureAtlasSprite {
      AccessibleTextureAtlasSprite(String par1Str) {
         super(par1Str);
      }
   }

   private static TextureAtlasSprite registerIcon(IconRegister par1IconRegister, String key) {
      TextureAtlasSprite ret = new AccessibleTextureAtlasSprite(key);
      ((TextureMap)par1IconRegister).setTextureEntry(key, ret);
      return ret;
   }

   public static void registerItemIconPieces(IconRegister par1IconRegister) {
      StructuralLevel.iconItemBase = StructuralLevel.registerIcon(par1IconRegister, BetterBarrels.modid + ":capaupg_base");
      StructuralLevel.iconItemArrow = StructuralLevel.registerIcon(par1IconRegister, BetterBarrels.modid + ":capaupg_color");
   }

   public static void registerBlockIconPieces(IconRegister par1IconRegister) {
      StructuralLevel.iconLabelBackground = StructuralLevel.registerIcon(par1IconRegister, BetterBarrels.modid + ":barrel_label_background");
      StructuralLevel.iconLabelBorder = StructuralLevel.registerIcon(par1IconRegister, BetterBarrels.modid + ":barrel_label_border");
      StructuralLevel.iconTopBackground = StructuralLevel.registerIcon(par1IconRegister, BetterBarrels.modid + ":barrel_top_background");
      StructuralLevel.iconTopBorder = StructuralLevel.registerIcon(par1IconRegister, BetterBarrels.modid + ":barrel_top_border");
      StructuralLevel.iconTopLabel = StructuralLevel.registerIcon(par1IconRegister, BetterBarrels.modid + ":barrel_top_label");
      StructuralLevel.iconSideBackground = StructuralLevel.registerIcon(par1IconRegister, BetterBarrels.modid + ":barrel_side_background");
      StructuralLevel.iconSideBorder = StructuralLevel.registerIcon(par1IconRegister, BetterBarrels.modid + ":barrel_side_border");
   }

   public void registerItemIcon(IconRegister par1IconRegister, int ordinal) {
      this.iconItem = StructuralLevel.registerIcon(par1IconRegister, BetterBarrels.modid + ":blanks/capacity/" + String.valueOf(ordinal));
   }

   public void registerBlockIcons(IconRegister par1IconRegister, int ordinal) {
      if (ordinal > 0) {
         this.iconBlockSide = StructuralLevel.registerIcon(par1IconRegister, BetterBarrels.modid + ":blanks/side/" + String.valueOf(ordinal));
         this.iconBlockLabel = StructuralLevel.registerIcon(par1IconRegister, BetterBarrels.modid + ":blanks/label/" + String.valueOf(ordinal));
         this.iconBlockTop = StructuralLevel.registerIcon(par1IconRegister, BetterBarrels.modid + ":blanks/top/" + String.valueOf(ordinal));
         this.iconBlockTopLabel = StructuralLevel.registerIcon(par1IconRegister, BetterBarrels.modid + ":blanks/toplabel/" + String.valueOf(ordinal));
      } else {
         this.iconBlockSide = (TextureAtlasSprite)par1IconRegister.registerIcon(BetterBarrels.modid + ":barrel_side_" + String.valueOf(ordinal));
         this.iconBlockTop = (TextureAtlasSprite)par1IconRegister.registerIcon(BetterBarrels.modid + ":barrel_top_" + String.valueOf(ordinal));
         this.iconBlockLabel = (TextureAtlasSprite)par1IconRegister.registerIcon(BetterBarrels.modid + ":barrel_label_" + String.valueOf(ordinal));
         this.iconBlockTopLabel = (TextureAtlasSprite)par1IconRegister.registerIcon(BetterBarrels.modid + ":barrel_labeltop_" + String.valueOf(ordinal));
      }
   }

   private String romanNumeral(int num) {
      LinkedHashMap<String, Integer> numeralConversion = new LinkedHashMap<String, Integer>();
      numeralConversion.put("M", 1000);
      numeralConversion.put("CM", 900);
      numeralConversion.put("D", 500);
      numeralConversion.put("CD", 400);
      numeralConversion.put("C", 100);
      numeralConversion.put("XC", 90);
      numeralConversion.put("L", 50);
      numeralConversion.put("XL", 40);
      numeralConversion.put("X", 10);
      numeralConversion.put("IX", 9);
      numeralConversion.put("V", 5);
      numeralConversion.put("IV", 4);
      numeralConversion.put("I", 1);

      String result = new String();

      while (numeralConversion.size() > 0) {
         String romanKey = (String)numeralConversion.keySet().toArray()[0];
         Integer arabicValue = (Integer)numeralConversion.values().toArray()[0];
         if (num < arabicValue) {
            numeralConversion.remove(romanKey);
         } else {
            num -= arabicValue;
            result += romanKey;
         }
      }

      return result;
   }

   private class PixelARGB {
      int A, R, G, B;
      int combined;
      private int addCount = 0;

      PixelARGB(final int pixel) {
         A = (pixel >> 24) & 0xFF;
         R = (pixel >> 16) & 0xFF;
         G = (pixel >> 8) & 0xFF;
         B = pixel & 0xFF;
         combined = pixel;
      }

      PixelARGB(final int alpha, final int red, final int green, final int blue) {
         A = alpha;
         R = red;
         G = green;
         B = blue;
         combined = ((A & 0xFF) << 24) + ((R & 0xFF) << 16) + ((G & 0xFF) << 8) + (B & 0xFF);
      }

      PixelARGB alphaAdd(PixelARGB add) {
         addCount++;
         A += add.A;
         R += (add.R * add.A) / 255;
         G += (add.G * add.G) / 255;
         B += (add.B * add.B) / 255;
         combined = ((A & 0xFF) << 24) + ((R & 0xFF) << 16) + ((G & 0xFF) << 8) + (B & 0xFF);
         return this;
      }

      PixelARGB normalize() {
         if (addCount == 0) return this;
         R = R * 255 / A;
         G = G * 255 / A;
         B = B * 255 / A;
         A = A / addCount;
         combined = ((A & 0xFF) << 24) + ((R & 0xFF) << 16) + ((G & 0xFF) << 8) + (B & 0xFF);
         addCount = 0;
         return this;
      }

      PixelARGB addIgnoreAlpha(PixelARGB add) {
         addCount++;
         R += add.R;
         G += add.G;
         B += add.B;
         combined = ((A & 0xFF) << 24) + ((R & 0xFF) << 16) + ((G & 0xFF) << 8) + (B & 0xFF);
         return this;
      }

      PixelARGB normalizeIgnoreAlpha() {
         if (addCount == 0) return this;
         R = R / addCount;
         G = G / addCount;
         B = B / addCount;
         combined = ((A & 0xFF) << 24) + ((R & 0xFF) << 16) + ((G & 0xFF) << 8) + (B & 0xFF);
         addCount = 0;
         return this;
      }

      PixelARGB YIQContrastTextColor() {
         int color = (((R * 299) + (G * 587) + (B * 114)) / 1000) >= 128 ? 0: 255;
         return new PixelARGB(255, color, color, color);
      }
   }

   private void grainMergeArrayWithColor(int[] pixels, PixelARGB color) {
      for (int i = 0; i < pixels.length; i++) {
         PixelARGB pix = new PixelARGB(pixels[i]);
         if (pix.A == 0)
            pixels[i] = 0;
         else
            pixels[i] = (new PixelARGB(Math.max(0, (Math.min(255, pix.A + color.A - 128))), Math.max(0, (Math.min(255, pix.R + color.R - 128))), Math.max(0, (Math.min(255, pix.G + color.G - 128))), Math.max(0, (Math.min(255, pix.B + color.B - 128))))).combined;
      }
   }

   private void mergeArraysBasedOnAlpha(int[] target, int[] merge) {
      // Merge arrays, ignoring any transparent pixels in the merge array
      for (int i = 0; i < merge.length; i++) {
         PixelARGB targetPixel = new PixelARGB(target[i]);
         PixelARGB mergePixel = new PixelARGB(merge[i]);
         target[i] = mergePixel.A == 0 ? targetPixel.combined: mergePixel.combined;
      }
   }

   private PixelARGB averageColorFromArray(int[] pixels) {
      PixelARGB totals = new PixelARGB(0);
      for (int pixel: pixels) {
         totals.alphaAdd(new PixelARGB(pixel));
      }
      return totals.normalize();
   }

   private PixelARGB averageColorFromArrayB(int[] pixels) {
      PixelARGB totals = new PixelARGB(0);
      for (int pixel: pixels) {
         totals.addIgnoreAlpha(new PixelARGB(pixel));
      }
      return totals.normalizeIgnoreAlpha();
   }

   private int[] getPixelsForTexture(IntBuffer pixelBuf, int bufferWidth, TextureAtlasSprite icon) {
      int[] pixels = new int[icon.getIconWidth() * icon.getIconHeight()];
      int offset = (icon.getOriginY() * bufferWidth) + icon.getOriginX();
      for (int i = 0; i < icon.getIconHeight(); i++) {
         pixelBuf.position(offset + (i * bufferWidth));
         pixelBuf.get(pixels, (i * icon.getIconWidth()), icon.getIconWidth());
      }
      return pixels;
   }

   private void uploadReplacementTexture(TextureAtlasSprite icon, int[] pixels) {
      IntBuffer iconPixelBuf = ByteBuffer.allocateDirect(icon.getIconWidth() * icon.getIconHeight() * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
      iconPixelBuf.clear();
      iconPixelBuf.limit(pixels.length);
      iconPixelBuf.put(pixels);
      iconPixelBuf.position(0);

      GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, icon.getOriginX(), icon.getOriginY(), icon.getIconWidth(), icon.getIconHeight(), GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, iconPixelBuf);
   }

   private void generateIcons() {
      int terrainTextureId = Minecraft.getMinecraft().renderEngine.getTexture(TextureMap.locationBlocksTexture).getGlTextureId();
      int itemTextureId = Minecraft.getMinecraft().renderEngine.getTexture(TextureMap.locationItemsTexture).getGlTextureId();
      if (terrainTextureId != 0 && itemTextureId != 0) {
         // Store previous texture
         int previousTextureID = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

         // bind and copy block texture into buffer
         GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrainTextureId);
         int terrainTextureWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
         int terrainTextureHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
         IntBuffer terrainTexturePixelBuf = ByteBuffer.allocateDirect(terrainTextureWidth * terrainTextureHeight * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
         terrainTexturePixelBuf.clear();
         GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, terrainTexturePixelBuf);
         terrainTexturePixelBuf.limit(terrainTextureWidth * terrainTextureHeight);

         // bind and copy item texture into buffer
         GL11.glBindTexture(GL11.GL_TEXTURE_2D, itemTextureId);
         int itemTextureWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
         int itemTextureHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
         IntBuffer itemTexturePixelBuf = ByteBuffer.allocateDirect(itemTextureWidth * itemTextureHeight * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
         itemTexturePixelBuf.clear();
         GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, itemTexturePixelBuf);
         itemTexturePixelBuf.limit(itemTextureWidth * itemTextureHeight);

         // Copy the block textures we need into arrays
         GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrainTextureId);
         int[] labelBorderPixels = this.getPixelsForTexture(terrainTexturePixelBuf, terrainTextureWidth, StructuralLevel.iconLabelBorder);
         int[] labelBackgroundPixels = this.getPixelsForTexture(terrainTexturePixelBuf, terrainTextureWidth, StructuralLevel.iconLabelBackground);
         int[] topBorderPixels = this.getPixelsForTexture(terrainTexturePixelBuf, terrainTextureWidth, StructuralLevel.iconTopBorder);
         int[] topBackgroundPixels = this.getPixelsForTexture(terrainTexturePixelBuf, terrainTextureWidth, StructuralLevel.iconTopBackground);
         int[] topLabelBorderPixels = this.getPixelsForTexture(terrainTexturePixelBuf, terrainTextureWidth, StructuralLevel.iconTopBorder);
         int[] topLabelBackgroundPixels = this.getPixelsForTexture(terrainTexturePixelBuf, terrainTextureWidth, StructuralLevel.iconTopLabel);
         int[] sideBorderPixels = this.getPixelsForTexture(terrainTexturePixelBuf, terrainTextureWidth, StructuralLevel.iconSideBorder);
         int[] sideBackgroundPixels = this.getPixelsForTexture(terrainTexturePixelBuf, terrainTextureWidth, StructuralLevel.iconSideBackground);

         // Copy the block textures we need into arrays
         GL11.glBindTexture(GL11.GL_TEXTURE_2D, itemTextureId);
         int[] itemBasePixels = this.getPixelsForTexture(itemTexturePixelBuf, itemTextureWidth, StructuralLevel.iconItemBase);
         int[] itemArrowPixels = this.getPixelsForTexture(itemTexturePixelBuf, itemTextureWidth, StructuralLevel.iconItemArrow);
         int[] itemRomanPixels = this.getPixelsForTexture(itemTexturePixelBuf, itemTextureWidth, this.iconItem);

         int[] materialPixels;
         // Check if the material is an item
         if (Block.blocksList[materialStack.itemID] == null || (Block.blocksList[materialStack.itemID] != null && Block.blocksList[materialStack.itemID].getUnlocalizedName().equalsIgnoreCase("tile.ForgeFiller"))) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, itemTextureId);
            materialPixels = this.getPixelsForTexture(itemTexturePixelBuf, itemTextureWidth, (TextureAtlasSprite)materialStack.getItem().getIconFromDamage(materialStack.getItemDamage()));
         } else {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrainTextureId);
            materialPixels = this.getPixelsForTexture(terrainTexturePixelBuf, terrainTextureWidth, (TextureAtlasSprite)materialStack.getItem().getIconFromDamage(materialStack.getItemDamage()));
         }

         // PixelARGB color = averageColorFromArray(materialPixels); // This makes iron... more red, kind of a neat rusty look, but meh
         PixelARGB color = averageColorFromArrayB(materialPixels);
         // System.out.println("Color R: " + color.R + ", G: " + color.G + ", B: " + color.B);

         this.textColor = color.YIQContrastTextColor().combined;

         grainMergeArrayWithColor(labelBorderPixels, color);
         grainMergeArrayWithColor(topBorderPixels, color);
         grainMergeArrayWithColor(topLabelBorderPixels, color);
         grainMergeArrayWithColor(sideBorderPixels, color);
         grainMergeArrayWithColor(itemArrowPixels, color);

         mergeArraysBasedOnAlpha(labelBorderPixels, labelBackgroundPixels);
         mergeArraysBasedOnAlpha(topBorderPixels, topBackgroundPixels);
         mergeArraysBasedOnAlpha(topLabelBorderPixels, topLabelBackgroundPixels);
         mergeArraysBasedOnAlpha(sideBorderPixels, sideBackgroundPixels);
         mergeArraysBasedOnAlpha(itemBasePixels, itemArrowPixels);
         mergeArraysBasedOnAlpha(itemBasePixels, itemRomanPixels);

         GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrainTextureId);
         uploadReplacementTexture(this.iconBlockLabel, labelBorderPixels);
         uploadReplacementTexture(this.iconBlockTop, topBorderPixels);
         uploadReplacementTexture(this.iconBlockTopLabel, topLabelBorderPixels);
         uploadReplacementTexture(this.iconBlockSide, sideBorderPixels);

         GL11.glBindTexture(GL11.GL_TEXTURE_2D, itemTextureId);
         uploadReplacementTexture(this.iconItem, itemBasePixels);

         GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTextureID);
      }
   }
}