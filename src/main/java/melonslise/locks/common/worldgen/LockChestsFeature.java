package melonslise.locks.common.worldgen;

import com.mojang.serialization.Codec;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class LockChestsFeature extends Feature<NoneFeatureConfiguration> {
	public LockChestsFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> pContext) {
		var rng = pContext.random();
		if (!LocksConfig.canGen(rng))
			return false;
		var world = pContext.level();
		var pos = pContext.origin();
		BlockState state = world.getBlockState(pos);
		BlockPos pos1 = state.getValue(ChestBlock.TYPE) == ChestType.SINGLE ? pos : pos.relative(ChestBlock.getConnectedDirection(state));
		ItemStack stack = LocksConfig.getRandomLock(rng);
		Lockable lkb = new Lockable(new Cuboid6i(pos, pos1), Lock.from(stack), Transform.fromDirection(state.getValue(ChestBlock.FACING), Direction.NORTH), stack, world.getLevel());
		lkb.bb.getContainedChunks((x, z) ->
		{
			((ILockableProvider) world.getChunk(x, z)).getLockables().add(lkb);
			return true;
		});
		return true;
	}
}