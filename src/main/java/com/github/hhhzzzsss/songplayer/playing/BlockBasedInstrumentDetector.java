package com.github.hhhzzzsss.songplayer.playing;

import com.github.hhhzzzsss.songplayer.song.Instrument;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;

/**
 * 基于方块的音符盒音色检测器
 * 根据音符盒下方的方块类型来判断音色
 */
public class BlockBasedInstrumentDetector {
    public static BlockState getSupportBlockState(Instrument instrument) {
        return switch (instrument) {
            case HARP -> Blocks.DIRT.getDefaultState();
            case BASEDRUM -> Blocks.STONE.getDefaultState();
            case SNARE -> Blocks.SAND.getDefaultState();
            case HAT -> Blocks.GLASS.getDefaultState();
            case BASS -> Blocks.OAK_PLANKS.getDefaultState();
            case FLUTE -> Blocks.CLAY.getDefaultState();
            case BELL -> Blocks.GOLD_BLOCK.getDefaultState();
            case GUITAR -> Blocks.WHITE_WOOL.getDefaultState();
            case CHIME -> Blocks.PACKED_ICE.getDefaultState();
            case XYLOPHONE -> Blocks.BONE_BLOCK.getDefaultState();
            case IRON_XYLOPHONE -> Blocks.IRON_BLOCK.getDefaultState();
            case COW_BELL -> Blocks.SOUL_SAND.getDefaultState();
            case DIDGERIDOO -> Blocks.PUMPKIN.getDefaultState();
            case BIT -> Blocks.EMERALD_BLOCK.getDefaultState();
            case BANJO -> Blocks.HAY_BLOCK.getDefaultState();
            case PLING -> Blocks.GLOWSTONE.getDefaultState();
        };
    }

    public static boolean supportsInstrument(BlockState belowBlockState, Instrument instrument) {
        if (belowBlockState.isAir() || belowBlockState.isLiquid()) {
            return false;
        }
        return getInstrumentFromBlock(belowBlockState) == instrument;
    }


    /**
     * 根据音符盒下方的方块状态获取对应的乐器
     * @param belowBlockState 音符盒下方的方块状态
     * @return 对应的乐器，如果无法识别则返回HARP（默认音色）
     */
    public static Instrument getInstrumentFromBlock(BlockState belowBlockState) {
        Block block = belowBlockState.getBlock();
        
        // 检查各种方块类型
        if (isWoodBlock(belowBlockState)) {
            return Instrument.BASS; // 木头 -> 贝斯
        }
        else if (isSandBlock(block)) {
            return Instrument.SNARE; // 沙子/沙砾 -> 小鼓
        }
        else if (isGlassBlock(belowBlockState)) {
            return Instrument.HAT; // 玻璃 -> 踩镲
        }
        else if (isStoneBlock(belowBlockState)) {
            return Instrument.BASEDRUM; // 石头 -> 底鼓
        }
        else if (block == Blocks.CLAY) {
            return Instrument.FLUTE; // 粘土 -> 长笛
        }
        else if (block == Blocks.GOLD_BLOCK) {
            return Instrument.BELL; // 金块 -> 铃铛
        }
        else if (isWoolBlock(belowBlockState)) {
            return Instrument.GUITAR; // 羊毛 -> 吉他
        }
        else if (block == Blocks.PACKED_ICE) {
            return Instrument.CHIME; // 浮冰 -> 钟琴
        }
        else if (block == Blocks.BONE_BLOCK) {
            return Instrument.XYLOPHONE; // 骨块 -> 木琴
        }
        else if (block == Blocks.IRON_BLOCK) {
            return Instrument.IRON_XYLOPHONE; // 铁块 -> 铁木琴
        }
        else if (block == Blocks.SOUL_SAND || block == Blocks.SOUL_SOIL) {
            return Instrument.COW_BELL; // 灵魂沙 -> 牛铃
        }
        else if (block == Blocks.PUMPKIN || block == Blocks.CARVED_PUMPKIN) {
            return Instrument.DIDGERIDOO; // 南瓜 -> 迪吉里杜管
        }
        else if (block == Blocks.EMERALD_BLOCK) {
            return Instrument.BIT; // 绿宝石块 -> 方波
        }
        else if (block == Blocks.HAY_BLOCK) {
            return Instrument.BANJO; // 干草块 -> 班卓琴
        }
        else if (block == Blocks.GLOWSTONE) {
            return Instrument.PLING; // 荧石 -> 电子琴音
        }
        else {
            return Instrument.HARP; // 默认音色（泥土等其他方块）
        }
    }
    
    /**
     * 检查是否为木头类方块
     */
    private static boolean isWoodBlock(BlockState blockState) {
        return blockState.isIn(BlockTags.LOGS) || 
               blockState.isIn(BlockTags.PLANKS) || 
               blockState.isIn(BlockTags.WOODEN_SLABS) ||
               blockState.isIn(BlockTags.WOODEN_STAIRS) ||
               blockState.isIn(BlockTags.WOODEN_FENCES) ||
               blockState.isIn(BlockTags.WOODEN_DOORS) ||
               blockState.isIn(BlockTags.WOODEN_TRAPDOORS) ||
               blockState.getBlock() == Blocks.CRAFTING_TABLE ||
               blockState.getBlock() == Blocks.BOOKSHELF ||
               blockState.getBlock() == Blocks.CHEST ||
               blockState.getBlock() == Blocks.TRAPPED_CHEST;
    }
    
    /**
     * 检查是否为沙子类方块
     */
    private static boolean isSandBlock(Block block) {
        return block == Blocks.SAND || 
               block == Blocks.RED_SAND || 
               block == Blocks.GRAVEL;
    }
    
    /**
     * 检查是否为玻璃类方块
     */
    private static boolean isGlassBlock(BlockState blockState) {
        return blockState.getBlock() == Blocks.GLASS ||
               blockState.getBlock().getTranslationKey().contains("glass");
    }
    
    /**
     * 检查是否为石头类方块
     */
    private static boolean isStoneBlock(BlockState blockState) {
        return blockState.isIn(BlockTags.STONE_ORE_REPLACEABLES) ||
               blockState.getBlock() == Blocks.STONE ||
               blockState.getBlock() == Blocks.COBBLESTONE ||
               blockState.getBlock() == Blocks.GRANITE ||
               blockState.getBlock() == Blocks.DIORITE ||
               blockState.getBlock() == Blocks.ANDESITE ||
               blockState.getBlock() == Blocks.DEEPSLATE ||
               blockState.getBlock() == Blocks.COBBLED_DEEPSLATE ||
               blockState.getBlock() == Blocks.BLACKSTONE ||
               blockState.getBlock() == Blocks.NETHERRACK ||
               blockState.getBlock() == Blocks.BASALT ||
               blockState.getBlock() == Blocks.END_STONE;
    }
    
    /**
     * 检查是否为羊毛类方块
     */
    private static boolean isWoolBlock(BlockState blockState) {
        return blockState.isIn(BlockTags.WOOL);
    }
}
