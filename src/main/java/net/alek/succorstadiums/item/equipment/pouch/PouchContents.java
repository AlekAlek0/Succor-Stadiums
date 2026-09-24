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

    // Initialize the slot count and max stack size of the container
    public static final int SLOT_COUNT = 27;
    public static final int MAX_STACK_SIZE = 99;

    // Initialize an empty instance that the pouch can fall back to if it's a new pouch
    public static final PouchContents EMPTY = new PouchContents(NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY));

    // Create a codec for the items of pouch contents to then store as nbt data. disk serialization
    public static final Codec<PouchContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items").forGetter(PouchContents::asList)
            ).apply(instance, PouchContents::fromList)
    );

    // Create a stream codec to use to send bytes over the network for the pouch contents. network serialization
    public static final StreamCodec<RegistryFriendlyByteBuf, PouchContents> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(NonNullList::createWithCapacity, ItemStack.OPTIONAL_STREAM_CODEC),
            PouchContents::asList,
            PouchContents::fromList
    );

    // Create a fixed sized list of 27 listing the items of the pouch contents
    private final NonNullList<ItemStack> items;
    private PouchContents(NonNullList<ItemStack> items) {
        this.items = items;
    }

    // Builds a fresh new list with a cap of slot count then copy into the list whatever was passed
    private static PouchContents fromList(List<ItemStack> list) {
        NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(SLOT_COUNT, list.size()); i++) {
            ItemStack stack = list.get(i);
            items.set(i, stack == null ? ItemStack.EMPTY : stack);
        }
        return new PouchContents(items);
    }

    // Public facing alias of the fromList constructor
    public static PouchContents of(NonNullList<ItemStack> items) {
        return fromList(items);
    }

    // Getter method to read the items in the field for both codec serializations
    public List<ItemStack> asList() {
        return this.items;
    }

    // Builds a fresh new list with each stack individually copied
    public NonNullList<ItemStack> copyItems() {
        NonNullList<ItemStack> copy = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < SLOT_COUNT; i++) {
            copy.set(i, this.items.get(i).copy());
        }
        return copy;
    }

    // Gets a total count of items in the pouch contents
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