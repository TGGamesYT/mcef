/*
 *     MCEF (Minecraft Chromium Embedded Framework)
 *     Copyright (C) 2023 CinemaMod Group
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 */

package com.cinemamod.mcef;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.renderer.texture.AbstractTexture;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

public class MCEFRenderer extends AbstractTexture {
    private final boolean transparent;
    private int texWidth = 0, texHeight = 0;

    protected MCEFRenderer(boolean transparent) {
        this.transparent = transparent;
    }

    public void initialize() {
        // texture created lazily on first paint
    }

    public boolean isTransparent() {
        return transparent;
    }

    public GpuTextureView getTextureView() {
        return textureView;
    }

    @Override
    public void close() {
        if (textureView != null) {
            textureView.close();
            textureView = null;
        }
        if (texture != null) {
            texture.close();
            texture = null;
        }
        texWidth = 0;
        texHeight = 0;
    }

    private void ensureTexture(int width, int height) {
        if (texture != null && texWidth == width && texHeight == height) return;
        close();
        GpuDevice device = RenderSystem.getDevice();
        texture = device.createTexture(
                "mcef_browser",
                GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST,
                TextureFormat.RGBA8, width, height, 1, 1
        );
        texture.setTextureFilter(FilterMode.LINEAR, false);
        textureView = device.createTextureView(texture);
        texWidth = width;
        texHeight = height;
    }

    public int getTextureID() {
        if (texture == null) return 0;
        return ((GlTexture) texture).glId();
    }

    public int getWidth() {
        return texWidth;
    }

    public int getHeight() {
        return texHeight;
    }

    protected void onPaint(ByteBuffer buffer, int width, int height) {
        ensureTexture(width, height);
        int glId = ((GlTexture) texture).glId();
        GlStateManager._bindTexture(glId);
        if (transparent) GlStateManager._enableBlend();
        GlStateManager._pixelStore(GL_UNPACK_ROW_LENGTH, width);
        GlStateManager._pixelStore(GL_UNPACK_SKIP_PIXELS, 0);
        GlStateManager._pixelStore(GL_UNPACK_SKIP_ROWS, 0);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
    }

    protected void onPaint(ByteBuffer buffer, int x, int y, int width, int height) {
        if (texture == null) return;
        int glId = ((GlTexture) texture).glId();
        GlStateManager._bindTexture(glId);
        glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, width, height, GL_BGRA,
                GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
    }
}
