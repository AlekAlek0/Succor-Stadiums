package net.alek.succorstadiums.item.equipment.pouch;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;

import java.util.List;

// PouchContents class
public final class PouchContents {
    public static final int SLOT_COUNT = 27;
    public static final int MAX_STACK_SIZE = 99;

    public static final PouchContents EMPTY = new PouchContents(NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY));

    public static final Codec<PouchContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items").forGetter(PouchContents::asList)
            ).apply(instance, PouchContents::fromList)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PouchContents> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(NonNullList::createWithCapacity, ItemStack.OPTIONAL_STREAM_CODEC),
            PouchContents::asList,
            PouchContents::fromList
    );

    private final NonNullList<ItemStack> items;

    private PouchContents(NonNullList<ItemStack> items) {
        this.items = items;
    }

    private static PouchContents fromList(List<ItemStack> list) {
        NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(SLOT_COUNT, list.size()); i++) {
            ItemStack stack = list.get(i);
            items.set(i, stack == null ? ItemStack.EMPTY : stack);
        }
        return new PouchContents(items);
    }

    public static PouchContents of(NonNullList<ItemStack> items) {
        return fromList(items);
    }

    public List<ItemStack> asList() {
        return this.items;
    }

    public NonNullList<ItemStack> copyItems() {
        NonNullList<ItemStack> copy = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < SLOT_COUNT; i++) {
            copy.set(i, this.items.get(i).copy());
        }
        return copy;
    }

    public int getTotalCount() {
        int total = 0;
        for (ItemStack stack : this.items) {
            total += stack.getCount();
        }
        return total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PouchContents other)) return false;
        if (this.items.size() != other.items.size()) return false;
        for (int i = 0; i < this.items.size(); i++) {
            if (!ItemStack.matches(this.items.get(i), other.items.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.items.hashCode();
    }
}