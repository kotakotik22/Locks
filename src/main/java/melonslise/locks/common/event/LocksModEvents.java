package melonslise.locks.common.event;

import melonslise.locks.Locks;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksNetwork;
import melonslise.locks.common.init.LocksPlacementModifiers;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Locks.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class LocksModEvents
{
	@SubscribeEvent
	public static void onSetup(FMLCommonSetupEvent e)
	{
		LocksNetwork.register();
	}

	@SubscribeEvent
	public static void onConfigLoad(ModConfigEvent.Loading e) {
		if (e.getConfig().getSpec() == LocksConfig.SPEC)
			LocksConfig.init();
		if (e.getConfig().getSpec() == LocksServerConfig.SPEC)
			LocksServerConfig.init();
	}

	/*
	 this is called while forge is registering items
	 during that time, the registry is unfrozen, and
	 we can register to raw vanilla registries
	*/
	@SubscribeEvent
	public static void onRegistryUnfrozen(RegistryEvent.Register<Item> event) {
		LocksPlacementModifiers.INSTANCE = new LocksPlacementModifiers();
	}
}