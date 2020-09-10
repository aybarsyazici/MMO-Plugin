package minecraft.mmoplugin;

import com.comphenix.protocol.events.PacketEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import leveledmobs.*;
import minecraft.mmoplugin.events.DungeonConfigManager;
import minecraft.mmoplugin.events.OpenWorldConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

import static minecraft.mmoplugin.MainClass.*;

public class DungeonListener implements Listener
{
    Plugin plugin;

    DungeonListener(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event)
    {
        Player p = event.getPlayer();
        //plugin.getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.DARK_AQUA + "On player Respawn event for the player: " + ChatColor.WHITE + p.getName());
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()) && !MainClass.classObjectMap.get(p.getUniqueId()).getCurrentDungeon().equalsIgnoreCase(""))
        {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,() ->{
                p.teleport(Bukkit.getWorld("world").getSpawnLocation());
            },0);
            String dungeonName = MainClass.classObjectMap.get(p.getUniqueId()).getCurrentDungeon();
            MainClass.classObjectMap.get(p.getUniqueId()).setCurrentDungeon("");
            DungeonManager.dungeonMap.get(dungeonName).getPlayerList().remove(p);
            if(DungeonManager.dungeonMap.get(dungeonName).getPlayerList().size() == 0)
            {
                for(Entity e : Bukkit.getWorld(dungeonName).getEntities())
                {
                    if(!(e instanceof Player))
                        e.remove();
                }
                DungeonManager.dungeonMap.get(dungeonName).setInProgress(false);
            }
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event)
    {
        Player joinedPlayer = event.getPlayer();
        if(joinedPlayer.getWorld().getName().contains("dungeon_"))
        {
            joinedPlayer.teleport(Bukkit.getWorld("world").getSpawnLocation());
            if(MainClass.classObjectMap.containsKey(joinedPlayer.getUniqueId()))
            {
                MainClass.classObjectMap.get(joinedPlayer.getUniqueId()).setCurrentDungeon("");
            }
        }
    }

    @EventHandler
    public void ChunkUnloadEvent(ChunkUnloadEvent event)
    {
        if(event.getWorld().getName().equals("openworld_emnia"))
        {
            //event.getChunk().setForceLoaded(false);
            for(Entity e : event.getChunk().getEntities())
            {
                if(e instanceof LivingEntity)
                    ((LivingEntity)e).setHealth(0);
                try {
                    e.remove();
                } catch (Exception exception) {
                   //exception.printStackTrace();
                }
            }
        }
    }

    @EventHandler
    public void ChunkLoadEvent(ChunkLoadEvent event)
    {
        if(event.getWorld().getName().equals("openworld_emnia"))
        {
            event.getChunk().setForceLoaded(false);
        }
    }


}
