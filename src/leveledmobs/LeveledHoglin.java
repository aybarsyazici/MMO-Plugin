package leveledmobs;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.HashMap;

public class LeveledHoglin extends EntityHoglin implements LeveledMob
{
    int level;

    public boolean isANotLeveledMob(EntityLiving e)
    {
        return (!(e instanceof LeveledMob) && !(e instanceof RaidBoss));
    }

    public LeveledHoglin(org.bukkit.World world, int level, Location loc, Plugin plugin)
    {
        super(EntityTypes.HOGLIN, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Hoglin "));
        this.setCustomNameVisible(true);
        this.setAge(20);

        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + (3*(double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level;


        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);

        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());

        this.level = level;

        /*EnumMap goalC = (EnumMap) MainClass.getPrivateField("c", PathfinderGoalSelector.class, goalSelector);
        goalC.clear();
        PathfinderGoalSelector targetB = (PathfinderGoalSelector) MainClass.getPrivateField("b", PathfinderGoalSelector.class, targetSelector);
        targetB.;
        EnumMap targetC = (EnumMap) MainClass.getPrivateField("c", PathfinderGoalSelector.class, targetSelector);
        targetC.clear();*/
        this.getWorld().addEntity(this);
        Field gsa = null;
        try {
            gsa = PathfinderGoalSelector.class.getDeclaredField("c");
            gsa.setAccessible(true);
            gsa.set(this.goalSelector, new HashMap<>());
            gsa.set(this.targetSelector, new HashMap<>());
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }


        // ok now take this instances goals and targets and blank them

        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new PathfinderGoalMeleeAttack(this,1.2D,true));
        this.goalSelector.a(2, new PathfinderGoalLookAtPlayer(this,EntityLiving.class,0.7f));
        this.goalSelector.a(3, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(4, new PathfinderGoalAvoidTarget<EntityChicken>(this,EntityChicken.class,2.0f,2.0D,2.0D));

        this.targetSelector.a(0, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,1,true,true,this::isANotLeveledMob));
    }

    @Override
    public void initPathfinder()
    {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new PathfinderGoalMeleeAttack(this,1.2D,true));
        this.goalSelector.a(2, new PathfinderGoalLookAtPlayer(this,EntityLiving.class,0.7f));
        this.goalSelector.a(3, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(4, new PathfinderGoalAvoidTarget<EntityChicken>(this,EntityChicken.class,2.0f,2.0D,2.0D));

        this.targetSelector.a(0, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,1,true,true,this::isANotLeveledMob));
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
