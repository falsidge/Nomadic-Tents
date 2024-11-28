package nomadictents.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nomadictents.NTRegistry;
import nomadictents.NomadicTents;
import nomadictents.block.IndluWallBlock;
import nomadictents.block.TentDoorBlock;
import nomadictents.block.YurtRoofBlock;
import nomadictents.block.YurtWallBlock;
import nomadictents.dimension.DynamicDimensionHelper;
import nomadictents.tileentity.TentDoorBlockEntity;
import nomadictents.util.Tent;
import nomadictents.util.TentSize;
import nomadictents.util.TentType;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public final class TentPlacer {

    /**
     * The direction to place tents inside a tent dimension.
     * Left = -Z, Right = +Z, Back of tent = +X, Front of tent = -X
     */
    public static final Direction TENT_DIRECTION = Direction.EAST;
    /**
     * The Y-position of tents inside a tent dimension
     **/
    public static final int TENT_Y = 64;
    private static final String MODID = NomadicTents.MODID;

    private static TentPlacer instance;

    /**
     * Nested map where keys = {TentSize,TentType} and value = {Door BlockState Supplier}
     */
    public static final Map<TentSize, Map<TentType, Supplier<Block>>> DOORS = new ImmutableMap.Builder<TentSize, Map<TentType, Supplier<Block>>>()
            .put(TentSize.TINY, new ImmutableMap.Builder<TentType, Supplier<Block>>()
                    .put(TentType.TEPEE, RegistryObject.create(new ResourceLocation(MODID, "tiny_tepee_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.YURT, RegistryObject.create(new ResourceLocation(MODID, "tiny_yurt_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.BEDOUIN, RegistryObject.create(new ResourceLocation(MODID, "tiny_bedouin_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.INDLU, RegistryObject.create(new ResourceLocation(MODID, "tiny_indlu_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.SHAMIYANA, RegistryObject.create(new ResourceLocation(MODID, "tiny_shamiyana_door"), ForgeRegistries.BLOCKS))
                    .build())
            .put(TentSize.SMALL, new ImmutableMap.Builder<TentType, Supplier<Block>>()
                    .put(TentType.TEPEE, RegistryObject.create(new ResourceLocation(MODID, "small_tepee_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.YURT, RegistryObject.create(new ResourceLocation(MODID, "small_yurt_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.BEDOUIN, RegistryObject.create(new ResourceLocation(MODID, "small_bedouin_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.INDLU, RegistryObject.create(new ResourceLocation(MODID, "small_indlu_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.SHAMIYANA, RegistryObject.create(new ResourceLocation(MODID, "small_shamiyana_door"), ForgeRegistries.BLOCKS))
                    .build())
            .put(TentSize.MEDIUM, new ImmutableMap.Builder<TentType, Supplier<Block>>()
                    .put(TentType.TEPEE, RegistryObject.create(new ResourceLocation(MODID, "medium_tepee_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.YURT, RegistryObject.create(new ResourceLocation(MODID, "medium_yurt_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.BEDOUIN, RegistryObject.create(new ResourceLocation(MODID, "medium_bedouin_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.INDLU, RegistryObject.create(new ResourceLocation(MODID, "medium_indlu_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.SHAMIYANA, RegistryObject.create(new ResourceLocation(MODID, "medium_shamiyana_door"), ForgeRegistries.BLOCKS))
                    .build())
            .put(TentSize.LARGE, new ImmutableMap.Builder<TentType, Supplier<Block>>()
                    .put(TentType.TEPEE, RegistryObject.create(new ResourceLocation(MODID, "large_tepee_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.YURT, RegistryObject.create(new ResourceLocation(MODID, "large_yurt_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.BEDOUIN, RegistryObject.create(new ResourceLocation(MODID, "large_bedouin_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.INDLU, RegistryObject.create(new ResourceLocation(MODID, "large_indlu_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.SHAMIYANA, RegistryObject.create(new ResourceLocation(MODID, "large_shamiyana_door"), ForgeRegistries.BLOCKS))
                    .build())
            .put(TentSize.GIANT, new ImmutableMap.Builder<TentType, Supplier<Block>>()
                    .put(TentType.TEPEE, RegistryObject.create(new ResourceLocation(MODID, "giant_tepee_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.YURT, RegistryObject.create(new ResourceLocation(MODID, "giant_yurt_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.BEDOUIN, RegistryObject.create(new ResourceLocation(MODID, "giant_bedouin_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.INDLU, RegistryObject.create(new ResourceLocation(MODID, "giant_indlu_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.SHAMIYANA, RegistryObject.create(new ResourceLocation(MODID, "giant_shamiyana_door"), ForgeRegistries.BLOCKS))
                    .build())
            .put(TentSize.MEGA, new ImmutableMap.Builder<TentType, Supplier<Block>>()
                    .put(TentType.TEPEE, RegistryObject.create(new ResourceLocation(MODID, "mega_tepee_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.YURT, RegistryObject.create(new ResourceLocation(MODID, "mega_yurt_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.BEDOUIN, RegistryObject.create(new ResourceLocation(MODID, "mega_bedouin_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.INDLU, RegistryObject.create(new ResourceLocation(MODID, "mega_indlu_door"), ForgeRegistries.BLOCKS))
                    .put(TentType.SHAMIYANA, RegistryObject.create(new ResourceLocation(MODID, "mega_shamiyana_door"), ForgeRegistries.BLOCKS))
                    .build())
            .build();

    /**
     * Map where keys = {Frame Block ID} and value = {Function with boolean "outside" that returns Tent Block}
     */
    public static final Map<ResourceLocation, Function<Boolean, BlockState>> FRAME_TO_BLOCK = new ImmutableMap.Builder<ResourceLocation, Function<Boolean, BlockState>>()
            .put(new ResourceLocation(MODID, "yurt_wall_frame"), outside -> NTRegistry.YURT_WALL.get().defaultBlockState().setValue(YurtWallBlock.OUTSIDE, outside))
            .put(new ResourceLocation(MODID, "yurt_roof_frame"), outside -> NTRegistry.YURT_ROOF.get().defaultBlockState().setValue(YurtRoofBlock.OUTSIDE, outside))
            .put(new ResourceLocation(MODID, "tepee_wall_frame"), outside -> NTRegistry.BLANK_TEPEE_WALL.get().defaultBlockState())
            .put(new ResourceLocation(MODID, "bedouin_wall_frame"), outside -> NTRegistry.BEDOUIN_WALL.get().defaultBlockState())
            .put(new ResourceLocation(MODID, "bedouin_roof_frame"), outside -> NTRegistry.BEDOUIN_ROOF.get().defaultBlockState())
            .put(new ResourceLocation(MODID, "indlu_wall_frame"), outside -> NTRegistry.INDLU_WALL.get().defaultBlockState().setValue(IndluWallBlock.OUTSIDE, outside))
            .put(new ResourceLocation(MODID, "shamiyana_wall_frame"), outside -> NTRegistry.WHITE_SHAMIYANA_WALL.get().defaultBlockState())
            .build();

    /**
     * Map where keys = {Tent Block ID} and value = {Frame Block Supplier}
     */
    public static final Map<ResourceLocation, RegistryObject<Block>> BLOCK_TO_FRAME = new ImmutableMap.Builder<ResourceLocation, RegistryObject<Block>>()
            .put(new ResourceLocation(MODID, "yurt_wall"), RegistryObject.create(new ResourceLocation(MODID, "yurt_wall_frame"), ForgeRegistries.BLOCKS))
            .put(new ResourceLocation(MODID, "yurt_roof"), RegistryObject.create(new ResourceLocation(MODID, "yurt_roof_frame"), ForgeRegistries.BLOCKS))
            .put(new ResourceLocation(MODID, "blank_tepee_wall"), RegistryObject.create(new ResourceLocation(MODID, "tepee_wall_frame"), ForgeRegistries.BLOCKS))
            .put(new ResourceLocation(MODID, "bedouin_wall"), RegistryObject.create(new ResourceLocation(MODID, "bedouin_wall_frame"), ForgeRegistries.BLOCKS))
            .put(new ResourceLocation(MODID, "bedouin_roof"), RegistryObject.create(new ResourceLocation(MODID, "bedouin_roof_frame"), ForgeRegistries.BLOCKS))
            .put(new ResourceLocation(MODID, "indlu_wall"), RegistryObject.create(new ResourceLocation(MODID, "indlu_wall_frame"), ForgeRegistries.BLOCKS))
            .put(new ResourceLocation(MODID, "white_shamiyana_wall"), RegistryObject.create(new ResourceLocation(MODID, "shamiyana_wall_frame"), ForgeRegistries.BLOCKS))
            .build();

    /**
     * Map where keys = {DyeColor} and value = {Shamiyana Block Supplier}
     */
    public static final Map<DyeColor, Supplier<Block>> SHAMIYANA_WALLS = new ImmutableMap.Builder<DyeColor, Supplier<Block>>()
            .put(DyeColor.BLACK, RegistryObject.create(new ResourceLocation(MODID, "black_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .put(DyeColor.BLUE, RegistryObject.create(new ResourceLocation(MODID, "blue_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .put(DyeColor.BROWN, RegistryObject.create(new ResourceLocation(MODID, "brown_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .put(DyeColor.CYAN, RegistryObject.create(new ResourceLocation(MODID, "cyan_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .put(DyeColor.GRAY, RegistryObject.create(new ResourceLocation(MODID, "gray_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .put(DyeColor.GREEN, RegistryObject.create(new ResourceLocation(MODID, "green_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .put(DyeColor.LIGHT_BLUE, RegistryObject.create(new ResourceLocation(MODID, "light_blue_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .put(DyeColor.LIGHT_GRAY, RegistryObject.create(new ResourceLocation(MODID, "light_gray_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .put(DyeColor.LIME, RegistryObject.create(new ResourceLocation(MODID, "lime_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .put(DyeColor.MAGENTA, RegistryObject.create(new ResourceLocation(MODID, "magenta_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .put(DyeColor.ORANGE, RegistryObject.create(new ResourceLocation(MODID, "orange_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .put(DyeColor.PINK, RegistryObject.create(new ResourceLocation(MODID, "pink_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .put(DyeColor.PURPLE, RegistryObject.create(new ResourceLocation(MODID, "purple_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .put(DyeColor.RED, RegistryObject.create(new ResourceLocation(MODID, "red_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .put(DyeColor.WHITE, RegistryObject.create(new ResourceLocation(MODID, "white_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .put(DyeColor.YELLOW, RegistryObject.create(new ResourceLocation(MODID, "yellow_shamiyana_wall"), ForgeRegistries.BLOCKS))
            .build();

    /**
     * Map where keys = {DyeColor} and value = {Shamiyana Structure Processor}
     */
    public static final Map<DyeColor, ShamiyanaStructureProcessor> SHAMIYANA_PROCESSORS = new ImmutableMap.Builder<DyeColor, ShamiyanaStructureProcessor>()
            .put(DyeColor.BLACK, new ShamiyanaStructureProcessor(DyeColor.BLACK))
            .put(DyeColor.BLUE, new ShamiyanaStructureProcessor(DyeColor.BLUE))
            .put(DyeColor.BROWN, new ShamiyanaStructureProcessor(DyeColor.BROWN))
            .put(DyeColor.CYAN, new ShamiyanaStructureProcessor(DyeColor.CYAN))
            .put(DyeColor.GRAY, new ShamiyanaStructureProcessor(DyeColor.GRAY))
            .put(DyeColor.GREEN, new ShamiyanaStructureProcessor(DyeColor.GREEN))
            .put(DyeColor.LIGHT_BLUE, new ShamiyanaStructureProcessor(DyeColor.LIGHT_BLUE))
            .put(DyeColor.LIGHT_GRAY, new ShamiyanaStructureProcessor(DyeColor.LIGHT_GRAY))
            .put(DyeColor.LIME, new ShamiyanaStructureProcessor(DyeColor.LIME))
            .put(DyeColor.MAGENTA, new ShamiyanaStructureProcessor(DyeColor.MAGENTA))
            .put(DyeColor.ORANGE, new ShamiyanaStructureProcessor(DyeColor.ORANGE))
            .put(DyeColor.PINK, new ShamiyanaStructureProcessor(DyeColor.PINK))
            .put(DyeColor.PURPLE, new ShamiyanaStructureProcessor(DyeColor.PURPLE))
            .put(DyeColor.RED, new ShamiyanaStructureProcessor(DyeColor.RED))
            .put(DyeColor.WHITE, new ShamiyanaStructureProcessor(DyeColor.WHITE))
            .put(DyeColor.YELLOW, new ShamiyanaStructureProcessor(DyeColor.YELLOW))
            .build();

    // instance fields that rely on registries being resolved before they can be initialized
    private final RuleTest barrierTest;
    private final RuleTest tentBlockTest;
    private final RuleProcessor removeBarrierProcessor;
    private final RuleProcessor removeTentBlockProcessor;
    private final RuleProcessor frameBlockProcessor;
    private final RuleProcessor insideTentProcessor;

    /**
     * Map where keys = {TentSize,TentType} and value = {Relative block positions}
     */
    private final Map<TentSize, Map<TentType, Set<BlockPos>>> templatePositions = new EnumMap<>(TentSize.class);

    public TentPlacer() {
        TagKey<Block> tentWallTag = BlockTags.create(new ResourceLocation(MODID, "tent/tent_wall"));

        // initialize rule tests
        barrierTest = new BlockMatchTest(Blocks.BARRIER);
        tentBlockTest = new TagMatchTest(tentWallTag);
        // create processor to replace barriers with air
        removeBarrierProcessor = new RuleProcessor(
                ImmutableList.of(new ProcessorRule(barrierTest, AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()))
        );
        // create processor to replace tent blocks with air
        removeTentBlockProcessor = new RuleProcessor(
                ImmutableList.of(new ProcessorRule(tentBlockTest, AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()))
        );

        // create rule entry builder for "tent block to frame" processor
        ImmutableList.Builder<ProcessorRule> frameBlocksBuilder = new ImmutableList.Builder<>();
        // iterate over registered "block to frame" values and add each one
        for (Map.Entry<ResourceLocation, RegistryObject<Block>> entry : BLOCK_TO_FRAME.entrySet()) {
            Block tentBlock = ForgeRegistries.BLOCKS.getValue(entry.getKey());
            if (tentBlock != null) {
                frameBlocksBuilder.add(new ProcessorRule(new BlockMatchTest(tentBlock), AlwaysTrueTest.INSTANCE, entry.getValue().get().defaultBlockState()));
            }
        }
        // create processor to replace tent blocks with correct frame
        frameBlockProcessor = new RuleProcessor(frameBlocksBuilder.build());

        // create processor to set "inside" properties for tent blocks
        insideTentProcessor = new RuleProcessor(
                new ImmutableList.Builder<ProcessorRule>()
                        .add(new ProcessorRule(new BlockMatchTest(NTRegistry.YURT_WALL.get()), AlwaysTrueTest.INSTANCE,
                                NTRegistry.YURT_WALL.get().defaultBlockState().setValue(YurtWallBlock.OUTSIDE, false)))
                        .add(new ProcessorRule(new BlockMatchTest(NTRegistry.YURT_ROOF.get()), AlwaysTrueTest.INSTANCE,
                                NTRegistry.YURT_ROOF.get().defaultBlockState().setValue(YurtRoofBlock.OUTSIDE, false)))
                        .add(new ProcessorRule(new BlockMatchTest(NTRegistry.INDLU_WALL.get()), AlwaysTrueTest.INSTANCE,
                                NTRegistry.INDLU_WALL.get().defaultBlockState().setValue(IndluWallBlock.OUTSIDE, false)))
                        .build());
    }

    /**
     * @return the TentPlacer instance
     */
    public static TentPlacer getInstance() {
        if (null == instance) {
            instance = new TentPlacer();
        }
        return instance;
    }

    /**
     * @param original the tent size
     * @return the tent size when built in the overworld
     */
    public static TentSize getOverworldSize(final TentSize original) {
        if (NomadicTents.CONFIG.USE_ACTUAL_SIZE.get()) {
            return original;
        }
        // determine the size of tent to place frames
        if (original == TentSize.TINY || original == TentSize.SMALL || original == TentSize.MEDIUM) {
            return TentSize.TINY;
        } else {
            return TentSize.SMALL;
        }
    }

    /**
     * @param level     the world
     * @param door      the door position
     * @param type      the tent type
     * @param size      the tent size
     * @param direction the facing direction of the tent
     * @return true if the tent frame can be placed at this position
     */
    public boolean canPlaceTentFrame(final Level level, final BlockPos door, final TentType type, final TentSize size, final Direction direction) {
        // determine the size of tent to place frames
        TentSize useSize = getOverworldSize(size);
        // determine template to use
        StructureTemplate template = getTemplate(level, type, useSize);
        if (null == template) {
            return false;
        }
        // determine location
        Rotation rotation = toRotation(direction);
        BlockPos origin = door.offset(BlockPos.ZERO.offset(0, 0, -template.getSize().getZ() / 2).rotate(rotation));
        Set<BlockPos> tentBlocks = getTentBlockPositions(level, door, type, useSize);
        // check each block to make sure it is replaceable
        BlockPos checkPos;
        BlockState checkState;
        for (BlockPos pos : tentBlocks) {
            checkPos = origin.offset(pos.rotate(rotation));
            checkState = level.getBlockState(checkPos);
            if (!checkState.canBeReplaced() && !checkState.is(NTRegistry.DOOR_FRAME.get())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Places a new tent or upgrades an existing one, then updates the tent door
     *
     * @param level       the world
     * @param door        the door position
     * @param tent        the tent information
     * @param sourceLevel the player previous world
     * @param sourceVec   the player previous location
     * @param sourceRot   the player previous rotation
     * @return true if the tent was placed or upgraded successfully
     */
    public boolean placeOrUpgradeTent(final Level level, final BlockPos door, final Tent tent,
                                      final ServerLevel sourceLevel, final Vec3 sourceVec, final float sourceRot) {
        // whether a structure was already built here (for upgrading and door-updating purposes)
        final boolean tentExists = level.getBlockState(door).getBlock() instanceof TentDoorBlock;
        // the old data stored by the tent door if it exists, or the current data if no door exists
        Tent prevTent = tent;
        if (tentExists) {
            BlockEntity blockEntity = level.getBlockEntity(door);
            if (blockEntity instanceof TentDoorBlockEntity) {
                TentDoorBlockEntity tentDoor = (TentDoorBlockEntity) blockEntity;
                // set up tile entity fields
                prevTent = tentDoor.getTent();
            }
        }
        // whether the tent needs to be replaced (size or color is changed)
        final boolean rebuildTent = prevTent.getSize() != tent.getSize() || prevTent.getColor() != tent.getColor();
        // whether the platform needs to be replaced (size or layers is changed)
        final boolean rebuildPlatform = prevTent.getSize() != tent.getSize() || prevTent.getLayers() != tent.getLayers();
        // remove tent in preparation for new tent
        if (rebuildTent) {
            removeTent(level, door, prevTent.getType(), prevTent.getSize(), TENT_DIRECTION);
        }
        // place new tent
        if (!tentExists || rebuildTent) {
            placeTent(level, door, tent.getType(), tent.getSize(), TENT_DIRECTION, tent.getColor());
        }
        // place platform
        if (!tentExists) {
            placePlatform(level, door, tent.getType(), tent.getSize(), tent.getLayers(), true);
        }
        // rebuild platform
        if (tentExists && rebuildPlatform) {
            upgradePlatform(level, door, tent.getType(), prevTent.getSize(), tent.getSize(), prevTent.getLayers(), tent.getLayers());
        }
        // place decorations
        if ((!tentExists && NomadicTents.CONFIG.TENT_DECOR_BUILD.get())
                || (rebuildTent && NomadicTents.CONFIG.TENT_DECOR_UPGRADE.get())) {
            placeTentDecor(level, door, tent.getType(), tent.getSize(), TENT_DIRECTION);
        }
        // update door
        BlockEntity blockEntity = level.getBlockEntity(door);
        if (blockEntity instanceof TentDoorBlockEntity) {
            TentDoorBlockEntity tentDoor = (TentDoorBlockEntity) blockEntity;
            // set up tile entity fields
            tentDoor.setSpawnpoint(sourceLevel, sourceVec);
            tentDoor.setSpawnRot(sourceRot);
            tentDoor.setTent(tent);
        }
        return true;
    }

    /**
     * Places a tent frame in the world and updates block entity information in the door
     *
     * @param level     the world
     * @param door      the door position
     * @param tent      the tent information
     * @param direction the tent direction
     * @param owner     the tent owner, if any
     * @return true if the tent was placed successfully
     */
    public boolean placeTentFrameWithDoor(final Level level, final BlockPos door, final Tent tent, final Direction direction, @Nullable final Player owner) {
        boolean success = placeTentFrame(level, door, tent.getType(), tent.getSize(), direction);
        // update door
        if (success) {
            BlockEntity blockEntity = level.getBlockEntity(door);
            if (blockEntity instanceof TentDoorBlockEntity) {
                TentDoorBlockEntity tentDoor = (TentDoorBlockEntity) blockEntity;
                // set up tile entity fields
                tentDoor.setTent(tent);
                tentDoor.setDirection(direction);
                if (owner != null) {
                    tentDoor.setOwner(owner.getUUID());
                }
            }
        }
        return success;
    }

    /**
     * Places a tent structure in the world, assumed to be a tent dimension
     *
     * @param level     the world
     * @param door      the door position
     * @param type      the tent type
     * @param size      the tent size
     * @param direction the facing direction of the tent
     * @param color     the color of the tent, if any
     * @return true if the tent was placed successfully
     */
    public boolean placeTent(final Level level, final BlockPos door, final TentType type, final TentSize size,
                             final Direction direction, @Nullable DyeColor color) {
        // ensure server side
        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevelAccessor serverLevel = (ServerLevel) level;
        // determine template to use
        StructureTemplate template = getTemplate(level, type, size);
        if (null == template) {
            return false;
        }
        // determine door block to use
        BlockState doorState = getDoor(type, size, direction);
        if (null == doorState) {
            return false;
        }

        // set up template placement settings
        Rotation rotation = toRotation(direction);
        BlockPos origin = door.offset(BlockPos.ZERO.offset(0, 0, -template.getSize().getZ() / 2).rotate(rotation));
        RandomSource rand = RandomSource.create(door.hashCode() + level.dimension().location().hashCode());
        BoundingBox mbb = BoundingBox.fromCorners(origin.subtract(template.getSize()), origin.offset(template.getSize()));
        StructurePlaceSettings placement = new StructurePlaceSettings()
                .setRotation(rotation).setRandom(rand).setBoundingBox(mbb)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR)
                .addProcessor(TepeeStructureProcessor.TEPEE_PROCESSOR)
                .addProcessor(insideTentProcessor);
        if (color != null) {
            placement.addProcessor(SHAMIYANA_PROCESSORS.get(color));
        }
        // place the template
        if (!template.placeInWorld(serverLevel, origin, origin, placement, rand, Block.UPDATE_ALL)) {
            return false;
        }
        // place door blocks
        level.setBlock(door, doorState, Block.UPDATE_ALL);
        return true;
    }

    /**
     * Places decoration for a tent structure in the world
     *
     * @param level     the world
     * @param door      the door position
     * @param type      the tent type
     * @param size      the tent size
     * @param direction the facing direction of the tent
     * @return true if the tent decor was placed successfully
     */
    public boolean placeTentDecor(final Level level, final BlockPos door, final TentType type, final TentSize size,
                                  final Direction direction) {
        // ensure server side
        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevelAccessor serverLevel = (ServerLevel) level;
        // determine template to use
        StructureTemplate template = getDecorTemplate(level, type, size);
        if (null == template) {
            return false;
        }

        // set up template placement settings
        Rotation rotation = toRotation(direction);
        BlockPos origin = door.offset(BlockPos.ZERO.offset(0, 0, -template.getSize().getZ() / 2).rotate(rotation));
        RandomSource rand = RandomSource.create(door.hashCode());
        BoundingBox mbb = BoundingBox.fromCorners(origin.subtract(template.getSize()), origin.offset(template.getSize()));
        StructurePlaceSettings placement = new StructurePlaceSettings()
                .setRotation(rotation).setRandom(rand).setBoundingBox(mbb)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR)
                .addProcessor(removeBarrierProcessor)
                .addProcessor(LocStructureProcessor.REPLACE_AIR);
        // place the template
        return template.placeInWorld(serverLevel, origin, origin, placement, rand, Block.UPDATE_ALL);
    }

    /**
     * Places a tent structure in the world with frame blocks only
     *
     * @param level     the world
     * @param door      the door position
     * @param type      the tent type
     * @param size      the tent size
     * @param direction the facing direction of the tent
     * @return true if the tent frame was placed successfully
     */
    public boolean placeTentFrame(final Level level, final BlockPos door, final TentType type, final TentSize size, final Direction direction) {
        // ensure server side
        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevelAccessor serverLevel = (ServerLevel) level;
        TentSize useSize = getOverworldSize(size);
        // determine template to use
        StructureTemplate template = getTemplate(level, type, useSize);
        if (null == template) {
            return false;
        }
        // determine door block to use
        BlockState doorState = getDoor(type, size, direction);
        if (null == doorState) {
            return false;
        }

        // set up template placement settings
        Rotation rotation = toRotation(direction);
        BlockPos origin = door.offset(BlockPos.ZERO.offset(0, 0, -template.getSize().getZ() / 2).rotate(rotation));
        RandomSource rand = RandomSource.create(door.hashCode());
        BoundingBox mbb = BoundingBox.fromCorners(origin.subtract(template.getSize()), origin.offset(template.getSize()));
        StructurePlaceSettings placement = new StructurePlaceSettings()
                .setRotation(rotation).setRandom(rand).setBoundingBox(mbb)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR)
                .addProcessor(new BlockIgnoreProcessor(ImmutableList.of(Blocks.BARRIER)))
                .addProcessor(frameBlockProcessor);
        // place the template
        if (!template.placeInWorld(serverLevel, origin, origin, placement, rand, Block.UPDATE_ALL)) {
            return false;
        }

        // place doors
        level.setBlock(door, doorState, Block.UPDATE_ALL);

        return true;
    }

    /**
     * Iterates over the expected blocks for the given tent and checks if each one is complete
     *
     * @param level     the world
     * @param door      the door position
     * @param type      the tent type
     * @param size      the tent size
     * @param direction the tent direction
     * @return true if the tent is complete
     */
    public boolean isTent(final Level level, final BlockPos door, final TentType type, final TentSize size, final Direction direction) {
        // ensure server side
        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return false;
        }
        // determine template to use
        StructureTemplate template = getTemplate(level, type, size);
        if (null == template) {
            return false;
        }
        // determine positions to check
        Rotation rotation = toRotation(direction);
        BlockPos origin = door.offset(BlockPos.ZERO.offset(0, 0, -template.getSize().getZ() / 2).rotate(rotation));
        Set<BlockPos> tentBlocks = getTentBlockPositions(level, door, type, size);
        // load tent block tag
        TagKey<Block> tentWallTag = BlockTags.create(new ResourceLocation(MODID, "tent/tent_wall"));
        // check each block to make sure it is in tent_wall tag (or is tent door)
        BlockPos checkPos;
        BlockState checkState;
        for (BlockPos pos : tentBlocks) {
            checkPos = origin.offset(pos.rotate(rotation));
            checkState = level.getBlockState(checkPos);
            if (!checkState.is(tentWallTag) && !(checkState.getBlock() instanceof TentDoorBlock)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Replaces tent blocks with air
     *
     * @param level     the world
     * @param door      the door position
     * @param type      the tent type
     * @param size      the tent size
     * @param direction the tent facing direction
     * @return true if the tent was successfully removed
     */
    public boolean removeTent(final Level level, final BlockPos door, final TentType type, final TentSize size, final Direction direction) {
        // ensure server side
        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevelAccessor serverLevel = (ServerLevel) level;
        // determine template to use
        StructureTemplate template = getTemplate(level, type, size);
        if (null == template) {
            return false;
        }

        Rotation rotation = toRotation(direction);
        BlockPos origin = door.offset(BlockPos.ZERO.offset(0, 0, -template.getSize().getZ() / 2).rotate(rotation));
        RandomSource rand = RandomSource.create(door.hashCode());

        BoundingBox mbb = BoundingBox.fromCorners(origin.subtract(template.getSize()), origin.offset(template.getSize()));
        StructurePlaceSettings placement = new StructurePlaceSettings()
                .setRotation(rotation).setRandom(rand).setBoundingBox(mbb)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR)
                .addProcessor(removeBarrierProcessor)
                .addProcessor(removeTentBlockProcessor);
        return template.placeInWorld(serverLevel, origin, origin, placement, rand, Block.UPDATE_ALL);
    }

    /**
     * Places dirt and rigid dirt in a platform underneath a tent. The tent must exist in the world.
     * At least one layer of regular dirt will be placed, with additional layers for each upgrade.
     *
     * @param level  the world
     * @param door   the door position
     * @param type   the tent type
     * @param size   the tent size
     * @param layers the number of layer upgrades
     * @param fill   true to fill harvestable space with dirt
     * @return true if the platform was created successfully
     */
    public boolean placePlatform(final Level level, final BlockPos door, final TentType type, final TentSize size, final int layers, boolean fill) {
        // ensure server side
        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return false;
        }
        // determine template to use
        StructureTemplate template = getTemplate(level, type, size);
        if (null == template) {
            return false;
        }

        BlockPos origin = door.offset(BlockPos.ZERO.offset(0, -1, -template.getSize().getZ() / 2));
        int width = template.getSize().getX();
        BlockState rigidDirt = NTRegistry.RIGID_DIRT.get().defaultBlockState();
        BlockState dirt = NomadicTents.CONFIG.getFloorBlock().defaultBlockState();

        // place dirt in a square at this location
        BlockPos p;
        boolean rigid;
        BlockState state;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                // determine block location
                p = origin.offset(x, 0, z);
                // determine which block state to place
                rigid = level.getBlockState(p.above()).getPistonPushReaction() == PushReaction.BLOCK;
                state = rigid ? rigidDirt : dirt;
                // place in a column at this location
                if (rigid || fill) {
                    for (int y = 0, l = layers + 1; y < l; y++) {
                        level.setBlock(p.below(y), state, Block.UPDATE_ALL);
                    }
                }
                level.setBlock(p.below(layers + 1), rigidDirt, Block.UPDATE_ALL);
            }
        }
        return true;
    }

    /**
     * Places dirt and rigid dirt in a platform underneath a newly upgraded tent. The tent must exist in the world.
     * Use this method if the platform was already built but needs to be changed.
     *
     * @param level     the world
     * @param door      the door position
     * @param type      the tent type
     * @param sizeOld   the previous tent size
     * @param sizeNew   the new tent size
     * @param layersOld the previous number of layer upgrades
     * @param layersNew the new number of layer upgrades
     * @return if the platform was upgraded successfully
     */
    public boolean upgradePlatform(final Level level, final BlockPos door, final TentType type,
                                   final TentSize sizeOld, final TentSize sizeNew,
                                   final int layersOld, final int layersNew) {
        // ensure server side
        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return false;
        }
        // determine template to use
        StructureTemplate templateOld = getTemplate(level, type, sizeOld);
        StructureTemplate templateNew = getTemplate(level, type, sizeNew);
        if (null == templateOld || null == templateNew) {
            return false;
        }

        int widthOld = templateOld.getSize().getX();
        int widthNew = templateNew.getSize().getX();
        // the difference in width for each side (half the total difference)
        int dwidth = (widthNew - widthOld) / 2;

        boolean upgradeSize = sizeOld != sizeNew;
        boolean upgradeLayers = layersOld != layersNew;

        BlockState rigidDirt = NTRegistry.RIGID_DIRT.get().defaultBlockState();
        BlockState dirt = NomadicTents.CONFIG.getFloorBlock().defaultBlockState();

        // place new
        if (upgradeSize) {
            BlockPos origin = door.offset(BlockPos.ZERO.offset(0, -1, -templateNew.getSize().getZ() / 2));

            // place dirt in a square at this location
            boolean rigid;
            BlockPos p;
            BlockState state;
            for (int x = 0; x < widthNew; x++) {
                for (int z = 0; z < widthNew; z++) {
                    // determine block location
                    p = origin.offset(x, 0, z);
                    // skip locations that existed in old platform that are not rigid dirt
                    if ((z > dwidth && z < widthNew - dwidth) && (x < widthOld)
                            && !level.getBlockState(p).is(rigidDirt.getBlock())) {
                        continue;
                    }
                    // determine which block state to place
                    rigid = level.getBlockState(p.above()).getPistonPushReaction() == PushReaction.BLOCK;
                    state = rigid ? rigidDirt : dirt;
                    // place in a column at this location
                    for (int y = 0, l = layersOld + 1; y < l; y++) {
                        level.setBlock(p.below(y), state, Block.UPDATE_ALL);
                    }
                    level.setBlock(p.below(layersOld + 1), rigidDirt, Block.UPDATE_ALL);
                }
            }
        }

        // replace rigid dirt with dirt for each new layer
        if (upgradeLayers) {
            BlockPos origin = door.offset(BlockPos.ZERO.offset(0, -1, -templateNew.getSize().getZ() / 2));

            // place dirt in a square at this location
            BlockPos p;
            boolean rigid;
            BlockState state;
            for (int x = 0; x < widthNew; x++) {
                for (int z = 0; z < widthNew; z++) {
                    // determine block location
                    p = origin.offset(x, 0, z);
                    // determine which block state to place
                    rigid = level.getBlockState(p.above()).getPistonPushReaction() == PushReaction.BLOCK;
                    if (rigid) {
                        state = rigidDirt;
                    } else {
                        state = dirt;
                    }
                    // place in a column at this location
                    for (int y = layersOld + 1, l = layersNew + 1; y < l; y++) {
                        level.setBlock(p.below(y), state, Block.UPDATE_ALL);
                    }
                    level.setBlock(p.below(layersNew + 1), rigidDirt, Block.UPDATE_ALL);
                }
            }
        }

        return upgradeSize || upgradeLayers;
    }

    /**
     * @param level the World
     * @param type  the tent type
     * @param size  the tent size
     * @return the tent structure template for the given type and size
     */
    @Nullable
    public static StructureTemplate getTemplate(final Level level, final TentType type, final TentSize size) {
        // determine structure to use
        String templateName = "tent/" + size.getSerializedName() + "_" + type.getSerializedName();
        ResourceLocation templateId = new ResourceLocation(NomadicTents.MODID, templateName);
        return getTemplate(level, templateId);
    }

    /**
     * @param level the World
     * @param type  the tent type
     * @param size  the tent size
     * @return the tent decor template for the given type and size
     */
    @Nullable
    public static StructureTemplate getDecorTemplate(final Level level, final TentType type, final TentSize size) {
        // determine structure to use
        String templateName = "tent/decor/" + size.getSerializedName() + "_" + type.getSerializedName();
        ResourceLocation templateId = new ResourceLocation(NomadicTents.MODID, templateName);
        return getTemplate(level, templateId);
    }

    /**
     * @param level      the World
     * @param templateId the template resource location
     * @return the tent structure template with the given ID, or null if none is found
     */
    @Nullable
    public static StructureTemplate getTemplate(final Level level, final ResourceLocation templateId) {
        // ensure server side
        if (null == level.getServer()) {
            return null;
        }
        StructureTemplateManager templateManager = level.getServer().getStructureManager();
        Optional<StructureTemplate> template = templateManager.get(templateId);
        if (template.isEmpty()) {
            NomadicTents.LOGGER.warn("Failed to load tent template for " + templateId);
        }
        return template.orElse(null);
    }

    public Set<BlockPos> getTentBlockPositions(final Level level, final BlockPos door, final TentType type, final TentSize size) {
        // ensure server side
        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return ImmutableSet.of();
        }
        // check if positions are added to map
        if (templatePositions.containsKey(size) && templatePositions.get(size).containsKey(type)) {
            return templatePositions.get(size).get(type);
        }
        // positions need to be calculated for the first time
        // determine template to use
        StructureTemplate template = getTemplate(level, type, size);
        if (null == template) {
            return ImmutableSet.of();
        }
        Rotation rotation = Rotation.NONE;
        BlockPos origin = door.offset(BlockPos.ZERO.offset(0, 0, -template.getSize().getZ() / 2).rotate(rotation));
        StructurePlaceSettings placement = new StructurePlaceSettings()
                .setRotation(rotation);

        Set<BlockPos> tentBlocks = new HashSet<>();
        // load tent block tag
        TagKey<Block> tentWallTag = BlockTags.create(new ResourceLocation(MODID, "tent/tent_wall"));
        // filter the template for each block and add to a set
        for (Block b : ForgeRegistries.BLOCKS.tags().getTag(tentWallTag)) {
            List<StructureTemplate.StructureBlockInfo> filtered = template.filterBlocks(origin, placement, b, false);
            for (StructureTemplate.StructureBlockInfo blockInfo : filtered) {
                tentBlocks.add(blockInfo.pos());
            }
        }
        // add positions to the map
        templatePositions.computeIfAbsent(size, s -> new EnumMap<>(TentType.class)).put(type, ImmutableSet.copyOf(tentBlocks));
        return tentBlocks;
    }

    /**
     * @param type      the tent type
     * @param size      the tent size
     * @param direction the facing direction of the door
     * @return the door blockstate for the given type and size
     */
    @Nullable
    public static BlockState getDoor(final TentType type, final TentSize size, Direction direction) {
        // ensure valid tent type
        if (!DOORS.containsKey(size)) {
            NomadicTents.LOGGER.warn("No tent door is registered for tent size " + size.getSerializedName());
            return null;
        }
        // ensure valid tent size
        Map<TentType, Supplier<Block>> doors = DOORS.get(size);
        if (!doors.containsKey(type)) {
            NomadicTents.LOGGER.warn("No tent door is registered for tent type " + size.getSerializedName()
                    + " " + type.getSerializedName());
            return null;
        }
        BlockState doorState = doors.get(type).get().defaultBlockState();
        doorState = doorState.setValue(TentDoorBlock.AXIS, direction.getAxis());
        return doorState;
    }

    /**
     * @param frame the frame block
     * @param level the world
     * @param pos   the block position
     * @return the block to replace the given frame block
     */
    public static BlockState getFrameTarget(final BlockState frame, final Level level, final BlockPos pos) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(frame.getBlock());
        boolean outside = !DynamicDimensionHelper.isInsideTent(level);
        if (FRAME_TO_BLOCK.containsKey(id)) {
            return FRAME_TO_BLOCK.get(id).apply(outside);
        }
        NomadicTents.LOGGER.warn("Failed to locate frame block target for " + id);
        return frame;
    }

    public static Rotation toRotation(Direction dir) {
        switch (dir) {
            case DOWN:
            case UP:
            case EAST:
            default:
                return Rotation.NONE;
            case WEST:
                return Rotation.CLOCKWISE_180;
            case NORTH:
                return Rotation.COUNTERCLOCKWISE_90;
            case SOUTH:
                return Rotation.CLOCKWISE_90;
        }
    }
}
