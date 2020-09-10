package leveledmobs;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;

public class LeveledWitherSkeleton extends EntitySkeletonWither implements LeveledMob
{
    int level;
    boolean isOpenWorld;
    private int saverid;

    public boolean isANotLeveledMob(EntityLiving e)
    {
        if(e.getBukkitEntity().getLocation().distance(this.getBukkitEntity().getLocation()) > 7)
        {
            return false;
        }
        return (!(e instanceof LeveledMob) || (e instanceof RaidBoss));
    }

    public LeveledWitherSkeleton(org.bukkit.World world, int level, Location loc)
    {
        super(EntityTypes.WITHER_SKELETON, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Wither Skeleton "));
        this.setCustomNameVisible(true);

        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + (1.3*(double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level*0.5;
        this.isOpenWorld = false;

        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);

        LivingEntity le = (LivingEntity) this.getBukkitEntity();
        le.setRemoveWhenFarAway(false);

        LeveledMobListener.randomEquipment(this);

        this.level = level;
        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        this.getWorld().addEntity(this);
    }

    public LeveledWitherSkeleton(org.bukkit.World world, int level, Location loc, boolean isOpenWorld, int saverid)
    {
        super(EntityTypes.WITHER_SKELETON, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Wither Skeleton "));
        this.setCustomNameVisible(true);

        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + (1.2*(double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level*0.5;
        this.isOpenWorld = isOpenWorld;
        this.saverid = saverid;

        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);

        LivingEntity le = (LivingEntity) this.getBukkitEntity();
        le.setRemoveWhenFarAway(false);

        LeveledMobListener.randomEquipment(this);

        this.level = level;
        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        this.getWorld().addEntity(this);
    }

    @Override
    public void initPathfinder()
    {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new PathfinderGoalMeleeAttack(this,1.2D,true));
        this.goalSelector.a(2, new PathfinderGoalLookAtPlayer(this,EntityLiving.class,0.7f));
        this.goalSelector.a(3, new PathfinderGoalRandomLookaround(this));
        if (this.isOpenWorld)
            this.goalSelector.a(4, new PathfinderGoalRandomStrollLand(this, 1.0D));

        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,10,true,true,this::isANotLeveledMob));
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
