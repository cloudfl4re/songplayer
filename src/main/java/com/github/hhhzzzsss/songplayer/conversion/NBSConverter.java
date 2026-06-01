package com.github.hhhzzzsss.songplayer.conversion;

import com.github.hhhzzzsss.songplayer.Config;
import com.github.hhhzzzsss.songplayer.song.Instrument;
import com.github.hhhzzzsss.songplayer.song.Note;
import com.github.hhhzzzsss.songplayer.song.Song;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Locale;

public class NBSConverter {
    public static Instrument[] instrumentIndex = new Instrument[] {
            Instrument.HARP,
            Instrument.BASS,
            Instrument.BASEDRUM,
            Instrument.SNARE,
            Instrument.HAT,
            Instrument.GUITAR,
            Instrument.FLUTE,
            Instrument.BELL,
            Instrument.CHIME,
            Instrument.XYLOPHONE,
            Instrument.IRON_XYLOPHONE,
            Instrument.COW_BELL,
            Instrument.DIDGERIDOO,
            Instrument.BIT,
            Instrument.BANJO,
            Instrument.PLING,
    };

    private static class NBSNote {
        public int tick;
        public short layer;
        public int instrument;
        public int key;
        public int velocity = 100;
        public int panning = 100;
        public short pitch = 0;
    }

    private static class NBSLayer {
        public String name;
        public int lock = 0;
        public int volume;
        public int stereo = 100;
    }

    private static class NBSCustomInstrument {
        public String name;
        public String soundFile;
        public int key;
        public boolean pressKey;
    }

    public static Song getSongFromBytes(byte[] bytes, String fileName) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        short songLength = 0;
        byte format = 0;
        int vanillaInstrumentCount = 10;
        songLength = buffer.getShort(); // If it's not 0, then it uses the old format
        if (songLength == 0) {
            format = buffer.get();
        }

        if (format >= 1) {
            vanillaInstrumentCount = Byte.toUnsignedInt(buffer.get());
        }
        if (format >= 3) {
            songLength = buffer.getShort();
        }

        short layerCount = buffer.getShort();
        String songName = getString(buffer, bytes.length);
        String songAuthor = getString(buffer, bytes.length);
        String songOriginalAuthor = getString(buffer, bytes.length);
        String songDescription = getString(buffer, bytes.length);
        short tempo = buffer.getShort();
        byte autoSaving = buffer.get();
        byte autoSavingDuration = buffer.get();
        byte timeSignature = buffer.get();
        int minutesSpent = buffer.getInt();
        int leftClicks = buffer.getInt();
        int rightClicks = buffer.getInt();
        int blocksAdded = buffer.getInt();
        int blocksRemoved = buffer.getInt();
        String origFileName = getString(buffer, bytes.length);

        byte loop = 0;
        byte maxLoopCount = 0;
        short loopStartTick = 0;
        if (format >= 4) {
            loop = buffer.get();
            maxLoopCount = buffer.get();
            loopStartTick = buffer.getShort();
        }

        ArrayList<NBSNote> nbsNotes = new ArrayList<>();
        short tick = -1;
        while (true) {
            int tickJumps = buffer.getShort();
            if (tickJumps == 0) break;
            tick += tickJumps;

            short layer = -1;
            while (true) {
                int layerJumps = buffer.getShort();
                if (layerJumps == 0) break;
                layer += layerJumps;
                NBSNote note = new NBSNote();
                note.tick = tick;
                note.layer = layer;
                note.instrument = Byte.toUnsignedInt(buffer.get());
                note.key = Byte.toUnsignedInt(buffer.get());
                if (format >= 4) {
                    note.velocity = Byte.toUnsignedInt(buffer.get());
                    note.panning = Byte.toUnsignedInt(buffer.get());
                    note.pitch = buffer.getShort();
                }
                nbsNotes.add(note);
            }
        }

        ArrayList<NBSLayer> nbsLayers = new ArrayList<>();
        if (buffer.hasRemaining()) {
            for (int i=0; i<layerCount; i++) {
                NBSLayer layer = new NBSLayer();
                layer.name = getString(buffer, bytes.length);
                if (format >= 4) {
                    layer.lock = Byte.toUnsignedInt(buffer.get());
                }
                layer.volume = Byte.toUnsignedInt(buffer.get());
                if (format >= 2) {
                    layer.stereo = Byte.toUnsignedInt(buffer.get());
                }
                nbsLayers.add(layer);
            }
        }

        ArrayList<NBSCustomInstrument> customInstruments = new ArrayList<>();
        if (buffer.hasRemaining()) {
            int customInstrumentCount = Byte.toUnsignedInt(buffer.get());
            for (int i=0; i<customInstrumentCount; i++) {
                NBSCustomInstrument customInstrument = new NBSCustomInstrument();
                customInstrument.name = getString(buffer, bytes.length);
                customInstrument.soundFile = getString(buffer, bytes.length);
                customInstrument.key = Byte.toUnsignedInt(buffer.get());
                customInstrument.pressKey = buffer.get() != 0;
                customInstruments.add(customInstrument);
            }
        }

        Song song = new Song(songName.trim().length() > 0 ? songName : fileName);
        if (loop > 0) {
            song.looping = true;
            song.loopPosition = getMilliTime(loopStartTick, tempo);
            song.loopCount = maxLoopCount;
        }
        boolean strictMapping = Config.getConfig().strictNbsMapping;
        for (NBSNote note : nbsNotes) {
            Instrument instrument = getInstrument(note.instrument, vanillaInstrumentCount, customInstruments, strictMapping);
            if (instrument == null) {
                continue;
            }

            int key = getEffectiveKey(note, vanillaInstrumentCount, customInstruments, strictMapping);
            if (strictMapping) {
                if (key < 33 || key > 57) {
                    continue;
                }
            }
            else {
                while (key < 33) {
                    key += 12;
                }
                while (key > 57) {
                    key -= 12;
                }
            }

            int layerVolume = 100;
            if (nbsLayers.size() > note.layer) {
                layerVolume = nbsLayers.get(note.layer).volume;
            }

            int pitch = key-33;
            int noteId = pitch + instrument.instrumentId*25;
            song.add(new Note(noteId, getMilliTime(note.tick, tempo), layerVolume));
        }

        if (song.size() > 0) {
            song.length = song.get(song.size()-1).time + 50;
        }

        return song;
    }

    private static Instrument getInstrument(int nbsInstrument, int vanillaInstrumentCount, ArrayList<NBSCustomInstrument> customInstruments, boolean strictMapping) {
        if (!strictMapping) {
            return nbsInstrument < instrumentIndex.length ? instrumentIndex[nbsInstrument] : null;
        }

        if (nbsInstrument < vanillaInstrumentCount) {
            return nbsInstrument < instrumentIndex.length ? instrumentIndex[nbsInstrument] : null;
        }

        NBSCustomInstrument customInstrument = getCustomInstrument(nbsInstrument, vanillaInstrumentCount, customInstruments);
        if (customInstrument == null) {
            return null;
        }

        Instrument instrument = getInstrumentFromName(customInstrument.name);
        if (instrument != null) {
            return instrument;
        }
        return getInstrumentFromName(customInstrument.soundFile);
    }

    private static int getEffectiveKey(NBSNote note, int vanillaInstrumentCount, ArrayList<NBSCustomInstrument> customInstruments, boolean strictMapping) {
        if (!strictMapping) {
            return note.key;
        }

        NBSCustomInstrument customInstrument = getCustomInstrument(note.instrument, vanillaInstrumentCount, customInstruments);
        if (customInstrument == null) {
            return note.key;
        }

        return note.key + customInstrument.key - 45;
    }

    private static NBSCustomInstrument getCustomInstrument(int nbsInstrument, int vanillaInstrumentCount, ArrayList<NBSCustomInstrument> customInstruments) {
        int customInstrumentIndex = nbsInstrument - vanillaInstrumentCount;
        if (customInstrumentIndex < 0 || customInstrumentIndex >= customInstruments.size()) {
            return null;
        }
        return customInstruments.get(customInstrumentIndex);
    }

    private static Instrument getInstrumentFromName(String name) {
        return switch (normalizeInstrumentName(name)) {
            case "harp", "piano" -> Instrument.HARP;
            case "bass", "doublebass" -> Instrument.BASS;
            case "basedrum", "bassdrum", "bd" -> Instrument.BASEDRUM;
            case "snare", "snaredrum" -> Instrument.SNARE;
            case "hat", "hihat", "click" -> Instrument.HAT;
            case "guitar" -> Instrument.GUITAR;
            case "flute" -> Instrument.FLUTE;
            case "bell" -> Instrument.BELL;
            case "chime" -> Instrument.CHIME;
            case "xylophone" -> Instrument.XYLOPHONE;
            case "ironxylophone" -> Instrument.IRON_XYLOPHONE;
            case "cowbell" -> Instrument.COW_BELL;
            case "didgeridoo" -> Instrument.DIDGERIDOO;
            case "bit" -> Instrument.BIT;
            case "banjo" -> Instrument.BANJO;
            case "pling" -> Instrument.PLING;
            default -> null;
        };
    }

    private static String normalizeInstrumentName(String name) {
        if (name == null) {
            return "";
        }

        String normalized = name.toLowerCase(Locale.ROOT).replace('\\', '/');
        int slashIndex = normalized.lastIndexOf('/');
        if (slashIndex >= 0) {
            normalized = normalized.substring(slashIndex + 1);
        }
        if (normalized.endsWith(".ogg")) {
            normalized = normalized.substring(0, normalized.length() - 4);
        }
        int namespaceIndex = normalized.lastIndexOf(':');
        if (namespaceIndex >= 0) {
            normalized = normalized.substring(namespaceIndex + 1);
        }
        int dotIndex = normalized.lastIndexOf('.');
        if (dotIndex >= 0) {
            normalized = normalized.substring(dotIndex + 1);
        }
        return normalized.replaceAll("[^a-z0-9]", "");
    }

    private static String getString(ByteBuffer buffer, int maxSize) throws IOException {
        int length = buffer.getInt();
        if (length > maxSize) {
            throw new IOException("String is too large");
        }
        byte[] arr = new byte[length];
        buffer.get(arr, 0, length);
        return new String(arr);
    }

    private static int getMilliTime(int tick, int tempo) {
        return 1000 * tick * 100 / tempo;
    }
}
