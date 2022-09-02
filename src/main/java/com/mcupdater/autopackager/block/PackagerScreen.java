package com.mcupdater.autopackager.block;

import com.mcupdater.autopackager.AutoPackager;
import com.mcupdater.autopackager.network.ModePacket;
import com.mcupdater.autopackager.network.PackagerChannel;
import com.mcupdater.mculib.gui.ConfigPanel;
import com.mcupdater.mculib.gui.TabConfig;
import com.mcupdater.mculib.gui.WidgetPower;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PackagerScreen extends AbstractContainerScreen<PackagerMenu> {
    private ResourceLocation GUI = new ResourceLocation(AutoPackager.MODID, "textures/gui/blank.png");
    private ConfigPanel configPanel;
    private TabConfig configTab;
    private Button modeButton;
    private WidgetPower powerWidget;

    public PackagerScreen(PackagerMenu container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    protected void init() {
        super.init();
        modeButton = this.addRenderableWidget(new Button(this.leftPos + 5, this.topPos + 16, 75, 20, new TextComponent("Change Mode"), buttonPress -> {
            PackagerChannel.INSTANCE.sendToServer(new ModePacket(menu.getBlockEntity().getBlockPos()));
            menu.broadcastChanges();
        }));
        powerWidget = this.addRenderableWidget(new WidgetPower(this.leftPos + 153, this.topPos + 5, 18, 71, menu.getEnergyHandler(), WidgetPower.Orientation.VERTICAL));
        this.configPanel = new ConfigPanel(this.menu, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        this.configPanel.setVisible(false);
        this.configTab = this.addRenderableWidget(new TabConfig(this.leftPos - 22, this.topPos + 2,22,22, (mouseX, mouseY) -> {
            this.configPanel.setVisible(!this.configPanel.isVisible());
            this.modeButton.visible = !this.modeButton.visible;
            this.powerWidget.visible = !this.powerWidget.visible;
        }));
        this.configTab.setChild(this.configPanel);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        if (!this.configPanel.isVisible()) {
            super.render(poseStack, mouseX, mouseY, partialTicks);
        } else {
            renderNoSlots(poseStack, mouseX, mouseY, partialTicks);
        }
        this.renderTooltip(poseStack, mouseX, mouseY); //renderHoveredToolTip
    }

    public void renderNoSlots(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        int i = this.leftPos;
        int j = this.topPos;
        this.renderBg(pPoseStack, pPartialTick, pMouseX, pMouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ContainerScreenEvent.DrawBackground(this, pPoseStack, pMouseX, pMouseY));
        RenderSystem.disableDepthTest();
        for(Widget widget : this.renderables) {
            widget.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(i, j, 0.0D);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderLabels(pPoseStack, pMouseX, pMouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ContainerScreenEvent.DrawForeground(this, pPoseStack, pMouseX, pMouseY));
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        if (!this.configPanel.isVisible()) {
            super.renderLabels(poseStack, mouseX, mouseY);
            this.font.draw(poseStack, new TranslatableComponent("autopackager.mode.current").append(" ").append(new TranslatableComponent(menu.getBlockEntity().mode.getMessage())), 5f, 41f, 4210752);
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI);
        int relX = this.leftPos;
        int relY = this.topPos;
        this.blit(poseStack,relX,relY,0,0,this.imageWidth,this.imageHeight);
    }

}
