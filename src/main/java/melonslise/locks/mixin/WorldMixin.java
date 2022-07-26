package melonslise.locks.mixin;

import melonslise.locks.common.util.LocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class WorldMixin
{
	@Inject(at = @At("HEAD"), method = "hasNeighborSignal", cancellable = true)
	private void hasNeighborSignal(BlockPos pos, CallbackInfoReturnable<Boolean> cir)
	{
		if (LocksUtil.locked((Level) (Object) this, pos))
			cir.setReturnValue(false);
	}
}