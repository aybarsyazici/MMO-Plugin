package minecraft.mmoplugin.pathfindergoals;

import minecraft.mmoplugin.Necromancer;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.PathfinderGoal;

import java.util.EnumSet;

public class PathfinderAttackOwnersTarget extends PathfinderGoal
{

    Necromancer.Summon summon;
    EntityLiving target;
    private final EntityInsentient a; //The Summon itself.
    private final float g;

    public PathfinderAttackOwnersTarget(EntityInsentient a, Necromancer.Summon summon, EntityLiving target, float distance)
    {
        this.a = a;
        this.target = target;
        this.g = distance;
        this.a(EnumSet.of(Type.TARGET));
        this.summon = summon;
    }

    @Override
    public boolean a()
    {
        if(target == null)
            return false;
        if(!target.isAlive())
            return false;
        Necromancer.Summon riding = summon.getRidingSummon();
        return riding == null || !target.getUniqueID().equals(riding.getNormalEntity().getUniqueId());
    }

    @Override
    public void c()
    {
        this.a.setGoalTarget(target);
    }

    @Override
    public boolean b()
    {
        return target.isAlive() && target.h(this.a) < (double) (this.g * this.g);
    }

    @Override
    public void d()
    {
        this.a.setGoalTarget(null);
    }

    public void setTarget(EntityLiving target)
    {
        this.target = target;
    }
}

