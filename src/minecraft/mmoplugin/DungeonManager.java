package minecraft.mmoplugin;

import leveledmobs.*;
import minecraft.mmoplugin.customItems.CustomItems;
import minecraft.mmoplugin.customItems.LegendaryItems;
import minecraft.mmoplugin.events.DBSaver;
import minecraft.mmoplugin.events.DungeonConfigManager;
import minecraft.mmoplugin.events.OpenWorldConfigManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DungeonManager
{
    public static HashMap<String, Dungeon> dungeonMap;
    public static DungeonConfigManager config;

    public static class DungeonLootChest
    {
        private Chest chest;
        private HashMap<ItemStack, HashMap<UUID,Integer>> playerRolls;
        private List<ItemStack> items;
        private BukkitTask timer;
        private Location loc;
        private DungeonLootHolder holder;
        private Block block;

        public Block getBlock() {
            return block;
        }

        public void setBlock(Block block) {
            this.block = block;
        }

        public DungeonLootHolder getHolder() {
            return holder;
        }

        public void setHolder(DungeonLootHolder holder) {
            this.holder = holder;
        }

        public Location getLoc() {
            return loc;
        }

        public void setLoc(Location loc) {
            this.loc = loc;
        }

        public Chest getChest()
        {
            return chest;
        }

        public void setChest(Chest chest)
        {
            this.chest = chest;
        }

        public HashMap<ItemStack, HashMap<UUID,Integer>> getPlayerRolls()
        {
            return playerRolls;
        }

        public void setPlayerRolls(HashMap<ItemStack, HashMap<UUID,Integer>> playerRolls)
        {
            this.playerRolls = playerRolls;
        }

        public boolean addPlayerRoll(ItemStack item, Player roller, int roll)
        {
            if(this.playerRolls.containsKey(item))
            {
                if(this.playerRolls.get(item).containsKey(roller.getUniqueId()))
                    return false;
                this.playerRolls.get(item).put(roller.getUniqueId(),roll);
                return true;
            }
            else
            {
                HashMap<UUID,Integer> temp = new HashMap<>();
                temp.put(roller.getUniqueId(),roll);
                this.playerRolls.put(item,temp);
                return true;
            }
        }

        public List<ItemStack> getItems()
        {
            return items;
        }

        public void setItems(List<ItemStack> items)
        {
            this.items = items;
        }

        public void addAsLoot(ItemStack itemStack)
        {
            this.items.add(itemStack);
        }

        public DungeonLootChest(Location loc, List<ItemStack> items, Dungeon dungeon)
        {
            loc.getBlock().setType(Material.CHEST);
            Chest chest = (Chest) loc.getBlock().getState();
            this.block = loc.getBlock();

            //chest.getInventory().setContents(items.toArray(new ItemStack[0]));

            this.loc = loc.getBlock().getState().getLocation();
            this.chest = chest;
            this.chest.setCustomName(ChatColor.GOLD + "Dungeon dropları");

            this.items = items;
            this.playerRolls = new HashMap<>();
            createLootGUI(MainClass.getPlugin(MainClass.class),this,dungeon);
            this.timer = new BukkitRunnable() {
                @Override
                public void run() {
                    List<HumanEntity> clone = new ArrayList<>(holder.getInventory().getViewers());
                    for(HumanEntity viewer : clone)
                    {
                        viewer.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                    }
                    loc.getBlock().setType(Material.AIR);
                    for(ItemStack item : playerRolls.keySet())
                    {
                        //Find the max roll for this item:
                        UUID maxRoller = null;
                        int maxRoll = -1;
                        for(UUID roller : playerRolls.get(item).keySet())
                        {
                            if(playerRolls.get(item).get(roller) > maxRoll)
                            {
                                maxRoller = roller;
                                maxRoll = playerRolls.get(item).get(roller);
                            }
                        }
                        OfflinePlayer winner = Bukkit.getOfflinePlayer(maxRoller);
                        if(winner.isOnline())
                        {
                            winner.getPlayer().getInventory().addItem(item);
                        }
                        else
                        {
                            BukkitTask insertIntoVault = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    try {
                                        PreparedStatement ps = MainClass.conn.prepareStatement("select count(*) as cnt from player_boughtItems group by player_id");
                                        ResultSet countRS = ps.executeQuery();
                                        int howManyItems = 0;
                                        if(countRS.next())
                                        {
                                            howManyItems = countRS.getInt("cnt");
                                        }
                                        ps.close();

                                        ps = MainClass.conn.prepareStatement("insert into player_boughtItems values (?,?,?)");
                                        ps.setString(1, String.valueOf(winner.getUniqueId()));
                                        ps.setString(2, DBSaver.toBase64(item));
                                        ps.setInt(3,((howManyItems/21)+1));
                                        ps.executeUpdate();
                                        ps.close();
                                    }
                                    catch (SQLException throwables)
                                    {
                                        throwables.printStackTrace();
                                    }
                                }
                            }.runTaskLaterAsynchronously(MainClass.getPlugin(MainClass.class),0);
                        }
                        for(Player inDungeon : dungeonMap.get(holder.getLootChest().getLoc().getWorld().getName()).getPlayerList())
                        {
                            inDungeon.sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + winner.getPlayer().getDisplayName() + ", " + ChatColor.GOLD +item.getItemMeta().getDisplayName() + ChatColor.GREEN + " adlı eşyayı kazandı.");
                        }
                    }
                    dungeonMap.get(loc.getWorld().getName()).setLoot(null);
                }
            }.runTaskLater(MainClass.getPlugin(MainClass.class),50*20);
        }

        public static void createLootGUI(Plugin plugin, DungeonManager.DungeonLootChest lootChest, DungeonManager.Dungeon dungeon)
        {
            //TODO Create a GUI with the given items.
            List<ItemStack> drops = lootChest.getItems();

            DungeonManager.DungeonLootHolder lootHolder = new DungeonManager.DungeonLootHolder(dungeon,lootChest);

            Inventory inv = Bukkit.createInventory(lootHolder, InventoryType.CHEST,ChatColor.DARK_AQUA + "Dungeon Dropları");

            lootHolder.setInventory(inv);

            lootChest.setHolder(lootHolder);


            ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta fillerMeta = filler.getItemMeta();
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);

            for(int i = 0; i < 10; i++)
            {
                inv.setItem(i,filler);
            }

            for(int i = 17; i < 27; i++)
            {
                inv.setItem(i,filler);
            }

            for(int i = 0; i < 7 && i < drops.size(); i++)
            {
                //Before setting the item, first create a copy of it.
                ItemStack copy = drops.get(i);
                //Check if anyone has rolled on this item
                if (lootChest.getPlayerRolls().containsKey(drops.get(i)))
                {
                    //Get it's Meta:
                    ItemMeta copyMeta = copy.getItemMeta();
                    //Check if it already has lore.
                    List<String> lore = copyMeta.getLore();
                    if(lore == null)
                        lore = new ArrayList<>(); //If it doesnt have lore, just create a new one.
                    lore.add("");
                    for(UUID uuid : lootChest.getPlayerRolls().get(drops.get(i)).keySet())
                    {
                        OfflinePlayer roller = Bukkit.getOfflinePlayer(uuid);
                        lore.add(ChatColor.YELLOW + roller.getName() + ": " + ChatColor.BLUE + lootChest.getPlayerRolls().get(drops.get(i)).get(uuid));
                    }
                    copyMeta.setLore(lore);
                    copy.setItemMeta(copyMeta);
                }
                inv.setItem(10+i,copy);
            }
        }
    }


    public static class Dungeon
    {
        private Plugin plugin;
        private List<Player> playerList;
        private String name;
        private DungeonLootChest loot;
        private boolean inProgress;
        private Player partyOwner;
        private BukkitTask kickPlayers;

        public BukkitTask getKickPlayers() {
            return kickPlayers;
        }

        public void setKickPlayers(BukkitTask kickPlayers) {
            this.kickPlayers = kickPlayers;
        }

        public DungeonLootChest getLoot() {
            return loot;
        }

        public void setLoot(DungeonLootChest loot) {
            this.loot = loot;
        }

        public List<Player> getPlayerList() {
            return playerList;
        }

        public void setPlayerList(List<Player> playerList) {
            this.playerList = playerList;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isInProgress() {
            return inProgress;
        }

        public void setInProgress(boolean inProgress) {
            this.inProgress = inProgress;
        }

        public Player getPartyOwner() {
            return partyOwner;
        }

        public void setPartyOwner(Player partyOwner) {
            this.partyOwner = partyOwner;
        }

        Dungeon(Plugin plugin, Player partyOwner, String name)
        {
            this.plugin = plugin;
            this.partyOwner = partyOwner;
            this.name = name;
            this.playerList = new ArrayList<>();
            this.loot = null;
            this.kickPlayers = null;
        }
    }

    public static void dungeonClick(String dungeon, Player player, Faction faction, Plugin plugin, String splittedName, MMOClass mmoClass)
    {
        if (!DungeonManager.dungeonMap.get(dungeon).isInProgress() && MainClass.classObjectMap.get(player.getUniqueId()).getCurrentDungeon().equals(""))
        {

            int level = MainClass.classObjectMap.get(player.getUniqueId()).getLevel();
            switch (splittedName)
            {
                case "nether":
                    if(level < 40)
                    {
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Nether dungeon'ına katılabilmek için 40 seviyenin üstünde olmalısın.");
                        return;
                    }
                    break;
                case "end":
                    if(level < 80)
                    {
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "End dungeon'ına katılabilmek için 80 seviyenin üstünde olmalısın.");
                        return;
                    }
                    break;
            }

            if(MainClass.playerCooldownMap.get(player.getUniqueId()).containsKey(splittedName) && MainClass.playerCooldownMap.get(player.getUniqueId()).get(splittedName).isAfterNow())
            {
                player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu dungeona katılmak için, " + ChatColor.YELLOW +Minutes.minutesBetween(new DateTime(),MainClass.playerCooldownMap.get(player.getUniqueId()).get(splittedName)).getMinutes() + "dk " + ChatColor.RED + "beklemen gerek.");
                return;
            }

            player.playSound(player.getLocation(),Sound.BLOCK_ENCHANTMENT_TABLE_USE,1.0f,1.0f);

            if(DungeonManager.dungeonMap.get(dungeon).getPlayerList().size() == 0)
            {
                DungeonManager.dungeonMap.get(dungeon).getPlayerList().add(player);
                MainClass.classObjectMap.get(player.getUniqueId()).setCurrentDungeon(dungeon);
                TextComponent clickToJoin = new TextComponent(MainClass.getPluginPrefix() + ChatColor.GREEN + ChatColor.UNDERLINE+ "Katılmak için tıklayın!");
                clickToJoin.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + DungeonManager.dungeonMap.get(dungeon).getPlayerList().get(0).getName()));
                clickToJoin.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.WHITE + "" + ChatColor.ITALIC + "Şu anki oyuncu sayısı: " + DungeonManager.dungeonMap.get(dungeon).getPlayerList().size() + "/3").create()));
                for(Player member : faction.getFactionMembers())
                {
                    member.sendMessage(MainClass.getPluginPrefix() + "Sizle aynı factionda olan, " + ChatColor.BLUE + player.getName() + ChatColor.WHITE + " adlı oyuncu "+splittedName+" dungeon'ı başlatmak istiyor.");
                    member.sendMessage(MainClass.getPluginPrefix() + "Oyuncu sayısı: 1/3");
                    member.spigot().sendMessage(clickToJoin);
                }
                BukkitTask task = new BukkitRunnable() {
                    int timer = 0;
                    @Override
                    public void run() {
                        clickToJoin.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.WHITE + "" + ChatColor.ITALIC + "Şu anki oyuncu sayısı: " + DungeonManager.dungeonMap.get(dungeon).getPlayerList().size() + "/3").create()));
                        if(DungeonManager.dungeonMap.get(dungeon).getPlayerList().size() == 3)
                        {
                            cancel();
                        }
                        else if(DungeonManager.dungeonMap.get(dungeon).getPlayerList().size() == 0)
                        {
                            for(Player m : MainClass.factionMap.get(mmoClass.getFaction().toLowerCase()).getFactionMembers())
                            {
                                m.sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + player.getName() + ChatColor.WHITE + " adlı oyuncunun başlatmaya çalıştığı "+splittedName+" dungeon yeterli kişi katılmadığından dolayı iptal oldu.");
                            }
                            for(Player triedToJoin : DungeonManager.dungeonMap.get(dungeon).getPlayerList())
                            {
                                MainClass.classObjectMap.get(triedToJoin.getUniqueId()).setCurrentDungeon("");
                            }
                            cancel();
                        }
                        if (timer == 60)
                        {
                            if(DungeonManager.dungeonMap.get(dungeon).getPlayerList().size() < 3)
                            {
                                for(Player m : MainClass.factionMap.get(mmoClass.getFaction().toLowerCase()).getFactionMembers())
                                {
                                    m.sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + player.getName() + ChatColor.WHITE + " adlı oyuncunun başlatmaya çalıştığı "+splittedName+" dungeon yeterli kişi katılmadığından dolayı iptal oldu.");
                                }
                                for(Player triedToJoin : DungeonManager.dungeonMap.get(dungeon).getPlayerList())
                                {
                                    MainClass.classObjectMap.get(triedToJoin.getUniqueId()).setCurrentDungeon("");
                                }
                                DungeonManager.dungeonMap.get(dungeon).getPlayerList().clear();
                            }
                            cancel();
                        }
                        if (timer == 30)
                        {
                            for(Player m : MainClass.factionMap.get(mmoClass.getFaction().toLowerCase()).getFactionMembers())
                            {
                                m.sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + player.getName() + ChatColor.WHITE + " adlı oyuncu "+splittedName+" dungeon'ı başlatmak istiyor. Son " + ChatColor.RED + "30 " + ChatColor.WHITE + "saniye.");
                                m.sendMessage(MainClass.getPluginPrefix() + "Oyuncu sayısı: " + DungeonManager.dungeonMap.get(dungeon).getPlayerList().size() + "/3");
                                m.spigot().sendMessage(clickToJoin);
                            }
                        }
                        if (timer == 45)
                        {
                            for(Player m : MainClass.factionMap.get(mmoClass.getFaction().toLowerCase()).getFactionMembers())
                            {
                                m.sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + player.getName() + ChatColor.WHITE + " adlı oyuncu "+splittedName+" dungeon'ı başlatmak istiyor. Son " + ChatColor.RED + "15 " + ChatColor.WHITE + "saniye.");
                                m.sendMessage(MainClass.getPluginPrefix() + "Oyuncu sayısı: " + DungeonManager.dungeonMap.get(dungeon).getPlayerList().size() + "/3");
                                m.spigot().sendMessage(clickToJoin);
                            }
                        }
                        if (timer == 15)
                        {
                            for(Player m : MainClass.factionMap.get(mmoClass.getFaction().toLowerCase()).getFactionMembers())
                            {
                                m.sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + player.getName() + ChatColor.WHITE + " adlı oyuncu "+splittedName+" dungeon'ı başlatmak istiyor. Son " + ChatColor.RED + "45 " + ChatColor.WHITE + "saniye.");
                                m.sendMessage(MainClass.getPluginPrefix() + "Oyuncu sayısı: " + DungeonManager.dungeonMap.get(dungeon).getPlayerList().size() + "/3");
                                m.spigot().sendMessage(clickToJoin);
                            }
                        }
                        timer++;
                    }
                }.runTaskTimer(plugin,0,20);
            }
            else if(DungeonManager.dungeonMap.get(dungeon).getPlayerList().size() < 3)
            {
                DungeonManager.joinDungeon(faction,player,dungeon, splittedName);
            }
        }
        else
        {
            if(DungeonManager.dungeonMap.get(dungeon).isInProgress())
                player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu dungeon şuanda devam etmekte.");
            if(!MainClass.classObjectMap.get(player.getUniqueId()).getCurrentDungeon().equals(""))
                player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "2 defa dungeon'a katılamazsın.");
        }
    }

    public static void joinDungeon(Faction faction, Player player, String dungeonName, String splittedName)
    {
        MMOClass mmoClass = MainClass.classObjectMap.get(player.getUniqueId());
        String factionName = MainClass.classObjectMap.get(DungeonManager.dungeonMap.get(dungeonName).getPlayerList().get(0).getUniqueId()).getFaction();

        if(MainClass.playerCooldownMap.get(player.getUniqueId()).containsKey(splittedName) && MainClass.playerCooldownMap.get(player.getUniqueId()).get(splittedName).isAfterNow())
        {
            player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu dungeona katılmak için, " + ChatColor.YELLOW +Minutes.minutesBetween(new DateTime(),MainClass.playerCooldownMap.get(player.getUniqueId()).get(splittedName)).getMinutes() + "dk " + ChatColor.RED + "beklemen gerek.");
            return;
        }
        if(factionName.equals(faction.getFactionName()))
        {
            int level = MainClass.classObjectMap.get(player.getUniqueId()).getLevel();
            switch (splittedName)
            {
                case "nether":
                    if(level < 40)
                    {
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Nether dungeon'ına katılabilmek için 40 seviyenin üstünde olmalısın.");
                        return;
                    }
                    break;
                case "end":
                    if(level < 80)
                    {
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "End dungeon'ına katılabilmek için 80 seviyenin üstünde olmalısın.");
                        return;
                    }
                    break;
            }
            player.playSound(player.getLocation(),Sound.BLOCK_ENCHANTMENT_TABLE_USE,1.0f,1.0f);
            DungeonManager.dungeonMap.get(dungeonName).getPlayerList().add(player);
            MainClass.classObjectMap.get(player.getUniqueId()).setCurrentDungeon(dungeonName);
            for(Player p : faction.getFactionMembers())
            {
                p.sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + player.getName() + ChatColor.WHITE + " adlı oyuncu "+ ChatColor.BLUE + DungeonManager.dungeonMap.get(dungeonName).getPlayerList().get(0).getName() + ChatColor.WHITE + " adlı oyuncunun " +splittedName+" dungeon partisine katıldı.");
                p.sendMessage(MainClass.getPluginPrefix() + "Oyuncu sayısı: " + DungeonManager.dungeonMap.get(dungeonName).getPlayerList().size() + "/3");
            }
            if(DungeonManager.dungeonMap.get(dungeonName).getPlayerList().size() == 3)
            {
                boolean readyToTeleport = true;
                String offlinePlayerName = "";
                for(Player partyMember : DungeonManager.dungeonMap.get(dungeonName).getPlayerList())
                {
                    if(!partyMember.isOnline())
                    {
                        readyToTeleport = false;
                        offlinePlayerName = partyMember.getName();
                        break;
                    }
                }
                if(readyToTeleport)
                {
                    DungeonManager.dungeonMap.get(dungeonName).setInProgress(true);
                    Plugin plugin = MainClass.getPlugin(MainClass.class);
                    String worldName = dungeonName;
                    DungeonManager.config.reloadConfig();
                    DungeonManager.config.getConfig().contains(worldName);
                    DungeonManager.config.getConfig().getConfigurationSection(worldName).getKeys(false).forEach(dungeonMob->{
                        int mobLevel = DungeonManager.config.getConfig().getInt(worldName+"."+dungeonMob+".level");
                        Location loc = DungeonManager.config.getConfig().getLocation(worldName+"."+dungeonMob+".location");
                        switch (DungeonManager.config.getConfig().getString(worldName+"."+dungeonMob+".type").toLowerCase())
                        {
                            case "husk":
                                LeveledHusk husk = new LeveledHusk(loc.getWorld(),mobLevel, loc);
                                break;
                            case "stray":
                                LeveledStray stray = new LeveledStray(loc.getWorld(),mobLevel, loc);
                                break;
                            case "blaze":
                                LeveledBlaze blaze = new LeveledBlaze(loc.getWorld(),mobLevel, loc);
                                break;
                            case "witherskeleton":
                                LeveledWitherSkeleton witherSkeleton = new LeveledWitherSkeleton(loc.getWorld(),mobLevel, loc);
                                break;
                            case "magmacube":
                                LeveledMagmaCube magmaCube = new LeveledMagmaCube(loc.getWorld(),mobLevel,loc, plugin);
                                break;
                            case "zombie":
                                LeveledZombie leveledZombie = new LeveledZombie(loc.getWorld(),mobLevel,loc);
                                break;
                            case "spider":
                                LeveledSpider leveledSpider = new LeveledSpider(loc.getWorld(),mobLevel,loc);
                                break;
                            case "skeleton":
                                LeveledSkeleton leveledSkeleton = new LeveledSkeleton(loc.getWorld(),mobLevel,loc);
                                break;
                            case "witch":
                                LeveledWitch leveledWitch = new LeveledWitch(loc.getWorld(),mobLevel,loc);
                                break;
                            case "elitegiant":
                                EliteGiant eliteGiant = new EliteGiant(loc.getWorld(),mobLevel,loc,plugin);
                                break;
                            case "pillager":
                                LeveledPillager leveledPillager = new LeveledPillager(loc.getWorld(),mobLevel,loc);
                                break;
                            case "evoker":
                                LeveledEvoker leveledEvoker = new LeveledEvoker(loc.getWorld(),mobLevel,loc);
                                break;
                            case "illusioner":
                                LeveledIllusioner leveledIllusioner = new LeveledIllusioner(loc.getWorld(),mobLevel,loc);
                                break;
                            case "ravager":
                                LeveledRavager leveledRavager = new LeveledRavager(loc.getWorld(),mobLevel,loc);
                                break;
                            case "vindicator":
                                LeveledVindicator leveledVindicator = new LeveledVindicator(loc.getWorld(),mobLevel,loc);
                                break;
                            case "wither":
                                LeveledWither leveledWither = new LeveledWither(loc.getWorld(),mobLevel,loc,plugin);
                                break;
                            case "witheradd":
                                LeveledWitherSkeletonWithHorse add = new LeveledWitherSkeletonWithHorse(loc.getWorld(),mobLevel,loc);
                                break;
                        }
                    });
                    Location tpLoc = plugin.getConfig().getLocation("DungeonSpawns."+dungeonName+".loc");;
                    for(Player partyMember : DungeonManager.dungeonMap.get(dungeonName).getPlayerList())
                    {
                        partyMember.sendMessage(MainClass.getPluginPrefix() + "Parti doldu! Dungeon'a ışınlanıyorsunuz.");
                        partyMember.sendMessage(MainClass.getPluginPrefix() + "Dungeon'ı tamamlamak için 40dk'nız var.");
                        MainClass.playerCooldownMap.get(partyMember.getUniqueId()).put(splittedName,new DateTime().plusMinutes(45));
                        if(tpLoc == null)
                            partyMember.teleport(Bukkit.getWorld(dungeonName).getSpawnLocation());
                        else
                            partyMember.teleport(tpLoc);
                        int index = dungeonName.lastIndexOf("_");
                        String correctedDungeonName = dungeonName.substring(0,index);
                        correctedDungeonName = correctedDungeonName.replace("_", " ").toUpperCase();
                        ActionBarUtil.sendTitle(partyMember,ChatColor.RED + correctedDungeonName, "",3*20,2*20,3*20);
                        partyMember.playSound((Bukkit.getWorld(dungeonName).getSpawnLocation()), Sound.AMBIENT_CAVE, SoundCategory.AMBIENT,1.0f,1.0f);
                    }
                    BukkitTask kickPlayers = new BukkitRunnable() {
                        @Override
                        public void run() {
                            clearDungeon(dungeonName);
                        }
                    }.runTaskLater(plugin,40*20*60);

                    dungeonMap.get(dungeonName).setKickPlayers(kickPlayers);
                }
                else
                {
                    for(Player m : MainClass.factionMap.get(mmoClass.getFaction().toLowerCase()).getFactionMembers())
                    {
                        m.sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + offlinePlayerName + ChatColor.WHITE + " adlı oyuncu şuan Online olmadığından dolayı, " + ChatColor.BLUE + DungeonManager.dungeonMap.get(dungeonName).getPlayerList().get(0).getName() + ChatColor.WHITE + " adlı oyuncunun başlatmaya çalıştığı "+splittedName+" dungeon iptal oldu.");
                    }
                    for(Player triedToJoin : DungeonManager.dungeonMap.get(dungeonName).getPlayerList())
                    {
                        MainClass.classObjectMap.get(triedToJoin.getUniqueId()).setCurrentDungeon("");
                    }
                    DungeonManager.dungeonMap.get(dungeonName).getPlayerList().clear();
                }
            }
        }
        else
        {
            player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu dungeonda olan oyuncuyla aynı factionda değilsiniz.");
            return;
        }
    }

    public static void clearDungeon(String dungeonName)
    {
        Dungeon dungeon = dungeonMap.get(dungeonName);
        for(Player dungeonPlayer : dungeon.getPlayerList())
        {
            MMOClass dungeonClass = MainClass.classObjectMap.get(dungeonPlayer.getUniqueId());
            dungeonClass.setCurrentDungeon("");
            dungeonPlayer.teleport(Bukkit.getWorld("world").getSpawnLocation());
        }
        for(Entity e : Bukkit.getWorld(dungeonName).getEntities())
        {
            e.remove();
        }
        dungeon.setInProgress(false);
        dungeon.setPartyOwner(null);
        dungeon.getPlayerList().clear();
    }

    public static class DungeonLootHolder implements InventoryHolder
    {
        DungeonManager.Dungeon dungeon;
        DungeonManager.DungeonLootChest lootChest;
        Inventory inventory;

        public DungeonLootHolder(DungeonManager.Dungeon dungeon, DungeonManager.DungeonLootChest lootChest)
        {
            this.dungeon = dungeon;
            this.lootChest = lootChest;
        }

        public DungeonManager.Dungeon getDungeon() {
            return dungeon;
        }

        public void setDungeon(DungeonManager.Dungeon dungeon) {
            this.dungeon = dungeon;
        }

        public DungeonManager.DungeonLootChest getLootChest() {
            return lootChest;
        }

        public void setLootChest(DungeonManager.DungeonLootChest lootChest) {
            this.lootChest = lootChest;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

    }

    public static List<ItemStack> generateEliteGiantDrops()
    {
        List<ItemStack> items = new ArrayList<>();
        Random rand = new Random();
        int roll = rand.nextInt(100);
        if(roll < 30) // 0-29 30% chance. Common stone axe.
        {
            items.add(CustomItems.createCustomItem(Material.STONE_AXE, "Taş Balta", CustomItems.Enderlik.SIRADAN, true,CustomItems.CustomItemStats.commonStoneAxeDamage,CustomItems.CustomItemStats.commonStoneAxeSpeed));
        }
        else if(roll < 50) // 30-49 20% chance. Uncommon stone axe.
        {
            items.add(CustomItems.createCustomItem(Material.STONE_AXE, "Taş Balta", CustomItems.Enderlik.NADIR, true,CustomItems.CustomItemStats.commonStoneAxeDamage+1,CustomItems.CustomItemStats.commonStoneAxeSpeed));
        }
        else if(roll < 70) // 50-69 20% chance. Common iron axe.
        {
            items.add(CustomItems.createCustomItem(Material.IRON_AXE, "Demir Balta", CustomItems.Enderlik.SIRADAN, true,CustomItems.CustomItemStats.commonIronAxeDamage,CustomItems.CustomItemStats.commonIronAxeSpeed));
        }
        else if(roll < 80) // 70-79 10% chance. Uncommon iron axe.
        {
            items.add(CustomItems.createCustomItem(Material.IRON_AXE, "Demir Balta", CustomItems.Enderlik.NADIR, true,CustomItems.CustomItemStats.commonIronAxeDamage+1,CustomItems.CustomItemStats.commonIronAxeSpeed));
        }
        else if(roll < 85) // 80-84 5% chance. Rare Iron axe.
        {
            items.add(CustomItems.createCustomItem(Material.IRON_AXE, "Demir Balta", CustomItems.Enderlik.ESSIZ, true,CustomItems.CustomItemStats.commonIronAxeDamage+2,CustomItems.CustomItemStats.commonIronAxeSpeed));
        }
        //15 % Chance of nothing.
        roll = rand.nextInt(100);
        if(roll < 30) // 0-29 30% chance. Common stone sword.
        {
            items.add(CustomItems.createCustomItem(Material.STONE_SWORD, "Taş Kılıç", CustomItems.Enderlik.SIRADAN, true,CustomItems.CustomItemStats.commonStoneSwordDamage,CustomItems.CustomItemStats.commonStoneSwordSpeed));
        }
        else if(roll < 50) // 30-49 20% chance. Uncommon stone sword.
        {
            items.add(CustomItems.createCustomItem(Material.STONE_SWORD, "Taş Kılıç", CustomItems.Enderlik.NADIR, true,CustomItems.CustomItemStats.commonStoneSwordDamage+1,CustomItems.CustomItemStats.commonStoneSwordSpeed));
        }
        else if(roll < 70) // 50-69 20% chance. Common iron sword.
        {
            items.add(CustomItems.createCustomItem(Material.IRON_SWORD, "Demir Kılıç", CustomItems.Enderlik.SIRADAN, true,CustomItems.CustomItemStats.commonIronSwordDamage,CustomItems.CustomItemStats.commonIronSwordSpeed));
        }
        else if(roll < 80) // 70-79 10% chance. Uncommon iron sword.
        {
            items.add(CustomItems.createCustomItem(Material.IRON_SWORD, "Demir Kılıç", CustomItems.Enderlik.NADIR, true,CustomItems.CustomItemStats.commonIronSwordDamage+1,CustomItems.CustomItemStats.commonIronSwordSpeed));
        }
        else if(roll < 85) // 80-84 5% chance. Rare Iron sword.
        {
            items.add(CustomItems.createCustomItem(Material.IRON_SWORD, "Demir Kılıç", CustomItems.Enderlik.ESSIZ, true,CustomItems.CustomItemStats.commonIronSwordDamage+2,CustomItems.CustomItemStats.commonIronSwordSpeed));
        }
        else if(roll < 86) // 85 1% chance. Legendary sword.
        {
            items.add(LegendaryItems.createYoshiramaru());
        }
        //15 % Chance of nothing.
        /*
        *
        *
        *  ARMOR DROPS !!!
        *
        * */

        //Helmets below:
        roll = rand.nextInt(100);
        if(roll < 30) // 0-29 30% chance. Common chain Helmet
        {
            items.add(CustomItems.createCustomItem(Material.CHAINMAIL_HELMET,"Zincirli Kask", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonChainHelmetArmor,CustomItems.CustomItemStats.commonChainHelmetToughness, EquipmentSlot.HEAD,null));
        }
        else if(roll < 50) // 30-49 20% chance. Uncommon chain Helmet
        {
            items.add(CustomItems.createCustomItem(Material.CHAINMAIL_HELMET,"Zincirli Kask", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonChainHelmetArmor+1,CustomItems.CustomItemStats.commonChainHelmetToughness, EquipmentSlot.HEAD,null));
        }
        else if(roll < 70) // 50-69 20% chance. Common Iron Helmet
        {
            items.add(CustomItems.createCustomItem(Material.IRON_HELMET,"Demir Kask", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonIronHelmetArmor,CustomItems.CustomItemStats.commonIronHelmetToughness, EquipmentSlot.HEAD,null));
        }
        else if(roll < 80) // 70-79 10% chance. Uncommon Iron Helmet
        {
            items.add(CustomItems.createCustomItem(Material.IRON_HELMET,"Demir Kask", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonIronHelmetArmor,CustomItems.CustomItemStats.commonIronHelmetToughness+1, EquipmentSlot.HEAD,null));
        }
        else if(roll < 85) // 80-84 5% chance. Rare Iron Helmet
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.IRON_HELMET,"Demir Kask", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonIronHelmetArmor,CustomItems.CustomItemStats.commonIronHelmetToughness+1, EquipmentSlot.HEAD,temp));
        }
        // 15% chance of nothing.


        //ChestPlate below:
        roll = rand.nextInt(100);
        if(roll < 30) // 0-29 30% chance. Common chain
        {
            items.add(CustomItems.createCustomItem(Material.CHAINMAIL_CHESTPLATE,"Zincirli Göğüslük", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonChainPlateArmor,CustomItems.CustomItemStats.commonChainPlateToughness, EquipmentSlot.CHEST,null));
        }
        else if(roll < 50) // 30-49 20% chance. Uncommon chain
        {
            items.add(CustomItems.createCustomItem(Material.CHAINMAIL_CHESTPLATE,"Zincirli Göğüslük", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonChainPlateArmor+1,CustomItems.CustomItemStats.commonChainPlateToughness, EquipmentSlot.CHEST,null));
        }
        else if(roll < 70) // 50-69 20% chance. Common Iron
        {
            items.add(CustomItems.createCustomItem(Material.IRON_CHESTPLATE,"Demir Göğüslük", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonIronPlateArmor,CustomItems.CustomItemStats.commonIronPlateToughness, EquipmentSlot.CHEST,null));
        }
        else if(roll < 80) // 70-79 10% chance. Uncommon Iron
        {
            items.add(CustomItems.createCustomItem(Material.IRON_CHESTPLATE,"Demir Göğüslük", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonIronPlateArmor,CustomItems.CustomItemStats.commonIronPlateToughness+1, EquipmentSlot.CHEST,null));
        }
        else if(roll < 85) // 80-84 5% chance. Rare Iron
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.IRON_CHESTPLATE,"Demir Göğüslük", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonIronPlateArmor,CustomItems.CustomItemStats.commonIronPlateToughness+1, EquipmentSlot.CHEST,temp));
        }
        // 15% chance of nothing.

        //Leggings below:
        roll = rand.nextInt(100);
        if(roll < 30) // 0-29 30% chance. Common chain
        {
            items.add(CustomItems.createCustomItem(Material.CHAINMAIL_LEGGINGS,"Zincirli Pantalon", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonChainLeggingsArmor,CustomItems.CustomItemStats.commonChainLeggingsToughness, EquipmentSlot.LEGS,null));
        }
        else if(roll < 50) // 30-49 20% chance. Uncommon chain
        {
            items.add(CustomItems.createCustomItem(Material.CHAINMAIL_LEGGINGS,"Zincirli Pantalon", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonChainLeggingsArmor+1,CustomItems.CustomItemStats.commonChainLeggingsToughness, EquipmentSlot.LEGS,null));
        }
        else if(roll < 70) // 50-69 20% chance. Common Iron
        {
            items.add(CustomItems.createCustomItem(Material.IRON_LEGGINGS,"Demir Pantalon", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonIronLeggingsArmor,CustomItems.CustomItemStats.commonIronLeggingsToughness, EquipmentSlot.LEGS,null));
        }
        else if(roll < 80) // 70-79 10% chance. Uncommon Iron
        {
            items.add(CustomItems.createCustomItem(Material.IRON_LEGGINGS,"Demir Pantalon", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonIronLeggingsArmor,CustomItems.CustomItemStats.commonIronLeggingsToughness+1, EquipmentSlot.LEGS,null));
        }
        else if(roll < 85) // 80-84 5% chance. Rare Iron
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.IRON_LEGGINGS,"Demir Pantalon", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonIronLeggingsArmor,CustomItems.CustomItemStats.commonIronLeggingsToughness+1, EquipmentSlot.LEGS,temp));
        }
        // 15% chance of nothing.


        //Boots below:
        roll = rand.nextInt(100);
        if(roll < 30) // 0-29 30% chance. Common chain
        {
            items.add(CustomItems.createCustomItem(Material.CHAINMAIL_BOOTS,"Zincirli Bot", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonChainBootsArmor,CustomItems.CustomItemStats.commonChainBootsToughness, EquipmentSlot.FEET,null));
        }
        else if(roll < 50) // 30-49 20% chance. Uncommon chain
        {
            items.add(CustomItems.createCustomItem(Material.CHAINMAIL_BOOTS,"Zincirli Bot", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonChainBootsArmor+1,CustomItems.CustomItemStats.commonChainBootsToughness, EquipmentSlot.FEET,null));
        }
        else if(roll < 70) // 50-69 20% chance. Common Iron
        {
            items.add(CustomItems.createCustomItem(Material.IRON_BOOTS,"Demir Bot", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonIronBootsArmor,CustomItems.CustomItemStats.commonIronBootsToughness, EquipmentSlot.FEET,null));
        }
        else if(roll < 80) // 70-79 10% chance. Uncommon Iron
        {
            items.add(CustomItems.createCustomItem(Material.IRON_BOOTS,"Demir Bot", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonIronBootsArmor,CustomItems.CustomItemStats.commonIronBootsToughness+1, EquipmentSlot.FEET,null));
        }
        else if(roll < 85) // 80-84 5% chance. Rare Iron
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.IRON_BOOTS,"Demir Bot", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonIronBootsArmor,CustomItems.CustomItemStats.commonIronBootsToughness+1, EquipmentSlot.FEET,temp));
        }
        // 15% chance of nothing.

        //Healing drops below:
        roll = rand.nextInt(100);
        if(roll < 50) // 0-49 50% chance. Common Healing.
        {
            items.add(CustomItems.createCustomPotion("Ani İyileştirme İksiri", PotionType.INSTANT_HEAL, CustomItems.Enderlik.SIRADAN,Material.POTION));
        }
        else if(roll < 85) // 50-84 35% chance. Uncommon Healing.
        {
            items.add(CustomItems.createCustomPotion("Ani İyileştirme İksiri", PotionType.INSTANT_HEAL, CustomItems.Enderlik.NADIR,Material.POTION));
        }
        else if(roll < 95) // 85-94 10% chance. Rare healing Healing.
        {
            items.add(CustomItems.createCustomPotion("Ani İyileştirme İksiri", PotionType.INSTANT_HEAL, CustomItems.Enderlik.ESSIZ,Material.POTION));
        }
        else if(roll < 100) // 95-99 5% chance. Epic Healing.
        {
            items.add(CustomItems.createCustomPotion("Ani İyileştirme İksiri", PotionType.INSTANT_HEAL, CustomItems.Enderlik.OLAGANUSTU,Material.POTION));
        }

        return items;
    }

    public static List<ItemStack> generateMagmaCubeDrops()
    {
        List<ItemStack> items = new ArrayList<>();
        Random rand = new Random();
        int roll = rand.nextInt(100);

        if(roll < 25) //0-24 25% chance. Uncommon Iron Sword
        {
            items.add(CustomItems.createCustomItem(Material.IRON_SWORD,"Demir Kılıç", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonIronSwordDamage+1,CustomItems.CustomItemStats.commonIronSwordSpeed));
        }
        else if(roll < 40) //25-39 15% chance. Rare Iron Sword
        {
            items.add(CustomItems.createCustomItem(Material.IRON_SWORD,"Demir Kılıç", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonIronSwordDamage+2,CustomItems.CustomItemStats.commonIronSwordSpeed));
        }
        else if(roll < 65) //40-64 25% chance. Common Diamond Sword
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_SWORD,"Elmas Kılıç", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonDiamondSwordDamage,CustomItems.CustomItemStats.commonDiamondSwordSpeed));
        }
        else if(roll < 80) //65-79 15% chance. Uncommon Diamond Sword
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_SWORD,"Elmas Kılıç", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonDiamondSwordDamage+1,CustomItems.CustomItemStats.commonDiamondSwordSpeed));
        }
        else if(roll < 90) //80-89 10% chance. Rare Diamond Sword
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_SWORD,"Elmas Kılıç", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonDiamondSwordDamage+2,CustomItems.CustomItemStats.commonDiamondSwordSpeed));
        }
        else if(roll < 95) //90-94 5% chance. Epic Diamond Sword
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_SWORD,"Elmas Kılıç", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonDiamondSwordDamage+3,CustomItems.CustomItemStats.commonDiamondSwordSpeed));
        }
        //95-99 5% chance of nothing.

        //Axe drops
        roll = rand.nextInt(100);
        if(roll < 25) //0-24 25% chance. Uncommon Iron Axe
        {
            items.add(CustomItems.createCustomItem(Material.IRON_AXE,"Demir Balta", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonIronAxeDamage+1,CustomItems.CustomItemStats.commonIronAxeSpeed));
        }
        else if(roll < 40) //25-39 15% chance. Rare Iron Axe
        {
            items.add(CustomItems.createCustomItem(Material.IRON_AXE,"Demir Balta", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonIronAxeDamage+2,CustomItems.CustomItemStats.commonIronAxeSpeed));
        }
        else if(roll < 65) //40-64 25% chance. Common Diamond Axe
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_AXE,"Elmas Balta", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonDiamondAxeDamage,CustomItems.CustomItemStats.commonDiamondAxeSpeed));
        }
        else if(roll < 80) //65-79 15% chance. Uncommon Diamond Axe
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_AXE,"Elmas Balta", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonDiamondAxeDamage+1,CustomItems.CustomItemStats.commonDiamondAxeSpeed));
        }
        else if(roll < 90) //80-89 10% chance. Rare Diamond Axe
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_AXE,"Elmas Balta", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonDiamondAxeDamage+2,CustomItems.CustomItemStats.commonDiamondAxeSpeed));
        }
        else if(roll < 95) //90-94 5% chance. Epic Diamond Axe
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_AXE,"Elmas Balta", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonDiamondAxeDamage+3,CustomItems.CustomItemStats.commonDiamondAxeSpeed));
        }
        else if(roll < 96) //95 1% chance. Legendary Axe
        {
            items.add(LegendaryItems.createCrimsonRage());
        }
        //95-99 5% chance of nothing.

        //Helmet drops
        roll = rand.nextInt(100);
        if(roll < 25) //0-24 25% chance. Uncommon Iron Helmet
        {
            items.add(CustomItems.createCustomItem(Material.IRON_HELMET,"Demir Kask", CustomItems.Enderlik.NADIR,true, CustomItems.CustomItemStats.commonIronHelmetArmor,CustomItems.CustomItemStats.commonIronHelmetToughness+1,EquipmentSlot.HEAD,null));
        }
        else if(roll < 40) //25-39 15% chance. Rare Iron Helmet
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.IRON_HELMET,"Demir Kask", CustomItems.Enderlik.ESSIZ,true, CustomItems.CustomItemStats.commonIronHelmetArmor,CustomItems.CustomItemStats.commonIronHelmetToughness+1,EquipmentSlot.HEAD,temp));
        }
        else if(roll < 65) //40-64 25% chance. Common Diamond Helmet
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_HELMET,"Elmas Kask", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonDiamondHelmetArmor,CustomItems.CustomItemStats.commonDiamondHelmetToughness,EquipmentSlot.HEAD,null));
        }
        else if(roll < 80) //65-79 15% chance. Uncommon Diamond Helmet
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_HELMET,"Elmas Kask", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonDiamondHelmetArmor,CustomItems.CustomItemStats.commonDiamondHelmetToughness+1,EquipmentSlot.HEAD,null));
        }
        else if(roll < 90) //80-89 10% chance. Rare Diamond Helmet
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_HELMET,"Elmas Kask", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonDiamondHelmetArmor,CustomItems.CustomItemStats.commonDiamondHelmetToughness+1,EquipmentSlot.HEAD,temp));
        }
        else if(roll < 95) //90-94 5% chance. Epic Diamond Helmet
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,3);
            temp.put(Enchantment.PROTECTION_EXPLOSIONS,2);
            temp.put(Enchantment.PROTECTION_PROJECTILE,2);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_HELMET,"Elmas Kask", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonDiamondHelmetArmor,CustomItems.CustomItemStats.commonDiamondHelmetToughness+1,EquipmentSlot.HEAD,temp));
        }
        //95-99 5% chance of nothing.

        //Plate drops
        roll = rand.nextInt(100);
        if(roll < 25) //0-24 25% chance. Uncommon Iron Plate
        {
            items.add(CustomItems.createCustomItem(Material.IRON_CHESTPLATE,"Demir Göğüslük", CustomItems.Enderlik.NADIR,true, CustomItems.CustomItemStats.commonIronPlateArmor,CustomItems.CustomItemStats.commonIronPlateToughness+1,EquipmentSlot.CHEST,null));
        }
        else if(roll < 40) //25-39 15% chance. Rare Iron Plate
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.IRON_CHESTPLATE,"Demir Göğüslük", CustomItems.Enderlik.ESSIZ,true, CustomItems.CustomItemStats.commonIronPlateArmor,CustomItems.CustomItemStats.commonIronPlateToughness+1,EquipmentSlot.CHEST,temp));
        }
        else if(roll < 65) //40-64 25% chance. Common Diamond Plate
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_CHESTPLATE,"Elmas Göğüslük", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonDiamondPlateArmor,CustomItems.CustomItemStats.commonDiamondPlateToughness,EquipmentSlot.CHEST,null));
        }
        else if(roll < 80) //65-79 15% chance. Uncommon Diamond Plate
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_CHESTPLATE,"Elmas Göğüslük", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonDiamondPlateArmor,CustomItems.CustomItemStats.commonDiamondPlateToughness+1,EquipmentSlot.CHEST,null));
        }
        else if(roll < 90) //80-89 10% chance. Rare Diamond Plate
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_CHESTPLATE,"Elmas Göğüslük", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonDiamondPlateArmor,CustomItems.CustomItemStats.commonDiamondPlateToughness+1,EquipmentSlot.CHEST,temp));
        }
        else if(roll < 95) //90-94 5% chance. Epic Diamond Plate
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,3);
            temp.put(Enchantment.PROTECTION_EXPLOSIONS,2);
            temp.put(Enchantment.PROTECTION_PROJECTILE,2);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_CHESTPLATE,"Elmas Göğüslük", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonDiamondPlateArmor,CustomItems.CustomItemStats.commonDiamondPlateToughness+1,EquipmentSlot.CHEST,temp));
        }
        //95-99 5% chance of nothing.

        //Leggings drops
        roll = rand.nextInt(100);
        if(roll < 25) //0-24 25% chance. Uncommon Iron Leggings
        {
            items.add(CustomItems.createCustomItem(Material.IRON_LEGGINGS,"Demir Pantalon", CustomItems.Enderlik.NADIR,true, CustomItems.CustomItemStats.commonIronLeggingsArmor,CustomItems.CustomItemStats.commonIronLeggingsToughness+1,EquipmentSlot.LEGS,null));
        }
        else if(roll < 40) //25-39 15% chance. Rare Iron Leggings
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.IRON_LEGGINGS,"Demir Pantalon", CustomItems.Enderlik.ESSIZ,true, CustomItems.CustomItemStats.commonIronLeggingsArmor,CustomItems.CustomItemStats.commonIronLeggingsToughness+1,EquipmentSlot.LEGS,temp));
        }
        else if(roll < 65) //40-64 25% chance. Common Diamond Leggings
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_LEGGINGS,"Elmas Pantalon", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonDiamondLeggingsArmor,CustomItems.CustomItemStats.commonDiamondLeggingsToughness,EquipmentSlot.LEGS,null));
        }
        else if(roll < 80) //65-79 15% chance. Uncommon Diamond Leggings
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_LEGGINGS,"Elmas Pantalon", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonDiamondLeggingsArmor,CustomItems.CustomItemStats.commonDiamondLeggingsToughness+1,EquipmentSlot.LEGS,null));
        }
        else if(roll < 90) //80-89 10% chance. Rare Diamond Leggings
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_LEGGINGS,"Elmas Pantalon", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonDiamondLeggingsArmor,CustomItems.CustomItemStats.commonDiamondLeggingsToughness+1,EquipmentSlot.LEGS,temp));
        }
        else if(roll < 95) //90-94 5% chance. Epic Diamond Leggings
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,3);
            temp.put(Enchantment.PROTECTION_EXPLOSIONS,2);
            temp.put(Enchantment.PROTECTION_PROJECTILE,2);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_LEGGINGS,"Elmas Pantalon", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonDiamondLeggingsArmor,CustomItems.CustomItemStats.commonDiamondLeggingsToughness+1,EquipmentSlot.LEGS,temp));
        }
        //95-99 5% chance of nothing.

        //Boot drops
        roll = rand.nextInt(100);
        if(roll < 25) //0-24 25% chance. Uncommon Iron Boots
        {
            items.add(CustomItems.createCustomItem(Material.IRON_BOOTS,"Demir Bot", CustomItems.Enderlik.NADIR,true, CustomItems.CustomItemStats.commonIronBootsArmor,CustomItems.CustomItemStats.commonIronLeggingsToughness+1,EquipmentSlot.FEET,null));
        }
        else if(roll < 40) //25-39 15% chance. Rare Iron Boots
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.IRON_BOOTS,"Demir Bot", CustomItems.Enderlik.ESSIZ,true, CustomItems.CustomItemStats.commonIronBootsArmor,CustomItems.CustomItemStats.commonIronLeggingsToughness+1,EquipmentSlot.FEET,temp));
        }
        else if(roll < 65) //40-64 25% chance. Common Diamond Boots
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_BOOTS,"Elmas Bot", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonDiamondBootsArmor,CustomItems.CustomItemStats.commonDiamondBootsToughness,EquipmentSlot.FEET,null));
        }
        else if(roll < 80) //65-79 15% chance. Uncommon Diamond Boots
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_BOOTS,"Elmas Bot", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonDiamondBootsArmor,CustomItems.CustomItemStats.commonDiamondBootsToughness+1,EquipmentSlot.FEET,null));
        }
        else if(roll < 90) //80-89 10% chance. Rare Diamond Boots
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_BOOTS,"Elmas Bot", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonDiamondBootsArmor,CustomItems.CustomItemStats.commonDiamondBootsToughness+1,EquipmentSlot.FEET,temp));
        }
        else if(roll < 95) //90-94 5% chance. Epic Diamond Boots
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,3);
            temp.put(Enchantment.PROTECTION_EXPLOSIONS,2);
            temp.put(Enchantment.PROTECTION_PROJECTILE,2);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_BOOTS,"Elmas Bot", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonDiamondBootsArmor,CustomItems.CustomItemStats.commonDiamondBootsToughness+1,EquipmentSlot.FEET,temp));
        }
        //95-99 5% chance of nothing.

        //POTIONS below:
        roll = rand.nextInt(100);
        if(roll < 50) // 0-49 50% chance. Uncommon Healing.
        {
            items.add(CustomItems.createCustomPotion("Ani İyileştirme İksiri", PotionType.INSTANT_HEAL, CustomItems.Enderlik.NADIR,Material.POTION));
        }
        else if(roll < 85) // 50-84 35% chance. Rare Healing.
        {
            items.add(CustomItems.createCustomPotion("Ani İyileştirme İksiri", PotionType.INSTANT_HEAL, CustomItems.Enderlik.ESSIZ,Material.POTION));
        }
        else if(roll < 95) // 85-94 10% chance. Epic healing Healing.
        {
            items.add(CustomItems.createCustomPotion("Ani İyileştirme İksiri", PotionType.INSTANT_HEAL, CustomItems.Enderlik.OLAGANUSTU,Material.POTION));
        }
        else if(roll < 100) // 95-99 5% chance. Legendary Healing.
        {
            items.add(CustomItems.createCustomPotion("Ani İyileştirme İksiri", PotionType.INSTANT_HEAL, CustomItems.Enderlik.DESTANSI,Material.POTION));
        }
        return items;

    }

    public static List<ItemStack> generateWitherDrops()
    {
        List<ItemStack> items = new ArrayList<>();
        Random rand = new Random();

        //Sword rops below
        int roll = rand.nextInt(100);
        if(roll < 20) //0-19, 20% chance. Uncommon Diamond Sword.
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_SWORD,"Elmas Kılıç", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonDiamondSwordDamage+1,CustomItems.CustomItemStats.commonDiamondSwordSpeed));
        }
        else if(roll < 32) // 20-31 12% chance. Rare Diamond Sword.
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_SWORD,"Elmas Kılıç", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonDiamondSwordDamage+2,CustomItems.CustomItemStats.commonDiamondSwordSpeed));
        }
        else if(roll < 40) // 32-39 8% chance. Epic Diamond Sword.
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_SWORD,"Elmas Kılıç", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonDiamondSwordDamage+3,CustomItems.CustomItemStats.commonDiamondSwordSpeed));
        }
        else if(roll < 60) // 40-59 20% chance. Common Netherite Sword.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_SWORD,"Netherit Kılıç", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonNetheriteSwordDamage,CustomItems.CustomItemStats.commonNetheriteSwordSpeed));
        }
        else if(roll < 72) // 60-71 12% chance. Uncommon Netherite Sword.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_SWORD,"Netherit Kılıç", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonNetheriteSwordDamage+1,CustomItems.CustomItemStats.commonNetheriteSwordSpeed));
        }
        else if(roll < 80) // 72-79 8% chance. Rare Netherite Sword.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_SWORD,"Netherit Kılıç", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonNetheriteSwordDamage+2,CustomItems.CustomItemStats.commonNetheriteSwordSpeed));
        }
        else if(roll < 85) // 80-84 5% chance. Epic Netherite Sword.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_SWORD,"Netherit Kılıç", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonNetheriteSwordDamage+3,CustomItems.CustomItemStats.commonNetheriteSwordSpeed));
        }
        else if(roll < 86) //85... 1% chance. Legendary Engin Tutku
        {
            items.add(LegendaryItems.createEnginTutku());
        }

        //Axe drops below
        roll = rand.nextInt(100);
        if(roll < 20) //0-19, 20% chance. Uncommon Diamond Axe.
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_AXE,"Elmas Balta", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonDiamondAxeDamage+1,CustomItems.CustomItemStats.commonDiamondAxeSpeed));
        }
        else if(roll < 32) // 20-31 12% chance. Rare Diamond Axe.
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_AXE,"Elmas Balta", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonDiamondAxeDamage+2,CustomItems.CustomItemStats.commonDiamondAxeSpeed));
        }
        else if(roll < 40) // 32-39 8% chance. Epic Diamond Axe.
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_AXE,"Elmas Balta", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonDiamondAxeDamage+3,CustomItems.CustomItemStats.commonDiamondAxeSpeed));
        }
        else if(roll < 60) // 40-59 20% chance. Common Netherite Axe.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_AXE,"Netherit Balta", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonNetheriteAxeDamage,CustomItems.CustomItemStats.commonNetheriteAxeSpeed));
        }
        else if(roll < 72) // 60-71 12% chance. Uncommon Netherite Axe.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_AXE,"Netherit Balta", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonNetheriteAxeDamage+1,CustomItems.CustomItemStats.commonNetheriteAxeSpeed));
        }
        else if(roll < 80) // 72-79 8% chance. Rare Netherite Axe.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_AXE,"Netherit Balta", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonNetheriteAxeDamage+2,CustomItems.CustomItemStats.commonNetheriteAxeSpeed));
        }
        else if(roll < 85) // 80-84 5% chance. Epic Netherite Axe.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_AXE,"Netherit Balta", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonNetheriteAxeDamage+3,CustomItems.CustomItemStats.commonNetheriteAxeSpeed));
        }

        //Helmet drops below
        roll = rand.nextInt(100);
        if(roll < 20) //0-19, 20% chance. Uncommon Diamond Helmet.
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_HELMET,"Elmas Kask", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonDiamondHelmetArmor,CustomItems.CustomItemStats.commonDiamondHelmetToughness+1,EquipmentSlot.HEAD,null));
        }
        else if(roll < 32) // 20-31 12% chance. Rare Diamond Helmet.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_HELMET,"Elmas Kask", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonDiamondHelmetArmor,CustomItems.CustomItemStats.commonDiamondHelmetToughness+1,EquipmentSlot.HEAD,temp));
        }
        else if(roll < 40) // 32-39 8% chance. Epic Diamond Helmet.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,3);
            temp.put(Enchantment.PROTECTION_EXPLOSIONS,2);
            temp.put(Enchantment.PROTECTION_PROJECTILE,2);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_HELMET,"Elmas Kask", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonDiamondHelmetArmor,CustomItems.CustomItemStats.commonDiamondHelmetToughness+1,EquipmentSlot.HEAD,temp));
        }
        else if(roll < 60) // 40-59 20% chance. Common Netherite Helmet.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_HELMET,"Netherit Kask", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonNetheriteHelmetArmor,CustomItems.CustomItemStats.commonNetheriteHelmetToughness,EquipmentSlot.HEAD,null));
        }
        else if(roll < 72) // 60-71 12% chance. Uncommon Netherite Helmet.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_HELMET,"Netherit Kask", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonNetheriteHelmetArmor,CustomItems.CustomItemStats.commonNetheriteHelmetToughness+1,EquipmentSlot.HEAD,null));
        }
        else if(roll < 80) // 72-79 8% chance. Rare Netherite Helmet.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.NETHERITE_HELMET,"Netherit Kask", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonNetheriteHelmetArmor,CustomItems.CustomItemStats.commonNetheriteHelmetToughness+1,EquipmentSlot.HEAD,temp));
        }
        else if(roll < 85) // 80-84 5% chance. Epic Netherite Axe.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,3);
            temp.put(Enchantment.PROTECTION_EXPLOSIONS,2);
            temp.put(Enchantment.PROTECTION_PROJECTILE,2);
            items.add(CustomItems.createCustomItem(Material.NETHERITE_HELMET,"Netherit Kask", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonNetheriteHelmetArmor,CustomItems.CustomItemStats.commonNetheriteHelmetToughness+1,EquipmentSlot.HEAD,temp));
        }

        //Chestplate drops below
        roll = rand.nextInt(100);
        if(roll < 20) //0-19, 20% chance. Uncommon Diamond Chestplate.
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_CHESTPLATE,"Elmas Göğüslük", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonDiamondPlateArmor,CustomItems.CustomItemStats.commonDiamondPlateToughness+1,EquipmentSlot.CHEST,null));
        }
        else if(roll < 32) // 20-31 12% chance. Rare Diamond Chestplate.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_CHESTPLATE,"Elmas Göğüslük", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonDiamondPlateArmor,CustomItems.CustomItemStats.commonDiamondPlateToughness+1,EquipmentSlot.CHEST,temp));
        }
        else if(roll < 40) // 32-39 8% chance. Epic Diamond Chestplate.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,3);
            temp.put(Enchantment.PROTECTION_EXPLOSIONS,2);
            temp.put(Enchantment.PROTECTION_PROJECTILE,2);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_CHESTPLATE,"Elmas Göğüslük", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonDiamondPlateArmor,CustomItems.CustomItemStats.commonDiamondPlateToughness+1,EquipmentSlot.CHEST,temp));
        }
        else if(roll < 60) // 40-59 20% chance. Common Netherite Chestplate.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_CHESTPLATE,"Netherit Göğüslük", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonNetheritePlateArmor,CustomItems.CustomItemStats.commonNetheritePlateToughness,EquipmentSlot.CHEST,null));
        }
        else if(roll < 72) // 60-71 12% chance. Uncommon Netherite Chestplate.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_CHESTPLATE,"Netherit Göğüslük", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonNetheritePlateArmor,CustomItems.CustomItemStats.commonNetheritePlateToughness+1,EquipmentSlot.CHEST,null));
        }
        else if(roll < 80) // 72-79 8% chance. Rare Netherite Chestplate.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.NETHERITE_CHESTPLATE,"Netherit Göğüslük", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonNetheritePlateArmor,CustomItems.CustomItemStats.commonNetheritePlateToughness+1,EquipmentSlot.CHEST,temp));
        }
        else if(roll < 85) // 80-84 5% chance. Epic Netherite Chestplate.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,3);
            temp.put(Enchantment.PROTECTION_EXPLOSIONS,2);
            temp.put(Enchantment.PROTECTION_PROJECTILE,2);
            items.add(CustomItems.createCustomItem(Material.NETHERITE_CHESTPLATE,"Netherit Göğüslük", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonNetheritePlateArmor,CustomItems.CustomItemStats.commonNetheritePlateToughness+1,EquipmentSlot.CHEST,temp));
        }


        //Leggings drops below
        roll = rand.nextInt(100);
        if(roll < 20) //0-19, 20% chance. Uncommon Diamond Leggings.
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_LEGGINGS,"Elmas Pantalon", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonDiamondLeggingsArmor,CustomItems.CustomItemStats.commonDiamondLeggingsToughness+1,EquipmentSlot.LEGS,null));
        }
        else if(roll < 32) // 20-31 12% chance. Rare Diamond Leggings.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_LEGGINGS,"Elmas Pantalon", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonDiamondLeggingsArmor,CustomItems.CustomItemStats.commonDiamondLeggingsToughness+1,EquipmentSlot.LEGS,temp));
        }
        else if(roll < 40) // 32-39 8% chance. Epic Diamond Leggings.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,3);
            temp.put(Enchantment.PROTECTION_EXPLOSIONS,2);
            temp.put(Enchantment.PROTECTION_PROJECTILE,2);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_LEGGINGS,"Elmas Pantalon", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonDiamondLeggingsArmor,CustomItems.CustomItemStats.commonDiamondLeggingsToughness+1,EquipmentSlot.LEGS,temp));
        }
        else if(roll < 60) // 40-59 20% chance. Common Netherite Leggings.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_LEGGINGS,"Netherit Pantalon", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonNetheriteLeggingsArmor,CustomItems.CustomItemStats.commonNetheriteLeggingsToughness,EquipmentSlot.LEGS,null));
        }
        else if(roll < 72) // 60-71 12% chance. Uncommon Netherite Leggings.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_LEGGINGS,"Netherit Pantalon", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonNetheriteLeggingsArmor,CustomItems.CustomItemStats.commonNetheriteLeggingsToughness+1,EquipmentSlot.LEGS,null));
        }
        else if(roll < 80) // 72-79 8% chance. Rare Netherite Leggings.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.NETHERITE_LEGGINGS,"Netherit Pantalon", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonNetheriteLeggingsArmor,CustomItems.CustomItemStats.commonNetheriteLeggingsToughness+1,EquipmentSlot.LEGS,temp));
        }
        else if(roll < 85) // 80-84 5% chance. Epic Netherite Leggings.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,3);
            temp.put(Enchantment.PROTECTION_EXPLOSIONS,2);
            temp.put(Enchantment.PROTECTION_PROJECTILE,2);
            items.add(CustomItems.createCustomItem(Material.NETHERITE_LEGGINGS,"Netherit Pantalon", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonNetheriteLeggingsArmor,CustomItems.CustomItemStats.commonNetheriteLeggingsToughness+1,EquipmentSlot.LEGS,temp));
        }


        //Boots drops below
        roll = rand.nextInt(100);
        if(roll < 20) //0-19, 20% chance. Uncommon Diamond Boots.
        {
            items.add(CustomItems.createCustomItem(Material.DIAMOND_BOOTS,"Elmas Bot", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonDiamondBootsArmor,CustomItems.CustomItemStats.commonDiamondBootsToughness+1,EquipmentSlot.FEET,null));
        }
        else if(roll < 32) // 20-31 12% chance. Rare Diamond Boots.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_BOOTS,"Elmas Bot", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonDiamondBootsArmor,CustomItems.CustomItemStats.commonDiamondBootsToughness+1,EquipmentSlot.FEET,temp));
        }
        else if(roll < 40) // 32-39 8% chance. Epic Diamond Boots.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,3);
            temp.put(Enchantment.PROTECTION_EXPLOSIONS,2);
            temp.put(Enchantment.PROTECTION_PROJECTILE,2);
            items.add(CustomItems.createCustomItem(Material.DIAMOND_BOOTS,"Elmas Bot", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonDiamondBootsArmor,CustomItems.CustomItemStats.commonDiamondBootsToughness+1,EquipmentSlot.FEET,temp));
        }
        else if(roll < 60) // 40-59 20% chance. Common Netherite Boots.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_BOOTS,"Netherit Bot", CustomItems.Enderlik.SIRADAN,true,CustomItems.CustomItemStats.commonNetheriteBootsArmor,CustomItems.CustomItemStats.commonNetheriteBootsToughness,EquipmentSlot.FEET,null));
        }
        else if(roll < 72) // 60-71 12% chance. Uncommon Netherite Boots.
        {
            items.add(CustomItems.createCustomItem(Material.NETHERITE_BOOTS,"Netherit Bot", CustomItems.Enderlik.NADIR,true,CustomItems.CustomItemStats.commonNetheriteBootsArmor,CustomItems.CustomItemStats.commonNetheriteBootsToughness+1,EquipmentSlot.FEET,null));
        }
        else if(roll < 80) // 72-79 8% chance. Rare Netherite Boots.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,1);
            items.add(CustomItems.createCustomItem(Material.NETHERITE_BOOTS,"Netherit Bot", CustomItems.Enderlik.ESSIZ,true,CustomItems.CustomItemStats.commonNetheriteBootsArmor,CustomItems.CustomItemStats.commonNetheriteBootsToughness+1,EquipmentSlot.FEET,temp));
        }
        else if(roll < 85) // 80-84 5% chance. Epic Netherite Boots.
        {
            HashMap<Enchantment,Integer> temp = new HashMap<Enchantment, Integer>();
            temp.put(Enchantment.PROTECTION_ENVIRONMENTAL,3);
            temp.put(Enchantment.PROTECTION_EXPLOSIONS,2);
            temp.put(Enchantment.PROTECTION_PROJECTILE,2);
            items.add(CustomItems.createCustomItem(Material.NETHERITE_BOOTS,"Netherit Bot", CustomItems.Enderlik.OLAGANUSTU,true,CustomItems.CustomItemStats.commonNetheriteBootsArmor,CustomItems.CustomItemStats.commonNetheriteBootsToughness+1,EquipmentSlot.FEET,temp));
        }

        //Healing drops below
        roll = rand.nextInt(100);
        if(roll < 30) // 0-29 30% chance. Uncommon Healing.
        {
            items.add(CustomItems.createCustomPotion("Ani İyileştirme İksiri", PotionType.INSTANT_HEAL, CustomItems.Enderlik.NADIR,Material.POTION));
        }
        else if(roll < 75) // 30-74 45% chance. Rare Healing.
        {
            items.add(CustomItems.createCustomPotion("Ani İyileştirme İksiri", PotionType.INSTANT_HEAL, CustomItems.Enderlik.ESSIZ,Material.POTION));
        }
        else if(roll < 95) // 75-94 20% chance. Epic Healing.
        {
            items.add(CustomItems.createCustomPotion("Ani İyileştirme İksiri", PotionType.INSTANT_HEAL, CustomItems.Enderlik.OLAGANUSTU,Material.POTION));
        }
        else if(roll < 100) // 95-99 5% chance. Legendary Healing.
        {
            items.add(CustomItems.createCustomPotion("Ani İyileştirme İksiri", PotionType.INSTANT_HEAL, CustomItems.Enderlik.DESTANSI,Material.POTION));
        }
        return items;


    }

    /*public static void checkOpenWorldMobs(Plugin plugin)
    {
        for(int id : OpenWorldConfigManager.openWorldSavers.keySet())
        {
            plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Checking id: " + id);
            OpenWorldConfigManager.OpenWorldSaver saver = OpenWorldConfigManager.openWorldSavers.get(id);
            int xMax = (int) Math.max(saver.getLoc1().getX(), saver.getLoc2().getX());
            int xMin = (int) Math.min(saver.getLoc1().getX(), saver.getLoc2().getX());

            int zMax = (int) Math.max(saver.getLoc1().getZ(), saver.getLoc2().getZ());
            int zMin = (int) Math.min(saver.getLoc1().getZ(), saver.getLoc2().getZ());

            plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "CurrentCount: " + saver.getCurrentMobCount());
            plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Mob Count: " + saver.getMobCount());

            if(saver.getCurrentMobCount() < saver.getMobCount())
            {
                //There are not enough mobs! Spawn more
                for(; saver.getCurrentMobCount() < saver.getMobCount(); saver.setCurrentMobCount(saver.getCurrentMobCount()+1))
                {
                    Random r = new Random();
                    int randomX = r.nextInt(xMax-xMin) + xMin;
                    int randomZ = r.nextInt(zMax-zMin) + zMin;
                    int whichMob = r.nextInt(saver.getMobsToSpawn().size());
                    int randomLevel = r.nextInt(saver.getUpperLevel()-saver.getLowerLevel()) + saver.getLowerLevel();

                    Location temp = new Location(saver.getLoc1().getWorld(),randomX,180, randomZ);
                    temp.getWorld().loadChunk(temp.getChunk());
                    temp.getWorld().setChunkForceLoaded(temp.getChunk().getX(),temp.getChunk().getZ(),true);
                    temp.getChunk().load();
                    temp.getChunk().setForceLoaded(true);
                    //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Spawning: " + saver.getMobsToSpawn().get(whichMob));

                    switch (saver.getMobsToSpawn().get(whichMob))
                    {
                        case "zombie":
                            LeveledZombie zombie = new LeveledZombie(saver.getLoc1().getWorld(),randomLevel,temp,true,saver.getId());
                            break;
                        case "husk":
                            LeveledHusk husk = new LeveledHusk(saver.getLoc1().getWorld(),randomLevel,temp,true,saver.getId());
                            break;
                        case "spider":
                            LeveledSpider spider = new LeveledSpider(saver.getLoc1().getWorld(),randomLevel,temp,true,saver.getId());
                            break;
                        case "skeleton":
                            LeveledSkeleton skeleton = new LeveledSkeleton(saver.getLoc1().getWorld(),randomLevel,temp,true,saver.getId());
                            break;
                        case "stray":
                            LeveledStray stray = new LeveledStray(saver.getLoc1().getWorld(),randomLevel,temp,true,saver.getId());
                            break;
                        case "witch":
                            LeveledWitch witch = new LeveledWitch(saver.getLoc1().getWorld(),randomLevel,temp,true,saver.getId());
                            break;
                    }
                }
                plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "After spawning: " + saver.getMobCount());
                plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "_____________________________________");
            }
        }
    }*/

    /*public static void checkOpenWorldMobs()
    {
        World world = Bukkit.getWorld("openworld_emnia");
        for(Chunk chunk : world.getLoadedChunks())
        {
            if(OpenWorldConfigManager.chunSaverMap.containsKey(chunk))
            {
                OpenWorldConfigManager.OpenWorldSaver saver = OpenWorldConfigManager.chunSaverMap.get(chunk);
                if(saver.getCurrentMobCount() < saver.getMobCount())
                {
                    DungeonListener.onChunk(new ChunkLoadEvent(chunk,true));
                }
            }
        }
    }*/
}
