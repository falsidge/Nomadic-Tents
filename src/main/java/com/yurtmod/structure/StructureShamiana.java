package com.yurtmod.structure;

import com.yurtmod.block.BlockShamianaWall;
import com.yurtmod.dimension.TentDimension;
import com.yurtmod.init.Content;
import com.yurtmod.structure.util.Blueprint;
import com.yurtmod.structure.util.StructureTent;
import com.yurtmod.structure.util.StructureWidth;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureShamiana extends StructureBase {

	@Override
	public StructureTent getTentType() {
		return StructureTent.SHAMIANA;
	}

	@Override
	public boolean generate(World worldIn, BlockPos doorBase, Direction dirForward, StructureWidth structureWidth,
			BlockState doorBlock, BlockState wallBlock, BlockState roofBlock) {
		final boolean tentDim = TentDimension.isTentDimension(worldIn);
		final Blueprint bp = getBlueprints(structureWidth);
		if (bp == null) {
			return false;
		}
		// build all relevant layers
		this.buildLayer(worldIn, doorBase, dirForward, wallBlock, bp.getWallCoords());
		this.buildLayer(worldIn, doorBase, dirForward, roofBlock, bp.getRoofCoords());
		// make door
		buildDoor(worldIn, doorBase, doorBlock, dirForward);
		// add dimension-only features
		final int structureWidthNum = Math.floorDiv(structureWidth.getSquareWidth(), 2);
		if (tentDim) {
			final boolean isRemoving = wallBlock.getMaterial() == Material.AIR;
			final Block pole = Blocks.OAK_FENCE;
			// place a pole in the middle
			BlockPos pos = getPosFromDoor(doorBase, structureWidthNum, 0, 0, TentDimension.STRUCTURE_DIR);
			final int height = structureWidthNum + 3;
			for (int i = 0; i < height; i++) {
				final BlockPos p = pos.up(i);
				if (isRemoving && worldIn.getBlockState(p).getBlock() == pole) {
					worldIn.setBlockToAir(p);
				} else if (/* structureWidth != StructureWidth.SMALL && */ !isRemoving && worldIn.isAirBlock(p)) {
					worldIn.setBlockState(p, pole.getDefaultState());
				}
			}
			super.buildLayer(worldIn, doorBase, dirForward, Content.TENT_BARRIER.getDefaultState(),
					bp.getBarrierCoords());
		}
		return !bp.isEmpty();
	}

	@Override
	public void buildLayer(final World worldIn, final BlockPos doorPos, final Direction dirForward,
			final BlockState stateIn, final BlockPos[] coordinates) {
		BlockState state = stateIn;
		final boolean isWall = state.getBlock().getClass() == BlockShamianaWall.class;
		if (isWall) {
			state = BlockShamianaWall.getShamianaState(this.data.getColor(), false, true);
		}
		for (final BlockPos coord : coordinates) {
			final BlockPos pos = getPosFromDoor(doorPos, coord, dirForward);
			if (isWall) {
				state = state.withProperty(BlockShamianaWall.PATTERN, BlockShamianaWall.shouldBePattern(pos, doorPos));
			}
			worldIn.setBlockState(pos, state, 3);
		}
	}

	public static Blueprint makeBlueprints(final StructureWidth StructureWidth) {
		final Blueprint bp = new Blueprint();
		switch (StructureWidth) {
		case SMALL:
			bp.addWallCoords(new int[][] {
					// layer 1
					{ 0, 0, 1 }, { 0, 0, 0 }, { 0, 0, -1 }, { 1, 0, -2 }, { 2, 0, -2 }, { 3, 0, -2 }, { 4, 0, -1 },
					{ 4, 0, 0 }, { 4, 0, 1 }, { 3, 0, 2 }, { 2, 0, 2 }, { 1, 0, 2 },
					// layer 2
					{ 0, 1, 1 }, { 0, 1, 0 }, { 0, 1, -1 }, { 1, 1, -2 }, { 2, 1, -2 }, { 3, 1, -2 }, { 4, 1, -1 },
					{ 4, 1, 0 }, { 4, 1, 1 }, { 3, 1, 2 }, { 2, 1, 2 }, { 1, 1, 2 },
					// layer 3
					{ 0, 2, 1 }, { 0, 2, 0 }, { 0, 2, -1 }, { 1, 2, -2 }, { 2, 2, -2 }, { 3, 2, -2 }, { 4, 2, -1 },
					{ 4, 2, 0 }, { 4, 2, 1 }, { 3, 2, 2 }, { 2, 2, 2 }, { 1, 2, 2 } });
			bp.addRoofCoords(new int[][] {
					// layer 4
					{ 1, 3, -1 }, { 1, 3, 0 }, { 1, 3, 1 }, { 2, 3, 1 }, { 3, 3, 1 }, { 3, 3, 0 }, { 3, 3, -1 },
					{ 2, 3, -1 },
					// layer 5
					{ 1, 4, 0 }, { 2, 4, 1 }, { 3, 4, 0 }, { 2, 4, -1 } });
			bp.addBarrierCoords(new int[][] { { 2, 5, 0 } });
			break;
		case MEDIUM:
			bp.addWallCoords(new int[][] {
					// layer 1
					{ 0, 0, -2 }, { 0, 0, -1 }, { 0, 0, 0 }, { 0, 0, 1 }, { 0, 0, 2 }, { 1, 0, 3 }, { 2, 0, 3 },
					{ 3, 0, 3 }, { 4, 0, 3 }, { 5, 0, 3 }, { 6, 0, -2 }, { 6, 0, -1 }, { 6, 0, 0 }, { 6, 0, 1 },
					{ 6, 0, 2 }, { 1, 0, -3 }, { 2, 0, -3 }, { 3, 0, -3 }, { 4, 0, -3 }, { 5, 0, -3 },
					// layer 2
					{ 0, 1, -2 }, { 0, 1, -1 }, { 0, 1, 0 }, { 0, 1, 1 }, { 0, 1, 2 }, { 1, 1, 3 }, { 2, 1, 3 },
					{ 3, 1, 3 }, { 4, 1, 3 }, { 5, 1, 3 }, { 6, 1, -2 }, { 6, 1, -1 }, { 6, 1, 0 }, { 6, 1, 1 },
					{ 6, 1, 2 }, { 1, 1, -3 }, { 2, 1, -3 }, { 3, 1, -3 }, { 4, 1, -3 }, { 5, 1, -3 },
					// layer 3
					{ 0, 2, -2 }, { 0, 2, -1 }, { 0, 2, 0 }, { 0, 2, 1 }, { 0, 2, 2 }, { 1, 2, 3 }, { 2, 2, 3 },
					{ 3, 2, 3 }, { 4, 2, 3 }, { 5, 2, 3 }, { 6, 2, -2 }, { 6, 2, -1 }, { 6, 2, 0 }, { 6, 2, 1 },
					{ 6, 2, 2 }, { 1, 2, -3 }, { 2, 2, -3 }, { 3, 2, -3 }, { 4, 2, -3 }, { 5, 2, -3 } });
			bp.addRoofCoords(new int[][] {
					// layer 4
					{ 1, 3, -2 }, { 1, 3, -1 }, { 1, 3, 0 }, { 1, 3, 1 }, { 1, 3, 2 }, { 2, 3, 2 }, { 3, 3, 2 },
					{ 4, 3, 2 }, { 5, 3, 2 }, { 5, 3, 1 }, { 5, 3, 0 }, { 5, 3, -1 }, { 5, 3, -2 }, { 4, 3, -2 },
					{ 3, 3, -2 }, { 2, 3, -2 }, { 1, 3, -2 },
					// layer 5
					{ 2, 4, -1 }, { 2, 4, 0 }, { 2, 4, 1 }, { 3, 4, 1 }, { 4, 4, 1 }, { 4, 4, 0 }, { 4, 4, -1 },
					{ 3, 4, -1 },
					// layer 6
					{ 2, 5, 0 }, { 3, 5, 1 }, { 4, 5, 0 }, { 3, 5, -1 } });
			bp.addBarrierCoords(new int[][] { { 3, 6, 0 } });
			break;
		case LARGE:
			bp.addWallCoords(new int[][] {
					// layer 1
					{ 0, 0, -3 }, { 0, 0, -2 }, { 0, 0, -1 }, { 0, 0, 0 }, { 0, 0, 1 }, { 0, 0, 2 }, { 0, 0, 3 },
					{ 1, 0, 4 }, { 2, 0, 4 }, { 3, 0, 4 }, { 4, 0, 4 }, { 5, 0, 4 }, { 6, 0, 4 }, { 7, 0, 4 },
					{ 8, 0, -3 }, { 8, 0, -2 }, { 8, 0, -1 }, { 8, 0, 0 }, { 8, 0, 1 }, { 8, 0, 2 }, { 8, 0, 3 },
					{ 1, 0, -4 }, { 2, 0, -4 }, { 3, 0, -4 }, { 4, 0, -4 }, { 5, 0, -4 }, { 6, 0, -4 }, { 7, 0, -4 },
					// layer 2
					{ 0, 1, -3 }, { 0, 1, -2 }, { 0, 1, -1 }, { 0, 1, 0 }, { 0, 1, 1 }, { 0, 1, 2 }, { 0, 1, 3 },
					{ 1, 1, 4 }, { 2, 1, 4 }, { 3, 1, 4 }, { 4, 1, 4 }, { 5, 1, 4 }, { 6, 1, 4 }, { 7, 1, 4 },
					{ 8, 1, -3 }, { 8, 1, -2 }, { 8, 1, -1 }, { 8, 1, 0 }, { 8, 1, 1 }, { 8, 1, 2 }, { 8, 1, 3 },
					{ 1, 1, -4 }, { 2, 1, -4 }, { 3, 1, -4 }, { 4, 1, -4 }, { 5, 1, -4 }, { 6, 1, -4 }, { 7, 1, -4 },
					// layer 3
					{ 0, 2, -3 }, { 0, 2, -2 }, { 0, 2, -1 }, { 0, 2, 0 }, { 0, 2, 1 }, { 0, 2, 2 }, { 0, 2, 3 },
					{ 1, 2, 4 }, { 2, 2, 4 }, { 3, 2, 4 }, { 4, 2, 4 }, { 5, 2, 4 }, { 6, 2, 4 }, { 7, 2, 4 },
					{ 8, 2, -3 }, { 8, 2, -2 }, { 8, 2, -1 }, { 8, 2, 0 }, { 8, 2, 1 }, { 8, 2, 2 }, { 8, 2, 3 },
					{ 1, 2, -4 }, { 2, 2, -4 }, { 3, 2, -4 }, { 4, 2, -4 }, { 5, 2, -4 }, { 6, 2, -4 }, { 7, 2, -4 } });
			bp.addRoofCoords(new int[][] {
					// layer 4
					{ 1, 3, -3 }, { 1, 3, -2 }, { 1, 3, -1 }, { 1, 3, 0 }, { 1, 3, 1 }, { 1, 3, 2 }, { 1, 3, 3 },
					{ 2, 3, 3 }, { 3, 3, 3 }, { 4, 3, 3 }, { 5, 3, 3 }, { 6, 3, 3 }, { 7, 3, 3 }, { 7, 3, 2 },
					{ 7, 3, 1 }, { 7, 3, 0 }, { 7, 3, -1 }, { 7, 3, -2 }, { 7, 3, -3 }, { 6, 3, -3 }, { 5, 3, -3 },
					{ 4, 3, -3 }, { 3, 3, -3 }, { 2, 3, -3 },
					// layer 5
					{ 2, 4, -2 }, { 2, 4, -1 }, { 2, 4, 0 }, { 2, 4, 1 }, { 2, 4, 2 }, { 3, 4, 2 }, { 4, 4, 2 },
					{ 5, 4, 2 }, { 6, 4, 2 }, { 6, 4, 1 }, { 6, 4, 0 }, { 6, 4, -1 }, { 6, 4, -2 }, { 5, 4, -2 },
					{ 4, 4, -2 }, { 3, 4, -2 }, { 2, 4, -2 },
					// layer 6
					{ 3, 5, -1 }, { 3, 5, 0 }, { 3, 5, 1 }, { 4, 5, 1 }, { 5, 5, 1 }, { 5, 5, 0 }, { 5, 5, -1 },
					{ 4, 5, -1 },
					// layer 7
					{ 3, 6, 0 }, { 4, 6, 1 }, { 5, 6, 0 }, { 4, 6, -1 } });
			bp.addBarrierCoords(new int[][] { { 4, 7, 0 } });
			break;
		case HUGE:
			bp.addWallCoords(new int[][] {
					// layer 1
					{ 0, 0, -4 }, { 0, 0, -3 }, { 0, 0, -2 }, { 0, 0, -1 }, { 0, 0, 0 }, { 0, 0, 1 }, { 0, 0, 2 },
					{ 0, 0, 3 }, { 0, 0, 4 }, { 1, 0, 5 }, { 2, 0, 5 }, { 3, 0, 5, }, { 4, 0, 5 }, { 5, 0, 5 },
					{ 6, 0, 5 }, { 7, 0, 5 }, { 8, 0, 5 }, { 9, 0, 5 }, { 10, 0, 4 }, { 10, 0, 3 }, { 10, 0, 2 },
					{ 10, 0, 1 }, { 10, 0, 0 }, { 10, 0, -1 }, { 10, 0, -2 }, { 10, 0, -3 }, { 10, 0, -4 },
					{ 9, 0, -5 }, { 8, 0, -5 }, { 7, 0, -5 }, { 6, 0, -5 }, { 5, 0, -5 }, { 4, 0, -5 }, { 3, 0, -5 },
					{ 2, 0, -5 }, { 1, 0, -5 },
					// layer 2
					{ 0, 1, -4 }, { 0, 1, -3 }, { 0, 1, -2 }, { 0, 1, -1 }, { 0, 1, 0 }, { 0, 1, 1 }, { 0, 1, 2 },
					{ 0, 1, 3 }, { 0, 1, 4 }, { 1, 1, 5 }, { 2, 1, 5 }, { 3, 1, 5, }, { 4, 1, 5 }, { 5, 1, 5 },
					{ 6, 1, 5 }, { 7, 1, 5 }, { 8, 1, 5 }, { 9, 1, 5 }, { 10, 1, 4 }, { 10, 1, 3 }, { 10, 1, 2 },
					{ 10, 1, 1 }, { 10, 1, 0 }, { 10, 1, -1 }, { 10, 1, -2 }, { 10, 1, -3 }, { 10, 1, -4 },
					{ 9, 1, -5 }, { 8, 1, -5 }, { 7, 1, -5 }, { 6, 1, -5 }, { 5, 1, -5 }, { 4, 1, -5 }, { 3, 1, -5 },
					{ 2, 1, -5 }, { 1, 1, -5 },
					// layer 3
					{ 0, 2, -4 }, { 0, 2, -3 }, { 0, 2, -2 }, { 0, 2, -1 }, { 0, 2, 0 }, { 0, 2, 1 }, { 0, 2, 2 },
					{ 0, 2, 3 }, { 0, 2, 4 }, { 1, 2, 5 }, { 2, 2, 5 }, { 3, 2, 5, }, { 4, 2, 5 }, { 5, 2, 5 },
					{ 6, 2, 5 }, { 7, 2, 5 }, { 8, 2, 5 }, { 9, 2, 5 }, { 10, 2, 4 }, { 10, 2, 3 }, { 10, 2, 2 },
					{ 10, 2, 1 }, { 10, 2, 0 }, { 10, 2, -1 }, { 10, 2, -2 }, { 10, 2, -3 }, { 10, 2, -4 },
					{ 9, 2, -5 }, { 8, 2, -5 }, { 7, 2, -5 }, { 6, 2, -5 }, { 5, 2, -5 }, { 4, 2, -5 }, { 3, 2, -5 },
					{ 2, 2, -5 }, { 1, 2, -5 } });
			bp.addRoofCoords(new int[][] {
					// layer 4
					{ 1, 3, -4 }, { 1, 3, -3 }, { 1, 3, -2 }, { 1, 3, -1 }, { 1, 3, 0 }, { 1, 3, 1 }, { 1, 3, 2 },
					{ 1, 3, 3 }, { 1, 3, 4 }, { 2, 3, 4 }, { 3, 3, 4 }, { 4, 3, 4 }, { 5, 3, 4 }, { 6, 3, 4 },
					{ 7, 3, 4 }, { 8, 3, 4 }, { 9, 3, 4 }, { 9, 3, 3 }, { 9, 3, 2 }, { 9, 3, 1 }, { 9, 3, 0 },
					{ 9, 3, -1 }, { 9, 3, -2 }, { 9, 3, -3 }, { 9, 3, -4 }, { 8, 3, -4 }, { 7, 3, -4 }, { 6, 3, -4 },
					{ 5, 3, -4 }, { 4, 3, -4 }, { 3, 3, -4 }, { 2, 3, -4 },
					// layer 5 - 7
					{ 2, 4, -3 }, { 2, 4, -2 }, { 2, 4, -1 }, { 2, 4, 0 }, { 2, 4, 1 }, { 2, 4, 2 }, { 2, 4, 3 },
					{ 3, 4, 3 }, { 4, 4, 3 }, { 5, 4, 3 }, { 6, 4, 3 }, { 7, 4, 3 }, { 8, 4, 3 }, { 8, 4, 2 },
					{ 8, 4, 1 }, { 8, 4, 0 }, { 8, 4, -1 }, { 8, 4, -2 }, { 8, 4, -3 }, { 7, 4, -3 }, { 6, 4, -3 },
					{ 5, 4, -3 }, { 4, 4, -3 }, { 3, 4, -3 }, { 3, 5, -2 }, { 3, 5, -1 }, { 3, 5, 0 }, { 3, 5, 1 },
					{ 3, 5, 2 }, { 4, 5, 2 }, { 5, 5, 2 }, { 6, 5, 2 }, { 7, 5, 2 }, { 7, 5, 1 }, { 7, 5, 0 },
					{ 7, 5, -1 }, { 7, 5, -2 }, { 6, 5, -2 }, { 5, 5, -2 }, { 4, 5, -2 }, { 3, 5, -2 }, { 4, 6, -1 },
					{ 4, 6, 0 }, { 4, 6, 1 }, { 5, 6, 1 }, { 6, 6, 1 }, { 6, 6, 0 }, { 6, 6, -1 }, { 5, 6, -1 },
					{ 4, 7, 0 }, { 5, 7, 1 }, { 6, 7, 0 }, { 5, 7, -1 } });
			bp.addBarrierCoords(new int[][] { { 5, 8, 0 } });
			break;
		case GIANT:
			bp.addWallCoords(new int[][] {
					// layer 1
					{ 0, 0, -5 }, { 0, 0, -4 }, { 0, 0, -3 }, { 0, 0, -2 }, { 0, 0, -1 }, { 0, 0, 0 }, { 0, 0, 1 },
					{ 0, 0, 2 }, { 0, 0, 3 }, { 0, 0, 4 }, { 0, 0, 5 }, { 1, 0, 6 }, { 2, 0, 6 }, { 3, 0, 6 },
					{ 4, 0, 6 }, { 5, 0, 6 }, { 6, 0, 6 }, { 7, 0, 6 }, { 8, 0, 6 }, { 9, 0, 6 }, { 10, 0, 6 },
					{ 11, 0, 6 }, { 12, 0, 5 }, { 12, 0, 4 }, { 12, 0, 3 }, { 12, 0, 2 }, { 12, 0, 1 }, { 12, 0, 0 },
					{ 12, 0, -1 }, { 12, 0, -2 }, { 12, 0, -3 }, { 12, 0, -4 }, { 12, 0, -5 }, { 11, 0, -6 },
					{ 10, 0, -6 }, { 9, 0, -6 }, { 8, 0, -6 }, { 7, 0, -6 }, { 6, 0, -6 }, { 5, 0, -6 }, { 4, 0, -6 },
					{ 3, 0, -6 }, { 2, 0, -6 }, { 1, 0, -6 },
					// layer 2
					{ 0, 1, -5 }, { 0, 1, -4 }, { 0, 1, -3 }, { 0, 1, -2 }, { 0, 1, -1 }, { 0, 1, 0 }, { 0, 1, 1 },
					{ 0, 1, 2 }, { 0, 1, 3 }, { 0, 1, 4 }, { 0, 1, 5 }, { 1, 1, 6 }, { 2, 1, 6 }, { 3, 1, 6 },
					{ 4, 1, 6 }, { 5, 1, 6 }, { 6, 1, 6 }, { 7, 1, 6 }, { 8, 1, 6 }, { 9, 1, 6 }, { 10, 1, 6 },
					{ 11, 1, 6 }, { 12, 1, 5 }, { 12, 1, 4 }, { 12, 1, 3 }, { 12, 1, 2 }, { 12, 1, 1 }, { 12, 1, 0 },
					{ 12, 1, -1 }, { 12, 1, -2 }, { 12, 1, -3 }, { 12, 1, -4 }, { 12, 1, -5 }, { 11, 1, -6 },
					{ 10, 1, -6 }, { 9, 1, -6 }, { 8, 1, -6 }, { 7, 1, -6 }, { 6, 1, -6 }, { 5, 1, -6 }, { 4, 1, -6 },
					{ 3, 1, -6 }, { 2, 1, -6 }, { 1, 1, -6 },
					// layer 3
					{ 0, 2, -5 }, { 0, 2, -4 }, { 0, 2, -3 }, { 0, 2, -2 }, { 0, 2, -1 }, { 0, 2, 0 }, { 0, 2, 1 },
					{ 0, 2, 2 }, { 0, 2, 3 }, { 0, 2, 4 }, { 0, 2, 5 }, { 1, 2, 6 }, { 2, 2, 6 }, { 3, 2, 6 },
					{ 4, 2, 6 }, { 5, 2, 6 }, { 6, 2, 6 }, { 7, 2, 6 }, { 8, 2, 6 }, { 9, 2, 6 }, { 10, 2, 6 },
					{ 11, 2, 6 }, { 12, 2, 5 }, { 12, 2, 4 }, { 12, 2, 3 }, { 12, 2, 2 }, { 12, 2, 1 }, { 12, 2, 0 },
					{ 12, 2, -1 }, { 12, 2, -2 }, { 12, 2, -3 }, { 12, 2, -4 }, { 12, 2, -5 }, { 11, 2, -6 },
					{ 10, 2, -6 }, { 9, 2, -6 }, { 8, 2, -6 }, { 7, 2, -6 }, { 6, 2, -6 }, { 5, 2, -6 }, { 4, 2, -6 },
					{ 3, 2, -6 }, { 2, 2, -6 }, { 1, 2, -6 } });
			bp.addRoofCoords(new int[][] {
					// layer 4
					{ 1, 3, -5 }, { 1, 3, -4 }, { 1, 3, -3 }, { 1, 3, -2 }, { 1, 3, -1 }, { 1, 3, 0 }, { 1, 3, 1 },
					{ 1, 3, 2 }, { 1, 3, 3 }, { 1, 3, 4 }, { 1, 3, 5 }, { 2, 3, 5 }, { 3, 3, 5 }, { 4, 3, 5 },
					{ 5, 3, 5 }, { 6, 3, 5 }, { 7, 3, 5 }, { 8, 3, 5 }, { 9, 3, 5 }, { 10, 3, 5 }, { 11, 3, 5 },
					{ 11, 3, 4 }, { 11, 3, 3 }, { 11, 3, 2 }, { 11, 3, 1 }, { 11, 3, 0 }, { 11, 3, -1 }, { 11, 3, -2 },
					{ 11, 3, -3 }, { 11, 3, -4 }, { 11, 3, -5 }, { 10, 3, -5 }, { 9, 3, -5 }, { 8, 3, -5 },
					{ 7, 3, -5 }, { 6, 3, -5 }, { 5, 3, -5 }, { 4, 3, -5 }, { 3, 3, -5 }, { 2, 3, -5 },
					// layer 5 - 8
					{ 2, 4, -4 }, { 2, 4, -3 }, { 2, 4, -2 }, { 2, 4, -1 }, { 2, 4, 0 }, { 2, 4, 1 }, { 2, 4, 2 },
					{ 2, 4, 3 }, { 2, 4, 4 }, { 3, 4, 4 }, { 4, 4, 4 }, { 5, 4, 4 }, { 6, 4, 4 }, { 7, 4, 4 },
					{ 8, 4, 4 }, { 9, 4, 4 }, { 10, 4, 4 }, { 10, 4, 3 }, { 10, 4, 2 }, { 10, 4, 1 }, { 10, 4, 0 },
					{ 10, 4, -1 }, { 10, 4, -2 }, { 10, 4, -3 }, { 10, 4, -4 }, { 9, 4, -4 }, { 8, 4, -4 },
					{ 7, 4, -4 }, { 6, 4, -4 }, { 5, 4, -4 }, { 4, 4, -4 }, { 3, 4, -4 }, { 3, 5, -3 }, { 3, 5, -2 },
					{ 3, 5, -1 }, { 3, 5, 0 }, { 3, 5, 1 }, { 3, 5, 2 }, { 3, 5, 3 }, { 4, 5, 3 }, { 5, 5, 3 },
					{ 6, 5, 3 }, { 7, 5, 3 }, { 8, 5, 3 }, { 9, 5, 3 }, { 9, 5, 2 }, { 9, 5, 1 }, { 9, 5, 0 },
					{ 9, 5, -1 }, { 9, 5, -2 }, { 9, 5, -3 }, { 8, 5, -3 }, { 7, 5, -3 }, { 6, 5, -3 }, { 5, 5, -3 },
					{ 4, 5, -3 }, { 4, 6, -2 }, { 4, 6, -1 }, { 4, 6, 0 }, { 4, 6, 1 }, { 4, 6, 2 }, { 5, 6, 2 },
					{ 6, 6, 2 }, { 7, 6, 2 }, { 8, 6, 2 }, { 8, 6, 1 }, { 8, 6, 0 }, { 8, 6, -1 }, { 8, 6, -2 },
					{ 7, 6, -2 }, { 6, 6, -2 }, { 5, 6, -2 }, { 4, 6, -2 }, { 5, 7, -1 }, { 5, 7, 0 }, { 5, 7, 1 },
					{ 6, 7, 1 }, { 7, 7, 1 }, { 7, 7, 0 }, { 7, 7, -1 }, { 6, 7, -1 }, { 5, 8, 0 }, { 6, 8, 1 },
					{ 7, 8, 0 }, { 6, 8, -1 } });
			bp.addBarrierCoords(new int[][] { { 6, 9, 0 } });
			break;
		case MEGA:
			bp.addWallCoords(new int[][] {
					// layer 1
					{ 0, 0, -6 }, { 0, 0, -5 }, { 0, 0, -4 }, { 0, 0, -3 }, { 0, 0, -2 }, { 0, 0, -1 }, { 0, 0, 0 }, { 0, 0, 1 }, { 0, 0, 2 },
					{ 0, 0, 3 }, { 0, 0, 4 }, { 0, 0, 5 }, { 0, 0, 6 },
					{ 1, 0, 7 }, { 2, 0, 7 }, { 3, 0, 7 }, { 4, 0, 7 }, { 5, 0, 7 }, { 6, 0, 7 },
					{ 7, 0, 7 }, { 8, 0, 7 }, { 9, 0, 7 }, { 10, 0, 7 }, { 11, 0, 7 }, { 12, 0, 7 }, { 13, 0, 7 }, 
					{ 14, 0, 6 }, { 14, 0, 5 }, { 14, 0, 4 },
					{ 14, 0, 3 }, { 14, 0, 2 }, { 14, 0, 1 }, { 14, 0, 0 }, { 14, 0, -1 }, { 14, 0, -2 }, { 14, 0, -3 },
					{ 14, 0, -4 }, { 14, 0, -5 }, { 14, 0, -6 }, { 13, 0, -7 }, { 12, 0, -7 }, { 11, 0, -7 }, { 10, 0, -7 }, { 9, 0, -7 }, { 8, 0, -7 },
					{ 7, 0, -7 }, { 6, 0, -7 }, { 5, 0, -7 }, { 4, 0, -7 }, { 3, 0, -7 }, { 2, 0, -7 }, { 1, 0, -7 },					
					// layer 2
					{ 0, 1, -6 }, { 0, 1, -5 }, { 0, 1, -4 }, { 0, 1, -3 }, { 0, 1, -2 }, { 0, 1, -1 }, { 0, 1, 0 }, { 0, 1, 1 }, { 0, 1, 2 },
					{ 0, 1, 3 }, { 0, 1, 4 }, { 0, 1, 5 }, { 0, 1, 6 },
					{ 1, 1, 7 }, { 2, 1, 7 }, { 3, 1, 7 }, { 4, 1, 7 }, { 5, 1, 7 }, { 6, 1, 7 },
					{ 7, 1, 7 }, { 8, 1, 7 }, { 9, 1, 7 }, { 10, 1, 7 }, { 11, 1, 7 }, { 12, 1, 7 }, { 13, 1, 7 }, 
					{ 14, 1, 6 }, { 14, 1, 5 }, { 14, 1, 4 },
					{ 14, 1, 3 }, { 14, 1, 2 }, { 14, 1, 1 }, { 14, 1, 0 }, { 14, 1, -1 }, { 14, 1, -2 }, { 14, 1, -3 },
					{ 14, 1, -4 }, { 14, 1, -5 }, { 14, 1, -6 }, { 13, 1, -7 }, { 12, 1, -7 }, { 11, 1, -7 }, { 10, 1, -7 }, { 9, 1, -7 }, { 8, 1, -7 },
					{ 7, 1, -7 }, { 6, 1, -7 }, { 5, 1, -7 }, { 4, 1, -7 }, { 3, 1, -7 }, { 2, 1, -7 }, { 1, 1, -7 },
					// layer 3
					{ 0, 2, -6 }, { 0, 2, -5 }, { 0, 2, -4 }, { 0, 2, -3 }, { 0, 2, -2 }, { 0, 2, -1 }, { 0, 2, 0 }, { 0, 2, 1 }, { 0, 2, 2 },
					{ 0, 2, 3 }, { 0, 2, 4 }, { 0, 2, 5 }, { 0, 2, 6 },
					{ 1, 2, 7 }, { 2, 2, 7 }, { 3, 2, 7 }, { 4, 2, 7 }, { 5, 2, 7 }, { 6, 2, 7 },
					{ 7, 2, 7 }, { 8, 2, 7 }, { 9, 2, 7 }, { 10, 2, 7 }, { 11, 2, 7 }, { 12, 2, 7 }, { 13, 2, 7 }, 
					{ 14, 2, 6 }, { 14, 2, 5 }, { 14, 2, 4 },
					{ 14, 2, 3 }, { 14, 2, 2 }, { 14, 2, 1 }, { 14, 2, 0 }, { 14, 2, -1 }, { 14, 2, -2 }, { 14, 2, -3 },
					{ 14, 2, -4 }, { 14, 2, -5 }, { 14, 2, -6 }, { 13, 2, -7 }, { 12, 2, -7 }, { 11, 2, -7 }, { 10, 2, -7 }, { 9, 2, -7 }, { 8, 2, -7 },
					{ 7, 2, -7 }, { 6, 2, -7 }, { 5, 2, -7 }, { 4, 2, -7 }, { 3, 2, -7 }, { 2, 2, -7 }, { 1, 2, -7 }
			});
			bp.addRoofCoords(new int[][] {
					// layer 4
					{ 1, 3, -6 }, { 1, 3, -5 }, { 1, 3, -4 }, { 1, 3, -3 }, { 1, 3, -2 }, { 1, 3, -1 }, { 1, 3, 0 },
					{ 1, 3, 1 }, { 1, 3, 2 }, { 1, 3, 3 }, { 1, 3, 4 }, { 1, 3, 5 }, { 1, 3, 6 }, { 2, 3, 6 },
					{ 3, 3, 6 }, { 4, 3, 6 }, { 5, 3, 6 }, { 6, 3, 6 }, { 7, 3, 6 }, { 8, 3, 6 }, { 9, 3, 6 },
					{ 10, 3, 6 }, { 11, 3, 6 }, { 12, 3, 6 }, { 13, 3, 6 },
					{ 13, 3, 5 }, { 13, 3, 4 }, { 13, 3, 3 }, { 13, 3, 2 },
					{ 13, 3, 1 }, { 13, 3, 0 }, { 13, 3, -1 }, { 13, 3, -2 }, { 13, 3, -3 }, { 13, 3, -4 },
					{ 13, 3, -5 }, { 13, 3, -6 }, { 12, 3, -6 }, { 11, 3, -6 }, { 10, 3, -6 }, { 9, 3, -6 }, { 8, 3, -6 },
					{ 7, 3, -6 }, { 6, 3, -6 }, { 5, 3, -6 }, { 4, 3, -6 }, { 3, 3, -6 }, { 2, 3, -6 },
					// layer 5 - 9
					{ 2, 4, -5 }, { 2, 4, -4 }, { 2, 4, -3 }, { 2, 4, -2 }, { 2, 4, -1 }, { 2, 4, 0 }, { 2, 4, 1 },
					{ 2, 4, 2 }, { 2, 4, 3 }, { 2, 4, 4 }, { 2, 4, 5 }, { 3, 4, 5 }, { 4, 4, 5 }, { 5, 4, 5 },
					{ 6, 4, 5 }, { 7, 4, 5 }, { 8, 4, 5 }, { 9, 4, 5 }, { 10, 4, 5 }, { 11, 4, 5 }, { 12, 4, 5 },
					{ 12, 4, 4 }, { 12, 4, 3 }, { 12, 4, 2 }, { 12, 4, 1 }, { 12, 4, 0 }, { 12, 4, -1 }, { 12, 4, -2 },
					{ 12, 4, -3 }, { 12, 4, -4 }, { 12, 4, -5 }, { 11, 4, -5 }, { 10, 4, -5 }, { 9, 4, -5 },
					{ 8, 4, -5 }, { 7, 4, -5 }, { 6, 4, -5 }, { 5, 4, -5 }, { 4, 4, -5 }, { 3, 4, -5 }, { 3, 5, -4 },
					{ 3, 5, -3 }, { 3, 5, -2 }, { 3, 5, -1 }, { 3, 5, 0 }, { 3, 5, 1 }, { 3, 5, 2 }, { 3, 5, 3 },
					{ 3, 5, 4 }, { 4, 5, 4 }, { 5, 5, 4 }, { 6, 5, 4 }, { 7, 5, 4 }, { 8, 5, 4 }, { 9, 5, 4 },
					{ 10, 5, 4 }, { 11, 5, 4 }, { 11, 5, 3 }, { 11, 5, 2 }, { 11, 5, 1 }, { 11, 5, 0 }, { 11, 5, -1 },
					{ 11, 5, -2 }, { 11, 5, -3 }, { 11, 5, -4 }, { 10, 5, -4 }, { 9, 5, -4 }, { 8, 5, -4 },
					{ 7, 5, -4 }, { 6, 5, -4 }, { 5, 5, -4 }, { 4, 5, -4 }, { 4, 6, -3 }, { 4, 6, -2 }, { 4, 6, -1 },
					{ 4, 6, 0 }, { 4, 6, 1 }, { 4, 6, 2 }, { 4, 6, 3 }, { 5, 6, 3 }, { 6, 6, 3 }, { 7, 6, 3 },
					{ 8, 6, 3 }, { 9, 6, 3 }, { 10, 6, 3 }, { 10, 6, 2 }, { 10, 6, 1 }, { 10, 6, 0 }, { 10, 6, -1 },
					{ 10, 6, -2 }, { 10, 6, -3 }, { 9, 6, -3 }, { 8, 6, -3 }, { 7, 6, -3 }, { 6, 6, -3 }, { 5, 6, -3 },
					{ 5, 7, -2 }, { 5, 7, -1 }, { 5, 7, 0 }, { 5, 7, 1 }, { 5, 7, 2 }, { 6, 7, 2 }, { 7, 7, 2 },
					{ 8, 7, 2 }, { 9, 7, 2 }, { 9, 7, 1 }, { 9, 7, 0 }, { 9, 7, -1 }, { 9, 7, -2 }, { 8, 7, -2 },
					{ 7, 7, -2 }, { 6, 7, -2 }, { 5, 7, -2 }, { 6, 8, -1 }, { 6, 8, 0 }, { 6, 8, 1 }, { 7, 8, 1 },
					{ 8, 8, 1 }, { 8, 8, 0 }, { 8, 8, -1 }, { 7, 8, -1 }, { 6, 9, 0 }, { 7, 9, 1 }, { 8, 9, 0 },
					{ 7, 9, -1 } 
			});
			bp.addBarrierCoords(new int[][] { { 7, 10, 0 } });
			break;
		}
		return bp;
	}
}
