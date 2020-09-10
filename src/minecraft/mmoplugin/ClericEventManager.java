package minecraft.mmoplugin;

import com.google.gson.internal.$Gson$Types;
import minecraft.mmoplugin.customItems.CustomItems;
import net.minecraft.server.v1_16_R1.NBTTagCompound;
import net.minecraft.server.v1_16_R1.NBTTagList;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import particles.LeashParticle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClericEventManager implements Listener
{

    Plugin plugin;

    ClericEventManager(Plugin plugin)
    {
        this.plugin = plugin;
    }
    /*
    @EventHandler
    public void onClericRegenHealth(EntityRegainHealthEvent event)
    {
        Entity who = event.getEntity();
        if (who instanceof Player && MainClass.classObjectMap.containsKey(who.getUniqueId()) && MainClass.classObjectMap.get(who.getUniqueId()).getClassName().equalsIgnoreCase("cleric"))
        {
            //Now we know that the entity that is regaining health is a cleric, so we regen more health depending on his level.
            MMOClass mmoClass = MainClass.classObjectMap.get(who.getUniqueId());
            int level = mmoClass.getLevel();
            Player player = (Player)who;
            plugin.getLogger().info("Player's current health is, " + player.getHealth());
            plugin.getLogger().info("Player's max health is, " + player.getAttribute(Attribute.GENERIC_MAX_HEALTH));
        }
    }
    */

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onClericGetDamagedEvent(EntityDamageEvent event)
    {
        if(event.isCancelled())
            return;
        if(event instanceof EntityDamageByEntityEvent)
        {
            if(MMOClass.ifSameFaction(((EntityDamageByEntityEvent) event).getDamager(),event.getEntity()))
                return;
        }
        Entity who = event.getEntity();
        if (who instanceof Player && MainClass.classObjectMap.containsKey(who.getUniqueId()) && MainClass.classObjectMap.get(who.getUniqueId()).getClassName().equalsIgnoreCase("cleric"))
        {
            //Now we know that the entity that is getting damaged is a cleric, so we apply a regen potion.
            MMOClass mmoClass = MainClass.classObjectMap.get(who.getUniqueId());
            Player player = (Player) who;
            Cleric cleric = (Cleric) mmoClass;
             int level = mmoClass.getLevel();
            int tempLevel = level;
            if(cleric.isDeathDefied())
            {
                event.setCancelled(true);
                return;
            }
            if(level > 99 && (event.getCause().equals(EntityDamageEvent.DamageCause.FIRE) || event.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK)))
            {
                who.setFireTicks(0);
                event.setCancelled(true);
                return;
            }
            if(level > 24)
            {
                tempLevel = 25;
            }
            PotionEffect potionEffect = new PotionEffect(PotionEffectType.REGENERATION,4*20,0);
            player.addPotionEffect(potionEffect);
            if(level > 74)
            {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        double currentHealth = player.getHealth();
                        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                        if(currentHealth/maxHealth < 0.4)
                        {
                            PotionEffect temp;
                            temp = new PotionEffect(PotionEffectType.SPEED, 10*20, 2);
                            player.addPotionEffect(temp);
                            temp = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 10*20, 2);
                            player.addPotionEffect(temp);
                        }
                    }
                },0);
            }
            if(level > 99)
            {
                potionEffect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,5*20, 0);
                player.addPotionEffect(potionEffect);

                double currentHealth = player.getHealth();
                double damage = event.getDamage();
                double damageReducedByArmor = event.getDamage(EntityDamageEvent.DamageModifier.ARMOR);
                double damageReducedByAbsorption = event.getDamage(EntityDamageEvent.DamageModifier.ABSORPTION);
                double damageReducedByBlocking = event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING);
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "damage that was being delt was, " + damage);
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "damage reduced by Armor, " + damageReducedByArmor);
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "damage reduced by Absorption, " + damageReducedByAbsorption);
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "damage reduced by Blocking, " + damageReducedByBlocking);
                damage = damage + (damageReducedByArmor + damageReducedByAbsorption + damageReducedByBlocking);
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Total reduced damage, " + damage);
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "*********************************************************");
                if((currentHealth - damage <= 0)  && !cleric.isDeathDefied())
                {
                    potionEffect = new PotionEffect(PotionEffectType.SPEED,7*20,1);
                    player.addPotionEffect(potionEffect);
                    event.setCancelled(true);
                    player.setHealth(100);
                    player.sendMessage(MainClass.getPluginPrefix() + "Tanrılar sizi 7 saniye daha hayatta tutacak.");
                    cleric.setDeathDefied(true);
                    DeathDefy defy = new DeathDefy(cleric, player);
                    BukkitTask task = defy.runTaskTimer(plugin,0,20);
                }
            }
        }
    }

    public void onClericDeathEvent(PlayerDeathEvent event)
    {
        Player p = event.getEntity();
        if(MainClass.classObjectMap.get(p.getUniqueId()).getClassName().equalsIgnoreCase("cleric"))
        {
            Cleric cleric = (Cleric)MainClass.classObjectMap.get(p.getUniqueId());
            if(cleric.isDeathDefied())
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClericItemChangeEvent(InventoryClickEvent event)
    {
        if(event.isCancelled())
            return;
        if(event.getClickedInventory() == null)
            return;
        if(!event.getInventory().getType().equals(InventoryType.CRAFTING))
            return;
        Entity who = event.getWhoClicked();
        if (who instanceof Player && ((Player) who).getGameMode().equals(GameMode.SURVIVAL) &&MainClass.classObjectMap.containsKey(who.getUniqueId()) && MainClass.classObjectMap.get(who.getUniqueId()).getClassName().equalsIgnoreCase("cleric"))
        {
            Player player = (Player) who;
            MMOClass mmoClass = MainClass.classObjectMap.get(who.getUniqueId());
            int level = mmoClass.getLevel();
            /*if(level > 25) //TODO OLD GOLD ARMOR BONUSES.
            {
                if(event.getSlotType().equals(InventoryType.SlotType.ARMOR))
                {
                    if(event.getClick().isShiftClick() || event.getClick().equals(ClickType.DOUBLE_CLICK))
                    {
                        event.setCancelled(true);
                        return;
                    }
                    //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "The clicked slot is " + event.getSlot());
                    ItemStack itemStack = player.getItemOnCursor();
                    ItemStack eskiItem = event.getCurrentItem();
                    List<String> lore = new ArrayList<>();

                    switch(itemStack.getType())
                        {
                        case GOLDEN_HELMET:
                            addCustomization(itemStack,lore,player,eskiItem, "head", "Kask",4 );
                            break;
                        case GOLDEN_CHESTPLATE:
                            addCustomization(itemStack,lore,player,eskiItem, "chest", "Göğüslük", 10);
                            break;
                        case GOLDEN_LEGGINGS:
                            addCustomization(itemStack,lore,player,eskiItem, "legs", "Pantalon", 6);
                            break;
                        case GOLDEN_BOOTS:
                            addCustomization(itemStack,lore,player,eskiItem,"feet", "Bot", 2);
                            break;
                        default:
                            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "You tried to equip a"+ ChatColor.RED + " NON " + ChatColor.WHITE +"golden item!");
                            switch (event.getSlot())
                            {
                                case 39:
                                    player.getInventory().setHelmet(itemStack);
                                    removeCustomization(eskiItem, lore, EquipmentSlot.HEAD);
                                    break;
                                case 38:
                                    player.getInventory().setChestplate(itemStack);
                                    removeCustomization(eskiItem, lore, EquipmentSlot.CHEST);
                                    break;
                                case 37:
                                    player.getInventory().setLeggings(itemStack);
                                    removeCustomization(eskiItem, lore, EquipmentSlot.LEGS);
                                    break;
                                case 36:
                                    player.getInventory().setBoots(itemStack);
                                    removeCustomization(eskiItem, lore, EquipmentSlot.FEET);
                                    break;
                                default:
                                    //Nothing.
                                    break;
                            }
                            player.setItemOnCursor(eskiItem);
                            break;
                        }
                    event.setCancelled(true);
                }
            }*/
            if(level > 49 && event.getSlot() == 40)
            {
                ItemStack itemStack = player.getItemOnCursor();
                ItemStack eskiItem = event.getCurrentItem();
                if(itemStack.getType().equals(Material.SHIELD))
                {
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS,new AttributeModifier(UUID.randomUUID(),"generic.armor_toughness",3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND));
                    itemMeta.addAttributeModifier(Attribute.GENERIC_ARMOR,new AttributeModifier(UUID.randomUUID(),"generic.armor",3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND));
                    itemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,new AttributeModifier(UUID.randomUUID(),"generic.attack_damage",-0.25, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND));

                    List<String> lore = itemMeta.getLore();
                    CustomItems.Enderlik rarity = CustomItems.Enderlik.SIRADAN;

                    if(lore == null)
                        lore = new ArrayList<>();
                    else
                    {
                        for(String s : lore)
                        {
                            if(s.contains("Enderlik : "))
                            {
                                String temp = s;
                                temp = ChatColor.stripColor(temp);
                                temp = temp.substring(11);
                                switch (temp)
                                {
                                    case "Sıradan":
                                        rarity = CustomItems.Enderlik.SIRADAN;
                                        break;
                                    case "Nadir":
                                        rarity = CustomItems.Enderlik.NADIR;
                                        break;
                                    case "Eşsiz":
                                        rarity = CustomItems.Enderlik.ESSIZ;
                                        break;
                                    case "Olağanüstü":
                                        rarity = CustomItems.Enderlik.OLAGANUSTU;
                                        break;
                                    case "Destansı":
                                        rarity = CustomItems.Enderlik.DESTANSI;
                                        break;
                                }
                                break;
                            }
                        }
                    }

                    lore.add("");
                    lore.add(ChatColor.DARK_PURPLE + "Ekstra Zırh: 3");
                    lore.add(ChatColor.DARK_PURPLE + "Ekstra Sertlik: 3");
                    lore.add(ChatColor.RED + "Saldırı Gücü: -25%");
                    lore.add(ChatColor.GOLD + "" + ChatColor.ITALIC + "LVL75 Talentınız kalkanınızı güçlendirdi");
                    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                    String oldName = itemMeta.getDisplayName();
                    oldName = ChatColor.translateAlternateColorCodes('&', "&6[GÜÇLENDİRİLMİŞ] " + CustomItems.rarityColor(rarity) +oldName);
                    itemMeta.setLore(lore);
                    itemMeta.setDisplayName(oldName);
                    itemStack.setItemMeta(itemMeta);
                    //REMOVE EFFECT FROM OLD ITEM HERE.
                    eskiItem = removeCustomization(eskiItem);
                    player.setItemOnCursor(eskiItem);
                    player.getInventory().setItemInOffHand(itemStack);
                    event.setCancelled(true);
                }
                else
                {
                    eskiItem = removeCustomization(eskiItem);
                    player.getInventory().setItemInOffHand(itemStack);
                    player.setItemOnCursor(eskiItem);
                    event.setCancelled(true);
                }
            }
        }
    }

    private ItemStack removeCustomization(ItemStack eskiItem) {
        if(eskiItem != null && eskiItem.getItemMeta() != null)
        {
            ItemMeta eskiItemMeta = eskiItem.getItemMeta();
            List<String> lore = eskiItemMeta.getLore();
            if(lore == null)
                return eskiItem;
            lore = eskiItemMeta.getLore();
            if (lore != null)
            {
                int index = 0;
                for(int i = 0; i < lore.size(); i++)
                {
                    String s = lore.get(i);
                    if(s.contains("LVL75 Talentınız kalkanınızı güçlendirdi"))
                    {
                        index = i;
                        break;
                    }
                }
                if(index == 0)
                    return eskiItem;
                lore.remove(index);
                lore.remove(index-1);
                lore.remove(index-2);
                lore.remove(index-3);
                lore.remove(index-4);
            }
            String oldDisplayName = eskiItemMeta.getDisplayName();
            String flavorText = "[GÜÇLENDİRİLMİŞ]";
            if(oldDisplayName.contains(flavorText))
            {
                int index = oldDisplayName.indexOf(flavorText);
                oldDisplayName = oldDisplayName.substring(index + flavorText.length()+1);
            }
            eskiItemMeta.setLore(lore);
            eskiItemMeta.setDisplayName(oldDisplayName);
            eskiItemMeta.removeAttributeModifier(EquipmentSlot.OFF_HAND);
            eskiItem.setItemMeta(eskiItemMeta);
            return eskiItem;
        }
        return new ItemStack(Material.AIR);
    }

    @EventHandler
    public void onClericDamageAnotherEntityEvent(EntityDamageByEntityEvent event) {
        if(event.isCancelled())
            return;
        Entity damagee = event.getEntity();
        Entity damager = event.getDamager();
        if(MMOClass.ifSameFaction(damager,damagee))
            return;
        if (damager instanceof Player && MainClass.classObjectMap.containsKey(damager.getUniqueId()) && MainClass.classObjectMap.get(((Player) damager).getUniqueId()).getClassName().equalsIgnoreCase("cleric") && damagee instanceof LivingEntity)
        {
            if(!MMOClassListener.canDamage((Player)damager, event.getCause()))
            {
                event.setCancelled(true);
                return;
            }
            MMOClass mmoClass = MainClass.classObjectMap.get(damager.getUniqueId());
            int level = mmoClass.getLevel();
            if(level > 24)
            {
                damagee.setFireTicks(30);
            }
            if(level > 50)
            {
                Map<Enchantment, Integer> enchants = ((Player) damager).getInventory().getItemInMainHand().getEnchantments();
                if(enchants.containsKey(Enchantment.DAMAGE_UNDEAD))
                {
                    int enchantMentLevel = enchants.get(Enchantment.DAMAGE_UNDEAD);
                    double damage = event.getDamage();
                    int tempLevel = level;
                    if(tempLevel > 74)
                    {
                        tempLevel = 75;
                    }
                    tempLevel -= 50;
                    int damageToAdd = (tempLevel/5);
                    damageToAdd *= 5;
                    damage = damage*((2*enchantMentLevel)+damageToAdd)/100;
                    damager.sendMessage(MainClass.getPluginPrefix() + "Seviye: " + level + " olduğundan dolayı " + ChatColor.RED + "Light's Bidding, " + ChatColor.WHITE + "%" + Integer.toString((2*enchantMentLevel)+damageToAdd) + " hasar vuruyor.");
                    damager.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Light’s Will, extra " +  ChatColor.GREEN + String.format("%.2f",damage) + ChatColor.RED + " hasar vurdu ! ");
                    event.setDamage(event.getDamage() + damage);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        //player.sendMessage("Your item in hand is " + player.getItemInHand());

        if(MainClass.classObjectMap.containsKey(player.getUniqueId()) && MainClass.classObjectMap.get(player.getUniqueId()).getClassName().equalsIgnoreCase("cleric"))
        {
            if((event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) // (player.getItemInHand().getType().equals(Material.WOODEN_SWORD) || player.getItemInHand().getType().equals(Material.STONE_SWORD) || player.getItemInHand().getType().equals(Material.NETHERITE_SWORD) || player.getItemInHand().getType().equals(Material.GOLDEN_SWORD) || player.getItemInHand().getType().equals(Material.DIAMOND_SWORD) || player.getItemInHand().getType().equals(Material.IRON_SWORD))
            {
                if (MMOClass.canUseSkills(player.getLocation().getWorld().getName()))
                {
                    MMOClass mmoClass = MainClass.classObjectMap.get(player.getUniqueId());
                    int level = mmoClass.getLevel();
                    if (level > 25)
                    {
                        if (MainClass.playerCooldownMap.get(player.getUniqueId()).get("Leash").isBeforeNow())
                        {
                            DateTime dateTime = new DateTime();
                            dateTime = dateTime.plusSeconds(MainClass.leashCooldown);
                            MainClass.playerCooldownMap.get(player.getUniqueId()).put("Leash", dateTime);
                            LeashParticle particle = new LeashParticle(player,10,plugin);
                            particle.runTaskTimer(plugin,0,1);
                        }
                        else
                        {
                            player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Holy Preparation yeteneğini kullanmak için, " + ChatColor.GOLD + (Seconds.secondsBetween(new DateTime(), MainClass.playerCooldownMap.get(player.getUniqueId()).get("Leash"))).getSeconds() + "saniye" + ChatColor.RED + " daha beklemelisin.");
                        }
                    }
                    else
                    {
                        //Do nothing.
                    }
                }
            }
            else if((event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && (player.getItemInHand().getType().equals(Material.BOW) || player.getItemInHand().getType().equals(Material.CROSSBOW)))
            {
                player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Cleric'ler ok kullanamaz");
                event.setCancelled(true);
            }
        }
    }

    public class DeathDefy extends BukkitRunnable
    {

        int time;
        Cleric cleric;
        Player player;
        int teamid;
        Team team;

        DeathDefy(Cleric cleric, Player player)
        {
            time = 0;
            this.cleric = cleric;
            this.player = player;
            teamid = MainClass.playerTeamMap.get(player.getUniqueId());
            team = MainClass.mainScoreboard.getTeam(Integer.toString(teamid));
            //team.setSuffix(ChatColor.RED + "" + ChatColor.BOLD + "[ÖLÜMSÜZ]");
            team.setPrefix(ChatColor.RED + "" + ChatColor.BOLD + "[ÖLÜMSÜZ]" + ChatColor.WHITE + "LVL " + Integer.toString(cleric.getLevel()) + " " + ChatColor.YELLOW + cleric.getClassName().toUpperCase() + " ");
        }

        @Override
        public void run() {
            if(!player.isOnline())
            {
                cancel();
            }
            time++;
            if(time == 7)
            {
                //team.setSuffix("");
                team.setPrefix(ChatColor.WHITE + "LVL " + Integer.toString(cleric.getLevel()) + " " + ChatColor.YELLOW + cleric.getClassName().toUpperCase() + " ");
                cleric.setDeathDefied(false);
                player.setHealth(0);
                player.sendMessage(MainClass.getPluginPrefix() + "Işığınız karanlığa teslim oldu.");
                Location loc = player.getLocation();
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Trying to create an explosion.");
                        List<Entity> tempList = (List<Entity>) loc.getWorld().getNearbyEntities(loc,15,15,15, this::isPlayer);
                        loc.getWorld().createExplosion(loc.getX(),loc.getY(),loc.getY(),7.0f,false,false);
                        loc.getWorld().strikeLightning(loc);
                        for(Entity e : tempList)
                        {
                            Location entitiesLoc = e.getLocation();
                            if(!MMOClass.ifSameFaction(cleric.getClassOwner(),e))
                            {
                                entitiesLoc.getWorld().strikeLightning(entitiesLoc);
                                entitiesLoc.getWorld().strikeLightning(entitiesLoc);
                                entitiesLoc.getWorld().strikeLightning(entitiesLoc);
                            }
                        }
                    }
                    private boolean isPlayer(Entity entity) {
                        return (entity instanceof Player);
                    }
                },0);
                cancel();
            }
        }
    }

}
