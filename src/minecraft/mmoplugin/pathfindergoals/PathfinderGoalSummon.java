package minecraft.mmoplugin.pathfindergoals;

import minecraft.mmoplugin.MainClass;
import minecraft.mmoplugin.Necromancer;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;

import java.util.EnumSet;

public class PathfinderGoalSummon extends PathfinderGoal
{
    Plugin plugin = MainClass.getPlugin(MainClass.class);
    Necromancer.Summon summon;
    private final EntityInsentient a; //The Summon itself.
    private EntityLiving b; //The Summoner / Owner of the Summon.

    private final double f; //Summon's speed
    private final float g; // Distance between Summon and Summoner.

    private double c; //x coordinate
    private double d; //y coordinate
    private double e; //z coordinate


    public PathfinderGoalSummon(EntityInsentient a, double speed, float distance, Necromancer.Summon summon)
    {
        this.a = a;
        this.f = speed;
        this.g = distance;
        this.a(EnumSet.of(Type.MOVE));
        this.summon = summon;
        this.plugin = plugin;
    }

    @Override
    public boolean a() //Runs every single tick and starts the pathfinding goal if it returns true.
    {
        this.b = (EntityLiving) ((CraftPlayer)this.summon.getOwner()).getHandle();
        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Current target is, " + this.a.getGoalTarget());
        if(this.a.getGoalTarget()!= null && !(this.a.getGoalTarget().getUniqueID().equals(this.summon.getOwner().getUniqueId())))
            return false;
        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "We're here!");
        Vec3D vec = RandomPositionGenerator.a((EntityCreature)this.a,16,7,this.b.getPositionVector());

        if(vec == null)
            return false;

        //Follow the owner.
        this.c = this.b.locX()-2;
        this.d = this.b.locY();
        this.e = this.b.locZ()-2;
        return true; //Now it will run C;
    }

    @Override
    public void c()
    {
        this.a.getNavigation().a(this.c,this.d,this.e,this.f);
    }

    @Override
    public boolean b()
    {
        //Runs every tick as long as its true and Repeats c();
        return !this.a.getNavigation().m() && this.b.h(this.a) < (double) (this.g * this.g);
        //Have I NOT made it to the location && Am I still in 20 blocks?
        //Because this.a.getNavigation().m() returns TRUE when the entity has reached the getNavigation location.
        //We check if it is false, meaning we check if the entity still needs to walk.
    }

    @Override
    public void d()
    {
        //Runs when b() return false...
    }
}
