package net.alek.succorstadiums.entity.monsters;

import net.alek.succorstadiums.particle.ModParticles;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;

public class GrassCreeper extends Creeper {

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final float DIRECT_DAMAGE = 2.0F;
    public static final double ATTACK_RADIUS = 4.5D;
    public static final int RING_DURATION_TICKS = 13 * 20;
    private static final int FUSE_TICKS = 24;

    public static final float RING_WIDTH_BLOCKS = 1.0F;
    public static final float GREEN_RING_OUTER_RADIUS = (float) ATTACK_RADIUS + RING_WIDTH_BLOCKS;
    public static final float GREEN_CLOUD_DAMAGE = 0.5F;

    public GrassCreeper(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
        trySetFuseTime();
    }

    private void trySetFuseTime() {
        try {
            Field maxSwellField = Creeper.class.getDeclaredField("maxSwell");
            maxSwellField.setAccessible(true);
            maxSwellField.setInt(this, FUSE_TICKS);
        } catch (Exception e) {
            LOGGER.error("Failed to set fuse time for grass creeper:", e);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SwellGoal(this));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 1.0D, 1.2D));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Cat.class, 6.0F, 1.0D, 1.2D));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    public void customExplode() {

        // If client side do nothing
        if (this.level().isClientSide()) return;

        // Set server level and x, y, and z positions
        ServerLevel serverLevel = (ServerLevel) this.level();
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        // Play explosion sound and send particles
        serverLevel.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE,
                2.0f, 1.0f + (serverLevel.getRandom().nextFloat() - serverLevel.getRandom().nextFloat()) * 0.2f);
        serverLevel.sendParticles(ModParticles.FOREST_ANGRY, x, y + 0.5f, z, 20, 0.5f, 0.5f, 0.5f, 0.01f);

        // Create a damage source and calculate if player is in the range if so take damage
        DamageSource explosionDamage = this.damageSources().explosion(this, this);
        for (Player player : serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(ATTACK_RADIUS))) {
            if (this.distanceTo(player) <= ATTACK_RADIUS) {
                player.hurtServer(serverLevel, explosionDamage, DIRECT_DAMAGE);
            }
        }

        // Summon new particle effects
        GrassCreeperCloud cloud = new GrassCreeperCloud(serverLevel, x, y, z);
        GrassCreeperGreenRingCloud greenRing = new GrassCreeperGreenRingCloud(serverLevel, x, y, z);
        serverLevel.addFreshEntity(cloud);
        serverLevel.addFreshEntity(greenRing);

        this.discard();
    }
}