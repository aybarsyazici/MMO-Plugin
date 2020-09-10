package leveledmobs;

import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;

public interface LeveledMob
{
    public int getLevel();

    public CraftEntity getNormalEntity();

}
