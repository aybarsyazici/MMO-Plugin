package minecraft.mmoplugin.pathfindergoals;

import minecraft.mmoplugin.MobStance;
import minecraft.mmoplugin.Necromancer;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.PathfinderGoalNearestAttackableTarget;
import org.bukkit.event.entity.EntityTargetEvent;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class PathFinderGoalSummonFindTarget <T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget
{
    private final EntityInsentient a;
    protected final int b;
    Necromancer.Summon summon;

    public EntityLiving getTarget() {
        return target;
    }

    public void setTarget(EntityLiving target) {
        this.target = target;
    }

    EntityLiving target;

    /*public PathFinderGoalSummonFindTarget(EntityInsentient entityinsentient, Class oclass, boolean flag, Necromancer.Summon summon) {
        super(entityinsentient, oclass, flag);
        this.b = 0;
        this.summon = summon;
        this.target = null;
    }*/

    public PathFinderGoalSummonFindTarget(EntityInsentient entityinsentient, Class oclass, int i, boolean flag, boolean flag1, @Nullable Predicate<EntityLiving> predicate, Necromancer.Summon summon) {
        super(entityinsentient, oclass, i, flag, flag1, predicate);
        this.b = i;
        this.a = entityinsentient;
        this.summon = summon;
    }

    @Override
    public boolean a() {
        if(summon.getStance() == MobStance.type.OFFENSIVE)
        {
            if (this.b > 0 && this.e.getRandom().nextInt(this.b) != 0) {
                return false;
            } else {
                this.g();
                return this.c != null;
            }
        }
        else if(summon.getStance() == MobStance.type.PASSIVE)
        {
            if(target == null)
                return false;
            if(target.isAlive())
            {
                this.c = target;
                return true;
            }
            else
            {
                this.c = null;
                target = null;
                return false;
            }
        }
        return  false;
    }

    @Override
    public void c() {
        this.e.setGoalTarget(this.c, this.c instanceof EntityPlayer ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY, true);
        super.c();
    }
}
