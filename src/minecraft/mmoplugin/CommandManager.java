package minecraft.mmoplugin;

import leveledmobs.*;
import minecraft.mmoplugin.customItems.CommonItems;
import minecraft.mmoplugin.customItems.CustomItems;
import minecraft.mmoplugin.customItems.LegendaryItems;
import minecraft.mmoplugin.events.OpenWorldConfigManager;
import minecraft.mmoplugin.events.PrivateSideBar;
import net.md_5.bungee.protocol.packet.Chat;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import particles.LeashParticle;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

import static minecraft.mmoplugin.MainClass.*;

public class CommandManager implements CommandExecutor
{
    Plugin plugin;

    CommandManager(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player)
        {
            Player player = (Player) sender;
            String commandStr = command.getName().toLowerCase();
            switch (commandStr) {
                case "class":
                {
                    if(args.length == 0)
                    {
                        return true;
                    }
                    String whichClass = args[0].toLowerCase();
                    ItemStack woodSword = CustomItems.createCustomItem(Material.WOODEN_SWORD, "Tahta Kılıç", CustomItems.Enderlik.SIRADAN,true, CustomItems.CustomItemStats.commonWoodenSwordDamage,CustomItems.CustomItemStats.commonWoodenSwordSpeed);
                    ItemStack woodAxe = CustomItems.createCustomItem(Material.WOODEN_AXE, "Tahta Balta", CustomItems.Enderlik.SIRADAN,true, CustomItems.CustomItemStats.commonWoodenAxeDamage,CustomItems.CustomItemStats.commonWoodenAxeSpeed);
                    ItemStack helmet = CustomItems.createCustomItem(Material.LEATHER_HELMET, "Deri Kask", CustomItems.Enderlik.SIRADAN,true, CustomItems.CustomItemStats.commonLeatherHelmetArmor,CustomItems.CustomItemStats.commonLeatherHelmetToughness,EquipmentSlot.HEAD,null);
                    ItemStack chestplate = CustomItems.createCustomItem(Material.LEATHER_CHESTPLATE, "Deri Göğüslük", CustomItems.Enderlik.SIRADAN,true, CustomItems.CustomItemStats.commonLeatherPlateArmor,CustomItems.CustomItemStats.commonLeatherPlateToughness,EquipmentSlot.CHEST,null);
                    ItemStack leggings = CustomItems.createCustomItem(Material.LEATHER_LEGGINGS, "Deri Pantalon", CustomItems.Enderlik.SIRADAN,true, CustomItems.CustomItemStats.commonLeatherLeggingsArmor,CustomItems.CustomItemStats.commonLeatherLeggingsToughness,EquipmentSlot.LEGS,null);
                    ItemStack boots = CustomItems.createCustomItem(Material.LEATHER_BOOTS, "Deri Bot", CustomItems.Enderlik.SIRADAN,true, CustomItems.CustomItemStats.commonLeatherBootsArmor,CustomItems.CustomItemStats.commonLeatherBootsToughness,EquipmentSlot.FEET,null);
                    ItemStack food = new ItemStack(Material.COOKED_BEEF);
                    food.setAmount(64);
                    switch(whichClass)
                    {
                        case "necromancer":
                        {
                            if (!classObjectMap.containsKey(player.getUniqueId())) //playerClassMap.get(player.getUniqueId()).equals("NULL")
                            {
                                //playerClassMap.put(player.getUniqueId(), "necromancer");
                                plugin.getConfig().set("PlayerLevels." + player.getUniqueId() + ".level", 1);
                                plugin.getConfig().set("PlayerLevels." + player.getUniqueId() + ".xp", 0);
                                plugin.saveConfig();
                                MainClass.createScoreboard(player, 1, 0, "necromancer");
                                classObjectMap.put(player.getUniqueId(), new Necromancer(plugin, player, 0, 1, "necromancer"));

                                plugin.getConfig().set("PlayerCooldowns."+player.getUniqueId()+".Summon",0);
                                plugin.getConfig().set("PlayerCooldowns."+player.getUniqueId()+".SummonHeal",0);

                                HashMap<String, DateTime> necromancerSkillsCooldown = new HashMap<>();
                                necromancerSkillsCooldown.put("Summon", new DateTime());
                                necromancerSkillsCooldown.put("SummonHeal", new DateTime());
                                MainClass.playerCooldownMap.put(player.getUniqueId(), necromancerSkillsCooldown);
                                MMOClass.adjustXPBar(player);
                                PrivateSideBar.createPrivateSideBar(player);
                                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
                                player.getInventory().addItem(woodSword);
                                player.getInventory().addItem(helmet);
                                player.getInventory().addItem(chestplate);
                                player.getInventory().addItem(leggings);
                                player.getInventory().addItem(boots);
                                player.getInventory().addItem(food);
                                ItemStack magicBook = new ItemStack(Material.ENCHANTED_BOOK);
                                ItemMeta magicBookMeta = magicBook.getItemMeta();
                                List<String> lore = new ArrayList<>();
                                magicBookMeta.setDisplayName(ChatColor.GOLD + "Summon kontrol");
                                lore.add("");
                                lore.add(ChatColor.YELLOW + "Summon'ını kontrol etmek için");
                                lore.add(ChatColor.YELLOW + "sağ tıkla!");
                                magicBookMeta.setLore(lore);
                                lore.clear();
                                magicBook.setItemMeta(magicBookMeta);
                                player.getInventory().addItem(magicBook);
                            }
                            else {
                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Zaten bir sınıf seçmişsiniz, sınıfınız: " + ChatColor.GREEN + classObjectMap.get(player.getUniqueId()).getClassName());
                            }
                            break;
                        }
                        case "warrior":
                        {
                            if(!classObjectMap.containsKey(player.getUniqueId()))
                            {
                                //playerClassMap.put(player.getUniqueId(), "warrior");
                                plugin.getConfig().set("PlayerLevels." + player.getUniqueId() + ".level", 1);
                                plugin.getConfig().set("PlayerLevels." + player.getUniqueId() + ".xp", 0);
                                plugin.saveConfig();
                                MainClass.createScoreboard(player, 1, 0, "warrior");
                                Warrior tempWarrior = new Warrior(plugin, player, 0, 1, "warrior");
                                classObjectMap.put(player.getUniqueId(),tempWarrior);

                                plugin.getConfig().set("PlayerCooldowns."+player.getUniqueId()+".Whirlwind",0);
                                plugin.getConfig().set("PlayerCooldowns."+player.getUniqueId()+".Sunder",0);

                                HashMap<String, DateTime> warriorSkillsCooldown = new HashMap<>();
                                warriorSkillsCooldown.put("Whirlwind", new DateTime());
                                warriorSkillsCooldown.put("Sunder", new DateTime());
                                MainClass.playerCooldownMap.put(player.getUniqueId(), warriorSkillsCooldown);
                                MMOClass.adjustXPBar(player);
                                PrivateSideBar.createPrivateSideBar(player);
                                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
                                player.getInventory().addItem(woodAxe);
                                player.getInventory().addItem(helmet);
                                player.getInventory().addItem(chestplate);
                                player.getInventory().addItem(leggings);
                                player.getInventory().addItem(boots);
                                player.getInventory().addItem(food);
                            }
                            else {
                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Zaten bir sınıf seçmişsiniz, sınıfınız: " + ChatColor.GREEN + classObjectMap.get(player.getUniqueId()).getClassName());
                            }
                            break;
                        }
                        case "cleric":
                        {
                            if(!classObjectMap.containsKey(player.getUniqueId()))
                            {
                                //playerClassMap.put(player.getUniqueId(), "warrior");
                                plugin.getConfig().set("PlayerLevels." + player.getUniqueId() + ".level", 1);
                                plugin.getConfig().set("PlayerLevels." + player.getUniqueId() + ".xp", 0);
                                plugin.getConfig().set("PlayerCooldowns."+player.getUniqueId()+".Leash",0);
                                plugin.saveConfig();
                                MainClass.createScoreboard(player, 1, 0, "cleric");
                                Cleric tempCleric = new Cleric(plugin, player, 0, 1, "cleric");
                                classObjectMap.put(player.getUniqueId(),tempCleric);

                                HashMap<String, DateTime> clericSkillsCooldown = new HashMap<>();
                                clericSkillsCooldown.put("Leash", new DateTime());
                                MainClass.playerCooldownMap.put(player.getUniqueId(), clericSkillsCooldown);

                                MMOClass.adjustXPBar(player);
                                PrivateSideBar.createPrivateSideBar(player);

                                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
                                player.getInventory().addItem(woodSword);
                                player.getInventory().addItem(helmet);
                                player.getInventory().addItem(chestplate);
                                player.getInventory().addItem(leggings);
                                player.getInventory().addItem(boots);
                                player.getInventory().addItem(CustomItems.createCustomShield("Kalkan", CustomItems.Enderlik.SIRADAN));
                                player.getInventory().addItem(food);

                            }
                            else {
                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Zaten bir sınıf seçmişsiniz, sınıfınız: " + ChatColor.GREEN + classObjectMap.get(player.getUniqueId()).getClassName());
                            }
                            break;
                        }
                        case "reset":
                        {
                            if(!player.hasPermission("aykit.admin"))
                                return true;
                            if(classObjectMap.containsKey(player.getUniqueId()))
                            {
                                plugin.getConfig().set("PlayerLevels." + player.getUniqueId() + ".level", null);
                                plugin.getConfig().set("PlayerLevels." + player.getUniqueId() + ".xp", null);
                                plugin.getConfig().set("ClassList." + player.getUniqueId() + ".Class", null);
                                if(classObjectMap.get(player.getUniqueId()).getFaction() != null)
                                {
                                    plugin.getConfig().set("PlayerFactions." + player.getUniqueId(), null);
                                }
                                PrivateSideBar.removeBar(player, classObjectMap.get(player.getUniqueId()));
                                MainClass.playerCooldownMap.remove(player.getUniqueId());
                                classObjectMap.remove(player.getUniqueId());
                                MainClass.mainScoreboard.getTeam(Integer.toString(MainClass.playerTeamMap.get(player.getUniqueId()))).removePlayer(player);
                                MMOClass.adjustXPBar(player);
                                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
                            }
                            else
                            {
                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Class'ını resetlemek için önce class seçmiş olmalısın.");
                            }
                            break;
                        }
                    }
                    break;
                }
                case "stats":
                {
                    if(classObjectMap.containsKey(player.getUniqueId()))
                    {
                        CustomInventory customInventory = new CustomInventory(plugin, player, classObjectMap.get(player.getUniqueId()));
                    }
                    else
                    {
                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Lütfen önce bir sınıf seçin.");
                    }
                    break;
                }
                case "sword":
                {
                    //PrivateSideBar.createPrivateSideBar(player);
                    break;
                }
                case "spider":
                {
                    if (player.hasPermission("aykit.admin")) {
                        int level = Integer.parseInt(args[0]);
                        LeveledSpider leveledSpider = new LeveledSpider(player.getWorld(),level,player.getLocation());
                    }
                    break;
                }
                case "mobspawn":
                {
                    if (player.hasPermission("aykit.admin")) {
                        if (args.length == 5)
                        {
                            plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "INSIDE ON COMMAND MOBSPAWN");
                            int mobCount = Integer.parseInt(args[0]);
                            int xIncrease = Integer.parseInt(args[1]);
                            int zIncrease = Integer.parseInt(args[2]);
                            int levelLowerbound = Integer.parseInt(args[3]);
                            int levelHigherbound = Integer.parseInt(args[4]);

                            if(levelHigherbound <= levelLowerbound)
                            {
                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Higherbound should be higher than the lowerbound");
                            }

                            else
                            {
                                BukkitTask task = new SpawnMobs(mobCount,xIncrease,zIncrease,levelLowerbound,levelHigherbound,player).runTaskTimer(plugin,0,2);
                            }
                            break;
                        }
                        else
                        {
                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Kullanım: " + ChatColor.BLUE + "/mobspawn [mobCount] [xLocation] [yLocation] [mobLevel1] [mobLevel2]");
                        }
                    }
                }
                case "spawnelitegiant":
                {
                    if (player.hasPermission("aykit.admin")) {
                        int level = Integer.parseInt(args[0]);
                        EliteGiant eliteGiant = new EliteGiant(player.getWorld(),level,player.getLocation(),plugin);
                    }
                    break;
                }
                case "createnpc":
                {
                    if (player.hasPermission("aykit.admin")) {
                        if(args.length == 2)
                            NPCManager.createNPC(player,args[0],args[1],"f");
                        else if(args.length == 3)
                            NPCManager.createNPC(player,args[0],args[1],args[2]);
                        else
                            player.sendMessage(getPluginPrefix() + "Komutu yanlış girdiniz\nKullanımı: /createnpc <isim>");
                    }
                    break;
                }
                case "setlvl":
                {
                    if (player.hasPermission("aykit.admin")) {
                        if(args.length==1)
                        {
                            try {
                                int level = Integer.parseInt(args[0]);
                                MMOClass temp = classObjectMap.get(player.getUniqueId());
                                int oldLevel = temp.getLevel();
                                double oldXP = temp.getXp();
                                temp.setLevel(level);
                                temp.setXp(0);
                                classObjectMap.put(player.getUniqueId(),temp);
                                int tempid = MainClass.playerTeamMap.get(player.getUniqueId());
                                PrivateSideBar.updateLevelAndXP(player,oldLevel, oldXP);
                                MainClass.mainScoreboard.getTeam(Integer.toString(tempid)).setPrefix(ChatColor.WHITE + "LVL " + Integer.toString(level) + " " + MMOClass.getClassBasedColour(temp.getClassName()) + (temp.getClassName().equalsIgnoreCase("necromancer") ? "SUMMONER" : temp.getClassName().toUpperCase()) + " ");
                                for(int i = oldLevel+1; i <= level; i++)
                                {
                                    MMOClass.sendLevelUpMessage(player,temp.getClassName(),i);
                                }
                                Location loc = player.getLocation();
                                World w = player.getWorld();
                                BukkitTask task = new BukkitRunnable() {
                                    int leftToCancel = 4;
                                    @Override
                                    public void run() {
                                        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Spawning firework...");
                                        Location newLocation = loc.add(new Vector(Math.random()-0.5, 2, Math.random()-0.5).multiply(3));
                                        assert w != null;

                                        Firework fw = w.spawn(newLocation, Firework.class);
                                        FireworkMeta fwMeta = fw.getFireworkMeta();
                                        FireworkEffect.Builder builder = FireworkEffect.builder();


                                        fwMeta.addEffect(builder.flicker(true).withColor(Color.AQUA).build());
                                        fwMeta.addEffect(builder.trail(true).build());
                                        fwMeta.addEffect(builder.withFade(Color.YELLOW).build());
                                        fwMeta.addEffect(builder.with(FireworkEffect.Type.STAR).build());
                                        fwMeta.setPower(1);
                                        fw.setFireworkMeta(fwMeta);

                                        leftToCancel--;
                                        if(leftToCancel==0)
                                        {
                                            cancel();
                                        }
                                    }
                                }.runTaskTimer(plugin,0,10);
                                ActionBarUtil.sendTitle(player,"",ChatColor.GREEN + "SEVİYE ATLADINIZ!",1*20,1*20,1*20);
                                if(level > 20)
                                {
                                    if(level%2 == 0)
                                    {
                                        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(level);
                                    }
                                    else
                                    {
                                        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(level-1);
                                    }
                                }
                                else
                                {
                                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
                                }
                                MMOClass.adjustXPBar(player);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Komutu doğru girdiğinden emin ol.");
                            }
                        }
                        else if(args.length == 2)
                        {
                            Player online = Bukkit.getPlayer(args[0]);
                            if(online == null)
                                return true;
                            try {
                                int level = Integer.parseInt(args[1]);
                                MMOClass temp = classObjectMap.get(online.getUniqueId());
                                int oldLevel = temp.getLevel();
                                double oldXP = temp.getXp();
                                temp.setLevel(level);
                                temp.setXp(0);
                                classObjectMap.put(online.getUniqueId(),temp);
                                int tempid = MainClass.playerTeamMap.get(online.getUniqueId());
                                PrivateSideBar.updateLevelAndXP(online,oldLevel, oldXP);
                                MainClass.mainScoreboard.getTeam(Integer.toString(tempid)).setPrefix(ChatColor.WHITE + "LVL " + Integer.toString(level) + " " + MMOClass.getClassBasedColour(temp.getClassName()) + (temp.getClassName().equalsIgnoreCase("necromancer") ? "SUMMONER" : temp.getClassName().toUpperCase()) + " ");
                                for(int i = oldLevel+1; i <= level; i++)
                                {
                                    MMOClass.sendLevelUpMessage(online,temp.getClassName(),i);
                                }
                                Location loc = online.getLocation();
                                World w = online.getWorld();
                                BukkitTask task = new BukkitRunnable() {
                                    int leftToCancel = 4;
                                    @Override
                                    public void run() {
                                        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Spawning firework...");
                                        Location newLocation = loc.add(new Vector(Math.random()-0.5, 2, Math.random()-0.5).multiply(3));
                                        assert w != null;

                                        Firework fw = w.spawn(newLocation, Firework.class);
                                        FireworkMeta fwMeta = fw.getFireworkMeta();
                                        FireworkEffect.Builder builder = FireworkEffect.builder();


                                        fwMeta.addEffect(builder.flicker(true).withColor(Color.AQUA).build());
                                        fwMeta.addEffect(builder.trail(true).build());
                                        fwMeta.addEffect(builder.withFade(Color.YELLOW).build());
                                        fwMeta.addEffect(builder.with(FireworkEffect.Type.STAR).build());
                                        fwMeta.setPower(1);
                                        fw.setFireworkMeta(fwMeta);

                                        leftToCancel--;
                                        if(leftToCancel==0)
                                        {
                                            cancel();
                                        }
                                    }
                                }.runTaskTimer(plugin,0,10);
                                ActionBarUtil.sendTitle(online,"",ChatColor.GREEN + "SEVİYE ATLADINIZ!",1*20,1*20,1*20);
                                if(level > 20)
                                {
                                    if(level%2 == 0)
                                    {
                                        online.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(level);
                                    }
                                    else
                                    {
                                        online.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(level-1);
                                    }
                                }
                                else
                                {
                                    online.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
                                }
                                MMOClass.adjustXPBar(online);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                online.sendMessage(getPluginPrefix() + ChatColor.RED + "Komutu doğru girdiğinden emin ol.");
                            }
                        }
                        else
                        {
                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Argüman sayısı yanlış.");
                        }
                    }
                    break;
                }
                case "removenpc":
                {
                    if (player.hasPermission("aykit.admin")) {
                        Location loc = player.getLocation();
                        for(EntityPlayer npc : NPCManager.getNPCs())
                        {
                            if(!npc.getWorld().getWorld().getName().equals(loc.getWorld().getName()))
                            {
                                //Then ignore.
                            }
                            else if(((Player)npc.getBukkitEntity()).getLocation().distance(loc) <=2)
                            {
                                for (Player onlinePlayer : Bukkit.getOnlinePlayers())
                                {
                                    PlayerConnection connection = ((CraftPlayer)onlinePlayer).getHandle().playerConnection;
                                    connection.sendPacket(new PacketPlayOutEntityDestroy(npc.getId()));
                                    connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,npc));
                                }
                                MainClass.npcConfig.getConfig().set("data."+NPCManager.npcIdMap.get(npc),null);
                                MainClass.npcConfig.saveConfig();
                                NPCManager.getNPCs().remove(npc);
                                player.sendMessage(getPluginPrefix() + "NPC deleted!");
                                break;
                            }
                        }
                    }
                    break;
                }
                case "target":
                {
                    if (player.hasPermission("aykit.admin")) {
                        if (classObjectMap.containsKey(player.getUniqueId()) && classObjectMap.get(player.getUniqueId()).getClassName().equalsIgnoreCase("necromancer"))
                        {
                            Necromancer necromancer = (Necromancer) classObjectMap.get(player.getUniqueId());
                            if(necromancer.getSummon() != null)
                            {
                                player.sendMessage(getPluginPrefix() + "Current target of your summon is, " + necromancer.getSummon().getTarget() + ".");
                            }
                            else
                            {
                                player.sendMessage(getPluginPrefix() + "You currently do not have a summon.");
                            }
                        }
                        else
                        {
                            player.sendMessage(getPluginPrefix() + "You have to be a necromancer to execute this command.");
                        }
                    }
                    break;
                }
                case "heal":
                {
                    if (player.hasPermission("aykit.heal"))
                    {
                        player.setHealth(player.getMaxHealth());
                        player.sendMessage(getPluginPrefix() + "Canın fullendi!");
                    }
                    break;
                }
                case "faction":
                {
                    if (classObjectMap.containsKey(player.getUniqueId()))
                    {
                            MMOClass mmoClass = classObjectMap.get(player.getUniqueId());
                            if(args.length == 3)
                            {
                                if(args[0].equalsIgnoreCase("rütbe")) {
                                    if (mmoClass.getFaction() == null) {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir factiona üye değilsin.");
                                        return true;
                                    }
                                    Faction faction = factionMap.get(mmoClass.getFaction().toLowerCase());
                                    if (mmoClass.getRank().equals(Faction.Ranks.KURUCU) || mmoClass.getRank().equals(Faction.Ranks.ADMİN)) {
                                        Player targetPlayer = Bukkit.getPlayer(args[1]);
                                        if (targetPlayer == null || !targetPlayer.isOnline())
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu oyuncu online değil.");
                                            return true;
                                        }
                                        else if (classObjectMap.containsKey(targetPlayer.getUniqueId()) && classObjectMap.get(targetPlayer.getUniqueId()).getFaction() == null || !classObjectMap.get(targetPlayer.getUniqueId()).getFaction().equalsIgnoreCase(faction.getFactionName()))
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu oyuncu senle aynı factionda değil!");
                                            return true;
                                        }
                                        if (mmoClass.getRank().equals(Faction.Ranks.KURUCU))
                                        {
                                            try {
                                                Faction.Ranks rank = Faction.Ranks.valueOf(args[2].toUpperCase());
                                                if (!rank.equals(Faction.Ranks.KURUCU))
                                                {
                                                    faction.removeAsOnlineFactionMember(player);
                                                    classObjectMap.get(targetPlayer.getUniqueId()).setFaction(faction.getFactionName(), rank);
                                                    return true;
                                                }
                                                else {
                                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Liderliği transfer etmek için: " + ChatColor.BLUE + "/faction transfer ");
                                                    return true;
                                                }
                                            }
                                            catch (IllegalArgumentException e)
                                            {
                                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Böyle bir rütbe yok.");
                                                return true;
                                            }
                                        }
                                        else
                                        {
                                            try
                                            {
                                                Faction.Ranks rank = Faction.Ranks.valueOf(args[2].toUpperCase());
                                                if (!rank.equals(Faction.Ranks.KURUCU) && !rank.equals(Faction.Ranks.ADMİN))
                                                {
                                                    classObjectMap.get(targetPlayer.getUniqueId()).setFaction(faction.getFactionName(), rank);
                                                    return true;
                                                }
                                                else
                                                {
                                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Birine kendi rütbenden daha yüksek bir rütbe veremezsin.");
                                                    return true;
                                                }
                                            }
                                            catch (IllegalArgumentException e)
                                            {
                                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Böyle bir rütbe yok.");
                                                return true;
                                            }
                                        }
                                    } else {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu kullanmak için rütben yeterince yüksek değil.");
                                        return true;
                                    }
                                }
                            }
                            else if(args.length == 2)
                            {
                                if(args[0].equalsIgnoreCase("düşman"))
                                {
                                    if(mmoClass.getFaction() == null)
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir faction'a üye değilsin.");
                                        return true;
                                    }
                                    if(!(mmoClass.getRank().equals(Faction.Ranks.ADMİN) || mmoClass.getRank().equals(Faction.Ranks.KURUCU)))
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu kullanmak için rütben yeteri kadar yüksek değil.");
                                        return true;
                                    }

                                    String factionName = mmoClass.getFaction();
                                    Faction faction = factionMap.get(factionName.toLowerCase());
                                    String enemyFactionName = args[1].toLowerCase();
                                    Faction enemyFaction = factionMap.get(enemyFactionName);
                                    if(factionMap.containsKey(enemyFactionName)) //Does the enemy faction exist?
                                    {
                                        if (!faction.getWarCoolDownMap().containsKey(enemyFactionName) || (faction.getWarCoolDownMap().containsKey(enemyFactionName) && faction.getWarCoolDownMap().get(enemyFactionName).isBeforeNow())) //Do we have a cooldown in place?
                                        {
                                            if(faction.getEnemies().containsKey(enemyFactionName))
                                            {
                                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu faction ile zaten savaştasın!");
                                                return true;
                                            }
                                            else if(faction.getAllyFactions().containsKey(enemyFactionName))
                                            {
                                                faction.removeFromAlly(enemyFaction);
                                                enemyFaction.removeFromAlly(faction);

                                                faction.addAsEnemy(enemyFaction);
                                                faction.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE + enemyFactionName + ChatColor.AQUA + " loncası ile artık savaştasınız! ");

                                                enemyFaction.sendMessageToMembers(getPluginPrefix() +ChatColor.WHITE + "Dostunuz olan, " + ChatColor.BLUE + factionName + ChatColor.RED + " adlı lonca size savaş açtı!");
                                                enemyFaction.addAsEnemy(faction);

                                            }
                                            else
                                            {
                                                faction.addAsEnemy(enemyFaction);
                                                faction.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE + enemyFactionName + ChatColor.AQUA + " loncası ile artık savaştasınız! ");

                                                enemyFaction.addAsEnemy(faction);
                                                enemyFaction.sendMessageToMembers(getPluginPrefix() + ChatColor.RED + factionName + ChatColor.WHITE + " adlı lonca size savaş açtı!");
                                                return true;
                                            }
                                        }
                                        else
                                        {
                                            Minutes min = Minutes.minutesBetween(new DateTime(), faction.getWarCoolDownMap().get(enemyFactionName));
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu faction'a " + ChatColor.LIGHT_PURPLE + min.getMinutes() + " dk, " + ChatColor.RED + " daha savaş açamazsınız. ");
                                            return true;
                                        }
                                    }
                                    else
                                    {
                                        player.sendMessage(getPluginPrefix() + "Böyle bir faction yok.");
                                    }
                                }
                                else if(args[0].equalsIgnoreCase("dost"))
                                {
                                    if(mmoClass.getFaction() == null)
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir faction'a üye değilsin.");
                                        return true;
                                    }
                                    if(!(mmoClass.getRank().equals(Faction.Ranks.ADMİN) || mmoClass.getRank().equals(Faction.Ranks.KURUCU)))
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu kullanmak için rütben yeteri kadar yüksek değil.");
                                        return true;
                                    }
                                    String factionName = mmoClass.getFaction();
                                    Faction faction = factionMap.get(factionName.toLowerCase());
                                    String allyFactionName = args[1].toLowerCase();
                                    if(factionMap.containsKey(allyFactionName))
                                    {
                                        Faction allyFaction = factionMap.get(allyFactionName);
                                        if(faction.getAllyFactions().containsKey(allyFactionName))
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu lonca ile zaten dostsunuz!");
                                            return true;
                                        }
                                        if(allyFaction.getAllyRequester() != null)
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu lonca zaten bir lonca ile antlaşma kurmaya çalışmakta.");
                                            return true;
                                        }
                                        if(faction.getEnemies().containsKey(allyFactionName))
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu lonca ile düşmansınız! Önce durumunuzu normale çekin! ");
                                            return true;
                                        }
                                        else
                                        {
                                            faction.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE + allyFactionName + ChatColor.WHITE +  " adlı loncaya dostluk isteği gönderildi!");
                                            allyFaction.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE +  factionName + ChatColor.WHITE + " adlı lonca size dostluk isteği gönderdi! Bu isteğe yanıt vermek için 2 dakikanız var.");
                                            allyFaction.setAllyRequester(faction);
                                            allyFaction.setRequestType(Faction.RequestType.ALLY);
                                            int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                                                allyFaction.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE + factionName + ChatColor.WHITE + " adlı loncanın dostluk isteğine vaktinde cevap vermediniz. İstek iptal edildi.");
                                                faction.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE + allyFactionName + ChatColor.WHITE + " adlı lonca dostluk isteğinize vaktinde cevap vermedi. İsteğiniz iptal edildi. ");
                                                allyFaction.setTimerTaskId(0);
                                                allyFaction.setAllyRequester(null);
                                                allyFaction.setRequestType(null);
                                            },120*20);
                                            allyFaction.setTimerTaskId(taskid);
                                        }
                                    }
                                    else
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Böyle bir lonca yok!");
                                        return true;
                                    }
                                }
                                else if(args[0].equalsIgnoreCase("info"))
                                {
                                    String targetFaction = args[1].toLowerCase();
                                    if(factionMap.containsKey(targetFaction))
                                    {
                                        Faction temp = factionMap.get(targetFaction);
                                        player.sendMessage(ChatColor.RED + "============== "+ ChatColor.BLUE + "FACTION INFO" + ChatColor.RED +" ==============");
                                        player.sendMessage(ChatColor.DARK_GREEN + "Faction ismi: " + ChatColor.YELLOW + temp.getFactionName());
                                        player.sendMessage(ChatColor.DARK_GREEN + "Maximum power: " + ChatColor.YELLOW + temp.getPower());
                                        player.sendMessage( ChatColor.DARK_GREEN + "Claim power: " + ChatColor.YELLOW + temp.getClaimedCount()*10);
                                        player.sendMessage(ChatColor.DARK_GREEN + "Lider: " + ChatColor.YELLOW + Bukkit.getOfflinePlayer(temp.getOwner()).getName());
                                        player.sendMessage(ChatColor.DARK_GREEN + "Dost Factionlar: ");
                                        HashMap<String,Faction> allies = temp.getAllyFactions();
                                        int i = 1;
                                        for(String name : allies.keySet())
                                        {
                                            player.sendMessage(ChatColor.BLUE + Integer.toString(i) + ".) " + ChatColor.GREEN + name.toUpperCase());
                                            i++;
                                        }
                                        player.sendMessage(ChatColor.DARK_GREEN + "Düşman Factionlar: ");
                                        HashMap<String,Faction> enemies = temp.getEnemies();
                                        i = 1;
                                        for(String name : enemies.keySet())
                                        {
                                            player.sendMessage(ChatColor.BLUE + Integer.toString(i) + ".) " + ChatColor.RED + name.toUpperCase());
                                            i++;
                                        }
                                        player.sendMessage(ChatColor.DARK_GREEN + "Online oyuncular " + ChatColor.GOLD + "(ilk 20): ");
                                        List<Player> playerList = temp.getOnlineFactionMembers();
                                        for(i = 0; i < 20 && i < playerList.size(); i++)
                                        {
                                            MMOClass onlineMember = classObjectMap.get(playerList.get(i).getUniqueId());
                                            String colour = MMOClass.getClassBasedColour(onlineMember.getClassName());
                                            player.sendMessage(ChatColor.BLUE + Integer.toString((i+1)) + ".) " + ChatColor.WHITE + "LVL " + onlineMember.getLevel() + colour + "[" + onlineMember.getClassName().toUpperCase() + "] " + ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "(" + onlineMember.getRank() + ")" + ChatColor.WHITE + playerList.get(i).getName());
                                        }
                                        player.sendMessage(ChatColor.RED + "========================================");
                                        return true;
                                    }
                                    else
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Böyle bir lonca bulunmuyor.");
                                }
                                else if(args[0].equalsIgnoreCase("normal"))
                                {
                                    if(mmoClass.getFaction() == null)
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir faction'a üye değilsin.");
                                        return true;
                                    }
                                    if(!(mmoClass.getRank().equals(Faction.Ranks.ADMİN) || mmoClass.getRank().equals(Faction.Ranks.KURUCU)))
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu kullanmak için rütben yeteri kadar yüksek değil.");
                                        return true;
                                    }
                                    Faction playersFaction = factionMap.get(mmoClass.getFaction().toLowerCase());
                                    String targetFactionsName = args[1].toLowerCase();
                                    if(factionMap.containsKey(targetFactionsName))
                                    {
                                        Faction targetFaction = factionMap.get(targetFactionsName);
                                        if(playersFaction.getAllyFactions().containsKey(targetFactionsName))
                                        {
                                            playersFaction.removeFromAlly(targetFaction);
                                            targetFaction.removeFromAlly(playersFaction);

                                            targetFaction.sendMessageToMembers(getPluginPrefix() + "Dostunuz olan, " + ChatColor.BLUE + playersFaction.getFactionName() + ChatColor.WHITE + " adlı lonca dostluk antlaşmanızı sonlandırdı! ");
                                            playersFaction.sendMessageToMembers(getPluginPrefix() + "Dostunuz olan, " + ChatColor.BLUE + targetFaction.getFactionName() + ChatColor.WHITE + " adlı lonca ile artık dost değilsiniz! ");
                                        }
                                        else if(playersFaction.getEnemies().containsKey(targetFactionsName))
                                        {
                                            if(targetFaction.getAllyRequester() != null)
                                            {
                                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu lonca zaten bir lonca ile antlaşma kurmaya çalışmakta.");
                                                return true;
                                            }
                                            else
                                            {
                                                playersFaction.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE + targetFactionsName + ChatColor.WHITE + " adlı loncaya ateşkes isteği gönderildi.");
                                                targetFaction.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE +  playersFaction.getFactionName() + ChatColor.WHITE + " adlı lonca size ateşkes isteği gönderdi! Bu isteğe yanıt vermek için 2 dakikanız var.");
                                                targetFaction.setAllyRequester(playersFaction);
                                                targetFaction.setRequestType(Faction.RequestType.CEASE_FIRE);
                                                int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                                                    targetFaction.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE + playersFaction.getFactionName() + ChatColor.WHITE + " adlı loncanın dostluk isteğine vaktinde cevap vermediniz. İstek iptal edildi.");
                                                    playersFaction.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE + targetFactionsName + ChatColor.WHITE + " adlı lonca dostluk isteğinize vaktinde cevap vermedi. İsteğiniz iptal edildi. ");
                                                    targetFaction.setTimerTaskId(0);
                                                    targetFaction.setAllyRequester(null);
                                                    targetFaction.setRequestType(Faction.RequestType.CEASE_FIRE);
                                                },120*20);
                                                targetFaction.setTimerTaskId(taskid);
                                            }
                                        }
                                        else
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu lonca ile zaten ne dost ne düşmansınız!");
                                            return true;
                                        }
                                    }
                                    else
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Böyle bir lonca yok!");
                                        return true;
                                    }
                                }
                                else if(args[0].equalsIgnoreCase("kabul"))
                                {
                                    if(mmoClass.getFaction() == null)
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir faction'a üye değilsin.");
                                        return true;
                                    }
                                    if(!(mmoClass.getRank().equals(Faction.Ranks.ADMİN) || mmoClass.getRank().equals(Faction.Ranks.KURUCU))) //TODO: Maybe allow moderator to accept invitations?
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu kullanmak için rütben yeteri kadar yüksek değil.");
                                        return true;
                                    }
                                    Faction temp = factionMap.get(mmoClass.getFaction().toLowerCase());
                                    String appliersName = args[1];
                                    Player applier = Bukkit.getPlayer(appliersName);
                                    if(applier != null)
                                    {
                                        if (temp.getApplications().containsKey(applier.getUniqueId()))
                                        {
                                            PreparedStatement ps = null;
                                            try {
                                                ps = MainClass.conn.prepareStatement("update all_factions set members = members + 1 where Name=?");
                                                ps.setString(1,temp.getFactionName());
                                                ps.executeUpdate();

                                                ps.close();
                                                ps = null;
                                                if(applier.isOnline())
                                                {
                                                    applier.sendMessage(getPluginPrefix() + ChatColor.BLUE + temp.getFactionName() + ChatColor.YELLOW + " adlı loncaya yaptığınız başvuru kabul oldu! ");
                                                    classObjectMap.get(applier.getUniqueId()).setFaction(temp.getFactionName(), Faction.Ranks.ÜYE);
                                                }
                                                else
                                                {
                                                    Faction.config.getConfig().set("PlayerFactions."+applier.getUniqueId()+".Name",temp.getFactionName()); //TODO: This is the only exception, where we touch the config file other than onEnable & onDisable.
                                                    Faction.config.getConfig().set("PlayerFactions."+applier.getUniqueId()+".Rank",Faction.Ranks.ÜYE.toString());
                                                    Faction.config.saveConfig();
                                                }
                                                temp.removeApplication(applier);
                                                temp.sendMessageToMembers(ChatColor.BLUE + "[" + ChatColor.GREEN + temp.getFactionName().toUpperCase() + ChatColor.BLUE + "] " + ChatColor.LIGHT_PURPLE + appliersName + ChatColor.WHITE + " adlı oyuncu loncaya katıldı! ");
                                            }
                                            catch (SQLException e)
                                            {
                                                plugin.getServer().getLogger().log(Level.SEVERE,"SQL Bağlantısında bir sorun çıktı! ", e);
                                            }
                                            return true;
                                        }
                                        else
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu adla bir oyuncu loncanıza başvurmamış.");
                                            return true;
                                        }
                                    }
                                }
                                else if(args[0].equalsIgnoreCase("red"))
                                {
                                    if(mmoClass.getFaction() == null)
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir faction'a üye değilsin.");
                                        return true;
                                    }
                                    if(!(mmoClass.getRank().equals(Faction.Ranks.ADMİN) || mmoClass.getRank().equals(Faction.Ranks.KURUCU))) //TODO: Maybe allow moderator to accept invitations?
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu kullanmak için rütben yeteri kadar yüksek değil.");
                                        return true;
                                    }
                                    Faction temp = factionMap.get(mmoClass.getFaction().toLowerCase());
                                    String appliersName = args[1];
                                    Player applier = Bukkit.getPlayer(appliersName);
                                    if(applier != null)
                                    {
                                        if(temp.getApplications().containsKey(applier.getUniqueId()))
                                        {
                                            temp.removeApplication(applier);
                                            if(applier.isOnline())
                                                applier.sendMessage(getPluginPrefix() + ChatColor.BLUE + temp.getFactionName() + ChatColor.WHITE + " adlı loncaya yaptığınız başvuru red edildi! ");
                                            temp.sendMessageToMembers(Faction.getFactionPrefix(temp.getFactionName()) + ChatColor.BLUE + applier.getName() + " adlı oyuncunun loncaya yaptığı başvuru rededildi! ");
                                        }
                                    }
                                }
                                else if(args[0].equalsIgnoreCase("tp"))
                                {
                                    if(mmoClass.getFaction() == null)
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir faction'a üye değilsin.");
                                        return true;
                                    }
                                    if(args[1].equalsIgnoreCase("kabul"))
                                    {
                                        if(mmoClass.getTpRequester() != null)
                                        {
                                            if(player.getWorld().getName().contains("faction_"))
                                            {
                                                mmoClass.getTpRequester().teleport(player.getLocation());
                                                player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Işınlanma tamamlandı!");
                                                mmoClass.getTpRequester().sendMessage(getPluginPrefix() + ChatColor.GREEN + "Işınlanma tamamlandı!");
                                                mmoClass.getTpRequest().cancel();
                                                mmoClass.setTpRequest(null);
                                            }
                                            else
                                            {
                                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "TP'yi kabul edebilmek için faction dünyasında olmanız gerekiyor.");
                                            }
                                        }
                                        return true;
                                    }
                                    else if(args[1].equalsIgnoreCase("red"))
                                    {
                                        if(mmoClass.getTpRequester() != null)
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Işınlanma red edildi!");
                                            mmoClass.getTpRequester().sendMessage(getPluginPrefix() + ChatColor.RED + "Işınlanma red edildi!");
                                            mmoClass.getTpRequest().cancel();
                                            mmoClass.setTpRequest(null);
                                            return true;
                                        }
                                        return true;
                                    }
                                    OfflinePlayer offlineTarget = Bukkit.getPlayer(args[1]);
                                    try
                                    {
                                        if(!offlineTarget.isOnline())
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu oyuncu online değil.");
                                            return true;
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu oyuncu online değil.");
                                        return true;
                                    }
                                    Player p = offlineTarget.getPlayer();
                                    if(classObjectMap.containsKey(p.getUniqueId()) && classObjectMap.get(p.getUniqueId()).getFaction().equalsIgnoreCase(mmoClass.getFaction()))
                                    {
                                        if (classObjectMap.get(p.getUniqueId()).getTpRequester() == null)
                                        {
                                            p.sendMessage(getPluginPrefix() + ChatColor.BLUE + player.getName() + ChatColor.WHITE + " adlı oyuncu size tp isteği gönderdi.");
                                            player.sendMessage(getPluginPrefix() + ChatColor.BLUE + p.getName() + ChatColor.WHITE + " adlı oyuncuya tp isteği gönderildi. ");
                                            BukkitTask task = new BukkitRunnable() {
                                                @Override
                                                public void run() {
                                                    classObjectMap.get(p.getUniqueId()).setTpRequester(null);
                                                    classObjectMap.get(p.getUniqueId()).setTpRequest(null);
                                                }
                                            }.runTaskLater(plugin,60*20);
                                            classObjectMap.get(p.getUniqueId()).setTpRequester(player);
                                            classObjectMap.get(p.getUniqueId()).setTpRequest(task);
                                        }
                                    }
                                    else
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu oyuncu sizle aynı faction'da değil.");
                                    }
                                }
                                else if (player.getWorld().getName().equalsIgnoreCase("world"))
                                {
                                    if(args[0].equalsIgnoreCase("create"))
                                    {
                                        if(mmoClass.getFaction() != null)
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Zaten bir factiona mensupsun.");
                                            return true;
                                        }
                                        else if(factionMap.containsKey(args[1].toLowerCase()))
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu isimle bir faction zaten var!");
                                            return true;
                                        }
                                        else
                                        {
                                            if(args[1].equalsIgnoreCase("aviana"))
                                            {
                                                player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu isimle bir lonca yaratmak yasaktır.");
                                                return true;
                                            }
                                            Faction temp = new Faction(args[1],player.getUniqueId(), player.getName());
                                            factionMap.put(args[1].toLowerCase(),temp);
                                            player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Faction yaratıldı!");
                                            mmoClass.setFaction(args[1],Faction.Ranks.KURUCU);
                                            return true;
                                        }
                                    }
                                    else if(args[0].equalsIgnoreCase("join"))
                                    {
                                        if(mmoClass.getFaction() != null)
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Zaten bir factiona mensupsun.");
                                            return true;
                                        }
                                        else if(!factionMap.containsKey(args[1].toLowerCase()))
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu isimle bir faction bulunmuyor.");
                                            return true;
                                        }
                                        else
                                        {
                                            Faction temp = factionMap.get(args[1].toLowerCase());
                                            Faction.JoinType joinType = temp.getJoinType();
                                            if(joinType.equals(Faction.JoinType.OPEN))
                                            {
                                                try
                                                {
                                                    PreparedStatement ps = MainClass.conn.prepareStatement("update all_factions set members = members + 1 where Name=?");
                                                    ps.setString(1,temp.getFactionName());
                                                    ps.executeUpdate();

                                                    ps.close();
                                                    ps = null;

                                                    MainClass.factionMap.get(temp.getFactionName().toLowerCase()).increasePower(10);
                                                    mmoClass.setFaction(factionMap.get(args[1].toLowerCase()).getFactionName(), Faction.Ranks.ÜYE);
                                                    MainClass.factionMap.get(temp.getFactionName().toLowerCase()).incrementSize();
                                                    player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Faction'a katıldın!");
                                                }
                                                catch (SQLException e)
                                                {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else if(joinType.equals(Faction.JoinType.INVITE_ONLY))
                                            {
                                                if(mmoClass.getApplications().containsKey(temp))
                                                {
                                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu loncaya zaten başvurmuş bulunmaktasın! Başvurunun, " + ChatColor.BLUE + Minutes.minutesBetween(new DateTime(),temp.getApplications().get(temp)).getMinutes() + " dk. " + ChatColor.RED + " süresi kalmış. ");
                                                    return true;
                                                }
                                                temp.addApplication(player);
                                                temp.sendMessageToMembers(getPluginPrefix() + ChatColor.YELLOW + player.getName() + ChatColor.AQUA + " adlı oyuncu, loncanıza başvurdu! ");
                                                //TODO send a request to the leader if online.
                                            }
                                            return true;
                                        }
                                    }
                                    else if(args[0].equalsIgnoreCase("transfer"))
                                    {
                                        if(mmoClass.getFaction() == null)
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir factiona üye değilsin.");
                                            return true;
                                        }
                                        Faction faction = factionMap.get(mmoClass.getFaction().toLowerCase());
                                        if(faction.getOwner().equals(player.getUniqueId()))
                                        {
                                            Player newOwner = Bukkit.getPlayer(args[1]);
                                            if(newOwner == null || !newOwner.isOnline())
                                            {
                                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu oyuncu online değil.");
                                            }
                                            else if(classObjectMap.containsKey(newOwner.getUniqueId()) && classObjectMap.get(newOwner.getUniqueId()).getFaction() == null || !classObjectMap.get(newOwner.getUniqueId()).getFaction().equalsIgnoreCase(faction.getFactionName()))
                                            {
                                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu oyuncu senle aynı factionda değil!");
                                                return true;
                                            }
                                            else
                                            {
                                                faction.changeOwner(newOwner.getUniqueId(),plugin,newOwner.getName());
                                                return true;
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu bu dünyada kullanamazsın.");
                                }
                            }
                            else if(args.length == 1)
                            {
                                String subCommand = args[0].toLowerCase();
                                switch (subCommand) {
                                    case "leave": {
                                        if (player.getWorld().getName().equalsIgnoreCase("world")) {
                                            if (mmoClass.getFaction() == null) {
                                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir factiona üye değilsin.");
                                                return true;
                                            } else {
                                                Faction faction = factionMap.get(mmoClass.getFaction().toLowerCase());
                                                if (player.getUniqueId().equals(faction.getOwner())) {
                                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Faction lideri faction'ı terk edemez, lütfen liderliği başkasına teslim edin veya factionı silin.");
                                                } else {
                                                    try {
                                                        PreparedStatement ps = MainClass.conn.prepareStatement("update all_factions set members = members - 1 where Name=?");
                                                        ps.setString(1, faction.getFactionName());
                                                        ps.executeUpdate();

                                                        ps.close();
                                                        ps = null;

                                                        MainClass.factionMap.get(faction.getFactionName().toLowerCase()).decreasePower(10);
                                                        player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Faction'dan ayrıldın.");
                                                        faction.removeAsOnlineFactionMember(player);
                                                        faction.decrementSize();
                                                        mmoClass.setFaction(null, null);
                                                    } catch (SQLException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        } else {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu bu dünyada kullanamazsın.");
                                        }
                                        return true;
                                    }
                                    case "delete": {
                                        if (player.getWorld().getName().equalsIgnoreCase("world")) {
                                            if (mmoClass.getFaction() == null) {
                                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir factiona üye değilsin.");
                                                return true;
                                            } else {
                                                Faction faction = factionMap.get(mmoClass.getFaction().toLowerCase());
                                                if (player.getUniqueId().equals(faction.getOwner())) {
                                                    for (Player onlineMember : faction.getOnlineFactionMembers()) {
                                                        MMOClass tempClass = classObjectMap.get(onlineMember.getUniqueId());
                                                        if (tempClass.getCurrentDungeon().equals("")) {
                                                            //Nothing.
                                                        } else {
                                                            String currentDungeon = tempClass.getCurrentDungeon();
                                                            DungeonManager.clearDungeon(currentDungeon);
                                                        }
                                                    }
                                                    faction.deleteFaction(player);
                                                    return true;
                                                } else {
                                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Faction'ı sadece faction lideri silebilir.");
                                                    return true;
                                                }
                                            }
                                        } else {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu bu dünyada kullanamazsın.");
                                        }
                                        return true;
                                    }
                                    case "chat": {
                                        if (mmoClass.getFaction() == null) {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir factiona üye değilsin.");
                                            return true;
                                        } else if (mmoClass.getChatType().equals(Faction.ChatType.NORMAL)) {
                                            player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Chat modun loncaya çevirildi.");
                                            mmoClass.setChatType(Faction.ChatType.FACTION);
                                            return true;
                                        } else if (mmoClass.getChatType().equals(Faction.ChatType.FACTION)) {
                                            player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Chat modun ittafak moduna çevirildi.");
                                            mmoClass.setChatType(Faction.ChatType.ALLIANCE);
                                            return true;
                                        } else if (mmoClass.getChatType().equals(Faction.ChatType.ALLIANCE)) {
                                            player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Chat modun normale çevirildi.");
                                            mmoClass.setChatType(Faction.ChatType.NORMAL);
                                            return true;
                                        }
                                        break;
                                    }
                                    case "claim": {
                                        if (classObjectMap.containsKey(player.getUniqueId()) && classObjectMap.get(player.getUniqueId()).getFaction()!=null && player.getWorld().getName().contains("faction_")) {
                                            Location loc = player.getLocation();
                                            if(loc.getX() < 192 && loc.getX() > -289 && loc.getZ() < 8 && loc.getZ() > -393)
                                            {
                                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Spawna çok yakınsın, burayı claimleyemezsin.");
                                                return true;
                                            }
                                            Chunk chunk = loc.getChunk();
                                            if (Faction.claimConfig.getConfig().contains(player.getLocation().getWorld().getName() + "." + String.valueOf(chunk)) && Faction.claimConfig.getConfig().getString(player.getLocation().getWorld().getName() + "." + String.valueOf(chunk)) != null) {
                                                String ownerfaction = Faction.claimConfig.getConfig().getString(player.getLocation().getWorld().getName() + "." + String.valueOf(chunk) + ".faction");
                                                if(ownerfaction == null)
                                                {
                                                    String factionName = classObjectMap.get(player.getUniqueId()).getFaction();
                                                    if (factionMap.get(factionName.toLowerCase()).getClaimedCount() * 10 + 10 <= factionMap.get(factionName.toLowerCase()).getPower()) {
                                                        Faction.claimConfig.getConfig().set(player.getLocation().getWorld().getName() + "." + String.valueOf(chunk) + ".faction", factionName);
                                                        Faction.claimConfig.saveConfig();
                                                        //player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Claim başarılı!");
                                                        //ActionBarUtil.sendActionBar(player,getPluginPrefix() + ChatColor.GREEN + ChatColor.BOLD + "Claim başarılı!",5*20,5*20,5*20);
                                                        player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Claim başarılı!");
                                                        factionMap.get(factionName.toLowerCase()).incrementClaim();
                                                        plugin.getConfig().set("FactionDetails." + factionName + ".claims", factionMap.get(factionName.toLowerCase()).getClaimedCount());
                                                        plugin.saveConfig();
                                                    } else {
                                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Burayı claimlemek için yeteri kadar powerınız yok!");
                                                        return true;
                                                    }
                                                    return true;
                                                }
                                                player.sendMessage(getPluginPrefix() + ChatColor.RED + ChatColor.BOLD + "Burası, " + ChatColor.BLUE + ChatColor.BOLD + ownerfaction + ChatColor.RED + ChatColor.BOLD + " adlı faction'a ait.");
                                                Faction chunkOwnerfaction = factionMap.get(ownerfaction.toLowerCase());
                                                if (chunkOwnerfaction.getPower() + 10 > chunkOwnerfaction.getClaimedCount() * 10) {
                                                    //DO NOTHING!
                                                    return true;
                                                }
                                                String playersFactionName = classObjectMap.get(player.getUniqueId()).getFaction();
                                                Faction playersFaction = factionMap.get(playersFactionName.toLowerCase());
                                                if (chunkOwnerfaction.getAllyFactions().containsKey(playersFactionName.toLowerCase())) {
                                                    //DO NOT ALLOW THEM TO CLAIM!
                                                    return true;
                                                } else {
                                                    if (playersFaction.getClaimedCount() * 10 + 10 <= playersFaction.getPower()) {
                                                        if(!classObjectMap.get(player.getUniqueId()).getRank().equals(Faction.Ranks.ADMİN) && !classObjectMap.get(player.getUniqueId()).getRank().equals(Faction.Ranks.KURUCU))
                                                        {
                                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Claim atabilmek için rütbeniz yeterli değil");
                                                            return true;
                                                        }
                                                        World w = player.getWorld();
                                                        int x = chunk.getX();
                                                        int z = chunk.getZ();
                                                        Chunk x_minus_1 = w.getChunkAt(x - 1, z);
                                                        Chunk x_plus_1 = w.getChunkAt(x + 1, z);

                                                        Chunk z_minus_1 = w.getChunkAt(x, z - 1);
                                                        Chunk z_plus_1 = w.getChunkAt(x, z + 1);

                                                        int checker = 0;
                                                        if (Faction.claimConfig.getConfig().contains(player.getLocation().getWorld().getName() + "." + String.valueOf(x_minus_1))) {
                                                            if (Faction.claimConfig.getConfig().getString(player.getLocation().getWorld().getName() + "." + String.valueOf(x_minus_1) + ".faction").equalsIgnoreCase(ownerfaction))
                                                                checker++;
                                                        }
                                                        if (Faction.claimConfig.getConfig().contains(player.getLocation().getWorld().getName() + "." + String.valueOf(x_plus_1))) {
                                                            if (Faction.claimConfig.getConfig().getString(player.getLocation().getWorld().getName() + "." + String.valueOf(x_plus_1) + ".faction").equalsIgnoreCase(ownerfaction))
                                                                checker++;
                                                        }
                                                        if (Faction.claimConfig.getConfig().contains(player.getLocation().getWorld().getName() + "." + String.valueOf(z_minus_1))) {
                                                            if (Faction.claimConfig.getConfig().getString(player.getLocation().getWorld().getName() + "." + String.valueOf(z_minus_1) + ".faction").equalsIgnoreCase(ownerfaction))
                                                                checker++;
                                                        }
                                                        if (Faction.claimConfig.getConfig().contains(player.getLocation().getWorld().getName() + "." + String.valueOf(z_plus_1))) {
                                                            if (Faction.claimConfig.getConfig().getString(player.getLocation().getWorld().getName() + "." + String.valueOf(z_plus_1) + ".faction").equalsIgnoreCase(ownerfaction))
                                                                checker++;
                                                        }
                                                        if (checker == 4) {
                                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Etrafı claimli olan bir noktayı claimleyemezsin!");
                                                            return true;
                                                        } else {
                                                            Faction.claimConfig.getConfig().set(w.getName() + "." + String.valueOf(chunk) + ".faction", playersFactionName);
                                                            Faction.claimConfig.saveConfig();
                                                            player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Claim başarılı!");
                                                            chunkOwnerfaction.sendMessageToMembers(getPluginPrefix() + ChatColor.RED + playersFactionName + ChatColor.WHITE + " adlı lonca toprağınızı ele geçirdi!");
                                                            playersFaction.incrementClaim();
                                                            chunkOwnerfaction.decrementClaim();
                                                            plugin.getConfig().set("FactionDetails." + playersFactionName + ".claims", playersFaction.getClaimedCount());
                                                            plugin.saveConfig();
                                                        }
                                                    } else {
                                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Burayı claimlemek için yeteri kadar powerınız yok!");
                                                        return true;
                                                    }
                                                }
                                            } else {
                                                String factionName = classObjectMap.get(player.getUniqueId()).getFaction();
                                                if (factionMap.get(factionName.toLowerCase()).getClaimedCount() * 10 + 10 <= factionMap.get(factionName.toLowerCase()).getPower()) {
                                                    Faction.claimConfig.getConfig().set(player.getLocation().getWorld().getName() + "." + String.valueOf(chunk) + ".faction", factionName);
                                                    Faction.claimConfig.saveConfig();
                                                    //player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Claim başarılı!");
                                                    //ActionBarUtil.sendActionBar(player,getPluginPrefix() + ChatColor.GREEN + ChatColor.BOLD + "Claim başarılı!",5*20,5*20,5*20);
                                                    player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Claim başarılı!");
                                                    factionMap.get(factionName.toLowerCase()).incrementClaim();
                                                    plugin.getConfig().set("FactionDetails." + factionName + ".claims", factionMap.get(factionName.toLowerCase()).getClaimedCount());
                                                    plugin.saveConfig();
                                                } else {
                                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Burayı claimlemek için yeteri kadar powerınız yok!");
                                                    return true;
                                                }
                                            }
                                        } else {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu kullanabilmek için bir factiona üye olman gerekiyor.");
                                        }
                                        break;
                                    }
                                    case "sethome":
                                    {
                                        if (mmoClass.getFaction() == null) {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir factiona üye değilsin.");
                                            return true;
                                        }
                                        if(!mmoClass.getRank().equals(Faction.Ranks.ADMİN) && !mmoClass.getRank().equals(Faction.Ranks.KURUCU))
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komut için rütben yeterince yüksek değil.");
                                            return true;
                                        }
                                        if (Faction.claimConfig.getConfig().contains(player.getLocation().getWorld().getName() + "." + String.valueOf(player.getChunk()))) {
                                            String ownerfaction = Faction.claimConfig.getConfig().getString(player.getLocation().getWorld().getName() + "." + String.valueOf(player.getChunk()) + ".faction");
                                            if(ownerfaction.equalsIgnoreCase(mmoClass.getFaction()))
                                            {
                                                factionMap.get(mmoClass.getFaction().toLowerCase()).setHome(player.getLocation());
                                                factionMap.get(mmoClass.getFaction().toLowerCase()).sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE + player.getName() + ChatColor.WHITE + " isimli oyuncu faction evini ayarladı.");
                                            }
                                        }
                                        return true;
                                    }
                                    case "home":
                                    {
                                        if (mmoClass.getFaction() == null) {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir factiona üye değilsin.");
                                            return true;
                                        }
                                        if(player.getWorld().getName().contains("dungeon_"))
                                        {
                                            player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu dünyada bu komutu kullanamazsın.");
                                            return true;
                                        }
                                        if(factionMap.get(mmoClass.getFaction().toLowerCase()).getHome() != null)
                                        {
                                            player.sendMessage(MainClass.getPluginPrefix() + ChatColor.WHITE + "5 saniye sonra faction evinize ışınlanıcaksınız. Lütfen hareket etmeyin.");
                                            BukkitTask tpToLobby = new BukkitRunnable() {
                                                Location startLoc = player.getLocation();
                                                int count = 0;
                                                @Override
                                                public void run() {
                                                    count++;
                                                    if(startLoc.getX() != player.getLocation().getX() || startLoc.getY() != player.getLocation().getY()  || startLoc.getZ() != player.getLocation().getZ())
                                                    {
                                                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Hareket ettiğiniz için tp işlemi iptal oldu.");
                                                        cancel();
                                                    }
                                                    if(count == 21)
                                                    {
                                                        player.teleport(factionMap.get(mmoClass.getFaction().toLowerCase()).getHome());
                                                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Faction evinize ışınlanıyorsunuz.");
                                                        cancel();
                                                    }
                                                }
                                            }.runTaskTimer(plugin,0,5);
                                        }
                                        return true;
                                    }
                                    case "info":
                                    {
                                        if (mmoClass.getFaction() == null) {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir factiona üye değilsin.");
                                            return true;
                                        }
                                        Faction temp = factionMap.get(mmoClass.getFaction().toLowerCase());
                                        player.sendMessage(ChatColor.RED + "============== " + ChatColor.BLUE + "FACTION INFO" + ChatColor.RED + " ==============");
                                        player.sendMessage(ChatColor.DARK_GREEN + "Faction ismi: " + ChatColor.YELLOW + temp.getFactionName());
                                        player.sendMessage(ChatColor.DARK_GREEN + "Maximum power: " + ChatColor.YELLOW + temp.getPower());
                                        player.sendMessage(ChatColor.DARK_GREEN + "Claim power: " + ChatColor.YELLOW + temp.getClaimedCount() * 10);
                                        player.sendMessage(ChatColor.DARK_GREEN + "Lider: " + ChatColor.YELLOW + Bukkit.getOfflinePlayer(temp.getOwner()).getName());
                                        player.sendMessage(ChatColor.DARK_GREEN + "Başvurular: ");
                                        int i = 1;
                                        HashMap<UUID, DateTime> applicationSet = temp.getApplications();
                                        for(UUID uuid : applicationSet.keySet())
                                        {
                                            Player applier = Bukkit.getPlayer(uuid);
                                            Minutes min = Minutes.minutesBetween(new DateTime(), applicationSet.get(uuid));
                                            player.sendMessage(ChatColor.BLUE + Integer.toString(i) + ".) " + ChatColor.YELLOW + applier.getName() + ":" + ChatColor.BLUE + " Kalan süre, " + ChatColor.YELLOW + min.getMinutes() + " dk.");
                                        }
                                        player.sendMessage(ChatColor.DARK_GREEN + "Dost Factionlar: ");
                                        HashMap<String, Faction> allies = temp.getAllyFactions();
                                        i = 1;
                                        for (String name : allies.keySet()) {
                                            player.sendMessage(ChatColor.BLUE + Integer.toString(i) + ".) " + ChatColor.GREEN + name.toUpperCase());
                                            i++;
                                        }
                                        player.sendMessage(ChatColor.DARK_GREEN + "Düşman Factionlar: ");
                                        HashMap<String, Faction> enemies = temp.getEnemies();
                                        i = 1;
                                        for (String name : enemies.keySet()) {
                                            player.sendMessage(ChatColor.BLUE + Integer.toString(i) + ".) " + ChatColor.RED + name.toUpperCase());
                                            i++;
                                        }
                                        player.sendMessage(ChatColor.DARK_GREEN + "Online oyuncular " + ChatColor.GOLD + "(ilk 20): ");
                                        List<Player> playerList = temp.getOnlineFactionMembers();
                                        for (i = 0; i < 20 && i < playerList.size(); i++) {
                                            MMOClass onlineMember = classObjectMap.get(playerList.get(i).getUniqueId());
                                            String colour = MMOClass.getClassBasedColour(onlineMember.getClassName());
                                            player.sendMessage(ChatColor.BLUE + Integer.toString((i + 1)) + ".) " + ChatColor.WHITE + "LVL " + onlineMember.getLevel() + colour + "[" + onlineMember.getClassName().toUpperCase() + "] " + ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "(" + onlineMember.getRank() + ")" + ChatColor.WHITE + playerList.get(i).getName());
                                        }
                                        player.sendMessage(ChatColor.RED + "========================================");
                                        return true;
                                    }
                                    case "kabul": {
                                        if (mmoClass.getFaction() == null) {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir faction'a üye değilsin.");
                                            return true;
                                        }
                                        if (!(mmoClass.getRank().equals(Faction.Ranks.ADMİN) || mmoClass.getRank().equals(Faction.Ranks.KURUCU))) {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu kullanmak için rütben yeteri kadar yüksek değil.");
                                            return true;
                                        }
                                        String factionName = mmoClass.getFaction().toLowerCase();
                                        Faction currentFaction = factionMap.get(factionName);
                                        if (currentFaction.getAllyRequester() != null) {
                                            Faction allyRequester = currentFaction.getAllyRequester();
                                            if (currentFaction.getRequestType().equals(Faction.RequestType.ALLY)) {
                                                Bukkit.getScheduler().cancelTask(currentFaction.getTimerTaskId());
                                                currentFaction.setTimerTaskId(0);
                                                currentFaction.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE + allyRequester.getFactionName() + ChatColor.WHITE + " adlı lonca ile dostluk kuruldu! ");
                                                allyRequester.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE + currentFaction.getFactionName() + ChatColor.WHITE + " adlı lonca dostluk isteğinizi kabul etti! ");

                                                currentFaction.addAsAlly(allyRequester);
                                                allyRequester.addAsAlly(currentFaction);
                                            } else if (currentFaction.getRequestType().equals(Faction.RequestType.CEASE_FIRE)) {
                                                Bukkit.getScheduler().cancelTask(currentFaction.getTimerTaskId());
                                                currentFaction.setTimerTaskId(0);
                                                currentFaction.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE + allyRequester.getFactionName() + ChatColor.WHITE + " adlı lonca ile ateşkes imzalandı! ");
                                                allyRequester.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE + currentFaction.getFactionName() + ChatColor.WHITE + " adlı lonca ateşkes isteğinizi kabul etti! ");

                                                currentFaction.removeFromEnemy(allyRequester);
                                                allyRequester.removeFromEnemy(currentFaction);

                                                currentFaction.addCooldown(allyRequester);
                                                allyRequester.addCooldown(currentFaction);
                                            }
                                            currentFaction.setAllyRequester(null);
                                        } else {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Kabul edilecek bir isteğiniz bulunmamakta.");
                                        }
                                        break;
                                    }
                                    case "red": {
                                        if (mmoClass.getFaction() == null) {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir faction'a üye değilsin.");
                                            return true;
                                        }
                                        if (!(mmoClass.getRank().equals(Faction.Ranks.ADMİN) || mmoClass.getRank().equals(Faction.Ranks.KURUCU))) {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu kullanmak için rütben yeteri kadar yüksek değil.");
                                            return true;
                                        }
                                        String factionName = mmoClass.getFaction().toLowerCase();
                                        Faction currentFaction = factionMap.get(factionName);
                                        if (currentFaction.getAllyRequester() != null) {
                                            Faction allyRequester = currentFaction.getAllyRequester();
                                            Bukkit.getScheduler().cancelTask(currentFaction.getTimerTaskId());
                                            currentFaction.setTimerTaskId(0);
                                            currentFaction.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE + allyRequester.getFactionName() + ChatColor.WHITE + " adlı loncanın isteği red edildi. ");
                                            allyRequester.sendMessageToMembers(getPluginPrefix() + ChatColor.BLUE + currentFaction.getFactionName() + ChatColor.WHITE + " adlı lonca isteğinizi red etti. ");
                                            currentFaction.setAllyRequester(null);
                                        }
                                        break;
                                    }
                                    case "mod":
                                    {
                                        if (mmoClass.getFaction() == null) {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bir faction'a üye değilsin.");
                                            return true;
                                        }
                                        if (!(mmoClass.getRank().equals(Faction.Ranks.ADMİN) || mmoClass.getRank().equals(Faction.Ranks.KURUCU))) {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu kullanmak için rütben yeteri kadar yüksek değil.");
                                            return true;
                                        }
                                        Faction faction = factionMap.get(mmoClass.getFaction().toLowerCase());
                                        Faction.JoinType type = faction.getJoinType();
                                        switch (type)
                                        {
                                            case OPEN:
                                                faction.sendMessageToMembers(getPluginPrefix() + ChatColor.GREEN + "Loncanızın katılma modu kapalıya çevrildi.");
                                                faction.setJoinType(Faction.JoinType.INVITE_ONLY);
                                                break;
                                            case INVITE_ONLY:
                                                faction.sendMessageToMembers(getPluginPrefix() + ChatColor.GREEN + "Loncanızın katılma modu açığa çevrildi.");
                                                faction.setJoinType(Faction.JoinType.OPEN);
                                                break;
                                        }
                                        return true;
                                    }
                                }
                            }
                            else
                            {
                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Komutu doğru girdiğinizden emin olun.");
                            }
                        }
                    else
                    {
                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu uygulayabilmek için bir class seçmiş olman gerekir.");
                    }
                    break;
                }
                case "join":
                {
                    if(args.length == 1)
                    {
                        Player targetPlayer = Bukkit.getPlayer(args[0]);
                        if(targetPlayer != null && targetPlayer.isOnline() && classObjectMap.containsKey(targetPlayer.getUniqueId()) && !classObjectMap.get(targetPlayer.getUniqueId()).getCurrentDungeon().equals(""))
                        {
                            if(classObjectMap.containsKey(player.getUniqueId()))
                            {
                                MMOClass temp = classObjectMap.get(player.getUniqueId());
                                Faction faction = factionMap.get(temp.getFaction().toLowerCase());
                                String dungeonName = classObjectMap.get(targetPlayer.getUniqueId()).getCurrentDungeon();
                                if (temp.getCurrentDungeon().equals(""))
                                {
                                    if(!DungeonManager.dungeonMap.get(dungeonName).isInProgress())
                                    {
                                        int startingIndex = dungeonName.indexOf("_");
                                        int endingIndex = dungeonName.lastIndexOf("_");
                                        String splittedName = dungeonName.substring(startingIndex+1,endingIndex).toLowerCase();
                                        DungeonManager.joinDungeon(faction,player,dungeonName,splittedName);
                                    }

                                    else
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu dungeon şuanda devam etmekte.");
                                }
                                else
                                {
                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "2 defa dungeona katılamazsın.");
                                }
                            }
                        }
                    }
                    else
                    {
                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Komutu doğru girdiğinizden emin olun.");
                    }
                }
                case "dungeon":
                {
                    if (player.hasPermission("aykit.admin")) {
                        if(args.length == 3)
                        {
                            if (args[0].equalsIgnoreCase("spawn"))
                            {
                                int mobLevel = Integer.parseInt(args[2]);
                                Location loc = player.getLocation();
                                switch (args[1].toLowerCase())
                                {
                                    case "husk":
                                        LeveledHusk husk = new LeveledHusk(player.getWorld(),mobLevel, loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + husk.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + husk.getUniqueIDString() + ".type","husk");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + husk.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "stray":
                                        LeveledStray stray = new LeveledStray(player.getWorld(),mobLevel, loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + stray.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + stray.getUniqueIDString() + ".type","stray");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + stray.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "blaze":
                                        LeveledBlaze blaze = new LeveledBlaze(player.getWorld(),mobLevel, loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + blaze.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + blaze.getUniqueIDString() + ".type","blaze");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + blaze.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "witherskeleton":
                                        LeveledWitherSkeleton witherSkeleton = new LeveledWitherSkeleton(player.getWorld(),mobLevel, loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + witherSkeleton.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + witherSkeleton.getUniqueIDString() + ".type","witherskeleton");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + witherSkeleton.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "magmacube":
                                        LeveledMagmaCube magmaCube = new LeveledMagmaCube(player.getWorld(),mobLevel,loc,plugin);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + magmaCube.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + magmaCube.getUniqueIDString() + ".type","magmacube");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + magmaCube.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "zombie":
                                        LeveledZombie zombie = new LeveledZombie(player.getWorld(),mobLevel,loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + zombie.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + zombie.getUniqueIDString() + ".type","zombie");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + zombie.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "spider":
                                        LeveledSpider spider = new LeveledSpider(player.getWorld(),mobLevel,loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + spider.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + spider.getUniqueIDString() + ".type","spider");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + spider.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "skeleton":
                                        LeveledSkeleton leveledSkeleton = new LeveledSkeleton(player.getWorld(),mobLevel,loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledSkeleton.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledSkeleton.getUniqueIDString() + ".type","skeleton");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledSkeleton.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "witch":
                                        LeveledWitch leveledWitch = new LeveledWitch(player.getWorld(),mobLevel,loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledWitch.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledWitch.getUniqueIDString() + ".type","witch");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledWitch.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "elitegiant":
                                        EliteGiant eliteGiant = new EliteGiant(player.getWorld(),mobLevel,loc,plugin);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + eliteGiant.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + eliteGiant.getUniqueIDString() + ".type","elitegiant");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + eliteGiant.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "pillager":
                                        LeveledPillager leveledPillager = new LeveledPillager(player.getWorld(),mobLevel,loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledPillager.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledPillager.getUniqueIDString() + ".type","pillager");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledPillager.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "evoker":
                                        LeveledEvoker leveledEvoker = new LeveledEvoker(player.getWorld(),mobLevel,loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledEvoker.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledEvoker.getUniqueIDString() + ".type","evoker");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledEvoker.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "illusioner":
                                        LeveledIllusioner leveledIllusioner = new LeveledIllusioner(player.getWorld(),mobLevel,loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledIllusioner.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledIllusioner.getUniqueIDString() + ".type","illusioner");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledIllusioner.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "ravager":
                                        LeveledRavager leveledRavager = new LeveledRavager(player.getWorld(),mobLevel,loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledRavager.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledRavager.getUniqueIDString() + ".type","ravager");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledRavager.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "vindicator":
                                        LeveledVindicator leveledVindicator = new LeveledVindicator(player.getWorld(),mobLevel,loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledVindicator.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledVindicator.getUniqueIDString() + ".type","vindicator");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledVindicator.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "wither":
                                        LeveledWither leveledWither = new LeveledWither(player.getWorld(),mobLevel,loc,plugin);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledWither.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledWither.getUniqueIDString() + ".type","wither");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + leveledWither.getUniqueIDString() + ".level",mobLevel);
                                        break;
                                    case "witheradd":
                                        LeveledWitherSkeletonWithHorse add = new LeveledWitherSkeletonWithHorse(player.getWorld(),mobLevel,loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + add.getUniqueIDString() + ".location",loc);
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + add.getUniqueIDString() + ".type","witheradd");
                                        DungeonManager.config.getConfig().set(loc.getWorld().getName() + "." + add.getUniqueIDString() + ".level",mobLevel);
                                        break;

                                }
                                DungeonManager.config.saveConfig();
                                DungeonManager.config.reloadConfig();
                            }
                            else
                            {
                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Komutu doğru girdiğinizden emin olun.");
                            }
                        }
                        else if(args.length == 1)
                        {
                            if(args[0].equalsIgnoreCase("remove"))
                            {
                                Location loc = player.getLocation();
                                if(loc.getWorld().getName().contains("dungeon_"))
                                {
                                    List<Entity> entityList = player.getNearbyEntities(1,1,1);
                                    for(Entity e : entityList)
                                    {
                                        if(e instanceof CraftEntity && ((CraftEntity)e).getHandle() instanceof LeveledMob)
                                        {
                                            try {
                                                DungeonManager.config.getConfig().getConfigurationSection(loc.getWorld().getName()).getKeys(false).forEach(mob -> {
                                                    Location location = DungeonManager.config.getConfig().getLocation(loc.getWorld().getName()+"."+mob+".location");
                                                    if(location.distance(e.getLocation()) <=1)
                                                    {
                                                        plugin.getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.GREEN + "Removing the mob with id: " + ChatColor.WHITE + mob);
                                                        DungeonManager.config.getConfig().set(loc.getWorld().getName()+"."+mob,null);
                                                        e.remove();
                                                    }
                                                });
                                                DungeonManager.config.saveConfig();
                                                DungeonManager.config.reloadConfig();
                                            } catch (Exception ex) {
                                                plugin.getServer().getConsoleSender().sendMessage(getPluginPrefix() + ChatColor.RED + "An error occured. Possibly caused by not being able to find the Mob in the config.");
                                                ex.printStackTrace();
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu sadece dungeon haritalarında kullanabilirsin.");
                                }
                            }
                        }
                    }
                    break;
                }
                case "fireball":
                {
                    if (player.hasPermission("aykit.admin")) {
                        if(args.length == 1)
                        {
                            Player p = Bukkit.getPlayer(args[0]);
                            if(p == null || !p.isOnline())
                            {
                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Böyle bir oyuncu yok.");
                                return false;
                            }
                            else
                            {
                                ActionBarUtil.sendTitle(p,"",ChatColor.RED + "BOSS FIREBALL HAZIRLIYOR!",1*20,2*20,1*20);


                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                    @Override
                                    public void run() {
                                        Location loc = player.getLocation();
                                        loc.add(0,8,0);
                                        Vector nonNormalized = (p.getLocation().toVector().subtract(loc.toVector()));
                                        LargeFireball fireball = loc.getWorld().spawn(loc, LargeFireball.class);
                                        fireball.setGlowing(true);
                                        fireball.setVelocity(nonNormalized.multiply(0.3));
                                        fireball.setDirection(nonNormalized);
                                        fireball.setShooter(player);


                                        LargeFireball fireball2 = loc.getWorld().spawn(loc, LargeFireball.class);
                                        fireball2.setGlowing(true);
                                        fireball2.setVelocity(nonNormalized.multiply(0.3));
                                        fireball2.setDirection(nonNormalized);
                                        fireball2.setShooter(player);
                                    }
                                },4*20);
                            }
                        }
                        else if(args.length == 0)
                        {
                            Location loc = player.getLocation();
                            List<Location> locations = new ArrayList<>();
                            Random rand = new Random();
                            ActionBarUtil.sendTitle(player,ChatColor.RED + "Boss bomba hazırlıyor!", "", 20,2*20, 20);
                            player.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT,1.0f,1.0f);

                            for(int i = 0; i < 7; i++)
                            {
                                int randomX = rand.nextInt(20)-10;
                                int randomY = rand.nextInt(20)-10;
                                Location temp = loc.clone();
                                temp.add(randomX,0,randomY);
                                locations.add(temp);
                                //TileEntityBanner banner = new TileEntityBanner(EnumColor.RED);
                                //CraftBanner cb = new CraftBanner(Material.SKULL_BANNER_PATTERN, banner);
                                temp.add(0,1,0);
                                temp.getBlock().setType(Material.RED_BANNER,false);
                                Banner b = (Banner) temp.getBlock().getState();
                                Pattern pattern = new Pattern(DyeColor.RED, PatternType.SKULL);
                                b.addPattern(pattern);
                            }
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                                for(Location l: locations)
                                {
                                    l.getWorld().createExplosion(l,20.0f,false,false);
                                    l.getBlock().setType(Material.AIR);

                                }
                            },5*20);
                        }
                    }
                    break;
                }
                case "setspawn":
                {
                    if (player.hasPermission("aykit.admin")) {
                        Location loc = player.getLocation();
                        plugin.getConfig().set("DungeonSpawns."+loc.getWorld().getName()+".loc",loc);
                        plugin.saveConfig();
                        player.sendMessage(getPluginPrefix() + ChatColor.GREEN + loc.getWorld().getName() + ChatColor.BLUE + " adlı dünyanın spawn'ı kaydedildi!");
                    }
                    break;
                }
                case "circle":
                {
                    if (player.hasPermission("aykit.admin")) {
                        LeashParticle particle = new LeashParticle(player,10,plugin);
                        particle.runTaskTimer(plugin,0,1);
                    }
                    break;
                }
                case "takas":
                {
                    if(!classObjectMap.containsKey(player.getUniqueId()))
                    {
                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Takas komutlarını kullanabilmek için sınıfını seçmiş olmalısın.");
                        return true;
                    }
                    MMOClass playersClass = classObjectMap.get(player.getUniqueId());
                    if(args.length == 1)
                    {
                        String parameter = args[0];
                        if(parameter.equalsIgnoreCase("kabul"))
                        {
                            if(playersClass.getTradeRequester() == null)
                            {
                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Kabul edilecek bir takas isteğin bulunmuyor!");
                                return true;
                            }

                            Player requester = playersClass.getTradeRequester();
                            CustomInventory.createTradingMenu(requester,player,plugin);
                            //TODO ACCEPT THE TRADE.
                            playersClass.getTradeCountdowner().cancel();
                            playersClass.setTradeCountdowner(null);
                            return true;
                        }
                        else if(parameter.equalsIgnoreCase("red"))
                        {
                            if(playersClass.getTradeRequester() == null)
                            {
                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Red edilecek bir takas isteğin bulunmuyor!");
                                return true;
                            }

                            playersClass.setTrading(false);
                            Player requester = playersClass.getTradeRequester();
                            requester.sendMessage(getPluginPrefix() + ChatColor.GOLD + player.getName() + ChatColor.BLUE + " adlı oyuncuya yolladığınız takas isteği red edildi.");
                            player.sendMessage(getPluginPrefix() + ChatColor.BLUE + requester.getName() + ChatColor.WHITE + " adlı oyuncunun takas isteği red edildi. ");
                            classObjectMap.get(requester.getUniqueId()).setTrading(false);
                            playersClass.setTradeRequester(null);
                            playersClass.getTradeCountdowner().cancel();
                            playersClass.setTradeCountdowner(null);
                            return true;
                        }
                        Player target = Bukkit.getPlayer(parameter);
                        if (!playersClass.isTrading())
                        {
                            if(target != null && target.isOnline())
                            {
                                if(classObjectMap.containsKey(target.getUniqueId()))
                                {
                                    if (target != player)
                                    {
                                        if (target.getWorld().equals(player.getWorld()) && target.getLocation().distance(player.getLocation()) <= 5)
                                        {
                                            MMOClass targetClass = classObjectMap.get(target.getUniqueId());
                                            if(targetClass.isTrading())
                                            {
                                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu oyuncu zaten bir takas yapmakta veya bir takas isteğini değerlendirmekte.");
                                                return true;
                                            }
                                            targetClass.setTrading(true);
                                            targetClass.setTradeRequester(player);
                                            target.sendMessage(getPluginPrefix() + ChatColor.BLUE + player.getName() + ChatColor.WHITE + " adlı oyuncu size takas isteği gönderdi. Kabul etmek için, " + ChatColor.LIGHT_PURPLE + " 2dk" + ChatColor.WHITE + "'nız bulunmaktadır.");
                                            BukkitTask countdown = new BukkitRunnable() {
                                                @Override
                                                public void run() {
                                                    target.sendMessage(getPluginPrefix() + ChatColor.BLUE + player.getName() + ChatColor.WHITE + " adlı oyuncunun takas isteğini zamanında cevaplamadığınızdan dolayı, istek iptal oldu.");
                                                    player.sendMessage(getPluginPrefix() + ChatColor.BLUE + target.getName() + ChatColor.WHITE + " adlı oyuncuya gönderdiğiniz takas isteği zamanında cevaplanmadığından dolayı iptal oldu. ");
                                                    playersClass.setTrading(false);
                                                    targetClass.setTrading(false);
                                                    targetClass.setTradeRequester(null);
                                                    targetClass.setTradeCountdowner(null);
                                                }
                                            }.runTaskLater(plugin,120*20);
                                            targetClass.setTradeCountdowner(countdown);

                                            player.sendMessage(getPluginPrefix() + ChatColor.BLUE + target.getName() + ChatColor.WHITE + " adlı oyuncuya takas isteği gönderildi.");
                                            playersClass.setTrading(true);
                                            return true;
                                        }
                                        else
                                        {
                                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu oyuncu çok uzakta.");
                                            return true;
                                        }
                                    }
                                    else
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Kendine takas isteği gönderemezsin.");
                                    }
                                }
                                else
                                {
                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu oyuncu daha sınıfını seçmemiş.");
                                }
                            }
                            else
                            {
                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Böyle bir oyuncu online değil.");
                            }
                        }
                        else
                        {
                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Zaten bir takas yapmaktasınız veya bir takas isteğiniz bulunmakta.");
                        }
                    }
                    break;
                }
                case "addmoney":
                {
                    if (player.hasPermission("aykit.admin")) {
                        if(args.length == 1)
                        {
                            if(classObjectMap.containsKey(player.getUniqueId()))
                            {
                                String amountStr = args[0];

                                try
                                {
                                    int amount = Integer.parseInt(amountStr);
                                    MMOClass.gainCurrency(player,amount);
                                }
                                catch (NumberFormatException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            else
                            {
                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu kullanabilmek için önce sınıf seçmiş olmalısın!");
                            }
                        }
                        else
                        {
                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Komutu yanlış yazdınız!");
                        }
                    }
                    else
                    {
                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu komutu kullanmak için permin yok.");
                    }
                    break;
                }
                case "epicaxe":
                {
                    if (player.hasPermission("aykit.admin")) {
                        player.getInventory().addItem(CustomItems.createCustomItem(Material.DIAMOND_AXE,"Elmas Balta", CustomItems.Enderlik.OLAGANUSTU,true, CustomItems.CustomItemStats.commonDiamondAxeDamage+3,CustomItems.CustomItemStats.commonDiamondAxeSpeed));
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Epic balta verildi!");
                    }
                    return true;
                }
                case "rareaxe":
                {
                    if (player.hasPermission("aykit.admin")) {
                        player.getInventory().addItem(CustomItems.createCustomItem(Material.DIAMOND_AXE,"Elmas Balta", CustomItems.Enderlik.ESSIZ,true, CustomItems.CustomItemStats.commonDiamondAxeDamage+2,CustomItems.CustomItemStats.commonDiamondAxeSpeed));
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Rare balta verildi!");
                    }
                    return true;
                }
                case "currentdungeon":
                {
                    if (player.hasPermission("aykit.admin")) {
                        if(args.length == 1)
                        {
                            if(classObjectMap.containsKey(player.getUniqueId()) && classObjectMap.get(player.getUniqueId()).getCurrentDungeon().equalsIgnoreCase(""))
                            {
                                if(DungeonManager.dungeonMap.containsKey(args[0]))
                                {
                                    classObjectMap.get(player.getUniqueId()).setCurrentDungeon(args[0]);
                                    DungeonManager.dungeonMap.get(args[0]).getPlayerList().add(player);
                                    player.sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Dungeonınız başarıyla setlendi");
                                }
                                else
                                {
                                    player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu isimle bir dungeon bulunumadı");
                                }
                            }
                            else
                            {
                                player.sendMessage(getPluginPrefix() + ChatColor.RED + "Zaten bir set'li bir dungeonınız var.");
                            }
                        }
                        else
                        {
                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Komutu yanlış kullandınız.");
                            return true;
                        }
                    }
                    return true;
                }
                case "removedungeon":
                {
                    if (player.hasPermission("aykit.admin")) {
                        if(classObjectMap.containsKey(player.getUniqueId()) && !classObjectMap.get(player.getUniqueId()).getCurrentDungeon().equalsIgnoreCase(""))
                        {
                            DungeonManager.dungeonMap.get(classObjectMap.get(player.getUniqueId()).getCurrentDungeon()).getPlayerList().remove(player);
                            classObjectMap.get(player.getUniqueId()).setCurrentDungeon("");
                        }
                        else
                        {
                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Setli bir dungeonınız yok.");
                        }
                    }
                    return true;
                }
                case "showrolls":
                {
                    if (player.hasPermission("aykit.admin")) {
                        if(classObjectMap.containsKey(player.getUniqueId()) && !classObjectMap.get(player.getUniqueId()).getCurrentDungeon().equalsIgnoreCase(""))
                        {
                            if(DungeonManager.dungeonMap.get(classObjectMap.get(player.getUniqueId()).getCurrentDungeon()).getLoot() == null)
                                return true;
                            HashMap<ItemStack, HashMap<UUID,Integer>> temp = DungeonManager.dungeonMap.get(classObjectMap.get(player.getUniqueId()).getCurrentDungeon()).getLoot().getPlayerRolls();
                            for(ItemStack itemStack : temp.keySet())
                            {
                                player.sendMessage(ChatColor.RED + "Player rolls for item: " + itemStack.getItemMeta().getDisplayName());
                                int count = 1;
                                for(UUID uuid : temp.get(itemStack).keySet())
                                {
                                    OfflinePlayer ofPlayer = Bukkit.getOfflinePlayer(uuid);
                                    player.sendMessage(count + ".)" + ofPlayer.getName() + " : " + temp.get(itemStack).get(uuid));
                                    count++;
                                }
                            }
                            player.sendMessage();
                        }
                        else
                        {
                            player.sendMessage(getPluginPrefix() + ChatColor.RED + "Setli bir dungeonınız yok.");
                        }
                    }
                    return true;
                }
                case "getattributes":
                {
                    if (player.hasPermission("aykit.admin")) {
                        ItemStack mainHand = player.getInventory().getItemInMainHand();
                        if(!mainHand.getType().equals(Material.AIR))
                        {
                            ItemMeta meta = mainHand.getItemMeta();
                            for(Attribute att : meta.getAttributeModifiers(EquipmentSlot.HAND).keySet())
                            {
                                player.sendMessage(att + " : " + meta.getAttributeModifiers(att));
                            }
                        }
                    }
                    break;
                }
                case "yoshiramaru":
                {
                    if (player.hasPermission("aykit.admin")) {
                        player.getInventory().addItem(LegendaryItems.createYoshiramaru());
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.GOLD + "Yoshiramaru, " + ChatColor.GREEN + "envanterinize eklendi.");
                    }
                    break;
                }
                case "crimsonrage":
                {
                    if (player.hasPermission("aykit.admin")) {
                        player.getInventory().addItem(LegendaryItems.createCrimsonRage());
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.GOLD + "Kızıl Öfke, " + ChatColor.GREEN + "envanterinize eklendi.");
                    }
                    break;
                }
                case "checkarmor":
                {
                    if (player.hasPermission("aykit.admin")) {
                        double currentArmor = player.getAttribute(Attribute.GENERIC_ARMOR).getValue();
                        double currentToughness = player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.AQUA + "Şuanki zırhınız: " + ChatColor.GREEN + currentArmor);
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.AQUA + "Şuanki sertlik: " + ChatColor.GREEN + currentToughness);
                    }
                    break;
                }
                /*case "openworld":
                {
                    if (player.hasPermission("aykit.admin")) {
                        OpenWorldConfigManager.OpenWorldSaver saver = null;
                        if(OpenWorldConfigManager.openWorldMap.containsKey(player.getUniqueId()))
                        {
                            saver = OpenWorldConfigManager.openWorldMap.get(player.getUniqueId());
                        }
                        else
                        {
                            saver = new OpenWorldConfigManager.OpenWorldSaver();
                            OpenWorldConfigManager.openWorldMap.put(player.getUniqueId(),saver);
                        }
                        if(args.length == 1)
                        {
                            switch (args[0].toLowerCase())
                            {
                                case "chunkadd":
                                {
                                    saver.getChunks().add(player.getChunk());
                                    player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Chunk eklendi.");
                                    return true;
                                }
                                case "listmobs":
                                {
                                    int counter = 1;
                                    player.sendMessage(ChatColor.BLUE + "_______________MOB LISTESI______________");
                                    for(String mobName : saver.getMobsToSpawn())
                                    {
                                        player.sendMessage(ChatColor.GREEN + "" +  counter + ".)" + ChatColor.WHITE + mobName);
                                        counter++;
                                    }
                                    player.sendMessage(ChatColor.BLUE + "_______________________________________");
                                    return true;
                                }
                                case "save":
                                {
                                    if(saver.getLoc1() == null || saver.getLoc2() == null || saver.getMobsToSpawn().size() == 0 || saver.getUpperLevel() == 0 || saver.getLowerLevel() == 0 || saver.getName() == null || saver.getMobCount() == 0)
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Değerlerinden bazıları setlenmemiş. Kayıt tamamlanamadı.");
                                        return true;
                                    }
                                    FileConfiguration tempConfig = openWorldConfig.getConfig();
                                    HashMap<Chunk,Integer> chunkMap = new HashMap<>();

                                    int xMax = (int) Math.max(saver.getLoc1().getX(), saver.getLoc2().getX());
                                    int xMin = (int) Math.min(saver.getLoc1().getX(), saver.getLoc2().getX());

                                    int zMax = (int) Math.max(saver.getLoc1().getZ(), saver.getLoc2().getZ());
                                    int zMin = (int) Math.min(saver.getLoc1().getZ(), saver.getLoc2().getZ());

                                    for(int x = xMin; x <= xMax; x++)
                                    {
                                        for(int z = zMin; z <= zMax; z++)
                                        {
                                            Location temp = new Location(saver.getLoc1().getWorld(),x,0,z);
                                            if(chunkMap.containsKey(temp.getChunk()))
                                                continue;
                                            chunkMap.put(temp.getChunk(),1);
                                            tempConfig.set(saver.getLoc1().getWorld().getName()+"."+temp.getChunk()+"."+saver.getId(),"");
                                            tempConfig.set(saver.getId() + ".Chunks"+"."+temp.getChunk(),"");
                                        }
                                    }
                                    tempConfig.set(saver.getId() + ".Location1",saver.getLoc1());
                                    tempConfig.set(saver.getId()  + ".Location2",saver.getLoc2());
                                    String mobString = "";
                                    for(String mobName : saver.getMobsToSpawn())
                                    {
                                        mobString += mobName + ",";
                                    }
                                    mobString = mobString.substring(0,mobString.length()-1);
                                    tempConfig.set(saver.getId() + ".Mobs",mobString);
                                    tempConfig.set(saver.getId() + ".Upperlevel",saver.getUpperLevel());
                                    tempConfig.set(saver.getId() + ".Lowerlevel",saver.getLowerLevel());
                                    tempConfig.set(saver.getId() + ".Mobcount",saver.getMobCount());
                                    tempConfig.set(saver.getId() + ".Name",saver.getName());
                                    openWorldConfig.saveConfig();
                                    OpenWorldConfigManager.openWorldSavers.put(saver.getId(),saver);
                                    OpenWorldConfigManager.openWorldMap.remove(player.getUniqueId());
                                    player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Operasyon başarıyla kaydedildi.");
                                    return true;
                                }
                                case "detay":
                                {
                                    player.sendMessage(saver.toString());
                                    return true;
                                }
                                case "list":
                                {
                                    if(OpenWorldConfigManager.openWorldSavers.keySet().size() == 0)
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Hiç bir operasyon yok!");
                                        return true;
                                    }
                                    player.sendMessage(ChatColor.GOLD + "__________________OPERASYON LISTESI___________________");
                                    for(int id : OpenWorldConfigManager.openWorldSavers.keySet())
                                    {
                                        player.sendMessage(ChatColor.GOLD + Integer.toString(id) + ".) " + ChatColor.GREEN + OpenWorldConfigManager.openWorldSavers.get(id).getName());
                                    }
                                    player.sendMessage(ChatColor.GOLD + "______________________________________________________");
                                    return true;
                                }
                                case "deselect":
                                {
                                    OpenWorldConfigManager.openWorldMap.remove(player.getUniqueId());
                                    player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Operasyon başarıyla bırakıldı.");
                                    return true;
                                }
                            }
                        }
                        else if(args.length == 2)
                        {
                            if(args[0].equalsIgnoreCase("upperlevel"))
                            {
                                try
                                {
                                    int level = Integer.parseInt(args[1]);
                                    if(saver.getLowerLevel() >= level)
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Lower level bound'u upper level bound'undan yüksek olamaz.");
                                        return true;
                                    }
                                    saver.setUpperLevel(level);
                                    player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Upper level bound'u başarıyla setlendi!");
                                }
                                catch (NumberFormatException e)
                                {
                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Sayıyı doğru verdiğinizden emin olun.");
                                }
                                return true;
                            }
                            else if(args[0].equalsIgnoreCase("lowerlevel"))
                            {
                                try
                                {
                                    int level = Integer.parseInt(args[1]);
                                    if(saver.getUpperLevel() <= level)
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Upper level bound'u lower level bound'undan küçük olamaz.");
                                        return true;
                                    }
                                    saver.setLowerLevel(level);
                                    player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Lower level bound'u başarıyla setlendi!");
                                }
                                catch (NumberFormatException e)
                                {
                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Sayıyı doğru verdiğinizden emin olun.");
                                }
                                return true;
                            }
                            else if(args[0].equalsIgnoreCase("name"))
                            {
                                saver.setName(args[1]);
                                player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Isim başarıyla setlendi!");
                                return true;
                            }
                            else if(args[0].equalsIgnoreCase("select"))
                            {
                                try
                                {
                                    int id = Integer.parseInt(args[1]);
                                    if(OpenWorldConfigManager.openWorldSavers.containsKey(id))
                                    {
                                        OpenWorldConfigManager.openWorldMap.put(player.getUniqueId(),OpenWorldConfigManager.openWorldSavers.get(id));
                                        player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Operasyon başarıyla seçildi");
                                    }
                                    else
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu id ile bir operasyon bulunamadı.");
                                }
                                catch (NumberFormatException e)
                                {
                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Sayıyı doğru verdiğinizden emin olun.");
                                }
                                return true;
                            }
                            else if(args[0].equalsIgnoreCase("mobcount"))
                            {
                                try
                                {
                                    int mobcount = Integer.parseInt(args[1]);
                                    saver.setMobCount(mobcount);
                                    player.sendMessage(ChatColor.GREEN + "Mob count başarıyla setlendi!");
                                }
                                catch (NumberFormatException e)
                                {
                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Sayıyı doğru verdiğinizden emin olun.");
                                }
                                return true;
                            }
                            else if(args[0].equalsIgnoreCase("delete"))
                            {
                                try
                                {
                                    int id = Integer.parseInt(args[1]);
                                    if(OpenWorldConfigManager.openWorldSavers.containsKey(id))
                                    {
                                        OpenWorldConfigManager.openWorldSavers.remove(id);
                                        if(OpenWorldConfigManager.openWorldMap.get(player.getUniqueId()).getId() == id)
                                        {
                                            OpenWorldConfigManager.openWorldMap.remove(player.getUniqueId());
                                        }
                                        openWorldConfig.getConfig().set(Integer.toString(id),null);
                                        openWorldConfig.saveConfig();
                                        player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Operasyon başarıyla silindi");
                                    }
                                    else
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu id ile bir operasyon bulunamadı.");
                                }
                                catch (NumberFormatException e)
                                {
                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Sayıyı doğru verdiğinizden emin olun.");
                                }
                                return true;
                            }
                        }
                        else if(args.length == 3)
                        {
                            switch (args[0].toLowerCase())
                            {
                                case "mob":
                                {
                                    if(args[1].equalsIgnoreCase("add"))
                                    {
                                        String mobName = args[2].toLowerCase();
                                        switch (mobName)
                                        {
                                            case "zombie":
                                            case "husk":
                                            case "spider":
                                            case "skeleton":
                                            case "stray":
                                            case "witch":
                                                saver.getMobsToSpawn().add(mobName);
                                                player.sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Mob başarıyla eklendi.");
                                                break;
                                            default:
                                                player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu isimle bir mob bulanamadı.");
                                                return true;
                                        }
                                    }
                                    else if(args[1].equalsIgnoreCase("remove"))
                                    {
                                        String mobName = args[2].toLowerCase();
                                        if(saver.getMobsToSpawn().contains(mobName))
                                        {
                                            saver.getMobsToSpawn().remove(mobName);
                                            player.sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Mob, spawn listesinden başarıyla kaldırıldı.");
                                            return true;
                                        }
                                        else
                                        {
                                            player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu listede böyle bir mob bulunamadı.");
                                            return true;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    return true;
                }*/
                case "emnia":
                {
                    // /emnia 15 30 zombie,spider,husk,skeleton 20 isim
                    if(player.hasPermission("aykit.admin"))
                    {
                        if(args.length == 5)
                        {
                            try
                            {
                                if(OpenWorldConfigManager.chunSaverMap.containsKey(player.getChunk()))
                                {
                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Bu chunk'ın bir operasyonu zaten bulunuyor.");
                                    return true;
                                }

                                int lowerlevel = Integer.parseInt(args[0]);
                                int upperlevel = Integer.parseInt(args[1]);

                                if(lowerlevel > upperlevel)
                                {
                                    player.sendMessage(getPluginPrefix() + ChatColor.RED + "Lower bound, higher bound'dan yüksek olamaz.");
                                    return true;
                                }

                                String[] mobList = args[2].split(",");

                                for (String temp : mobList)
                                {
                                    if(!temp.equalsIgnoreCase("zombie") && !temp.equalsIgnoreCase("husk") && !temp.equalsIgnoreCase("spider") && !temp.equalsIgnoreCase("skeleton") && !temp.equalsIgnoreCase("stray") && !temp.equalsIgnoreCase("witch"))
                                    {
                                        player.sendMessage(getPluginPrefix() + ChatColor.RED + "Verdiğiniz mob isimlerinden biri bulunamadı.");
                                        return true;
                                    }
                                }

                                int mobCount = Integer.parseInt(args[3]);

                                String isim = args[4];

                                FileConfiguration tempConfig = openWorldConfig.getConfig();
                                tempConfig.set(player.getChunk()+".Lowerlevel",lowerlevel);
                                tempConfig.set(player.getChunk()+".Upperlevel",upperlevel);
                                tempConfig.set(player.getChunk()+".Moblist",args[2]);
                                tempConfig.set(player.getChunk()+".Mobcount",mobCount);
                                tempConfig.set(player.getChunk()+".Name",isim);
                                openWorldConfig.saveConfig();

                                OpenWorldConfigManager.OpenWorldSaver saver = new OpenWorldConfigManager.OpenWorldSaver(player.getChunk(),mobCount,lowerlevel,upperlevel,mobList,isim);
                                OpenWorldConfigManager.chunSaverMap.put(player.getChunk(),saver);
                                player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Chunk operasyonu başarıyla setlendi!");

                            }
                            catch (NumberFormatException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }

                }
                case "checkspawns":
                {
                    /*if (player.hasPermission("aykit.admin")) {
                        /*new BukkitRunnable() {
                            @Override
                            public void run() {
                                DungeonManager.checkOpenWorldMobs(plugin);
                            }
                        }.runTaskAsynchronously(plugin);
                        DungeonManager.checkOpenWorldMobs();
                    }*/
                    break;
                }
                case "lobi":
                {
                    if(player.getWorld().getName().contains("dungeon_"))
                    {
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu dünyada bu komutu kullanamazsın.");
                        return true;
                    }
                    player.sendMessage(MainClass.getPluginPrefix() + ChatColor.WHITE + "5 saniye sonra lobiye ışınlanıcaksınız. Lütfen hareket etmeyin.");
                    BukkitTask tpToLobby = new BukkitRunnable() {
                        Location startLoc = player.getLocation();
                        int count = 0;
                        @Override
                        public void run() {
                            count++;
                            if(startLoc.getX() != player.getLocation().getX() || startLoc.getY() != player.getLocation().getY()  || startLoc.getZ() != player.getLocation().getZ())
                            {
                                player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Hareket ettiğiniz için tp işlemi iptal oldu.");
                                cancel();
                            }
                            if(count == 21)
                            {
                                player.teleport(Bukkit.getWorld("world").getSpawnLocation());
                                player.sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Lobiye ışınlanıyorsunuz.");
                                cancel();
                            }
                        }
                    }.runTaskTimer(plugin,0,5);
                    break;
                }
                case "resetcooldown":
                {
                    if(player.hasPermission("aykit.admin"))
                    {
                        MainClass.playerCooldownMap.remove(player.getUniqueId());
                        player.sendMessage(getPluginPrefix() + ChatColor.GREEN + "Dungeon cooldownlarınız sıfırlandı!");
                    }
                    return true;
                }
                case "engintutku":
                {
                    if(player.hasPermission("aykit.admin"))
                    {
                        player.getInventory().addItem(LegendaryItems.createEnginTutku());
                        player.sendMessage(getPluginPrefix() + ChatColor.GOLD + "Engin Tutku," + ChatColor.GREEN + " envanterinize eklendi.");
                    }
                    return true;
                }
                case "removeall":
                {
                    if(player.hasPermission("aykit.admin"))
                    {
                        for(Entity e : Bukkit.getWorld("openworld_emnia").getEntities())
                        {
                            if(e instanceof LivingEntity)
                                ((LivingEntity)e).setHealth(0);
                            try {
                                e.remove();
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                        player.sendMessage(ChatColor.GREEN + "All entities removed!");
                    }
                    return true;
                }
            }
        }
        return true;
    }

}
