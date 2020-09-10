package leveledmobs;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LeveledMagmaCube extends EntityMagmaCube implements LeveledMob, RaidBoss
{
    Plugin plugin;
    HashMap<Player, String> playerList;
    private HashMap<UUID, Double> playerDamages;
    private List<LeveledSmallMagmaCube> smallMagmaCubes;
    private boolean reinforcementsCalled;
    private boolean spawningFireballs;
    BossBar bar;
    int level;

    public boolean isSpawningFireballs() {
        return spawningFireballs;
    }

    public void setSpawningFireballs(boolean spawningFireballs) {
        this.spawningFireballs = spawningFireballs;
    }

    @Override
    public HashMap<UUID, Double> getPlayerDamages() {
        return playerDamages;
    }

    @Override
    public void setPlayerDamages(HashMap<UUID, Double> playerDamages) {
        this.playerDamages = playerDamages;
    }

    public List<LeveledSmallMagmaCube> getSmallMagmaCubes() {
        return smallMagmaCubes;
    }

    public void setSmallMagmaCubes(List<LeveledSmallMagmaCube> smallMagmaCubes) {
        this.smallMagmaCubes = smallMagmaCubes;
    }

    public void removeFromSmallMagmaCubes(LeveledSmallMagmaCube smallMagmaCube)
    {
        this.smallMagmaCubes.remove(smallMagmaCube);
    }

    public boolean isReinforcementsCalled() {
        return reinforcementsCalled;
    }

    public void setReinforcementsCalled(boolean reinforcementsCalled) {
        this.reinforcementsCalled = reinforcementsCalled;
    }

    public boolean isANotLeveledMob(EntityLiving e)
    {
        return ((!(e instanceof LeveledMob) && !(e instanceof RaidBoss)) && Math.abs(e.locY() - this.locY()) <= 4.0D && Math.abs(e.locX() - this.locX()) <= 4.0D && Math.abs(e.locZ() - this.locZ()) <= 4.0D);
    }

    public LeveledMagmaCube(org.bukkit.World world, int level, Location loc, Plugin plugin)
    {
        super(EntityTypes.MAGMA_CUBE, ((CraftWorld)world).getHandle());
        this.setSize(10,false);
        double health = this.getHealth() + (15*level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed + 0.005*level);
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Magma Cube : " + health + ChatColor.RED + "❤"));
        this.setCustomNameVisible(true);
        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        this.plugin = plugin;
        this.level = level;
        playerList = new HashMap<>();
        playerDamages = new HashMap<>();
        smallMagmaCubes = new ArrayList<>();
        LivingEntity en = (LivingEntity) this.getBukkitEntity();
        en.setRemoveWhenFarAway(false);
        bar = plugin.getServer().createBossBar(ChatColor.BLUE + "[LVL " + level + "]" + ChatColor.WHITE + " Magma Cube : " + health + ChatColor.RED + "❤", BarColor.RED, BarStyle.SOLID);
        bar.setProgress(this.getHealth()/this.getMaxHealth());
        this.reinforcementsCalled = false;
        this.spawningFireballs = false;
        this.getWorld().addEntity(this);
        /*plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ATTACK DAMAGE IS " + this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue());
        //.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ATTACK SPEED IS " + this.getAttributeInstance(GenericAttributes.ATTACK_SPEED).getValue());
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ATTACK KNOCKBACK IS " + this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).getValue());
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ARMOUR IS " + this.getAttributeInstance(GenericAttributes.ARMOR).getValue());
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ARMOUR TOUGHNESS IS " + this.getAttributeInstance(GenericAttributes.ARMOR_TOUGHNESS).getValue());*/
        BukkitTask task = new BossBarChecker(this).runTaskTimer(plugin,0,60);
    }

    @Override
    protected void initPathfinder()
    {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
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

    public HashMap<Player, String> getGiantPlayerList()
    {
        return playerList;
    }

    public BossBar getGiantBossBar()
    {
        return bar;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public CraftEntity getNormalEntity() {
        return this.getBukkitEntity();
    }


    @Override
    public CraftEntity bukkitEntity() {
        return this.getBukkitEntity();
    }

    @Override
    public boolean isStillAlive() {
        return (this != null && this.isAlive());
    }

    @Override
    public HashMap<Player, String> getPlayerList() {
        return this.playerList;
    }

    @Override
    public BossBar getBar() {
        return this.bar;
    }

    @Override
    public float currentHealth() {
        return this.getHealth();
    }

    @Override
    public float maxHealth() {
        return this.getMaxHealth();
    }
}
