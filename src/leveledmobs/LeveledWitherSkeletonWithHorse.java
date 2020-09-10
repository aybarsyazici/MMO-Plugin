package leveledmobs;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import minecraft.mmoplugin.MainClass;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ravager;
import org.bukkit.inventory.ItemStack;

public class LeveledWitherSkeletonWithHorse extends EntitySkeletonWither implements LeveledMob
{
    int level;

    public boolean isANotLeveledMob(EntityLiving e)
    {
        if(e.getBukkitEntity().getLocation().distance(this.getBukkitEntity().getLocation()) > 11)
        {
            return false;
        }
        return (!(e instanceof LeveledMob) && !(e instanceof RaidBoss));
    }

    public LeveledWitherSkeletonWithHorse(org.bukkit.World world, int level, Location loc)
    {
        super(EntityTypes.WITHER_SKELETON, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Wither Skeleton "));
        this.setCustomNameVisible(true);

        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + (1.2*(double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level*0.30;


        attackDamage = attackDamage*0.4;
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);

        LivingEntity le = (LivingEntity) this.getBukkitEntity();
        le.setRemoveWhenFarAway(false);

        org.bukkit.inventory.ItemStack sword = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL,5);
        sword.addEnchantment(Enchantment.FIRE_ASPECT,2);

        org.bukkit.inventory.ItemStack helmet = new org.bukkit.inventory.ItemStack(Material.NETHERITE_HELMET);
        helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL,4);
        helmet.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS,4);
        helmet.addEnchantment(Enchantment.PROTECTION_FIRE,4);
        helmet.addEnchantment(Enchantment.PROTECTION_PROJECTILE,4);

        org.bukkit.inventory.ItemStack chest = new org.bukkit.inventory.ItemStack(Material.NETHERITE_CHESTPLATE);
        chest.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL,4);
        chest.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS,1);
        chest.addEnchantment(Enchantment.PROTECTION_FIRE,4);
        chest.addEnchantment(Enchantment.PROTECTION_PROJECTILE,4);

        org.bukkit.inventory.ItemStack leggings = new org.bukkit.inventory.ItemStack(Material.NETHERITE_LEGGINGS);
        leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL,4);
        leggings.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS,1);
        leggings.addEnchantment(Enchantment.PROTECTION_FIRE,1);
        leggings.addEnchantment(Enchantment.PROTECTION_PROJECTILE,4);

        org.bukkit.inventory.ItemStack boots = new org.bukkit.inventory.ItemStack(Material.NETHERITE_BOOTS);
        boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL,4);
        boots.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS,4);
        boots.addEnchantment(Enchantment.PROTECTION_FIRE,4);
        boots.addEnchantment(Enchantment.PROTECTION_PROJECTILE,4);


        le.getEquipment().setHelmet(helmet);
        le.getEquipment().setItemInMainHand(sword);
        le.getEquipment().setChestplate(chest);
        le.getEquipment().setLeggings(leggings);
        le.getEquipment().setBoots(boots);

        LeveledRavager leveledRavager = new LeveledRavager(world,level,loc);
        LivingEntity leRavager = (LivingEntity) leveledRavager.getBukkitEntity();
        leRavager.setPassenger(this.getBukkitEntity());
        leRavager.setRemoveWhenFarAway(false);
        double aDamage = leRavager.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
        leveledRavager.goalSelector.a(1, new PathfinderGoalMeleeAttack(leveledRavager,1.2d,true));
        this.level = level;
        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        this.getWorld().addEntity(this);
    }

    @Override
    public void initPathfinder()
    {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new PathfinderGoalMeleeAttack(this,1.2D,true));
        this.goalSelector.a(2, new PathfinderGoalLookAtPlayer(this,EntityLiving.class,0.7f));
        this.goalSelector.a(3, new PathfinderGoalRandomLookaround(this));

        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,10,true,true,this::isANotLeveledMob));
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public CraftEntity getNormalEntity() {
        return this.getBukkitEntity();
    }


}
