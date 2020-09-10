package leveledmobs;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class LeveledBlaze extends EntityBlaze implements LeveledMob
{
    int level;
    Plugin plugin;


    public boolean isANotLeveledMob(EntityLiving e)
    {
        if(e.getBukkitEntity().getLocation().distance(this.getBukkitEntity().getLocation()) > 11)
        {
            return false;
        }
        return (!(e instanceof LeveledMob) && !(e instanceof RaidBoss));
    }

    public LeveledBlaze(org.bukkit.World world, int level, Location loc)
    {
        super(EntityTypes.BLAZE, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Blaze "));
        this.setCustomNameVisible(true);
        this.setAirTicks(0);

        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + (3*(double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level;


        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);

        LivingEntity le = (LivingEntity)this.getBukkitEntity();
        le.setRemoveWhenFarAway(true);

        this.level = level;
        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,0,true,true,this::isANotLeveledMob));
        this.getWorld().addEntity(this);
    }

    @Override
    public void initPathfinder()
    {
        Class c = EntityBlaze.class.getDeclaredClasses()[0];
        /*Plugin pl = MainClass.getPlugin(MainClass.class);
        pl.getServer().getConsoleSender().sendMessage("PRINTING OUT DECLARED CONSTRUCTORS.");
        for(Constructor temp : c.getDeclaredConstructors())
        {
            System.out.println(temp);
            pl.getServer().getConsoleSender().sendMessage(temp.toString());
            pl.getServer().getConsoleSender().sendMessage("**********");
        }
        pl.getServer().getConsoleSender().sendMessage("PUBLIC CONSTRUCTORS");
        for(Constructor temp : c.getConstructors())
        {
            System.out.println(temp);
            pl.getServer().getConsoleSender().sendMessage(temp.toString());
            pl.getServer().getConsoleSender().sendMessage("**********");
        }
        pl.getServer().getConsoleSender().sendMessage("DONE.");*/

        Constructor  constructor = null;
        try {
            constructor = c.getDeclaredConstructor(EntityBlaze.class);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        try {
            this.goalSelector.a(1, (PathfinderGoal) constructor.newInstance(this));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }

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
