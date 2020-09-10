package leveledmobs;

import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import minecraft.mmoplugin.DungeonManager;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LeveledWither extends EntityWither implements LeveledMob, RaidBoss
{
    public enum Enrange
    {
        NONE,
        SOFT,
        HARD
    }

    Plugin plugin;
    HashMap<Player, String> playerList;
    private HashMap<UUID, Double> playerDamages;
    BossBar bar;
    int level;
    private LeveledWither.Enrange enrageLevel;
    private boolean mechanicsStarted;

    public boolean isMechanicsStarted() {
        return mechanicsStarted;
    }

    public void setMechanicsStarted(boolean mechanicsStarted) {
        this.mechanicsStarted = mechanicsStarted;
    }

    public Enrange getEnrageLevel() {
        return enrageLevel;
    }

    public void setEnrageLevel(Enrange enrageLevel) {
        this.enrageLevel = enrageLevel;
    }


    @Override
    public HashMap<UUID, Double> getPlayerDamages() {
        return playerDamages;
    }

    @Override
    public void setPlayerDamages(HashMap<UUID, Double> playerDamages) {
        this.playerDamages = playerDamages;
    }


    public boolean isANotLeveledMob(EntityLiving e)
    {
        return ( ( !(e instanceof LeveledMob) && !(e instanceof RaidBoss) ) && Math.abs(e.locY() - this.locY()) <= 4.0D && Math.abs(e.locX() - this.locX()) <= 4.0D && Math.abs(e.locZ() - this.locZ()) <= 4.0D);
    }

    public LeveledWither(org.bukkit.World world, int level, Location loc, Plugin plugin)
    {
        super(EntityTypes.WITHER, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Wither "));
        this.setCustomNameVisible(true);
        double health = this.getHealth() + (20*level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed + 0.005*level);
        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        this.plugin = plugin;
        this.level = level;
        this.setAirTicks(0);
        this.enrageLevel = Enrange.NONE;
        this.mechanicsStarted = false;

        playerList = new HashMap<>();
        playerDamages = new HashMap<>();

        LivingEntity en = (LivingEntity) this.getBukkitEntity();
        en.setRemoveWhenFarAway(false);
        en.setMaximumAir(0);
        en.setRemainingAir(0);

        bar = plugin.getServer().createBossBar(ChatColor.BLUE + "[LVL " + level + "]" + ChatColor.WHITE + " Wither ", BarColor.RED, BarStyle.SOLID);
        bar.setProgress(this.getHealth()/this.getMaxHealth());

        Bukkit.getMobGoals().removeGoal((Wither)this.getBukkitEntity(), VanillaGoal.RANDOM_STROLL);
        Bukkit.getMobGoals().removeGoal((Wither)this.getBukkitEntity(), VanillaGoal.RANDOM_STROLL_LAND);
        Bukkit.getMobGoals().removeGoal((Wither)this.getBukkitEntity(), VanillaGoal.NEAREST_ATTACKABLE_TARGET);
        this.goalSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityPlayer>(this,EntityPlayer.class,true));
        this.goalSelector.a(3, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,10,true,true,this::isANotLeveledMob));
        this.goalSelector.a(4, new PathfinderGoalLookAtPlayer(this,EntityLiving.class,0.7f));
        this.goalSelector.a(5, new PathfinderGoalRandomLookaround(this));


        this.getWorld().addEntity(this);

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
