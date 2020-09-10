package leveledmobs;

import minecraft.mmoplugin.DungeonManager;
import minecraft.mmoplugin.MainClass;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public interface RaidBoss
{

    CraftEntity bukkitEntity();
    boolean isStillAlive();
    HashMap<Player, String> getPlayerList();
    BossBar getBar();
    float currentHealth();
    float maxHealth();
    HashMap<UUID, Double> getPlayerDamages();
    void setPlayerDamages(HashMap<UUID, Double> playerDamages);
    int getLevel();

    class BossBarChecker extends BukkitRunnable
    {

        Plugin plugin = MainClass.getPlugin((MainClass.class));
        RaidBoss raidBoss;
        String worldName;
        org.bukkit.World world;

        BossBarChecker(RaidBoss raidBoss)
        {
            this.raidBoss = raidBoss;
            worldName = raidBoss.bukkitEntity().getWorld().getName();
            world = plugin.getServer().getWorld(worldName);
        }

        @Override
        public void run()
        {
            if(!raidBoss.isStillAlive())
            {
                plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Raid boss is dead, canceling task");
                for(Player p: raidBoss.getPlayerList().keySet())
                {
                    raidBoss.getBar().removePlayer(p);
                }
                cancel();
            }
            else
            {
                for(Player p : world.getPlayers())
                {
                    if(p.getLocation().distance(raidBoss.bukkitEntity().getLocation()) <= 30)
                    {
                        if(raidBoss.getPlayerList().containsKey(p))
                        {
                            //Do nothing
                        }
                        else
                        {
                            raidBoss.getBar().addPlayer(p);
                            raidBoss.getPlayerList().put(p,"");
                        }
                    }
                    else
                    {
                        if(raidBoss.getPlayerList().containsKey(p))
                        {
                            raidBoss.getBar().removePlayer(p);
                            raidBoss.getPlayerList().remove(p);
                        }
                        else
                        {
                            //Do nothing.
                        }
                    }
                }
            }
        }
    }

    class AuraEffect extends BukkitRunnable
    {

        Entity e;
        double t;
        Location loc;

        AuraEffect(Entity entity)
        {
            this.e = entity;
            this.t = 0;
            Location loc = e.getLocation();
        }

        @Override
        public void run() {
            t = t + Math.PI/8;
            double x = 1*cos(t);
            double y = t;
            double z = 1*sin(t);

            loc.add(x,y,z);
            
            loc.subtract(x,y,z);
            if(t > Math.PI*4)
            {
                cancel();
            }
        }

    }

    class WitherSkeletonMechanics extends BukkitRunnable
    {
        int totalSecondsElapsed;
        Wither witherBoss;
        DungeonManager.Dungeon dungeon;
        Random rand;
        LeveledWither lWither;
        Plugin plugin;

        WitherSkeletonMechanics(Wither wither, DungeonManager.Dungeon dungeon, Plugin plugin)
        {
            this.witherBoss = wither;
            totalSecondsElapsed = 0;
            this.dungeon = dungeon;
            rand = new Random();
            this.lWither = (LeveledWither) ((CraftEntity) witherBoss).getHandle();
            this.plugin = plugin;
        }

        @Override
        public void run() {
            totalSecondsElapsed++;
            if(witherBoss.isDead())
            {
                cancel();
                return;
            }
            if(lWither.isStillAlive() && lWither.getEnrageLevel().equals(LeveledWither.Enrange.HARD))
            {
                if (totalSecondsElapsed%5 == 0)
                {
                    double maxHP = witherBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    if(witherBoss.getHealth() + maxHP*0.05 > maxHP)
                    {
                        witherBoss.setHealth(maxHP);
                    }
                    else
                    {
                        witherBoss.setHealth(witherBoss.getHealth() + maxHP*0.05);
                    }
                    lWither.getBar().setProgress(witherBoss.getHealth()/maxHP);
                    MainClass.getPlugin(MainClass.class).getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Boss can dolduruyor!");
                }
            }
            if(totalSecondsElapsed%10 == 0)
            {
                int randRoll = rand.nextInt(3);
                PotionEffect potionEffect = null;
                switch (randRoll)
                {
                    case 0:
                        potionEffect = new PotionEffect(PotionEffectType.BLINDNESS,3*20,0);
                        break;
                    case 1:
                        potionEffect = new PotionEffect(PotionEffectType.CONFUSION,6*20,0);
                        break;
                    case 2:
                        potionEffect = new PotionEffect(PotionEffectType.POISON,10*20,5);
                        break;
                }
                randRoll = rand.nextInt(dungeon.getPlayerList().size());

                dungeon.getPlayerList().get(randRoll).addPotionEffect(potionEffect);
            }
            if(totalSecondsElapsed%20 == 0)
            {
                int randRoll = rand.nextInt(dungeon.getPlayerList().size());

                Location loc = new Location(witherBoss.getWorld(),5,5,5);
                loc.setYaw(-177);
                loc.setPitch(-2);
                Location oldLoc = dungeon.getPlayerList().get(randRoll).getLocation();
                dungeon.getPlayerList().get(randRoll).teleport(loc);
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                    dungeon.getPlayerList().get(randRoll).teleport(oldLoc);
                },6*20);
            }
        }
    }
}
