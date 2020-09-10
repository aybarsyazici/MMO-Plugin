package leveledmobs;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class LeveledSmallMagmaCube extends EntityMagmaCube implements LeveledMob
{

    int level;
    private LeveledMagmaCube parent;

    public LeveledMagmaCube getParent() {
        return parent;
    }

    public void setParent(LeveledMagmaCube parent) {
        this.parent = parent;
    }

    public boolean isANotLeveledMob(EntityLiving e)
    {
        return ((!(e instanceof LeveledMob) && !(e instanceof RaidBoss)) && Math.abs(e.locY() - this.locY()) <= 4.0D);
    }

    public LeveledSmallMagmaCube(org.bukkit.World world, int level, Location loc, LeveledMagmaCube owner)
    {
        super(EntityTypes.MAGMA_CUBE, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Magma Cube "));
        this.setCustomNameVisible(true);
        this.setSize(1,false);
        double health = this.getHealth() + (2*level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed + 0.005*level);
        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        this.level = level;
        LivingEntity en = (LivingEntity) this.getBukkitEntity();
        en.setRemoveWhenFarAway(false);
        this.parent = owner;
        this.getWorld().addEntity(this);
        /*plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ATTACK DAMAGE IS " + this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue());
        //.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ATTACK SPEED IS " + this.getAttributeInstance(GenericAttributes.ATTACK_SPEED).getValue());
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ATTACK KNOCKBACK IS " + this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).getValue());
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ARMOUR IS " + this.getAttributeInstance(GenericAttributes.ARMOR).getValue());
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ARMOUR TOUGHNESS IS " + this.getAttributeInstance(GenericAttributes.ARMOR_TOUGHNESS).getValue());*/
    }

    @Override
    protected void initPathfinder()
    {
        Class c = EntitySlime.class.getDeclaredClasses()[2]; //TODO: CAREFUL!
        Constructor constructor = null;
        try {
            constructor = c.getDeclaredConstructor(EntitySlime.class);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        try {
            this.goalSelector.a(1, (PathfinderGoal) constructor.newInstance(this));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }

        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        try {
            this.goalSelector.a(1, (PathfinderGoal) constructor.newInstance(this));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        this.goalSelector.a(2, new PathfinderGoalLeapAtTarget(this,0.4f));
        this.goalSelector.a(3, new PathfinderGoalOcelotAttack(this));
        this.goalSelector.a(4, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,10,true,true,this::isANotLeveledMob));
        this.goalSelector.a(5, new PathfinderGoalLookAtPlayer(this,EntityLiving.class,0.7f));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));

        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,1,true,true,this::isANotLeveledMob));
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
