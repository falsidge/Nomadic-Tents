package nomadictents.structure;

import java.util.Random;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nomadictents.block.BlockTepeeWall;
import nomadictents.dimension.TentDimensionManager;
import nomadictents.init.Content;
import nomadictents.structure.util.Blueprint;
import nomadictents.structure.util.TentData;
import nomadictents.structure.util.TentType;
import nomadictents.structure.util.TentWidth;

public class StructureTepee extends StructureBase {
	
	public static final int LAYER_DEPTH = 2;

	@Override
	public TentType getTentType() {
		return TentType.TEPEE;
	}

	@Override
	public boolean generate(final World worldIn, final BlockPos doorBase, final TentData data, final Direction dirForward,
			final BlockState doorBlock, final BlockState wallBlock, final BlockState roofBlock) {
		boolean tentDim = TentDimensionManager.isTent(worldIn);
		Blueprint bp = getBlueprints(data);
		if(bp == null) {
			return false;
		}
		// build all relevant layers
		this.buildLayer(worldIn, doorBase, dirForward, wallBlock, bp.getWallCoords());
		// make door
		buildDoor(worldIn, doorBase, doorBlock, dirForward);
		// add dimension-only features
		if (tentDim && wallBlock.getMaterial() != Material.AIR) {
			if(getTentType().areFeaturesEnabled()) {
				// build a campfire in the center of the tent (use torch for smallest tent)
				final BlockPos center = getCenter(doorBase, data.getWidth(), dirForward);
				final BlockState fire = data.getWidth() == TentWidth.SMALL 
						? Blocks.TORCH.getDefaultState()
						: Blocks.CAMPFIRE.getDefaultState().with(CampfireBlock.LIT, true);
				if(worldIn.isAirBlock(center) && (worldIn.isAirBlock(center.down()) 
						|| Block.isDirt(worldIn.getBlockState(center.down()).getBlock()))) {
					worldIn.setBlockState(center.down(), Blocks.COBBLESTONE.getDefaultState(), 2);
					worldIn.setBlockState(center, fire, 2);
				}
			}
			super.buildLayer(worldIn, doorBase, dirForward, Content.TENT_BARRIER.getDefaultState(), bp.getBarrierCoords());
		}
		return !bp.isEmpty();
	}

	@Override
	public boolean isValidForFacing(final World worldIn, final TentData data, final BlockPos doorBase, final Direction facing) {
		final Blueprint bp = getBlueprints(data);
		final Predicate<BlockState> TENT_PRED = (BlockState b) 
				-> data.getTent().getInterface().isAssignableFrom(b.getBlock().getClass());
		// check wall arrays
		return validateArray(worldIn, doorBase, bp.getWallCoords(), facing, TENT_PRED);
	}

	@Override
	public void buildLayer(World worldIn, BlockPos doorPos, Direction dirForward, BlockState state,
			BlockPos[] coordinates) {
		// if it's a tepee block, calculate what kind of design it should have
		if(state.getBlock() instanceof BlockTepeeWall) {
			// custom block-placement math for each position
			for (BlockPos coord : coordinates) {
				BlockPos pos = getPosFromDoor(doorPos, coord, dirForward);
				BlockState tepeeState;
				if (pos.getY() % 2 == 0) {
					// psuedo-random seed ensures that all blocks that are same y-dis from door get
					// the same seed
					int randSeed = Math.abs(pos.getY() * 123 + doorPos.getX() + doorPos.getZ() * 321);
					tepeeState = BlockTepeeWall.getStateForRandomPattern(new Random(randSeed), true);
				} else {
					tepeeState = BlockTepeeWall.getStateForRandomDesignWithChance(worldIn.rand, true);
				}
				worldIn.setBlockState(pos, tepeeState, 3);
			}
		} else {
			// if it's not a tepee block, default to super method
			super.buildLayer(worldIn, doorPos, dirForward, state, coordinates);
		}	
	}
	
	public static Blueprint makeBlueprints(final TentWidth size) {
		final Blueprint bp = new Blueprint();
		switch (size) {
		case MEGA:
			bp.addWallCoords(new int[][] {
				// layer 1 and 2
				{ 0, 0, -2 }, { 0, 0, -1 }, { 0, 0, 0 }, { 0, 0, 1 }, { 0, 0, 2 }, { 1, 0, 3 }, { 1, 0, 4 }, { 2, 0, 5 }, { 3, 0, 6 }, { 4, 0, 6 },
				{ 5, 0, 7 }, { 6, 0, 7 }, { 7, 0, 7 }, { 8, 0, 7 }, { 9, 0, 7 }, { 10, 0, 6 }, { 11, 0, 6 }, { 12, 0, 5 }, { 13, 0, 4 }, { 13, 0, 3 },
				{ 14, 0, 2 }, { 14, 0, 1 }, { 14, 0, 0 }, { 14, 0, -1 }, { 14, 0, -2 }, { 13, 0, -3 }, { 13, 0, -4 }, { 12, 0, -5 }, { 11, 0, -6 }, { 10, 0, -6 },
				{ 9, 0, -7 }, { 8, 0, -7 }, { 7, 0, -7 }, { 6, 0, -7 }, { 5, 0, -7 }, { 4, 0, -6 }, { 3, 0, -6 }, { 2, 0, -5 }, { 1, 0, -4 }, { 1, 0, -3 },
				{ 0, 1, -2 }, { 0, 1, -1 }, { 0, 1, 0 }, { 0, 1, 1 }, { 0, 1, 2 }, { 1, 1, 3 }, { 1, 1, 4 }, { 2, 1, 5 }, { 3, 1, 6 }, { 4, 1, 6 },
				{ 5, 1, 7 }, { 6, 1, 7 }, { 7, 1, 7 }, { 8, 1, 7 }, { 9, 1, 7 }, { 10, 1, 6 }, { 11, 1, 6 }, { 12, 1, 5 }, { 13, 1, 4 }, { 13, 1, 3 },
				{ 14, 1, 2 }, { 14, 1, 1 }, { 14, 1, 0 }, { 14, 1, -1 }, { 14, 1, -2 }, { 13, 1, -3 }, { 13, 1, -4 }, { 12, 1, -5 }, { 11, 1, -6 }, { 10, 1, -6 },
				{ 9, 1, -7 }, { 8, 1, -7 }, { 7, 1, -7 }, { 6, 1, -7 }, { 5, 1, -7 }, { 4, 1, -6 }, { 3, 1, -6 }, { 2, 1, -5 }, { 1, 1, -4 }, { 1, 1, -3 },
				// layers 3 and 4
				{ 0, 2, -1 }, { 0, 2, 0 }, { 0, 2, 1 }, { 1, 2, 2 }, { 2, 2, 3 }, { 2, 2, 4 }, { 3, 2, 5 }, { 4, 2, 5 }, { 5, 2, 6 },
				{ 6, 2, 7 }, { 7, 2, 7 }, { 8, 2, 7 }, { 9, 2, 6 }, { 10, 2, 5 }, { 11, 2, 5 }, { 12, 2, 4 }, { 12, 2, 3 }, { 13, 2, 2 },
				{ 14, 2, 1 }, { 14, 2, 0 }, { 14, 2, -1 }, { 13, 2, -2 }, { 12, 2, -3 }, { 12, 2, -4 }, { 11, 2, -5 }, { 10, 2, -5 }, { 9, 2, -6 },
				{ 8, 2, -7 }, { 7, 2, -7 }, { 6, 2, -7 }, { 5, 2, -6 }, { 4, 2, -5 }, { 3, 2, -5 }, { 2, 2, -4 }, { 2, 2, -3 }, { 1, 2, -2 },
				{ 0, 3, -1 }, { 0, 3, 0 }, { 0, 3, 1 }, { 1, 3, 2 }, { 2, 3, 3 }, { 2, 3, 4 }, { 3, 3, 5 }, { 4, 3, 5 }, { 5, 3, 6 },
				{ 6, 3, 7 }, { 7, 3, 7 }, { 8, 3, 7 }, { 9, 3, 6 }, { 10, 3, 5 }, { 11, 3, 5 }, { 12, 3, 4 }, { 12, 3, 3 }, { 13, 3, 2 },
				{ 14, 3, 1 }, { 14, 3, 0 }, { 14, 3, -1 }, { 13, 3, -2 }, { 12, 3, -3 }, { 12, 3, -4 }, { 11, 3, -5 }, { 10, 3, -5 }, { 9, 3, -6 },
				{ 8, 3, -7 }, { 7, 3, -7 }, { 6, 3, -7 }, { 5, 3, -6 }, { 4, 3, -5 }, { 3, 3, -5 }, { 2, 3, -4 }, { 2, 3, -3 }, { 1, 3, -2 },
				// layers 5 to 16
				{ 1, 4, -1 }, { 1, 4, 0 }, { 1, 4, 1 }, { 2, 4, 2 }, { 3, 4, 3 }, { 3, 4, 4 }, { 4, 4, 4 },
				{ 5, 4, 5 }, { 6, 4, 6 }, { 7, 4, 6 }, { 8, 4, 6 }, { 9, 4, 5 }, { 10, 4, 4 }, { 11, 4, 4 },
				{ 11, 4, 3 }, { 12, 4, 2 }, { 13, 4, 1 }, { 13, 4, 0 }, { 13, 4, -1 }, { 12, 4, -2 }, { 11, 4, -3 },
				{ 11, 4, -4 }, { 10, 4, -4 }, { 9, 4, -5 }, { 8, 4, -6 }, { 7, 4, -6 }, { 6, 4, -6 }, { 5, 4, -5 },
				{ 4, 4, -4 }, { 3, 4, -4 }, { 3, 4, -3 }, { 2, 4, -2 }, { 1, 5, -1 }, { 1, 5, 0 }, { 1, 5, 1 },
				{ 2, 5, 2 }, { 3, 5, 3 }, { 3, 5, 4 }, { 4, 5, 4 }, { 5, 5, 5 }, { 6, 5, 6 }, { 7, 5, 6 },
				{ 8, 5, 6 }, { 9, 5, 5 }, { 10, 5, 4 }, { 11, 5, 4 }, { 11, 5, 3 }, { 12, 5, 2 }, { 13, 5, 1 },
				{ 13, 5, 0 }, { 13, 5, -1 }, { 12, 5, -2 }, { 11, 5, -3 }, { 11, 5, -4 }, { 10, 5, -4 },
				{ 9, 5, -5 }, { 8, 5, -6 }, { 7, 5, -6 }, { 6, 5, -6 }, { 5, 5, -5 }, { 4, 5, -4 }, { 3, 5, -4 },
				{ 3, 5, -3 }, { 2, 5, -2 }, { 2, 6, -1 }, { 2, 6, 0 }, { 2, 6, 1 }, { 3, 6, 2 }, { 4, 6, 3 },
				{ 5, 6, 4 }, { 6, 6, 5 }, { 7, 6, 5 }, { 8, 6, 5 }, { 9, 6, 4 }, { 10, 6, 3 }, { 11, 6, 2 },
				{ 12, 6, 1 }, { 12, 6, 0 }, { 12, 6, -1 }, { 11, 6, -2 }, { 10, 6, -3 }, { 9, 6, -4 }, { 8, 6, -5 },
				{ 7, 6, -5 }, { 6, 6, -5 }, { 5, 6, -4 }, { 4, 6, -3 }, { 3, 6, -2 }, { 2, 7, -1 }, { 2, 7, 0 },
				{ 2, 7, 1 }, { 3, 7, 2 }, { 4, 7, 3 }, { 5, 7, 4 }, { 6, 7, 5 }, { 7, 7, 5 }, { 8, 7, 5 },
				{ 9, 7, 4 }, { 10, 7, 3 }, { 11, 7, 2 }, { 12, 7, 1 }, { 12, 7, 0 }, { 12, 7, -1 }, { 11, 7, -2 },
				{ 10, 7, -3 }, { 9, 7, -4 }, { 8, 7, -5 }, { 7, 7, -5 }, { 6, 7, -5 }, { 5, 7, -4 }, { 4, 7, -3 },
				{ 3, 7, -2 }, { 3, 8, -1 }, { 3, 8, 0 }, { 3, 8, 1 }, { 4, 8, 2 }, { 5, 8, 3 }, { 6, 8, 4 },
				{ 7, 8, 4 }, { 8, 8, 4 }, { 9, 8, 3 }, { 10, 8, 2 }, { 11, 8, 1 }, { 11, 8, 0 }, { 11, 8, -1 },
				{ 10, 8, -2 }, { 9, 8, -3 }, { 8, 8, -4 }, { 7, 8, -4 }, { 6, 8, -4 }, { 5, 8, -3 }, { 4, 8, -2 },
				{ 3, 9, -1 }, { 3, 9, 0 }, { 3, 9, 1 }, { 4, 9, 2 }, { 5, 9, 3 }, { 6, 9, 4 }, { 7, 9, 4 },
				{ 8, 9, 4 }, { 9, 9, 3 }, { 10, 9, 2 }, { 11, 9, 1 }, { 11, 9, 0 }, { 11, 9, -1 }, { 10, 9, -2 },
				{ 9, 9, -3 }, { 8, 9, -4 }, { 7, 9, -4 }, { 6, 9, -4 }, { 5, 9, -3 }, { 4, 9, -2 }, { 4, 10, -1 },
				{ 4, 10, 0 }, { 4, 10, 1 }, { 5, 10, 2 }, { 6, 10, 3 }, { 7, 10, 3 }, { 8, 10, 3 }, { 9, 10, 2 },
				{ 10, 10, 1 }, { 10, 10, 0 }, { 10, 10, -1 }, { 9, 10, -2 }, { 8, 10, -3 }, { 7, 10, -3 },
				{ 6, 10, -3 }, { 5, 10, -2 }, { 4, 11, -1 }, { 4, 11, 0 }, { 4, 11, 1 }, { 5, 11, 2 }, { 6, 11, 3 },
				{ 7, 11, 3 }, { 8, 11, 3 }, { 9, 11, 2 }, { 10, 11, 1 }, { 10, 11, 0 }, { 10, 11, -1 },
				{ 9, 11, -2 }, { 8, 11, -3 }, { 7, 11, -3 }, { 6, 11, -3 }, { 5, 11, -2 }, 
				{ 5, 12, -1 }, { 5, 12, 0 }, { 5, 12, 1 }, { 6, 12, 2 }, { 7, 12, 2 }, { 8, 12, 2 }, { 9, 12, 1 }, 
				{ 9, 12, 0 }, { 9, 12, -1 }, { 8, 12, -2 }, { 7, 12, -2 }, { 6, 12, -2 }, 
				{ 5, 13, -1 }, { 5, 13, 0 }, { 5, 13, 1 }, { 6, 13, 2 }, { 7, 13, 2 }, { 8, 13, 2 }, { 9, 13, 1 }, 
				{ 9, 13, 0 }, { 9, 13, -1 }, { 8, 13, -2 }, { 7, 13, -2 }, { 6, 13, -2 }, 
				{ 6, 14, -1 }, { 6, 14, 0 }, { 6, 14, 1 },
				{ 7, 14, 1 }, { 8, 14, 1 }, { 8, 14, 0 }, { 8, 14, -1 }, { 7, 14, -1 }, 
				{ 6, 15, -1 }, { 6, 15, 0 }, { 6, 15, 1 }, { 7, 15, 1 }, { 8, 15, 1 }, { 8, 15, 0 }, { 8, 15, -1 }, { 7, 15, -1 } 
			});
			bp.addBarrierCoords(new int[][] { { 7, 16, 0 } });
			break;
		case GIANT:
			bp.addWallCoords(new int[][] {
				// layer 1 and 2
				{ 0, 0, -2 }, { 0, 0, -1 }, { 0, 0, 0 }, { 0, 0, 1 }, { 0, 0, 2 }, { 1, 0, 3 }, { 1, 0, 4 }, { 2, 0, 5 }, { 3, 0, 5 },
				{ 4, 0, 6 }, { 5, 0, 6 }, { 6, 0, 6 }, { 7, 0, 6 }, { 8, 0, 6 }, { 9, 0, 5 }, { 10, 0, 5 }, { 11, 0, 4 }, { 11, 0, 3 },
				{ 12, 0, 2 }, { 12, 0, 1 }, { 12, 0, 0 }, { 12, 0, -1 }, { 12, 0, -2 }, { 11, 0, -3 }, { 11, 0, -4 }, { 10, 0, -5 }, { 9, 0, -5 },
				{ 9, 0, -6 }, { 8, 0, -6 }, { 7, 0, -6 }, { 6, 0, -6 }, { 5, 0, -6 }, { 4, 0, -6 }, { 3, 0, -5 }, { 2, 0, -5 }, { 1, 0, -4 }, { 1, 0, -3 },
				{ 0, 1, -2 }, { 0, 1, -1 }, { 0, 1, 0 }, { 0, 1, 1 }, { 0, 1, 2 }, { 1, 1, 3 }, { 1, 1, 4 }, { 2, 1, 5 }, { 3, 1, 5 },
				{ 4, 1, 6 }, { 5, 1, 6 }, { 6, 1, 6 }, { 7, 1, 6 }, { 8, 1, 6 }, { 9, 1, 5 }, { 10, 1, 5 }, { 11, 1, 4 }, { 11, 1, 3 },
				{ 12, 1, 2 }, { 12, 1, 1 }, { 12, 1, 0 }, { 12, 1, -1 }, { 12, 1, -2 }, { 11, 1, -3 }, { 11, 1, -4 }, { 10, 1, -5 }, { 9, 1, -5 },
				{ 9, 1, -6 }, { 8, 1, -6 }, { 7, 1, -6 }, { 6, 1, -6 }, { 5, 1, -6 }, { 4, 1, -6 }, { 3, 1, -5 }, { 2, 1, -5 }, { 1, 1, -4 }, { 1, 1, -3 },
				// layer 3 and 4
				{ 0, 2, -1 }, { 0, 2, 0 }, { 0, 2, 1 }, { 1, 2, 2 }, { 2, 2, 3 }, { 2, 2, 4 }, { 3, 2, 4 }, { 4, 2, 5 },
				{ 5, 2, 6 }, { 6, 2, 6 }, { 7, 2, 6 }, { 8, 2, 5 }, { 9, 2, 4 }, { 10, 2, 4 }, { 10, 2, 3 }, { 11, 2, 2 },
				{ 12, 2, 1 }, { 12, 2, 0 }, { 12, 2, -1 }, { 11, 2, -2 }, { 10, 2, -3 }, { 10, 2, -4 }, { 9, 2, -4 }, { 8, 2, -5 },
				{ 7, 2, -6 }, { 6, 2, -6 }, { 5, 2, -6 }, { 4, 2, -5 }, { 3, 2, -4 }, { 2, 2, -4 }, { 2, 2, -3 }, { 1, 2, -2 },
				{ 0, 3, -1 }, { 0, 3, 0 }, { 0, 3, 1 }, { 1, 3, 2 }, { 2, 3, 3 }, { 2, 3, 4 }, { 3, 3, 4 }, { 4, 3, 5 },
				{ 5, 3, 6 }, { 6, 3, 6 }, { 7, 3, 6 }, { 8, 3, 5 }, { 9, 3, 4 }, { 10, 3, 4 }, { 10, 3, 3 }, { 11, 3, 2 },
				{ 12, 3, 1 }, { 12, 3, 0 }, { 12, 3, -1 }, { 11, 3, -2 }, { 10, 3, -3 }, { 10, 3, -4 }, { 9, 3, -4 }, { 8, 3, -5 },
				{ 7, 3, -6 }, { 6, 3, -6 }, { 5, 3, -6 }, { 4, 3, -5 }, { 3, 3, -4 }, { 2, 3, -4 }, { 2, 3, -3 }, { 1, 3, -2 },
				// layers 5 to 14
				{ 1, 4, -1 }, { 1, 4, 0 }, { 1, 4, 1 }, { 2, 4, 2 }, { 3, 4, 3 }, { 4, 4, 4 }, { 5, 4, 5 },
				{ 6, 4, 5 }, { 7, 4, 5 }, { 8, 4, 4 }, { 9, 4, 3 }, { 10, 4, 2 }, { 11, 4, 1 }, { 11, 4, 0 },
				{ 11, 4, -1 }, { 10, 4, -2 }, { 9, 4, -3 }, { 8, 4, -4 }, { 7, 4, -5 }, { 6, 4, -5 }, { 5, 4, -5 },
				{ 4, 4, -4 }, { 3, 4, -3 }, { 2, 4, -2 }, { 1, 5, -1 }, { 1, 5, 0 }, { 1, 5, 1 }, { 2, 5, 2 },
				{ 3, 5, 3 }, { 4, 5, 4 }, { 5, 5, 5 }, { 6, 5, 5 }, { 7, 5, 5 }, { 8, 5, 4 }, { 9, 5, 3 },
				{ 10, 5, 2 }, { 11, 5, 1 }, { 11, 5, 0 }, { 11, 5, -1 }, { 10, 5, -2 }, { 9, 5, -3 }, { 8, 5, -4 },
				{ 7, 5, -5 }, { 6, 5, -5 }, { 5, 5, -5 }, { 4, 5, -4 }, { 3, 5, -3 }, { 2, 5, -2 }, { 2, 6, -1 },
				{ 2, 6, 0 }, { 2, 6, 1 }, { 3, 6, 2 }, { 4, 6, 3 }, { 5, 6, 4 }, { 6, 6, 4 }, { 7, 6, 4 },
				{ 8, 6, 3 }, { 9, 6, 2 }, { 10, 6, 1 }, { 10, 6, 0 }, { 10, 6, -1 }, { 9, 6, -2 }, { 8, 6, -3 },
				{ 7, 6, -4 }, { 6, 6, -4 }, { 5, 6, -4 }, { 4, 6, -3 }, { 3, 6, -2 }, { 2, 7, -1 }, { 2, 7, 0 },
				{ 2, 7, 1 }, { 3, 7, 2 }, { 4, 7, 3 }, { 5, 7, 4 }, { 6, 7, 4 }, { 7, 7, 4 }, { 8, 7, 3 },
				{ 9, 7, 2 }, { 10, 7, 1 }, { 10, 7, 0 }, { 10, 7, -1 }, { 9, 7, -2 }, { 8, 7, -3 }, { 7, 7, -4 },
				{ 6, 7, -4 }, { 5, 7, -4 }, { 4, 7, -3 }, { 3, 7, -2 }, { 3, 8, -1 }, { 3, 8, 0 }, { 3, 8, 1 },
				{ 4, 8, 2 }, { 5, 8, 3 }, { 6, 8, 3 }, { 7, 8, 3 }, { 8, 8, 2 }, { 9, 8, 1 }, { 9, 8, 0 },
				{ 9, 8, -1 }, { 8, 8, -2 }, { 7, 8, -3 }, { 6, 8, -3 }, { 5, 8, -3 }, { 4, 8, -2 }, { 3, 9, -1 },
				{ 3, 9, 0 }, { 3, 9, 1 }, { 4, 9, 2 }, { 5, 9, 3 }, { 6, 9, 3 }, { 7, 9, 3 }, { 8, 9, 2 },
				{ 9, 9, 1 }, { 9, 9, 0 }, { 9, 9, -1 }, { 8, 9, -2 }, { 7, 9, -3 }, { 6, 9, -3 }, { 5, 9, -3 },
				{ 4, 9, -2 }, 
				{ 4, 10, -1 }, { 4, 10, 0 }, { 4, 10, 1 }, { 5, 10, 2 }, { 6, 10, 2 }, { 7, 10, 2 },
				{ 8, 10, 1 }, { 8, 10, 0 }, { 8, 10, -1 }, { 7, 10, -2 }, { 6, 10, -2 }, { 5, 10, -2 },
				{ 4, 11, -1 }, { 4, 11, 0 }, { 4, 11, 1 }, { 5, 11, 2 }, { 6, 11, 2 }, { 7, 11, 2 }, { 8, 11, 1 },
				{ 8, 11, 0 }, { 8, 11, -1 }, { 7, 11, -2 }, { 6, 11, -2 }, { 5, 11, -2 }, 
				{ 5, 12, -1 },
				{ 5, 12, 0 }, { 5, 12, 1 }, { 6, 12, 1 }, { 7, 12, 1 }, { 7, 12, 0 }, { 7, 12, -1 }, { 6, 12, -1 },
				{ 5, 13, -1 }, { 5, 13, 0 }, { 5, 13, 1 }, { 6, 13, 1 }, { 7, 13, 1 }, { 7, 13, 0 }, { 7, 13, -1 },
				{ 6, 13, -1 }			
			});
			bp.addBarrierCoords(new int[][] { { 6, 14, 0 } });
			break;
		case HUGE:
			bp.addWallCoords(new int[][] {
				// layer 1 and 2
				{ 0, 0, -2 }, { 0, 0, -1 }, { 0, 0, 0 }, { 0, 0, 1 }, { 0, 0, 2 }, { 1, 0, 3 }, { 2, 0, 4 },
				{ 3, 0, 5, }, { 4, 0, 5 }, { 5, 0, 5 }, { 6, 0, 5 }, { 7, 0, 5 }, { 8, 0, 4 }, { 9, 0, 3 },
				{ 10, 0, 2 }, { 10, 0, 1 }, { 10, 0, 0 }, { 10, 0, -1 }, { 10, 0, -2 }, { 9, 0, -3 }, { 8, 0, -4 },
				{ 7, 0, -5 }, { 6, 0, -5 }, { 5, 0, -5 }, { 4, 0, -5 }, { 3, 0, -5 }, { 2, 0, -4 }, { 1, 0, -3 },
				{ 0, 1, -2 }, { 0, 1, -1 }, { 0, 1, 0 }, { 0, 1, 1 }, { 0, 1, 2 }, { 1, 1, 3 }, { 2, 1, 4 },
				{ 3, 1, 5, }, { 4, 1, 5 }, { 5, 1, 5 }, { 6, 1, 5 }, { 7, 1, 5 }, { 8, 1, 4 }, { 9, 1, 3 },
				{ 10, 1, 2 }, { 10, 1, 1 }, { 10, 1, 0 }, { 10, 1, -1 }, { 10, 1, -2 }, { 9, 1, -3 }, { 8, 1, -4 },
				{ 7, 1, -5 }, { 6, 1, -5 }, { 5, 1, -5 }, { 4, 1, -5 }, { 3, 1, -5 }, { 2, 1, -4 }, { 1, 1, -3 },
				// layer 3 and 4
				{ 0, 2, -1 }, { 0, 2, 0 }, { 0, 2, 1 }, { 1, 2, 2 }, { 2, 2, 3 }, { 3, 2, 4 },
				{ 4, 2, 5 }, { 5, 2, 5 }, { 6, 2, 5 }, { 7, 2, 4 }, { 8, 2, 3 }, { 9, 2, 2 },
				{ 10, 2, 1 }, { 10, 2, 0 }, { 10, 2, -1 }, { 9, 2, -2 }, { 8, 2, -3 }, { 7, 2, -4 },
				{ 6, 2, -5 }, { 5, 2, -5 }, { 4, 2, -5 }, { 3, 2, -4 }, { 2, 2, -3 }, { 1, 2, -2 },
				{ 0, 3, -1 }, { 0, 3, 0 }, { 0, 3, 1 }, { 1, 3, 2 }, { 2, 3, 3 }, { 3, 3, 4 },
				{ 4, 3, 5 }, { 5, 3, 5 }, { 6, 3, 5 }, { 7, 3, 4 }, { 8, 3, 3 }, { 9, 3, 2 },
				{ 10, 3, 1 }, { 10, 3, 0 }, { 10, 3, -1 }, { 9, 3, -2 }, { 8, 3, -3 }, { 7, 3, -4 },
				{ 6, 3, -5 }, { 5, 3, -5 }, { 4, 3, -5 }, { 3, 3, -4 }, { 2, 3, -3 }, { 1, 3, -2 },
				// layer 5 and 6
				{ 1, 4, -1 }, { 1, 4, 0 }, { 1, 4, 1 }, { 2, 4, 2 }, { 3, 4, 3 },
				{ 4, 4, 4 }, { 5, 4, 4 }, { 6, 4, 4 }, { 7, 4, 3 }, { 8, 4, 2 },
				{ 9, 4, 1 }, { 9, 4, 0 }, { 9, 4, -1 }, { 8, 4, -2 }, { 7, 4, -3 },
				{ 6, 4, -4 }, { 5, 4, -4 }, { 4, 4, -4 }, { 3, 4, -3 }, { 2, 4, -2 },
				{ 1, 5, -1 }, { 1, 5, 0 }, { 1, 5, 1 }, { 2, 5, 2 }, { 3, 5, 3 },
				{ 4, 5, 4 }, { 5, 5, 4 }, { 6, 5, 4 }, { 7, 5, 3 }, { 8, 5, 2 },
				{ 9, 5, 1 }, { 9, 5, 0 }, { 9, 5, -1 }, { 8, 5, -2 }, { 7, 5, -3 },
				{ 6, 5, -4 }, { 5, 5, -4 }, { 4, 5, -4 }, { 3, 5, -3 }, { 2, 5, -2 },
				// layer 7 and 8
				{ 2, 6, -1 }, { 2, 6, 0 }, { 2, 6, 1 }, { 3, 6, 2 }, { 4, 6, 3 }, { 5, 6, 3 }, { 6, 6, 3 }, { 7, 6, 2 },
				{ 8, 6, 1 }, { 8, 6, 0 }, { 8, 6, -1 }, { 7, 6, -2 }, { 6, 6, -3 }, { 5, 6, -3 }, { 4, 6, -3 }, { 3, 6, -2 },
				{ 2, 7, -1 }, { 2, 7, 0 }, { 2, 7, 1 }, { 3, 7, 2 }, { 4, 7, 3 }, { 5, 7, 3 }, { 6, 7, 3 }, { 7, 7, 2 },
				{ 8, 7, 1 }, { 8, 7, 0 }, { 8, 7, -1 }, { 7, 7, -2 }, { 6, 7, -3 }, { 5, 7, -3 }, { 4, 7, -3 }, { 3, 7, -2 },
				// layer 9 and 10
				{ 3, 8, -1 }, { 3, 8, 0 }, { 3, 8, 1 }, { 4, 8, 2 }, { 5, 8, 2 }, { 6, 8, 2 },
				{ 7, 8, 1 }, { 7, 8, 0 }, { 7, 8, -1 }, { 6, 8, -2 }, { 5, 8, -2 }, { 4, 8, -2 },
				{ 3, 9, -1 }, { 3, 9, 0 }, { 3, 9, 1 }, { 4, 9, 2 }, { 5, 9, 2 }, { 6, 9, 2 },
				{ 7, 9, 1 }, { 7, 9, 0 }, { 7, 9, -1 }, { 6, 9, -2 }, { 5, 9, -2 }, { 4, 9, -2 },
				// layer 11 and 12
				{ 4, 10, -1 }, { 4, 10, 0 }, { 4, 10, 1 }, { 5, 10, 1 }, { 6, 10, 1 }, { 6, 10, 0 }, { 6, 10, -1 }, { 5, 10, -1 },
				{ 4, 11, -1 }, { 4, 11, 0 }, { 4, 11, 1 }, { 5, 11, 1 }, { 6, 11, 1 }, { 6, 11, 0 }, { 6, 11, -1 }, { 5, 11, -1 }
			});
			bp.addBarrierCoords(new int[][] { { 5, 12, 0 } });
			break;
		case LARGE:
			bp.addWallCoords(new int[][] {
					// layer 1 and 2
					{ 0, 0, -2 }, { 0, 0, -1 }, { 0, 0, 0 }, { 0, 0, 1 }, { 0, 0, 2 }, { 1, 0, 3 }, { 2, 0, 4 },
					{ 3, 0, 4 }, { 4, 0, 4 }, { 5, 0, 4 }, { 6, 0, 4 }, { 7, 0, 3 }, { 8, 0, 2 }, { 8, 0, 1 },
					{ 8, 0, 0 }, { 8, 0, -1 }, { 8, 0, -2 }, { 7, 0, -3 }, { 6, 0, -4 }, { 5, 0, -4 }, { 4, 0, -4 },
					{ 3, 0, -4 }, { 2, 0, -4 }, { 1, 0, -3 },
					{ 0, 1, -2 }, { 0, 1, -1 }, { 0, 1, 0 }, { 0, 1, 1 }, { 0, 1, 2 }, { 1, 1, 3 }, { 2, 1, 4 },
					{ 3, 1, 4 }, { 4, 1, 4 }, { 5, 1, 4 }, { 6, 1, 4 }, { 7, 1, 3 }, { 8, 1, 2 }, { 8, 1, 1 },
					{ 8, 1, 0 }, { 8, 1, -1 }, { 8, 1, -2 }, { 7, 1, -3 }, { 6, 1, -4 }, { 5, 1, -4 }, { 4, 1, -4 },
					{ 3, 1, -4 }, { 2, 1, -4 }, { 1, 1, -3 },
					// layer 3 and 4
					{ 0, 2, -1 }, { 0, 2, 0 }, { 0, 2, 1 }, { 1, 2, 2 }, { 2, 2, 3 }, { 3, 2, 4 }, { 4, 2, 4 },
					{ 5, 2, 4 }, { 6, 2, 3 }, { 7, 2, 2 }, { 8, 2, 1 }, { 8, 2, 0 }, { 8, 2, -1 }, { 7, 2, -2 },
					{ 6, 2, -3 }, { 5, 2, -4 }, { 4, 2, -4 }, { 3, 2, -4 }, { 2, 2, -3 }, { 1, 2, -2 },
					{ 0, 3, -1 }, { 0, 3, 0 }, { 0, 3, 1 }, { 1, 3, 2 }, { 2, 3, 3 }, { 3, 3, 4 }, { 4, 3, 4 },
					{ 5, 3, 4 }, { 6, 3, 3 }, { 7, 3, 2 }, { 8, 3, 1 }, { 8, 3, 0 }, { 8, 3, -1 }, { 7, 3, -2 },
					{ 6, 3, -3 }, { 5, 3, -4 }, { 4, 3, -4 }, { 3, 3, -4 }, { 2, 3, -3 }, { 1, 3, -2 },
					// layer 5 and 6
					{ 1, 4, -1 }, { 1, 4, 0 }, { 1, 4, 1 }, { 2, 4, 2 }, { 3, 4, 3 }, { 4, 4, 3 }, { 5, 4, 3 },
					{ 6, 4, 2 }, { 7, 4, 1 }, { 7, 4, 0 }, { 7, 4, -1 }, { 6, 4, -2 }, { 5, 4, -3 }, { 4, 4, -3 },
					{ 3, 4, -3 }, { 2, 4, -2 },
					{ 1, 5, -1 }, { 1, 5, 0 }, { 1, 5, 1 }, { 2, 5, 2 }, { 3, 5, 3 }, { 4, 5, 3 }, { 5, 5, 3 },
					{ 6, 5, 2 }, { 7, 5, 1 }, { 7, 5, 0 }, { 7, 5, -1 }, { 6, 5, -2 }, { 5, 5, -3 }, { 4, 5, -3 },
					{ 3, 5, -3 }, { 2, 5, -2 },
					// layer 7 and 8
					{ 2, 6, 1 }, { 2, 6, 0 }, { 2, 6, -1 }, { 3, 6, -2 }, { 4, 6, -2 }, { 5, 6, -2 }, { 6, 6, -1 },
					{ 6, 6, 0 }, { 6, 6, 1 }, { 5, 6, 2 }, { 4, 6, 2 }, { 3, 6, 2 },
					{ 2, 7, 1 }, { 2, 7, 0 }, { 2, 7, -1 }, { 3, 7, -2 }, { 4, 7, -2 }, { 5, 7, -2 }, { 6, 7, -1 },
					{ 6, 7, 0 }, { 6, 7, 1 }, { 5, 7, 2 }, { 4, 7, 2 }, { 3, 7, 2 },
					// layer 9 and 10
					{ 3, 8, -1 }, { 3, 8, 0 }, { 3, 8, 1 }, { 4, 8, 1 }, { 5, 8, 1 }, { 5, 8, 0 }, { 5, 8, -1 },
					{ 4, 8, -1 },
					{ 3, 9, -1 }, { 3, 9, 0 }, { 3, 9, 1 }, { 4, 9, 1 }, { 5, 9, 1 }, { 5, 9, 0 }, { 5, 9, -1 },
					{ 4, 9, -1 } });
			bp.addBarrierCoords(new int[][] { { 4, 10, 0 } });
			break;
		case MEDIUM:
			bp.addWallCoords(new int[][] {
					// layer 1 and 2
					{ 0, 0, -1 }, { 0, 0, 0 }, { 0, 0, 1 }, { 1, 0, 2 }, { 2, 0, 3 }, { 3, 0, 3 }, { 4, 0, 3 },
					{ 5, 0, 2 }, { 6, 0, 1 }, { 6, 0, 0 }, { 6, 0, -1 }, { 5, 0, -2 }, { 4, 0, -3 }, { 3, 0, -3 },
					{ 2, 0, -3 }, { 1, 0, -2 },
					{ 0, 1, -1 }, { 0, 1, 0 }, { 0, 1, 1 }, { 1, 1, 2 }, { 2, 1, 3 }, { 3, 1, 3 }, { 4, 1, 3 },
					{ 5, 1, 2 }, { 6, 1, 1 }, { 6, 1, 0 }, { 6, 1, -1 }, { 5, 1, -2 }, { 4, 1, -3 }, { 3, 1, -3 },
					{ 2, 1, -3 }, { 1, 1, -2 },
					// layer 3 and 4
					{ 0, 2, 0 }, { 1, 2, 1 }, { 1, 2, -1 }, { 2, 2, -2 }, { 3, 2, -2 }, { 4, 2, -2 }, { 5, 2, -1 },
					{ 5, 2, 0 }, { 5, 2, 1 }, { 4, 2, 2 }, { 3, 2, 2 }, { 2, 2, 2 },
					{ 1, 3, 1 }, { 1, 3, 0 }, { 1, 3, -1 }, { 2, 3, -2 }, { 3, 3, -2 }, { 4, 3, -2 }, { 5, 3, -1 },
					{ 5, 3, 0 }, { 5, 3, 1 }, { 4, 3, 2 }, { 3, 3, 2 }, { 2, 3, 2 },
					// layer 5 and 6
					{ 2, 4, -1 }, { 2, 4, 0 }, { 2, 4, 1 }, { 3, 4, 1 }, { 4, 4, 1 }, { 4, 4, 0 }, { 4, 4, -1 },
					{ 3, 4, -1 },
					{ 2, 5, -1 }, { 2, 5, 0 }, { 2, 5, 1 }, { 3, 5, 1 }, { 4, 5, 1 }, { 4, 5, 0 }, { 4, 5, -1 },
					{ 3, 5, -1 },
					// layer 7 and 8
					{ 2, 6, 0 }, { 3, 6, 1 }, { 4, 6, 0 }, { 3, 6, -1 },
					{ 2, 7, 0 }, { 3, 7, 1 }, { 4, 7, 0 }, { 3, 7, -1 } });
			bp.addBarrierCoords(new int[][] { { 3, 8, 0 } });
			break;
		case SMALL:
			bp.addWallCoords(new int[][] {
					// layer 1 and 2
					{ 0, 0, 1 }, { 0, 0, 0 }, { 0, 0, -1 }, { 1, 0, -2 }, { 2, 0, -2 }, { 3, 0, -2 }, { 4, 0, -1 },
					{ 4, 0, 0 }, { 4, 0, 1 }, { 3, 0, 2 }, { 2, 0, 2 }, { 1, 0, 2 },
					{ 0, 1, 1 }, { 0, 1, 0 }, { 0, 1, -1 }, { 1, 1, -2 }, { 2, 1, -2 }, { 3, 1, -2 }, { 4, 1, -1 },
					{ 4, 1, 0 }, { 4, 1, 1 }, { 3, 1, 2 }, { 2, 1, 2 }, { 1, 1, 2 },
					// layer 3 and 4
					{ 0, 2, 0 }, { 1, 2, -1 }, { 1, 2, 1 }, { 2, 2, 1 }, { 3, 2, 1 }, { 3, 2, 0 }, { 3, 2, -1 },
					{ 2, 2, -1 },
					{ 1, 3, -1 }, { 1, 3, 0 }, { 1, 3, 1 }, { 2, 3, 1 }, { 3, 3, 1 }, { 3, 3, 0 }, { 3, 3, -1 },
					{ 2, 3, -1 },
					// layer 5 and 6
					{ 1, 4, 0 }, { 2, 4, 1 }, { 3, 4, 0 }, { 2, 4, -1 },
					{ 1, 5, 0 }, { 2, 5, 1 }, { 3, 5, 0 }, { 2, 5, -1 } });
			bp.addBarrierCoords(new int[][] { { 2, 6, 0 } });
			break;
		}
		return bp;
	}
}
