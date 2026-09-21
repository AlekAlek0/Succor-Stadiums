package net.alek.succorstadiums.mana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public final class HypermanaData {
    private static final int REGEN_DELAY_TICKS = 10; // Half a second
    private static final int REGEN_INTERVAL_TICKS = 20; // 1 Second
    private static final int REGEN_AMOUNT = 2; // 1 Full Star

    public static final Codec<HypermanaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("hypermana").forGetter(HypermanaData::getHypermana),
                    Codec.INT.fieldOf("maxHypermana").forGetter(HypermanaData::getMaxHypermana),
                    Codec.INT.fieldOf("ticksSinceLastUse").forGetter(HypermanaData::getTicksSinceLastUse),
                    Codec.INT.fieldOf("regenTickCounter").forGetter(HypermanaData::getRegenTickCounter)
            ).apply(instance, HypermanaData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, HypermanaData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, HypermanaData::getHypermana,
                    ByteBufCodecs.VAR_INT, HypermanaData::getMaxHypermana,
                    ByteBufCodecs.VAR_INT, HypermanaData::getTicksSinceLastUse,
                    ByteBufCodecs.VAR_INT, HypermanaData::getRegenTickCounter,
                    HypermanaData::new
            );

    private final int hypermana;
    private final int maxHypermana;
    private final int ticksSinceLastUse;
    private final int regenTickCounter;

    public HypermanaData() {
        this(0, 0, REGEN_DELAY_TICKS, 0);
    }

    public HypermanaData(int hypermana, int maxHypermana, int ticksSinceLastUse, int regenTickCounter) {
        this.maxHypermana = maxHypermana;
        this.hypermana = Mth.clamp(hypermana, 0, maxHypermana);
        this.ticksSinceLastUse = ticksSinceLastUse;
        this.regenTickCounter = regenTickCounter;
    }

    public int getHypermana() { return this.hypermana; }
    public int getMaxHypermana() { return this.maxHypermana; }
    public int getTicksSinceLastUse() { return this.ticksSinceLastUse; }
    public int getRegenTickCounter() { return this.regenTickCounter; }

    public HypermanaData withHypermanaConsumed(int amount) {
        if (this.hypermana < amount) {
            return this;
        }
        return new HypermanaData(this.hypermana - amount, this.maxHypermana, 0, 0);
    }

    public HypermanaData withHypermanaConsumedClamped(int amount) {
        int newAmount = Math.max(0, this.hypermana - amount);
        if (newAmount == this.hypermana) {
            return this;
        }
        return new HypermanaData(newAmount, this.maxHypermana, 0, 0);
    }

    public boolean hasEnoughHypermana(int amount) {
        return this.hypermana >= amount;
    }

    public HypermanaData withHypermanaGranted(int amount) {
        int newMax = Math.max(this.maxHypermana, this.hypermana + amount);
        return new HypermanaData(this.hypermana + amount, newMax, this.ticksSinceLastUse, this.regenTickCounter);
    }

    public HypermanaData withHypermanaAdded(int amount) {
        int newAmount = Math.min(this.hypermana + amount, this.maxHypermana);
        if (newAmount == this.hypermana) {
            return this;
        }
        return new HypermanaData(newAmount, this.maxHypermana, this.ticksSinceLastUse, this.regenTickCounter);
    }

    public HypermanaData ticked() {
        if (this.hypermana <= 0) {
            return this;
        }

        if (this.hypermana >= this.maxHypermana) {
            return this;
        }

        if (this.ticksSinceLastUse < REGEN_DELAY_TICKS) {
            return new HypermanaData(this.hypermana, this.maxHypermana, this.ticksSinceLastUse + 1, this.regenTickCounter);
        }

        int newCounter = this.regenTickCounter + 1;
        if (newCounter >= REGEN_INTERVAL_TICKS) {
            int newAmount = Math.min(this.hypermana + REGEN_AMOUNT, this.maxHypermana);
            return new HypermanaData(newAmount, this.maxHypermana, this.ticksSinceLastUse, 0);
        }

        return new HypermanaData(this.hypermana, this.maxHypermana, this.ticksSinceLastUse, newCounter);
    }
}