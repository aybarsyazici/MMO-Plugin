package minecraft.mmoplugin.events;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class FactionClaimConfig
{
    Plugin plugin;
    FileConfiguration dataConfig;
    File configFile;

    public FactionClaimConfig(Plugin plugin) throws IOException {
        this.plugin = plugin;
        dataConfig = null;
        configFile = null;
        saveDefaultConfig();
    }

    public void reloadConfig()
    {
        if(this.configFile == null)
        {
            this.configFile = new File(this.plugin.getDataFolder(), "FactionClaimConfig.yml");
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);
        InputStream defaultStream = this.plugin.getResource("FactionClaimConfig.yml");
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
            configFile = new File(this.plugin.getDataFolder(), "FactionClaimConfig.yml");
        }
        if(!configFile.exists())
        {
            System.out.println("Config file did not exist. Creating a new one.");
            configFile.createNewFile();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    System.out.println("Loading defaults into FactionClaimConfig.");
                    plugin.saveResource("FactionClaimConfig.yml", false);
                }
            },0);
        }
    }
}
