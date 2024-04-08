package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Locks.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class LocksCapabilities {

	public static final Capability<ILockableHandler> LOCKABLE_HANDLER = CapabilityManager.get(new CapabilityToken<>() {
	});

	public static final Capability<ILockableStorage> LOCKABLE_STORAGE = CapabilityManager.get(new CapabilityToken<>() {
	});

	public static final Capability<ISelection> SELECTION = CapabilityManager.get(new CapabilityToken<>() {
	});

	@SubscribeEvent
	public static void register(RegisterCapabilitiesEvent event) {
		event.register(ILockableHandler.class);
		event.register(ILockableStorage.class);
		event.register(ISelection.class);
	}


	private LocksCapabilities() {
	}

	public static void attachToWorld(AttachCapabilitiesEvent<Level> e) {
		e.addCapability(LockableHandler.ID, new SerializableCapabilityProvider<>(LOCKABLE_HANDLER, new LockableHandler(e.getObject())));
	}

	public static void attachToChunk(AttachCapabilitiesEvent<LevelChunk> e) {
		e.addCapability(LockableStorage.ID, new SerializableCapabilityProvider<>(LOCKABLE_STORAGE, new LockableStorage(e.getObject())));
	}

	public static void attachToEntity(AttachCapabilitiesEvent<Entity> e) {
		if (e.getObject() instanceof Player)
			e.addCapability(Selection.ID, new CapabilityProvider<>(SELECTION, new Selection()));
	}
}