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
 *
 *     This file was modified on 2026-04-25 to port the mod to Minecraft 1.21.8.
 *     Changes: replaced PoseStack (removed from GUI rendering in 1.21.8) with
 *     absolute coordinate rendering using GuiGraphics.fill/drawString directly.
 */

package com.cinemamod.mcef.internal;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MCEFDownloaderMenu extends Screen {
    private final Screen menu;

    public MCEFDownloaderMenu(Screen menu) {
        super(Component.literal("MCEF is downloading required libraries..."));
        this.menu = menu;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int cx = width / 2;
        int cy = height / 2;

        int progressBarHeight = 14;
        int progressBarWidth = width / 3;

        int barX = cx - progressBarWidth / 2;
        int barY = cy - progressBarHeight / 2;

        /* Draw Progress Bar */
        graphics.fill(barX, barY, barX + progressBarWidth, barY + progressBarHeight, -1);
        graphics.fill(barX + 2, barY + 2, barX + progressBarWidth - 2, barY + progressBarHeight - 2, -16777215);
        graphics.fill(barX + 4, barY + 4,
                barX + 4 + (int) ((progressBarWidth - 8) * MCEFDownloadListener.INSTANCE.getProgress()),
                barY + progressBarHeight - 4,
                -1
        );

        String[] text = new String[]{
                MCEFDownloadListener.INSTANCE.getTask(),
                Math.round(MCEFDownloadListener.INSTANCE.getProgress() * 100) + "%",
        };

        /* Draw Text */
        int lineHeight = font.lineHeight + 2;
        int totalTextHeight = (text.length + 1) * lineHeight;
        int textStartY = barY - totalTextHeight - 4;

        // draw menu name
        String titleStr = ChatFormatting.GOLD + title.getString();
        graphics.drawString(font, titleStr, cx - font.width(titleStr) / 2, textStartY, 0xFFFFFF);

        for (int idx = 0; idx < text.length; idx++) {
            String s = text[idx];
            graphics.drawString(font, s, cx - font.width(s) / 2, textStartY + (idx + 1) * lineHeight, 0xFFFFFF);
        }

        // TODO: if listener.isFailed(), draw some "Failed to initialize MCEF" text with an "OK" button to proceed
    }

    @Override
    public void tick() {
        if (MCEFDownloadListener.INSTANCE.isDone() || MCEFDownloadListener.INSTANCE.isFailed()) {
            onClose();
            Minecraft.getInstance().setScreen(menu);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
