package leveledmobs;

import minecraft.mmoplugin.Necromancer;
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

import java.util.HashMap;
import java.util.UUID;

public class EliteGiant extends EntityGiantZombie implements LeveledMob, RaidBoss
{

    Plugin plugin;
    HashMap<Player, String> playerList;
    private HashMap<UUID, Double> playerDamages;
    BossBar bar;

    @Override
    public HashMap<UUID, Double> getPlayerDamages() {
        return playerDamages;
    }

    @Override
    public void setPlayerDamages(HashMap<UUID, Double> playerDamages) {
        this.playerDamages = playerDamages;
    }

    int level;

    public EliteGiant(org.bukkit.World world, int level, Location loc, Plugin plugin)
    {
        super(EntityTypes.GIANT, ((CraftWorld)world).getHandle());
        double health = this.getHealth() + (15*level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
        double basedmg = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue();
        basedmg = basedmg - (basedmg*92/100);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(basedmg);
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed + 0.002*level);
        this.setHealth((float)health);
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + ChatColor.WHITE + " Elite Giant : " + health + ChatColor.RED + "❤"));
        this.setCustomNameVisible(true);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        this.plugin = plugin;
        this.level = level;
        playerList = new HashMap<>();
        playerDamages = new HashMap<>();
        LivingEntity en = (LivingEntity) this.getBukkitEntity();
        en.setRemoveWhenFarAway(false);
        bar = plugin.getServer().createBossBar(ChatColor.BLUE + "[LVL " + level + "]" + ChatColor.WHITE + " Elite Giant : " + health + ChatColor.RED + "❤", BarColor.RED, BarStyle.SOLID);
        bar.setProgress(this.getHealth()/this.getMaxHealth());
        this.goalSelector.a(0, new PathfinderGoalNearestAttackableTarget<EntityPlayer>(this,EntityPlayer.class,true));
        this.goalSelector.a(1, new PathfinderGoalNearestAttackableTarget<Necromancer.CustomZombie>(this, Necromancer.CustomZombie.class,true));
        this.goalSelector.a(1, new PathfinderGoalNearestAttackableTarget<Necromancer.CustomSkeleton>(this, Necromancer.CustomSkeleton.class,true));
        this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, 1, true));
        this.getWorld().addEntity(this);
        /*plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ATTACK DAMAGE IS " + this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue());
        //.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ATTACK SPEED IS " + this.getAttributeInstance(GenericAttributes.ATTACK_SPEED).getValue());
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ATTACK KNOCKBACK IS " + this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).getValue());
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ARMOUR IS " + this.getAttributeInstance(GenericAttributes.ARMOR).getValue());
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "ARMOUR TOUGHNESS IS " + this.getAttributeInstance(GenericAttributes.ARMOR_TOUGHNESS).getValue());*/
        BukkitTask task = new BossBarChecker(this).runTaskTimer(plugin,0,60);
    }

    public EliteGiant(org.bukkit.World world)
    {
        super(EntityTypes.GIANT, ((CraftWorld)world).getHandle());
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
    public CraftEntity bukkitEntity() {
        return this.getBukkitEntity();
    }

    @Override
    public boolean isStillAlive() {
        return this.isAlive();
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

    @Override
    public CraftEntity getNormalEntity() {
        return this.getBukkitEntity();
    }

}
