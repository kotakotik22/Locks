package melonslise.locks.mixin;

import melonslise.locks.common.util.LocksUtil;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonStructureResolver.class)
public class PistonBlockStructureHelperMixin
{
	@Inject(at = @At("HEAD"), method = "resolve()Z", cancellable = true)
	private void resolve(CallbackInfoReturnable<Boolean> cir)
	{
		PistonStructureResolver h = (PistonStructureResolver) (Object) this;
		if(LocksUtil.locked(h.level, h.startPos))
			cir.setReturnValue(false);
	}
}