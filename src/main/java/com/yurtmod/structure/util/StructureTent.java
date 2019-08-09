package com.yurtmod.structure.util;

import com.yurtmod.block.BlockYurtRoof;
import com.yurtmod.block.Categories.IBedouinBlock;
import com.yurtmod.block.Categories.IIndluBlock;
import com.yurtmod.block.Categories.IShamianaBlock;
import com.yurtmod.block.Categories.ITentBlockBase;
import com.yurtmod.block.Categories.ITepeeBlock;
import com.yurtmod.block.Categories.IYurtBlock;
import com.yurtmod.dimension.TentDimension;
import com.yurtmod.init.Content;
import com.yurtmod.init.TentConfig;
import com.yurtmod.structure.StructureBase;
import com.yurtmod.structure.StructureBedouin;
import com.yurtmod.structure.StructureIndlu;
import com.yurtmod.structure.StructureShamiana;
import com.yurtmod.structure.StructureTepee;
import com.yurtmod.structure.StructureYurt;

import net.minecraft.block.BlockState;
import net.minecraft.util.IStringSerializable;

public enum StructureTent implements IStringSerializable {
	
	YURT(new StructureYurt()) {
		////////// YURT IMPLEMENTATIONS OF ABSTRACT METHODS //////////
		public boolean isEnabled() { return TentConfig.TENTS.ALLOW_YURT; }
		public int getMaxSize() { return TentConfig.TENTS.TIERS_YURT; }
		public Class<? extends ITentBlockBase> getInterface() {	return IYurtBlock.class; }
		public BlockState getRoofBlock(int dimID) { return Content.YURT_ROOF.getDefaultState().withProperty(BlockYurtRoof.OUTSIDE, !TentDimension.isTentDimension(dimID) ); }
		public BlockState getFrameBlock(boolean isRoof) { return isRoof ? Content.FRAME_YURT_ROOF.getDefaultState() : Content.FRAME_YURT_WALL.getDefaultState(); }
		public BlockState getWallBlock(int dimID) { 
			return TentDimension.isTentDimension(dimID) 
				? Content.YURT_WALL_INNER.getDefaultState() 
				: Content.YURT_WALL_OUTER.getDefaultState(); 
		}
	},
	TEPEE(new StructureTepee()) {
		////////// TEPEE IMPLEMENTATIONS OF ABSTRACT METHODS //////////
		public boolean isEnabled() { return TentConfig.TENTS.ALLOW_TEPEE; }
		public int getMaxSize() { return TentConfig.TENTS.TIERS_TEPEE; }
		public Class<? extends ITentBlockBase> getInterface() {	return ITepeeBlock.class; }
		public BlockState getWallBlock(int dimID) { return Content.TEPEE_WALL_BLANK.getDefaultState();	}
		public BlockState getRoofBlock(int dimID) { return Content.TEPEE_WALL_BLANK.getDefaultState(); }
		public BlockState getFrameBlock(boolean isRoof) { return Content.FRAME_TEPEE_WALL.getDefaultState(); }
	},
	BEDOUIN(new StructureBedouin()) {
		////////// BEDOUIN IMPLEMENTATIONS OF ABSTRACT METHODS //////////
		public boolean isEnabled() { return TentConfig.TENTS.ALLOW_BEDOUIN; }
		public int getMaxSize() { return TentConfig.TENTS.TIERS_BEDOUIN; }
		public Class<? extends ITentBlockBase> getInterface() {	return IBedouinBlock.class; }
		public BlockState getWallBlock(int dimID) { return Content.BEDOUIN_WALL.getDefaultState(); }
		public BlockState getRoofBlock(int dimID) { return Content.BEDOUIN_ROOF.getDefaultState(); }
		public BlockState getFrameBlock(boolean isRoof) { return isRoof ? Content.FRAME_BEDOUIN_ROOF.getDefaultState() : Content.FRAME_BEDOUIN_WALL.getDefaultState(); }
	},
	INDLU(new StructureIndlu()) {
		////////// INDLU IMPLEMENTATIONS OF ABSTRACT METHODS //////////
		public boolean isEnabled() { return TentConfig.TENTS.ALLOW_INDLU; }
		public int getMaxSize() { return TentConfig.TENTS.TIERS_INDLU; }
		public Class<? extends ITentBlockBase> getInterface() {	return IIndluBlock.class; }
		public BlockState getRoofBlock(int dimID) { return Content.INDLU_WALL_OUTER.getDefaultState(); }
		public BlockState getFrameBlock(boolean isRoof) { return Content.FRAME_INDLU_WALL.getDefaultState(); }
		public BlockState getWallBlock(int dimID) { 
			return TentDimension.isTentDimension(dimID) 
				? Content.INDLU_WALL_INNER.getDefaultState() 
				: Content.INDLU_WALL_OUTER.getDefaultState(); 
		}
	},
	SHAMIANA(new StructureShamiana()) {
		////////// SHAMIANA IMPLEMENTATIONS OF ABSTRACT METHODS //////////
		public boolean isEnabled() { return TentConfig.TENTS.ALLOW_SHAMIANA; }
		public int getMaxSize() { return TentConfig.TENTS.TIERS_SHAMIANA; }
		public Class<? extends ITentBlockBase> getInterface() {	return IShamianaBlock.class; }
		public BlockState getRoofBlock(int dimID) { return Content.SHAMIANA_WALL_WHITE.getDefaultState(); }
		public BlockState getWallBlock(int dimID) { return Content.SHAMIANA_WALL_WHITE.getDefaultState(); }
		public BlockState getFrameBlock(boolean isRoof) { 
			// TODO we might re-enable the roof
			return isRoof ? Content.FRAME_SHAMIANA_WALL.getDefaultState() 
					: Content.FRAME_SHAMIANA_WALL.getDefaultState(); 
		}
	};
	
	private final StructureBase structure;
	
	StructureTent(final StructureBase struct) {
		this.structure = struct;
	}
	
	/** @return a StructureBase that will use the given StructureData **/
	public StructureBase makeStructure(final StructureData data) {
		return this.structure.withData(data);
	}
	
	/** @return A unique identifier. For now just the ordinal value **/
	public byte getId() {
		return (byte)this.ordinal();
	}
	
	/** @return The StructureTent that uses this ID **/
	public static StructureTent getById(final byte id) {
		return values()[id];
	}
	
	/** @return the corresponding StructureTent, or YURT for invalid name **/
	public static StructureTent getByName(final String name) {
		for(final StructureTent t : values()) {
			if(name.equals(t.getName())) {
				return t;
			}
		}
		return YURT;
	}

	@Override
	public String getName() {
		return this.toString().toLowerCase();
	}
	
	/////////// ABSTRACT METHODS ///////////
	
	/** @return whether this tent type is enabled in the config **/
	public abstract boolean isEnabled();

	/** @return the block interface expected by this structure type **/
	public abstract Class<? extends ITentBlockBase> getInterface();

	/** @return the main building block for this tent type. May be different inside tent. **/
	public abstract BlockState getWallBlock(int dimID);

	/** @return the specific Roof block for this tent type. May be different inside tent. **/
	public abstract BlockState getRoofBlock(int dimID);

	/** @return the specific Frame for this structure type. May be different between walls and roofs **/
	public abstract BlockState getFrameBlock(boolean isRoof);

	/** @return the maximum Width value for this tent type. (1 = SMALL, 6 = MEGA.) **/
	public abstract int getMaxSize();
}

