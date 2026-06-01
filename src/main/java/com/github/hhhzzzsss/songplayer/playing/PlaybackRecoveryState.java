package com.github.hhhzzzsss.songplayer.playing;

import com.github.hhhzzzsss.songplayer.SongPlayer;
import com.github.hhhzzzsss.songplayer.Util;
import com.github.hhhzzzsss.songplayer.song.Song;
import com.google.gson.Gson;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlaybackRecoveryState {
    private static final int VERSION = 1;
    private static final Gson GSON = new Gson();
    private static final Path RECOVERY_FILE = SongPlayer.SONGPLAYER_DIR.resolve("recovery.json");

    public int version = VERSION;
    public String serverIdentifier;
    public String worldName;
    public int stageX;
    public int stageY;
    public int stageZ;
    public NoteblockDetectionMode noteblockDetectionMode;
    public ArrayList<NotePosition> noteblockPositions = new ArrayList<>();
    public ArrayList<BlockRecord> originalBlocks = new ArrayList<>();
    public boolean cleaningUp;
    public boolean building;
    public String songSourceLocation;
    public long songTime;
    public boolean songLooping;
    public long songLoopPosition;
    public int songLoopCount;
    public int songCurrentLoop;

    public static void save(SongHandler handler) {
        try {
            if (!shouldSave(handler)) {
                delete();
                return;
            }

            PlaybackRecoveryState state = new PlaybackRecoveryState(handler);
            BufferedWriter writer = Files.newBufferedWriter(RECOVERY_FILE);
            writer.write(GSON.toJson(state));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PlaybackRecoveryState load() {
        if (!Files.exists(RECOVERY_FILE)) {
            return null;
        }

        try {
            BufferedReader reader = Files.newBufferedReader(RECOVERY_FILE);
            PlaybackRecoveryState state = GSON.fromJson(reader, PlaybackRecoveryState.class);
            reader.close();
            if (state == null || state.version != VERSION) {
                return null;
            }
            return state;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void delete() {
        try {
            Files.deleteIfExists(RECOVERY_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean shouldSave(SongHandler handler) {
        return handler.lastStage != null
                && (handler.dirty || handler.cleaningUp || handler.currentSong != null)
                && (!handler.originalBlocks.isEmpty() || handler.currentSong != null);
    }

    private PlaybackRecoveryState() {
    }

    private PlaybackRecoveryState(SongHandler handler) {
        Stage recoveryStage = handler.lastStage != null ? handler.lastStage : handler.stage;
        serverIdentifier = recoveryStage.serverIdentifier;
        worldName = recoveryStage.worldName;
        stageX = recoveryStage.position.getX();
        stageY = recoveryStage.position.getY();
        stageZ = recoveryStage.position.getZ();
        noteblockDetectionMode = recoveryStage.noteblockDetectionMode;
        cleaningUp = handler.cleaningUp;
        building = handler.building;

        for (Map.Entry<Integer, BlockPos> entry : recoveryStage.noteblockPositions.entrySet()) {
            noteblockPositions.add(new NotePosition(entry.getKey(), entry.getValue()));
        }

        for (Map.Entry<BlockPos, BlockState> entry : handler.originalBlocks.entrySet()) {
            if (entry.getValue() != null) {
                originalBlocks.add(new BlockRecord(entry.getKey(), Block.getRawIdFromState(entry.getValue())));
            }
        }

        Song song = handler.currentSong;
        if (song != null) {
            if (!song.paused) {
                song.advanceTime();
            }
            songSourceLocation = song.sourceLocation;
            songTime = song.time;
            songLooping = song.looping;
            songLoopPosition = song.loopPosition;
            songLoopCount = song.loopCount;
            songCurrentLoop = song.currentLoop;
        }
    }

    public boolean matchesCurrentWorld() {
        return Objects.equals(serverIdentifier, Util.getServerIdentifier())
                && Objects.equals(worldName, Util.getWorldName());
    }

    public boolean hasSongSource() {
        return songSourceLocation != null && !songSourceLocation.isEmpty();
    }

    public void restoreStage(SongHandler handler, boolean activeStage) {
        Stage restoredStage = new Stage();
        restoredStage.position = new BlockPos(stageX, stageY, stageZ);
        restoredStage.serverIdentifier = serverIdentifier;
        restoredStage.worldName = worldName;
        restoredStage.noteblockDetectionMode = noteblockDetectionMode == null ? NoteblockDetectionMode.NBT_DATA : noteblockDetectionMode;
        restoredStage.noteblockPositions = new HashMap<>();
        for (NotePosition notePosition : noteblockPositions) {
            restoredStage.noteblockPositions.put(notePosition.noteId, notePosition.toBlockPos());
        }

        handler.lastStage = restoredStage;
        handler.stage = activeStage ? restoredStage : null;
        handler.originalBlocks.clear();
        for (BlockRecord blockRecord : originalBlocks) {
            handler.originalBlocks.put(blockRecord.toBlockPos(), Block.getStateFromRawId(blockRecord.rawStateId));
        }
        handler.dirty = true;
    }

    public static class NotePosition {
        public int noteId;
        public int x;
        public int y;
        public int z;

        public NotePosition(int noteId, BlockPos pos) {
            this.noteId = noteId;
            x = pos.getX();
            y = pos.getY();
            z = pos.getZ();
        }

        public BlockPos toBlockPos() {
            return new BlockPos(x, y, z);
        }
    }

    public static class BlockRecord {
        public int x;
        public int y;
        public int z;
        public int rawStateId;

        public BlockRecord(BlockPos pos, int rawStateId) {
            x = pos.getX();
            y = pos.getY();
            z = pos.getZ();
            this.rawStateId = rawStateId;
        }

        public BlockPos toBlockPos() {
            return new BlockPos(x, y, z);
        }
    }
}
