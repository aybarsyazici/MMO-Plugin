package leveledmobs;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;

public class LeveledCaveSpider extends EntityCaveSpider implements LeveledMob
{
    int level;

    public boolean isANotLeveledMob(EntityLiving e)
    {
        return (!(e instanceof LeveledMob) && !(e instanceof RaidBoss));
    }

    public LeveledCaveSpider(org.bukkit.World world, int level, Location loc)
    {
        super(EntityTypes.CAVE_SPIDER, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Cave Spider "));
        this.setCustomNameVisible(true);


        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + (2*(double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level;


        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);


        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this,EntityLiving.class,1,true,true,this::isANotLeveledMob));
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
}
