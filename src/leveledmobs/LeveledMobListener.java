package leveledmobs;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.destroystokyo.paper.event.entity.WitchThrowPotionEvent;
import minecraft.mmoplugin.DungeonManager;
import minecraft.mmoplugin.MMOClass;
import minecraft.mmoplugin.MainClass;
import minecraft.mmoplugin.Necromancer;
import minecraft.mmoplugin.events.OpenWorldConfigManager;
import net.minecraft.server.v1_16_R1.EntityCreature;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EntityMonster;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R1.entity.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LeveledMobListener implements Listener
{

    public static int mobCount;

    Plugin plugin;

    public LeveledMobListener(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLeveledMobDeathEvent(EntityDeathEvent event)
    {
        Entity entity = event.getEntity();

        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Inside leveledmob Death Event");
        if(entity instanceof CraftEntity && ((CraftEntity)entity).getHandle() instanceof LeveledMob && !(((CraftEntity)entity).getHandle() instanceof RaidBoss))
        {
            event.setDroppedExp(0);
            event.getDrops().clear();
            CraftEntity craftEntity = (CraftEntity)entity;
            LivingEntity livingEntity = (LivingEntity) entity;
            if ((livingEntity.getKiller() != null && MainClass.classObjectMap.containsKey(livingEntity.getKiller().getUniqueId())) || (entity.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent)entity.getLastDamageCause()).getDamager() instanceof LivingEntity && ((CraftEntity) ((EntityDamageByEntityEvent)entity.getLastDamageCause()).getDamager()).getHandle() instanceof Necromancer.Summon))
            {
                if(((CraftEntity)entity).getHandle() instanceof LeveledSmallMagmaCube)
                {
                    LeveledSmallMagmaCube smallMagmaCube = (LeveledSmallMagmaCube) (((CraftEntity)entity).getHandle());
                    smallMagmaCube.getParent().removeFromSmallMagmaCubes(smallMagmaCube);
                    return;
                }
                LeveledMob leveledMob = (LeveledMob)craftEntity.getHandle();
                Player player;
                if(livingEntity.getKiller() != null)
                {
                    player = (Player) ((LivingEntity) entity).getKiller();
                }
                else
                {
                    Entity damager = ((EntityDamageByEntityEvent)entity.getLastDamageCause()).getDamager();
                    CraftEntity ce = (CraftEntity) damager;
                    Necromancer.Summon summon = (Necromancer.Summon) ce.getHandle();
                    player = summon.getOwner();
                    damager = null;
                    ce = null;
                    summon = null;
                }
                MMOClass mmoClass = MainClass.classObjectMap.get(player.getUniqueId());
                double xpToBeGiven = 6*leveledMob.getLevel();
                Random rand = new Random();
                int randomRoll = rand.nextInt(100);
                double multiplier = 1;
                if(randomRoll < 5) // 5% Chance. 0-9
                {
                    multiplier = 5;
                }
                else if(randomRoll < 35) //30% Chance. 5-34
                {
                    multiplier = 3;
                }
                else if(randomRoll < 85) //50% Chance. 35-84
                {
                    multiplier = 2;
                }
                else { // 15% Chance. 85-99
                    multiplier = 1;
                }
                int dungeonMultipler = 1;
                if(entity.getWorld().getName().contains("dungeon_"))
                {
                    dungeonMultipler = 3;
                }
                int currencyToBeGiven = (int) (leveledMob.getLevel()*multiplier*dungeonMultipler);
                List<Player> nearybyFactionMembers = new ArrayList<>();
                nearybyFactionMembers.add(player);
                List<Player> nearbyFactionMembersForGold = new ArrayList<>();
                if(leveledMob.getLevel() + 25 >= mmoClass.getLevel())
                    nearbyFactionMembersForGold.add(player);
                else
                    player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + " Mob ile seviye farkınız çok olduğundan dolayı, para kazanmadınız. ");
                if (mmoClass.getFaction() != null)
                {
                    for(Player onlinePlayer : Bukkit.getWorld(player.getWorld().getName()).getPlayers())
                    {
                        if(MainClass.classObjectMap.containsKey(onlinePlayer.getUniqueId()) && MainClass.classObjectMap.get(onlinePlayer.getUniqueId()).getFaction() != null &&MainClass.classObjectMap.get(onlinePlayer.getUniqueId()).getFaction().equalsIgnoreCase(mmoClass.getFaction()))
                        { //TODO CHANGE THE ABOVE IF STATEMENT WITH MMOCLASS.ifSameFaction(player,onlinePlayer), to allow for allies to share xp. UPDATE: 24.07.2020: NOT REALLY, ALLIED FACTIONS SHOULD NOT SHARE EXP.
                            if(!player.getUniqueId().equals(onlinePlayer.getUniqueId()) && player.getLocation().distance(onlinePlayer.getLocation()) < 21)
                            {
                                if(MainClass.classObjectMap.get(onlinePlayer.getUniqueId()).getLevel() < 100)
                                    nearybyFactionMembers.add(onlinePlayer);
                                if(leveledMob.getLevel() + 25 >= MainClass.classObjectMap.get(onlinePlayer.getUniqueId()).getLevel())
                                {
                                    nearbyFactionMembersForGold.add(onlinePlayer);
                                }
                                else
                                {
                                    onlinePlayer.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + " Mob ile seviye farkınız çok olduğundan dolayı, para kazanmadınız. ");
                                }
                            }
                        }
                    }
                }
                if(leveledMob.getLevel() > 75)
                    xpToBeGiven = xpToBeGiven*2.8;
                else if(leveledMob.getLevel() > 50)
                    xpToBeGiven = xpToBeGiven*2.6;
                else if(leveledMob.getLevel() > 25)
                    xpToBeGiven = xpToBeGiven*2.3;

                xpToBeGiven = xpToBeGiven/nearybyFactionMembers.size();
                if (!(entity.getLocation().getWorld().getName().contains("faction_"))) { //NO xp gain from faction world.
                    for(Player factionmember : nearybyFactionMembers)
                    {
                        MMOClass.gainXP(factionmember,xpToBeGiven,plugin);
                    }
                }
                if(nearbyFactionMembersForGold.size() > 0)
                {
                    currencyToBeGiven = currencyToBeGiven/nearybyFactionMembers.size();
                    for(Player forGold : nearbyFactionMembersForGold)
                    {
                        MMOClass.gainCurrency(forGold,currencyToBeGiven);
                    }
                }
            }
        }
        return;
    }

    @EventHandler
    public void onNormalEntityDeathEvent(EntityDeathEvent event)
    {
        event.setDroppedExp(0);
        if(event.getEntity() instanceof CraftCow)
            return;
        else if(event.getEntity() instanceof CraftPig)
            return;
        event.getDrops().clear();
    }

    public static String getLeveledColor(int level)
    {
        if(level < 51)
            return ChatColor.WHITE + "";
        else if(level < 91)
            return ChatColor.GOLD + "";
        else if(level < 121)
            return ChatColor.RED + "";

        return ChatColor.WHITE + "";
    }

    public static void randomEquipment(LeveledMob leveledMob)
    {
        Random rand = new Random();
        int n = rand.nextInt(9);
        LivingEntity le = (LivingEntity) leveledMob.getNormalEntity();
        if(le instanceof Zombie)
        {
            if(n < 3)
            {
                //Nothing.
            }
            else if(n < 6)
            {
                ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);
                le.getEquipment().setItemInMainHand(itemStack);
            }
            else
            {
                ItemStack itemStack = new ItemStack(Material.IRON_SWORD);
                le.getEquipment().setItemInMainHand(itemStack);
            }
            n = rand.nextInt(3);
            ItemStack itemStack = null;
            switch (n)
            {
                case 0:
                    itemStack = new ItemStack(Material.CHAINMAIL_HELMET);
                    break;
                case 1:
                    itemStack = new ItemStack(Material.IRON_HELMET);
                    break;
                case 2:
                    break;
            }
            le.getEquipment().setHelmet(itemStack);
            n = rand.nextInt(3);
            switch (n)
            {
                case 0:
                    itemStack = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
                    break;
                case 1:
                    itemStack = new ItemStack(Material.IRON_CHESTPLATE);
                    break;
                case 2:
                    break;
            }
            le.getEquipment().setChestplate(itemStack);
            n = rand.nextInt(3);
            switch (n)
            {
                case 0:
                    itemStack = new ItemStack(Material.CHAINMAIL_LEGGINGS);
                    break;
                case 1:
                    itemStack = new ItemStack(Material.IRON_LEGGINGS);
                    break;
                case 2:
                    break;
            }
            le.getEquipment().setLeggings(itemStack);
            n = rand.nextInt(3);
            switch (n)
            {
                case 0:
                    itemStack = new ItemStack(Material.CHAINMAIL_BOOTS);
                    break;
                case 1:
                    itemStack = new ItemStack(Material.IRON_BOOTS);
                    break;
                case 2:
                    break;
            }
            le.getEquipment().setBoots(itemStack);
        }
        else if(le instanceof WitherSkeleton)
        {
            if(n < 3)
            {
                //Nothing.
            }
            else if(n < 6)
            {
                ItemStack itemStack = new ItemStack(Material.NETHERITE_SWORD);
                le.getEquipment().setItemInMainHand(itemStack);
            }
            else
            {
                ItemStack itemStack = new ItemStack(Material.NETHERITE_AXE);
                le.getEquipment().setItemInMainHand(itemStack);
            }
            n = rand.nextInt(4);
            if(n==0)
            {
                ItemStack itemStack = new ItemStack(Material.NETHERITE_HELMET);
                le.getEquipment().setHelmet(itemStack);
            }
            else
            {
                //Nothing.
            }
            n = rand.nextInt(4);
            if(n==0)
            {
                ItemStack itemStack = new ItemStack(Material.NETHERITE_CHESTPLATE);
                le.getEquipment().setChestplate(itemStack);
            }
            else
            {
                //Nothing.
            }
            n = rand.nextInt(4);
            if(n==0)
            {
                ItemStack itemStack = new ItemStack(Material.NETHERITE_LEGGINGS);
                le.getEquipment().setLeggings(itemStack);
            }
            else
            {
                //Nothing.
            }
            n = rand.nextInt(4);
            if(n==0)
            {
                ItemStack itemStack = new ItemStack(Material.NETHERITE_BOOTS);
                le.getEquipment().setBoots(itemStack);
            }
            else
            {
                //Nothing.
            }
        }
    }

    @EventHandler
    public void WitchPotionEvent(WitchThrowPotionEvent event)
    {
        Witch witch = event.getEntity();
        if(witch instanceof CraftEntity && ((CraftEntity)witch).getHandle() instanceof LeveledMob)
        {
            //LeveledMob mob = (LeveledMob)(((CraftEntity) witch).getHandle());
            //int mobLevel = mob.getLevel();
            ItemStack potionItem = event.getPotion();
            PotionMeta potionMeta = (PotionMeta) potionItem.getItemMeta();
            PotionEffect potionEffect;
            switch (potionMeta.getBasePotionData().getType())
            {
                case WEAKNESS:
                    potionEffect = new PotionEffect(PotionEffectType.WEAKNESS,20*20,1);
                    potionMeta.addCustomEffect(potionEffect,true);
                    break;
                case INSTANT_DAMAGE:
                    potionEffect = new PotionEffect(PotionEffectType.HARM,20,0);
                    potionMeta.addCustomEffect(potionEffect,true);
                    break;
                case POISON:
                    potionEffect = new PotionEffect(PotionEffectType.POISON,20*20,2);
                    potionMeta.addCustomEffect(potionEffect,true);
                    break;
            }
            potionItem.setItemMeta(potionMeta);
            event.setPotion(potionItem);
        }
    }

    @EventHandler
    public void onLeveledMobDamageLivingEntityEvent(EntityDamageByEntityEvent event)
    {
        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();

        if(damagee instanceof CraftEntity && ((CraftEntity)damagee).getHandle() instanceof LeveledMob && (damager instanceof Arrow || damager instanceof Fireball))
        {
            ProjectileSource projSource = ((Projectile) damager).getShooter();
            if(projSource instanceof CraftEntity && ((CraftEntity)projSource).getHandle() instanceof LeveledMob)
            {
                event.setCancelled(true);
            }
        }
        else if(damagee instanceof Player && (damager instanceof Arrow || damager instanceof Fireball))
        {
            ProjectileSource projSource = ((Projectile) damager).getShooter();
            if(projSource instanceof CraftEntity && ((CraftEntity)projSource).getHandle() instanceof LeveledMob && !(((CraftEntity)projSource).getHandle() instanceof RaidBoss))
            {
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + "Inside the leveled mob projectile modification.");
                LeveledMob leveledMob = (LeveledMob)(((CraftEntity)projSource).getHandle());
                double damage = event.getDamage();
                int mobLevel = leveledMob.getLevel();
                if (damager instanceof Arrow) {
                    if(mobLevel < 40)
                    {
                        damage += (damage);
                    }
                    else if(mobLevel < 80)
                    {
                        damage += (damage*4);
                    }
                    else
                    {
                        damage += (damage*9);
                    }
                }
                else if (damager instanceof Fireball)
                {
                    if(mobLevel < 40)
                    {
                        damage += (5*damage);
                    }
                    else if(mobLevel < 80)
                    {
                        damage += (6*damage);
                    }
                    else
                    {
                        damage += (7*damage);
                    }
                }
                event.setDamage(damage);
            }
        }
    }


    @EventHandler
    public void onIgniteEvent(EntityCombustEvent event)
    {
        if(event instanceof EntityCombustByEntityEvent)
        {
            Entity combuster = ((EntityCombustByEntityEvent) event).getCombuster();
            if(combuster instanceof Player)
            {
                return;
            }
        }
        event.setCancelled(true);
        event.getEntity().setFireTicks(0);
    }

    @EventHandler
    public void onLeveledMobGetDamagedEvent(EntityDamageEvent event)
    {
        Entity damagee = event.getEntity();

        if(damagee instanceof CraftEntity && ((CraftEntity)damagee).getHandle() instanceof LeveledMob)
        {
            if(damagee instanceof MagmaCube)
            {
                if(event.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) || event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION))
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void mobSpawnEvent(EntitySpawnEvent event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof Vex)
        {
            ((Vex) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(36);
            ((Vex) entity).setHealth(36);
            double baseAD = ((Vex) entity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue();
            ((Vex) entity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(baseAD*4);
            ItemStack itemStack = new ItemStack(Material.NETHERITE_SWORD);
            itemStack.addEnchantment(Enchantment.DAMAGE_ALL,5);
            ((Vex) entity).getEquipment().setItemInMainHand(itemStack);
            entity.setCustomName(ChatColor.WHITE + "Vex : 36.0 " + ChatColor.RED + "❤");
        }
        else if(entity instanceof Phantom)
        {
            event.setCancelled(true);
        }
        else
        {
            if(entity.getWorld().getName().contains("faction_"))
            {
                //event.setCancelled(true);
                Location loc = event.getLocation();
                if(loc.getX() < 163 && loc.getX() > -289 && loc.getZ() < 77 && loc.getZ() > -286)
                {
                    net.minecraft.server.v1_16_R1.Entity e = ((CraftEntity)entity).getHandle();
                    if(e instanceof EntityInsentient)
                    {
                        event.setCancelled(true);
                    }
                    return;
                }
                net.minecraft.server.v1_16_R1.Entity e = ((CraftEntity)entity).getHandle();
                if(e instanceof EntityMonster)
                {
                    event.setCancelled(true);
                }
            }
            else
            {
                if(entity instanceof CraftEntity)
                {
                    //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Entity was CraftEntity");
                    net.minecraft.server.v1_16_R1.Entity e = ((CraftEntity)entity).getHandle();
                    if(e instanceof EntityInsentient)
                    {
                        if(e instanceof LeveledMob || e instanceof Necromancer.Summon)
                        {
                            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Entity was leveledMob");
                            //No problem.
                        }
                        else
                        {
                            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Entity was NOT leveledmob, canceling.");
                            if(entity.getWorld().getName().equals("openworld_emnia"))
                            {
                                if(entity instanceof Zombie || entity instanceof Skeleton || entity instanceof Spider || entity instanceof Creeper || entity instanceof Enderman)
                                {
                                    event.setCancelled(true);
                                    if(mobCount != 35)
                                    {
                                        mobCount++;
                                        return;
                                    }
                                    Location loc = entity.getLocation();
                                    if(loc.getX() > 357 && loc.getX() <  469 && loc.getZ() > 468 && loc.getZ() < 471) //Stray area
                                    {

                                        Random random = new Random();
                                        int randLevel = random.nextInt(50-30)+30;

                                        LeveledStray stray = new LeveledStray(event.getLocation().getWorld(),randLevel,event.getLocation(),true);
                                    }
                                    else if(loc.getX() > -217 && loc.getX() < -157 && loc.getZ() < 154 && loc.getZ() > 53) //Witch area
                                    {
                                        Random random = new Random();
                                        int randLevel = random.nextInt(50-30)+30;

                                        LeveledWitch witch = new LeveledWitch(event.getLocation().getWorld(),randLevel,event.getLocation(),true);
                                    }
                                    else if(loc.getX() < 278 && loc.getX() > -220 && loc.getZ() < -48 && loc.getZ() > -120) //Spawn area
                                    {
                                        //DONT SPAWN ANYTHING.
                                    }
                                    else
                                    {
                                        Random random = new Random();
                                        int randLevel = random.nextInt(45-10)+10;
                                        int randomMob = random.nextInt(9);

                                        switch (randomMob)
                                        {
                                            case 0:
                                            case 1:
                                            case 2:
                                                LeveledZombie zombie = new LeveledZombie(event.getLocation().getWorld(),randLevel,event.getLocation(),true);
                                                break;
                                            case 3:
                                            case 4:
                                            case 5:
                                                LeveledHusk husk = new LeveledHusk(event.getLocation().getWorld(),randLevel,event.getLocation(),true);
                                                break;
                                            case 6:
                                            case 7:
                                                LeveledSpider spider = new LeveledSpider(event.getLocation().getWorld(),randLevel,event.getLocation(),true);
                                                break;
                                            case 8:
                                                LeveledSkeleton skeleton = new LeveledSkeleton(event.getLocation().getWorld(),randLevel,event.getLocation(),true);
                                                break;
                                        }
                                    }
                                    mobCount = 0;
                                }
                            }
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

}
