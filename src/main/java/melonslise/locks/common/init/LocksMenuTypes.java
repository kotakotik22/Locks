package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.client.gui.KeyRingScreen;
import melonslise.locks.client.gui.LockPickingScreen;
import melonslise.locks.common.container.KeyRingContainer;
import melonslise.locks.common.container.LockPickingContainer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class LocksMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, Locks.ID);

    public static final RegistryObject<MenuType<LockPickingContainer>>
            LOCK_PICKING = add("lock_picking", new MenuType(LockPickingContainer.FACTORY));

    public static final RegistryObject<MenuType<KeyRingContainer>>
            KEY_RING = add("key_ring", new MenuType(KeyRingContainer.FACTORY));

    private LocksMenuTypes() {
    }

    public static void registerScreens() {
        MenuScreens.register(LOCK_PICKING.get(), LockPickingScreen::new);
        MenuScreens.register(KEY_RING.get(), KeyRingScreen::new);
    }

    public static void register() {
        MENU_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> add(String name, MenuType<T> type) {
        return MENU_TYPES.register(name, () -> type);
    }
}