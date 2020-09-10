package minecraft.mmoplugin;

import minecraft.mmoplugin.events.RightClickNPC;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class NPCListener implements Listener
{
    Plugin plugin;

    NPCListener(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinNPCEvent(PlayerJoinEvent event)
    {
        if(NPCManager.getNPCs() == null || NPCManager.getNPCs().isEmpty())
            return;
        NPCManager.addJoinPacket(event.getPlayer());
    }

    @EventHandler
    public void onNPCClick(RightClickNPC event)
    {
        Player player = event.getPlayer();
        //player.sendMessage(ChatColor.GOLD + "You have right clicked an NPC with name " + event.getNPC().getName());

        switch(event.getNPC().getName())
        {
            case "§f[NPC]Warrior":
                ClassInfoInventory.createInfoInventory(plugin,player,"warrior");
                break;
            case "§f[NPC]Cleric":
                ClassInfoInventory.createInfoInventory(plugin,player,"cleric");
                break;
            case "§f[NPC]Summoner":
                ClassInfoInventory.createInfoInventory(plugin,player,"necromancer");
                break;
            case "§e[NPC]Manager":
                CustomInventory.createDungeonInfoInventory(player, plugin,"dungeon_nether_");
                break;
            case "§a[NPC]Manager":
                CustomInventory.createDungeonInfoInventory(player, plugin,"dungeon_jungle_");
                break;
            case "§c[NPC]Manager":
                CustomInventory.createDungeonInfoInventory(player, plugin,"dungeon_end_");
                break;
            case "§c[NPC]Faction":
                CustomInventory.createFactionMenu(player,plugin,null,0);
                break;
            case "§6[NPC]Pazar":
                if(MainClass.classObjectMap.containsKey(player.getUniqueId()))
                    CustomInventory.createAuctionWelcomeMenu(player,plugin);
                else
                    player.sendMessage(MainClass.getPluginPrefix() + ChatColor.GOLD + "Market sistemini kullanabilmek için önce sınıfını seçmiş olman gerekir.");
                break;
            case "§e[NPC]Demirci":
                CustomInventory.createMarketNPCWelcomeInventory(player,plugin,"Demirci");
                break;
            case "§3[NPC]Oduncu":
                CustomInventory.createMarketNPCWelcomeInventory(player,plugin,"Oduncu");
                break;
            case "§7[NPC]Madenci":
                CustomInventory.createMarketNPCWelcomeInventory(player,plugin,"Madenci");
                break;
            case "§a[NPC]Biyolog":
                CustomInventory.createMarketNPCWelcomeInventory(player,plugin,"Biyolog");
                break;
        }
    }
}
