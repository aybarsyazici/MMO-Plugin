package minecraft.mmoplugin;

import minecraft.mmoplugin.customItems.CommonItems;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;


public class InventoryListener implements Listener
{
    Plugin plugin;

    InventoryListener(Plugin plugin)
    {
        this.plugin = plugin;
    }



    @EventHandler
    public void onInventoryClicked(InventoryClickEvent event)
    {
        Inventory inventory = event.getClickedInventory();

        if(event.isCancelled())
            return;

        if(inventory == null)
            return;

        if(event.getWhoClicked().getName().equals("Yoshiane"))
        {
            event.getWhoClicked().sendMessage("Clicked: " + event.getSlot());
        }

        if(event.getView().getTitle().contains("Takas Menüsü"))
        {
            if(event.getClick().isShiftClick())
            {
                event.setCancelled(true);
                return;
            }
            if(event.getClick().isRightClick())
            {
                event.setCancelled(true);
                return;
            }

            if(inventory.getType().equals(InventoryType.PLAYER))
                return;

            int clickedSlot = event.getSlot();

            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Clicked slot is, " + clickedSlot);
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "______________________________________");

            CustomInventory.TradingInventoryHolder holder = (CustomInventory.TradingInventoryHolder)inventory.getHolder();

            Player clicker = (Player) event.getWhoClicked();

            if(clicker.getUniqueId().equals(holder.getPlayer1().getUniqueId()))
            {
                //Clicker is the Player1
                if(CustomInventory.AllowedTradingSlot(1,clickedSlot))
                {
                    //plugin.getServer().getConsoleSender().sendMessage("We are in");
                    if(clickedSlot == 47)
                    {
                        clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                        event.setCancelled(true);
                        event.setCurrentItem(event.getCurrentItem());
                        clicker.setItemOnCursor(clicker.getItemOnCursor());
                        ItemStack readyWool = inventory.getItem(47);
                        if(readyWool.getType().equals(Material.RED_WOOL))
                        {
                            //Player 1 is trying to ready up.
                            ItemStack isNowReady = new ItemStack(Material.GREEN_WOOL);
                            ItemMeta readyMeta = isNowReady.getItemMeta();
                            readyMeta.setDisplayName(ChatColor.GREEN + "Hazır");
                            isNowReady.setItemMeta(readyMeta);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                                inventory.setItem(47,isNowReady);
                                //TODO Check if both parties are ready.
                                if(inventory.getItem(51).getType().equals(Material.GREEN_WOOL) && holder.getTradeConfirmer() == null)
                                {
                                    BukkitTask task = new CustomInventory.ConfirmTrade(inventory,holder.getPlayer1(),holder.getPlayer2()).runTaskTimer(plugin,0,20);
                                    holder.setTradeConfirmer(task);
                                }
                            },0);

                        }
                        else if (readyWool.getType().equals(Material.GREEN_WOOL))
                        {
                            //Player 1 is Un Readying.
                            ItemStack isNowReady = new ItemStack(Material.RED_WOOL);
                            ItemMeta readyMeta = isNowReady.getItemMeta();
                            readyMeta.setDisplayName(ChatColor.RED + "Hazır değil");
                            isNowReady.setItemMeta(readyMeta);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                                inventory.setItem(47,isNowReady);
                            },0);
                        }
                    }
                    else if(clickedSlot == 48)
                    {
                        //Player 1 will enter the amount of money.
                        event.setCancelled(true);

                        if(holder.getTradeConfirmer() != null)
                        {
                            holder.getTradeConfirmer().cancel();
                            holder.setTradeConfirmer(null);
                            if(holder.getPlayer2().isOnline())
                                holder.getPlayer2().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Geri sayım durdu!");
                            holder.getPlayer1().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Geri sayım durdu!");
                        }
                        ItemStack isNowReady = new ItemStack(Material.RED_WOOL);
                        ItemMeta readyMeta = isNowReady.getItemMeta();
                        readyMeta.setDisplayName(ChatColor.RED + "Hazır değil");
                        isNowReady.setItemMeta(readyMeta);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                            inventory.setItem(47,isNowReady);
                        },0);

                        event.setCurrentItem(event.getCurrentItem());
                        clicker.setItemOnCursor(clicker.getItemOnCursor());
                        MainClass.classObjectMap.get(clicker.getUniqueId()).setUsingSignGUI(true);
                        MainClass.signMenu.newMenu(new ArrayList<>()).reopenIfFail().response(((player, strings) -> {
                            String amount = "";
                            for(String s : strings)
                            {
                                amount = s;
                                break;
                            }
                            String finalAmount = amount;
                            ItemStack sign = inventory.getItem(48);
                            ItemMeta signMeta = sign.getItemMeta();
                            List<String> lore = new ArrayList<>();
                            try {
                                if(Integer.parseInt(finalAmount) <= MainClass.classObjectMap.get(clicker.getUniqueId()).getCurrency())
                                {
                                    lore.add(finalAmount);
                                    signMeta.setLore(lore);
                                    sign.setItemMeta(signMeta);
                                    inventory.setItem(48,sign);
                                }
                                else
                                {
                                    player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu kadar paran bulunmuyor.");
                                }
                            } catch (NumberFormatException e) {
                                //Player entered a string inside of a number.
                            }
                            if(player.isOnline())
                            {
                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                                    player.openInventory(inventory);
                                    MainClass.classObjectMap.get(player.getUniqueId()).setUsingSignGUI(false);
                                },0);
                            }
                            return true;
                        })).open((Player) event.getWhoClicked());
                    }
                    else
                    {
                        //plugin.getServer().getConsoleSender().sendMessage("We are in2");
                        if(holder.getTradeConfirmer() != null)
                        {
                            holder.getTradeConfirmer().cancel();
                            holder.setTradeConfirmer(null);
                            if(holder.getPlayer2().isOnline())
                                holder.getPlayer2().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Geri sayım durdu!");
                            holder.getPlayer1().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Geri sayım durdu!");
                        }
                        ItemStack isNowReady = new ItemStack(Material.RED_WOOL);
                        ItemMeta readyMeta = isNowReady.getItemMeta();
                        readyMeta.setDisplayName(ChatColor.RED + "Hazır değil");
                        isNowReady.setItemMeta(readyMeta);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                            inventory.setItem(47,isNowReady);
                        },0);
                    }
                }
                else
                {
                    /*plugin.getServer().getConsoleSender().sendMessage("Current item is: " + event.getCurrentItem());
                    plugin.getServer().getConsoleSender().sendMessage("Item on cursor is: " + clicker.getItemOnCursor());
                    plugin.getServer().getConsoleSender().sendMessage("********************");*/
                    event.setCancelled(true);
                }
            }
            else if(clicker.getUniqueId().equals(holder.getPlayer2().getUniqueId()))
            {
                //Clicker is Player2
                if(CustomInventory.AllowedTradingSlot(2,clickedSlot))
                {
                    if(clickedSlot == 51)
                    {
                        clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                        event.setCancelled(true);
                        event.setCurrentItem(event.getCurrentItem());
                        clicker.setItemOnCursor(clicker.getItemOnCursor());
                        ItemStack readyWool = inventory.getItem(51);
                        if(readyWool.getType().equals(Material.RED_WOOL))
                        {
                            //Player 2 is trying to ready up.
                            ItemStack isNowReady = new ItemStack(Material.GREEN_WOOL);
                            ItemMeta readyMeta = isNowReady.getItemMeta();
                            readyMeta.setDisplayName(ChatColor.GREEN + "Hazır");
                            isNowReady.setItemMeta(readyMeta);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                                inventory.setItem(51,isNowReady);

                                //TODO Check if both parties are ready.
                                if(inventory.getItem(47).getType().equals(Material.GREEN_WOOL) && holder.getTradeConfirmer() == null)
                                {
                                    BukkitTask task = new CustomInventory.ConfirmTrade(inventory,holder.getPlayer1(),holder.getPlayer2()).runTaskTimer(plugin,0,20);
                                    holder.setTradeConfirmer(task);
                                }
                            },0);
                        }
                        else if (readyWool.getType().equals(Material.GREEN_WOOL))
                        {
                            //Player 2 is Un Readying.
                            ItemStack isNowReady = new ItemStack(Material.RED_WOOL);
                            ItemMeta readyMeta = isNowReady.getItemMeta();
                            readyMeta.setDisplayName(ChatColor.RED + "Hazır değil");
                            isNowReady.setItemMeta(readyMeta);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                                inventory.setItem(51,isNowReady);
                            },0);
                        }
                    }
                    else if(clickedSlot == 52)
                    {
                        //Player 2 will enter the amount of money.
                        event.setCancelled(true);

                        if(holder.getTradeConfirmer() != null)
                        {
                            holder.getTradeConfirmer().cancel();
                            holder.setTradeConfirmer(null);
                            if(holder.getPlayer1().isOnline())
                                holder.getPlayer1().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Geri sayım durdu!");
                            holder.getPlayer2().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Geri sayım durdu!");
                        }
                        ItemStack isNowReady = new ItemStack(Material.RED_WOOL);
                        ItemMeta readyMeta = isNowReady.getItemMeta();
                        readyMeta.setDisplayName(ChatColor.RED + "Hazır değil");
                        isNowReady.setItemMeta(readyMeta);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                            inventory.setItem(51,isNowReady);
                        },0);

                        event.setCurrentItem(event.getCurrentItem());
                        clicker.setItemOnCursor(clicker.getItemOnCursor());
                        MainClass.classObjectMap.get(clicker.getUniqueId()).setUsingSignGUI(true);
                        MainClass.signMenu.newMenu(new ArrayList<>()).reopenIfFail().response(((player, strings) -> {
                            String amount = "";
                            for(String s : strings)
                            {
                                amount = s;
                                break;
                            }
                            String finalAmount = amount;
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                                ItemStack sign = inventory.getItem(52);
                                ItemMeta signMeta = sign.getItemMeta();
                                List<String> lore = new ArrayList<>();
                                try {
                                    if(Integer.parseInt(finalAmount) <= MainClass.classObjectMap.get(clicker.getUniqueId()).getCurrency())
                                    {
                                        lore.add(finalAmount);
                                        signMeta.setLore(lore);
                                        sign.setItemMeta(signMeta);

                                        inventory.setItem(52,sign);
                                    }
                                    else
                                    {
                                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu kadar paran bulunmuyor.");
                                    }
                                } catch (NumberFormatException e) {
                                    //Player entered a string instead of number.
                                }
                                if(player.isOnline())
                                {
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                                        player.openInventory(inventory);
                                        MainClass.classObjectMap.get(player.getUniqueId()).setUsingSignGUI(false);
                                    },0);
                                }
                            },0);
                            return true;
                        })).open((Player) event.getWhoClicked());
                    }
                    else
                    {
                        if(holder.getTradeConfirmer() != null)
                        {
                            holder.getTradeConfirmer().cancel();
                            holder.setTradeConfirmer(null);
                            if(holder.getPlayer1().isOnline())
                                holder.getPlayer1().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Geri sayım durdu!");
                            holder.getPlayer2().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Geri sayım durdu!");
                        }
                        ItemStack isNowReady = new ItemStack(Material.RED_WOOL);
                        ItemMeta readyMeta = isNowReady.getItemMeta();
                        readyMeta.setDisplayName(ChatColor.RED + "Hazır değil");
                        isNowReady.setItemMeta(readyMeta);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                            inventory.setItem(51,isNowReady);
                        },0);
                    }
                }
                else
                {
                    /*plugin.getServer().getConsoleSender().sendMessage("Current item is: " + event.getCurrentItem());
                    plugin.getServer().getConsoleSender().sendMessage("Item on cursor is: " + clicker.getItemOnCursor());
                    plugin.getServer().getConsoleSender().sendMessage("********************");*/
                    event.setCancelled(true);
                }
            }
        }
        else if(event.getView().getTitle().contains("Statlar") || event.getView().getTitle().contains("Class Info"))
        {

            if(event.getCurrentItem() == null)
                return;

            if(event.getCurrentItem().getItemMeta() == null)
                return;
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            if(event.getInventory() != event.getClickedInventory())
                return;

            ItemStack item = event.getCurrentItem();
            if(event.getView().getTitle().contains("WARRIOR Class Info"))
            {
                if(event.getSlot() == 35)
                {
                    plugin.getServer().dispatchCommand(player,"class warrior");
                    return;
                }
            }
            else if(event.getView().getTitle().contains("CLERIC Class Info"))
            {
                if(event.getSlot() == 35)
                {
                    plugin.getServer().dispatchCommand(player, "class cleric");
                    return;
                }
            }
            else if(event.getView().getTitle().contains("SUMMONER Class Info"))
            {
                if(event.getSlot() == 35)
                {
                    plugin.getServer().dispatchCommand(player, "class necromancer");
                    return;
                }
            }
        }
        else if(event.getView().getTitle().contains("SUMMON ENVANTERI"))
        {
            MMOClass mmoClass = MainClass.classObjectMap.get(event.getWhoClicked().getUniqueId());
            if(event.isShiftClick())
            {
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "It was a special click, canceling.");
                event.setCancelled(true);
                return;
            }
            if(event.getClickedInventory() != event.getInventory())
                return;
            if (mmoClass.getLevel() > 74)
            {
                if (10 < event.getSlot() && event.getSlot() < 16)
                {
                    ItemStack itemToBeChanged = event.getWhoClicked().getItemOnCursor();
                    ItemStack oldItem = event.getCurrentItem();
                    event.setCancelled(true);
                    //this.plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "ToBeChanged: " + itemToBeChanged);
                    //this.plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "OldItem: " + oldItem);
                    //this.plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Clicked inventory type: " + event.getClickedInventory().getType());
                    net.minecraft.server.v1_16_R1.ItemStack nmsOldItem;
                    if (!itemToBeChanged.getType().equals(Material.AIR))
                    {
                        nmsOldItem = CraftItemStack.asNMSCopy(itemToBeChanged);
                        try
                        {
                            nmsOldItem.getTag().setBoolean("Unbreakable", true);
                        }
                        catch (Exception e) {
                            //e.printStackTrace();
                        }
                        itemToBeChanged = CraftItemStack.asBukkitCopy(nmsOldItem);
                    }
                    else if (!oldItem.getType().equals(Material.AIR))
                    {
                        nmsOldItem = CraftItemStack.asNMSCopy(oldItem);
                        try
                        {
                            nmsOldItem.removeTag("Unbreakable");
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        oldItem = CraftItemStack.asBukkitCopy(nmsOldItem);
                    }
                    event.setCurrentItem(itemToBeChanged);
                    event.getWhoClicked().setItemOnCursor(oldItem);

                }
                else
                {
                    event.setCancelled(true);
                }
            }
            else
            {
                event.setCancelled(true);
            }
        }
        else if(event.getView().getTitle().contains("SUMMON KONTROL"))
        {
            event.setCancelled(true);
            if(event.getClickedInventory() != event.getInventory())
                return;
            switch (event.getSlot())
            {
                case 0:
                    if(Necromancer.canSpawnSummon(event.getWhoClicked().getWorld().getName()))
                        NecromancerListener.createSummon((Player) event.getWhoClicked());
                    else
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu dünyada summonını çağıramazsın.");
                    break;
                case 2:
                {
                    Necromancer necromancer = (Necromancer) MainClass.classObjectMap.get(event.getWhoClicked().getUniqueId());
                    if(necromancer.getSummon() != null)
                    {
                        necromancer.getSummon().kill();
                        necromancer.setSummon(null);
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Summonınız resetlendi.");
                    }
                    else
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + "Resetlenecek bir summonınız bulunmuyor.");
                    }
                    break;
                }

                case 4:
                    NecromancerListener.changeSummonStance((Player) event.getWhoClicked(), MobStance.type.PASSIVE);
                    break;
                case 5:
                    NecromancerListener.changeSummonStance((Player) event.getWhoClicked(), MobStance.type.OFFENSIVE);
                    break;
                case 8:
                    NecromancerListener.healSummon((Player) event.getWhoClicked());
                    break;
            }

        }
        else if(event.getView().getTitle().contains("Dungeon Menu"))
        {
            int index = event.getView().getTitle().indexOf(" ");
            String titledungeon = event.getView().getTitle().substring(0,index).toLowerCase();
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getInventory())
                return;
            if(event.getInventory().getType() == InventoryType.PLAYER)
            {
                return;
            }
            Player player = (Player) event.getWhoClicked();
            if (MainClass.classObjectMap.containsKey(player.getUniqueId()))
            {
                if (MainClass.classObjectMap.get(player.getUniqueId()).getFaction() != null)
                {
                    MMOClass mmoClass = MainClass.classObjectMap.get(player.getUniqueId());
                    Faction faction = MainClass.factionMap.get(mmoClass.getFaction().toLowerCase());
                    titledungeon = ChatColor.stripColor(titledungeon);
                    String dungeonName = "dungeon_"+titledungeon+"_";
                    //plugin.getServer().getConsoleSender().sendMessage("CLICKED DUNGEON WAS: " + dungeonName);
                    switch (event.getSlot())
                    {
                        case 9:
                            dungeonName += "0";
                            DungeonManager.dungeonClick(dungeonName,player,faction,plugin,titledungeon,mmoClass);
                            break;
                        case 11:
                            dungeonName += "1";
                            DungeonManager.dungeonClick(dungeonName,player,faction,plugin,titledungeon,mmoClass);
                            break;
                        case 13:
                            dungeonName += "2";
                            DungeonManager.dungeonClick(dungeonName,player,faction,plugin,titledungeon,mmoClass);
                            break;
                        case 15:
                            dungeonName += "3";
                            DungeonManager.dungeonClick(dungeonName,player,faction,plugin,titledungeon,mmoClass);
                            break;
                        case 17:
                            dungeonName += "4";
                            DungeonManager.dungeonClick(dungeonName,player,faction,plugin,titledungeon,mmoClass);
                            break;
                    }
                }
                else
                {
                    player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Dungeonlara katılabilmek için bir faction'a üye olmalısın.");
                }
            }
            else
            {
                player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Dungeonlara katılabilmek için bir class seçmiş olmalısın.");
            }
        }
        else if(event.getView().getTitle().contains("Faction Arama"))
        {
            event.setCancelled(true);
            if(event.getClickedInventory() != event.getInventory())
                return;

            if(event.getClickedInventory() != null && !event.getClickedInventory().getType().equals(InventoryType.PLAYER))
            {
                int clickedPosition = event.getSlot();
                ItemStack itemStack = event.getClickedInventory().getItem(clickedPosition);
                Player whoClicked = (Player)event.getWhoClicked();
                if(clickedPosition == 40)
                {
                    //Faction arama!
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    MainClass.signMenu.newMenu(new ArrayList<>()).reopenIfFail().response(((player, strings) -> {
                        String toBeSearched = "";
                        for(String s : strings)
                        {
                            toBeSearched = s;
                            break;
                        }
                        String finalToBeSearched = toBeSearched;
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                            CustomInventory.createFactionMenu((Player) event.getWhoClicked(),plugin, finalToBeSearched,0);
                        },0);
                        return true;
                    })).open((Player) event.getWhoClicked());

                }
                else if(clickedPosition == 37)
                {
                    CustomInventory.FactionInventoryHolder holder = (CustomInventory.FactionInventoryHolder)inventory.getHolder();
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    int page = holder.getPage();
                    String likeCondition = holder.getSearchQuery();
                    if(page == 0)
                    {
                        //Do nothing.
                    }
                    else
                    {
                        CustomInventory.createFactionMenu((Player) event.getWhoClicked(),plugin,likeCondition,page-1);
                    }
                }
                else if(clickedPosition == 43)
                {
                    CustomInventory.FactionInventoryHolder holder = (CustomInventory.FactionInventoryHolder)inventory.getHolder();
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    int page = holder.getPage();
                    String likeCondition = holder.getSearchQuery();
                    CustomInventory.createFactionMenu((Player) event.getWhoClicked(),plugin,likeCondition,page+1);
                }
                else if(itemStack != null && MainClass.factionMap.containsKey(ChatColor.stripColor(itemStack.getItemMeta().getDisplayName().toLowerCase())))
                {
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    plugin.getServer().dispatchCommand(event.getWhoClicked(), "faction join " + ChatColor.stripColor(itemStack.getItemMeta().getDisplayName()));
                }
            }
        }
        else if(event.getView().getTitle().contains("Isınlanma Menusu"))
        {
            event.setCancelled(true);

            Inventory menu = event.getInventory();
            if(menu != inventory)
                return;

            int slot = event.getSlot();
            Player whoClicked = (Player) event.getWhoClicked();

            switch (slot)
            {
                case 11:
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    Location destination = Bukkit.getWorld("summoner_hub").getSpawnLocation();
                    destination.setYaw(179.6f);
                    destination.setPitch(-7.2f);
                    whoClicked.teleport(destination);
                    break;
                case 13:
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    whoClicked.teleport(Bukkit.getWorld("warrior_hub").getSpawnLocation());
                    break;
                case 15:
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    Location destination2 = Bukkit.getWorld("paladin_hub").getSpawnLocation();
                    destination2.setYaw(-89.3f);
                    destination2.setPitch(2.0f);
                    whoClicked.teleport(destination2);
                    break;

            }
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event)
    {
        Inventory inv = event.getInventory();
        if(inv.getHolder() instanceof CustomInventory.TradingInventoryHolder)
        {
            Player closer = (Player) event.getPlayer();
            if(!MainClass.classObjectMap.containsKey(closer.getUniqueId()))
            {
                MMOClass.cancelTrade(inv);
                return;
            }
            MMOClass closerClass = MainClass.classObjectMap.get(closer.getUniqueId());
            if(closerClass.isUsingSignGUI())
            {
                closerClass.setUsingSignGUI(false);
            }
            else
            {
                MMOClass.cancelTrade(inv);
            }
        }
    }

    @EventHandler
    public void onItemDragEvent(InventoryDragEvent event)
    {
        Inventory inv = event.getInventory();

        if(inv.getHolder() instanceof CustomInventory.TradingInventoryHolder)
        {
            Player p = (Player)event.getWhoClicked();

            CustomInventory.TradingInventoryHolder tradeHolder = (CustomInventory.TradingInventoryHolder)inv.getHolder();

            if(p.getUniqueId().equals(tradeHolder.getPlayer1().getUniqueId()))
            {
                //Dragger is player1
                Map<Integer, ItemStack> newItems = event.getNewItems();
                for(int index : newItems.keySet())
                {
                    if(CustomInventory.AllowedTradingSlot(1,index))
                    {
                        CustomInventory.TradingInventoryHolder holder = (CustomInventory.TradingInventoryHolder)event.getInventory().getHolder();
                        if(holder.getTradeConfirmer() != null)
                        {
                            holder.getTradeConfirmer().cancel();
                            holder.setTradeConfirmer(null);
                            if(holder.getPlayer2().isOnline())
                                holder.getPlayer2().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Geri sayım durdu!");
                            holder.getPlayer1().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Geri sayım durdu!");
                        }
                        ItemStack isNowReady = new ItemStack(Material.RED_WOOL);
                        ItemMeta readyMeta = isNowReady.getItemMeta();
                        readyMeta.setDisplayName(ChatColor.RED + "Hazır değil");
                        isNowReady.setItemMeta(readyMeta);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                            event.getInventory().setItem(47,isNowReady);
                        },0);
                    }
                    else
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            else if(p.getUniqueId().equals(tradeHolder.getPlayer2().getUniqueId()))
            {
                //Dragger is player2
                Map<Integer, ItemStack> newItems = event.getNewItems();
                for(int index : newItems.keySet())
                {
                    if(CustomInventory.AllowedTradingSlot(2,index))
                    {
                        CustomInventory.TradingInventoryHolder holder = (CustomInventory.TradingInventoryHolder)event.getInventory().getHolder();
                        if(holder.getTradeConfirmer() != null)
                        {
                            holder.getTradeConfirmer().cancel();
                            holder.setTradeConfirmer(null);
                            if(holder.getPlayer1().isOnline())
                                holder.getPlayer1().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Geri sayım durdu!");
                            holder.getPlayer2().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Geri sayım durdu!");
                        }
                        ItemStack isNowReady = new ItemStack(Material.RED_WOOL);
                        ItemMeta readyMeta = isNowReady.getItemMeta();
                        readyMeta.setDisplayName(ChatColor.RED + "Hazır değil");
                        isNowReady.setItemMeta(readyMeta);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                            event.getInventory().setItem(51,isNowReady);
                        },0);
                    }
                    else
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
        else if(event.getView().getTitle().contains("Dungeon Menu"))
        {
            event.setCancelled(true);
        }
        else if(event.getView().getTitle().contains("SUMMON KONTROL"))
        {
            event.setCancelled(true);
        }
        else if(event.getView().getTitle().contains("Faction Arama"))
        {
            event.setCancelled(true);
        }
        else if(event.getView().getTitle().contains("Statlar"))
        {
            event.setCancelled(true);
        }
        else if(event.getView().getTitle().contains("Class Info"))
        {
            event.setCancelled(true);
        }
        else if(event.getView().getTitle().contains("Işınlanma Menüsü"))
        {
            event.setCancelled(true);
        }
    }
}
