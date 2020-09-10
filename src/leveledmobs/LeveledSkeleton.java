package leveledmobs;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class LeveledSkeleton extends EntitySkeleton implements LeveledMob
{

    int level;
    boolean isOpenWorld;
    private int saverid;

    public boolean isANotLeveledMob(EntityLiving e)
    {
        if(e.getBukkitEntity().getLocation().distance(this.getBukkitEntity().getLocation()) > 7)
        {
            return false;
        }
        return (!(e instanceof LeveledMob) && !(e instanceof RaidBoss));
    }

    public LeveledSkeleton(org.bukkit.World world, int level, Location loc)
    {
        super(EntityTypes.SKELETON, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Skeleton "));
        this.setCustomNameVisible(true);

        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + (0.85*(double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level;

        LivingEntity le = (LivingEntity)this.getBukkitEntity();
        org.bukkit.inventory.ItemStack bow = new ItemStack(Material.BOW);
        le.getEquipment().setItemInMainHand(bow);
        ItemStack frostWalker = new ItemStack(Material.LEATHER_BOOTS);
        frostWalker.addEnchantment(Enchantment.FROST_WALKER,1);
        le.getEquipment().setBoots(frostWalker);
        this.isOpenWorld = false;

        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);

        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());

        this.level = level;
        this.getWorld().addEntity(this);
    }

    public LeveledSkeleton(org.bukkit.World world, int level, Location loc, boolean isOpenWorld)
    {
        super(EntityTypes.SKELETON, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Skeleton "));
        this.setCustomNameVisible(true);

        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + (0.85*(double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level;

        LivingEntity le = (LivingEntity)this.getBukkitEntity();
        le.setRemoveWhenFarAway(true);
        org.bukkit.inventory.ItemStack bow = new ItemStack(Material.BOW);
        le.getEquipment().setItemInMainHand(bow);
        this.isOpenWorld = isOpenWorld;

        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);

        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());

        this.level = level;
        this.getWorld().addEntity(this);
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public CraftEntity getNormalEntity() {
        return this.getBukkitEntity();
    }

    @Override
    protected void initPathfinder()
    {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalBowShoot(this, 0.01D, 40, 20.0F));
        this.goalSelector.a(3, new PathfinderGoalArrowAttack(this, 0.01D, 40, 20.0F));
        this.goalSelector.a(4, new PathfinderGoalLookAtPlayer(this,EntityLiving.class,0.7f));
        this.goalSelector.a(5, new PathfinderGoalRandomLookaround(this));
        if(this.isOpenWorld)
            this.goalSelector.a(6, new PathfinderGoalRandomStrollLand(this,1.0D));

        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,10,true,false,this::isANotLeveledMob));
    }
}
