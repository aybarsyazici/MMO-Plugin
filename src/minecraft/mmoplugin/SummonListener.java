package minecraft.mmoplugin;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class SummonListener implements Listener
{
    Plugin plugin;

    SummonListener(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSummonGetDamaged(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof CraftEntity)
        {
            if(((CraftEntity)entity).getHandle() instanceof Necromancer.Summon)
            {
                Necromancer.Summon summon = (Necromancer.Summon) ((CraftEntity)entity).getHandle();
                if (event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING)
                {
                    entity.setFireTicks(0);
                    event.setCancelled(true);
                }
                else if(event instanceof EntityDamageByEntityEvent)
                {
                    EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent)event;
                    Entity damager = event2.getDamager();
                    if(!(damager instanceof Player))
                    {
                        double dmg = event.getDamage();
                        event.setDamage(dmg*0.5);
                    }
                    else
                    {
                        if(entity.getWorld().getName().equals("openworld_emnia"))
                        {
                            event.setDamage(0);
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByLivingEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        Entity entity2 = event.getEntity();
        if(entity instanceof CraftEntity && ((CraftEntity)entity).getHandle() instanceof Necromancer.Summon)
        {
            Necromancer.Summon summon = (Necromancer.Summon) ((CraftEntity)entity).getHandle();
            if(entity2 instanceof Player && ((Player) entity2).getUniqueId().equals(summon.getOwner().getUniqueId()))
            {
                event.setCancelled(true);
                return;
            }
            else if(entity2 instanceof LivingEntity)
            {
                PotionEffect potionEffect;
                MMOClass mmoClass = MainClass.classObjectMap.get(summon.getOwner().getUniqueId());
                int level = mmoClass.getLevel();
                if(level > 24)
                {
                    potionEffect = new PotionEffect(PotionEffectType.SPEED,30,1);
                    summon.getOwner().addPotionEffect(potionEffect);
                }
                if(level > 49)
                {
                    potionEffect = new PotionEffect(PotionEffectType.SLOW,30,0);
                    ((LivingEntity) entity2).addPotionEffect(potionEffect);
                    potionEffect = new PotionEffect(PotionEffectType.POISON,30,1);
                    ((LivingEntity) entity2).addPotionEffect(potionEffect);
                }
                //this.getLogger().info("LIGHTNING STRIKE");
                //entity2.getWorld().strikeLightning(entity2.getLocation());
            }
        }
        else if(entity instanceof Player && entity2 instanceof CraftEntity && ((CraftEntity)entity2).getHandle() instanceof Necromancer.Summon)
        {
            Player player = (Player) entity;
            Necromancer.Summon summon = (Necromancer.Summon) ((CraftEntity)entity2).getHandle();
            if(summon.getOwner().getUniqueId().equals(player.getUniqueId()))
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onSummonInventoryCloseEvent(InventoryCloseEvent event)
    {
        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "You have closed the inventory, " + event.getInventory());
        if(event.getInventory().getHolder() instanceof SummonInventory.SummonCustomInventoryHolder)
        {
            Player player = (Player) event.getPlayer();
            Necromancer necromancer = (Necromancer) MainClass.classObjectMap.get(player.getUniqueId());
            Necromancer.Summon summon = necromancer.getSummon();
            LivingEntity le = null;
            if(summon.getRidingSummon() != null)
            {
                le = (LivingEntity) necromancer.getSummon().getRidingSummon().getNormalEntity();
                summon = summon.getRidingSummon();
            }
            else
            {
                le = (LivingEntity) necromancer.getSummon().getNormalEntity();
            }
            List<ItemStack> itemsToBeEquipped = new ArrayList<>();
            boolean helmetEquipped = false;
            boolean chestEquipped = false;
            boolean legginsEquipped = false;
            boolean bootsEquipped = false;
            boolean mainHandEquipped = false;
            for(int i = 11; i < 16; i++)
            {
                itemsToBeEquipped.add(event.getInventory().getItem(i));
                if(event.getInventory().getItem(i) != null)
                {
                    switch (event.getInventory().getItem(i).getType())
                    {
                        case TURTLE_HELMET:
                        case LEATHER_HELMET:
                        case IRON_HELMET:
                        case GOLDEN_HELMET:
                        case DIAMOND_HELMET:
                        case NETHERITE_HELMET: {
                            le.getEquipment().setHelmet(event.getInventory().getItem(i));
                            helmetEquipped = true;
                            break;
                        }
                        case LEATHER_CHESTPLATE:
                        case IRON_CHESTPLATE:
                        case GOLDEN_CHESTPLATE:
                        case DIAMOND_CHESTPLATE:
                        case NETHERITE_CHESTPLATE: {
                            le.getEquipment().setChestplate(event.getInventory().getItem(i));
                            chestEquipped = true;
                            break;
                        }
                        case LEATHER_LEGGINGS:
                        case IRON_LEGGINGS:
                        case GOLDEN_LEGGINGS:
                        case DIAMOND_LEGGINGS:
                        case NETHERITE_LEGGINGS: {
                            le.getEquipment().setLeggings(event.getInventory().getItem(i));
                            legginsEquipped = true;
                            break;
                        }
                        case LEATHER_BOOTS:
                        case IRON_BOOTS:
                        case GOLDEN_BOOTS:
                        case DIAMOND_BOOTS:
                        case NETHERITE_BOOTS: {
                            le.getEquipment().setBoots(event.getInventory().getItem(i));
                            bootsEquipped = true;
                            break;
                        }
                        case WOODEN_SWORD:
                        case WOODEN_AXE:
                        case WOODEN_HOE:
                        case WOODEN_PICKAXE:
                        case WOODEN_SHOVEL:
                        case NETHERITE_SWORD:
                        case NETHERITE_AXE:
                        case NETHERITE_HOE:
                        case NETHERITE_PICKAXE:
                        case NETHERITE_SHOVEL:
                        case DIAMOND_SWORD:
                        case DIAMOND_AXE:
                        case DIAMOND_HOE:
                        case DIAMOND_PICKAXE:
                        case DIAMOND_SHOVEL:
                        case IRON_SWORD:
                        case IRON_AXE:
                        case IRON_HOE:
                        case IRON_PICKAXE:
                        case IRON_SHOVEL:
                        case GOLDEN_SWORD:
                        case GOLDEN_AXE:
                        case GOLDEN_HOE:
                        case GOLDEN_PICKAXE:
                        case GOLDEN_SHOVEL: {
                            le.getEquipment().setItemInMainHand(event.getInventory().getItem(i));
                            mainHandEquipped = true;
                            break;
                        }
                    }
                    //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Item type is, " +  event.getInventory().getItem(i).getType());
                    //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "***************************************************************");
                }
            }

            if(!mainHandEquipped)
            {
                le.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
            }
            if(!helmetEquipped)
            {
                le.getEquipment().setHelmet(new ItemStack(Material.AIR));
            }
            if(!chestEquipped)
            {
                le.getEquipment().setChestplate(new ItemStack(Material.AIR));
            }
            if(!legginsEquipped)
            {
                le.getEquipment().setLeggings(new ItemStack(Material.AIR));
            }
            if(!bootsEquipped)
            {
                le.getEquipment().setBoots(new ItemStack(Material.AIR));
            }


            summon.setItems(itemsToBeEquipped);


            Necromancer.mobConfig.getConfig().set(player.getUniqueId()+".MainHand",le.getEquipment().getItemInMainHand());
            Necromancer.mobConfig.getConfig().set(player.getUniqueId()+".Helmet",le.getEquipment().getHelmet());
            Necromancer.mobConfig.getConfig().set(player.getUniqueId()+".Chestplate",le.getEquipment().getChestplate());
            Necromancer.mobConfig.getConfig().set(player.getUniqueId()+".Leggings",le.getEquipment().getLeggings());
            Necromancer.mobConfig.getConfig().set(player.getUniqueId()+".Boots",le.getEquipment().getBoots());

            Necromancer.mobConfig.saveConfig();

            /*le.getEquipment().setItemInMainHand(mainHand);
            le.getEquipment().setItemInMainHand(helmet);
            le.getEquipment().setItemInMainHand(chest);
            le.getEquipment().setItemInMainHand(leggings);
            le.getEquipment().setItemInMainHand(boots);*/

        }
    }

    @EventHandler
    public void onSummonDropItemEvent(EntityDropItemEvent event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof CraftEntity && ((CraftEntity)entity).getHandle() instanceof Necromancer.Summon)
        {
            //todo
        }
    }

    @EventHandler
    public void SummonDeathEvent(EntityDeathEvent event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof CraftEntity && ((CraftEntity)entity).getHandle() instanceof Necromancer.Summon)
        {
            event.getDrops().clear();
            Necromancer.Summon summon = (Necromancer.Summon) ((CraftEntity) entity).getHandle();
            MMOClass mmoClass = MainClass.classObjectMap.get(summon.getOwner().getUniqueId());
            Necromancer necro = (Necromancer)mmoClass;
            if(entity instanceof SkeletonHorse)
            {
                //We get here when a player kills the Horse and not the wither skeleton.
                necro.setSummon(summon.getRidingSummon());
                return;
            }
            else if(entity instanceof WitherSkeleton && necro.getSummon().getNormalEntity() instanceof CraftEntity && ((CraftEntity)necro.getSummon().getNormalEntity()).getHandle() instanceof Necromancer.CustomSkeletonHorse)
            {
                //We get in here when a player kills the Wither Skeleton that is on the horse.
                necro.getSummon().kill();
            }
            necro.setSummon(null);
            MainClass.classObjectMap.put(summon.getOwner().getUniqueId(), necro);
            DateTime dateTime = new DateTime();
            dateTime = dateTime.plusSeconds(MainClass.summonCooldown);
            MainClass.playerCooldownMap.get(summon.getOwner().getUniqueId()).put("Summon", dateTime);
        }
    }
}
