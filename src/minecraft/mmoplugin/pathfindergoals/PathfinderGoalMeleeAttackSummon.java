package minecraft.mmoplugin.pathfindergoals;

import minecraft.mmoplugin.MMOClass;
import minecraft.mmoplugin.Necromancer;
import net.minecraft.server.v1_16_R1.EntityCreature;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.PathEntity;
import net.minecraft.server.v1_16_R1.PathfinderGoalMeleeAttack;
import org.bukkit.entity.Player;

import java.util.EnumSet;

public class PathfinderGoalMeleeAttackSummon extends PathfinderGoalMeleeAttack
{
    protected final EntityCreature a;
    private final double b;
    private final boolean c;
    private PathEntity d;
    private double e;
    private double f;
    private double g;
    private int h;
    private int i;
    private final int j = 20;
    private long k;
    private Necromancer.Summon summon;

    public PathfinderGoalMeleeAttackSummon(EntityCreature var0, double var1, boolean var3, Necromancer.Summon summon) {
        super(var0, var1, var3);
        this.a = var0;
        this.b = var1;
        this.c = var3;
        this.a(EnumSet.of(Type.MOVE, Type.LOOK));
        this.summon = summon;
    }

    @Override
    public boolean a() {
        EntityLiving var2 = this.a.getGoalTarget();
        if (var2 == null) {
            return false;
        }
        else if (!var2.isAlive()) {
            return false;
        }
        else if(var2.getUniqueID().equals(summon.getOwner().getUniqueId()))
        {
            return false;
        }
        else if(var2.getBukkitEntity().getLocation().distance(summon.getNormalEntity().getLocation()) > 12)
            return false;
        else if(summon.getRidingSummon() != null && var2.getUniqueID().equals(summon.getRidingSummon().getNormalEntity().getUniqueId()))
            return false;
        else if(MMOClass.ifSameFaction(summon.getOwner(),var2.getBukkitEntity()))
            return false;
        else if(var2.getBukkitEntity() instanceof Player && var2.getBukkitEntity().getWorld().getName().equals("openworld_emnia"))
            return false;
        else
        {
            this.d = this.a.getNavigation().a(var2, 0);
            if (this.d != null)
            {
                return true;
            }
            else
            {
                return this.a(var2) >= this.a.g(var2.locX(), var2.locY(), var2.locZ());
            }
        }
    }
}
