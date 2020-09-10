package leveledmobs;

import com.destroystokyo.paper.entity.ai.VanillaGoal;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.*;

public class LeveledRavager extends EntityRavager implements LeveledMob
{
    int level;

    public boolean isANotLeveledMob(EntityLiving e)
    {
        if(e.getBukkitEntity().getLocation().distance(this.getBukkitEntity().getLocation()) > 7)
        {
            return false;
        }
        return (!(e instanceof LeveledMob) && !(e instanceof RaidBoss));
    }

    public LeveledRavager(org.bukkit.World world, int level, Location loc)
    {
        super(EntityTypes.RAVAGER, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Ravager "));
        this.setCustomNameVisible(true);


        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + (4*(double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level*0.25;


        LivingEntity en = (LivingEntity) this.getBukkitEntity();
        en.setRemoveWhenFarAway(false);
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);

        this.level = level;
        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());

        Bukkit.getMobGoals().removeGoal((Ravager)this.getBukkitEntity(), VanillaGoal.RANDOM_STROLL_LAND);
        Bukkit.getMobGoals().removeGoal((Ravager)this.getBukkitEntity(), VanillaGoal.RANDOM_STROLL);
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(3, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(4, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this, EntityLiving.class, 6, true, false, this::isANotLeveledMob));
        this.getWorld().addEntity(this);

    }


    @Override
    protected void H() {
        boolean flag = !(this.getRidingPassenger() instanceof EntityInsentient) || this.getRidingPassenger().getEntityType().a(TagsEntity.RADIERS) || this.getRidingPassenger().getEntityType().a(TagsEntity.SKELETONS);
        boolean flag1 = !(this.getVehicle() instanceof EntityBoat);
        this.goalSelector.a(PathfinderGoal.Type.MOVE, flag);
        this.goalSelector.a(PathfinderGoal.Type.JUMP, flag && flag1);
        this.goalSelector.a(PathfinderGoal.Type.LOOK, flag);
        this.goalSelector.a(PathfinderGoal.Type.TARGET, flag);
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public CraftEntity getNormalEntity() {
        return this.getBukkitEntity();
    }

}
