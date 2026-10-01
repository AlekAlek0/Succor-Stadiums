package net.alek.succorstadiums.advancement.criterion.summon;

import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.serialization.Codec;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

// SummonItemUseCriterion class
public class SummonItemUseCriterion extends SimpleCriterionTrigger<SummonItemUseCriterion.Conditions> {

    @Override
    public @NonNull Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player) {
        trigger(player, Conditions::requirementsMet);
    }

    public record Conditions(Optional<ContextAwarePredicate> playerPredicate) implements SimpleCriterionTrigger.SimpleInstance {
        public static Codec<SummonItemUseCriterion.Conditions> CODEC = ContextAwarePredicate.CODEC.optionalFieldOf("player")
                .xmap(Conditions::new, Conditions::player).codec();

        @Override
        public @NonNull Optional<ContextAwarePredicate> player() {
            return this.playerPredicate;
        }

        public boolean requirementsMet() {
            return true;
        }
    }
}