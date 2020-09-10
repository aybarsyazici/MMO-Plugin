package minecraft.mmoplugin.pathfindergoals;

import minecraft.mmoplugin.MainClass;
import minecraft.mmoplugin.Necromancer;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.PathfinderGoal;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;

import java.util.EnumSet;

public class PathFinderGoalTeleportToOwner extends PathfinderGoal
{
    Plugin plugin = MainClass.getPlugin(MainClass.class);
    Necromancer.Summon summon;
    private final EntityInsentient a; //The Summon itself.
    private EntityLiving b; //The Summoner / Owner of the Summon.


    private final float g; // Distance between Summon and Summoner.

    private double c; //x coordinate
    private double d; //y coordinate
    private double e; //z coordinate


    public PathFinderGoalTeleportToOwner(EntityInsentient a, float distance, Necromancer.Summon summon)
    {
        this.a = a;
        this.g = distance;
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, Type.TARGET));
        this.summon = summon;
        this.plugin = plugin;
    }

    @Override
    public boolean a() //Runs every single tick and starts the pathfinding goal if it returns true.
    {
        this.b = (EntityLiving) ((CraftPlayer)this.summon.getOwner()).getHandle();
        if((this.b.h(this.a) > (double) (this.g*this.g)))
        {
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Owner is far away, trying to tp...");
            this.a.setPosition(this.b.locX(), this.b.locY(), this.b.locZ());
            summon.setTarget(null);
            this.a.setGoalTarget(null);
            return false;
        }
        return false;
    }

    @Override
    public void c()
    {
        //Nothing.
    }

    @Override
    public boolean b()
    {
        return false;
    }

    @Override
    public void d()
    {
        //nothing.
    }
}
