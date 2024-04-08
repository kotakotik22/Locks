package melonslise.locks.client.init;

import melonslise.locks.common.init.LocksMenuTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class LocksContainers {
    private LocksContainers() {
    }

    public static void register() {
        LocksMenuTypes.registerScreens();
    }
}