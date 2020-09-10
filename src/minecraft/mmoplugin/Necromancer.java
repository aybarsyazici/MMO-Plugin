package minecraft.mmoplugin;

import minecraft.mmoplugin.events.MobConfigManager;
import minecraft.mmoplugin.pathfindergoals.PathFinderGoalSummonFindTarget;
import minecraft.mmoplugin.pathfindergoals.PathFinderGoalTeleportToOwner;
import minecraft.mmoplugin.pathfindergoals.PathfinderGoalMeleeAttackSummon;
import minecraft.mmoplugin.pathfindergoals.PathfinderGoalSummon;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Necromancer extends MMOClass
{

    public static boolean canSpawnSummon(String worldName)
    {
        if(worldName.contains("dungeon_"))
            return true;
        else if(worldName.contains("faction_"))
            return true;
        else if(worldName.contains("openworld_"))
            return true;
        else if(worldName.equalsIgnoreCase("world_nether"))
            return true;
        else if(worldName.equalsIgnoreCase("world_the_end"))
            return true;
        else
            return false;
    }

    public static MobConfigManager mobConfig;

    private Summon summon;

    public Necromancer(Plugin plugin, Player player, double xp, int level, String className) {
        super(plugin, player, xp, level, className);
        summon = null;
    }

    public Summon getSummon() {
        return summon;
    }

    public void setSummon(Summon summon) {
        this.summon = summon;
    }

    public Necromancer(){}

    public class CustomZombie extends EntityZombie implements Necromancer.Summon{

        private Player owner;
        private MobStance.type stance;
        Plugin plugin;
        Entity target;
        PathFinderGoalSummonFindTarget findTarget;
        List<ItemStack> items;



        @Override
        public List<ItemStack> getItems() {
            return items;
        }

        @Override
        public void setItems(List<ItemStack> newItems) {
            this.items = newItems;
        }

        @Override
        public Player getOwner() {
            return owner;
        }

        public void setOwner(Player owner) {
            this.owner = owner;
        }

        public MobStance.type getStance() {
            return stance;
        }

        @Override
        public Entity getTarget()
        {
            return target;
        }

        @Override
        public void setTarget(Entity entity)
        {
            if(entity == null)
            {
                findTarget.setTarget(null);
                this.setGoalTarget(null);
                this.target = null;
                return;
            }
            this.target = entity;
            this.setGoalTarget((EntityLiving) ((CraftEntity)entity).getHandle(),entity instanceof Player ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY, true);
            findTarget.setTarget((EntityLiving) ((CraftEntity)entity).getHandle());
        }

        public void setStance(MobStance.type stance) {
            this.stance = stance;
            this.setTarget(null);
        }

        public void setPlugin(Plugin plugin) { this.plugin = plugin; }

        public Plugin getPlugin() { return plugin; }

        public CustomZombie(World world, Player player, Plugin plugin)
        {
            super(((CraftWorld)world).getHandle());
            this.owner = player;
            this.plugin = plugin;

            this.setCustomName(new ChatComponentText(ChatColor.DARK_AQUA + player.getName() + " Zombisi"));
            this.setCustomNameVisible(true);


            MMOClass mmoClass = MainClass.classObjectMap.get(owner.getUniqueId());
            int level = mmoClass.getLevel();

            double baseMS = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue();
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(baseMS+(double)level/350);

            double baseHealth = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue();
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(baseHealth + 1.5*(double)level);
            this.setHealth((float)(baseHealth + 3*level));


            double baseAttack = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue();
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(baseAttack+(double)level*2/5);


            ItemStack mainHand = null;
            ItemStack helmet= null;
            ItemStack chestplate= null;
            ItemStack leggings = null;
            ItemStack boots = null;

            items = new ArrayList<>();
            LivingEntity en = (LivingEntity) this.getBukkitEntity();
            if(!mobConfig.getConfig().contains(String.valueOf(owner.getUniqueId())))
            {
                mainHand = new ItemStack(Material.WOODEN_SWORD);
                helmet = new ItemStack(Material.LEATHER_HELMET);
                chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                leggings = new ItemStack(Material.LEATHER_LEGGINGS);
                boots = new ItemStack(Material.LEATHER_BOOTS);

                mobConfig.getConfig().set(owner.getUniqueId()+".MainHand",mainHand);
                mobConfig.getConfig().set(owner.getUniqueId()+".Helmet",helmet);
                mobConfig.getConfig().set(owner.getUniqueId()+".Chestplate",chestplate);
                mobConfig.getConfig().set(owner.getUniqueId()+".Leggings",leggings);
                mobConfig.getConfig().set(owner.getUniqueId()+".Boots",boots);

                mobConfig.saveConfig();
            }
            else
            {
                mainHand = (ItemStack) mobConfig.getConfig().get(owner.getUniqueId()+".MainHand");
                helmet = (ItemStack)mobConfig.getConfig().get(owner.getUniqueId()+".Helmet");
                chestplate = (ItemStack)mobConfig.getConfig().get(owner.getUniqueId()+".Chestplate");
                leggings = (ItemStack)mobConfig.getConfig().get(owner.getUniqueId()+".Leggings");
                boots = (ItemStack)mobConfig.getConfig().get(owner.getUniqueId()+".Boots");

            }
            en.getEquipment().setItemInMainHand(mainHand);
            en.getEquipment().setHelmet(helmet);
            en.getEquipment().setChestplate(chestplate);
            en.getEquipment().setLeggings(leggings);
            en.getEquipment().setBoots(boots);
            items.add(mainHand);
            items.add(helmet);
            items.add(chestplate);
            items.add(leggings);
            items.add(boots);
            this.target = null;
            this.stance = MobStance.type.PASSIVE;
            Location loc = player.getLocation();
            this.setLocation(loc.getX(),loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            //this.goalSelector.a(0, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,15,true,true,this::isNotOwner));
            this.getWorld().addEntity(this);
        }

        @Override
        public void initPathfinder()
        {
            this.goalSelector.a(0, new PathfinderGoalFloat(this));
            this.goalSelector.a(1, new PathFinderGoalTeleportToOwner(this,20,this));
            this.findTarget = new PathFinderGoalSummonFindTarget<EntityLiving>(this,EntityLiving.class,1,true,true,this::isNotOwner,this);
            this.targetSelector.a(2, findTarget);
            this.goalSelector.a(3, new PathfinderGoalMeleeAttackSummon(this,1.3D, true,this));
            this.goalSelector.a(4, new PathfinderGoalSummon(this,0.8D,15,this));

            //this.goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, 0.6D));
            //this.goalSelector.a(5, new PathfinderGoalMoveThroughVillage(this, 0.6D,false,1,null));
            //this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, 0.6D));
            //this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
            //this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0f));
        }

        @Override
        public void kill()
        {
            this.killEntity();
            ((Necromancer)MainClass.classObjectMap.get(owner.getUniqueId())).setSummon(null);
        }

        @Override
        public void teleportToPlayer()
        {
            Location loc = owner.getLocation();
            this.getBukkitEntity().teleport(loc);
            loc = null;
        }

        @Override
        public UUID getid() {
            return this.getUniqueID();
        }

        private boolean isNotOwner(EntityLiving e)
        {
            if(e.getBukkitEntity().getLocation().distance(this.getBukkitEntity().getLocation()) > 12)
                return false;
            if(e.getBukkitEntity() instanceof Player && e.getBukkitEntity().getWorld().getName().equals("openworld_emnia"))
                return false;
            if(e.getUniqueID().equals(owner.getUniqueId()))
            {
                return false;
            }
            if(getRidingSummon() != null && e.getUniqueID().equals(getRidingSummon().getNormalEntity().getUniqueId()))
                return false;
            if(MainClass.classObjectMap.containsKey(e.getUniqueID()))
            {
                if(ifSameFaction(owner, e.getBukkitEntity()))
                {
                    return false;
                }
            }
            if(e instanceof Summon)
            {
                Player otherSummonOwner = ((Summon)e).getOwner();
                if(ifSameFaction(owner, otherSummonOwner))
                {
                    return false;
                }
            }
            return true;
        }

        private boolean isOwner(EntityLiving e)
        {
            return e.getUniqueID().equals(owner.getUniqueId());
        }


        @Override
        public Entity getNormalEntity()
        {
            return this.getBukkitEntity();
        }

        @Override
        public PathFinderGoalSummonFindTarget getFindTarget() {
            return findTarget;
        }

        @Override
        public Summon getRidingSummon()
        {
            return null;
        }
    }

    public class CustomSkeleton extends EntitySkeleton implements Necromancer.Summon
    {

        private Player owner;
        private MobStance.type stance;
        Plugin plugin;
        Entity target;
        PathFinderGoalSummonFindTarget findTarget;
        List<ItemStack> items;

        public CustomSkeleton(org.bukkit.World world, Player player, Plugin plugin)
        {
            super(EntityTypes.SKELETON, ((CraftWorld)world).getHandle());
            this.owner = player;
            this.plugin = plugin;

            this.setCustomName(new ChatComponentText(ChatColor.DARK_AQUA + player.getName() + " İskeleti"));
            this.setCustomNameVisible(true);

            MMOClass mmoClass = MainClass.classObjectMap.get(owner.getUniqueId());
            int level = mmoClass.getLevel();

            double baseMS = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue();
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(baseMS+(double)level/400);

            double baseHealth = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue();
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(baseHealth + 1.5*(double)level);
            this.setHealth((float)(baseHealth + 3*level));


            double baseAttack = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue();
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(baseAttack+(double)level*2/5);



            ItemStack mainHand = null;
            ItemStack helmet= null;
            ItemStack chestplate= null;
            ItemStack leggings = null;
            ItemStack boots = null;

            items = new ArrayList<>();
            LivingEntity en = (LivingEntity) this.getBukkitEntity();
            if(!mobConfig.getConfig().contains(String.valueOf(owner.getUniqueId())))
            {
                mainHand = new ItemStack(Material.WOODEN_SWORD);
                helmet = new ItemStack(Material.LEATHER_HELMET);
                chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                leggings = new ItemStack(Material.LEATHER_LEGGINGS);
                boots = new ItemStack(Material.LEATHER_BOOTS);

                mobConfig.getConfig().set(owner.getUniqueId()+".MainHand",mainHand);
                mobConfig.getConfig().set(owner.getUniqueId()+".Helmet",helmet);
                mobConfig.getConfig().set(owner.getUniqueId()+".Chestplate",chestplate);
                mobConfig.getConfig().set(owner.getUniqueId()+".Leggings",leggings);
                mobConfig.getConfig().set(owner.getUniqueId()+".Boots",boots);

                mobConfig.saveConfig();
            }
            else
            {
                mainHand = (ItemStack) mobConfig.getConfig().get(owner.getUniqueId()+".MainHand");
                helmet = (ItemStack)mobConfig.getConfig().get(owner.getUniqueId()+".Helmet");
                chestplate = (ItemStack)mobConfig.getConfig().get(owner.getUniqueId()+".Chestplate");
                leggings = (ItemStack)mobConfig.getConfig().get(owner.getUniqueId()+".Leggings");
                boots = (ItemStack)mobConfig.getConfig().get(owner.getUniqueId()+".Boots");

            }
            en.getEquipment().setItemInMainHand(mainHand);
            en.getEquipment().setHelmet(helmet);
            en.getEquipment().setChestplate(chestplate);
            en.getEquipment().setLeggings(leggings);
            en.getEquipment().setBoots(boots);
            items.add(mainHand);
            items.add(helmet);
            items.add(chestplate);
            items.add(leggings);
            items.add(boots);
            this.target = null;
            this.stance = MobStance.type.PASSIVE;
            Location loc = player.getLocation();
            this.setLocation(loc.getX(),loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            //this.goalSelector.a(0, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,15,true,true,this::isNotOwner));
            this.getWorld().addEntity(this);
        }

        private boolean isNotOwner(EntityLiving e)
        {
            if(e.getBukkitEntity().getLocation().distance(this.getBukkitEntity().getLocation()) > 12)
                return false;
            if(e.getBukkitEntity() instanceof Player && e.getBukkitEntity().getWorld().getName().equals("openworld_emnia"))
                return false;
            if(e.getUniqueID().equals(owner.getUniqueId()))
            {
                return false;
            }
            if(getRidingSummon() != null && e.getUniqueID().equals(getRidingSummon().getNormalEntity().getUniqueId()))
                return false;
            if(MainClass.classObjectMap.containsKey(e.getUniqueID()))
            {
                if(ifSameFaction(owner, e.getBukkitEntity()))
                {
                    return false;
                }
            }
            if(e instanceof Summon)
            {
                Player otherSummonOwner = ((Summon)e).getOwner();
                if(ifSameFaction(owner, otherSummonOwner))
                {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void initPathfinder()
        {
            this.goalSelector.a(0, new PathfinderGoalFloat(this));
            this.goalSelector.a(1, new PathFinderGoalTeleportToOwner(this,20,this));
            this.findTarget = new PathFinderGoalSummonFindTarget<EntityLiving>(this,EntityLiving.class,1,true,true,this::isNotOwner,this);
            this.targetSelector.a(2, findTarget);
            //this.goalSelector.a(3, new PathfinderGoalArrowAttack(this,0.6D,1,40,15.0f));
            this.goalSelector.a(3, new PathfinderGoalMeleeAttackSummon(this,1.5D, true,this));
            this.goalSelector.a(4, new PathfinderGoalSummon(this,0.8D,15,this));
        }


        @Override
        public Player getOwner() {
            return owner;
        }


        private boolean isOwner(EntityLiving e)
        {
            return e.getUniqueID().equals(owner.getUniqueId());
        }

        @Override
        public void kill()
        {
            this.killEntity();
            ((Necromancer)MainClass.classObjectMap.get(owner.getUniqueId())).setSummon(null);
        }

        @Override
        public void teleportToPlayer()
        {
            Location loc = owner.getLocation();
            this.getBukkitEntity().teleport(loc);
            loc = null;
        }

        @Override
        public void setStance(MobStance.type mobStance) {
            this.stance = mobStance;
            this.setTarget(null);
        }

        @Override
        public MobStance.type getStance()
        {
            return stance;
        }

        @Override
        public Entity getTarget()
        {
            return target;
        }

        @Override
        public void setTarget(Entity entity)
        {
            if(entity == null)
            {
                findTarget.setTarget(null);
                this.setGoalTarget(null);
                this.target = null;
                return;
            }
            this.target = entity;
            this.setGoalTarget((EntityLiving) ((CraftEntity)entity).getHandle(),entity instanceof Player ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY, true);
            findTarget.setTarget((EntityLiving) ((CraftEntity)entity).getHandle());
        }

        @Override
        public Entity getNormalEntity()
        {
            return this.getBukkitEntity();
        }

        @Override
        public PathFinderGoalSummonFindTarget getFindTarget() {
            return findTarget;
        }

        @Override
        public UUID getid() {
            return this.getUniqueID();
        }

        @Override
        public List<ItemStack> getItems() {
            return items;
        }

        @Override
        public void setItems(List<ItemStack> newItems) {
            this.items = newItems;
        }

        @Override
        public Summon getRidingSummon()
        {
            return null;
        }
    }

    public class CustomWitherSkeleton extends EntitySkeletonWither implements Necromancer.Summon
    {

        private Player owner;
        private MobStance.type stance;
        Plugin plugin;
        Entity target;
        PathFinderGoalSummonFindTarget findTarget;
        List<ItemStack> items;
        Summon ridingSummon;

        public CustomWitherSkeleton(org.bukkit.World world, Player player, Plugin plugin)
        {
            super(EntityTypes.WITHER_SKELETON, ((CraftWorld)world).getHandle());
            this.owner = player;
            this.plugin = plugin;

            this.setCustomName(new ChatComponentText(ChatColor.DARK_AQUA + player.getName() + " İskeleti"));
            this.setCustomNameVisible(true);

            MMOClass mmoClass = MainClass.classObjectMap.get(owner.getUniqueId());
            int level = mmoClass.getLevel();

            double baseMS = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue();
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(baseMS+(double)level/400);

            double baseHealth = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue();
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(baseHealth + 0.75*(double)level);
            this.setHealth((float)(baseHealth + 3*level));

            this.ridingSummon = null;

            double baseAttack = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue();
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(baseAttack+(double)level*2/5);

            ItemStack mainHand = null;
            ItemStack helmet= null;
            ItemStack chestplate= null;
            ItemStack leggings = null;
            ItemStack boots = null;

            items = new ArrayList<>();
            LivingEntity en = (LivingEntity) this.getBukkitEntity();
            if(!mobConfig.getConfig().contains(String.valueOf(owner.getUniqueId())))
            {
                mainHand = new ItemStack(Material.WOODEN_SWORD);
                helmet = new ItemStack(Material.LEATHER_HELMET);
                chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                leggings = new ItemStack(Material.LEATHER_LEGGINGS);
                boots = new ItemStack(Material.LEATHER_BOOTS);

                mobConfig.getConfig().set(owner.getUniqueId()+".MainHand",mainHand);
                mobConfig.getConfig().set(owner.getUniqueId()+".Helmet",helmet);
                mobConfig.getConfig().set(owner.getUniqueId()+".Chestplate",chestplate);
                mobConfig.getConfig().set(owner.getUniqueId()+".Leggings",leggings);
                mobConfig.getConfig().set(owner.getUniqueId()+".Boots",boots);

                mobConfig.saveConfig();
            }
            else
            {
                mainHand = (ItemStack) mobConfig.getConfig().get(owner.getUniqueId()+".MainHand");
                //ItemMeta mainHandMeta = (ItemMeta) mobConfig.getConfig().get(owner.getUniqueId()+".MainHand.meta");
                //mainHand.setItemMeta(mainHandMeta);

                helmet = (ItemStack)mobConfig.getConfig().get(owner.getUniqueId()+".Helmet");
                //ItemMeta helmetMeta = (ItemMeta) mobConfig.getConfig().get(owner.getUniqueId()+".Helmet.meta");
                //helmet.setItemMeta(helmetMeta);

                chestplate = (ItemStack)mobConfig.getConfig().get(owner.getUniqueId()+".Chestplate");
                //ItemMeta chestMeta = (ItemMeta) mobConfig.getConfig().get(owner.getUniqueId()+".Chestplate.meta");
                //chestplate.setItemMeta(chestMeta);

                leggings = (ItemStack)mobConfig.getConfig().get(owner.getUniqueId()+".Leggings");
                //ItemMeta leggingsMeta = (ItemMeta) mobConfig.getConfig().get(owner.getUniqueId()+".Leggings.meta");
                //leggings.setItemMeta(leggingsMeta);

                boots = (ItemStack)mobConfig.getConfig().get(owner.getUniqueId()+".Boots");
                //ItemMeta bootsMeta = (ItemMeta) mobConfig.getConfig().get(owner.getUniqueId()+".Boots.meta");
                //boots.setItemMeta(bootsMeta);
            }

            if(mainHand != null && !mainHand.getType().equals(Material.AIR))
            {
                net.minecraft.server.v1_16_R1.ItemStack nmsMainHand = CraftItemStack.asNMSCopy(mainHand);
                nmsMainHand.getTag().setBoolean("Unbreakable", true);
                mainHand = CraftItemStack.asBukkitCopy(nmsMainHand);
            }

            if(helmet != null && !helmet.getType().equals(Material.AIR))
            {
                net.minecraft.server.v1_16_R1.ItemStack nmsHelmet = CraftItemStack.asNMSCopy(helmet);
                nmsHelmet.getTag().setBoolean("Unbreakable", true);
                helmet = CraftItemStack.asBukkitCopy(nmsHelmet);
            }

            if(chestplate != null && !chestplate.getType().equals(Material.AIR))
            {
                net.minecraft.server.v1_16_R1.ItemStack nmsChest = CraftItemStack.asNMSCopy(chestplate);
                nmsChest.getTag().setBoolean("Unbreakable", true);
                chestplate = CraftItemStack.asBukkitCopy(nmsChest);
            }

            if(leggings != null && !leggings.getType().equals(Material.AIR))
            {
                net.minecraft.server.v1_16_R1.ItemStack nmsLeggings = CraftItemStack.asNMSCopy(leggings);
                nmsLeggings.getTag().setBoolean("Unbreakable", true);
                leggings = CraftItemStack.asBukkitCopy(nmsLeggings);
            }

            if(boots != null && !boots.getType().equals(Material.AIR))
            {
                net.minecraft.server.v1_16_R1.ItemStack nmsBoots = CraftItemStack.asNMSCopy(boots);
                nmsBoots.getTag().setBoolean("Unbreakable", true);
                boots = CraftItemStack.asBukkitCopy(nmsBoots);
            }



            en.getEquipment().setItemInMainHand(mainHand);
            en.getEquipment().setHelmet(helmet);
            en.getEquipment().setChestplate(chestplate);
            en.getEquipment().setLeggings(leggings);
            en.getEquipment().setBoots(boots);
            items.add(mainHand);
            items.add(helmet);
            items.add(chestplate);
            items.add(leggings);
            items.add(boots);
            this.target = null;
            this.stance = MobStance.type.PASSIVE;
            Location loc = player.getLocation();
            this.setLocation(loc.getX(),loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            //this.goalSelector.a(0, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,15,true,true,this::isNotOwner));
            this.getWorld().addEntity(this);
        }



        @Override
        public boolean d(MobEffect mobeffect) {
            return false;
        }

        @Override
        public boolean attackEntity(net.minecraft.server.v1_16_R1.Entity entity) {
            if (!super.attackEntity(entity)) {
                return false;
            } else {
                return true;
            }
        }

        private boolean isNotOwner(EntityLiving e)
        {
            if(e.getBukkitEntity().getLocation().distance(this.getBukkitEntity().getLocation()) > 12)
                return false;
            if(e.getBukkitEntity() instanceof Player && e.getBukkitEntity().getWorld().getName().equals("openworld_emnia"))
                return false;
            if(e.getBukkitEntity() instanceof Player && e.getBukkitEntity().getWorld().getName().equals("openworld_emnia"))
                return false;
            if(e.getUniqueID().equals(owner.getUniqueId()))
            {
                return false;
            }
            if(getRidingSummon() != null && e.getUniqueID().equals(getRidingSummon().getNormalEntity().getUniqueId()))
                return false;
            if(MainClass.classObjectMap.containsKey(e.getUniqueID()))
            {
                if(ifSameFaction(owner, e.getBukkitEntity()))
                {
                    return false;
                }
            }
            if(e instanceof Summon)
            {
                Player otherSummonOwner = ((Summon)e).getOwner();
                if(ifSameFaction(owner, otherSummonOwner))
                {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void initPathfinder()
        {
            PathFinderGoalTeleportToOwner teleportToOwner = new PathFinderGoalTeleportToOwner(this,20,this);
            this.goalSelector.a(0, new PathfinderGoalFloat(this));
            this.goalSelector.a(1, teleportToOwner);
            //this.goalSelector.a(3, new PathfinderGoalArrowAttack(this,0.6D,1,40,15.0f));
            //this.goalSelector.a(2, new PathfinderGoalArrowAttack(this,0.01D,1,20,30.0F));
            this.goalSelector.a(2, new PathfinderGoalMeleeAttackSummon(this,1.2D, true,this));
            this.goalSelector.a(3, new PathfinderGoalSummon(this,1.4D,15,this));
            this.goalSelector.a(4, new PathfinderGoalLookAtPlayer(this,EntityPlayer.class,1.0f));

            this.findTarget = new PathFinderGoalSummonFindTarget<EntityLiving>(this,EntityLiving.class,1,true,true,this::isNotOwner,this);
            this.targetSelector.a(0, teleportToOwner);
            this.targetSelector.a(1, findTarget);
        }


        @Override
        protected void dropDeathLoot(DamageSource damagesource, int i, boolean flag)
        {
            //Nothing.
        }

        @Override
        public Player getOwner() {
            return owner;
        }

        @Override
        public void kill() {
            this.killEntity();
            ((Necromancer)MainClass.classObjectMap.get(owner.getUniqueId())).setSummon(null);
        }

        @Override
        public void teleportToPlayer() {
            Location loc = owner.getLocation();
            this.getBukkitEntity().teleport(loc);
            loc = null;
        }

        @Override
        public void setStance(MobStance.type mobStance) {
            this.stance = mobStance;
            this.setTarget(null);
        }

        @Override
        public MobStance.type getStance() {
            return stance;
        }

        @Override
        public Entity getTarget() {
            return target;
        }

        @Override
        public void setTarget(Entity entity) {
            if(entity == null)
            {
                findTarget.setTarget(null);
                this.setGoalTarget(null);
                this.target = null;
                return;
            }
            this.target = entity;
            this.setGoalTarget((EntityLiving) ((CraftEntity)entity).getHandle(),entity instanceof Player ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY, true);
            findTarget.setTarget((EntityLiving) ((CraftEntity)entity).getHandle());
        }

        @Override
        public Entity getNormalEntity() {
            return this.getBukkitEntity();
        }

        @Override
        public PathFinderGoalSummonFindTarget getFindTarget() {
            return findTarget;
        }

        @Override
        public UUID getid() {
            return this.getUniqueID();
        }

        @Override
        public List<ItemStack> getItems() {
            return items;
        }

        @Override
        public void setItems(List<ItemStack> newItems) {
            this.items = newItems;
        }

        @Override
        public Summon getRidingSummon()
        {
            return ridingSummon;
        }

        public void setRidingSummon(Summon summon)
        {
            this.ridingSummon = summon;
        }
    }

    public class CustomSkeletonHorse extends EntityHorseSkeleton implements Necromancer.Summon
    {

        private Player owner;
        private MobStance.type stance;
        Plugin plugin;
        Entity target;
        PathFinderGoalSummonFindTarget findTarget;
        List<ItemStack> items;
        Summon passenger;

        public CustomSkeletonHorse(org.bukkit.World world, Player player, Plugin plugin)
        {
            super(EntityTypes.SKELETON_HORSE, ((CraftWorld)world).getHandle());
            this.owner = player;
            this.plugin = plugin;
            LivingEntity le = (LivingEntity)this.getBukkitEntity();
            le.setRemoveWhenFarAway(false);
            ItemStack[] arrayStack = new ItemStack[1];
            arrayStack[0] = new ItemStack(Material.DIAMOND_HORSE_ARMOR);
            le.getEquipment().setArmorContents(arrayStack);
            this.setCustomName(new ChatComponentText(ChatColor.RED + player.getName() + " Atlı İskeleti "));
            this.setCustomNameVisible(true);

            MMOClass mmoClass = MainClass.classObjectMap.get(owner.getUniqueId());
            int level = mmoClass.getLevel();

            double baseMS = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue();
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(baseMS+(double)level/400);

            double baseHealth = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue();
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(baseHealth + 0.75*(double)level);
            this.setHealth((float)(baseHealth + 3*level));

            this.target = null;
            this.stance = MobStance.type.PASSIVE;
            Location loc = player.getLocation();
            this.setLocation(loc.getX(),loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            //this.goalSelector.a(0, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,15,true,true,this::isNotOwner));
            CustomWitherSkeleton witherSkeleton = new CustomWitherSkeleton(world,player,plugin);
            witherSkeleton.setRidingSummon(this);
            this.passenger = witherSkeleton;
            this.setOwnerUUID(witherSkeleton.getUniqueID());
            le.setPassenger(witherSkeleton.getBukkitEntity());
            this.getWorld().addEntity(this);
        }



        @Override
        public boolean d(MobEffect mobeffect) {
            return false;
        }

        @Override
        public boolean attackEntity(net.minecraft.server.v1_16_R1.Entity entity) {
            if (!super.attackEntity(entity)) {
                return false;
            } else {
                return true;
            }
        }

        private boolean isNotOwner(EntityLiving e)
        {
            if(e.getBukkitEntity().getLocation().distance(this.getBukkitEntity().getLocation()) > 12)
                return false;
            if(e.getBukkitEntity() instanceof Player && e.getBukkitEntity().getWorld().getName().equals("openworld_emnia"))
                return false;
            if(e.getUniqueID().equals(owner.getUniqueId()))
            {
                return false;
            }
            if(getRidingSummon() != null && e.getUniqueID().equals(getRidingSummon().getNormalEntity().getUniqueId()))
                return false;
            if(MainClass.classObjectMap.containsKey(e.getUniqueID()))
            {
                if(ifSameFaction(owner, e.getBukkitEntity()))
                {
                    return false;
                }
            }
            if(e instanceof Summon)
            {
                Player otherSummonOwner = ((Summon)e).getOwner();
                if(ifSameFaction(owner, otherSummonOwner))
                {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void initPathfinder()
        {
            PathFinderGoalTeleportToOwner teleportToOwner = new PathFinderGoalTeleportToOwner(this,20,this);
            this.goalSelector.a(0, new PathfinderGoalFloat(this));
            this.goalSelector.a(1, teleportToOwner);
            //this.goalSelector.a(3, new PathfinderGoalArrowAttack(this,0.6D,1,40,15.0f));
            //this.goalSelector.a(2, new PathfinderGoalArrowAttack(this,0.01D,1,20,30.0F));
            this.goalSelector.a(3, new PathfinderGoalSummon(this,1.1D,15,this));
            this.goalSelector.a(4, new PathfinderGoalLookAtPlayer(this,EntityPlayer.class,1.0f));

            this.findTarget = new PathFinderGoalSummonFindTarget<EntityLiving>(this,EntityLiving.class,1,true,true,this::isNotOwner,this);
            this.targetSelector.a(0, teleportToOwner);
            this.targetSelector.a(1, findTarget);
        }


        @Override
        protected void dropDeathLoot(DamageSource damagesource, int i, boolean flag)
        {
            //Nothing.
        }

        @Override
        public Player getOwner() {
            return owner;
        }

        @Override
        public void kill() {
            this.getRidingSummon().kill();
            this.killEntity();
            ((Necromancer)MainClass.classObjectMap.get(owner.getUniqueId())).setSummon(null);
        }

        @Override
        public void teleportToPlayer() {
            Location loc = owner.getLocation();
            this.getBukkitEntity().teleport(loc);
            loc = null;
        }

        @Override
        public void setStance(MobStance.type mobStance) {
            this.stance = mobStance;
            this.setTarget(null);
        }

        @Override
        public MobStance.type getStance() {
            return stance;
        }

        @Override
        public Entity getTarget() {
            return target;
        }

        @Override
        public void setTarget(Entity entity) {
            if(entity == null)
            {
                findTarget.setTarget(null);
                this.setGoalTarget(null);
                this.target = null;
                return;
            }
            this.target = entity;
            this.setGoalTarget((EntityLiving) ((CraftEntity)entity).getHandle(),entity instanceof Player ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY, true);
            findTarget.setTarget((EntityLiving) ((CraftEntity)entity).getHandle());
        }

        @Override
        public Entity getNormalEntity() {
            return this.getBukkitEntity();
        }

        @Override
        public PathFinderGoalSummonFindTarget getFindTarget() {
            return findTarget;
        }

        @Override
        public UUID getid() {
            return this.getUniqueID();
        }

        @Override
        public List<ItemStack> getItems() {
            return items;
        }

        @Override
        public void setItems(List<ItemStack> newItems) {
            this.items = newItems;
        }

        @Override
        public Summon getRidingSummon()
        {
            return this.passenger;
        }
    }

    public interface Summon
    {
        public Player getOwner();
        public void kill();
        public void teleportToPlayer();
        public void setStance(MobStance.type mobStance);
        public MobStance.type getStance();
        public Entity getTarget();
        public void setTarget(Entity entity);
        public Entity getNormalEntity();
        public PathFinderGoalSummonFindTarget getFindTarget();
        public UUID getid();
        public List<ItemStack> getItems();
        public void setItems(List<ItemStack> newItems);
        public void initPathfinder();
        public Summon getRidingSummon();
    }
}

