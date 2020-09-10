package minecraft.mmoplugin;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import minecraft.mmoplugin.customItems.CommonItems;
import minecraft.mmoplugin.customItems.CustomEnchants;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.util.*;

public class MMOClassListener implements Listener {
    Plugin plugin;

    MMOClassListener(Plugin plugin)
    {
        this.plugin = plugin;
    }

    public static boolean insideFactionTerritory(Player p)
    {
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()))
        {
            //We know that this person has selected a class and has a faction.
            //Get his faction name.
            //Plugin pl = MainClass.getPlugin(MainClass.class);
            MMOClass mmoClass = MainClass.classObjectMap.get(p.getUniqueId());
            Faction faction = null;
            if(MainClass.classObjectMap.get(p.getUniqueId()).getFaction() != null)
                faction  = MainClass.factionMap.get(mmoClass.getFaction().toLowerCase());
            //pl.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Faction is: " + faction.getFactionName());
            /*if(mmoClass.getRank().equals(Faction.Ranks.ÜYE)) //ÜYE ranks are not allowed to build or break. This up to debate.
                return false;*/
            Chunk chunk = p.getChunk(); //Get his current chunk.
            //Now check if the current chunk the player is in belongs to his faction.
            FileConfiguration config = Faction.claimConfig.getConfig();
            //pl.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Chunk is: " + chunk);
            if(config.contains(p.getWorld().getName() + "."+chunk))
            {
                //pl.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "The config contains the chunk.");
                String chunkOwnerFaction = config.getString(p.getWorld().getName()+"."+chunk+".faction");
                //pl.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "ChunkOwnerFaction is: " + chunkOwnerFaction);
                if(chunkOwnerFaction == null)
                    return true;
                if(faction != null && chunkOwnerFaction.equalsIgnoreCase(faction.getFactionName()))
                    return true;
            }
            else
            {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void MMOClassDamageAnotherMMOClass(EntityDamageByEntityEvent event)
    {
        if(event.isCancelled())
            return;
        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();
        if(MMOClass.ifSameFaction(damager,damagee))
        {
            event.setCancelled(true);
        }
        else if(MainClass.classObjectMap.containsKey(damager.getUniqueId()))
        {
            LivingEntity le = (LivingEntity)damager;
            short durability = 0;
            le.getEquipment().getItemInMainHand().setDurability(durability);

            Player damagerPlayer = (Player)damager;
            if(damagerPlayer.getInventory().getItemInMainHand().getItemMeta() == null)
                return;
            if(damagerPlayer.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains("Kızıl Öfke"))
            {
                if(!MainClass.classObjectMap.get(damager.getUniqueId()).getClassName().equalsIgnoreCase("warrior"))
                    return;
                double currentHealth =  damagerPlayer.getHealth();
                double maxHealth = damagerPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double toBeHealed = event.getDamage()*0.25;
                if(currentHealth + toBeHealed < maxHealth)
                    ((Player) damager).setHealth(currentHealth+toBeHealed);
                else
                    ((Player) damager).setHealth(maxHealth);
            }
            else if(((Player) damager).getInventory().getItemInMainHand().getItemMeta().hasEnchant(CustomEnchants.THUNDERLORDS))
            {
                if(MainClass.playerCooldownMap.containsKey(damager.getUniqueId()) && MainClass.playerCooldownMap.get(damager.getUniqueId()).containsKey("thunderlords") && MainClass.playerCooldownMap.get(damager.getUniqueId()).get("thunderlords").isAfterNow())
                    return;
                damagee.getWorld().strikeLightning(damagee.getLocation());
                MainClass.playerCooldownMap.get(damager.getUniqueId()).put("thunderlords",new DateTime().plusSeconds(MainClass.thunderlordsCooldown));
            }
        }
    }

    @EventHandler
    public void MMOClassDeathEvent(PlayerDeathEvent event)
    {
        Player damagee = event.getEntity();
        if(damagee.getKiller() != null)
        {
            Player damager = damagee.getKiller();
            if(MMOClass.currentlyAtWar(damager,damagee))
            {
                MMOClass damageeClass = MainClass.classObjectMap.get(damagee.getUniqueId());
                MMOClass damagerClass = MainClass.classObjectMap.get(damager.getUniqueId());

                String gainingFaction = MainClass.classObjectMap.get(damager.getUniqueId()).getFaction();
                String losingFaction = MainClass.classObjectMap.get(damagee.getUniqueId()).getFaction();

                Faction winFaction = MainClass.factionMap.get(gainingFaction.toLowerCase());
                Faction loseFaction = MainClass.factionMap.get(losingFaction.toLowerCase());
                if(winFaction.getPower() + 2 <= winFaction.getSize()*15)
                {
                    MainClass.factionMap.get(gainingFaction.toLowerCase()).increasePower(2);
                    winFaction.sendMessageToMembers(MainClass.getPluginPrefix() + ChatColor.BLUE + damager.getName() + ", " + ChatColor.DARK_PURPLE + "["+damageeClass.getFaction()+"]" + ChatColor.RED + damagee.getName() + ChatColor.WHITE + " adlı oyuncuyu öldürdüğü için, 2 power kazandınız.");
                }
                else
                {
                    winFaction.sendMessageToMembers(MainClass.getPluginPrefix() + ChatColor.RED + "Daha fazla faction power kazanmazsınız!");
                }
                if(loseFaction.getPower()-5 >= 0)
                {
                    MainClass.factionMap.get(losingFaction.toLowerCase()).decreasePower(5);
                    loseFaction.sendMessageToMembers(MainClass.getPluginPrefix() + ChatColor.DARK_PURPLE + "["+damagerClass.getFaction()+"]" + ChatColor.RED + damager.getName() + ", " + ChatColor.BLUE  + damagee.getName() + ChatColor.WHITE + " adlı oyuncuyu öldürdüğü için, 5 power kaybettiniz.");
                }
                else
                {
                    loseFaction.sendMessageToMembers(MainClass.getPluginPrefix() + ChatColor.RED + "Daha fazla faction power kaybedemezsiniz!");

                    loseFaction.sendMessageToMembers(MainClass.getPluginPrefix() + ChatColor.RED + "Powerınız daha düşemeyeceği için, "+ ChatColor.BLUE + gainingFaction + ChatColor.RED + " adlı loncasıyla olan savaşınız otomatik olarak bitmiştir.");
                    winFaction.sendMessageToMembers(MainClass.getPluginPrefix() + ChatColor.BLUE + losingFaction + ChatColor.RED + "adlı lonca daha fazla power kaybedemeyeceğinden dolayı savaşınız otomatik olarak bitmiştir.");
                    winFaction.removeFromEnemy(loseFaction);
                    loseFaction.removeFromEnemy(winFaction);

                    winFaction.addCooldown(loseFaction);
                    loseFaction.addCooldown(winFaction);
                }

            }
        }

        if(damagee.getWorld().getName().contains("dungeon_"))
            return;

        if(damagee.getWorld().getName().contains("openworld_"))
            return;

        if(damagee.getWorld().getName().equalsIgnoreCase("world"))
            return;

        Random rand = new Random();
        int roll = rand.nextInt(damagee.getInventory().getSize());
        Inventory inventory = damagee.getInventory();
        ItemStack toBeLost = inventory.getItem(roll);
        HashMap<Integer, String> alreadyTried = new HashMap<>();
        alreadyTried.put(roll,"");
        int count = 1;
        while((toBeLost == null || toBeLost.getType().equals(Material.AIR)) && count < inventory.getSize())
        {
            roll = rand.nextInt(damagee.getInventory().getSize());
            if(alreadyTried.containsKey(roll))
                continue;
            toBeLost = inventory.getItem(roll);
            alreadyTried.put(roll,"");
            count++;
        }

        if(toBeLost == null)
            return;

        if(toBeLost.getType().equals(Material.SHIELD))
            return;
        if(toBeLost.getType().equals(Material.ENCHANTED_BOOK))
            return;



        if(roll == 40)
            damagee.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        if(roll == 39)
            damagee.getInventory().setHelmet(new ItemStack(Material.AIR));
        if(roll == 38)
            damagee.getInventory().setChestplate(new ItemStack(Material.AIR));
        if(roll == 37)
            damagee.getInventory().setLeggings(new ItemStack(Material.AIR));
        if(roll == 36)
            damagee.getInventory().setBoots(new ItemStack(Material.AIR));
        else
            damagee.getInventory().remove(toBeLost);
        ItemMeta eskiItemMeta = toBeLost.getItemMeta();

        if (eskiItemMeta != null)
        {
            if(eskiItemMeta.getDisplayName().contains("[ANCESTORS STRENGTH]"))
            {
                List<String> lore = eskiItemMeta.getLore();
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
                String flavorText = "[ANCESTORS STRENGTH]";
                String oldDisplayName = toBeLost.getItemMeta().getDisplayName();
                int index = oldDisplayName.indexOf(flavorText);
                oldDisplayName = oldDisplayName.substring(index + flavorText.length()+1);
                eskiItemMeta.setLore(lore);
                eskiItemMeta.setDisplayName(oldDisplayName);
                eskiItemMeta.removeAttributeModifier(EquipmentSlot.OFF_HAND);
                toBeLost.setItemMeta(eskiItemMeta);
            }
            else if(eskiItemMeta.getDisplayName().contains("[GÜÇLENDİRİLMİŞ]"))
            {
                List<String> lore = eskiItemMeta.getLore();
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
                    lore.remove(index);
                    lore.remove(index-1);
                    lore.remove(index-2);
                    lore.remove(index-3);
                    lore.remove(index-4);
                }
                String oldDisplayName = eskiItemMeta.getDisplayName();
                String flavorText = "[GÜÇLENDİRİLMİŞ]";
                int index = oldDisplayName.indexOf(flavorText);
                oldDisplayName = oldDisplayName.substring(index + flavorText.length()+1);
                eskiItemMeta.setLore(lore);
                eskiItemMeta.setDisplayName(oldDisplayName);
                eskiItemMeta.removeAttributeModifier(EquipmentSlot.OFF_HAND);
                toBeLost.setItemMeta(eskiItemMeta);
            }
        }


        ItemStack finalToBeLost = toBeLost;
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
            damagee.getWorld().dropItemNaturally(damagee.getLocation().add(0,0,0), finalToBeLost);
            damagee.sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + finalToBeLost.getItemMeta().getDisplayName() + ChatColor.RED + " adlı eşyanızı kaybettiniz!");
        },15);
    }

    @EventHandler
    public void CraftEvent(CraftItemEvent event)
    {
        Recipe result = event.getRecipe();
        ItemStack item = result.getResult();
        Material type = item.getType();
        if(type.equals(Material.ENCHANTED_BOOK))
        {
            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu eşyayı craftlayamazsınız.");
            event.setCancelled(true);
        }
        else if(type.equals(Material.ANVIL))
        {
            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu eşyayı craftlayamazsınız.");
            event.setCancelled(true);
        }
        else if(type.equals(Material.ENCHANTING_TABLE))
        {
            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu eşyayı craftlayamazsınız.");
            event.setCancelled(true);
        }
        else if(type.equals(Material.SHIELD))
        {
            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu eşyayı craftlayamazsınız.");
            event.setCancelled(true);
        }
        else
        {
            String typeString = type.toString();
            if(typeString.contains("SWORD") || typeString.contains("PICKAXE") || typeString.contains("HOE") ||  typeString.contains("AXE") ||  typeString.contains("SHOVEL") || typeString.contains("HELMET") ||  typeString.contains("CHESTPLATE") ||  typeString.contains("LEGGINGS") ||  typeString.contains("BOOTS"))
            {
                event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bu eşyayı craftlayamazsınız.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void playerBlockPlaceEvent(BlockPlaceEvent event)
    {
        Player p = event.getPlayer();

        if(p.isOp())
            return;

        if(p.getWorld().getName().equals("world_nether"))
            return;

        if(p.getWorld().getName().equals("world_the_end"))
            return;

        if (p.getWorld().getName().contains("faction_"))
        {
            Location loc = p.getLocation();
            if(loc.getX() < 192 && loc.getX() > -289 && loc.getZ() < 8 && loc.getZ() > -393)
            {
                p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Spawn bölgesine block koyamazsın.");
                event.setCancelled(true);
                return;
            }
            if (insideFactionTerritory(p))
                return;
            p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Başka bir faction'ın toprağını değiştiremezsiniz.");
            event.setCancelled(true);
        }
        else
        {
            if(p.getName().equalsIgnoreCase("RedFear") || p.getName().equalsIgnoreCase("Yoshiane") || p.isOp())
                return;
            event.setCancelled(true);
            p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Block koymanız yasaktır.");
        }
    }

    @EventHandler
    public void playerPortalEvent(EntityPortalEnterEvent event)
    {
        if(event.getEntity() instanceof Player)
        {
            event.getEntity().teleport(Bukkit.getWorld("world").getSpawnLocation());
        }
    }

    @EventHandler
    public void playerBlockBreakEvent(BlockBreakEvent event)
    {
        Player p = event.getPlayer();
        p.getInventory().getItemInMainHand().setDurability((short)0);

        if(p.isOp())
            return;

        if(p.getWorld().getName().equals("world_nether"))
            return;

        if(p.getWorld().getName().equals("world_the_end"))
            return;

        if (p.getWorld().getName().contains("faction_"))
        {
            Location loc = p.getLocation();
            if(loc.getX() < 192 && loc.getX() > -289 && loc.getZ() < 8 && loc.getZ() > -393)
            {
                p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Spawn bölgesinde block kıramazsın.");
                event.setCancelled(true);
                return;
            }
            if(insideFactionTerritory(p))
            {
                Block block = event.getBlock();
                event.setDropItems(false);
                Collection<ItemStack> drops = block.getDrops(p.getInventory().getItemInMainHand());
                for(ItemStack d : drops)
                {
                    try {
                        block.getWorld().dropItemNaturally(block.getLocation(),CommonItems.createCommonItem(d.getType(),d.getI18NDisplayName(),false,0,0,false,0));
                    } catch (Exception e) {
                        if(d.getItemMeta() != null)
                            block.getWorld().dropItemNaturally(block.getLocation(),CommonItems.createCommonItem(d.getType(),d.getItemMeta().getDisplayName(),false,0,0,false,0));
                        else
                            block.getWorld().dropItemNaturally(block.getLocation(),CommonItems.createCommonItem(d.getType(),"",false,0,0,false,0));
                        //e.printStackTrace();
                    }
                }
                event.setExpToDrop(0);
                return;
            }
            p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Başka bir faction'ın toprağını kıramazsınız.");
            event.setCancelled(true);
        }
        else
        {
            if(p.getName().equalsIgnoreCase("RedFear") || p.getName().equalsIgnoreCase("Yoshiane") || p.isOp())
                return;
            event.setCancelled(true);
            p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Block kırmanız yasaktır.");
        }
    }

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent event)
    {
        Player p = event.getPlayer();
        if(!insideFactionTerritory(p))
        {
            event.setCancelled(true);
            //p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Başka bir loncanın toprağında bunu yapamazsınız.");
        }
    }

    @EventHandler
    public void armorEquipEvent(PlayerArmorChangeEvent event)
    {
        ItemStack newItem = event.getNewItem();
        if(newItem == null || newItem.getType().equals(Material.AIR))
            return;

        ItemStack oldItem = event.getOldItem();

        Player p = event.getPlayer();
        PlayerArmorChangeEvent.SlotType slot = event.getSlotType();
        if(!MainClass.classObjectMap.containsKey(p.getUniqueId()))
        {
            p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Zırh değiştirmek için önce sınıf seçmiş olmanız gerekir.");
            switch (slot)
            {
                case HEAD:
                    p.getInventory().addItem(newItem);
                    p.getInventory().setHelmet(oldItem);
                    break;
                case CHEST:
                    p.getInventory().addItem(newItem);
                    p.getInventory().setChestplate(oldItem);
                    break;
                case LEGS:
                    p.getInventory().addItem(newItem);
                    p.getInventory().setLeggings(oldItem);
                    break;
                case FEET:
                    p.getInventory().addItem(newItem);
                    p.getInventory().setBoots(oldItem);
                    break;
            }
            p.setItemOnCursor(null);
            p.updateInventory();
            return;
        }


        MMOClass mmoClass = MainClass.classObjectMap.get(p.getUniqueId());

        if(newItem.getType().toString().contains("NETHERITE"))
        {
            if(mmoClass.getLevel() < 80)
            {
                p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Netherite Zırh kullanabilmek için en az 80 seviye olmanız gerekir.");
                switch (slot)
                {
                    case HEAD:
                        p.getInventory().addItem(newItem);
                        p.getInventory().setHelmet(oldItem);
                        break;
                    case CHEST:
                        p.getInventory().addItem(newItem);
                        p.getInventory().setChestplate(oldItem);
                        break;
                    case LEGS:
                        p.getInventory().addItem(newItem);
                        p.getInventory().setLeggings(oldItem);
                        break;
                    case FEET:
                        p.getInventory().addItem(newItem);
                        p.getInventory().setBoots(oldItem);
                        break;
                }
                p.setItemOnCursor(null);
                p.updateInventory();
            }
        }
        else if(newItem.getType().toString().contains("DIAMOND"))
        {
            if(mmoClass.getLevel() < 40)
            {
                p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Diamond Zırh kullanabilmek için en az 40 seviye olmanız gerekir.");
                switch (slot)
                {
                    case HEAD:
                        p.getInventory().addItem(newItem);
                        p.getInventory().setHelmet(oldItem);
                        break;
                    case CHEST:
                        p.getInventory().addItem(newItem);
                        p.getInventory().setChestplate(oldItem);
                        break;
                    case LEGS:
                        p.getInventory().addItem(newItem);
                        p.getInventory().setLeggings(oldItem);
                        break;
                    case FEET:
                        p.getInventory().addItem(newItem);
                        p.getInventory().setBoots(oldItem);
                        break;
                }
                p.setItemOnCursor(null);
                p.updateInventory();
            }
        }

    }

    public static boolean canDamage(Player p, EntityDamageEvent.DamageCause cause)
    {
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()))
        {
            MMOClass mmoClass = MainClass.classObjectMap.get(p.getUniqueId());
            int level = mmoClass.getLevel();

            if(cause.equals(EntityDamageEvent.DamageCause.CUSTOM))
                return true;

            ItemStack itemInHand = p.getInventory().getItemInMainHand();
            if(itemInHand.getType().equals(Material.AIR))
                return true;

            if(itemInHand.getType().toString().contains("DIAMOND"))
            {
                if(level < 40)
                {
                    p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Diamond bir ekipman kullanmabilmek için en az 40 seviye olmanız gerekmektedir.");
                    return false;
                }
            }

            else if(itemInHand.getType().toString().contains("NETHERITE"))
            {
                if(level < 80)
                {
                    p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Netherite bir ekipman kullanmabilmek için en az 80 seviye olmanız gerekmektedir.");
                    return false;
                }
            }
        }
        else
        {
            return false;
        }
        return true;
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent event)
    {
        Player p = (Player) event.getPlayer();
        if(!p.getLocation().getWorld().getName().equalsIgnoreCase("world"))
            return;

        Location from = event.getFrom();
        Location to = event.getTo();

        if((int)to.getX() != -2498 || !(to.getZ() < 843.7 && to.getZ() > 839.2))
            return; //Player is not moving to the portal.

        if((int)from.getX() == -2498 && from.getZ() > 839.2 && from.getZ() < 843.7)
            return; //Player is already in the portal


        if(p.getOpenInventory().getTitle().contains("Isınlanma Menusu"))
        {
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Menu was already open...");
            return;
        }

        CustomInventory.createTeleportUI(p,plugin);
    }

    @EventHandler
    public void onPortalFinishTeleportEvent(PlayerPortalEvent event)
    {
        if(!event.getPlayer().getLocation().getWorld().getName().equalsIgnoreCase("world"))
            return;

        event.setCancelled(true); //This is so that we can prevent players from portaling to nether from the mainhub. But rather we will show a menu for the players.
    }

    @EventHandler
    public void onMMOClassClickEvent(PlayerInteractEvent event)
    {
        Player p = event.getPlayer();
        if(!MainClass.classObjectMap.containsKey(p.getUniqueId()))
            return;
        if(p.getInventory().getItemInMainHand().getItemMeta() == null)
            return;
        MMOClass mmoClass = MainClass.classObjectMap.get(p.getUniqueId());
        if(p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains("Engin Tutku") && mmoClass instanceof Necromancer)
        {
            if(!event.getAction().equals(Action.RIGHT_CLICK_AIR))
                return;

            if(MainClass.playerCooldownMap.containsKey(p.getUniqueId()) && MainClass.playerCooldownMap.get(p.getUniqueId()).containsKey("engintutku") && MainClass.playerCooldownMap.get(p.getUniqueId()).get("engintutku").isAfterNow())
            {
                DateTime dt = MainClass.playerCooldownMap.get(p.getUniqueId()).get("engintutku");
                Seconds seconds = Seconds.secondsBetween(new DateTime(),dt);
                p.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Blink'i kullanmak için, " + ChatColor.GOLD + seconds.getSeconds() + "saniye " + ChatColor.RED + " beklemelisin.");
                return;
            }
            Vector normalized = p.getLocation().getDirection().normalize();
            normalized.multiply(10);
            p.teleport(p.getLocation().add(normalized));
            p.playSound(p.getLocation(),Sound.ENTITY_ENDERMAN_TELEPORT,1.0f,1.0f);
            MainClass.playerCooldownMap.get(p.getUniqueId()).put("engintutku",new DateTime().plusSeconds(MainClass.blinkCooldown));
        }
    }



}
