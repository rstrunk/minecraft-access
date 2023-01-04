package com.github.khanshoaib3.minecraft_access;

import com.github.khanshoaib3.minecraft_access.features.*;
import com.github.khanshoaib3.minecraft_access.features.InventoryControls.InventoryControls;
import com.github.khanshoaib3.minecraft_access.features.PointOfInterest.POIBlocks;
import com.github.khanshoaib3.minecraft_access.features.PointOfInterest.POIEntities;
import com.github.khanshoaib3.minecraft_access.screen_reader.ScreenReaderController;
import com.github.khanshoaib3.minecraft_access.screen_reader.ScreenReaderInterface;
import com.mojang.text2speech.Narrator;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainClass {
    public static final String MOD_ID = "minecraft_access";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ScreenReaderInterface screenReader = null;

    public static CameraControls cameraControls = null;
    public static InventoryControls inventoryControls = null;
    public static ReadCrosshair readCrosshair = null;
    public static BiomeIndicator biomeIndicator = null;
    public static FacingDirection facingDirection = null;
    public static PositionNarrator positionNarrator = null;
    public static HealthNHunger healthNHunger = null;
    public static PlayerWarnings playerWarnings = null;
    public static NarratorMenu narratorMenu = null;
    public static POIBlocks poiBlocks = null;
    public static POIEntities poiEntities = null;

    public static boolean debugMode = true; // TODO add option to toggle this
    public static boolean isForge = false;
    public static boolean interrupt = true;
    private static boolean alreadyDisabledAdvancementKey = false;

    /**
     * Initializes the mod
     */
    public static void init(){
        String msg = "Initializing Minecraft Access";
        MainClass.infoLog(msg);

        MainClass.setScreenReader(ScreenReaderController.getAvailable());
        if (MainClass.getScreenReader() != null && MainClass.getScreenReader().isInitialized())
            MainClass.getScreenReader().say(msg, true);

        MainClass.cameraControls = new CameraControls();
        MainClass.inventoryControls = new InventoryControls();
        MainClass.readCrosshair = new ReadCrosshair();
        MainClass.biomeIndicator = new BiomeIndicator();
        MainClass.facingDirection = new FacingDirection();
        MainClass.positionNarrator = new PositionNarrator();
        MainClass.healthNHunger = new HealthNHunger();
        MainClass.playerWarnings = new PlayerWarnings();
        MainClass.narratorMenu = new NarratorMenu();
        MainClass.poiBlocks = new POIBlocks();
        MainClass.poiEntities = new POIEntities();

        ClientTickEvent.CLIENT_POST.register(MainClass::clientTickEventsMethod);

        // This executes when minecraft closes
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (MainClass.getScreenReader() != null && MainClass.getScreenReader().isInitialized())
                MainClass.getScreenReader().closeScreenReader();
        }, "Shutdown-thread"));
    }

    /**
     * This method gets called at the end of every tick
     *
     * @param minecraftClient The current minecraft client object
     */
    public static void clientTickEventsMethod(MinecraftClient minecraftClient) {
        if(!MainClass.alreadyDisabledAdvancementKey && minecraftClient.options!=null) {
            minecraftClient.options.advancementsKey.setBoundKey(InputUtil.fromTranslationKey("key.keyboard.unknown"));
            MainClass.alreadyDisabledAdvancementKey = true;
            infoLog("Unbound advancements key");
        }

        MenuFix.update(minecraftClient);

        if (inventoryControls != null) inventoryControls.update();

        if (cameraControls != null) cameraControls.update();

        if (readCrosshair != null) readCrosshair.update();

        if (biomeIndicator != null) biomeIndicator.update();

        if (facingDirection != null) facingDirection.update();

        if (positionNarrator != null) positionNarrator.update();

        if (healthNHunger != null) healthNHunger.update();

        if (playerWarnings != null) playerWarnings.update();

        if (narratorMenu != null) narratorMenu.update();

        if (poiBlocks != null) poiBlocks.update();

        if (poiEntities != null) poiEntities.update();
    }

    public static void infoLog(String msg) {
        if (debugMode) LOGGER.info(msg);
    }

    public static void errorLog(String msg) {
        LOGGER.error(msg);
    }

    public static ScreenReaderInterface getScreenReader() {
        return MainClass.screenReader;
    } //TODO remove this

    public static void setScreenReader(ScreenReaderInterface screenReader) {
        MainClass.screenReader = screenReader;
    }

    public static void speakWithNarrator(String text, boolean interrupt){
        MainClass.interrupt = interrupt;
        if(isForge){
            MinecraftClient.getInstance().getNarratorManager().narrate(text);
            return;
        }

        Narrator.getNarrator().say(text, interrupt);
    }
}
