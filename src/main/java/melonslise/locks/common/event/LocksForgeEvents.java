package melonslise.locks.common.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.Locks;
import melonslise.locks.common.capability.ILockableHandler;
import melonslise.locks.common.capability.ISelection;
import melonslise.locks.common.config.LocksClientConfig;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksItemTags;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.item.KeyRingItem;
import melonslise.locks.common.item.LockingItem;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksPredicates;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = Locks.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LocksForgeEvents {
    public static final Component LOCKED_MESSAGE = new TranslatableComponent(Locks.ID + ".status.locked");

    private LocksForgeEvents() {
    }

    @SubscribeEvent
    public static void attachCapabilitiesToWorld(AttachCapabilitiesEvent<Level> e) {
        LocksCapabilities.attachToWorld(e);
    }

    @SubscribeEvent
    public static void attachCapabilitiesToChunk(AttachCapabilitiesEvent<LevelChunk> e) {
        LocksCapabilities.attachToChunk(e);
    }

    @SubscribeEvent
    public static void attachCapabilitiesToEntity(AttachCapabilitiesEvent<Entity> e) {
        LocksCapabilities.attachToEntity(e);
    }

    // todo (kota): remove
//	@SubscribeEvent(priority = EventPriority.HIGH)
//	public static void onBiomeLoad(BiomeLoadingEvent e)
//	{
//		LocksConfiguredFeatures.addTo(e);
//	}

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent e) {
        // Only modify if it was a vanilla chest loot table
        ResourceLocation name = e.getName();
        if (!name.getNamespace().equals("minecraft") || !name.getPath().startsWith("chests"))
            return;
        // And only if there is a corresponding inject table...
        ResourceLocation injectLoc = new ResourceLocation(Locks.ID, "loot_tables/inject/" + name.getPath() + ".json");
        if (!LocksUtil.resourceManager.hasResource(injectLoc))
            return;
        // todo (kota): bring back
    }

    @SubscribeEvent
    public static void addVillagerTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.TOOLSMITH)
            return;
        Int2ObjectMap<List<ItemListing>> levels = e.getTrades();
        List<ItemListing> trades;
        trades = levels.get(1);
        trades.add(new VillagerTrades.ItemsForEmeralds(new ItemStack(LocksItems.WOOD_LOCK_PICK.get()), 1, 2, 16, 2, 0.05f));
        trades.add(new VillagerTrades.ItemsForEmeralds(new ItemStack(LocksItems.WOOD_LOCK_MECHANISM.get()), 2, 1, 12, 1, 0.2f));
        trades = levels.get(2);
        trades.add(new VillagerTrades.ItemsForEmeralds(new ItemStack(LocksItems.IRON_LOCK_PICK.get()), 2, 2, 16, 5, 0.05f));
        trades = levels.get(3);
        trades.add(new VillagerTrades.ItemsForEmeralds(new ItemStack(LocksItems.GOLD_LOCK_PICK.get()), 6, 2, 12, 20, 0.05f));
        trades.add(new VillagerTrades.ItemsForEmeralds(new ItemStack(LocksItems.IRON_LOCK_MECHANISM.get()), 5, 1, 8, 10, 0.2f));
        trades = levels.get(4);
        trades.add(new VillagerTrades.ItemsForEmeralds(new ItemStack(LocksItems.STEEL_LOCK_PICK.get()), 4, 2, 16, 20, 0.05f));
        trades = levels.get(5);
        trades.add(new VillagerTrades.ItemsForEmeralds(new ItemStack(LocksItems.DIAMOND_LOCK_PICK.get()), 8, 2, 12, 30, 0.05f));
        trades.add(new VillagerTrades.ItemsForEmeralds(new ItemStack(LocksItems.STEEL_LOCK_MECHANISM.get()), 8, 1, 8, 30, 0.2f));
    }

    @SubscribeEvent
    public static void addWandererTrades(WandererTradesEvent e) {
        List<ItemListing> trades;
        trades = e.getGenericTrades();
        trades.add(new VillagerTrades.ItemsForEmeralds(LocksItems.GOLD_LOCK_PICK.get(), 5, 2, 6, 1));
        trades.add(new VillagerTrades.ItemsForEmeralds(LocksItems.STEEL_LOCK_PICK.get(), 3, 2, 8, 1));
        trades.add(new VillagerTrades.EnchantedItemForEmeralds(LocksItems.STEEL_LOCK.get(), 16, 4, 1));
        trades = e.getRareTrades();
        trades.add(new VillagerTrades.ItemsForEmeralds(LocksItems.STEEL_LOCK_MECHANISM.get(), 6, 1, 4, 1));
        trades.add(new VillagerTrades.EnchantedItemForEmeralds(LocksItems.DIAMOND_LOCK.get(), 28, 4, 1));
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload e) {
        LevelChunk ch = (LevelChunk) e.getChunk();
        ILockableHandler handler = ch.getLevel().getCapability(LocksCapabilities.LOCKABLE_HANDLER).orElse(null);
        ch.getCapability(LocksCapabilities.LOCKABLE_STORAGE).orElse(null).get().values().forEach(lkb ->
        {
            handler.getLoaded().remove(lkb.id);
            lkb.deleteObserver(handler);
        });
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock e) {
        BlockPos pos = e.getPos();
        Level world = e.getWorld();
        Player player = e.getPlayer();
        ILockableHandler handler = world.getCapability(LocksCapabilities.LOCKABLE_HANDLER).orElse(null);
        Lockable[] intersect = handler.getInChunk(pos).values().stream().filter(lkb -> lkb.bb.intersects(pos)).toArray(Lockable[]::new);
        if (intersect.length == 0)
            return;
        if (e.getHand() != InteractionHand.MAIN_HAND) // FIXME Better way to prevent firing multiple times
        {
            e.setUseBlock(Event.Result.DENY);
            return;
        }
        ItemStack stack = e.getItemStack();
        Optional<Lockable> locked = Arrays.stream(intersect).filter(LocksPredicates.LOCKED).findFirst();
        if (locked.isPresent()) {
            Lockable lkb = locked.get();
            e.setUseBlock(Event.Result.DENY);
            Item item = stack.getItem();
            // FIXME erase this ugly ass hard coded shit from the face of the earth and make a proper way to do this (maybe mixin to where the right click event is fired from)
            if (!stack.is(LocksItemTags.LOCK_PICKS) && item != LocksItems.MASTER_KEY.get() && (!stack.is(LocksItemTags.KEYS) || LockingItem.getOrSetId(stack) != lkb.lock.id) && (item != LocksItems.KEY_RING.get() || !KeyRingItem.containsId(stack, lkb.lock.id))) {
                lkb.swing(20);
                world.playSound(player, pos, LocksSoundEvents.LOCK_RATTLE.get(), SoundSource.BLOCKS, 1f, 1f);
            }
            player.swing(InteractionHand.MAIN_HAND);
            if (world.isClientSide && LocksClientConfig.DEAF_MODE.get())
                player.displayClientMessage(LOCKED_MESSAGE, true);
            return;
        }
        if (LocksServerConfig.ALLOW_REMOVING_LOCKS.get() && player.isShiftKeyDown() && stack.isEmpty()) {
            Lockable[] match = Arrays.stream(intersect).filter(LocksPredicates.NOT_LOCKED).toArray(Lockable[]::new);
            if (match.length == 0)
                return;
            e.setUseBlock(Event.Result.DENY);
            world.playSound(player, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8f, 0.8f + world.random.nextFloat() * 0.4f);
            player.swing(InteractionHand.MAIN_HAND);
            if (!world.isClientSide)
                for (Lockable lkb : match) {
                    world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, lkb.stack));
                    handler.remove(lkb.id);
                }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != Phase.START)
            return;
        ISelection select = e.player.getCapability(LocksCapabilities.SELECTION).orElse(null);
        if (select.get() == null)
            return;
        for (ItemStack stack : e.player.getHandSlots())
            if (stack.is(LocksItemTags.LOCKS))
                return;
        select.set(null);
    }

    public static boolean canBreakLockable(Player player, BlockPos pos) {
        return !LocksServerConfig.PROTECT_LOCKABLES.get() || player.isCreative() || !LocksUtil.locked(player.level, pos);
    }

    @SubscribeEvent
    public static void onBlockBreaking(PlayerEvent.BreakSpeed e) {
        if (!canBreakLockable(e.getPlayer(), e.getPos()))
            e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent e) {
        if (!canBreakLockable(e.getPlayer(), e.getPos()))
            e.setCanceled(true);
    }
}