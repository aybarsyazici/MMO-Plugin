package leveledmobs;

import com.comphenix.packetwrapper.WrapperPlayServerBlockAction;
import minecraft.mmoplugin.*;
import minecraft.mmoplugin.customItems.CustomItems;
import net.minecraft.server.v1_16_R1.BlockPosition;
import net.minecraft.server.v1_16_R1.ChatComponentText;
import net.minecraft.server.v1_16_R1.TileEntityChest;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.logging.Level;

public class RaidBossListener implements Listener
{
    Plugin plugin;

    public RaidBossListener(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRaidBossDeathEvent(EntityDeathEvent event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof CraftEntity && ((CraftEntity)entity).getHandle() instanceof RaidBoss)
        {
            RaidBoss raidBoss = (RaidBoss) ((CraftEntity) entity).getHandle();
            HashMap<UUID, Double> damageList = raidBoss.getPlayerDamages();
            double totalXP = raidBoss.getLevel()*85;
            DungeonManager.Dungeon dungeon = DungeonManager.dungeonMap.get(entity.getWorld().getName());
            if(dungeon.getKickPlayers() != null)
            {
                dungeon.getKickPlayers().cancel();
                dungeon.setKickPlayers(null);
            }
            for(Player member : entity.getWorld().getPlayers())
            {
                member.sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN +"Raid boss öldü! 1dk içerisinde dışarı ışınlanıcaksınız.");
                for(Entity dungeonEntity : entity.getWorld().getEntities())
                {
                    if(!(dungeonEntity instanceof Player))
                        dungeonEntity.remove();
                }
                if(MainClass.classObjectMap.containsKey(member.getUniqueId()) && damageList.containsKey(member.getUniqueId()))
                {
                    double dmgPercentage = damageList.get(member.getUniqueId()) / raidBoss.maxHealth();
                    MMOClass.gainXP(member,dmgPercentage*totalXP,plugin);
                }
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                for(Player member : entity.getWorld().getPlayers())
                {
                    member.sendMessage(MainClass.getPluginPrefix() + ChatColor.DARK_PURPLE + "Dışarı ışınlanıyorsunuz.");
                    if(MainClass.classObjectMap.containsKey(member.getUniqueId()))
                        MainClass.classObjectMap.get(member.getUniqueId()).setCurrentDungeon("");
                    member.teleport(Bukkit.getWorld("world").getSpawnLocation());
                }
                DungeonManager.dungeonMap.get(entity.getWorld().getName()).getPlayerList().clear();
                DungeonManager.dungeonMap.get(entity.getWorld().getName()).setInProgress(false);
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Raid boss in the dungeon named: " + ChatColor.GREEN + entity.getWorld() + ChatColor.WHITE + " has been killed. Removing all entities.");
            },55*20);
            event.getDrops().clear();
            if(raidBoss instanceof EliteGiant)
            {
                //Generate the drops of this boss.
                List<ItemStack> items = DungeonManager.generateEliteGiantDrops();
                //Create the loot chest with the given drops.
                DungeonManager.DungeonLootChest lootChest = new DungeonManager.DungeonLootChest(entity.getLocation(),items,dungeon);
                //Don't forget to set the loot chest of the dungeon.
                dungeon.setLoot(lootChest);
                //TODO Elite Giant drops.
            }
            else if(raidBoss instanceof LeveledMagmaCube)
            {
                //Generate the drops of this boss.
                List<ItemStack> items = DungeonManager.generateMagmaCubeDrops();
                //Create the loot chest with the given drops.
                DungeonManager.DungeonLootChest lootChest = new DungeonManager.DungeonLootChest(entity.getLocation(),items,dungeon);
                //Don't forget to set the loot chest of the dungeon.
                dungeon.setLoot(lootChest);
            }
            else if(raidBoss instanceof LeveledWither)
            {
                //Generate the drops of this boss.
                List<ItemStack> items = DungeonManager.generateWitherDrops();
                //Create the loot chest with the given drops.
                DungeonManager.DungeonLootChest lootChest = new DungeonManager.DungeonLootChest(entity.getLocation(),items,dungeon);
                //Don't forget to set the loot chest of the dungeon.
                dungeon.setLoot(lootChest);
            }
        }
    }

    @EventHandler
    public void RaidBossSplitEvent(SlimeSplitEvent event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof CraftEntity && ((CraftEntity)entity).getHandle() instanceof LeveledMob)
        {
            event.setCount(0);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onRaidBossDamagedEvent(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof CraftEntity && ((CraftEntity)entity).getHandle() instanceof RaidBoss)
        {
            if(event.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) || event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION))
            {
                event.setCancelled(true);
                return;
            }
            RaidBoss raidBoss = (RaidBoss) ((CraftEntity) entity).getHandle();
            double Oldpercentage = raidBoss.currentHealth()/raidBoss.maxHealth();

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    LivingEntity le = (LivingEntity)entity;
                    double percentage = le.getHealth()/raidBoss.maxHealth();
                    if(!le.isDead() && percentage >= 0 && !(le instanceof Wither))
                    {
                        raidBoss.getBar().setProgress(percentage);
                        String oldTitle = raidBoss.getBar().getTitle();
                        int index = oldTitle.indexOf(":");
                        oldTitle = oldTitle.substring(0,index+1);
                        raidBoss.getBar().setTitle(oldTitle + ": " + String.format("%.2f",le.getHealth()) + ChatColor.RED + "❤");
                    }
                    int intPercentage = (int)(percentage*100);
                    int oldPercentage =  (int)(Oldpercentage*100);
                    if (entity instanceof MagmaCube)
                    {
                        LeveledMagmaCube leveledMagmaCube = (LeveledMagmaCube)raidBoss;
                        if(intPercentage < 100 && oldPercentage == 100)
                        {
                            for(Player p : raidBoss.getPlayerList().keySet())
                                p.playSound(entity.getLocation(),Sound.MUSIC_DISC_PIGSTEP,1.0f,1.0f);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                                for(Player p : raidBoss.getPlayerList().keySet())
                                    if(!entity.isDead())
                                        p.playSound(entity.getLocation(),Sound.MUSIC_DISC_PIGSTEP,1.0f,1.0f);
                            },3000);
                        }
                        if(intPercentage % 10 == 0 && intPercentage != oldPercentage && !leveledMagmaCube.isSpawningFireballs()) {
                            leveledMagmaCube.setSpawningFireballs(true);
                            List<Player> playerList = new ArrayList<>(raidBoss.getPlayerList().keySet());
                            for (Player p : playerList)
                            {
                                //ActionBarUtil.sendTitle(p, "", ChatColor.RED + "BOSS FIREBALL HAZIRLIYOR!", 1 * 20, 2 * 20, 1 * 20);
                                p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "BOSS FIREBALL HAZIRLIYOR.");
                                //p.sendMessage("intPercentage: " + intPercentage + " oldPercentage: " + oldPercentage);
                                //p.sendMessage(ChatColor.BLUE + "Playerlist is: " + raidBoss.getPlayerList());
                                //p.sendMessage(ChatColor.BLUE + "Ketset is: " + raidBoss.getPlayerList().keySet());
                            }
                            new BukkitRunnable() {
                                int count = 0;
                                @Override
                                public void run() {
                                    count++;
                                    if(count > 7)
                                    {
                                        if(playerList.size() > count-8 && !entity.isDead())
                                        {

                                            Location loc = entity.getLocation();
                                            loc.add(0,8,0);

                                            Player p = playerList.get(count-8);
                                            Vector nonNormalized = (p.getLocation().toVector().subtract(loc.toVector()));
                                            Vector normalized = (p.getLocation().toVector().subtract(loc.toVector()).normalize());
                                            LargeFireball fireball = loc.getWorld().spawn(loc, LargeFireball.class);
                                            fireball.setGlowing(true);
                                            fireball.setVelocity(nonNormalized.multiply(0.2));
                                            fireball.setDirection(nonNormalized);
                                            fireball.setShooter(le);

                                            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Vector is: " + nonNormalized);
                                            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Normalized is: " + normalized);

                                            LargeFireball fireball2 = loc.getWorld().spawn(loc, LargeFireball.class);
                                            fireball2.setGlowing(true);
                                            fireball2.setVelocity(nonNormalized.multiply(0.2));
                                            fireball2.setDirection(nonNormalized);
                                            fireball2.setShooter(le);
                                            loc.subtract(normalized);
                                        }
                                        else
                                        {
                                            leveledMagmaCube.setSpawningFireballs(false);
                                            cancel();
                                        }
                                    }
                                }
                            }.runTaskTimer(plugin,0,10);
                        }
                        if(intPercentage % 20 == 0 && oldPercentage != intPercentage)
                        {
                            List<Location> locations = new ArrayList<>();
                            Random rand = new Random();
                            for(Player player : raidBoss.getPlayerList().keySet())
                            {
                                Location loc = player.getLocation();
                                ActionBarUtil.sendTitle(player,ChatColor.RED + "Boss bomba hazırlıyor!", "", 20,2*20, 20);
                                player.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT,1.0f,1.0f);
                            }
                            for(int i = 0; i < 4; i++)
                            {
                                int randomX = rand.nextInt(20)-10;
                                int randomY = rand.nextInt(20)-10;
                                Location temp = entity.getLocation();
                                temp.add(randomX,0,randomY);
                                locations.add(temp);
                                temp.add(0,1,0);
                                temp.getBlock().setType(Material.RED_BANNER,false);
                                Banner b = (Banner) temp.getBlock().getState();
                                Pattern pattern = new Pattern(DyeColor.RED, PatternType.SKULL);
                                b.addPattern(pattern);
                            }
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                                for(Location l: locations)
                                {
                                    if(!entity.isDead())
                                        l.getWorld().createExplosion(l,8.0f,false,false);
                                    l.getBlock().setType(Material.AIR);
                                }
                            },5*20);
                        }
                        if(intPercentage < 25 && oldPercentage >= 25 && !leveledMagmaCube.isReinforcementsCalled())
                        {
                            List<LeveledSmallMagmaCube> magmaCubeList = new ArrayList<>();
                            for(int i = 0; i < 5; i++)
                            {
                                Random rand = new Random();
                                int randomLevel = rand.nextInt(50-30) + 30;
                                LeveledSmallMagmaCube smallMagmaCube = new LeveledSmallMagmaCube(entity.getWorld(),randomLevel,entity.getLocation(),leveledMagmaCube);
                                magmaCubeList.add(smallMagmaCube);
                            }
                            for(Player p : raidBoss.getPlayerList().keySet())
                            {
                                ActionBarUtil.sendTitle(p,ChatColor.RED + "Boss bölündü!", "",20,2*20,20);
                                p.playSound(p.getLocation(),Sound.BLOCK_BELL_RESONATE,1.0f,1.0f);
                            }
                            leveledMagmaCube.setReinforcementsCalled(true);
                            leveledMagmaCube.setSmallMagmaCubes(magmaCubeList);
                            BukkitTask healMagmaCube = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.AQUA + "Small magma cube size: " + leveledMagmaCube.getSmallMagmaCubes().size());
                                    if(!leveledMagmaCube.getNormalEntity().isDead() && leveledMagmaCube.getSmallMagmaCubes().size() != 0)
                                    {
                                        for(Player ppp : leveledMagmaCube.getPlayerList().keySet())
                                        {
                                            ppp.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Boss can dolduruyor!");
                                        }
                                        double healthBeforeHeal = le.getHealth();
                                        healthBeforeHeal += (healthBeforeHeal*10/100);
                                        if(healthBeforeHeal < le.getMaxHealth())
                                        {
                                            le.setHealth(healthBeforeHeal);
                                            raidBoss.getBar().setProgress(healthBeforeHeal/le.getMaxHealth());
                                        }
                                        else
                                        {
                                            le.setHealth(le.getMaxHealth());
                                            raidBoss.getBar().setProgress(100.0);
                                        }
                                    }
                                    else
                                    {
                                        for(Player ppp : leveledMagmaCube.getPlayerList().keySet())
                                        {
                                            ppp.sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Boss can doldurmayı bıraktı!");
                                        }
                                        cancel();
                                    }
                                }
                            }.runTaskTimer(plugin,0,100);
                        }
                    }
                    else if(entity instanceof Giant)
                    {
                        if(intPercentage < 100 && oldPercentage == 100)
                        {
                            for(Player p : raidBoss.getPlayerList().keySet())
                                p.playSound(entity.getLocation(), Sound.MUSIC_DISC_STAL,1.0f,1.0f);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                                for(Player p : raidBoss.getPlayerList().keySet())
                                    if(!entity.isDead())
                                        p.playSound(entity.getLocation(),Sound.MUSIC_DISC_STAL,1.0f,1.0f);
                            },3000);
                        }
                        if((intPercentage < 75 && oldPercentage >= 75) || (intPercentage < 50 && oldPercentage >=50) || (intPercentage < 25 && oldPercentage >=25))
                        {
                            Location currentLocation = le.getLocation();
                            Location oldLocation = currentLocation.clone();
                            currentLocation.add(0,10,0);
                            for(Player target : raidBoss.getPlayerList().keySet())
                            {
                                ActionBarUtil.sendTitle(target,ChatColor.DARK_AQUA + "Boss güçleniyor!","",20,2*20,20);
                                if(!(intPercentage < 25 && oldPercentage >=25))
                                    target.playSound(target.getLocation(),Sound.ENTITY_ELDER_GUARDIAN_CURSE,1.0f, 1.0f);
                                else
                                    target.playSound(target.getLocation(),Sound.ENTITY_ENDER_DRAGON_GROWL,1.0f,1.0f);
                            }
                            long oldTime = entity.getWorld().getTime();
                            entity.getWorld().setTime(16000);
                            BukkitTask stayInAir = new BukkitRunnable() {
                                int counter = 0;
                                @Override
                                public void run() {
                                    entity.teleport(currentLocation);
                                    counter++;
                                    if(counter%30==0)
                                    {
                                        for(Player target : raidBoss.getPlayerList().keySet())
                                        {
                                            target.getWorld().strikeLightning(target.getLocation());
                                        }
                                    }
                                    if(counter==90)
                                    {
                                        entity.getWorld().setTime(oldTime);
                                        le.teleport(oldLocation);
                                        cancel();
                                    }
                                }
                            }.runTaskTimer(plugin,0,1);

                            if(intPercentage < 75 && oldPercentage >= 75)
                            {
                                le.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
                                le.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                                le.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                                le.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                                le.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
                            }
                            else if(intPercentage < 50 && oldPercentage >=50)
                            {
                                le.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
                                le.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                                le.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                                le.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                                le.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                            }
                            else
                            {
                                le.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
                                le.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
                                le.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
                                le.getEquipment().setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
                                le.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));
                            }
                        }
                    }
                    else if(entity instanceof Wither)
                    {
                        LeveledWither leveledWither = (LeveledWither) raidBoss;
                        DungeonManager.Dungeon dungeon = DungeonManager.dungeonMap.get(entity.getWorld().getName());
                        if(!leveledWither.isMechanicsStarted())
                        {
                            plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.LIGHT_PURPLE + "Wither mechanics started!");
                            leveledWither.setMechanicsStarted(true);
                            BukkitTask task = new RaidBoss.WitherSkeletonMechanics((Wither) entity, DungeonManager.dungeonMap.get(entity.getWorld().getName()), plugin).runTaskTimer(plugin,0,40);
                            for(Player p : dungeon.getPlayerList())
                                p.playSound(entity.getLocation(), Sound.MUSIC_DISC_MELLOHI,1.0f,1.0f);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                                for(Player p : dungeon.getPlayerList())
                                    if(!entity.isDead())
                                        p.playSound(entity.getLocation(),Sound.MUSIC_DISC_MELLOHI,1.0f,1.0f);
                            },3000);
                        }
                        else if(intPercentage < 50 && oldPercentage >= 50 && leveledWither.getEnrageLevel().equals(LeveledWither.Enrange.NONE))
                        {
                            leveledWither.setEnrageLevel(LeveledWither.Enrange.SOFT);
                            try
                            {
                                double ms = le.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getDefaultValue();

                                ms += ms*0.20;

                                le.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(ms);

                                for(Player inDungeon : dungeon.getPlayerList())
                                {
                                    ActionBarUtil.sendTitle(inDungeon,"",ChatColor.GOLD + "Boss soft enrageledi!",20,20,20);
                                    inDungeon.playSound(inDungeon.getLocation(),Sound.ENTITY_WITHER_SPAWN,1.0f,1.0f);
                                }
                            }
                            catch (Exception e)
                            {
                                plugin.getServer().getLogger().log(Level.SEVERE, "Could not set the attribute instance of Wither Raid Boss", e);
                            }
                        }
                        else if(intPercentage < 25 & oldPercentage >=25 && !leveledWither.getEnrageLevel().equals(LeveledWither.Enrange.SOFT))
                        {
                            leveledWither.setEnrageLevel(LeveledWither.Enrange.HARD);
                            try
                            {
                                double ms = le.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getDefaultValue();

                                ms += ms*0.40;

                                le.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(ms);

                                for(Player inDungeon : dungeon.getPlayerList())
                                {
                                    ActionBarUtil.sendTitle(inDungeon,"",ChatColor.RED + "Boss hard enrageledi!",20,20,20);
                                    inDungeon.playSound(inDungeon.getLocation(),Sound.ENTITY_ENDER_DRAGON_GROWL,1.0f,1.0f);
                                }
                            }
                            catch (Exception e)
                            {
                                plugin.getServer().getLogger().log(Level.SEVERE, "Could not set the attribute instance of Wither Raid Boss", e);
                            }
                            /*for(Player p : )
                            ActionBarUtil.sendTitle();*/
                            plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Boss hard enrageledi!");
                        }
                    }
                }
            },0);
        }
    }

    @EventHandler
    public void onEliteGiantDamagedByPlayerOrSummonEvent(EntityDamageByEntityEvent event)
    {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if(entity instanceof CraftEntity && ((CraftEntity)entity).getHandle() instanceof RaidBoss)
        {
            UUID uuid = null;
            if(damager instanceof Player)
            {
                Player player = (Player) damager;
                uuid = player.getUniqueId();
            }
            else if(((CraftEntity)damager).getHandle() instanceof Necromancer.Summon)
            {
                Player player = ( (Necromancer.Summon)(((CraftEntity) damager).getHandle())).getOwner();
                uuid = player.getUniqueId();
            }
            else
            {
                return;
            }
            RaidBoss raidBoss = (RaidBoss) ((CraftEntity) entity).getHandle();
            HashMap<UUID, Double> playerDamages = raidBoss.getPlayerDamages();
            if(playerDamages.containsKey(uuid))
            {
                Double oldDamageDealt = playerDamages.get(uuid);
                oldDamageDealt += event.getDamage();
                playerDamages.put(uuid, oldDamageDealt);
            }
            else
                playerDamages.put(uuid, event.getDamage());
            raidBoss.setPlayerDamages(playerDamages);
        }
        else if(entity instanceof Player && damager instanceof Fireball)
        {
            Fireball fireball = (Fireball)damager;
            if (fireball.getShooter() instanceof CraftEntity && ((CraftEntity)fireball.getShooter()).getHandle() instanceof RaidBoss)
            {
                if(((CraftEntity)fireball.getShooter()).getHandle() instanceof LeveledMagmaCube)
                {
                    double damage = event.getDamage();
                    event.setDamage(damage * 12);
                }
                else if(((CraftEntity)fireball.getShooter()).getHandle() instanceof LeveledWither)
                {
                    plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + "Fireball damager was wither.");
                    double damage = event.getDamage();
                    LeveledWither lWither = (LeveledWither) ((CraftEntity)fireball.getShooter()).getHandle();
                    switch (lWither.getEnrageLevel())
                    {
                        case NONE:
                            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.AQUA + "Damage was multiplied by 4");
                            damage += 3*damage;
                            break;
                        case SOFT:
                            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.AQUA + "Damage was multiplied by 5");
                            damage += 4*damage;
                            break;
                        case HARD:
                            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.AQUA + "Damage was multiplied by 6");
                            damage += 5*damage;
                            break;
                    }
                    event.setDamage(damage);
                }
            }
        }
        else if(entity instanceof Player && damager instanceof CraftEntity && ((CraftEntity)damager).getHandle() instanceof RaidBoss)
        {
            /*if(damager instanceof Giant && event.getDamage() != 0)
            {
                PotionEffect potionEffect = new PotionEffect(PotionEffectType.POISON,3*20,1);
                ((Player) entity).addPotionEffect(potionEffect);
            }*/
        }
    }

    public static WrapperPlayServerBlockAction PrepareChestPacket(Block block, boolean open) {
        WrapperPlayServerBlockAction blockAction = new WrapperPlayServerBlockAction();
        blockAction.setBlockType(block.getType());
        blockAction.setByte1(1);
        blockAction.setByte2(open ? 1 : 0);
        //MainClass.getPlugin(MainClass.class).getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Location is: " + ChatColor.WHITE + block.getLocation());
        blockAction.setLocation(new com.comphenix.protocol.wrappers.BlockPosition(block.getX(),block.getY(),block.getZ()));
        return blockAction;
    }


    @EventHandler
    public void onLootChestInteractEvent(PlayerInteractEvent event) //TODO: is it inventory open event, or interact event?
    {
        Player p = (Player) event.getPlayer();
        if(!MainClass.classObjectMap.containsKey(p.getUniqueId()))
            return;
        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;
        if(p.getWorld().getName().contains("dungeon_") && event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.CHEST))
        {
            MMOClass mmoClass = MainClass.classObjectMap.get(p.getUniqueId());
            if(DungeonManager.dungeonMap.containsKey(mmoClass.getCurrentDungeon()))
            {
                DungeonManager.Dungeon dungeon = DungeonManager.dungeonMap.get(mmoClass.getCurrentDungeon());
                //Now get the lootchest.
                DungeonManager.DungeonLootChest lootChest = dungeon.getLoot();
                //Now get the items and create a Custom Inventory.
                if(lootChest != null)
                {
                    if (lootChest.getLoc().equals(event.getClickedBlock().getState().getLocation())) {
                        event.setCancelled(true);
                        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Opening LootChest inventory!");
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                            p.openInventory(lootChest.getHolder().getInventory());
                            WrapperPlayServerBlockAction packet = PrepareChestPacket(lootChest.getBlock(),true);
                            for(Player dungeonMember : dungeon.getPlayerList())
                            {
                                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.AQUA + "Sending packet to: " + dungeonMember);
                                packet.sendPacket(dungeonMember);
                                dungeonMember.playSound(lootChest.getLoc(),Sound.BLOCK_CHEST_OPEN,1.0f,1.0f);
                            }
                        },0);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerCloseLootInventory(InventoryCloseEvent event)
    {

        if(event.getInventory().getHolder() instanceof DungeonManager.DungeonLootHolder)
        {
            //We know that the closed inventory is a loot inventory.
            //Check how many more players are looking at it.
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Viewers before schedule: " + event.getInventory().getViewers().size());
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.YELLOW + "Viewers after schedule: " + event.getInventory().getViewers().size());
                if(event.getInventory().getViewers().size() == 0)
                {
                    //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GOLD + "Nobody is looking at the loot inventory.");
                    //Nobody is looking at it! Close the chest
                    DungeonManager.DungeonLootHolder holder = (DungeonManager.DungeonLootHolder) event.getInventory().getHolder();
                    WrapperPlayServerBlockAction packet = PrepareChestPacket(holder.getLootChest().getBlock(),false);
                    for(Player dungeonMember : holder.getDungeon().getPlayerList())
                    {
                        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.AQUA + "Sending closing packet to " + dungeonMember);
                        packet.sendPacket(dungeonMember);
                        dungeonMember.playSound(holder.getLootChest().getLoc(),Sound.BLOCK_CHEST_CLOSE,1.0f,1.0f);
                    }
                }
            },0);
        }
    }



    @EventHandler
    public void onLootChestClickEvent(InventoryClickEvent event)
    {
        Inventory clickedInventory = event.getClickedInventory();

        if(event.isCancelled())
            return;

        if(clickedInventory == null)
            return;

        Inventory menu = event.getInventory();

        if(menu != clickedInventory)
            return;

        if(menu.getHolder() instanceof DungeonManager.DungeonLootHolder)
        {
            DungeonManager.DungeonLootHolder holder = (DungeonManager.DungeonLootHolder) menu.getHolder();

            event.setCancelled(true);

            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + "Here");

            Player player = (Player)event.getWhoClicked();

            if(!MainClass.classObjectMap.containsKey(player.getUniqueId()))
                return;

            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + "Here 2");

            MMOClass mmoClass = MainClass.classObjectMap.get(player.getUniqueId());
            if(mmoClass.getCurrentDungeon().equalsIgnoreCase(""))
                return;

            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + "Here 3");

            DungeonManager.Dungeon dungeon = holder.getDungeon();

            player.updateInventory();

            int clickedSlot = event.getSlot();

            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + "Clicked slot is: " + clickedSlot);

            switch (clickedSlot)
            {
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                {
                    player.playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    ItemStack clickedItem = menu.getItem(clickedSlot);
                    if(clickedItem != null && !clickedItem.getType().equals(Material.AIR))
                    {
                        //We know that the player has clicked on a non-null item.
                        int roll = new Random().nextInt(100);
                        if(holder.getLootChest().addPlayerRoll(holder.getLootChest().getItems().get(clickedSlot-10),player,roll))
                        {
                            //Get it's Meta:
                            ItemMeta copyMeta = clickedItem.getItemMeta();
                            //Check if it already has lore.
                            List<String> lore = copyMeta.getLore();
                            if(lore == null)
                                lore = new ArrayList<>(); //If it doesnt have lore, just create a new one.
                            lore.add("");
                            lore.add(ChatColor.YELLOW + player.getName() + ": " + ChatColor.BLUE + roll);
                            copyMeta.setLore(lore);
                            clickedItem.setItemMeta(copyMeta);
                            menu.setItem(clickedSlot,clickedItem);

                            for(Player inDungeon : dungeon.getPlayerList())
                            {
                                inDungeon.sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + player.getDisplayName() + ", " + ChatColor.GOLD + clickedItem.getItemMeta().getDisplayName() + ChatColor.BLUE + " adlı eşyaya " + ChatColor.GOLD + roll + ChatColor.BLUE + " rolladı!");
                                inDungeon.updateInventory();

                                /*if(inDungeon.getOpenInventory().getTitle().contains("Dungeon Dropları"))
                                {
                                    inDungeon.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                                }*/
                            }
                            return;
                        }
                        else
                        {
                            player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Zaten roll atmış bulunmaktasınız.");
                            return;
                        }
                    }

                    break;
                }
                default:
                    break;
            }
        }
    }


}
