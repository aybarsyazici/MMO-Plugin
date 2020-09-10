package minecraft.mmoplugin;

import net.minecraft.server.v1_16_R1.EntityLiving;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.util.HashMap;

public class NecromancerListener implements Listener
{
    Plugin plugin;

    NecromancerListener(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSummonerDamageLivingEntity(EntityDamageByEntityEvent event)
    {
        if(event.isCancelled())
            return;
        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();
        if(MMOClass.ifSameFaction(damager,damagee))
            return;
        if(damager instanceof Player && MainClass.classObjectMap.containsKey(damager.getUniqueId()) && MainClass.classObjectMap.get(damager.getUniqueId()).getClassName().equalsIgnoreCase("necromancer"))
        {
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Necromancer attacked, setting the current target for your summon to, " + damagee);

            if(!MMOClassListener.canDamage((Player)damager, event.getCause()))
            {
                event.setCancelled(true);
                return;
            }
            Necromancer necromancer = (Necromancer)MainClass.classObjectMap.get(damager.getUniqueId());
            Necromancer.Summon summon = necromancer.getSummon();
            if(summon == null)
                return;
            if(!summon.getStance().equals(MobStance.type.PASSIVE))
                return;
            if(summon.getid().equals(damagee.getUniqueId()))
            {
                event.setCancelled(true);
                return;
            }
            if(summon.getRidingSummon() != null && summon.getRidingSummon().getid().equals(damagee.getUniqueId()))
            {
                event.setCancelled(true);
                return;
            }
            else
            {
                EntityLiving targetOfSummon = (((CraftMob)summon.getNormalEntity()).getHandle()).getGoalTarget();
                if(damagee instanceof LivingEntity &&  targetOfSummon!= null && targetOfSummon.getUniqueID().equals(damagee.getUniqueId()))
                {
                    double dmg = event.getDamage();
                    dmg = dmg*20/100;
                    damager.sendMessage(MainClass.getPluginPrefix() + "Summon'ın ile aynı kişiye vuruyorsun! Extra: " + ChatColor.RED + String.format("%.2f",dmg) + ChatColor.WHITE + " hasar vurdun!");
                    //((LivingEntity) damagee).damage(dmg);
                    event.setDamage(event.getDamage() + dmg);
                }
                necromancer.getSummon().setTarget(damagee);
                if(necromancer.getSummon().getNormalEntity() instanceof SkeletonHorse)
                    necromancer.getSummon().getRidingSummon().setTarget(damagee);
            }
        }
    }

    @EventHandler
    public void onNecromancerPlayerInteractEvent(PlayerInteractEntityEvent event)
    {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        Entity e = event.getRightClicked();
        //player.sendMessage("Your item in hand is " + player.getItemInHand());
        if(MainClass.classObjectMap.containsKey(player.getUniqueId()) && MainClass.classObjectMap.get(player.getUniqueId()).getClassName().equalsIgnoreCase("necromancer"))
        {
            Necromancer necromancer = (Necromancer) MainClass.classObjectMap.get(player.getUniqueId());
            if(necromancer.getSummon() != null && necromancer.getSummon().getid().equals(e.getUniqueId()) && player.getInventory().getItemInMainHand().getType().equals(Material.AIR))
            {
                SummonInventory.createInfoInventory(plugin,player,necromancer.getSummon());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onNecromancerRightClickEvent(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        if(MainClass.classObjectMap.containsKey(player.getUniqueId()) && MainClass.classObjectMap.get(player.getUniqueId()).getClassName().equalsIgnoreCase("necromancer"))
        {
            if((event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && player.getInventory().getItemInMainHand().getType().equals(Material.ENCHANTED_BOOK))
            {
                SummonInventory.createControlInventory(plugin,player);
            }
        }
    }

    public static void createSummon(Player player)
    {
        Necromancer necromancer = (Necromancer) MainClass.classObjectMap.get(player.getUniqueId());
        if (necromancer.getSummon() == null)
        {
            if (MainClass.playerCooldownMap.get(player.getUniqueId()).get("Summon").isBeforeNow())
            {
                int level = necromancer.getLevel();
                if (level < 26)
                {
                    Necromancer.CustomZombie customZombie = necromancer.new CustomZombie(player.getWorld(), player, MainClass.getPlugin(MainClass.class));
                    necromancer.setSummon(customZombie);
                }
                else if (level < 51)
                {
                    Necromancer.CustomSkeleton customSkeleton = necromancer.new CustomSkeleton(player.getWorld(), player, MainClass.getPlugin(MainClass.class));
                    necromancer.setSummon(customSkeleton);
                }
                else if(level < 76)
                {
                    Necromancer.CustomWitherSkeleton customWitherSkeleton = necromancer.new CustomWitherSkeleton(player.getWorld(), player, MainClass.getPlugin(MainClass.class));
                    necromancer.setSummon(customWitherSkeleton);
                }
                else
                {
                    Necromancer.CustomSkeletonHorse customSkeletonHorse = necromancer.new CustomSkeletonHorse(player.getWorld(), player, MainClass.getPlugin(MainClass.class));
                    necromancer.setSummon(customSkeletonHorse);
                }
            }
            else
            {
                player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu yeteneği kullanmak için, " + ChatColor.GOLD + Seconds.secondsBetween(new DateTime(), MainClass.playerCooldownMap.get(player.getUniqueId()).get("Summon")).getSeconds() + "saniye" + ChatColor.RED + " daha beklemelisin.");
            }
            //MainClass.classObjectMap.put(player.getUniqueId(),necromancer);
        }
        else
        {
            player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Zaten bir tane Summon'ın bulunuyor.");
        }
    }

    public static void changeSummonStance(Player player, MobStance.type stance)
    {
        MMOClass mmoClass = MainClass.classObjectMap.get(player.getUniqueId());
        if (mmoClass.getClassName().equalsIgnoreCase("necromancer"))
        {
            Necromancer necromancer = (Necromancer) MainClass.classObjectMap.get(player.getUniqueId());
            if (necromancer.getSummon() != null)
            {
                necromancer.getSummon().setStance(stance);
                if(necromancer.getSummon().getNormalEntity() instanceof SkeletonHorse)
                {
                    necromancer.getSummon().getRidingSummon().setStance(stance);
                }
                player.sendMessage(MainClass.getPluginPrefix() + "Summonınızın stance'i, " + ChatColor.GREEN + necromancer.getSummon().getStance() + ChatColor.WHITE + "'e çevirildi.");
            }
            else
            {
                player.sendMessage(MainClass.getPluginPrefix() + "Bir summon'ın bulunmuyor.");
            }
        }
    }

    @SuppressWarnings("deprecation") //SUPRESS WARNINGS HERE.
    public static void healSummon(Player player)
    {
        Necromancer necromancer = (Necromancer) MainClass.classObjectMap.get(player.getUniqueId());
        if(necromancer.getSummon() == null)
        {
            player.sendMessage(MainClass.getPluginPrefix() + "Bir summon'ın bulunmuyor.");
            return;
        }
        HashMap<String, DateTime> cdMap = MainClass.playerCooldownMap.get(player.getUniqueId());
        if(cdMap.containsKey("SummonHeal"))
        {
            if (cdMap.get("SummonHeal").isBeforeNow())
            {
                try {
                    DateTime dt = new DateTime();
                    dt = dt.plusSeconds(MainClass.SummonHealCooldown);
                    cdMap.put("SummonHeal", dt);
                    MainClass.playerCooldownMap.put(player.getUniqueId(),cdMap);

                    LivingEntity le = (LivingEntity)necromancer.getSummon().getNormalEntity();
                    le.setHealth(le.getMaxHealth());
                    player.sendMessage(MainClass.getPluginPrefix() + "Summonının canı fullendi!");

                    String oldName = le.getName();
                    int index = oldName.indexOf(":");
                    oldName = oldName.substring(0, index);

                    Double newHealth = le.getHealth();
                    String health = String.format("%.2f", newHealth);

                    oldName += ChatColor.WHITE + ": " + health + ChatColor.RED + "❤";
                    le.setCustomName(oldName);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
            else
            {
                player.sendMessage(MainClass.getPluginPrefix() + "Bu yeteneği kullanabilmek için, " + ChatColor.RED + Seconds.secondsBetween(new DateTime(),MainClass.playerCooldownMap.get(player.getUniqueId()).get("SummonHeal")).getSeconds() + " saniye " + ChatColor.WHITE  + " beklemelisin." );
            }
        }
    }

    @EventHandler
    public static void onSummonerGetDamagedEvent(EntityDamageEvent event)
    {
        if(event.isCancelled())
            return;
        Entity entity = event.getEntity();
        if(entity instanceof Player && MainClass.classObjectMap.containsKey(entity.getUniqueId()) && MainClass.classObjectMap.get(entity.getUniqueId()).getClassName().equalsIgnoreCase("necromancer"))
        {
            MMOClass mmoClass = MainClass.classObjectMap.get(entity.getUniqueId());
            Player player = (Player) entity;
            int level = mmoClass.getLevel();
            if(level > 99)
            {
                Necromancer necromancer = (Necromancer)mmoClass;
                if(necromancer.getSummon() != null && !necromancer.getSummon().getNormalEntity().isDead())
                {
                    double currentHealth = player.getHealth();
                    double damage = event.getDamage();
                    double damageReducedByArmor = event.getDamage(EntityDamageEvent.DamageModifier.ARMOR);
                    double damageReducedByAbsorption = event.getDamage(EntityDamageEvent.DamageModifier.ABSORPTION);
                    double damageReducedByBlocking = event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING);
                    damage = damage + (damageReducedByArmor + damageReducedByAbsorption + damageReducedByBlocking);
                    if((currentHealth - damage <= 0))
                    {
                        necromancer.getSummon().kill();
                        event.setCancelled(true);
                        player.setHealth(player.getMaxHealth());
                        player.sendMessage(MainClass.getPluginPrefix() + "Summon'ın kendini feda etti! Canın fullendi!");
                    }
                }
            }
        }
    }

    @EventHandler
    public static void onSummonerDeathEvent(PlayerDeathEvent event)
    {
        if(event.isCancelled())
            return;

        Player p = event.getEntity();
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()) && MainClass.classObjectMap.get(p.getUniqueId()).getClassName().equalsIgnoreCase("necromancer"))
        {
            //We know that the player who died is a necromancer.
            //Check if he/she has a summon currently alive.
            Necromancer necromancer = (Necromancer)MainClass.classObjectMap.get(p.getUniqueId());
            if(necromancer.getSummon() != null && !necromancer.getSummon().getNormalEntity().isDead())
            {
                //So we know that summoner has died and he has a summon which is still alive.
                //Thus kill it.
                necromancer.getSummon().kill();
            }
        }
    }


    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event)
    {
        if(event.isCancelled())
            return;
        if(event.getClickedInventory() == null)
            return;
        Player p = (Player) event.getWhoClicked();
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()) && MainClass.classObjectMap.get(p.getUniqueId()).getClassName().equalsIgnoreCase("necromancer"))
        {
            ItemStack itemToBeChanged = event.getWhoClicked().getItemOnCursor();
            if(itemToBeChanged != null && itemToBeChanged.getType().equals(Material.SHIELD))
            {
                event.setCancelled(true);
                p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Summoner'lar kalkan kullanamaz.");
                event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
                return;
            }
            if(!event.getClickedInventory().getType().equals(InventoryType.PLAYER))
                return;
            if(event.getSlot() == 40)
            {
                if(itemToBeChanged.getType().equals(Material.SHIELD))
                {
                    event.setCancelled(true);
                    p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Summoner'lar kalkan kullanamaz.");
                }
            }
            else if(event.getSlotType().equals(InventoryType.SlotType.QUICKBAR))
            {
                if(itemToBeChanged.getType().equals(Material.SHIELD))
                {
                    event.setCancelled(true);
                    p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Summoner'lar kalkan kullanamaz.");
                }
            }
        }
    }

    @EventHandler
    public void swapMainHandEvent(PlayerSwapHandItemsEvent event)
    {
        if (event.isCancelled())
            return;

        Player p = event.getPlayer();
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()) && MainClass.classObjectMap.get(p.getUniqueId()).getClassName().equalsIgnoreCase("necromancer"))
        {
            if(event.getOffHandItem().getType().equals(Material.SHIELD))
            {
                p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Summoner'lar kalkan kullanamaz!");
                event.setCancelled(true);
                p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler
    public void changeMainHandEvent(PlayerChangedMainHandEvent event)
    {
        Player p = event.getPlayer();
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()) && MainClass.classObjectMap.get(p.getUniqueId()).getClassName().equalsIgnoreCase("necromancer"))
        {
            if(p.getInventory().getItemInMainHand().getType().equals(Material.SHIELD))
            {
                p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Summoner'lar kalkan kullanamaz!");
                p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler
    public void ItemHeldEvent(PlayerItemHeldEvent event)
    {
        if(event.isCancelled())
            return;

        Player p = event.getPlayer();
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()) && MainClass.classObjectMap.get(p.getUniqueId()).getClassName().equalsIgnoreCase("necromancer"))
        {
            int i = event.getNewSlot();
            if (p.getInventory().getItem(i) != null && p.getInventory().getItem(i).getType().equals(Material.SHIELD))
            {
                p.getInventory().setItem(i,new ItemStack(Material.AIR));
                p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Summoner'lar kalkan kullanamaz.");
            }
        }
    }
    @EventHandler
    public void pickupEvent(PlayerAttemptPickupItemEvent event)
    {
        if (event.isCancelled())
            return;

        Player p = event.getPlayer();
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()) && MainClass.classObjectMap.get(p.getUniqueId()).getClassName().equalsIgnoreCase("necromancer"))
        {
            if(event.getItem().getType().equals(Material.SHIELD))
            {
                p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Summoner'lar kalkan kullanamaz.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDragEvent(InventoryDragEvent event)
    {
        if(event.isCancelled())
            return;

        Player p = (Player) event.getWhoClicked();
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()) && MainClass.classObjectMap.get(p.getUniqueId()).getClassName().equalsIgnoreCase("necromancer"))
        {
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + event.getCursor());
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + event.getOldCursor());
            if(event.getOldCursor().getType().equals(Material.SHIELD))
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void SikimiYe2(PlayerArmorStandManipulateEvent event)
    {
        if (event.isCancelled())
            return;

        Player p = event.getPlayer();
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()) && MainClass.classObjectMap.get(p.getUniqueId()).getClassName().equalsIgnoreCase("necromancer"))
        {
            ItemStack itemStack = event.getPlayerItem();
            if (itemStack.getType().equals(Material.SHIELD))
            {
                event.setCancelled(true);
                event.getPlayerItem().setAmount(0);
                return;
            }
        }
    }

}
