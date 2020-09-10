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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class LeveledSpider extends EntitySpider implements LeveledMob
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

    public LeveledSpider(org.bukkit.World world, int level, Location loc)
    {
        super(EntityTypes.SPIDER, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Spider "));
        this.setCustomNameVisible(true);
        this.isOpenWorld = false;


        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + ((double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level/3;


        LivingEntity le = (LivingEntity)this.getBukkitEntity();
        le.setRemoveWhenFarAway(false);
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);


        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        this.level = level;
        this.getWorld().addEntity(this);
    }

    public LeveledSpider(org.bukkit.World world, int level, Location loc, boolean isOpenWorld)
    {
        super(EntityTypes.SPIDER, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Spider "));
        this.setCustomNameVisible(true);
        this.isOpenWorld = isOpenWorld;


        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + ((double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level/3;


        LivingEntity en = (LivingEntity) this.getBukkitEntity();
        org.bukkit.inventory.ItemStack frostWalker = new ItemStack(Material.LEATHER_BOOTS);
        frostWalker.addEnchantment(Enchantment.FROST_WALKER,1);
        en.getEquipment().setBoots(frostWalker);
        en.setRemoveWhenFarAway(true);
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);


        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        this.level = level;
        this.getWorld().addEntity(this);
    }

    @Override
    public void initPathfinder()
    {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalLeapAtTarget(this, 0.4F));
        Class c = EntitySpider.class.getDeclaredClasses()[1]; //TODO: CAREFUL!
        Constructor constructor = null;
        try {
            constructor = c.getDeclaredConstructor(EntitySpider.class);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        try {
            this.goalSelector.a(1, (PathfinderGoal) constructor.newInstance(this));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }

        if(isOpenWorld)
        {
            this.goalSelector.a(3, new PathfinderGoalRandomStrollLand(this,0.8D));
        }

        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,10,true,false,this::isANotLeveledMob));
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
