/*******************************************************************************
 * Copyright (c) 2015
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.graphics.image;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import go.graphics.GLDrawContext;
import go.graphics.GeometryHandle;
import go.graphics.IllegalBufferException;
import go.graphics.SharedGeometry;
import go.graphics.TextureHandle;

import java.awt.image.BufferedImage;

import jsettlers.common.Color;
import jsettlers.graphics.image.reader.ImageMetadata;

/**
 * This is the base for all images that are directly loaded from the image file.
 * <p>
 * This class interprets the image data in 5-5-5-1-Format. To change the interpretation, it is possible to subclass this class.
 *
 * @author Michael Zangl
 */
public class SingleImage extends Image implements ImageDataPrivider {

	protected ShortBuffer data;
	protected final int width;
	protected final int height;
	protected int textureWidth = 0;
	protected int textureHeight = 0;
	protected final int offsetX;
	protected final int offsetY;

	private TextureHandle texture = null;
	protected SharedGeometry.SharedGeometryHandle geometryIndex = null;

	/**
	 * Creates a new image by the given buffer.
	 *
	 * @param data
	 * 		The data buffer for the image with an unspecified color format.
	 * @param width
	 * 		The width.
	 * @param height
	 * 		The height.
	 * @param offsetX
	 * 		The x offset of the image.
	 * @param offsetY
	 * 		The y offset of the image.
	 */
	protected SingleImage(ShortBuffer data, int width, int height, int offsetX,
			int offsetY) {
		this.data = data;
		this.width = width;
		this.height = height;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	/**
	 * Creates a new image by linking this images data to the data of the provider.
	 *
	 * @param metadata
	 * 		The mata data to use.
	 * @param data
	 * 		The data to use.
	 */
	protected SingleImage(ImageMetadata metadata, short[] data) {
		this.data = ShortBuffer.wrap(data);
		this.width = metadata.width;
		this.height = metadata.height;
		this.offsetX = metadata.offsetX;
		this.offsetY = metadata.offsetY;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public int getOffsetX() {
		return this.offsetX;
	}

	@Override
	public int getOffsetY() {
		return this.offsetY;
	}

	/**
	 * Converts the current data to match the power of two size.
	 */
	protected void adaptDataToTextureSize() {
		if (width == 0 || height == 0) {
			return;
		}

		this.data.rewind();
		short[] newData = new short[textureHeight * textureWidth];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				newData[y * textureWidth + x] = data.get(y * width + x);
			}
			for (int x = width; x < textureWidth; x++) {
				newData[y * textureWidth + x] = newData[y * textureWidth + width - 1];
			}
		}
		for (int y = height; y < textureHeight; y++) {
			for (int x = 0; x < textureWidth; x++) {
				newData[y * textureWidth + x] = newData[(height - 1) * textureWidth + x];
			}
		}
		data = ShortBuffer.wrap(newData);
	}

	/**
	 * Generates the texture, if needed, and returns the index of that texutre.
	 *
	 * @param gl
	 * 		The gl context to use to generate the image.
	 * @return The gl handle or <code>null</code> if the texture is not allocated.
	 */
	public TextureHandle getTextureIndex(GLDrawContext gl) {
		if (texture == null || !texture.isValid()) {
			if (textureWidth == 0) {
				textureWidth = gl.makeSideLengthValid(width);
				textureHeight = gl.makeSideLengthValid(height);
				if (textureWidth != width || textureHeight != height) {
					adaptDataToTextureSize();
				}
				data.position(0);
			}
			texture = gl.generateTexture(textureWidth, textureHeight, this.data);
		}
		return this.texture;
	}

	private static SharedGeometry.SharedGeometryHandle rectHandle = null;

	@Override
	public void drawImageAtRect(GLDrawContext gl, float x, float y, float width, float height) {
		try {
			TextureHandle textureHandle = getTextureIndex(gl);

			if(rectHandle == null || SharedGeometry.isValid(gl, rectHandle)) rectHandle = SharedGeometry.addGeometry(gl, SharedGeometry.createQuadGeometry(0, 1, 1, 0, 0, 0, width/textureWidth, height/textureHeight));

			gl.glPushMatrix();
			gl.glTranslatef(x, y, 0);
			gl.glScalef(width, height, 0);
			gl.drawQuadWithTexture(textureHandle, rectHandle.geometry, rectHandle.index);
			gl.glPopMatrix();
		} catch (IllegalBufferException e) {
			handleIllegalBufferException(e);
		}
	}

	@Override
	public ShortBuffer getData() {
		return this.data;
	}

	@Override
	public void drawAt(GLDrawContext gl, float x, float y, float z, Color torsoColor, float fow) {
		gl.glPushMatrix();
		gl.glTranslatef(x, y, z);
		drawOnlyImageAt(gl, fow);
		gl.glPopMatrix();
	}

	@Override
	public void drawOnlyImageAt(GLDrawContext gl, float fow) {
		try {
			TextureHandle textureIndex = getTextureIndex(gl);
			GeometryHandle geometryIndex2 = getGeometry(gl);
			gl.color(fow, fow, fow, 1);
			gl.drawQuadWithTexture(textureIndex, geometryIndex2, geometryIndex.index);
		} catch (IllegalBufferException e) {
			handleIllegalBufferException(e);
		}
	}

	@Override
	public void drawOnlyTorsoAt(GLDrawContext gl, Color torsoColor, float fow) {
		try {
			TextureHandle textureIndex = getTextureIndex(gl);
			GeometryHandle geometryIndex2 = getGeometry(gl);
			gl.color(torsoColor.getRed()*fow, torsoColor.getGreen()*fow, torsoColor.getBlue()*fow, torsoColor.getAlpha());
			gl.drawQuadWithTexture(textureIndex, geometryIndex2, geometryIndex.index);
		} catch (IllegalBufferException e) {
			handleIllegalBufferException(e);
		}
	}

	protected float[] getGeometry() {
		int left = getOffsetX();
		int top = -getOffsetY();
		return SharedGeometry.createQuadGeometry(left, top, left+width, top-height, 0, 0, (float)width/textureWidth, (float)height/textureHeight);
	}

	protected void setGeometry(SharedGeometry.SharedGeometryHandle geometry) {
		geometryIndex = geometry;
	}

	protected GeometryHandle getGeometry(GLDrawContext context) throws IllegalBufferException {
		if(geometryIndex == null || SharedGeometry.isValid(context, geometryIndex)) geometryIndex = SharedGeometry.addGeometry(context, getGeometry());
		return geometryIndex.geometry;
	}

	public float getTextureScaleX() {
		return (float) width / textureWidth;
	}

	public float getTextureScaleY() {
		return (float) height / textureHeight;
	}

	protected float convertU(float relativeU) {
		return relativeU * getTextureScaleX();
	}

	protected float convertV(float relativeV) {
		return relativeV * getTextureScaleY();
	}

	private static final Object buildLock = new Object(); // should never be triggered, but who knows ?
	private static GeometryHandle buildHandle = null;
	private static ByteBuffer buildBfr = ByteBuffer.allocateDirect(5*4*3).order(ByteOrder.nativeOrder());

	/**
	 * Draws a triangle part of this image on the image buffer.
	 *
	 * @param gl
	 * 		The context to use
	 * @param viewX
	 * 		Image center x coordinate
	 * @param viewY
	 * 		Image center y coordinate
	 * @param u1
	 * @param v1
	 * @param u2
	 * @param v2
	 * @param u3
	 * @param v3
	 * @param color
	 */
	public void drawTriangle(GLDrawContext gl, float viewX,
			float viewY, float u1, float v1, float u2, float v2, float u3, float v3, float color) {
		try {
			float left = getOffsetX() + viewX;
			float top = -getOffsetY() + viewY;
			// In the draw process sub-integer coordinates can be rounded in unexpected ways that is particularly noticeable when redrawing the
			// growing
			// image of a building in the construction phase. By aligning to the nearest integer images can be placed in a more predictable and
			// controlled
			// manner.
			u1 = (float) Math.round(u1 * width) / width;
			u2 = (float) Math.round(u2 * width) / width;
			u3 = (float) Math.round(u3 * width) / width;
			v1 = (float) Math.round(v1 * height) / height;
			v2 = (float) Math.round(v2 * height) / height;
			v3 = (float) Math.round(v3 * height) / height;

			synchronized (buildLock) {
				if(buildHandle == null || !buildHandle.isValid()) buildHandle = gl.generateGeometry(5*4*3);
				buildBfr.asFloatBuffer().put(new float[] {
						u1 * width,
						-v1 * height,
						0,
						convertU(u1),
						convertV(v1),

						u2 * width,
						-v2 * height,
						0,
						convertU(u2),
						convertV(v2),

						u3 * width,
						-v3 * height,
						0,
						convertU(u3),
						convertV(v3),

				});
				gl.updateGeometryAt(buildHandle, 0, buildBfr);

				gl.color(color, color, color, 1);

				gl.glPushMatrix();
				gl.glTranslatef(left, top ,0);
				gl.drawTrianglesWithTexture(getTextureIndex(gl), buildHandle, 1);
				gl.glPopMatrix();
			}
		} catch (IllegalBufferException e) {
			handleIllegalBufferException(e);
		}
	}

	public BufferedImage convertToBufferedImage() {
		if (width <= 0 || height <= 0) {
			return null;
		}

		BufferedImage rendered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		ShortBuffer data = getData().duplicate();
		data.rewind();

		int[] rgbArray = new int[data.remaining()];
		for (int i = 0; i < rgbArray.length; i++) {
			short myColor = data.get();
			rgbArray[i] = Color.convertTo32Bit(myColor);
		}

		rendered.setRGB(0, 0, width, height, rgbArray, 0, width);
		return rendered;
	}

	public Long hash() {
		long hashCode = 1L;
		long multiplier = 1L;
		while (data.hasRemaining()) {
			multiplier *= 31L;
			hashCode += (data.get() + 27L) * multiplier;
		}
		data.rewind();
		return hashCode;
	}
}