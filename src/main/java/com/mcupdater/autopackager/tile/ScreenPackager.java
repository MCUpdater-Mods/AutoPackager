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
        this.addButton(new Button(this.guiLeft + 5, this.guiTop + 5, 75, 20, new StringTextComponent("Change Mode"), buttonPress -> {
            PackagerChannel.INSTANCE.sendToServer(new ModePacket(container.getTileEntity().getPos()));
            container.detectAndSendChanges();
        }));
        this.addButton(new WidgetPower(this.guiLeft + 153, this.guiTop + 5, 18, 71, container.getEnergyHandler(), WidgetPower.Orientation.VERTICAL));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY); //renderHoveredToolTip
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        // Do not draw normal container labels
        this.font.func_238422_b_(matrixStack, new TranslationTextComponent("autopackager.mode.current").appendString(" ").append(new TranslationTextComponent(container.getTileEntity().mode.getMessage())), 5f, 30f, 4210752);
    }


    //renderBackground
    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI);
        int relX = this.guiLeft;
        int relY = this.guiTop;
        this.blit(matrixStack, relX, relY, 0, 0, this.xSize, this.ySize);
    }
}
