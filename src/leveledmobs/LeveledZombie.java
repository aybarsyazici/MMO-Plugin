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

public class LeveledZombie extends EntityZombie implements LeveledMob {
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

    public LeveledZombie(org.bukkit.World world, int level, Location loc)
    {
        super(EntityTypes.ZOMBIE, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Zombie "));
        this.setCustomNameVisible(true);


        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + (1.2*(double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level*2/3;


        LivingEntity en = (LivingEntity) this.getBukkitEntity();
        en.setRemoveWhenFarAway(false);
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);
        this.isOpenWorld = false;

        this.level = level;
        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        this.getWorld().addEntity(this);
    }

    public LeveledZombie(org.bukkit.World world, int level, Location loc, boolean isOpenWorld)
    {
        super(EntityTypes.ZOMBIE, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Zombie "));
        this.setCustomNameVisible(true);


        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + (1.2*(double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level*0.5;
        this.isOpenWorld = isOpenWorld;
        LivingEntity le = (LivingEntity)this.getBukkitEntity();
        le.setRemoveWhenFarAway(true);
        ItemStack frostWalker = new ItemStack(Material.LEATHER_BOOTS);
        frostWalker.addEnchantment(Enchantment.FROST_WALKER,1);
        le.getEquipment().setBoots(frostWalker);
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);

        this.level = level;
        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        this.getWorld().addEntity(this);
    }


    @Override
    public void initPathfinder()
    {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        //this.goalSelector.a(1, new PathfinderGoalHurtByTarget(this));
        this.goalSelector.a(4, new PathfinderGoalZombieAttack(this,1.2D,true));
        this.goalSelector.a(5, new PathfinderGoalLookAtPlayer(this,EntityLiving.class,0.7f));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));

        if(this.isOpenWorld)
            this.goalSelector.a(7, new PathfinderGoalRandomStrollLand(this,1.0D));

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
