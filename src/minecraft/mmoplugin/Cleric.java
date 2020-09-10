package minecraft.mmoplugin;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Cleric extends MMOClass
{

    private boolean deathDefied;

    public Cleric(Plugin plugin, Player player, double xp, int level, String className)
    {
        super(plugin, player, xp, level, className);
        this.deathDefied = false;
    }

    public boolean isDeathDefied() {
        return deathDefied;
    }

    public void setDeathDefied(boolean deathDefied) {
        this.deathDefied = deathDefied;
    }
}
