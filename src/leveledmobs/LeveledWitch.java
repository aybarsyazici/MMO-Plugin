package leveledmobs;

import com.destroystokyo.paper.entity.ai.VanillaGoal;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Witch;

public class LeveledWitch extends EntityWitch implements LeveledMob
{
    int level;
    private PathfinderGoalNearestAttackableTargetWitch<EntityLiving> bz;
    boolean isOpenWorld;
    private int saverid;

    public boolean isANotLeveledMob(EntityLiving e)
    {
        if(e.getBukkitEntity().getLocation().distance(this.getBukkitEntity().getLocation()) > 7)
        {
            return false;
        }
        return (!(e instanceof LeveledMob) && !(e instanceof RaidBoss));
    }

    public LeveledWitch(org.bukkit.World world, int level, Location loc)
    {
        super(EntityTypes.WITCH, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Witch "));
        this.setCustomNameVisible(true);


        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + ((double)level/2);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level*3/2;
        this.isOpenWorld = false;


        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);

        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        this.level = level;
        LivingEntity en = (LivingEntity) this.getBukkitEntity();
        en.setRemoveWhenFarAway(false);


        this.bz = new PathfinderGoalNearestAttackableTargetWitch<EntityLiving>(this, EntityLiving.class, 10, true, false, this::isANotLeveledMob);
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalArrowAttack(this, 1.0D, 60, 10.0F));
        this.goalSelector.a(3, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(4, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this));
        this.targetSelector.a(2, this.bz);


        Bukkit.getMobGoals().removeGoal((Witch)this.getBukkitEntity(),VanillaGoal.RANDOM_STROLL);
        Bukkit.getMobGoals().removeGoal((Witch)this.getBukkitEntity(),VanillaGoal.RANDOM_STROLL_LAND);
        this.getWorld().addEntity(this);
    }

    public LeveledWitch(org.bukkit.World world, int level, Location loc, boolean isOpenWorld)
    {
        super(EntityTypes.WITCH, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Witch "));
        this.setCustomNameVisible(true);


        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + ((double)level/2);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level*3/2;
        this.isOpenWorld = false;


        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);

        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        this.level = level;
        LivingEntity le = (LivingEntity)this.getBukkitEntity();
        le.setRemoveWhenFarAway(true);

        this.bz = new PathfinderGoalNearestAttackableTargetWitch<EntityLiving>(this, EntityLiving.class, 10, true, false, this::isANotLeveledMob);
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalArrowAttack(this, 1.0D, 60, 10.0F));
        this.goalSelector.a(3, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(4, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this));
        this.targetSelector.a(2, this.bz);

        if(!this.isOpenWorld)
        {
            Bukkit.getMobGoals().removeGoal((Witch)this.getBukkitEntity(),VanillaGoal.RANDOM_STROLL);
            Bukkit.getMobGoals().removeGoal((Witch)this.getBukkitEntity(),VanillaGoal.RANDOM_STROLL_LAND);
        }
        this.getWorld().addEntity(this);
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public CraftEntity getNormalEntity() {
        return this.getBukkitEntity();
    }

}
