package melonslise.locks.common.container;

import melonslise.locks.Locks;
import melonslise.locks.client.gui.LockPickingScreen;
import melonslise.locks.common.init.*;
import melonslise.locks.common.item.LockPickItem;
import melonslise.locks.common.network.toclient.TryPinResultPacket;
import melonslise.locks.common.util.Lockable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class LockPickingContainer extends AbstractContainerMenu {
	public static class HiddenSlot extends Slot {
		public HiddenSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}

		@OnlyIn(Dist.CLIENT)
		@Override
		public boolean isActive() {
			return false;
		}
	}

	public static final Component TITLE = new TranslatableComponent(Locks.ID + ".gui.lockpicking.title");

	public final Player player;
	public final InteractionHand hand;
	public final Lockable lockable;

	public final Vec3 pos;

	public final int shocking, sturdy, complexity;

	protected int currIndex = 0;

	public LockPickingContainer(int id, Player player, InteractionHand hand, Lockable lkb) {
		super(LocksMenuTypes.LOCK_PICKING.get(), id);
		this.player = player;
		this.hand = hand;
		this.lockable = lkb;

		Lockable.State state = lkb.getLockState(player.level);
		this.pos = state == null ? lkb.bb.center() : state.pos;

		this.shocking = EnchantmentHelper.getItemEnchantmentLevel(LocksEnchantments.SHOCKING.get(), this.lockable.stack);
		this.sturdy = EnchantmentHelper.getItemEnchantmentLevel(LocksEnchantments.STURDY.get(), this.lockable.stack);
		this.complexity = EnchantmentHelper.getItemEnchantmentLevel(LocksEnchantments.COMPLEXITY.get(), this.lockable.stack);

		// Syncs the player inventory

		var inventory = player.getInventory();
		for (int rows = 0; rows < 3; ++rows)
			for (int cols = 0; cols < 9; ++cols)
				this.addSlot(new HiddenSlot(inventory, cols + rows * 9 + 9, 0, 0));

		for (int slots = 0; slots < 9; ++slots)
			this.addSlot(new HiddenSlot(inventory, slots, 0, 0));
	}

	public boolean isValidPick(ItemStack stack)
	{
		return stack.is(LocksItemTags.LOCK_PICKS) && LockPickItem.canPick(stack, this.complexity);
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return this.lockable.lock.isLocked() && this.isValidPick(player.getItemInHand(this.hand));
	}

	public boolean isOpen()
	{
		return this.currIndex == this.lockable.lock.getLength();
	}

	protected void reset()
	{
		this.currIndex = 0;
	}

	// SERVER ONLY
	public void tryPin(int currPin)
	{
		if(this.isOpen())
			return;
		boolean correct = false;
		boolean reset = false;
		if(this.lockable.lock.checkPin(this.currIndex, currPin))
		{
			++this.currIndex;
			correct = true;
			this.player.level.playSound(null, this.pos.x, this.pos.y, this.pos.z, LocksSoundEvents.PIN_MATCH.get(), SoundSource.BLOCKS, 1f, 1f);
		}
		else
		{
			if(this.tryBreakPick(player, currPin))
			{
				reset = true;
				this.reset();
				if(this.shocking > 0)
				{
					this.player.hurt(LocksDamageSources.SHOCK, shocking * 1.5f);
					this.player.level.playSound(null, this.player.position().x, this.player.position().y, this.player.position().z, LocksSoundEvents.SHOCK.get(), SoundSource.BLOCKS, 1f, 1f);
				}
			}
			else
				this.player.level.playSound(null, this.pos.x, this.pos.y, this.pos.z, LocksSoundEvents.PIN_FAIL.get(), SoundSource.BLOCKS, 1f, 1f);
		}
		LocksNetwork.MAIN.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) this.player), new TryPinResultPacket(correct, reset));
	}

	@OnlyIn(Dist.CLIENT)
	public void handlePin(boolean correct, boolean reset)
	{
		Screen screen = Minecraft.getInstance().screen;
		if(screen instanceof LockPickingScreen)
			((LockPickingScreen) screen).handlePin(correct, reset);
		if(correct)
			++this.currIndex;
		if(reset)
			this.reset();
	}

	protected boolean tryBreakPick(Player player, int pin) {
		ItemStack pickStack = player.getItemInHand(this.hand);
		float sturdyModifier = this.sturdy == 0 ? 1f : 0.75f + this.sturdy * 0.5f;
		float ch = LockPickItem.getOrSetStrength(pickStack) / sturdyModifier;
		float ex = (1f - ch) * (1f - this.getBreakChanceMultiplier(pin));

		if (!pickStack.is(LocksItemTags.LOCK_PICKS) || player.level.random.nextFloat() < ex + ch)
			return false;
		this.player.broadcastBreakEvent(this.hand);
		pickStack.shrink(1);
		if (pickStack.isEmpty()) {
			var inventory = player.getInventory();
			for (int a = 0; a < inventory.getContainerSize(); ++a) {
				ItemStack stack = inventory.getItem(a);
				if (this.isValidPick(stack)) {
					player.setItemInHand(hand, stack);
					inventory.removeItemNoUpdate(a);
					break;
				}
			}
		}
		return true;
	}

	/*
	protected float getPinDifficulty(int index)
	{
		// Basically takes the the distance (how many pins away) between the clicked pin and the next correct pin, then divides that by the amount of pins left to click and then plugs it into a simple linear function -ax+a where a = 0.4
		// This way we get a higher chance to break the further away we were from the correct pin
		return -0.5f * ((float) (index - this.currIndex) / (this.lockable.lock.getLength() - this.currIndex - 1)) + 0.5f;
	}
	*/

	protected float getBreakChanceMultiplier(int pin)
	{
		return Math.abs(this.lockable.lock.getPin(this.currIndex) - pin) == 1 ? 0.33f : 1f;
	}

	@Override
	public void removed(@NotNull Player player) {
		super.removed(player);
		if (!this.isOpen() || !this.lockable.lock.isLocked())
			return;
		this.lockable.lock.setLocked(!this.lockable.lock.isLocked());
		this.player.level.playSound(player, this.pos.x, this.pos.y, this.pos.z, LocksSoundEvents.LOCK_OPEN.get(), SoundSource.BLOCKS, 1f, 1f);
	}

	public static final IContainerFactory<LockPickingContainer> FACTORY = (id, inv, buf) ->
	{
		return new LockPickingContainer(id, inv.player, buf.readEnum(InteractionHand.class), inv.player.level.getCapability(LocksCapabilities.LOCKABLE_HANDLER).orElse(null).getLoaded().get(buf.readInt()));
	};

	public static class Writer implements Consumer<FriendlyByteBuf> {
		public final InteractionHand hand;
		public final Lockable lockable;

		public Writer(InteractionHand hand, Lockable lkb) {
			this.hand = hand;
			this.lockable = lkb;
		}

		@Override
		public void accept(FriendlyByteBuf buf) {
			buf.writeEnum(this.hand);
			buf.writeInt(this.lockable.id);
		}
	}

	public static class Provider implements MenuProvider {
		public final InteractionHand hand;
		public final Lockable lockable;

		public Provider(InteractionHand hand, Lockable lkb) {
			this.hand = hand;
			this.lockable = lkb;
		}

		public @NotNull Component getDisplayName() {
			return TITLE;
		}

		@Nullable
		@Override
		public AbstractContainerMenu createMenu(int id, @NotNull Inventory pPlayerInventory, @NotNull Player player) {
			return new LockPickingContainer(id, player, this.hand, this.lockable);

		}
	}
}