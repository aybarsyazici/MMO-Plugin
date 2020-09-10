package minecraft.mmoplugin;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class AnimateStand extends BukkitRunnable
{
    Player player;
    ArmorStand stand;

    AnimateStand(Player player, ArmorStand stand)
    {
        this.player = player;
        this.stand = stand;
    }
    @SuppressWarnings("deprecation")
    @Override
    public void run()
    {
        EulerAngle oldRot = stand.getRightArmPose();
        EulerAngle newRot = oldRot.add(0,1.2f,0);
        stand.setRightArmPose(newRot);
        stand.teleport(player.getLocation());
    }
}