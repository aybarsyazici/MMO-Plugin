package particles;

import leveledmobs.RaidBoss;
import minecraft.mmoplugin.MMOClass;
import minecraft.mmoplugin.MainClass;
import minecraft.mmoplugin.Necromancer;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EntityLiving;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class LeashParticle extends BukkitRunnable
{
    Player player;
    double radius;
    Location loc;
    double counter;
    double yToAdd;
    Plugin plugin;

    boolean isNotAPlayer(LivingEntity e)
    {
        if(((CraftEntity)e).getHandle() instanceof Necromancer.Summon)
        {
            return true;
        }
        return !(e instanceof Player);
    }

    public LeashParticle(Player p, double radius, Plugin plugin)
    {
        this.player = p;
        this.radius = radius;
        this.counter = radius;
        this.yToAdd = 5;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Location loc = player.getLocation();
        if(player.isDead())
            cancel();
        if(yToAdd > 0)
        {
            loc.add(0,yToAdd,0);
        }
        for(int theta = 0; theta <=360; theta+=5)
        {
            double xToAdd = counter*Math.sin(theta);
            double yToAdd = counter*Math.cos(theta);
            loc.add(xToAdd,0,yToAdd);
            loc.getWorld().spawnParticle(Particle.FALLING_OBSIDIAN_TEAR,loc,1);
            loc.subtract(xToAdd,0,yToAdd);
        }
        if(yToAdd > 0)
        {
            loc.subtract(0,yToAdd,0);
            yToAdd -= 0.3;
        }
        else
        {
            counter -= 0.2;
            if(counter <= 0)
            {
                cancel();
                int playersLeashed = 0;
                int mobsLeashed = 0;
                List<Player> playerList = new ArrayList<>();
                if (loc.getWorld().getPVP())
                {
                    playerList = (List<Player>) loc.getNearbyPlayers(10);
                    for(Player nearbyPlayer : playerList)
                    {
                        if(MMOClass.ifSameFaction(player,nearbyPlayer))
                        {
                            //Do nothing.
                        }
                        else
                        {
                            nearbyPlayer.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP,1.0f,1.0f);
                            nearbyPlayer.teleport(loc);
                            playersLeashed++;
                        }
                    }
                }
                List<LivingEntity> entities = (List<LivingEntity>) loc.getNearbyLivingEntities(radius,this::isNotAPlayer);
                for(LivingEntity le : entities)
                {
                    if(le instanceof CraftEntity && (((CraftEntity)le).getHandle() instanceof RaidBoss || le instanceof ArmorStand) && Math.abs(le.getLocation().getY() - loc.getY()) <= 5)
                        continue;
                    le.teleport(loc);
                    EntityLiving temp = ((CraftLivingEntity)le).getHandle();
                    ((EntityInsentient)temp).setGoalTarget(((CraftPlayer)player).getHandle(), EntityTargetEvent.TargetReason.CUSTOM,true);
                    mobsLeashed++;
                }
                player.setAbsorptionAmount(player.getAbsorptionAmount() + 2*playersLeashed + mobsLeashed);
                loc.getWorld().strikeLightning(loc);
            }
        }
    }
}
