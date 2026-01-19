package com.example.examplemod;

import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.List;

public class SampleUiScreen extends Screen {

    private static final List<ParticleType<?>> particleTypes =
            BuiltInRegistries.PARTICLE_TYPE.stream().toList();
    private static int selectedParticleIndex = 0;

    private EditBox textBox;
    private Button saveButton;

    public static boolean lasersEnabled = false;

    private Checkbox checkbox;

    public static String savedText = "";
    public static String selectedType = "Type: Water";
    public static double laserPower = 0.5; // from 0.0 to 1.0

    public SampleUiScreen() {
        super(Component.literal("Sample UI Screen"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        // Layout configuration
        int widgetCount = 5;
        int widgetHeight = 20;
        int spacing = 10;
        int totalHeight = widgetCount * widgetHeight + (widgetCount - 1) * spacing;
        int startY = (this.height - totalHeight) / 2;
        int widgetWidth = 200;

        // Text Box
        int widgetNumber = 1;
        textBox = new EditBox(this.font, centerX - widgetWidth / 2,
                startY + (widgetNumber - 1) * (widgetHeight + spacing), widgetWidth,
                widgetHeight, Component.literal("Enter your message"));
        textBox.setValue(savedText);
        textBox.setResponder(value -> savedText = value);
        this.addRenderableWidget(textBox);

        // Set initial keyboard focus
        this.setInitialFocus(textBox);

        // Cycle Button
        widgetNumber = 2;
        CycleButton<String> cycleButton = CycleButton.<String>builder(option -> Component.literal(option))
            .withValues(List.of("Type: Water", "Type: Grass", "Type: Ghost", "Type: Cheese"))
            .withInitialValue(selectedType)
            .displayOnlyValue()
            .create(
                    centerX - widgetWidth / 2,
                    startY + (widgetNumber - 1) * (widgetHeight + spacing),
                    widgetWidth,
                    widgetHeight,
                    Component.literal("Type"),
                    (button, value) -> {
                        selectedType = value;
                        Minecraft.getInstance().player.displayClientMessage(
                        Component.literal("Set to " + value),
                        false
                        );
                }
            );
        this.addRenderableWidget(cycleButton);

        // Checkbox
        widgetNumber = 3;
        checkbox = Checkbox.builder(Component.literal("Enable Laser Eyes"), this.font)
                .pos(centerX - widgetWidth / 2, startY + (widgetNumber - 1) * (widgetHeight + spacing))
            .selected(lasersEnabled)
            .onValueChange((checkbox, checked) ->
            {
                    lasersEnabled = checked;
                    Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("Laser Eyes: " + (checked ? "Enabled" : "Disabled")),
                    false
                    );
            })
            .maxWidth(widgetWidth)
            .build();
        this.addRenderableWidget(checkbox);

        // Slider - numbers
        widgetNumber = 4;
        AbstractSliderButton laserSlider = new AbstractSliderButton(
            centerX - widgetWidth / 2,
            startY + (widgetNumber - 1) * (widgetHeight + spacing),
            widgetWidth,
            widgetHeight,
            Component.empty(),
            laserPower
        ) {
            {
                    updateMessage();  // Set initial label
            }

            @Override
            protected void updateMessage() {
                    int percent = (int)(value * 100);
                setMessage(Component.literal("Laser Power: " + percent + "%"));
            }

            @Override
            protected void applyValue() {
                laserPower = value;
                int percent = (int)(value * 100);
                Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("Laser Power set to " + percent + "%"),
                    false
                );
            }
        };
        this.addRenderableWidget(laserSlider);

        // Slider - particle types
        widgetNumber = 5;
        AbstractSliderButton particleSlider = new AbstractSliderButton(
                centerX - widgetWidth / 2,
                startY + (widgetNumber - 1) * (widgetHeight + spacing),
                widgetWidth,
                widgetHeight,
                Component.empty(),
                (double) selectedParticleIndex / (particleTypes.size() - 1)
        ) {
            {
                    updateMessage();
            }

            @Override
            protected void updateMessage() {
                selectedParticleIndex = (int) Math.round(this.value * (particleTypes.size() - 1));
                ParticleType<?> particle = particleTypes.get(selectedParticleIndex);
                String name = BuiltInRegistries.PARTICLE_TYPE.getKey(particle).getPath();  // just the variable name
                setMessage(Component.literal("Particle: " + name));
            }

            @Override
            protected void applyValue() {
                selectedParticleIndex = (int) Math.round(this.value * (particleTypes.size() - 1));
                ParticleType<?> selected = particleTypes.get(selectedParticleIndex);
                String name = BuiltInRegistries.PARTICLE_TYPE.getKey(selected).getPath();  // just the variable name
                Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("Selected Particle: " + name),
                    false
                );
            }

        };
        this.addRenderableWidget(particleSlider);

        // Save Button
        widgetNumber = 6;
        saveButton = Button.builder(Component.literal("Save Text"), button -> {
            Minecraft.getInstance().player.displayClientMessage(
                Component.literal("Saved: " + savedText),
                false
            );
            Minecraft.getInstance().setScreen(null);
        }).bounds(centerX - 40, startY + (widgetNumber - 1) * (widgetHeight + spacing), 80, widgetHeight).build();
        this.addRenderableWidget(saveButton);

    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderTransparentBackground(graphics);

        int labelY = textBox.getY() - 20; // 10 pixels above the text box

        // NOTE: Color is ARGB, so you must use four hex values
        // 0xFFFFFF will become 0x00FFFFFF, which is transparent white!
        graphics.drawCenteredString(this.font, "Enter your message:",
                this.width / 2, labelY, 0xFFFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC closes the screen
        if (keyCode == 256) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // helper method so we can find out what particle type the player selected
    public static ParticleType<?> getSelectedParticleType() {
        return particleTypes.get(selectedParticleIndex);
    }
}