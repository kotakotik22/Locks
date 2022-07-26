package melonslise.locks.common.init;

import com.mojang.serialization.Codec;
import melonslise.locks.Locks;
import melonslise.locks.common.worldgen.RightChestFilter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber
public class LocksPlacementModifiers {
    public static @Nullable LocksPlacementModifiers INSTANCE;

    public PlacementModifierType<RightChestFilter> RIGHT_CHEST_FILTER = add("right_chest_filter", RightChestFilter.CODEC);

    public <P extends PlacementModifier> PlacementModifierType<P> add(String name, Codec<P> codec) {
        return Registry.register(Registry.PLACEMENT_MODIFIERS, new ResourceLocation(Locks.ID, name), () -> codec);
    }
}
