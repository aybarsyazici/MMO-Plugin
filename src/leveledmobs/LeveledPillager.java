package leveledmobs;

import com.destroystokyo.paper.entity.ai.VanillaGoal;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Witch;
import org.bukkit.inventory.ItemStack;

public class LeveledPillager extends EntityPillager implements LeveledMob
{
    int level;

    public boolean isANotLeveledMob(EntityLiving e)
    {
        if(e.getBukkitEntity().getLocation().distance(this.getBukkitEntity().getLocation()) > 7)
        {
            return false;
        }
        return (!(e instanceof LeveledMob) && !(e instanceof RaidBoss));
    }

    public LeveledPillager(org.bukkit.World world, int level, Location loc)
    {
        super(EntityTypes.PILLAGER, ((CraftWorld)world).getHandle());
        this.setCustomName(new ChatComponentText(ChatColor.BLUE + "[LVL " + level + "]" + LeveledMobListener.getLeveledColor(level) + " Pillager "));
        this.setCustomNameVisible(true);


        double health = this.getAttributeInstance(GenericAttributes.MAX_HEALTH).getBaseValue() + (2*(double)level);
        double speed = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getBaseValue() + 0.001*(double)level;
        double attackDamage = this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getBaseValue() + (double)level;


        LivingEntity en = (LivingEntity) this.getBukkitEntity();
        en.setRemoveWhenFarAway(false);
        ItemStack crossbow = new ItemStack(org.bukkit.Material.CROSSBOW);
        crossbow.addEnchantment(Enchantment.MULTISHOT,1);
        crossbow.addEnchantment(Enchantment.QUICK_CHARGE,3);
        en.getEquipment().setItemInMainHand(crossbow);
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(health);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(attackDamage);

        this.level = level;
        this.setHealth((float)health);
        this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(),loc.getPitch());
        Bukkit.getMobGoals().removeAllGoals((Pillager)this.getBukkitEntity());
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalCrossbowAttack<>(this, 1.25D, 8.0F));
        this.goalSelector.a(3, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(4, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this, EntityLiving.class, 6, true, false, this::isANotLeveledMob));
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
