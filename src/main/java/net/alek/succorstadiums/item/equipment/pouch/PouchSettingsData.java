package net.alek.succorstadiums.item.equipment.pouch;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;

// PouchSettingsData class
public final class PouchSettingsData {

    private final Map<Identifier, Boolean> autoDepositByType;

    // Constructor
    public PouchSettingsData() {
        this(new HashMap<>());
    }

    // Private constructor which will copy the contents into a new hash map
    private PouchSettingsData(Map<Identifier, Boolean> autoDepositByType) {
        this.autoDepositByType = new HashMap<>(autoDepositByType);
    }

    // Codec for the auto deposit boolean. disk serialization
    public static final Codec<PouchSettingsData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.unboundedMap(Identifier.CODEC, Codec.BOOL)
                            .fieldOf("autoDeposit").forGetter(d -> d.autoDepositByType)
            ).apply(instance, PouchSettingsData::new)
    );

    // Stream codec for the auto deposit boolean. network serialization
    public static final StreamCodec<RegistryFriendlyByteBuf, PouchSettingsData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, ByteBufCodecs.BOOL),
            d -> d.autoDepositByType,
            PouchSettingsData::new
    );

    // Accessor method to check if auto deposit is enabled
    public boolean isAutoDepositEnabled(Identifier pouchTypeId) {
        return this.autoDepositByType.getOrDefault(pouchTypeId, true);
    }

    // Copy the current map, apply the change, and create a new hash map
    public PouchSettingsData withAutoDeposit(Identifier pouchTypeId, boolean enabled) {
        Map<Identifier, Boolean> copy = new HashMap<>(this.autoDepositByType);
        copy.put(pouchTypeId, enabled);
        return new PouchSettingsData(copy);
    }
}