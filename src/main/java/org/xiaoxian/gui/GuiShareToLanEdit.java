package org.xiaoxian.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.xiaoxian.lan.ShareToLan;
import org.xiaoxian.util.ConfigUtil;
import org.xiaoxian.util.TextBoxUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.ServerSocket;

public class GuiShareToLanEdit {

    public static EditBox PortTextBox;
    public static String PortText = "";
    public static String PortWarningText = "";

    public static EditBox MaxPlayerBox;
    public static String MaxPlayerText = "";
    public static String MaxPlayerWarningText = "";

    @SubscribeEvent
    public void onGuiOpenEvent(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof ShareToLanScreen) {
            event.setNewScreen(new GuiShareToLanModified(new PauseScreen(true)));
        }
    }

    public static class GuiShareToLanModified extends ShareToLanScreen {
        Font fontRenderer = Minecraft.getInstance().font;

        public GuiShareToLanModified(Screen parentScreen) {
            super(parentScreen);
        }

        @Override
        public void init() {
            super.init();

            PortTextBox = new TextBoxUtil(fontRenderer, this.width / 2 - 155, this.height - 70, 145, 20, "");
            PortTextBox.setMaxLength(5);
            PortTextBox.setValue(PortText);

            MaxPlayerBox = new TextBoxUtil(fontRenderer, this.width / 2 + 5, this.height - 70, 145, 20, "");
            MaxPlayerBox.setMaxLength(6);
            MaxPlayerBox.setValue(MaxPlayerText);

            Button button101 = (Button) findButton();
            if (button101 != null) {
                button101.active = checkPortAndEnableButton(PortTextBox.getValue()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getValue());
            }

            Button originalButton = null;
            for (Renderable widget : this.renderables) {
                if (widget instanceof Button button) {
                    if (button.getMessage().getString().equals(I18n.get("lanServer.start"))) {
                        originalButton = button;
                        break;
                    }
                }
            }

            if (originalButton != null) {
                int width = originalButton.getWidth();
                int height = originalButton.getHeight();
                int x = originalButton.getX();
                int y = originalButton.getY();

                this.renderables.remove(originalButton);
                this.removeWidget(originalButton);

                Button finalOriginalButton = originalButton;
                Button newButton = Button.builder(Component.translatable(I18n.get("lanServer.start")), button -> {
                    new ShareToLan().handleLanSetup();
                    finalOriginalButton.onPress();
                    ConfigUtil.set("Port", PortText);
                    ConfigUtil.set("MaxPlayer", MaxPlayerText);
                    ConfigUtil.save();
                }).bounds(x, y, width, height).build();

                this.addRenderableWidget(newButton);
            }

            EditBox targetEditBox = null;
            for (Renderable widget : this.renderables) {
                if (widget instanceof EditBox editBox) {
                    if (editBox.getMessage().equals(Component.translatable("lanServer.port"))) {
                        targetEditBox = editBox;
                    }
                }
            }

            if (targetEditBox != null) {
                this.removeWidget(targetEditBox);
            }

        }

        @Override
        public void render(@Nonnull GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
            super.render(matrixStack, mouseX, mouseY, partialTicks);

            PortTextBox.render(matrixStack, mouseX,mouseY,partialTicks);
            MaxPlayerBox.render(matrixStack, mouseX,mouseY,partialTicks);

            matrixStack.drawString(Minecraft.getInstance().font, I18n.get("easylan.text.port"), this.width / 2 - 155, this.height - 85, 0xFFFFFF);
            matrixStack.drawString(fontRenderer, PortWarningText, this.width / 2 - 155, this.height - 45, 0xFF0000);

            matrixStack.drawString(fontRenderer, I18n.get("easylan.text.maxplayer"), this.width / 2 + 5, this.height - 85, 0xFFFFFF);
            matrixStack.drawString(fontRenderer, MaxPlayerWarningText, this.width / 2 + 5, this.height - 45, 0xFF0000);

            // 来自开发者: 懒得管了
            //matrixStack.fill(this.width / 2 - 32, 140, this.width / 2 + 32, 150, 0xFF000000);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            PortTextBox.keyPressed(keyCode, scanCode, modifiers);
            MaxPlayerBox.keyPressed(keyCode, scanCode, modifiers);

            Button button101 = (Button) findButton();
            if (button101 != null) {
                button101.active = checkPortAndEnableButton(PortTextBox.getValue()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getValue());
            }

            MaxPlayerText = MaxPlayerBox.getValue();
            PortText = PortTextBox.getValue();

            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char typedChar, int keyCode) {
            PortTextBox.charTyped(typedChar, keyCode);
            MaxPlayerBox.charTyped(typedChar, keyCode);

            String previousText = PortTextBox.getValue();
            String previousMaxPlayerText = MaxPlayerBox.getValue();

            if (Character.isDigit(typedChar)) {
                String newPortText = PortTextBox.getValue();
                String newMaxPlayerText = MaxPlayerBox.getValue();
                try {
                    int newPort = Integer.parseInt(newPortText);
                    int newMaxPlayer = Integer.parseInt(newMaxPlayerText);

                    if (!(newPort >= 100 && newPort <= 65535)) {
                        PortTextBox.setValue(previousText);
                    }

                    if (!(newMaxPlayer >= 2 && newMaxPlayer <= 500000)) {
                        MaxPlayerBox.setValue(previousMaxPlayerText);
                    }
                } catch (NumberFormatException e) {
                    PortTextBox.setValue(previousText);
                    MaxPlayerBox.setValue(previousMaxPlayerText);
                }
            }

            Button button101 = (Button) findButton();
            if (button101 != null) {
                button101.active = checkPortAndEnableButton(PortTextBox.getValue()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getValue());
            }

            MaxPlayerText = MaxPlayerBox.getValue();
            PortText = PortTextBox.getValue();

            return super.charTyped(typedChar, keyCode);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            PortTextBox.mouseClicked(mouseX, mouseY, mouseButton);
            PortText = PortTextBox.getValue();

            MaxPlayerBox.mouseClicked(mouseX, mouseY, mouseButton);
            MaxPlayerText = MaxPlayerBox.getValue();

            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        private Renderable findButton() {
            for (Renderable widget : this.renderables) {
                if (widget instanceof Button button) {
                    if (button.getMessage().getString().equals(I18n.get("lanServer.start"))) {
                        return button;
                    }
                }
            }
            return null;
        }

        private boolean checkPortAndEnableButton(String portText) {
            if (portText.isEmpty()) {
                PortWarningText = "";
                return true;
            } else {
                try {
                    int port = Integer.parseInt(portText);
                    boolean isPortAvailable = port >= 100 && port <= 65535 && isPortAvailable(port);
                    PortWarningText = isPortAvailable ? "" : I18n.get("easylan.text.port.used");

                    if (!(port >= 100 && port <= 65535)) {
                        PortWarningText = I18n.get("easylan.text.port.invalid");
                    }

                    return isPortAvailable;
                } catch (NumberFormatException e) {
                    PortWarningText = I18n.get("easylan.text.port.invalid");
                    return false;
                }
            }
        }

        private boolean checkMaxPlayerAndEnableButton(String maxPlayerText) {
            if (maxPlayerText.isEmpty()) {
                MaxPlayerWarningText = "";
                return true;
            } else {
                try {
                    int maxPlayer = Integer.parseInt(maxPlayerText);
                    if (!(maxPlayer >= 2 && maxPlayer <= 500000)) {
                        MaxPlayerWarningText = I18n.get("easylan.text.maxplayer.invalid");
                        return false;
                    }
                    MaxPlayerWarningText = "";
                    return true;
                } catch (NumberFormatException e) {
                    MaxPlayerWarningText = I18n.get("easylan.text.maxplayer.invalid");
                    return false;
                }
            }
        }

        public boolean isPortAvailable(int port) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                serverSocket.setReuseAddress(true);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }
}
