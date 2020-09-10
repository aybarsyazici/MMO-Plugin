package leveledmobs;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;

public class LeveledPiglin extends EntityPiglin implements LeveledMob
{
    int level;

    public boolean isANotLeveledMob(EntityLiving e)
    {
        return (!(e instanceof LeveledMob) && !(e instanceof RaidBoss));
    }

    public LeveledPiglin(org.bukkit.World world, int level, Location loc)
    {
        super(EntityTypes.PIGLIN, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Piglin "));
        this.setCustomNameVisible(true);

        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + (3*(double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level*3/2;


        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);

        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());

        this.level = level;

        /*List goalC = (List) MainClass.getPrivateField("c", PathfinderGoalSelector.class, goalSelector);
        goalC.clear();
        EnumMap targetB = (List) (EnumMap) MainClass.getPrivateField("b", PathfinderGoalSelector.class, targetSelector);
        targetB.clear();
        EnumMap targetC = (EnumMap) MainClass.getPrivateField("c", PathfinderGoalSelector.class, targetSelector);
        targetC.clear();*/
        this.initPathfinder();
        this.getWorld().addEntity(this);
    }

    @Override
    public void initPathfinder()
    {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new PathfinderGoalMeleeAttack(this,1.2D,true));
        this.goalSelector.a(2, new PathfinderGoalLookAtPlayer(this,EntityLiving.class,0.7f));
        this.goalSelector.a(3, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,1,true,true,this::isANotLeveledMob));
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
