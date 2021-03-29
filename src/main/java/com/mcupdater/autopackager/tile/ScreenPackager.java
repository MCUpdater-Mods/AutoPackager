package com.mcupdater.autopackager.tile;

import com.mcupdater.autopackager.AutoPackager;
import com.mcupdater.autopackager.network.ModePacket;
import com.mcupdater.autopackager.network.PackagerChannel;
import com.mcupdater.mculib.gui.WidgetPower;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScreenPackager extends ContainerScreen<ContainerPackager> {
    private ResourceLocation GUI = new ResourceLocation(AutoPackager.MODID, "textures/gui/blank.png");

    public ScreenPackager(ContainerPackager container, PlayerInventory inv, ITextComponent name) {
        super(container, inv, name);
        this.passEvents = false;
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new Button(this.leftPos + 5, this.topPos + 5, 75, 20, new StringTextComponent("Change Mode"), buttonPress -> {
            PackagerChannel.INSTANCE.sendToServer(new ModePacket(menu.getTileEntity().getBlockPos()));
            menu.broadcastChanges();
        }));
        this.addButton(new WidgetPower(this.leftPos + 153, this.topPos + 5, 18, 71, menu.getEnergyHandler(), WidgetPower.Orientation.VERTICAL));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY); //renderHoveredToolTip
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        // Do not draw normal container labels
        this.font.draw(matrixStack, new TranslationTextComponent("autopackager.mode.current").append(" ").append(new TranslationTextComponent(menu.getTileEntity().mode.getMessage())), 5f, 30f, 4210752);
    }


    //renderBackground
    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(GUI);
        int relX = this.leftPos;
        int relY = this.topPos;
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }
}
