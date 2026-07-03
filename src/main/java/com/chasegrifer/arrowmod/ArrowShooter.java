package com.chasegrifer.arrowmod;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class ArrowShooter {
    private static boolean isArrowModeEnabled = false;
    private static long lastShotTime = 0;
    private static final long SHOT_DELAY = 100;
    private static boolean leftAltPressed = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onClientTick());
    }

    private static void onClientTick() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null) return;

        // Левый Alt для включения/отключения режима стрельбы
        boolean isLeftAltPressed = isKeyPressed(GLFW.GLFW_KEY_LEFT_ALT);
        if (isLeftAltPressed && !leftAltPressed) {
            isArrowModeEnabled = !isArrowModeEnabled;
            String message = isArrowModeEnabled ? "§a✓ Режим стрельбы: ВКЛ" : "§c✗ Режим стрельбы: ВЫКЛ";
            client.player.sendMessage(Text.literal(message), true);
        }
        leftAltPressed = isLeftAltPressed;

        // Левая кнопка мыши для выстрела
        if (isArrowModeEnabled && client.mouse.wasLeftButtonClicked()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastShotTime > SHOT_DELAY) {
                shootArrows(client);
                lastShotTime = currentTime;
            }
        }
    }

    private static void shootArrows(MinecraftClient client) {
        int arrowCount = 10;
        
        // Проверяем, есть ли алмазный меч в инвентаре
        boolean hasDiamondSword = false;
        for (ItemStack stack : client.player.getInventory().main) {
            if (stack.getItem() == Items.DIAMOND_SWORD) {
                hasDiamondSword = true;
                break;
            }
        }

        if (!hasDiamondSword) {
            client.player.sendMessage(Text.literal("§cОшибка: Нужен алмазный меч!"), true);
            return;
        }

        // Проверяем, есть ли стрелы
        int arrowsAvailable = countArrows(client.player);
        if (arrowsAvailable < arrowCount) {
            client.player.sendMessage(
                Text.literal("§cОшибка: Нужно минимум " + arrowCount + " стрел! У вас: " + arrowsAvailable),
                true
            );
            return;
        }

        // Стреляем 10 стрел
        for (int i = 0; i < arrowCount; i++) {
            ArrowEntity arrow = new ArrowEntity(client.world, client.player);
            
            // Направление выстрела
            Vec3d lookVec = client.player.getRotationVector();
            double velocity = 2.0;
            
            arrow.setVelocity(
                lookVec.x * velocity,
                lookVec.y * velocity,
                lookVec.z * velocity
            );
            
            arrow.setOwner(client.player);
            arrow.setCritical(true);
            
            client.world.spawnEntity(arrow);
            removeArrow(client.player);
        }

        client.player.sendMessage(Text.literal("§a✓ 10 стрел выпущено!"), true);
    }

    private static int countArrows(net.minecraft.entity.player.PlayerEntity player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem() == Items.ARROW) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void removeArrow(net.minecraft.entity.player.PlayerEntity player) {
        for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem() == Items.ARROW && stack.getCount() > 0) {
                stack.decrement(1);
                return;
            }
        }
    }

    private static boolean isKeyPressed(int keyCode) {
        return GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), keyCode) == GLFW.GLFW_PRESS;
    }
}