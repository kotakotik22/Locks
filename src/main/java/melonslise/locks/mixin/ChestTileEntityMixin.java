package melonslise.locks.mixin;

import melonslise.locks.common.util.LocksUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlockEntity.class)
public class ChestTileEntityMixin
{
	@Inject(at = @At("HEAD"), method = "getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;", cancellable = true, remap = false)
	private void getCapability(Capability cap, Direction side, CallbackInfoReturnable<LazyOptional> cir)
	{
		BlockEntity te = (BlockEntity) (Object) this;
		if(!te.isRemoved() && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && te.hasLevel() && LocksUtil.locked(te.getLevel(), te.getBlockPos()))
			cir.setReturnValue(LazyOptional.empty());
	}
}