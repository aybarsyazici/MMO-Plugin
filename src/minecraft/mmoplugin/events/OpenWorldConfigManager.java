package minecraft.mmoplugin.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.joda.time.DateTime;
import sun.awt.geom.AreaOp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;

public class OpenWorldConfigManager
{

    public static HashMap<UUID, OpenWorldSaver> openWorldMap;
    public static HashMap<Integer, OpenWorldSaver> openWorldSavers;
    public static int lastSaverID;
    public static HashMap<Chunk, OpenWorldSaver> chunSaverMap;

    Plugin plugin;
    FileConfiguration dataConfig;
    File configFile;

    public OpenWorldConfigManager(Plugin plugin) throws IOException {
        this.plugin = plugin;
        dataConfig = null;
        configFile = null;
        saveDefaultConfig();
    }

    public void reloadConfig()
    {
        if(this.configFile == null)
        {
            this.configFile = new File(this.plugin.getDataFolder(), "OpenWorldConfig.yml");
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);
        InputStream defaultStream = this.plugin.getResource("OpenWorldConfig.yml");
        if(defaultStream != null)
        {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.dataConfig.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig()
    {
        if(this.dataConfig == null)
        {
            System.out.println("Config file was null, reloading.");
            reloadConfig();
        }
        return this.dataConfig;
    }

    public void saveConfig()
    {
        if(this.dataConfig == null || this.configFile == null)
            return;
        try
        {
            getConfig().save(configFile);
        }
        catch (IOException e)
        {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile,e);
        }
    }

    public void saveDefaultConfig() throws IOException {
        if(configFile == null)
        {
            System.out.println("Getting the data folder to create a new file..." + plugin.getDataFolder());
            configFile = new File(this.plugin.getDataFolder(), "OpenWorldConfig.yml");
        }
        if(!configFile.exists())
        {
            System.out.println("Config file did not exist. Creating a new one.");
            configFile.createNewFile();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    System.out.println("Loading defaults into OpenWorldConfig.");
                    plugin.saveResource("OpenWorldConfig.yml", false);
                }
            },0);
        }
    }


    public static class OpenWorldSaver
    {

        private String name;
        private List<String> mobsToSpawn;
        private int id;
        private int mobCount;
        private int lowerLevel;
        private int upperLevel;
        public int currentMobCount;
        private Chunk chunk;

        public OpenWorldSaver()
        {
            lastSaverID++;
            this.name = null;
            this.mobsToSpawn = new ArrayList<>();
            this.id = lastSaverID;
            this.mobCount = 0;
            this.upperLevel = 0;
            this.lowerLevel = 0;
            this.currentMobCount = 0;
            this.chunk = null;
        }

        public OpenWorldSaver(Chunk chunk, int mobCount, int lowerLevel, int upperLevel, String[] mobsToSpawn, String name)
        {
            lastSaverID++;
            this.name = name;
            this.mobsToSpawn = new ArrayList<>(Arrays.asList(mobsToSpawn));
            this.id = lastSaverID;
            this.mobCount = mobCount;
            this.upperLevel = upperLevel;
            this.lowerLevel = lowerLevel;
            this.currentMobCount = 0;
            this.chunk = chunk;
        }

        public int getCurrentMobCount() {
            return currentMobCount;
        }

        public void setCurrentMobCount(int currentMobCount) {
            this.currentMobCount = currentMobCount;
        }

        public Chunk getChunk() {
            return chunk;
        }

        public void setChunk(Chunk chunk) {
            this.chunk = chunk;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getMobsToSpawn() {
            return mobsToSpawn;
        }

        public void setMobsToSpawn(List<String> mobsToSpawn) {
            this.mobsToSpawn = mobsToSpawn;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getMobCount() {
            return mobCount;
        }

        public void setMobCount(int mobCount) {
            this.mobCount = mobCount;
        }

        public int getLowerLevel() {
            return lowerLevel;
        }

        public void setLowerLevel(int lowerLevel) {
            this.lowerLevel = lowerLevel;
        }

        public int getUpperLevel() {
            return upperLevel;
        }

        public void setUpperLevel(int upperLevel) {
            this.upperLevel = upperLevel;
        }

        @Override
        public String toString()
        {
            String toReturn = ChatColor.GOLD + "_____________________________" + "\n" + ChatColor.WHITE;;
            toReturn += ChatColor.GOLD + "Operasyon: " + id + ".)" + "\n";
            toReturn += ChatColor.AQUA + "Chunk: " + ChatColor.WHITE;
            if(chunk != null)
            {
                toReturn += "\n";
                toReturn += "\tWorld: " + chunk.getWorld().getName() + "\n";
                toReturn += "\tX: " + String.format(".%2f",chunk.getX()) +"\n";
                toReturn += "\tZ: " + String.format(".%2f",chunk.getZ()) +"\n";
            }
            else
            {
                toReturn += ChatColor.RED + "null" + ChatColor.WHITE + "\n";
            }
            toReturn += "MobCount: " + mobCount + "\n";
            toReturn += "Name: " + name + "\n";
            toReturn += "UpperLevel: " + ChatColor.RED + upperLevel + "\n" + ChatColor.WHITE;
            toReturn += "LowerLevel: " + ChatColor.DARK_PURPLE + lowerLevel + "\n" + ChatColor.WHITE;
            toReturn += ChatColor.GOLD + "_____________________________" + "\n" + ChatColor.WHITE;
            return toReturn;
        }
    }
}
