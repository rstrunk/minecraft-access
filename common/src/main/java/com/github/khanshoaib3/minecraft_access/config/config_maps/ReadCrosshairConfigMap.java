package com.github.khanshoaib3.minecraft_access.config.config_maps;

import com.github.khanshoaib3.minecraft_access.config.Config;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class ReadCrosshairConfigMap {
    private static ReadCrosshairConfigMap instance;

    @SerializedName("Enabled")
    private boolean enabled;
    @SerializedName("Speak Block Sides")
    private boolean speakSide;
    @SerializedName("Disable Speaking Consecutive Blocks With Same Name")
    private boolean disableSpeakingConsecutiveBlocks;
    @SerializedName("Repeat Speaking Interval (in milliseconds) (0 to disable)")
    private long repeatSpeakingInterval;
    @SerializedName("Relative Position Sound Cue")
    private RCRelativePositionSoundCueConfigMap relativePositionSoundCueConfigMap;
    @SerializedName("Partial Speaking")
    private RCPartialSpeakingConfigMap partialSpeakingConfigMap;

    private ReadCrosshairConfigMap() {
    }

    public static ReadCrosshairConfigMap buildDefault() {
        ReadCrosshairConfigMap m = new ReadCrosshairConfigMap();
        m.setEnabled(true);
        m.setSpeakSide(true);
        m.setDisableSpeakingConsecutiveBlocks(false);
        m.setRepeatSpeakingInterval(0L);
        m.relativePositionSoundCueConfigMap = RCRelativePositionSoundCueConfigMap.buildDefault();
        m.partialSpeakingConfigMap = RCPartialSpeakingConfigMap.buildDefault();

        setInstance(m);
        return m;
    }

    public static ReadCrosshairConfigMap getInstance() {
        if (instance == null) Config.getInstance().loadConfig();
        return instance;
    }

    public static void setInstance(ReadCrosshairConfigMap map) {
        RCRelativePositionSoundCueConfigMap.setInstance(map.relativePositionSoundCueConfigMap);
        RCPartialSpeakingConfigMap.setInstance(map.partialSpeakingConfigMap);
        instance = map;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSpeakSide() {
        return speakSide;
    }

    public void setSpeakSide(boolean speakSide) {
        this.speakSide = speakSide;
    }

    public boolean isDisableSpeakingConsecutiveBlocks() {
        return disableSpeakingConsecutiveBlocks;
    }

    public void setDisableSpeakingConsecutiveBlocks(boolean disableSpeakingConsecutiveBlocks) {
        this.disableSpeakingConsecutiveBlocks = disableSpeakingConsecutiveBlocks;
    }

    public long getRepeatSpeakingInterval() {
        return repeatSpeakingInterval;
    }

    public void setRepeatSpeakingInterval(long repeatSpeakingInterval) {
        this.repeatSpeakingInterval = repeatSpeakingInterval;
    }

    public void resetMissingSectionsToDefault() {
        if (Objects.isNull(this.partialSpeakingConfigMap)) {
            this.partialSpeakingConfigMap = RCPartialSpeakingConfigMap.buildDefault();
        }
        if (Objects.isNull(this.relativePositionSoundCueConfigMap)) {
            this.relativePositionSoundCueConfigMap = RCRelativePositionSoundCueConfigMap.buildDefault();
        }
    }
}
