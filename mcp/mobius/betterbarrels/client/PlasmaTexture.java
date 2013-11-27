package mcp.mobius.betterbarrels.client;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class PlasmaTexture {

	//private static float[] sinus     = new float[4096];
	private static int[] radial  = new int[4096];
	private static int[] cloud   = new int[4096];
	private static int[] textureInt = new int[4096]; // 16x16
	private static int texSize = 64;
	private static IntBuffer textureIDBuffer = BufferUtils.createIntBuffer(1);
	private static ByteBuffer texture = BufferUtils.createByteBuffer(4096 * 4);
	private static boolean textureDone = false;
	public  static int texIndex = -1;
	
	
	public static void precomputeTables(){
		//for (int i = 0; i < sinus.length; i++)
		//	sinus[i]     = (float)Math.sin( i * ((2.0D*Math.PI)/sinus.length) );
		
		for (int i = 0; i < texSize; i++)
			for (int j = 0; j < texSize; j++){
				radial[i+texSize*j] = 128 + (int) (Math.sin(Math.sqrt(Math.pow(texSize-i, 2)+Math.pow(texSize-j, 2))) * 127);
				cloud [i+texSize*j] = 128 + (int) (Math.sin(i/(37.0D+15.0D*Math.cos(j/74.0D))) * (float) Math.cos(j/(31.0D+11.0D*Math.sin(i/57.0D))) * 127);
			}

		/*
		for (int i = 0; i < texSize; i++)
			System.out.printf("%s ", radial[i]);
		System.out.printf("\n");
		*/
	}
	
	public static void generateTexture(){
		for (int i = 0; i < texSize; i++)
			for (int j = 0; j < texSize; j ++){
				texture.put((byte) radial[i+texSize*j]);//R
				texture.put((byte) 0x00);	//G
				texture.put((byte) 0x00);   //cloud [i+texSize*j]);//B
				texture.put((byte) 0xFF);	//A
			}
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
				//textureInt[i+texSize*j] = (radial[i+texSize*j] << 16) + (cloud[i+texSize*j] << 8);
		
		GL11.glGenTextures(textureIDBuffer);
		
		textureIDBuffer.rewind();
		texture.rewind();
		
		/*
		for (int i = 0; i < texSize; i++)
			System.out.printf("%s ", texture.get());
		System.out.printf("\n");

		texture.rewind();
		*/		
		
		texIndex = textureIDBuffer.get(0);
		
		//System.out.printf("==================== %s ====================\n", texIndex);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D,texIndex);
		
		GL11.glTexImage2D (
				GL11.GL_TEXTURE_2D, 	//Type : texture 2D
				0, 						//Mipmap : aucun
				GL11.GL_RGBA8,			//Couleurs : 4
				texSize, 				//Largeur : 2
				texSize, 				//Hauteur : 2
				0, 						//Largeur du bord : 0
				GL11.GL_RGBA, 			//Format : RGBA
				GL11.GL_UNSIGNED_BYTE, 	//Type des couleurs
				texture 				//Addresse de l'image
				);		
		
	}
	
	public static void bindTexture(){
		if (!textureDone){
			generateTexture();
			textureDone = true;
		}
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D,texIndex);
	}
	
	public static int getTextureIndex(){
		return texIndex;
	}
}
