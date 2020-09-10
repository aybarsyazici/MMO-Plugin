package particles;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class CircleParticle extends BukkitRunnable
{

    LivingEntity e;
    double radius;
    double counter;

    public CircleParticle(LivingEntity e, double radius)
    {
        this.e = e;
        this.radius = radius;
        counter=0.0;
    }

    @Override
    public void run() {
        Location loc = e.getLocation();
        loc.add(0,counter,0);
        for(int theta = 0; theta <=360; theta+=15)
        {
            double xToAdd = radius*Math.sin(theta);
            double yToAdd = radius*Math.cos(theta);
            loc.add(xToAdd,0,yToAdd);
            loc.getWorld().spawnParticle(Particle.DRIP_LAVA,loc,1); // ASH hoş, BUBBLE hoş, BARRIER literally barrier. OBSIDIAN TEAR fena değil, DRIP LAVA güzel. FALLING WATER->Cleanse. DRIPPING OBSIDIAN TEAR baya uzun kalıyor.
            loc.subtract(xToAdd,0,yToAdd);   /* TODO: DRIPPING_* uzunlar için, FALLING_* kısalar için. */
        }
        loc.subtract(0,counter,0);
        counter += 0.2;
        if(counter > e.getHeight()+0.5)
        {
            //MainClass.getPlugin(MainClass.class).getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Counter was: " + counter + " canceling.");
            //MainClass.getPlugin(MainClass.class).getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "e.getHeight was " + e.getHeight());
            cancel();
        }
    }
}
