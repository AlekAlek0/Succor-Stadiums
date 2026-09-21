package net.alek.succorstadiums.mana;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import com.mojang.serialization.Codec;
import net.minecraft.util.Mth;

// ManaData class
public final class ManaData {
    private static final int DEFAULT_MAX_MANA = 10;

    private static final int REGEN_DELAY_TICKS = 40;
    private static final int REGEN_DELAY_TICKS_SICK = 60;
    private static final int REGEN_INTERVAL_TICKS = 10;
    private static final int REGEN_INTERVAL_TICKS_SICK = 20;
    private static final int REGEN_AMOUNT = 1;

    public static final Codec<ManaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("mana").forGetter(ManaData::getMana),
                    Codec.INT.fieldOf("maxMana").forGetter(ManaData::getMaxMana),
                    Codec.INT.fieldOf("ticksSinceLastUse").forGetter(ManaData::getTicksSinceLastUse),
                    Codec.INT.fieldOf("regenTickCounter").forGetter(ManaData::getRegenTickCounter)
            ).apply(instance, ManaData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ManaData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ManaData::getMana,
                    ByteBufCodecs.VAR_INT, ManaData::getMaxMana,
                    ByteBufCodecs.VAR_INT, ManaData::getTicksSinceLastUse,
                    ByteBufCodecs.VAR_INT, ManaData::getRegenTickCounter,
                    ManaData::new
            );

    private final int mana;
    private final int maxMana;
    private final int ticksSinceLastUse;
    private final int regenTickCounter;

    public ManaData() {
        this(DEFAULT_MAX_MANA, DEFAULT_MAX_MANA, REGEN_DELAY_TICKS, 0);
    }

    public ManaData(int mana, int maxMana, int ticksSinceLastUse, int regenTickCounter) {
        this.maxMana = maxMana;
        this.mana = Mth.clamp(mana, 0, maxMana);
        this.ticksSinceLastUse = ticksSinceLastUse;
        this.regenTickCounter = regenTickCounter;
    }

    public int getMana() {
        return this.mana;
    }

    public int getMaxMana() {
        return this.maxMana;
    }

    public int getTicksSinceLastUse() {
        return this.ticksSinceLastUse;
    }

    public int getRegenTickCounter() {
        return this.regenTickCounter;
    }

    public ManaData withManaConsumed(int amount) {
        if (this.mana < amount) {
            return this;
        }
        return new ManaData(this.mana - amount, this.maxMana, 0, 0);
    }

    public ManaData withManaAdded(int amount) {
        int newMana = Math.min(this.mana + amount, this.maxMana);
        if (newMana == this.mana) {
            return this;
        }
        return new ManaData(newMana, this.maxMana, this.ticksSinceLastUse, this.regenTickCounter);
    }

    public boolean hasEnoughMana(int amount) {
        return this.mana >= amount;
    }

    /**
     * @param sick whether Mana Sickness is active: delay 40->60 ticks, interval 10->20 ticks
     */
    public ManaData ticked(boolean sick) {
        if (this.mana >= this.maxMana) {
            return this;
        }

        int delay = sick ? REGEN_DELAY_TICKS_SICK : REGEN_DELAY_TICKS;
        int interval = sick ? REGEN_INTERVAL_TICKS_SICK : REGEN_INTERVAL_TICKS;

        if (this.ticksSinceLastUse < delay) {
            return new ManaData(this.mana, this.maxMana, this.ticksSinceLastUse + 1, this.regenTickCounter);
        }

        int newCounter = this.regenTickCounter + 1;
        if (newCounter >= interval) {
            int newMana = Math.min(this.mana + REGEN_AMOUNT, this.maxMana);
            return new ManaData(newMana, this.maxMana, this.ticksSinceLastUse, 0);
        }

        return new ManaData(this.mana, this.maxMana, this.ticksSinceLastUse, newCounter);
    }
}