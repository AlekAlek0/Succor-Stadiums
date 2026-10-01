package net.alek.succorstadiums.advancement.criterion;

import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.serialization.Codec;

import java.util.Optional;

// BackpackOpenCriterion class
public class BackpackOpenCriterion extends SimpleCriterionTrigger<BackpackOpenCriterion.Conditions> {

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player) {
        trigger(player, Conditions::requirementsMet);
    }

    public record Conditions(Optional<ContextAwarePredicate> playerPredicate) implements SimpleCriterionTrigger.SimpleInstance {
        public static Codec<BackpackOpenCriterion.Conditions> CODEC = ContextAwarePredicate.CODEC.optionalFieldOf("player")
                .xmap(Conditions::new, Conditions::player).codec();

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.playerPredicate;
        }

        public boolean requirementsMet() {
            return true;
        }
    }
}