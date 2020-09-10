package minecraft.mmoplugin;

import com.comphenix.packetwrapper.WrapperPlayServerUpdateTime;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import leveledmobs.*;
import minecraft.mmoplugin.customItems.CustomEnchants;
import minecraft.mmoplugin.events.*;
import net.md_5.bungee.protocol.PacketWrapper;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class MainClass extends JavaPlugin implements Listener
{

    public static String getPluginPrefix()
    {
        return ChatColor.YELLOW + "[" + ChatColor.DARK_AQUA + "Aviana REBORN" + ChatColor.YELLOW + "]" + ChatColor.WHITE + "";
    }

    enum customMobs
    {
        LEVELEDZOMBIE,
        LEVELEDSPIDER,
        LEVELEDCAVESPIDER,
    }

    static List<customMobs> customMobList;

    final int whirlwindCooldown = 30;
    public static final int summonCooldown = 60;
    public static final int sunderCooldown = 25;
    public static final int SummonHealCooldown = 120;
    public static final int leashCooldown = 90;
    public static final int blinkCooldown = 120;
    public static final int thunderlordsCooldown = 120;
    public static SignMenuFactory signMenu;
    public static OpenWorldConfigManager openWorldConfig;
    public static BukkitTask mobSpawnChecker;
    public static long emniaTime;



    public static Connection conn;
    public static BukkitTask applicationChecker;
    public static HashMap<UUID, Boolean> playerLoginMap;
    public static NPCConfigManager npcConfig;
    public static HashMap<UUID, List<BukkitTask>> playerTaskMap;
    public static HashMap<UUID, HashMap<String, DateTime>> playerCooldownMap;
    //public static HashMap<UUID, String> playerClassMap;
    HashMap<UUID, Integer> zombieIDList;
    public static HashMap<UUID, Integer> playerTeamMap;
    public static HashMap<UUID, MMOClass> classObjectMap;
    public static HashMap<String, Faction> factionMap;
    List<EliteGiant> giantList;
    static int latestTeamID;
    int latestZombieID = 1;
    public static Scoreboard mainScoreboard;
    private int switchingIndex = 0;

    public void OnBlockExplode(BlockExplodeEvent event)
    {
        event.setCancelled(true);
    }

    public void OnBlockIgnite(BlockIgniteEvent event)
    {
        event.setCancelled(true);
    }

    public void loadConfig()
    {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @SuppressWarnings("deprecation")
    public void createSpinningSwords(Player player)
    {
        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Whirlwind" + ChatColor.WHITE + ", yeteneği kullanıldı!");
        Location loc = player.getLocation();
        ArmorStand armorStand = loc.getWorld().spawn(loc,ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setArms(false);
        armorStand.setCanPickupItems(false);
        armorStand.setVelocity(player.getLocation().getDirection());
        armorStand.setItemInHand(new ItemStack(Material.NETHERITE_SWORD));


        AnimateStand animateStand = new AnimateStand(player, armorStand);
        BukkitTask animate = animateStand.runTaskTimer(this,0,1);

        SpinningSwordDamager spinningSwordDamager = new SpinningSwordDamager(player);
        BukkitTask damager = spinningSwordDamager.runTaskTimer(this,0,10);
        addToTaskMap(player, damager);
        addToTaskMap(player, animate);

        BukkitTask task = new BukkitRunnable(){

            @Override
            public void run() {
                if(Bukkit.getScheduler().isCurrentlyRunning(animate.getTaskId()) || Bukkit.getScheduler().isQueued(animate.getTaskId()))
                {
                    Bukkit.getScheduler().cancelTask(animate.getTaskId());
                    playerTaskMap.get(player.getUniqueId()).remove(animate);
                }
                if(Bukkit.getScheduler().isCurrentlyRunning(damager.getTaskId()) || Bukkit.getScheduler().isQueued(damager.getTaskId()))
                {
                    Bukkit.getScheduler().cancelTask(damager.getTaskId());
                    playerTaskMap.get(player.getUniqueId()).remove(damager);
                }
                armorStand.remove();
            }
        }.runTaskLater(this,100);

        playerTaskMap.get(player.getUniqueId()).add(task);
    }


    public void loadAllNpcs()
    {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this,()->{
            npcConfig.getConfig().getConfigurationSection("data").getKeys(false).forEach(npc -> {
                Location location = new Location(Bukkit.getWorld(npcConfig.getConfig().getString("data."+npc+".world")),
                        npcConfig.getConfig().getDouble("data."+npc+".x"),
                        npcConfig.getConfig().getDouble("data."+npc+".y"),
                        npcConfig.getConfig().getDouble("data."+npc+".z"));
                location.setPitch((float)npcConfig.getConfig().getDouble("data."+npc+".pitch"));
                location.setYaw((float)npcConfig.getConfig().getDouble("data."+npc+".yaw"));

                String name = npcConfig.getConfig().getString("data."+npc+".name");
                String color = npcConfig.getConfig().getString("data."+npc+".color");
                GameProfile gameProfile;
                if(color == null)
                    gameProfile = new GameProfile(UUID.randomUUID(),"[NPC]" + name);
                else
                    gameProfile = new GameProfile(UUID.randomUUID(),"§"+color+"[NPC]" + name);
                gameProfile.getProperties().put("textures", new Property("textures", npcConfig.getConfig().getString("data."+npc+".texture"), npcConfig.getConfig().getString("data."+npc+".signature")));
                EntityPlayer npcLoaded = NPCManager.loadNPC(location, gameProfile);
                NPCManager.npcIdMap.put(npcLoaded, UUID.fromString(npc));
            });
        },0);
    }


    @Override
    public void onEnable() {
        this.getServer().getConsoleSender().sendMessage(getPluginPrefix() + "Enabling MMOPlugin, v0.0.10");
        getServer().getPluginManager().registerEvents(this,this);
        getServer().getPluginManager().registerEvents(new RaidBossListener(this), this);
        getServer().getPluginManager().registerEvents(new NPCListener(this),this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this),this);
        getServer().getPluginManager().registerEvents(new LeveledMobListener(this),this);
        getServer().getPluginManager().registerEvents(new WarriorEventManager(this),this);
        getServer().getPluginManager().registerEvents(new ClericEventManager(this),this);
        getServer().getPluginManager().registerEvents(new SummonListener(this),this);
        getServer().getPluginManager().registerEvents(new NecromancerListener(this),this);
        getServer().getPluginManager().registerEvents(new MMOClassListener(this),this);
        getServer().getPluginManager().registerEvents(new AuctionInventoryListener(this),this);
        getServer().getPluginManager().registerEvents(new MarketNPCInventoryListener(this),this);
        getServer().getPluginManager().registerEvents(new DungeonListener(this),this);
        CustomEnchants.register();
        emniaTime = 0;
        LeveledMobListener.mobCount = 0;
        CommandManager cm = new CommandManager(this);
        TabHelper tb = new TabHelper() ;
        getCommand("class").setExecutor(cm);
        //getCommand("class").setTabCompleter(tb);
        getCommand("stats").setExecutor(cm);
        getCommand("spider").setExecutor(cm);
        getCommand("mobspawn").setExecutor(cm);
        getCommand("createnpc").setExecutor(cm);
        getCommand("setlvl").setExecutor(cm);
        getCommand("removenpc").setExecutor(cm);
        getCommand("target").setExecutor(cm);
        getCommand("heal").setExecutor(cm);
        getCommand("faction").setExecutor(cm);
        getCommand("faction").setTabCompleter(tb);
        getCommand("join").setExecutor(cm);
        getCommand("dungeon").setExecutor(cm);
        getCommand("dungeon").setTabCompleter(tb);
        getCommand("fireball").setExecutor(cm);
        getCommand("setspawn").setExecutor(cm);
        getCommand("circle").setExecutor(cm);
        getCommand("takas").setExecutor(cm);
        getCommand("addmoney").setExecutor(cm);
        getCommand("rareaxe").setExecutor(cm);
        getCommand("epicaxe").setExecutor(cm);
        getCommand("currentdungeon").setExecutor(cm);
        getCommand("removedungeon").setExecutor(cm);
        getCommand("showrolls").setExecutor(cm);
        getCommand("getattributes").setExecutor(cm);
        getCommand("yoshiramaru").setExecutor(cm);
        getCommand("crimsonrage").setExecutor(cm);
        getCommand("checkarmor").setExecutor(cm);
        getCommand("openworld").setExecutor(cm);
        getCommand("checkspawns").setExecutor(cm);
        getCommand("lobi").setExecutor(cm);
        getCommand("resetcooldown").setExecutor(cm);
        getCommand("engintutku").setExecutor(cm);
        getCommand("removeall").setExecutor(cm);
        try
        {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/aviana", "root", "");
            getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.GREEN + "Established the SQL Connection!");
        }
        catch (SQLException e)
        {
            getServer().getLogger().log(Level.SEVERE,"Error occured while establishing the SQL Connection!", e);
            return;
        }
        //playerClassMap = new HashMap<>();
        classObjectMap = new HashMap<>();
        zombieIDList = new HashMap<>();
        playerTaskMap = new HashMap<>();
        playerCooldownMap = new HashMap<>();
        playerTeamMap = new HashMap<>();
        factionMap = new HashMap<>();
        playerLoginMap = new HashMap<>();
        OpenWorldConfigManager.chunSaverMap = new HashMap<>();
        OpenWorldConfigManager.openWorldMap = new HashMap<>();
        OpenWorldConfigManager.openWorldSavers = new HashMap<>();
        AuctionRunnables.runnableMap = new HashMap<>();
        latestTeamID = 1;
        customMobList = new ArrayList<>();
        customMobList.add(customMobs.LEVELEDZOMBIE);
        customMobList.add(customMobs.LEVELEDSPIDER);
        customMobList.add(customMobs.LEVELEDCAVESPIDER);
        giantList = new ArrayList<>();
        Faction.applicationQueue = new ArrayDeque<String>();
        this.signMenu = new SignMenuFactory(this);

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                if(mainScoreboard.getObjective("dummy") != null)
                {
                    mainScoreboard.getObjective("dummy").unregister();
                }
                registerHealthBar();
                registerNameTag();
                if(!Bukkit.getOnlinePlayers().isEmpty())
                {
                    for(Player online : Bukkit.getOnlinePlayers())
                    {
                        PacketReader reader = new PacketReader(MainClass.this);
                        reader.inject(online);

                        if(!classObjectMap.containsKey(online.getUniqueId()))
                        {
                            PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(online,"");
                            onPlayerJoinEvent(playerJoinEvent);

                        }
                        MMOClass mmoClass = classObjectMap.get(online.getUniqueId());
                        createScoreboard(online,mmoClass.getLevel(),mmoClass.getXp(),mmoClass.getClassName());
                    }
                }
            }
        },0);
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Field a = packet.getClass().getDeclaredField("header");
                    a.setAccessible(true);
                    Field b = packet.getClass().getDeclaredField("footer");
                    b.setAccessible(true);

                    Object header0 = new ChatComponentText("§bAVIANA REBORN\n§6MMORPG Elementli Faction!");
                    Object header1 = new ChatComponentText("§aAVIANA REBORN\n§6MMORPG Elementli Faction!");
                    Object header2 = new ChatComponentText("§1AVIANA REBORN\n§6MMORPG Elementli Faction!");
                    Object header3 = new ChatComponentText("§cAVIANA REBORN\n§6MMORPG Elementli Faction!");
                    Object header4 = new ChatComponentText("§9AVIANA REBORN\n§6MMORPG Elementli Faction!");
                    Object header5 = new ChatComponentText("§6AVIANA REBORN\n§6MMORPG Elementli Faction!");


                    Object footer = new ChatComponentText("§bOnline Oyuncular: §f" + Bukkit.getServer().getOnlinePlayers().size());
                    switch (switchingIndex)
                    {
                        case 0:
                            a.set(packet, header0);
                            break;
                        case 1:
                            a.set(packet,header1);
                            break;
                        case 2:
                            a.set(packet,header2);
                            break;
                        case 3:
                            a.set(packet,header3);
                            break;
                        case 4:
                            a.set(packet,header4);
                            break;
                        case 5:
                            a.set(packet,header5);
                            break;
                    }
                    switchingIndex++;
                    if(switchingIndex == 6)
                    {
                        switchingIndex = 0;
                    }
                    b.set(packet, footer);

                    if (Bukkit.getOnlinePlayers().size() == 0) return;
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if(player.getWorld().getName().equals("openworld_emnia"))
                        {
                            player.setPlayerTime(emniaTime,false);//Send the fake time packet to emnia players.
                        }
                        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                    }

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                emniaTime = (emniaTime+20)%24000;
            }
        };
        runnable.runTaskTimer(this,20,20);



        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    npcConfig = new NPCConfigManager(MainClass.this);
                    npcConfig.reloadConfig();
                    Necromancer.mobConfig = new MobConfigManager(MainClass.this);
                    Necromancer.mobConfig.reloadConfig();
                    DungeonManager.config = new DungeonConfigManager(MainClass.this);
                    DungeonManager.config.reloadConfig();
                    Faction.claimConfig = new FactionClaimConfig(MainClass.this);
                    Faction.claimConfig.reloadConfig();
                    Faction.config = new FactionConfig(MainClass.this);
                    Faction.config.reloadConfig();
                    CustomInventory.tradeConfig = new TradeConfigManager(MainClass.this);
                    CustomInventory.tradeConfig.reloadConfig();
                    openWorldConfig = new OpenWorldConfigManager(MainClass.this);
                    openWorldConfig.reloadConfig();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Faction.config.getConfig().contains("FactionDetails")) //Load factions from the config.
                {
                    Faction.config.getConfig().getConfigurationSection("FactionDetails").getKeys(false).forEach(faction-> {

                        String ownerUUID = Faction.config.getConfig().getString("FactionDetails."+faction+".leader");
                        Faction.JoinType joinType = Faction.JoinType.valueOf(Faction.config.getConfig().getString("FactionDetails."+faction+".joinPerms"));
                        int claimCount = Faction.config.getConfig().getInt("FactionDetails."+faction+".claims");
                        int power = Faction.config.getConfig().getInt("FactionDetails."+faction+".power");
                        int size = Faction.config.getConfig().getInt("FactionDetails."+faction+".size");
                        Location home = null;
                        if(Faction.config.getConfig().contains("FactionDetails."+faction+".home"))
                        {
                            home = Faction.config.getConfig().getLocation("FactionDetails."+faction+".home");
                        }
                        HashMap<String, Faction> enemies = new HashMap<>();
                        HashMap<String, Faction> allies = new HashMap<>();
                        HashMap<String, DateTime> cooldowns = new HashMap<>();
                        HashMap<UUID, DateTime> applications = new HashMap<>();
                        if(Faction.config.getConfig().contains("FactionDetails."+faction+".enemies"))
                        {
                            Faction.config.getConfig().getConfigurationSection("FactionDetails."+faction+".enemies").getKeys(false).forEach(enemy->{
                                enemies.put(enemy,null);
                            });
                        }
                        if(Faction.config.getConfig().contains("FactionDetails."+faction+".allies"))
                        {
                            Faction.config.getConfig().getConfigurationSection("FactionDetails."+faction+".allies").getKeys(false).forEach(ally->{
                                allies.put(ally,null);
                            });
                        }
                        if(Faction.config.getConfig().contains("FactionDetails."+faction+".cooldowns"))
                        {
                            Faction.config.getConfig().getConfigurationSection("FactionDetails."+faction+".cooldowns").getKeys(false).forEach(cooldown->{
                                int remainingMinutes = Faction.config.getConfig().getInt("FactionDetails."+faction+".cooldowns."+cooldown);
                                DateTime dt = new DateTime();
                                dt = dt.plusMinutes(remainingMinutes);
                                cooldowns.put(cooldown, dt);
                            });
                        }
                        if(Faction.config.getConfig().contains("FactionDetails."+faction+".applications"))
                        {
                            Faction.config.getConfig().getConfigurationSection("FactionDetails."+faction+".applications").getKeys(false).forEach(application->{
                                int remainingMinutes = Faction.config.getConfig().getInt("FactionDetails."+faction+".applications."+application);
                                DateTime dt = new DateTime();
                                dt = dt.plusMinutes(remainingMinutes);
                                applications.put(UUID.fromString(application), dt);
                            });
                        }
                        Faction temp = new Faction(faction,UUID.fromString(ownerUUID),Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)).getName());
                        temp.setClaimedCount(claimCount);
                        temp.setJoinType(joinType);
                        temp.setPower(power);
                        temp.setSize(size);
                        temp.setEnemyFactions(enemies);
                        temp.setAllyFactions(allies);
                        temp.setWarCoolDownMap(cooldowns);
                        temp.setApplications(applications);
                        temp.setHome(home);
                        factionMap.put(faction.toLowerCase(),temp);
                    });
                }
                if(Faction.config.getConfig().contains("ApplicationQueue"))
                {
                    Faction.config.getConfig().getConfigurationSection("ApplicationQueue").getKeys(false).forEach(member->{
                        Faction.applicationQueue.add(member);
                    });
                }
                if (npcConfig.getConfig().contains("data"))
                {
                    loadAllNpcs();
                }
                DungeonManager.dungeonMap = new HashMap<>();
                List<Player> playerList = new ArrayList<>();
                for(int i = 0; i < 5; i++)
                {
                    playerList = Objects.requireNonNull(Bukkit.getWorld("dungeon_nether_" + i)).getPlayers();
                    if(playerList.size() > 0)
                    {
                        DungeonManager.Dungeon dungeon = new DungeonManager.Dungeon(MainClass.this,playerList.get(0),"dungeon_nether_"+i);
                        dungeon.setInProgress(true);
                        DungeonManager.dungeonMap.put("dungeon_nether_"+i,dungeon);
                    }
                    else
                    {
                        DungeonManager.Dungeon dungeon = new DungeonManager.Dungeon(MainClass.this,null,"dungeon_nether_"+i);
                        dungeon.setInProgress(false);
                        DungeonManager.dungeonMap.put("dungeon_nether_"+i,dungeon);
                    }
                    playerList.clear();
                    playerList = Objects.requireNonNull(Bukkit.getWorld("dungeon_jungle_" + i)).getPlayers();
                    if(playerList.size() > 0)
                    {
                        DungeonManager.Dungeon dungeon = new DungeonManager.Dungeon(MainClass.this,playerList.get(0),"dungeon_jungle_"+i);
                        dungeon.setInProgress(true);
                        DungeonManager.dungeonMap.put("dungeon_jungle_"+i,dungeon);
                    }
                    else
                    {
                        DungeonManager.Dungeon dungeon = new DungeonManager.Dungeon(MainClass.this,null,"dungeon_jungle_"+i);
                        dungeon.setInProgress(false);
                        DungeonManager.dungeonMap.put("dungeon_jungle_"+i,dungeon);
                    }
                    playerList.clear();
                    playerList = Objects.requireNonNull(Bukkit.getWorld("dungeon_end_" + i)).getPlayers();
                    if(playerList.size() > 0)
                    {
                        DungeonManager.Dungeon dungeon = new DungeonManager.Dungeon(MainClass.this,playerList.get(0),"dungeon_end_"+i);
                        dungeon.setInProgress(true);
                        DungeonManager.dungeonMap.put("dungeon_end_"+i,dungeon);
                    }
                    else
                    {
                        DungeonManager.Dungeon dungeon = new DungeonManager.Dungeon(MainClass.this,null,"dungeon_end_"+i);
                        dungeon.setInProgress(false);
                        DungeonManager.dungeonMap.put("dungeon_end_"+i,dungeon);
                    }


                }
                //The addition of dungeon_test below is just for development purposes. TODO: Don't forget to remove it later.
                playerList.clear();
                playerList = Objects.requireNonNull(Bukkit.getWorld("dungeon_test")).getPlayers();
                if(playerList.size() > 0)
                {
                    DungeonManager.Dungeon dungeon = new DungeonManager.Dungeon(MainClass.this,playerList.get(0),"dungeon_test");
                    dungeon.setInProgress(true);
                    DungeonManager.dungeonMap.put("dungeon_test",dungeon);
                }
                else
                {
                    DungeonManager.Dungeon dungeon = new DungeonManager.Dungeon(MainClass.this,null,"dungeon_test");
                    dungeon.setInProgress(false);
                    DungeonManager.dungeonMap.put("dungeon_test",dungeon);
                }


            }
        },0);
        this.getServer().getConsoleSender().sendMessage(getPluginPrefix() + "Enabled MMOPlugin v0.0.10");

        applicationChecker = new BukkitRunnable() {
            int connCounter = 0;
            @Override
            public void run() {
                if (Faction.applicationQueue.size() > 0)
                {
                    String top = Faction.applicationQueue.peek();
                    int index = top.indexOf(";");
                    String factionName = top.substring(0,index);
                    UUID uuid = UUID.fromString(top.substring(index+1,top.length()));
                    Faction faction = factionMap.get(factionName.toLowerCase());
                    if(faction.getApplications().get(uuid).isBeforeNow())
                    {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                        if(p.isOnline())
                        {
                            p.getPlayer().sendMessage(getPluginPrefix() + ChatColor.BLUE + factionName + ChatColor.RED + " adlı loncaya olan başvuruna vaktinde cevap gelmediğinden dolayı başvurun iptal oldu! ");
                        }
                        faction.removeApplication(uuid);
                    }
                }
                connCounter++;
                if(connCounter != 4)
                {
                    try
                    {
                        PreparedStatement ps = conn.prepareStatement("SELECT 1"); //Ping the server to keep the connection alive.
                        ps.execute();
                        ps.close();
                        ps = null;
                    }
                    catch (SQLException throwables) { //Oh no connection is gone! Try to reconnect.
                        try
                        {
                            if(!conn.isClosed())
                                conn.close();
                            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/aviana", "root", "");
                        }
                        catch (SQLException e)
                        {
                            //Nothing.
                        }
                        throwables.printStackTrace();
                    }
                }
                else
                {
                    connCounter = 0;
                    try
                    {
                        //conn.close();
                        //getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.GREEN + "Successfully closed the SQL connection.");
                        //conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/aviana", "root", ""); // conn = DriverManager.getConnection("jdbc:mysql://remotemysql.com:3306/MnojkxD0Cc", "MnojkxD0Cc", "ZcoT9T7Pre");

                        //getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.GREEN + "Successfully re-established the SQL connection.");
                        //getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.AQUA + "Checking for items to be sold...");
                        PreparedStatement ps = conn.prepareStatement("select id,base64String,Name,Seller from auction_items where EndDate <= now()");
                        ResultSet rs = ps.executeQuery();
                        while(rs.next())
                        {
                            //getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.GOLD + "Found items ready to be sold.");
                            //These items should get sold. If they have a bidder.
                            //So lets first check if anyone has bid on this item.
                            int itemId = rs.getInt("id");
                            String base64String = rs.getString("base64String");
                            String sellerUUIDString = rs.getString("Seller");
                            OfflinePlayer seller = Bukkit.getOfflinePlayer(UUID.fromString(sellerUUIDString));
                            String itemName = rs.getString("Name");

                            ps = conn.prepareStatement("select player_id, bidAmount from items_bidders where item_id=? order by bidAmount DESC");
                            ps.setInt(1,itemId);
                            ResultSet secondRs = ps.executeQuery();
                            OfflinePlayer buyer = null;
                            int bidAmount = 0;
                            while (secondRs.next())
                            {
                                //This item did have a bidder.
                                if (buyer == null)
                                {
                                    //get the winner/buyer.
                                    //And insert it into bought items of Player.

                                    int howManyItems = 0;

                                    ps = conn.prepareStatement("select count(*) as cnt from player_boughtItems group by player_id");
                                    ResultSet countRS = ps.executeQuery();
                                    if(countRS.next())
                                    {
                                        howManyItems = countRS.getInt("cnt");
                                    }
                                    ps.close();


                                    bidAmount = secondRs.getInt("bidAmount");
                                    String buyerUUIDString = secondRs.getString("player_id");
                                    ps = conn.prepareStatement("insert into player_boughtItems values (?,?,?)");
                                    ps.setString(1,buyerUUIDString);
                                    ps.setString(2, base64String);
                                    ps.setInt(3,((howManyItems/21)+1));
                                    ps.executeUpdate();
                                    ps.close();

                                    UUID buyersUUID = UUID.fromString(buyerUUIDString);
                                    buyer = Bukkit.getOfflinePlayer(buyersUUID);
                                    if(buyer.isOnline())
                                    {
                                        //Player is online so send him a message
                                        buyer.getPlayer().sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + seller.getName() + ChatColor.WHITE + " adlı oyuncunun " + ChatColor.GREEN + itemName + ChatColor.WHITE + " adlı eşya için yarattığı açık artırmayı kazandınız! Eşyayı Market NPC'sinden alabilirsiniz." );
                                    }
                                    else
                                    {
                                        getConfig().set("PlayerLevels."+buyerUUIDString+".yeniEşya",true);
                                        saveConfig();
                                    }
                                }
                                else
                                {
                                    //Now as the item has been sold, we give back the money of the other people who have placed a bid.
                                    int bidAmound = secondRs.getInt("bidAmound");
                                    String bidderUUID = rs.getString("player_id");

                                    OfflinePlayer bidder = Bukkit.getOfflinePlayer(UUID.fromString(bidderUUID));
                                    if(bidder.isOnline())
                                    {
                                        //Player is online, so just message him and give him his money back.
                                        bidder.getPlayer().sendMessage(getPluginPrefix() + ChatColor.BLUE + seller.getName() + ChatColor.WHITE + " adlı oyuncunun " + ChatColor.GREEN + itemName + ChatColor.WHITE + " adlı eşya için yarattığı açık artırmayı kaybettiniz. Paranız geri verildi. ");
                                        if(classObjectMap.containsKey(bidder.getUniqueId()))
                                        {
                                            MMOClass.gainCurrency(bidder.getPlayer(),bidAmound);
                                        }
                                    }
                                }
                            }

                            ps.close();

                            ps = conn.prepareStatement("delete from items_bidders where item_id=?");
                            ps.setInt(1,itemId);
                            ps.executeUpdate();

                            secondRs.close();
                            secondRs = null;
                            if(buyer == null)
                            {
                                //No one has bid on this item! Give it back to its owner!

                                int howManyItems = 0;

                                ps = conn.prepareStatement("select count(*) as cnt from player_boughtItems group by player_id");
                                ResultSet countRS = ps.executeQuery();
                                if(countRS.next())
                                {
                                    howManyItems = countRS.getInt("cnt");
                                }
                                ps.close();

                                ps = conn.prepareStatement("insert into player_boughtItems values (?,?,?)");
                                ps.setString(1,sellerUUIDString);
                                ps.setString(2, base64String);
                                ps.setInt(3,((howManyItems/21)+1));
                                ps.executeUpdate();
                                ps.close();

                                if(seller.isOnline())
                                {
                                    seller.getPlayer().sendMessage(getPluginPrefix() + "Satmaya çalıştığınız, " + ChatColor.BLUE + itemName + ChatColor.WHITE + " adlı eşyanın açık artırma süresi doldu, teklif veren kimse olmadığından dolayı eşyanız açık artırmadan çıktı. Eşyayı Market NPC'sinden alabilirsiniz.");
                                }
                                else
                                {
                                    getConfig().set("PlayerLevels."+sellerUUIDString+".yeniEşya",true);
                                    saveConfig();
                                }

                            }
                            else
                            {
                                //Someone did buy this item! Give the money to the seller!
                                if(seller.isOnline())
                                {
                                    seller.getPlayer().sendMessage(getPluginPrefix() + "Satmaya çalıştığınız, " + ChatColor.BLUE + itemName + ChatColor.WHITE + " adlı eşya, "+ ChatColor.GOLD + buyer.getName() + ChatColor.WHITE + " adlı oyuncuya satıldı!");
                                    MMOClass.gainCurrency(seller.getPlayer(),bidAmount);
                                }
                                else
                                {
                                    int oldSellerCurrency = getConfig().getInt("PlayerLevels."+sellerUUIDString+".currency");
                                    oldSellerCurrency += bidAmount;

                                    int oldSoldAmount = 0;
                                    if(getConfig().contains("PlayerLevels."+sellerUUIDString+".soldAmount"))
                                        oldSoldAmount = getConfig().getInt("PlayerLevels."+sellerUUIDString+".soldAmount");

                                    bidAmount+=oldSoldAmount;

                                    getConfig().set("PlayerLevels."+sellerUUIDString+".currency",oldSellerCurrency);
                                    getConfig().set("PlayerLevels."+sellerUUIDString+".soldAmount",bidAmount);
                                    saveConfig();
                                }
                            }

                            ps = conn.prepareStatement("delete from auction_items where id=?");
                            ps.setInt(1,itemId);
                            ps.executeUpdate();
                        }
                        ps.close();
                        rs.close();

                        ps = null;
                        rs = null;
                    }
                    catch (SQLException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskTimerAsynchronously(this,60, 60*20); //TODO: CHECK IF ASYNC IS CAUSING PROBLEMS.
    }

    @Override
    public void onDisable()
    {
        if(!Bukkit.getOnlinePlayers().isEmpty())
        {
            for(Player player : Bukkit.getOnlinePlayers())
            {
                PacketReader reader = new PacketReader(this);
                reader.uninject(player);
                for(EntityPlayer npc : NPCManager.getNPCs())
                {
                    NPCManager.removeNPC(player, npc);
                }
                if(classObjectMap.containsKey(player.getUniqueId()) && classObjectMap.get(player.getUniqueId()).getClassName().equalsIgnoreCase("necromancer"))
                {
                    Necromancer necromancer = (Necromancer)classObjectMap.get(player.getUniqueId());
                    if (necromancer.getSummon() != null)
                    {
                        if(necromancer.getSummon().getRidingSummon() != null)
                            necromancer.getSummon().getRidingSummon().kill();
                        necromancer.getSummon().kill();
                    }
                }
                player.kickPlayer(ChatColor.RED + "Server kapanıyor!");
                PlayerQuitEvent event = new PlayerQuitEvent(player,"");
                Bukkit.getPluginManager().callEvent(event);
            }
        }
        getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.RED +"Starting to kill entities in dungeons. Please don't force quit.");
        try {
            for(int i = 0; i < 5; i++)
            {
                getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.GREEN + "Killing mobs in: " + ChatColor.BLUE + "dungeon_nether_"+i);
                for(LivingEntity e : Bukkit.getWorld("dungeon_nether_"+i).getLivingEntities())
                {
                    e.remove();
                }
                getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.GREEN + "Killing mobs in: " + ChatColor.BLUE + "dungeon_jungle_"+i);
                for(LivingEntity e : Bukkit.getWorld("dungeon_jungle_"+i).getLivingEntities())
                {
                    e.remove();
                }
                getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.GREEN + "Killing mobs in: " + ChatColor.BLUE + "dungeon_end_"+i);
                for(LivingEntity e : Bukkit.getWorld("dungeon_end_"+i).getLivingEntities())
                {
                    e.remove();
                }
            }
            getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.GREEN + "Killing mobs in: " + ChatColor.BLUE + "openworld_emnia");
            for(LivingEntity e : Bukkit.getWorld("openworld_emnia").getLivingEntities())
            {
                e.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(conn != null)
                conn.close();
            getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.GREEN + "Successfully closed the SQL connection.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        FileConfiguration fconfig = Faction.config.getConfig();
        for(String factionName : factionMap.keySet())
        {
            Faction temp = factionMap.get(factionName);
            factionName = temp.getFactionName();
            fconfig.set("FactionDetails."+factionName+".leader", temp.getOwner().toString());
            fconfig.set("FactionDetails."+factionName+".joinPerms", temp.getJoinType().toString());
            fconfig.set("FactionDetails."+factionName+".claims", temp.getClaimedCount());
            fconfig.set("FactionDetails."+factionName+".power", temp.getPower());
            fconfig.set("FactionDetails."+factionName+".size", temp.getSize());
            fconfig.set("FactionDetails."+factionName+".enemies", null);
            fconfig.set("FactionDetails."+factionName+".allies", null);
            fconfig.set("FactionDetails."+factionName+".cooldowns",null);
            fconfig.set("FactionDetails."+factionName+".applications",null);
            fconfig.set("FactionDetails."+factionName+".home",temp.getHome());
            for(String enemy : temp.getEnemies().keySet())
            {
                fconfig.set("FactionDetails."+factionName+".enemies." + enemy, "");
            }
            for(String ally : temp.getAllyFactions().keySet())
            {
                fconfig.set("FactionDetails."+factionName+".allies." + ally, "");
            }
            for(String cooldown : temp.getWarCoolDownMap().keySet())
            {
                fconfig.set("FactionDetails."+factionName+".cooldowns." + cooldown, Minutes.minutesBetween(new DateTime(), temp.getWarCoolDownMap().get(cooldown)).getMinutes());
            }
            for(UUID uuid : temp.getApplications().keySet())
            {
                fconfig.set("FactionDetails."+factionName+".applications."+uuid,Minutes.minutesBetween(new DateTime(), temp.getApplications().get(uuid)).getMinutes());
            }
        }
        fconfig.set("ApplicationQueue",null);
        for(String s : Faction.applicationQueue)
        {
            fconfig.set("ApplicationQueue."+s,"");
        }
        Faction.config.saveConfig();
    }


    public static void registerHealthBar()
    {
        if(mainScoreboard.getObjective("health") != null)
        {
            mainScoreboard.getObjective("health").unregister();
        }
        Objective o = mainScoreboard.registerNewObjective("health", "health", ChatColor.RED + "❤");
        o.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    public static void registerNameTag()
    {
        if(mainScoreboard.getTeam(Integer.toString(latestTeamID)) != null)
        {
            mainScoreboard.getTeam(Integer.toString(latestTeamID)).unregister();
        }
        Team t = mainScoreboard.registerNewTeam(Integer.toString(latestTeamID));
        latestTeamID++;
    }

    @SuppressWarnings("deprecation")
    public static void createScoreboard(Player player, int level, double xp, String className)
    {

        registerHealthBar();
        registerNameTag();
        registerHealthBar();
        int oldTeamID = latestTeamID-1;
        mainScoreboard.getTeam(Integer.toString(oldTeamID)).addPlayer(player);
        mainScoreboard.getTeam(Integer.toString(oldTeamID)).setPrefix(ChatColor.WHITE + "LVL " + Integer.toString(level) + " " + MMOClass.getClassBasedColour(className) + (className.equalsIgnoreCase("necromancer") ? "SUMMONER" : className.toUpperCase()) + " ");

        playerTeamMap.put(player.getUniqueId(), oldTeamID);
        player.setScoreboard(mainScoreboard);

    }

    public static void addToTaskMap(Player player, BukkitTask taskID)
    {
        if(playerTaskMap.containsKey(player.getUniqueId()))
        {
            List<BukkitTask> oldTaskList = playerTaskMap.get(player.getUniqueId());
            oldTaskList.add(taskID);
            playerTaskMap.put(player.getUniqueId(), oldTaskList);
        }
        else
        {
            List<BukkitTask> taskList = new ArrayList<>();
            taskList.add(taskID);
            playerTaskMap.put(player.getUniqueId(), taskList);
        }
    }
    @EventHandler
    public void playerWorldChangeEvent(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        NPCManager.addJoinPacket(player);

        if(player.getWorld().getName().equals("openworld_emnia"))
        {
            WrapperPlayServerUpdateTime time = new WrapperPlayServerUpdateTime();
            time.setTimeOfDay(emniaTime);
            time.sendPacket(player);
        }

        if(classObjectMap.containsKey(player.getUniqueId()))
        {
            MMOClass mmoClass = classObjectMap.get(player.getUniqueId());
            if(mmoClass.getClassName().equalsIgnoreCase("necromancer"))
            {
                Necromancer necromancer = (Necromancer)mmoClass;
                if(necromancer.getSummon() != null)
                {
                    if(Necromancer.canSpawnSummon(player.getWorld().getName()))
                    {
                        getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.GREEN + "Trying to teleport the summon to the summoner.");
                        necromancer.getSummon().teleportToPlayer();
                    }
                    else
                    {
                        player.sendMessage(getPluginPrefix() + "Işınlandığınız dünyada, canavar canlanmasına izin olmadığından dolayı summonınız otomatik olarak de-spawnlandı.");
                        necromancer.getSummon().kill();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event)
    {
        Entity entity = event.getPlayer();
        Player joinedPlayer = (Player) entity;
        //Location location2 = getServer().getWorld("world2").getSpawnLocation();
        //joinedPlayer.teleport(location2);
        PacketReader reader = new PacketReader(this);
        double hp = joinedPlayer.getHealth();
        joinedPlayer.setWalkSpeed(0.2f);
        //double baseMS = joinedPlayer.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getDefaultValue();
        //getServer().getConsoleSender().sendMessage(getPluginPrefix() + "Base ms is: " + baseMS);
        //getServer().getConsoleSender().sendMessage(getPluginPrefix() + "Base walkspeed is: " + joinedPlayer.getWalkSpeed());
        //getServer().getConsoleSender().sendMessage(getPluginPrefix() + "Base ms attribute is: " + joinedPlayer.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getDefaultValue());
        if(hp > 1)
        {
            joinedPlayer.setHealth(hp - 1);
        }
        else
        {
            joinedPlayer.setHealth(hp+1);
        }
        joinedPlayer.setHealth(hp);
        reader.inject(joinedPlayer);
        if(joinedPlayer.getWorld().getName().equals("openworld_emnia"))
        {
            joinedPlayer.teleport(Bukkit.getWorld("world").getSpawnLocation());
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                ActionBarUtil.sendTitle(joinedPlayer,ChatColor.DARK_AQUA + "AVIANA REBORN", ChatColor.YELLOW + "Kuralları okumayı unutmayın, bize websitemizden veya discordumuzdan ulaşabilirsiniz.", 15*20, 7*20, 10*20);
                //ActionBarUtil.sendActionBar(joinedPlayer,ChatColor.YELLOW + "Kuralları okumayı unutmayın, bize websitemizden veya discordumuzdan ulaşabilirsiniz.", 15*20, 5*20, 10*20);
            }
        },0);

        joinedPlayer.sendMessage(getPluginPrefix() + ChatColor.AQUA + "AVIANA REBORN" + ChatColor.GOLD + "'a hoşgeldin, " + ChatColor.WHITE + joinedPlayer.getName());
        try
        {
            CraftPlayer craftPlayer = (CraftPlayer) joinedPlayer;
            GameProfile gameProfile = craftPlayer.getProfile();
            String[] skinString = NPCManager.getSkin(joinedPlayer,joinedPlayer.getName());
            gameProfile.getProperties().put("textures", new Property("textures", skinString[0], skinString[1]));
            joinedPlayer.sendMessage(getPluginPrefix() + "Skininiz uygulandı!");
        }
        catch (Exception e)
        {
            joinedPlayer.sendMessage(getPluginPrefix() + "Skininiz uygulanamadı.");
        }
        UUID uuid = event.getPlayer().getUniqueId();
        String className = this.getConfig().getString("ClassList." + uuid + ".Class");
        if(className == null)
        {
            this.getLogger().info("CLASS NAME IS NULL");
            event.getPlayer().sendMessage(ChatColor.RED + "Daha sınıf seçmediniz.");
            //playerClassMap.put(event.getPlayer().getUniqueId(), "NULL");
        }
        else
        {
            this.getLogger().info("CLASS NAME IS " + className);
            //playerClassMap.put(event.getPlayer().getUniqueId(), className);

            String faction = null;
            getServer().getConsoleSender().sendMessage("Playerfaction is," + Faction.config.getConfig().get("PlayerFactions." + uuid));
            if(Faction.config.getConfig().get("PlayerFactions." + uuid) != null)
                faction = Faction.config.getConfig().getString("PlayerFactions." + uuid + ".Name");
            int lvl = getConfig().getInt("PlayerLevels." + uuid + ".level");
            double xp = getConfig().getDouble("PlayerLevels." + uuid + ".xp");
            int currency = getConfig().getInt("PlayerLevels." + uuid + ".currency");
            if (getConfig().contains("PlayerLevels." + uuid + ".yeniEşya"))
            {
                boolean yeniEşya = getConfig().getBoolean("PlayerLevels." + uuid + ".yeniEşya");
                if(yeniEşya)
                {
                    joinedPlayer.sendMessage(getPluginPrefix() + "Açık artırmadan bazı yeni eşyalarınız var, Market NPC'si ile konuşun!");
                    getConfig().set("PlayerLevels." + uuid + ".yeniEşya",false);
                }
            }
            if (getConfig().contains("PlayerLevels." + uuid + ".soldAmount"))
            {
                int soldAmount = getConfig().getInt("PlayerLevels." + uuid + ".soldAmount");

                if(soldAmount > 0)
                {
                    joinedPlayer.sendMessage(getPluginPrefix() + "Çevrimdışıyken yaptığınız satışlardan, " + ChatColor.GOLD + soldAmount + " para " + ChatColor.WHITE + " kazandınız!");
                    getConfig().set("PlayerLevels." + uuid + ".soldAmount",null);
                }
            }
            else
            {
                getConfig().set("PlayerLevels." + uuid + ".yeniEşya",false);
            }
            if(lvl > 20)
            {
                if(lvl % 2 == 0)
                {
                    joinedPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(lvl);
                }
                else
                    joinedPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(lvl-1);
            }
            else
            {
                joinedPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
            }
            switch (className)
            {
                case "necromancer":
                {
                    Necromancer necromancer = new Necromancer(this, joinedPlayer, xp, lvl, className);
                    classObjectMap.put(joinedPlayer.getUniqueId(),necromancer);

                    HashMap<String, DateTime> necromancerSkillsCooldown = new HashMap<>();
                    int SummoncooldownRemaining = getConfig().getInt("PlayerCooldowns." + uuid + ".Summon");
                    DateTime dateTime = new DateTime();
                    dateTime = dateTime.plusSeconds(SummoncooldownRemaining);
                    necromancerSkillsCooldown.put("Summon", dateTime);

                    if(getConfig().contains("PlayerCooldowns."+uuid+".Thunderlords"))
                    {
                        int thunderLordCd = getConfig().getInt("PlayerCooldowns."+uuid+".Thunderlords");
                        DateTime dt = new DateTime().plusSeconds(thunderLordCd);
                        necromancerSkillsCooldown.put("thunderlords", dt);
                    }
                    if(getConfig().contains("PlayerCooldowns."+uuid+".EnginTutku"))
                    {
                        int engintutkucd = getConfig().getInt("PlayerCooldowns."+uuid+".EnginTutku");
                        DateTime dt = new DateTime().plusSeconds(engintutkucd);
                        necromancerSkillsCooldown.put("EnginTutku", dt);
                    }
                    if(SummoncooldownRemaining > 0)
                    {
                        joinedPlayer.sendMessage(getPluginPrefix() + "Your skill cooldown is still in place " + SummoncooldownRemaining + " seconds");
                        //BukkitTask task = new StartCooldown(playerCooldownMap.get(joinedPlayer.getUniqueId()),"Summon", joinedPlayer).runTaskTimer(this,0,20);
                        //addToTaskMap(joinedPlayer, task);
                    }

                    int summonHealCooldownRemaining = getConfig().getInt("PlayerCooldowns." + uuid + ".SummonHeal");
                    DateTime dateTime2 = new DateTime();
                    dateTime2 = dateTime2.plusSeconds(summonHealCooldownRemaining);
                    necromancerSkillsCooldown.put("SummonHeal", dateTime2);
                    playerCooldownMap.put(uuid, necromancerSkillsCooldown);
                    break;
                }
                case "warrior":
                {
                    //System.out.println("Default attack speed is " + joinedPlayer.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getDefaultValue());
                    Warrior tempWar = new Warrior(this, joinedPlayer, xp, lvl, className);
                    //joinedPlayer.sendMessage("Your item in off-hand is " + joinedPlayer.getInventory().getItemInOffHand().getItemMeta().getAttributeModifiers(EquipmentSlot.OFF_HAND));
                    //joinedPlayer.sendMessage("Your item in off-hand is " + joinedPlayer.getInventory().getItemInOffHand().getItemMeta().getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE));
                    classObjectMap.put(joinedPlayer.getUniqueId(),tempWar);


                    HashMap<String, DateTime> warriorSkillsCooldown = new HashMap<>();
                    int currentwhirlwindcd = getConfig().getInt("PlayerCooldowns." + uuid + ".Whirlwind");
                    int currentsundercd = getConfig().getInt("PlayerCooldowns." + uuid + ".Sunder");
                    DateTime wwDateTime = new DateTime();
                    DateTime sunderDateTime = new DateTime();
                    wwDateTime = wwDateTime.plusSeconds(currentwhirlwindcd);
                    sunderDateTime = sunderDateTime.plusSeconds(currentsundercd);
                    warriorSkillsCooldown.put("Whirlwind", wwDateTime);
                    warriorSkillsCooldown.put("Sunder", sunderDateTime);
                    playerCooldownMap.put(uuid, warriorSkillsCooldown);
                    if(currentwhirlwindcd > 0)
                    {
                        joinedPlayer.sendMessage(getPluginPrefix() + "Your WW cooldown is still in place " + currentwhirlwindcd + " seconds");
                        //BukkitTask task = new StartCooldown(playerCooldownMap.get(joinedPlayer.getUniqueId()),"Whirlwind", joinedPlayer).runTaskTimer(this,0,20);
                        //addToTaskMap(joinedPlayer, task);
                    }
                    if(currentsundercd > 0)
                    {
                        joinedPlayer.sendMessage(getPluginPrefix() + "Your Sunder cooldown is still in place " + currentsundercd + " seconds");
                        //BukkitTask task = new StartCooldown(playerCooldownMap.get(joinedPlayer.getUniqueId()),"Sunder", joinedPlayer).runTaskTimer(this,0,20);
                        //addToTaskMap(joinedPlayer, task);
                    }
                    if(lvl > 74)
                    {
                        WarriorEventManager.adjustWarriorAttackSpeed(joinedPlayer);
                    }
                    else
                        joinedPlayer.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4);
                    break;
                }
                case "cleric":
                {
                    Cleric cleric = new Cleric(this, joinedPlayer, xp, lvl, className);
                    classObjectMap.put(joinedPlayer.getUniqueId(),cleric);
                    int currentLeashCd = getConfig().getInt("PlayerCooldowns." + uuid + ".Leash");
                    DateTime leashDateTime = new DateTime();
                    leashDateTime = leashDateTime.plusSeconds(currentLeashCd);
                    HashMap<String, DateTime> clericSkillCooldown = new HashMap<>();
                    clericSkillCooldown.put("Leash", leashDateTime);
                    playerCooldownMap.put(uuid, clericSkillCooldown);
                    /*
                    *
                    *
                    *
                    * */
                    break;
                }
            }
            classObjectMap.get(joinedPlayer.getUniqueId()).setCurrency(currency);
            createScoreboard(joinedPlayer, lvl, xp, className);
            if(getConfig().contains("PlayerApplications."+uuid))
            {
                HashMap<Faction, DateTime> playerApplications = new HashMap<>();
                getConfig().getConfigurationSection("PlayerApplications."+uuid).getKeys(false).forEach(appliedFactionName -> {
                    if(factionMap.containsKey(appliedFactionName.toLowerCase()))
                    {
                        playerApplications.put(factionMap.get(appliedFactionName.toLowerCase()), null);
                    }
                });
                classObjectMap.get(joinedPlayer.getUniqueId()).setApplications(playerApplications);
            }
            if (faction != null)
            {
                if(factionMap.containsKey(faction.toLowerCase()))
                {
                    Faction.Ranks rank = Faction.Ranks.valueOf(Faction.config.getConfig().getString("PlayerFactions."+joinedPlayer.getUniqueId()+".Rank"));
                    classObjectMap.get(joinedPlayer.getUniqueId()).setFaction(faction,rank);
                }
            }
            else
            {
                classObjectMap.get(joinedPlayer.getUniqueId()).setFaction(null,null);
            }
            PrivateSideBar.createPrivateSideBar(joinedPlayer);
            MMOClass.adjustXPBar(joinedPlayer);
            //createSideScoreboard(joinedPlayer, lvl, xp, className);

            if(getConfig().contains("PlayerCooldowns." + uuid + ".End"))
            {
                int year = getConfig().getInt("PlayerCooldowns."+uuid+".End"+".Year");
                int month = getConfig().getInt("PlayerCooldowns."+uuid+".End"+".Month");
                int day = getConfig().getInt("PlayerCooldowns."+uuid+".End"+".Day");
                int hour = getConfig().getInt("PlayerCooldowns."+uuid+".End"+".Hour");
                int minute = getConfig().getInt("PlayerCooldowns."+uuid+".End"+".Minute");
                DateTime endDateTime = new DateTime(year,month,day,hour,minute);
                playerCooldownMap.get(uuid).put("end",endDateTime);
            }
            if(getConfig().contains("PlayerCooldowns." + uuid + ".Nether"))
            {
                int year = getConfig().getInt("PlayerCooldowns."+uuid+".Nether"+".Year");
                int month = getConfig().getInt("PlayerCooldowns."+uuid+".Nether"+".Month");
                int day = getConfig().getInt("PlayerCooldowns."+uuid+".Nether"+".Day");
                int hour = getConfig().getInt("PlayerCooldowns."+uuid+".Nether"+".Hour");
                int minute = getConfig().getInt("PlayerCooldowns."+uuid+".Nether"+".Minute");
                DateTime endDateTime = new DateTime(year,month,day,hour,minute);
                playerCooldownMap.get(uuid).put("nether",endDateTime);
            }
            if(getConfig().contains("PlayerCooldowns." + uuid + ".Jungle"))
            {
                int year = getConfig().getInt("PlayerCooldowns."+uuid+".Jungle"+".Year");
                int month = getConfig().getInt("PlayerCooldowns."+uuid+".Jungle"+".Month");
                int day = getConfig().getInt("PlayerCooldowns."+uuid+".Jungle"+".Day");
                int hour = getConfig().getInt("PlayerCooldowns."+uuid+".Jungle"+".Hour");
                int minute = getConfig().getInt("PlayerCooldowns."+uuid+".Jungle"+".Minute");
                DateTime endDateTime = new DateTime(year,month,day,hour,minute);
                playerCooldownMap.get(uuid).put("jungle",endDateTime);
            }
        }
        return;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onEntitySpawnEvent(EntitySpawnEvent event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof LivingEntity && !(entity instanceof Player))
        {
            if(!(entity instanceof ArmorStand) && entity instanceof CraftEntity && ( ((CraftEntity)entity).getHandle() instanceof LeveledMob || ((CraftEntity)entity).getHandle() instanceof Necromancer.Summon))
            {
                if( ((CraftEntity)entity).getHandle() instanceof LeveledMob && entity.getWorld().getName().contains("faction_") || entity.getWorld().getName().equals("openworld_emnia"))
                    return;

                String oldMobName = "";
                if (entity.getCustomName() != null)
                    oldMobName = entity.getCustomName();
                else
                    oldMobName = entity.getName();
                LivingEntity le = (LivingEntity) entity;
                String health = String.format("%.2f", le.getMaxHealth());
                oldMobName += ChatColor.WHITE + " : " + health + ChatColor.RED + "❤";
                entity.setCustomNameVisible(true);
                entity.setCustomName(oldMobName);
            }
        }
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent event)
    {
        UUID uuid = event.getPlayer().getUniqueId();
        PacketReader reader = new PacketReader(this);
        reader.uninject(event.getPlayer());
        if(classObjectMap.containsKey(uuid)) //!playerClassMap.get(uuid).equals("NULL")
        {
           MMOClass mmoClass = classObjectMap.get(uuid);
           String className = mmoClass.getClassName();
           this.getConfig().set("ClassList." + uuid + ".Class", className);
           switch (className)
           {
               case"necromancer":
               {
                   if(playerCooldownMap.containsKey(uuid) && playerCooldownMap.get(uuid).containsKey("Summon") && playerCooldownMap.get(uuid).get("Summon").isAfterNow())
                   {
                       getConfig().set("PlayerCooldowns."+uuid+".Summon",Seconds.secondsBetween(new DateTime(), playerCooldownMap.get(uuid).get("Summon")).getSeconds());
                   }
                   else
                   {
                       getConfig().set("PlayerCooldowns."+uuid+".Summon",0);
                   }
                   if(MainClass.playerCooldownMap.containsKey(uuid) && MainClass.playerCooldownMap.get(uuid).containsKey("thunderlords") && MainClass.playerCooldownMap.get(uuid).get("thunderlords").isAfterNow())
                   {
                       getConfig().set("PlayerCooldowns."+uuid+".Thunderlords",Seconds.secondsBetween(new DateTime(), playerCooldownMap.get(uuid).get("thunderlords")).getSeconds());
                   }
                   else
                   {
                       getConfig().set("PlayerCooldowns."+uuid+".Thunderlords",0);
                   }
                   if(MainClass.playerCooldownMap.containsKey(uuid) && MainClass.playerCooldownMap.get(uuid).containsKey("engintutku") && MainClass.playerCooldownMap.get(uuid).get("engintutku").isAfterNow())
                   {
                       getConfig().set("PlayerCooldowns."+uuid+".EnginTutku",Seconds.secondsBetween(new DateTime(), playerCooldownMap.get(uuid).get("engintutku")).getSeconds());
                   }
                   else
                   {
                       getConfig().set("PlayerCooldowns."+uuid+".EnginTutku",0);
                   }
                   Necromancer necromancer = (Necromancer)classObjectMap.get(event.getPlayer().getUniqueId());
                   if (necromancer.getSummon() != null)
                   {
                       necromancer.getSummon().kill();
                   }
                   break;
               }
               case "warrior":
               {
                   if(playerCooldownMap.containsKey(uuid) && playerCooldownMap.get(uuid).containsKey("Whirlwind") && playerCooldownMap.get(uuid).get("Whirlwind").isAfterNow())
                   {
                       getConfig().set("PlayerCooldowns."+uuid+".Whirlwind",Seconds.secondsBetween(new DateTime(), playerCooldownMap.get(uuid).get("Whirlwind")).getSeconds());
                   }
                   else
                   {
                       getConfig().set("PlayerCooldowns."+uuid+".Whirlwind",0);
                   }
                   if(playerCooldownMap.containsKey(uuid) && playerCooldownMap.get(uuid).containsKey("Sunder") && playerCooldownMap.get(uuid).get("Sunder").isAfterNow())
                   {
                       getConfig().set("PlayerCooldowns."+uuid+".Sunder",Seconds.secondsBetween(new DateTime(), playerCooldownMap.get(uuid).get("Sunder")).getSeconds());
                   }
                   else
                   {
                       getConfig().set("PlayerCooldowns."+uuid+".Sunder",0);
                   }
                   break;
               }
               case "cleric":
               {
                   if(playerCooldownMap.containsKey(uuid) && playerCooldownMap.get(uuid).containsKey("Leash") && playerCooldownMap.get(uuid).get("Leash").isAfterNow())
                   {
                       getConfig().set("PlayerCooldowns." + uuid + ".Leash",Seconds.secondsBetween(new DateTime(), playerCooldownMap.get(uuid).get("Leash")).getSeconds());
                   }
                   else
                   {
                       getConfig().set("PlayerCooldowns."+uuid+".Leash",0);
                   }
                   break;
               }
           }
           this.getConfig().set("PlayerLevels."+uuid+".level",classObjectMap.get(uuid).getLevel());
           this.getConfig().set("PlayerLevels."+uuid+".xp",classObjectMap.get(uuid).getXp());
           this.getConfig().set("PlayerLevels."+uuid+".currency",classObjectMap.get(uuid).getCurrency());
           for(Faction applied : mmoClass.getApplications().keySet())
           {
               this.getConfig().set("PlayerApplications."+uuid+"."+applied.getFactionName(), "");
           }
           this.saveConfig();
           if(playerTaskMap.get(uuid) != null)
           {
               for (BukkitTask task : playerTaskMap.get(uuid))
               {
                   this.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "IN THE FOR LOOP TASK ID IS " + ChatColor.GOLD + task + " / " + task.getTaskId() + ChatColor.RED +" TASK CLASS: " +task.getClass());
                   Bukkit.getScheduler().cancelTask(task.getTaskId());
               }
           }
            if(!mmoClass.getCurrentDungeon().equalsIgnoreCase(""))
            {
                DungeonManager.dungeonMap.get(mmoClass.getCurrentDungeon()).getPlayerList().remove(event.getPlayer());
                if(DungeonManager.dungeonMap.get(mmoClass.getCurrentDungeon()).getPlayerList().size() == 0)
                {
                    DungeonManager.dungeonMap.get(mmoClass.getCurrentDungeon()).setInProgress(false);
                    for(Entity e : Bukkit.getWorld(mmoClass.getCurrentDungeon()).getEntities())
                    {
                        if(!(e instanceof Player))
                            e.remove();
                    }
                }
            }
           if(mmoClass.getFaction() != null)
           {
               factionMap.get(mmoClass.getFaction().toLowerCase()).removeAsOnlineFactionMember(event.getPlayer());
               Faction.config.getConfig().set("PlayerFactions."+uuid+".Name",mmoClass.getFaction());
               Faction.config.getConfig().set("PlayerFactions."+uuid+".Rank",mmoClass.getRank().toString());
           }
           else
           {
               Faction.config.getConfig().set("PlayerFactions."+uuid+".Name",null);
               Faction.config.getConfig().set("PlayerFactions."+uuid+".Rank",null);
           }
            if(MainClass.playerCooldownMap.get(uuid).containsKey("end"))
            {

                int year = playerCooldownMap.get(uuid).get("end").getYear();
                int month = playerCooldownMap.get(uuid).get("end").getMonthOfYear();
                int day = playerCooldownMap.get(uuid).get("end").getDayOfMonth();
                int hour = playerCooldownMap.get(uuid).get("end").getHourOfDay();
                int minute = playerCooldownMap.get(uuid).get("end").getMinuteOfDay();

                getConfig().set("PlayerCooldowns."+uuid+".End"+".Year",year);
                getConfig().set("PlayerCooldowns."+uuid+".End"+".Month",month);
                getConfig().set("PlayerCooldowns."+uuid+".End"+".Day",day);
                getConfig().set("PlayerCooldowns."+uuid+".End"+".Hour",hour);
                getConfig().set("PlayerCooldowns."+uuid+".End"+".Minute",minute);
            }
            if(MainClass.playerCooldownMap.get(uuid).containsKey("nether"))
            {
                int year = playerCooldownMap.get(uuid).get("Nether").getYear();
                int month = playerCooldownMap.get(uuid).get("Nether").getMonthOfYear();
                int day = playerCooldownMap.get(uuid).get("Nether").getDayOfMonth();
                int hour = playerCooldownMap.get(uuid).get("Nether").getHourOfDay();
                int minute = playerCooldownMap.get(uuid).get("Nether").getMinuteOfDay();

                getConfig().set("PlayerCooldowns."+uuid+".Nether"+".Year",year);
                getConfig().set("PlayerCooldowns."+uuid+".Nether"+".Month",month);
                getConfig().set("PlayerCooldowns."+uuid+".Nether"+".Day",day);
                getConfig().set("PlayerCooldowns."+uuid+".Nether"+".Hour",hour);
                getConfig().set("PlayerCooldowns."+uuid+".Nether"+".Minute",minute);
            }
            if(MainClass.playerCooldownMap.get(uuid).containsKey("jungle"))
            {
                int year = playerCooldownMap.get(uuid).get("Jungle").getMonthOfYear();
                int month = playerCooldownMap.get(uuid).get("Jungle").getYear();
                int day = playerCooldownMap.get(uuid).get("Jungle").getDayOfMonth();
                int hour = playerCooldownMap.get(uuid).get("Jungle").getHourOfDay();
                int minute = playerCooldownMap.get(uuid).get("Jungle").getMinuteOfDay();

                getConfig().set("PlayerCooldowns."+uuid+".Jungle"+".Year",year);
                getConfig().set("PlayerCooldowns."+uuid+".Jungle"+".Month",month);
                getConfig().set("PlayerCooldowns."+uuid+".Jungle"+".Day",day);
                getConfig().set("PlayerCooldowns."+uuid+".Jungle"+".Hour",hour);
                getConfig().set("PlayerCooldowns."+uuid+".Jungle"+".Minute",minute);
            }
            saveConfig();
            playerTaskMap.remove(uuid);
            playerCooldownMap.remove(uuid);
            classObjectMap.remove(uuid);
            Faction.config.saveConfig();
        }
    }

    @EventHandler
    public void playerXPChangeEvent(PlayerExpChangeEvent event)
    {
        //Player player = event.getPlayer();
        //getServer().getConsoleSender().sendMessage(getPluginPrefix() + "XP Change Event!");
        /*if(classObjectMap.containsKey(player.getUniqueId()) && classObjectMap.get(player.getUniqueId()).isXpAdjusted())
        {
            getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.RED +"XP WAS adjusted.");
            //Let the XP change and set the XpAdjusted to false
            classObjectMap.get(player.getUniqueId()).setXpAdjusted(false);
        }
        else
        {
            getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.GREEN +"XP was NOT adjusted.");
            //Otherwise block the attempt of XP changing.
            event.setAmount(0);
        }*/
        event.setAmount(0);
    }

    @EventHandler
    public void playerLVLChangeEvent(PlayerLevelChangeEvent event)
    {
        //getServer().getConsoleSender().sendMessage(getPluginPrefix() + "Player LEVEL change event.");
        Player player = event.getPlayer();
        if(classObjectMap.containsKey(player.getUniqueId()))
        {
            player.setLevel(classObjectMap.get(player.getUniqueId()).getLevel());
        }
        else
        {
            //Nothing. Player has not chosen a class yet.
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) //For canceling the zombie from getting damaged by fire.
    {
        Entity entity = event.getEntity();
        if(entity instanceof LivingEntity && !(entity instanceof Player))
        {
            if((((CraftEntity)entity).getHandle() instanceof LeveledMob) && event.getCause().equals(EntityDamageEvent.DamageCause.FALL))
            {
                //getServer().getConsoleSender().sendMessage(ChatColor.RED + "Inside the LEVELEDMOB IF");
                event.setCancelled(true);
            }
            else
            {
                try
                {
                    //getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "INSIDE THE IF");
                    if(!(entity instanceof ArmorStand) && (((CraftEntity) entity).getHandle() instanceof LeveledMob || ((CraftEntity) entity).getHandle() instanceof Necromancer.Summon))
                    {
                        if(event instanceof EntityDamageByEntityEvent)
                        {
                            EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent) event;
                            Entity damager = event2.getDamager();
                            if(damager instanceof Player && classObjectMap.containsKey(damager.getUniqueId()) && classObjectMap.get(damager.getUniqueId()).getClassName().equalsIgnoreCase("necromancer"))
                            {
                                Necromancer necromancer = (Necromancer) classObjectMap.get(damager.getUniqueId());
                                if(necromancer.getSummon() != null && necromancer.getSummon().getid().equals(entity.getUniqueId()))
                                {
                                    event.setCancelled(true);
                                    return;
                                }
                            }
                        }

                        if( ((CraftEntity) entity).getHandle() instanceof LeveledMob && entity.getWorld().getName().contains("faction_") || entity.getWorld().getName().equals("openworld_emnia"))
                            return;

                        LivingEntity le = (LivingEntity) entity;
                        BukkitTask later = new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    String oldName = "";
                                    if (le.getCustomName() != null)
                                        oldName = le.getCustomName();
                                    else
                                        oldName = le.getName();
                                    le.setCustomNameVisible(true);
                                    if (!oldName.equals("") && !entity.isDead()) {
                                        int index = oldName.indexOf(":");
                                        if (index > 0) {
                                            oldName = oldName.substring(0, index);
                                            Double newHealth = le.getHealth();
                                            if (newHealth > 0) {
                                                String health = String.format("%.2f", newHealth);
                                                oldName += ChatColor.WHITE + ": " + health + ChatColor.RED + "❤";
                                                entity.setCustomNameVisible(true);
                                                entity.setCustomName(oldName);
                                            } else {
                                                oldName += ChatColor.WHITE + ": " + "0" + ChatColor.RED + "❤";

                                                entity.setCustomNameVisible(true);
                                                entity.setCustomName(oldName);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    //e.printStackTrace();
                                }
                            }
                        }.runTaskLater(this,0);
                    }
                }
                catch (StringIndexOutOfBoundsException e)
                {
                    this.getServer().getConsoleSender().sendMessage(ChatColor.BLUE + "[" + ChatColor.LIGHT_PURPLE  + "MMOPlugin" + ChatColor.BLUE + "]" + ChatColor.GREEN + "A known error regarding the index of substring has occured");
                }
            }
        }
        else if(entity instanceof Player)
        {
            short durability =0;
            Player player = (Player) event.getEntity();
            for(ItemStack itemStack : player.getInventory().getArmorContents())
            {
                if(itemStack != null)
                    itemStack.setDurability(durability);
            }
            if(player.getInventory().getItemInOffHand().getType().equals(Material.SHIELD))
            {
                player.getInventory().getItemInOffHand().setDurability(durability);
            }
        }
    }

    public Plugin returnMainPlugin()
    {
        return this;
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event)
    {
        event.setDroppedExp(0);
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.getDrops().clear();
    }


    /*@EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event)
    {
        event.setCancelled(true);
    }*/


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();
        if(MainClass.classObjectMap.containsKey(player.getUniqueId()) && classObjectMap.get(player.getUniqueId()).getFaction() != null)
        {
            Faction faction = factionMap.get(classObjectMap.get(player.getUniqueId()).getFaction().toLowerCase());
            if(classObjectMap.get(player.getUniqueId()).getChatType().equals(Faction.ChatType.FACTION))
            {
                event.setCancelled(true);
                for(Player factionMember : faction.getFactionMembers())
                {
                    factionMember.sendMessage(ChatColor.GOLD + "{LONCA}" + ChatColor.BLUE + "[" + ChatColor.GREEN + faction.getFactionName().toUpperCase() + ChatColor.BLUE + "] " + ChatColor.WHITE + player.getName()+ ": " + ChatColor.BLUE +event.getMessage());
                }
                return;
            }
            else if(classObjectMap.get(player.getUniqueId()).getChatType().equals(Faction.ChatType.ALLIANCE))
            {
                event.setCancelled(true);
                faction.sendMessageToMembers(ChatColor.GOLD + "{İTTİFAK}" + ChatColor.BLUE + "[" + ChatColor.GREEN + faction.getFactionName().toUpperCase() + ChatColor.BLUE + "] " + ChatColor.WHITE + player.getName()+ ": " + ChatColor.BLUE +event.getMessage());
                for(String allyFactionName : faction.getAllyFactions().keySet())
                {
                    Faction allyFaction = factionMap.get(allyFactionName.toLowerCase());
                    allyFaction.sendMessageToMembers(ChatColor.GOLD + "{İTTİFAK}" + ChatColor.BLUE + "[" + ChatColor.GREEN + faction.getFactionName().toUpperCase() + ChatColor.BLUE + "] " + ChatColor.WHITE + player.getName()+ ": " + ChatColor.AQUA +event.getMessage());
                }
                return;
            }
        }
        if(player.getName().equals("Yoshiane") || player.getName().equals("Yuni")|| player.getName().equals("RedFear") || (classObjectMap.containsKey(player.getUniqueId()) && classObjectMap.get(player.getUniqueId()).getClassName().equalsIgnoreCase("admin")))
        {
            event.setFormat(ChatColor.RED + "" +ChatColor.BOLD + "[ADMIN]" + ChatColor.WHITE + event.getFormat());
        }
        if(MainClass.classObjectMap.containsKey(player.getUniqueId()))
        {
            String classname = classObjectMap.get(player.getUniqueId()).getClassName().toLowerCase();
            event.setFormat(MMOClass.getClassBasedColour(classname) + "[" + (classname.equalsIgnoreCase("necromancer") ? "SUMMONER" : classname.toUpperCase()) + "]" + ChatColor.WHITE + event.getFormat());
        }
    }

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent event)
    {
        if(event.getAnimationType().equals(PlayerAnimationType.ARM_SWING))
        {
            //event.getPlayer().sendMessage("You swinged arm");
        }
    }

    @SuppressWarnings("deprecation")


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        //player.sendMessage("Your item in hand is " + player.getItemInHand());

        if(classObjectMap.containsKey(player.getUniqueId()) && classObjectMap.get(player.getUniqueId()).getClassName().equalsIgnoreCase("warrior"))
        {
            if((event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && (player.getItemInHand().getType().equals(Material.WOODEN_SWORD) || player.getItemInHand().getType().equals(Material.STONE_SWORD) || player.getItemInHand().getType().equals(Material.NETHERITE_SWORD) || player.getItemInHand().getType().equals(Material.GOLDEN_SWORD) || player.getItemInHand().getType().equals(Material.DIAMOND_SWORD) || player.getItemInHand().getType().equals(Material.IRON_SWORD)))
            {
                MMOClass mmoClass = classObjectMap.get(player.getUniqueId());
                int level = mmoClass.getLevel();
                if (level > 24 && MMOClass.canUseSkills(player.getWorld().getName()))
                {
                    if (playerCooldownMap.get(player.getUniqueId()).get("Whirlwind").isBeforeNow())
                    {
                        createSpinningSwords(player);
                        DateTime dateTime =  new DateTime();
                        //System.out.println("DateTime was: " + dateTime);
                        dateTime = dateTime.plusSeconds(whirlwindCooldown);
                        //System.out.println("After ading the cooldown, it is: " + dateTime);
                        playerCooldownMap.get(player.getUniqueId()).put("Whirlwind", dateTime);
                        //BukkitTask taskID = new StartCooldown(playerCooldownMap.get(player.getUniqueId()),"Whirlwind", player).runTaskTimer(this,0,20);
                        //addToTaskMap(player,taskID);
                    }
                    else
                    {
                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Whirlwind yeteneğini kullanmak için, " + ChatColor.GOLD + (Seconds.secondsBetween(new DateTime(), playerCooldownMap.get(player.getUniqueId()).get("Whirlwind"))).getSeconds() + "saniye" + ChatColor.RED + " daha beklemelisin.");
                    }
                    event.setCancelled(true);
                }
            }
            else if((event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && (player.getItemInHand().getType().equals(Material.BOW) || player.getItemInHand().getType().equals(Material.CROSSBOW)))
            {
                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Warrior'lar ok kullanamaz");
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onBlockIgniteEvent(BlockIgniteEvent event)
    {
        if(event.getCause().equals(BlockIgniteEvent.IgniteCause.LIGHTNING))
        {
            event.setCancelled(true);
        }
    }


/*
    public static class StartCooldown extends BukkitRunnable
    {
        HashMap<String, Integer> specificPlayersCooldowns;
        String skillName;
        Player skillOwner;
        StartCooldown(HashMap<String, Integer> temp, String skill, Player player)
        {
            specificPlayersCooldowns = temp;
            skillName = skill;
            this.skillOwner = player;
        }
        @Override
        public void run()
        {
            int cooldownRemaining = specificPlayersCooldowns.get(skillName);
            if(cooldownRemaining > 0)
            {
                specificPlayersCooldowns.put(skillName,cooldownRemaining-1);
            }
            else
            {
                cancel();
                //getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Finished executing task, trying to remove...");
                for(BukkitTask task : playerTaskMap.get(skillOwner.getUniqueId()))
                {
                    if(task.getTaskId() == this.getTaskId())
                    {
                        //getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Found the task, removing, id is: " + this.getTaskId());
                        playerTaskMap.get(skillOwner.getUniqueId()).remove(task);
                        break;
                    }
                }
                if(skillName.equalsIgnoreCase("Sunder") && skillOwner.isOnline())
                {
                    skillOwner.sendMessage(ChatColor.GOLD + skillName.toUpperCase() + ChatColor.GREEN + " kullanılmaya hazır.");
                }
            }
        }
    }
*/
    public static class SpawnMobs extends BukkitRunnable
    {

        int currentCount;
        int mobCount;
        int xIncrease;
        int zIncrease;
        int levelHigherbound;
        int levelLowerbound;
        Random rand;
        Location location;
        double oldX;
        double oldZ;
        org.bukkit.World world;

        SpawnMobs(int mobCount, int xIncrease, int zIncrease, int levelLowerbound, int levelHigherbound, Player player)
        {
          this.mobCount = mobCount;
          this.currentCount = 0;
          this.xIncrease = xIncrease;
          this.zIncrease = zIncrease;
          this.levelLowerbound = levelLowerbound;
          this.levelHigherbound = levelHigherbound;
          this.location = player.getLocation();
          this.oldX = location.getX();
          this.oldZ = location.getZ();
          rand = new Random();
          this.world = player.getWorld();
        }

        @Override
        public void run()
        {
            //getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "Current mob count is " + currentCount + " and desired value is " + mobCount);
            int whichMob = rand.nextInt(3);
            double xToAdd = (xIncrease+1) * rand.nextDouble();
            double zToAdd = (zIncrease+1) * rand.nextDouble();
            //getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "X TO ADD: " + ChatColor.YELLOW +xToAdd + ChatColor.DARK_AQUA +" z TO ADD "+ ChatColor.YELLOW + zToAdd);
            int level = rand.nextInt(levelHigherbound-levelLowerbound) + levelLowerbound;
            customMobs customMob = customMobList.get(whichMob);
            location.setX(oldX + xToAdd);
            location.setZ(oldZ + zToAdd);
            switch (customMob)
            {
                case LEVELEDZOMBIE:
                {
                    LeveledZombie leveledZombie = new LeveledZombie(world,level,location);
                    break;
                }
                case LEVELEDSPIDER:
                {
                    LeveledSpider leveledSpider = new LeveledSpider(world,level,location);
                    break;
                }
                case LEVELEDCAVESPIDER:
                {
                    LeveledCaveSpider leveledCaveSpider = new LeveledCaveSpider(world,level,location);
                    break;
                }
            }
            currentCount++;
            if(currentCount > mobCount)
            {
                //getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "INSIDE THE IF STATEMENT, TRYING TO TERMINATE.");
                cancel();
                return;
            }
        }
    }

    public class SpinningSwordDamager extends BukkitRunnable
    {
        Player spinningSwordDamagerOwner;
        MMOClass mmoClass;
        int DamagerOwnerLevel;

        public boolean isLivingEntity(Entity entity)
        {
            return (entity instanceof LivingEntity && !(entity.getUniqueId().equals(spinningSwordDamagerOwner.getUniqueId())) && !(entity instanceof ArmorStand));
        }

        public boolean isDamageable(Entity entity)
        {
            if(entity.getUniqueId().equals(spinningSwordDamagerOwner.getUniqueId()))
                return false;
            return (entity instanceof Damageable);
        }

        SpinningSwordDamager(Player player)
        {
            mmoClass = classObjectMap.get(player.getUniqueId());
            this.spinningSwordDamagerOwner = player;
            DamagerOwnerLevel = mmoClass.getLevel();
            if(DamagerOwnerLevel > 44)
            {
                DamagerOwnerLevel = 45;
            }
            DamagerOwnerLevel -= 25;
        }

        @Override
        public void run()
        {
            List<LivingEntity> tempList = (List<LivingEntity>) spinningSwordDamagerOwner.getLocation().getWorld().getNearbyLivingEntities(spinningSwordDamagerOwner.getLocation(),2,2,2,this::isDamageable);
            for(Entity e: tempList)
            {
                //Bukkit.getPluginManager().callEvent(new EntityDamageByEntityEvent(spinningSwordDamagerOwner,e, EntityDamageEvent.DamageCause.ENTITY_ATTACK,50));
                LivingEntity le = (LivingEntity)e;
                //System.out.println("Damage owner level is " + DamagerOwnerLevel);
                double damage = 4 + (DamagerOwnerLevel/5);
                //getServer().getConsoleSender().sendMessage(getPluginPrefix() + "Tried dealing: " + damage);
                double armor = le.getAttribute(Attribute.GENERIC_ARMOR).getValue();
                double toughness = le.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
                //getServer().getConsoleSender().sendMessage(getPluginPrefix() + "Armor: " + armor);
                //getServer().getConsoleSender().sendMessage(getPluginPrefix() + "Ignoring: " + (armor*30/100));
                armor -= (armor*0.15);
                damage = MMOClass.damageCalculation(damage,armor,toughness);
                //getServer().getConsoleSender().sendMessage(getPluginPrefix() + "Toughness: " + toughness);
                //getServer().getConsoleSender().sendMessage(getPluginPrefix() + "Got reduced to: " + damage);
                //getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.GREEN + "************************");
                EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(spinningSwordDamagerOwner, e, EntityDamageEvent.DamageCause.CUSTOM, damage);
                Bukkit.getServer().getPluginManager().callEvent(event);
                //le.damage(damage,spinningSwordDamagerOwner);
            }
        }
    }

    @EventHandler
    public void shootFireball(PlayerInteractEvent event)
    {
        Player p = event.getPlayer();
        if(p.getInventory().getItemInMainHand().getType().equals(Material.BLAZE_ROD))
        {
            Location loc = p.getLocation();
            Entity f = loc.getWorld().spawnEntity(loc.add(loc.getDirection()), EntityType.FIREBALL);
            f.setVelocity(loc.getDirection());
        }
    }
 }
