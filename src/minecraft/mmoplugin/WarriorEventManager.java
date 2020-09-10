package minecraft.mmoplugin;

import leveledmobs.RaidBoss;
import net.md_5.bungee.protocol.packet.Chat;
import net.minecraft.server.v1_16_R1.EntityLiving;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.joda.time.DateTime;
import particles.CircleParticle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class WarriorEventManager implements Listener {

    Plugin plugin;

    WarriorEventManager(Plugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onWarriorDamageEvent(EntityDamageByEntityEvent event) {
        if (event.isCancelled())
            return;
        Entity damagee = event.getEntity();
        Entity damager = event.getDamager();
        if (MMOClass.ifSameFaction(damager,damagee))
            return;
        //System.out.println("DAMAGEE INSTANCE OF LIVINGENTITY, " + (damagee instanceof LivingEntity));
        if (damager instanceof Player && MainClass.classObjectMap.containsKey(damager.getUniqueId()) && MainClass.classObjectMap.get(((Player) damager).getUniqueId()).getClassName().equalsIgnoreCase("warrior") && damagee instanceof LivingEntity && !(damagee instanceof ArmorStand)) {
            Player player = (Player) damager;
            if(!MMOClassListener.canDamage(player, event.getCause()))
            {
                event.setCancelled(true);
                return;
            }
            LivingEntity le = (LivingEntity) damagee;
            MMOClass mmoClass = MainClass.classObjectMap.get(player.getUniqueId());
            int level = mmoClass.getLevel();

            if (!event.getCause().equals(EntityDamageEvent.DamageCause.CUSTOM))
            {
                if (MainClass.playerCooldownMap.get(player.getUniqueId()).get("Sunder").isBeforeNow()) {
                    int templvl = level;
                    if (templvl > 24) {
                        templvl = 24;
                    }
                    PotionEffect potionEffect = new PotionEffect(PotionEffectType.SLOW, (templvl / 5) * 20, 99);
                    le.addPotionEffect(potionEffect);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                        CircleParticle particle = new CircleParticle((LivingEntity) damagee,1.2);
                        particle.runTaskTimer(plugin,0,1);
                    },10);
                    DateTime dateTime = new DateTime();
                    dateTime = dateTime.plusSeconds(MainClass.sunderCooldown);
                    MainClass.playerCooldownMap.get(player.getUniqueId()).put("Sunder", dateTime);
                    //BukkitTask task = new MainClass.StartCooldown(MainClass.playerCooldownMap.get(player.getUniqueId()),"Sunder", player).runTaskTimer(plugin,0,20);
                    //MainClass.addToTaskMap(player, task);

                    player.sendMessage(MainClass.getPluginPrefix() + ChatColor.GOLD + "SUNDER" + ChatColor.GREEN + " yeteneği kullanıldı!");


                    Location temploc = le.getLocation();

                    ((Player) damager).playSound(temploc, Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
                    if(damagee instanceof Player)
                        ((Player) damagee).playSound(temploc, Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
                }

                if (level > 49) {
                    //System.out.println("LEVEL IS: " + level);
                    int templvl = level;
                    if (templvl > 49) {
                        templvl = 49;
                    }
                    templvl -= 25;
                    double damage = event.getDamage();
                    if (player.getInventory().getItemInMainHand().getType().equals(Material.WOODEN_AXE) || player.getInventory().getItemInMainHand().getType().equals(Material.STONE_AXE) || player.getInventory().getItemInMainHand().getType().equals(Material.IRON_AXE) || player.getInventory().getItemInMainHand().getType().equals(Material.GOLDEN_AXE) || player.getInventory().getItemInMainHand().getType().equals(Material.DIAMOND_AXE) || player.getInventory().getItemInMainHand().getType().equals(Material.NETHERITE_AXE)) {
                        double damage2 = event.getDamage();
                        damage2 = (damage2 * 20 / 100);
                        player.sendMessage(MainClass.getPluginPrefix() + "Baltayla saldırdın extra, " + ChatColor.GREEN + "" + String.format("%.2f", damage2) + ChatColor.WHITE + " hasar verildi ");
                        event.setDamage(event.getDamage() + damage2);
                    }
                    int extra = (templvl / 5) * 1;
                    damage = (damage * (12 + extra) / 100);
                    player.sendMessage(MainClass.getPluginPrefix() + "Bloody Rage, " + ChatColor.GREEN + "" + String.format("%.2f", damage) + ChatColor.WHITE + " gerçek hasar vurdu! ");
                    EntityDamageByEntityEvent newEvent = new EntityDamageByEntityEvent(damager, damagee, EntityDamageEvent.DamageCause.CUSTOM, damage);
                    Bukkit.getServer().getPluginManager().callEvent(newEvent);
                    //le.damage(damage);
                    /*try
                    {
                        le.setHealth(le.getHealth() - damage);
                    }
                    catch (IllegalArgumentException e)
                    {
                        le.setHealth(0);
                    }*/
                    Random rand = new Random();
                    int randomNumber = rand.nextInt(100);
                    //System.out.println("Rolled number was: " + randomNumber);
                    if (randomNumber < 10) {
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.WHITE + "Wither effecti uygulandı.");
                        PotionEffect potionEffect = new PotionEffect(PotionEffectType.WITHER, 5 * 20, 2);
                        le.addPotionEffect(potionEffect);
                    }
                }
                if (level > 99) {
                    if (!(le instanceof CraftEntity && ((CraftEntity)le).getHandle() instanceof RaidBoss)) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                double damageeHealthPercentage = le.getHealth() / le.getMaxHealth();
                                if (damageeHealthPercentage < 0.15) {
                                    le.damage(le.getMaxHealth());
                                    event.setCancelled(true);
                                    damager.sendMessage(MainClass.getPluginPrefix() + ChatColor.DARK_AQUA + le.getName() + ChatColor.WHITE + " executelandı.");
                                    Warrior warrior = (Warrior) MainClass.classObjectMap.get(damager.getUniqueId());
                                    if(warrior.getMoveSpeedID() != null)
                                    {
                                        if(!warrior.getMoveSpeedID().isCancelled())
                                            warrior.getMoveSpeedID().cancel();
                                        warrior.setMoveSpeedID(null);
                                    }
                                    ((Player) damager).setWalkSpeed(0.1f*5.5f);
                                    BukkitTask task = new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            ((Player) damager).setWalkSpeed(0.2f);
                                        }
                                    }.runTaskLater(plugin,10*20);
                                    warrior.setMoveSpeedID(task);
                                }
                            }
                        }, 0);
                    }
                }
            }
            else {
                if(event.isCancelled())
                    return;
                double damage = event.getDamage();
                if(!(le instanceof Player))
                    damage = damage*3;
                if(le.getAbsorptionAmount() > 0)
                {
                   if(le.getAbsorptionAmount()-damage > 0)
                   {
                       le.setAbsorptionAmount(le.getAbsorptionAmount()-damage);
                       le.playEffect(EntityEffect.HURT);
                       return;
                   }
                   else
                   {
                       damage = damage-le.getAbsorptionAmount();
                       le.setAbsorptionAmount(0);
                   }
                }
                if(le.getHealth()-damage > 0)
                    le.setHealth(le.getHealth()-damage);
                else
                {
                    le.setKiller((Player) damager);
                    le.setLastDamageCause(new EntityDamageByEntityEvent(damager,damagee, EntityDamageEvent.DamageCause.ENTITY_ATTACK,damage));
                    le.setHealth(0.0);
                    //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Whirlwind killed an entity...");
                }
                le.playEffect(EntityEffect.HURT);
            }
        }
    }


    @EventHandler
    public void onWarriorDamagedEvent(EntityDamageEvent event) {
        if(event.isCancelled())
            return;
        Entity damagee = event.getEntity();
        if (damagee instanceof Player && MainClass.classObjectMap.containsKey(damagee.getUniqueId()) && MainClass.classObjectMap.get(damagee.getUniqueId()).getClassName().equalsIgnoreCase("warrior") && MainClass.classObjectMap.get(damagee.getUniqueId()).getLevel() > 74) {
            Player p = (Player)damagee;
            if(p.getHealth() / p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() %10 == 0)
                adjustWarriorAttackSpeed(damagee);
        }
    }

    @EventHandler
    public void onWarriorRegainHealth(EntityRegainHealthEvent event) {
        if(event.isCancelled())
            return;
        Entity who = event.getEntity();
        if (who instanceof Player && MainClass.classObjectMap.containsKey(who.getUniqueId()) && MainClass.classObjectMap.get(who.getUniqueId()).getClassName().equalsIgnoreCase("warrior") && MainClass.classObjectMap.get(who.getUniqueId()).getLevel() > 74) {
            Player p = (Player)who;
            if(p.getHealth() / p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() %10 == 0)
                adjustWarriorAttackSpeed(who);
        }
    }

    @SuppressWarnings("deprecation")
    public static void adjustWarriorAttackSpeed(Entity who) {
        MMOClass mmoClass = MainClass.classObjectMap.get(who.getUniqueId());
        int level = mmoClass.getLevel();
        if (level > 74) {
            Player player = (Player) who;
            //System.out.println("WARRIORS HEALTH CHANGED, MAX HEALTH OF WARRIOR: " + player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            //System.out.println("MAX HEALTH OF WARRIOR AGAIN: " + player.getMaxHealth());
            double newAttackSpeed = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getDefaultValue();
            double currentHealthPercentage = (player.getHealth() / player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            //System.out.println("CURRENT HEALTH PERCENTAGE: " + currentHealthPercentage);
            newAttackSpeed = newAttackSpeed * (100 + (-25 * currentHealthPercentage + 25)) / 100;
            //System.out.println("NEW ATTACK SPEED:  " + newAttackSpeed);
            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(newAttackSpeed);
        }
    }

    @EventHandler
    public void onInventoryDragEvent(InventoryDragEvent event)
    {
        if(event.isCancelled())
            return;

        Player p = (Player) event.getWhoClicked();
        event.getInventory().getType();
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()) && MainClass.classObjectMap.get(p.getUniqueId()).getClassName().equalsIgnoreCase("warrior"))
        {
           /*if(event.getOldCursor() != null && event.getOldCursor().getType().equals(Material.SHIELD))
            {
                event.setCancelled(true);
            }
            else if(event.getCursor() != null && event.getCursor().getType().equals(Material.SHIELD))
            {
                event.setCancelled(true);
            }*/

            /*if(event.getInventory().getType().equals(InventoryType.PLAYER))
            {
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Inside warrior inventory drag event for player");
                for(int index : event.getNewItems().keySet())
                {
                    if (index == 40)
                        event.setCancelled(true);
                }
            }*/
            if(event.getInventory().getType().equals(InventoryType.CRAFTING))
            {
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Inside warrior inventory drag event for crafting");
                for(int index : event.getNewItems().keySet())
                {
                    //plugin.getServer().getConsoleSender().sendMessage("index is: " + index);
                    if (index == 45)
                        event.setCancelled(true);
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWarriorInventoryChangeEvent(InventoryClickEvent event) {
        if(event.isCancelled()) //TODO: TÜM RIGHT VE SHIFT CLİCKLER BURDA İPTAL EDİLİYOR!!!!
            return;
        if(event.getClickedInventory() == null)
            return;

        if (event.getWhoClicked().getGameMode().equals(GameMode.SURVIVAL) && MainClass.classObjectMap.containsKey(event.getWhoClicked().getUniqueId()) && MainClass.classObjectMap.get(event.getWhoClicked().getUniqueId()).getClassName().equalsIgnoreCase("warrior")) {
            Warrior warrior = (Warrior) MainClass.classObjectMap.get(event.getWhoClicked().getUniqueId());
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Clicked inv is: " + event.getClickedInventory());
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Inv is: " + event.getInventory());
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "****************************************");
            if(!event.getClickedInventory().getType().equals(InventoryType.PLAYER)) //This is where we cancel warrior from being able to pickup shields from chests.
            {
                if(event.getCurrentItem() != null && event.getCurrentItem().getType().equals(Material.SHIELD))
                {
                    event.setCancelled(true);
                    return;
                }
            }
            if(!event.getInventory().getType().equals(InventoryType.CRAFTING))
                return;
            if(event.isShiftClick())
            {
                event.setCancelled(true);
                return;
            }
            ItemStack itemToBeChanged = event.getWhoClicked().getItemOnCursor();
            ItemStack oldItem = event.getCurrentItem();
            if(itemToBeChanged.getType().equals(Material.SHIELD) && (event.getSlotType().equals(InventoryType.SlotType.QUICKBAR)))
            {
                event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Warrior'lar kalkan kullanamaz.");
                event.setCancelled(true);
                return;
            }
            if (event.getSlot() == 40) {
                if(itemToBeChanged.getType().equals(Material.SHIELD))
                {
                    event.setCancelled(true);
                    return;
                }
                if (warrior.getLevel() > 75) {
                    List<String> lore = new ArrayList<>();
                    int damageToAdd = 0;
                    switch (itemToBeChanged.getType()) {
                        case WOODEN_AXE:
                        case STONE_SWORD:
                            damageToAdd = (1 + itemToBeChanged.getEnchantmentLevel(Enchantment.DAMAGE_ALL)) / 2;
                            break;
                        case WOODEN_SWORD:
                            damageToAdd = (itemToBeChanged.getEnchantmentLevel(Enchantment.DAMAGE_ALL)) / 2;
                            break;
                        case STONE_AXE:
                        case GOLDEN_SWORD:
                            damageToAdd = (2 + itemToBeChanged.getEnchantmentLevel(Enchantment.DAMAGE_ALL)) / 2;
                            break;
                        case IRON_AXE:
                        case DIAMOND_SWORD:
                            damageToAdd = (4 + itemToBeChanged.getEnchantmentLevel(Enchantment.DAMAGE_ALL)) / 2;
                            break;
                        case IRON_SWORD:
                        case GOLDEN_AXE:
                            damageToAdd = (3 + itemToBeChanged.getEnchantmentLevel(Enchantment.DAMAGE_ALL)) / 2;
                            break;
                        case DIAMOND_AXE:
                        case NETHERITE_SWORD:
                            damageToAdd = (5 + itemToBeChanged.getEnchantmentLevel(Enchantment.DAMAGE_ALL)) / 2;
                            break;
                        case NETHERITE_AXE:
                            damageToAdd = (6 + itemToBeChanged.getEnchantmentLevel(Enchantment.DAMAGE_ALL)) / 2;
                            break;
                        default:
                            if(oldItem != null && oldItem.getItemMeta() != null)
                            {
                                ItemMeta eskiItemMeta = oldItem.getItemMeta();
                                lore = eskiItemMeta.getLore();
                                if (lore != null)
                                {
                                    int index = 0;
                                    for(int i = 0; i < lore.size(); i++)
                                    {
                                        String s = lore.get(i);
                                        if(s.contains("Ana ele aktarılan güç"))
                                        {
                                            index = i;
                                            break;
                                        }
                                    }
                                    if(index != 0)
                                    {
                                        lore.remove(index);
                                        if ((index-1) > 0 && lore.get(index-1).equalsIgnoreCase(""))
                                        {
                                            lore.remove(index-1);
                                        }
                                    }
                                }
                                String oldDisplayName = eskiItemMeta.getDisplayName();
                                String flavorText = "[ANCESTORS STRENGTH]";
                                if(oldDisplayName.contains(flavorText))
                                {
                                    int index = oldDisplayName.indexOf(flavorText);
                                    oldDisplayName = oldDisplayName.substring(index + flavorText.length()+1);
                                }
                                eskiItemMeta.setLore(lore);
                                eskiItemMeta.setDisplayName(oldDisplayName);
                                eskiItemMeta.removeAttributeModifier(EquipmentSlot.OFF_HAND);
                                oldItem.setItemMeta(eskiItemMeta);
                            }
                            event.setCancelled(true);
                            event.getWhoClicked().setItemOnCursor(oldItem);
                            event.getWhoClicked().getInventory().setItemInOffHand(itemToBeChanged);
                            return;
                    }
                    itemToBeChanged = Warrior.addAncestorsStrength(itemToBeChanged,damageToAdd, plugin);
                    event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Ancestors Strength" + ChatColor.WHITE + " skill'i gücünüze güç kattı...");
                    event.setCancelled(true);
                    if(oldItem != null && oldItem.getItemMeta() != null)
                    {
                        ItemMeta eskiItemMeta = oldItem.getItemMeta();
                        lore = eskiItemMeta.getLore();
                        if (lore != null)
                        {
                            int index = 0;
                            for(int i = 0; i < lore.size(); i++)
                            {
                                String s = lore.get(i);
                                if(s.contains("Ana ele aktarılan güç"))
                                {
                                    index = i;
                                    break;
                                }
                            }
                            lore.remove(index);
                            if ((index-1) > 0 && lore.get(index-1).equalsIgnoreCase(""))
                            {
                                lore.remove(index-1);
                            }
                        }
                        String oldDisplayName = eskiItemMeta.getDisplayName();
                        String flavorText = "[ANCESTORS STRENGTH]";
                        if(oldDisplayName.contains(flavorText))
                        {
                            int index = oldDisplayName.indexOf(flavorText);
                            oldDisplayName = oldDisplayName.substring(index + flavorText.length()+1);
                        }
                        eskiItemMeta.setLore(lore);
                        eskiItemMeta.setDisplayName(oldDisplayName);
                        eskiItemMeta.removeAttributeModifier(EquipmentSlot.OFF_HAND);
                        oldItem.setItemMeta(eskiItemMeta);
                    }
                    event.getWhoClicked().setItemOnCursor(oldItem);
                    event.getWhoClicked().getInventory().setItemInOffHand(itemToBeChanged);
                }
            }
        }
    }

    @EventHandler
    public void onWarriorDeathEvent(PlayerDeathEvent event)
    {
        if(event.isCancelled())
            return;
        Player who = event.getEntity();
        if(MainClass.classObjectMap.containsKey(who.getUniqueId()) && MainClass.classObjectMap.get(who.getUniqueId()).getClassName().equalsIgnoreCase("warrior"))
        {
            double defaultValue = who.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getBaseValue();
            who.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(defaultValue);
        }
    }

    @EventHandler
    public void swapMainHandEvent(PlayerSwapHandItemsEvent event)
    {
        if (event.isCancelled())
            return;
        Player p = event.getPlayer();
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()) && MainClass.classObjectMap.get(p.getUniqueId()).getClassName().equalsIgnoreCase("warrior"))
        {
            if(event.getOffHandItem().getType().equals(Material.SHIELD))
            {
                p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Warrior'lar kalkan kullanamaz!");
                event.setCancelled(true);
                //p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler
    public void changeMainHandEvent(PlayerChangedMainHandEvent event)
    {
        Player p = event.getPlayer();
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()) && MainClass.classObjectMap.get(p.getUniqueId()).getClassName().equalsIgnoreCase("warrior"))
        {
            if(p.getInventory().getItemInMainHand().getType().equals(Material.SHIELD))
            {
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Warrior tried equipping a shield.");
                for(int index = 9; index < 36; index ++)
                {
                    if(p.getInventory().getItem(index) == null)
                    {
                        p.getInventory().setItem(index, p.getInventory().getItemInMainHand());
                    }
                    else
                    {
                        plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Item was not null, " + p.getInventory().getItem(index));
                    }
                }
                p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Warrior'lar kalkan kullanamaz!");
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "********************************************");
                p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            }
        }
    }


    @EventHandler
    public void ItemHeldEvent(PlayerItemHeldEvent event)
    {
        if (event.isCancelled())
            return;
        Player p = event.getPlayer();
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()) && MainClass.classObjectMap.get(p.getUniqueId()).getClassName().equalsIgnoreCase("warrior"))
        {
            int i = event.getNewSlot();
            if (p.getInventory().getItem(i) != null && p.getInventory().getItem(i).getType().equals(Material.SHIELD))
            {
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Warrior tried equipping a shield.");
                boolean foundEmptySlot = false;
                for(int index = 9; index < 36; index ++)
                {
                    if(p.getInventory().getItem(index) == null)
                    {
                        p.getInventory().setItem(index, p.getInventory().getItem(i));
                        foundEmptySlot = true;
                        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Found empty slot at: " + index);
                        break;
                    }
                    else
                    {
                        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Item was not null, " + p.getInventory().getItem(index));
                    }
                }
                if(!foundEmptySlot)
                {
                    p.getLocation().getWorld().dropItemNaturally(p.getLocation(),p.getInventory().getItem(i));
                }
                p.getInventory().setItem(i, new ItemStack(Material.AIR));
                p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Warrior'lar kalkan kullanamaz.");
            }
        }
    }


    @EventHandler
    public void pickupEvent(PlayerAttemptPickupItemEvent event)
    {
        if (event.isCancelled())
            return;

        Player p = event.getPlayer();
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()) && MainClass.classObjectMap.get(p.getUniqueId()).getClassName().equalsIgnoreCase("warrior"))
        {
            if(event.getItem().getItemStack().getType().equals(Material.SHIELD))
            {
                boolean foundEmptySlot = false;
                for(int index = 9; index < 36; index ++)
                {
                    if(p.getInventory().getItem(index) == null)
                    {
                        p.getInventory().setItem(index, event.getItem().getItemStack());
                        event.getItem().remove();
                        event.setCancelled(true);
                        foundEmptySlot = true;
                        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Found empty slot at: " + index);
                        break;
                    }
                    else
                    {
                        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Item was not null, " + p.getInventory().getItem(index));
                    }
                }
                if(!foundEmptySlot)
                {
                    p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Envanterinizde boş yer yok. (Alttaki 9 yer sayılmamaktadır.)");
                    event.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void SikimiYe(BlockDispenseArmorEvent event) //TODO: DISPENSERLERIN ARMOR İŞİ BURDA KAPANIYOR!
    {

        if (event.isCancelled())
            return;

        if(event.getItem().getType().equals(Material.SHIELD))
        {
            event.setCancelled(true);
            return;
        }
    }


    @EventHandler
    public void SikimiYe2(PlayerArmorStandManipulateEvent event) //TODO ARMOR STAND'LERIN SHIELD İŞİ BURDA KAPANIYOR.
    {
        if (event.isCancelled())
            return;

        Player p = event.getPlayer();
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()) && MainClass.classObjectMap.get(p.getUniqueId()).getClassName().equalsIgnoreCase("warrior"))
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

    @EventHandler
    public void WarriorRightClickEvent(PlayerInteractEvent event)
    {

        Player player = event.getPlayer();
        if(MainClass.classObjectMap.containsKey(player.getUniqueId()) && MainClass.classObjectMap.get(player.getUniqueId()).getClassName().equalsIgnoreCase("warrior"))
        {
            if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            {
                if(player.getInventory().getItemInMainHand().getType().equals(Material.SHIELD))
                {
                    ItemStack shield = player.getInventory().getItemInMainHand();
                    boolean foundEmptySlot = false;
                    for(int index = 9; index < 36; index ++)
                    {
                        if(player.getInventory().getItem(index) == null)
                        {
                            player.getInventory().setItem(index, shield);
                            foundEmptySlot = true;
                            break;
                        }
                        else
                        {
                            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Item was not null, " + p.getInventory().getItem(index));
                        }
                    }
                    if(!foundEmptySlot)
                    {
                        player.getLocation().getWorld().dropItemNaturally(player.getLocation(),shield);
                    }
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Warriorlar kalkan kullanamaz.");
                }
            }
            return;
        }
    }
}
