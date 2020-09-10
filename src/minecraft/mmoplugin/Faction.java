package minecraft.mmoplugin;

import minecraft.mmoplugin.events.FactionClaimConfig;
import minecraft.mmoplugin.events.FactionConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.joda.time.DateTime;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static minecraft.mmoplugin.MainClass.factionMap;

public class Faction
{

    public static final int warCooldown = 60; //in minutes.
    public static FactionClaimConfig claimConfig;
    public static FactionConfig config;
    public static Queue<String> applicationQueue;

    public static String getFactionPrefix(String factionName)
    {
        return ChatColor.BLUE + "[" + ChatColor.GREEN + factionName.toUpperCase() + ChatColor.BLUE + "] " + ChatColor.WHITE;
    }

    public static String getFactionPrefix(Faction faction)
    {
        return ChatColor.BLUE + "[" + ChatColor.GREEN + faction.getFactionName().toUpperCase() + ChatColor.BLUE + "] " + ChatColor.WHITE;
    }

    public enum Ranks
    {
        ÜYE, //⭐
        MEMUR, // ⭐⭐
        MODERATÖR, // ⭐⭐⭐
        ADMİN, // ⭐⭐⭐⭐
        KURUCU // 👑
    }

    public enum RequestType
    {
        ALLY,
        CEASE_FIRE
    }

    public enum JoinType
    {
        OPEN,
        INVITE_ONLY
    }

    public enum ChatType
    {
        NORMAL,
        FACTION,
        ALLIANCE
    }

    private String factionName;
    private List<Player> onlineFactionMembers;
    private UUID owner;
    private JoinType joinType;
    private int claimedCount;
    private int power;
    private HashMap<String, Faction> enemyFactions;
    private HashMap<String, Faction> allyFactions;
    private int size;
    private Faction allyRequester;
    private int timerTaskId;
    private RequestType requestType;
    private HashMap<String, DateTime> warCoolDownMap;
    private HashMap<UUID, DateTime> applications;
    private Location home;

    public HashMap<UUID, DateTime> getApplications() {
        return applications;
    }

    public void setApplications(HashMap<UUID, DateTime> applications)
    {
        this.applications = applications;
    }

    public void addApplication(Player p)
    {
        DateTime temp = new DateTime().plusMinutes(120);
        this.applications.put(p.getUniqueId(),temp);
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()))
            MainClass.classObjectMap.get(p.getUniqueId()).getApplications().put(this,temp);
        String addToQueue = factionName+";"+p.getUniqueId().toString();
        applicationQueue.add(addToQueue);
    }

    public void addApplication(UUID uuid)
    {
        DateTime temp = new DateTime().plusMinutes(120);
        this.applications.put(uuid,temp);
        if(MainClass.classObjectMap.containsKey(uuid))
            MainClass.classObjectMap.get(uuid).getApplications().put(this,temp);
        String addToQueue = factionName+";"+uuid.toString();
        applicationQueue.add(addToQueue);
    }

    public void removeApplication(Player p)
    {
        this.applications.remove(p.getUniqueId());
        if(MainClass.classObjectMap.containsKey(p.getUniqueId()))
            MainClass.classObjectMap.get(p.getUniqueId()).getApplications().remove(this);
        String toRemove = factionName+";"+p.getUniqueId().toString();
        applicationQueue.remove(toRemove);
    }

    public void removeApplication(UUID uuid)
    {
        this.applications.remove(uuid);
        if(MainClass.classObjectMap.containsKey(uuid))
            MainClass.classObjectMap.get(uuid).getApplications().remove(this);
        String toRemove = factionName+";"+uuid.toString();
        applicationQueue.remove(toRemove);
    }

    public HashMap<String, DateTime> getWarCoolDownMap() {
        return warCoolDownMap;
    }

    public void setWarCoolDownMap(HashMap<String, DateTime> warCoolDownMap) {
        this.warCoolDownMap = warCoolDownMap;
    }

    public void addCooldown(Faction faction)
    {
        this.warCoolDownMap.put(faction.getFactionName(), new DateTime().plusMinutes(warCooldown));
    }

    public void addCooldown(String factionName)
    {
        this.warCoolDownMap.put(factionName, new DateTime().plusMinutes(warCooldown));
    }

    public void removeCooldown(String factionName)
    {
        this.warCoolDownMap.remove(factionName);
    }

    public void removeCooldown(Faction faction)
    {
        this.warCoolDownMap.remove(faction);
    }

    public Faction getAllyRequester() {
        return allyRequester;
    }

    public void setAllyRequester(Faction allyRequester) {
        this.allyRequester = allyRequester;
    }

    public int getTimerTaskId() {
        return timerTaskId;
    }

    public void setTimerTaskId(int timerTaskId) {
        this.timerTaskId = timerTaskId;
    }


    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setEnemyFactions(HashMap<String, Faction> enemyFactions) {
        this.enemyFactions = enemyFactions;
    }

    public HashMap<String, Faction> getAllyFactions() {
        return allyFactions;
    }

    public void setAllyFactions(HashMap<String, Faction> allyFactions) {
        this.allyFactions = allyFactions;
    }

    public Location getHome() {
        return home;
    }

    public void setHome(Location home) {
        this.home = home;
    }

    public void incrementSize()
    {
        this.size++;
    }

    public void decrementSize()
    {
        this.size--;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getClaimedCount() {
        return claimedCount;
    }

    public void setClaimedCount(int claimedCount) {
        this.claimedCount = claimedCount;
    }

    public void increasePower(int power)
    {
        this.power = this.power + power;
    }

    public void decreasePower(int power)
    {
        this.power = this.power - power;
    }

    public void addAsEnemy(Faction f)
    {
        this.enemyFactions.put(f.getFactionName().toLowerCase(),f);
    }

    public void removeFromEnemy(Faction f)
    {
        this.enemyFactions.remove(f.getFactionName().toLowerCase());
    }

    public void addAsAlly(Faction f)
    {
        this.allyFactions.put(f.getFactionName().toLowerCase(),f);
    }

    public void removeFromAlly(Faction f)
    {
        this.allyFactions.remove(f.getFactionName().toLowerCase());
    }

    public HashMap<String, Faction> getEnemies()
    {
        return this.enemyFactions;
    }

    public List<Player> getOnlineFactionMembers() {
        return onlineFactionMembers;
    }

    public void setOnlineFactionMembers(List<Player> onlineFactionMembers) {
        this.onlineFactionMembers = onlineFactionMembers;
    }

    public void incrementClaim()
    {
        this.claimedCount++;
    }

    public void decrementClaim()
    {
        this.claimedCount--;
    }

    public String getFactionName() {
        return factionName;
    }

    public void setFactionName(String factionName) {
        this.factionName = factionName;
    }

    public List<Player> getFactionMembers() {
        return onlineFactionMembers;
    }

    public void setFactionMembers(List<Player> factionMembers) {
        this.onlineFactionMembers = factionMembers;
    }

    public void addAsOnlineFactionMember(Player p)
    {
        this.onlineFactionMembers.add(p);
    }

    public UUID getOwner() {
        return owner;
    }

    public void changeOwner(UUID newOwner, Plugin plugin, String newOwnerName) {
        try {

            PreparedStatement ps = MainClass.conn.prepareStatement("update all_factions set owner=? where Name=?");
            ps.setString(1,newOwnerName);
            ps.setString(2, factionName);
            ps.executeUpdate();

            ps.close();
            ps = null;

            Bukkit.getPlayer(owner).sendMessage(MainClass.getPluginPrefix() + "Faction sahipliği, " + ChatColor.BLUE + Bukkit.getPlayer(newOwner).getName() + ChatColor.WHITE + " adlı oyuncuğa geçti.");
            MainClass.classObjectMap.get(owner).setFaction(factionName, Ranks.ADMİN);
            this.owner = newOwner;
            MainClass.classObjectMap.get(newOwner).setFaction(factionName, Ranks.KURUCU);
            Bukkit.getPlayer(newOwner).sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + factionName + ChatColor.WHITE + " adlı factionın yeni lideri sizsiniz!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        try
        {
            PreparedStatement ps = MainClass.conn.prepareStatement("update all_factions set all_factions.join = ? where Name=?");
            if(joinType.equals(JoinType.OPEN))
                ps.setString(1, "AÇIK");
            else if(joinType.equals(JoinType.INVITE_ONLY))
                ps.setString(1, "KAPALI");
            ps.setString(2, getFactionName());
            ps.executeUpdate();

            ps.close();
            ps = null;

            this.joinType = joinType;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void removeAsOnlineFactionMember(Player p) {
        this.onlineFactionMembers.remove(p);
    }

    Faction(String factionName, UUID owner, String ownerName)
    {
        try
        {

            PreparedStatement ps = MainClass.conn.prepareStatement("insert IGNORE into all_factions values(?,?,?,?)");
            ps.setString(1,factionName);
            ps.setInt(2,1);
            ps.setString(3,"AÇIK");
            ps.setString(4,ownerName);
            int i = ps.executeUpdate();

            ps.close();
            ps = null;

            this.factionName = factionName;
            onlineFactionMembers = new ArrayList<>();
            this.owner = owner;
            this.joinType = JoinType.OPEN;
            this.claimedCount = 0;
            this.power = 60;
            this.enemyFactions = new HashMap<>();
            this.allyFactions = new HashMap<>();
            this.warCoolDownMap = new HashMap<>();
            this.applications = new HashMap<>();
            this.size = 1;
            this.allyRequester = null;
            this.timerTaskId = 0;
            this.home = null;

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

    }

    public void deleteFaction(Player p)
    {
        try {

            //TODO Check if any of the members are currently in a dungeon, and if they are just kick them.
            PreparedStatement ps = MainClass.conn.prepareStatement("delete from all_factions where Name=?");
            ps.setString(1,factionName);
            ps.executeUpdate();

            ps.close();
            ps = null;

            for(Player member : onlineFactionMembers)
            {
                MainClass.classObjectMap.get(member.getUniqueId()).setFaction(null,null);
                member.sendMessage(MainClass.getPluginPrefix() + "Faction lideriniz, " + ChatColor.BLUE + p.getName() + ChatColor.WHITE + ", isimli oyuncu, üyesi olduğunuz " + ChatColor.YELLOW + factionName + ChatColor.WHITE + " adlı factionı sildi!" );
            }
            onlineFactionMembers.clear();
            Faction.config.getConfig().set("FactionDetails."+getFactionName(),null);
            Faction.config.saveConfig();
            if (claimConfig.getConfig().contains("faction_world"))
            {
                claimConfig.getConfig().getConfigurationSection("faction_world").getKeys(false).forEach(chunk-> {

                    String nameinConfig = claimConfig.getConfig().getString("faction_world."+chunk+".faction");
                    if(nameinConfig.equalsIgnoreCase(factionName))
                    {
                        claimConfig.getConfig().set("faction_world."+chunk+".faction",null);
                    }
                });
                claimConfig.saveConfig();
            }
            MainClass.getPlugin(MainClass.class).getConfig().set("FactionDetails." + factionName + ".claims",null);
            p.sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Faction silindi!");
            for(String allyFactionName : allyFactions.keySet())
            {
                Faction ally = factionMap.get(allyFactionName.toLowerCase());
                ally.removeFromAlly(this);
                ally.sendMessageToMembers(MainClass.getPluginPrefix() + "Dostunuz olan, " +ChatColor.BLUE + factionName + ChatColor.WHITE + " adlı faction lideri tarafından silindi! ");
            }
            for(String enemyFactionName : enemyFactions.keySet())
            {
                Faction enemy = factionMap.get(enemyFactionName.toLowerCase());
                enemy.removeFromEnemy(this);
                enemy.sendMessageToMembers(MainClass.getPluginPrefix() + "Düşmanınız olan, " +ChatColor.RED + factionName + ChatColor.WHITE + " adlı faction lideri tarafından silindi! ");
            }
            for(UUID uuid : applications.keySet())
            {
                Player applier = Bukkit.getPlayer(uuid);
                if(applier.isOnline())
                {
                    applier.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Başvurmuş olduğunuz, " + ChatColor.BLUE + getFactionName() + ChatColor.RED + " adlı lonca lideri tarafından silindiğinden dolayı, başvurunuz iptal edildi. ");
                }
                removeApplication(uuid);
            }
            applications.clear();
            MainClass.classObjectMap.get(p.getUniqueId()).setFaction(null,null);
            MainClass.factionMap.remove(getFactionName().toLowerCase());




        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void sendMessageToMembers(String message)
    {
        for(Player member : onlineFactionMembers)
        {
            member.sendMessage(message);
        }
    }

}
