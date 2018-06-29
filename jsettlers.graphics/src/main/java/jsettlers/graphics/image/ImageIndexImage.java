/*******************************************************************************
 * Copyright (c) 2015 - 2018
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

import go.graphics.GLDrawContext;
import go.graphics.GeometryHandle;
import go.graphics.IllegalBufferException;
import go.graphics.SharedGeometry;
import jsettlers.common.Color;

/**
 * This is an image that is stored in the image index file.
 *
 * @author Michael Zangl
 */
public class ImageIndexImage extends Image {
	private static final float IMAGE_DRAW_OFFSET = .5f;
	private static final ByteBuffer tempBuffer = ByteBuffer.allocateDirect(5*4*4).order(ByteOrder.nativeOrder());

	private final short width;
	private final short height;
	private final float[] geometry;
	private SharedGeometry.SharedGeometryHandle geometryIndex = null;
	private final ImageIndexTexture texture;
	private final int offsetX;
	private final int offsetY;
	private final float umin;
	private final float vmin;
	private final float umax;
	private final float vmax;
	private final boolean isTorso;

	private ImageIndexImage torso;

	/**
	 * Constructs a new image in an image index.
	 *
	 * @param texture
	 * 		The texture this image is part of.
	 * @param offsetX
	 * 		The x-offset to the center of the image.
	 * @param offsetY
	 * 		The y-offset to the center of the image.
	 * @param width
	 * 		The width of the image
	 * @param height
	 * 		The height of the image.
	 * @param umin
	 * 		The bounds of the image on the texture (0..1).
	 * @param vmin
	 * 		The bounds of the image on the texture (0..1).
	 * @param umax
	 * 		The bounds of the image on the texture (0..1).
	 * @param vmax
	 * 		The bounds of the image on the texture (0..1).
	 */
	ImageIndexImage(ImageIndexTexture texture, int offsetX, int offsetY, short width, short height, float umin, float vmin, float umax, float vmax, boolean isTorso) {
		this.texture = texture;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.width = width;
		this.height = height;
		this.umin = umin;
		this.vmin = vmin;
		this.umax = umax;
		this.vmax = vmax;
		this.isTorso = isTorso;

		geometry = createGeometry();
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void drawOnlyImageAt(GLDrawContext gl, float fow) {
		if(isTorso) return;
		gl.color(fow, fow, fow, 1);
		draw(gl, geometryIndex.geometry, geometryIndex.index);
	}

	@Override
	public void drawOnlyTorsoAt(GLDrawContext gl, Color torsoColor, float fow) {
		if(isTorso) {
			gl.color(torsoColor.getRed()*fow, torsoColor.getGreen()*fow, torsoColor.getBlue()*fow, torsoColor.getAlpha());
			draw(gl, geometryIndex.geometry, geometryIndex.index);
		}
		if(torso != null) torso.drawOnlyTorsoAt(gl, torsoColor, fow);
	}

	private void draw(GLDrawContext gl, GeometryHandle handle, int offset) {
		try {
			if(geometryIndex == null) geometryIndex = SharedGeometry.addGeometry(gl, geometry);

			gl.drawQuadWithTexture(texture.getTextureIndex(gl), handle, offset);
		} catch (IllegalBufferException e) {
			try {
				texture.recreateTexture();
				gl.drawQuadWithTexture(texture.getTextureIndex(gl), handle, offset);
			} catch (IllegalBufferException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private float[] createGeometry() {
		return SharedGeometry.createQuadGeometry(-offsetX + IMAGE_DRAW_OFFSET, -offsetY + IMAGE_DRAW_OFFSET,
				-offsetX + width + IMAGE_DRAW_OFFSET,-offsetY + height + IMAGE_DRAW_OFFSET,
				umin, vmax, umax, vmin);
	}

	private static SharedGeometry.SharedGeometryHandle imageRectHandle = null;

	@Override
	public void drawImageAtRect(GLDrawContext gl, float x, float y, float width, float height) {

		gl.glPushMatrix();
		gl.glTranslatef(x, y, 0);
		gl.glScalef(width, height, 0);
		try {
			if(imageRectHandle == null) imageRectHandle = SharedGeometry.addGeometry(gl, SharedGeometry.createQuadGeometry(0,1, 1, 0, umin, vmin, umax, vmax));
			draw(gl, imageRectHandle.geometry, imageRectHandle.index);
		} catch (IllegalBufferException e) {
			e.printStackTrace();
		}
		gl.glPopMatrix();

		if (torso != null) {
			torso.drawImageAtRect(gl, x, y, width, height);
		}
	}

	public void setTorso(ImageIndexImage torso) {
		this.torso = torso;
	}
}
