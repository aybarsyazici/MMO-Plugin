package leveledmobs;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;


public class LeveledStray extends EntitySkeletonStray implements LeveledMob
{

    int level;
    boolean isOpenWorld;
    private int saverid;

    public boolean isANotLeveledMob(EntityLiving e)
    {
        return (!(e instanceof LeveledMob) && !(e instanceof RaidBoss));
    }

    public LeveledStray(org.bukkit.World world, int level, Location loc)
    {
        super(EntityTypes.STRAY, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Stray "));
        this.setCustomNameVisible(true);

        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + (0.85*(double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level;

        LivingEntity le = (LivingEntity)this.getBukkitEntity();
        ItemStack bow = new ItemStack(Material.BOW);
        le.getEquipment().setItemInMainHand(bow);
        le.setRemoveWhenFarAway(false);
        this.isOpenWorld = false;

        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);

        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());

        this.level = level;
        this.getWorld().addEntity(this);
    }

    public LeveledStray(org.bukkit.World world, int level, Location loc, boolean isOpenWorld)
    {
        super(EntityTypes.STRAY, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Stray "));
        this.setCustomNameVisible(true);

        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + (0.85*(double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level;

        LivingEntity le = (LivingEntity)this.getBukkitEntity();
        ItemStack bow = new ItemStack(Material.BOW);
        le.getEquipment().setItemInMainHand(bow);
        le.setRemoveWhenFarAway(true);
        this.isOpenWorld = isOpenWorld;

        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);

        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());

        this.level = level;
        this.getWorld().addEntity(this);
    }


    @Override
    protected void initPathfinder()
    {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalBowShoot(this, 1.2D, 40, 20.0F));
        this.goalSelector.a(3, new PathfinderGoalArrowAttack(this, 1.2D, 40, 20.0F));
        this.goalSelector.a(4, new PathfinderGoalLookAtPlayer(this,EntityLiving.class,0.7f));
        this.goalSelector.a(5, new PathfinderGoalRandomLookaround(this));
        if(this.isOpenWorld)
            this.goalSelector.a(6, new PathfinderGoalRandomStrollLand(this,1.0D));

        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,10,true,false,this::isANotLeveledMob));
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
