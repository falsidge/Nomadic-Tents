package nomadictents.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.material;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import nomadictents.NTRegistry;
import nomadictents.NTSavedData;
import nomadictents.NomadicTents;
import nomadictents.block.FrameBlock;
import nomadictents.dimension.DynamicDimensionHelper;
import nomadictents.structure.TentPlacer;
import nomadictents.util.Tent;
import nomadictents.util.TentLayers;
import nomadictents.util.TentSize;
import nomadictents.util.TentType;

import javax.annotation.Nullable;
import java.util.List;

public class TentItem extends Item {

    private static final String DOOR = "door";
    private static final String DIRECTION = "direction";

    private final TentType type;
    private final TentSize size;

    private static final CauldronInteraction WASH_TENT = (state, level, pos, player, hand, itemStack) -> {
        // only interact when item stack has color other than white
        if (!itemStack.hasTag() || !itemStack.getOrCreateTag().contains(Tent.COLOR)
                || DyeColor.byName(itemStack.getOrCreateTag().getString(Tent.COLOR), DyeColor.WHITE) == DyeColor.WHITE) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            // replace item with white color information
            ItemStack replace = itemStack.copy();
            replace.getOrCreateTag().putString(Tent.COLOR, DyeColor.WHITE.getSerializedName());
            player.setItemInHand(hand, replace);
            // reduce cauldron fill level
            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    };

    public TentItem(TentType type, TentSize width, Properties properties) {
        super(properties);
        this.type = type;
        this.size = width;

        CauldronInteraction.WATER.put(this, WASH_TENT);
    }

    @Override
    public boolean isFireResistant() {
        return super.isFireResistant() || NomadicTents.CONFIG.TENT_FIREPROOF.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        list.add(Component.translatable("item.nomadictents.tent.tooltip").withStyle(this.size.getColor()));
        if (this.type == TentType.SHAMIYANA || (stack.hasTag() && stack.getOrCreateTag().contains(Tent.COLOR))) {
            DyeColor color = DyeColor.byName(stack.getOrCreateTag().getString(Tent.COLOR), DyeColor.WHITE);
            String translationKey = "item.minecraft.firework_star." + color.getSerializedName();
            list.add(Component.translatable(translationKey));
        }
        if (flag.isAdvanced() || net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            // layer tooltip
            byte layers = stack.getOrCreateTag().getByte(Tent.LAYERS);
            byte maxLayers = TentLayers.getMaxLayers(this.size);
            list.add(Component.translatable("item.nomadictents.tent.tooltip.layer", layers, maxLayers).withStyle(ChatFormatting.GRAY));
            // ID tooltip
            int id = stack.getOrCreateTag().getInt(Tent.ID);
            list.add(Component.translatable("item.nomadictents.tent.tooltip.id", id).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 7200;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // determine block and item
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        ItemStack itemStack = context.getItemInHand();
        // begin using the item
        if (context.getPlayer() != null) {
            context.getPlayer().startUsingItem(context.getHand());
        }
        // client should not run anything else
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        // cannot place tent inside tent
        if (DynamicDimensionHelper.isInsideTent(context.getLevel())) {
            // send message
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(Component.translatable("tent.build.deny.inside_tent"), true);
            }
            return InteractionResult.PASS;
        }
        // cannot place tent inside blacklisted dimension
        if (NomadicTents.CONFIG.isDimensionBlacklist(context.getLevel())) {
            // send message
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(Component.translatable("tent.build.deny.dimension"), true);
            }
            return InteractionResult.PASS;
        }
        // add door frame
        if (NTRegistry.DOOR_FRAME.get() != state.getBlock()) {
            // determine placement position
            BlockPos placePos = context.getClickedPos();
            if (!context.getLevel().getBlockState(placePos).canBeReplaced(new BlockPlaceContext(context))) {
                placePos = placePos.relative(context.getClickedFace());
            }
            // determine if placement position is valid
            BlockState replace = context.getLevel().getBlockState(placePos);
            if (!replace.isAir() && replace.getFluidState() != Fluids.EMPTY.defaultFluidState()) {
                return InteractionResult.FAIL;
            }
            if (canPlaceTent(context.getLevel(), placePos, context.getHorizontalDirection())) {
                // place door frame
                context.getLevel().setBlock(placePos, NTRegistry.DOOR_FRAME.get().defaultBlockState(), Block.UPDATE_ALL);
                // remember the door position and player direction
                itemStack.getOrCreateTag().put(DOOR, NbtUtils.writeBlockPos(placePos));
                itemStack.getTag().putString(DIRECTION, context.getHorizontalDirection().getSerializedName());

                return InteractionResult.SUCCESS;
            } else {
                // send message
                if (context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(Component.translatable("tent.build.deny.space"), true);
                }
            }
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return super.onItemUseFirst(stack, context);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int duration) {
        if (level.isClientSide) {
            return;
        }
        // locate door frame
        if (stack.hasTag() && stack.getTag().contains(DOOR) && stack.getTag().contains(DIRECTION)) {
            BlockPos pos = NbtUtils.readBlockPos(stack.getTag().getCompound(DOOR));
            Direction direction = Direction.byName(stack.getTag().getString(DIRECTION));
            if (level.isLoaded(pos)) {
                // detect door frame
                BlockState state = level.getBlockState(pos);
                if (NTRegistry.DOOR_FRAME.get() == state.getBlock()) {
                    int progress = state.getValue(FrameBlock.PROGRESS);
                    if (entity instanceof Player && progress == FrameBlock.MAX_PROGRESS) {
                        // place tent
                        placeTent(stack, level, pos, direction, (Player) entity);
                    } else {
                        // cancel tent
                        cancelTent(stack, level, pos);
                    }
                }
            }
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int duration) {
        // delay between updates
        if (level.isClientSide || duration % 5 != 1) {
            return;
        }
        // locate selected block
        BlockHitResult result = clipFrom(entity, entity.getAttribute(ForgeMod.BLOCK_REACH.get()).getValue());
        if (result.getType() != HitResult.Type.BLOCK) {
            entity.releaseUsingItem();
            return;
        }
        // locate door frame
        BlockPos pos = result.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (NTRegistry.DOOR_FRAME.get() != state.getBlock()) {
            entity.releaseUsingItem();
            return;
        }
        // determine tent direction
        Direction direction = entity.getDirection();
        if (stack.getOrCreateTag().contains(DIRECTION)) {
            direction = Direction.byName(stack.getTag().getString(DIRECTION));
        }
        // update door frame progress stages
        int progress = state.getValue(FrameBlock.PROGRESS);
        if (progress == FrameBlock.MAX_PROGRESS) {
            // determine if position is valid
            if (entity instanceof Player && canPlaceTent(level, pos, direction)) {
                // place tent
                placeTent(stack, level, pos, direction, (Player) entity);
                entity.releaseUsingItem();
                return;
            } else {
                // remove door frame
                cancelTent(stack, level, pos);
                // send message
                if (entity instanceof Player) {
                    ((Player) entity).displayClientMessage(Component.translatable("tent.build.deny.space"), true);
                }
            }
        }
        // increment progress
        int next = progress + 2;
        level.setBlock(pos, state.setValue(FrameBlock.PROGRESS, Math.min(next, FrameBlock.MAX_PROGRESS)), Block.UPDATE_ALL);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        this.releaseUsing(stack, level, entity, 0);
        return stack;
    }

    /**
     * @param level     the world
     * @param startPos  the tent door position
     * @param direction the tent direction
     * @return true if the player can place a tent at the given location
     */
    private boolean canPlaceTent(Level level, BlockPos startPos, Direction direction) {
        TentPlacer tentPlacer = TentPlacer.getInstance();
        return tentPlacer.canPlaceTentFrame(level, startPos, this.type, this.size, direction);
    }

    /**
     * Places a tent at the given location. If the tent does not have an ID, registers a new ID.
     *
     * @param stack      the tent item stack
     * @param level      the world
     * @param clickedPos the door position
     * @param direction  the tent direction
     * @param owner      the player who placed the tent
     */
    private void placeTent(ItemStack stack, Level level, BlockPos clickedPos, Direction direction, @Nullable Player owner) {
        if (level.isClientSide() || null == level.getServer()) {
            return;
        }
        // ensure tent ID exists
        if (!stack.getOrCreateTag().contains(Tent.ID) || stack.getOrCreateTag().getInt(Tent.ID) == 0) {
            NTSavedData ntSavedData = NTSavedData.get(level.getServer());
            int tentId = ntSavedData.getNextTentId();
            stack.getOrCreateTag().putInt(Tent.ID, tentId);
        }
        // create tent wrapper
        Tent tent = Tent.from(stack, this.type, this.size);
        // place the tent
        level.destroyBlock(clickedPos, false);
        TentPlacer tentPlacer = TentPlacer.getInstance();
        if (tentPlacer.placeTentFrameWithDoor(level, clickedPos, tent, direction, owner)) {
            // remove tent from inventory
            stack.shrink(1);
        }
    }

    /**
     * Removes the door frame if one is currently in progress
     *
     * @param stack      the tent item stack
     * @param level      the world
     * @param clickedPos the position of the door frame, if any
     */
    private void cancelTent(ItemStack stack, Level level, BlockPos clickedPos) {
        // remove door frame
        BlockState state = level.getBlockState(clickedPos);
        if (state.is(NTRegistry.DOOR_FRAME.get())) {
            level.setBlock(clickedPos, state.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
        }
        // remove NBT data
        stack.getOrCreateTag().remove(DOOR);
        stack.getOrCreateTag().remove(DIRECTION);
    }

    public static BlockHitResult clipFrom(final LivingEntity player, final double range) {
        // raytrace to determine which block the player is looking at within the given range
        final Vec3 startVec = player.getEyePosition(1.0F);
        final float pitch = (float) Math.toRadians(-player.getXRot());
        final float yaw = (float) Math.toRadians(-player.getYRot());
        float cosYaw = Mth.cos(yaw - (float) Math.PI);
        float sinYaw = Mth.sin(yaw - (float) Math.PI);
        float cosPitch = -Mth.cos(pitch);
        float sinPitch = Mth.sin(pitch);
        final Vec3 endVec = startVec.add(sinYaw * cosPitch * range, sinPitch * range, cosYaw * cosPitch * range);
        return player.level().clip(new ClipContext(startVec, endVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }
}
