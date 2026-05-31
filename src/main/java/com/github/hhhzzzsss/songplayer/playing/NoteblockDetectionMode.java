package com.github.hhhzzzsss.songplayer.playing;

public enum NoteblockDetectionMode {
    NBT_DATA("NBT data"),
    BLOCK_BASED("Block based");

    private final String displayName;

    NoteblockDetectionMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
