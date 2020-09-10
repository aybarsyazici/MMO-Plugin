package minecraft.mmoplugin;

import minecraft.mmoplugin.events.PrivateSideBar;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;

public class MMOClass
{

    public enum  Jobs
    {
        Oduncu,
        Madenci,
        Çiftçi,
        Avcı,
        Demirci
    }

    Plugin plugin;
    Player classOwner;
    private double xp;
    private int level;
    private String className;
    private boolean xpAdjusted;
    private String faction;
    private Faction.ChatType chatType;
    private String currentDungeon;
    private Faction.Ranks rank;
    private HashMap<Faction, DateTime> applications;
    private int currency;
    private boolean isTrading;
    private Player tradeRequester;
    private boolean isUsingSignGUI;
    private BukkitTask tradeCountdowner;
    private Jobs job;
    private int jobLevel;
    private double jobXp;
    private BukkitTask tpRequest;
    private Player tpRequester;

    public Player getTpRequester() {
        return tpRequester;
    }

    public void setTpRequester(Player tpRequester) {
        this.tpRequester = tpRequester;
    }

    public int getJobLevel() {
        return jobLevel;
    }

    public void setJobLevel(int jobLevel) {
        this.jobLevel = jobLevel;
    }

    public double getJobXp() {
        return jobXp;
    }

    public void setJobXp(double jobXp) {
        this.jobXp = jobXp;
    }

    public Jobs getJob() {
        return job;
    }

    public void setJob(Jobs job) {
        this.job = job;
    }

    public BukkitTask getTradeCountdowner() {
        return tradeCountdowner;
    }

    public void setTradeCountdowner(BukkitTask tradeCountdowner) {
        this.tradeCountdowner = tradeCountdowner;
    }


    public boolean isUsingSignGUI() {
        return isUsingSignGUI;
    }

    public void setUsingSignGUI(boolean usingSignGUI) {
        isUsingSignGUI = usingSignGUI;
    }


    public Inventory getTradingInv() {
        return tradingInv;
    }

    public void setTradingInv(Inventory tradingInv) {
        this.tradingInv = tradingInv;
    }

    private Inventory tradingInv;

    public boolean isTrading() {
        return isTrading;
    }

    public void setTrading(boolean trading) {
        isTrading = trading;
    }

    public Player getTradeRequester() {
        return tradeRequester;
    }

    public void setTradeRequester(Player tradeRequester) {
        this.tradeRequester = tradeRequester;
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
    }

    public HashMap<Faction, DateTime> getApplications() {
        return applications;
    }

    public void setApplications(HashMap<Faction, DateTime> applications) {
        this.applications = applications;
    }

    public Faction.Ranks getRank() {
        return rank;
    }

    public void setRank(Faction.Ranks rank) {
        this.rank = rank;
    }

    public String getCurrentDungeon() {
        return currentDungeon;
    }

    public void setCurrentDungeon(String currentDungeon) {
        this.currentDungeon = currentDungeon;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction, Faction.Ranks rank)
    {
        String oldFaction = "";
        if(this.getFaction() != null)
        {
            oldFaction = this.getFaction();
        }
        int teamid = MainClass.playerTeamMap.get(classOwner.getUniqueId());
        Team team = MainClass.mainScoreboard.getTeam(Integer.toString(teamid));
        if(faction != null)
        {
            String suffixToBeSet = ChatColor.RED + " [" + faction + "]";
            switch (rank)
            {
                case ÜYE:
                    team.setSuffix(suffixToBeSet + ChatColor.GREEN + " ⭐");
                    break;
                case MEMUR:
                    team.setSuffix(suffixToBeSet + ChatColor.GREEN + " ⭐⭐");
                    break;
                case MODERATÖR:
                    team.setSuffix(suffixToBeSet + ChatColor.GREEN + " ⭐⭐⭐");
                    break;
                case ADMİN:
                    team.setSuffix(suffixToBeSet + ChatColor.GREEN + " ⭐⭐⭐⭐");
                    break;
                case KURUCU:
                    team.setSuffix(suffixToBeSet + ChatColor.GREEN + " ☠");
                    break;
            }
            MainClass.factionMap.get(faction.toLowerCase()).addAsOnlineFactionMember(classOwner);
            for(Faction f : applications.keySet())
            {
                if(f.getApplications().containsKey(classOwner.getUniqueId()))
                    f.removeApplication(classOwner.getUniqueId());
            }
            applications.clear();
        }
        else
            team.setSuffix("");

        this.rank = rank;


        this.faction = faction;
        plugin.saveConfig();

        PrivateSideBar.updateFaction(classOwner,oldFaction);

    }

    public boolean isXpAdjusted() {
        return xpAdjusted;
    }

    public void setXpAdjusted(boolean xpAdjusted) {
        this.xpAdjusted = xpAdjusted;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public double getXp()
    {
        return xp;
    }

    public void setXp(double xp)
    {
        this.xp = xp;
    }

    public void addCurrency(int amount)
    {
        this.currency += amount;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public Player getClassOwner() {
        return classOwner;
    }

    public void setClassOwner(Player player) {
        this.classOwner = player;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(MainClass plugin) {
        this.plugin = plugin;
    }

    public void giveEXP(double xp)
    {
        this.xp += xp;
    }

    public void levelup()
    {
        this.level++;
    }

    public Faction.ChatType getChatType() {
        return chatType;
    }

    public static void gainCurrency(Player player, int amount)
    {
        if(MainClass.classObjectMap.containsKey(player.getUniqueId()))
        {
            MMOClass temp = MainClass.classObjectMap.get(player.getUniqueId());
            int oldAmount = temp.getCurrency();
            temp.addCurrency(amount);
            PrivateSideBar.updateCurrency(player,oldAmount);
            player.sendMessage(MainClass.getPluginPrefix() + ChatColor.GOLD + amount + " para " + ChatColor.GREEN + " kazandınız! ");
        }
    }

    public void setChatType(Faction.ChatType chatType) {
        this.chatType = chatType;
    }

    public MMOClass(Plugin plugin, Player player, double xp, int level, String className) {
        this.plugin = plugin;
        this.classOwner = player;
        this.xp = xp;
        this.level = level;
        this.className = className;
        this.xpAdjusted = false;
        this.faction = null;
        this.chatType = Faction.ChatType.NORMAL;
        this.currentDungeon = "";
        this.applications = new HashMap<>();
        this.currency = 0;
        this.isTrading = false;
        this.tradeRequester = null;
        this.tradingInv = null;
        this.isUsingSignGUI = false;
        this.tradeCountdowner = null;
        this.job = null;
        this.jobLevel = 0;
        this.jobXp = 0;
        this.tpRequest = null;
        this.tpRequester = null;
    }

    public BukkitTask getTpRequest() {
        return tpRequest;
    }

    public void setTpRequest(BukkitTask tpRequest) {
        this.tpRequest = tpRequest;
    }

    public MMOClass(){}

    public static double getExpRequired(int level)
    {
        return (50*Math.pow(level,1.3));
    }

    public static int getNMSXp(int level)
    {
        if(level < 16)
        {
            return (2*level +7);
        }
        else if(level < 31)
        {
            return 5*level-38;
        }
        else
        {
            return 9*level -158;
        }
    }

    public static void adjustXPBar(Player player)
    {
        if(MainClass.classObjectMap.containsKey(player.getUniqueId()))
        {
            MMOClass mmoClass = MainClass.classObjectMap.get(player.getUniqueId());

            if (mmoClass.getLevel() < 100)
            {
                int NMSxpRequired = MMOClass.getNMSXp(mmoClass.getLevel());

                double MMOxpRequired = MMOClass.getExpRequired(mmoClass.getLevel());
                double MMOcurrentxp = mmoClass.getXp();

                float percentageOfXP = (float) (MMOcurrentxp / MMOxpRequired);

                //player.sendMessage(MainClass.getPluginPrefix() + "To be set is: " + percentageOfXP);

                MainClass.classObjectMap.get(player.getUniqueId()).setXpAdjusted(true);

                player.setExp(percentageOfXP);

                player.setLevel(mmoClass.getLevel());
            }
            else
            {
                player.setExp(1.0f);

                player.setLevel(100);
            }
        }
        else
        {
            player.setExp(0);

            player.setLevel(0);
        }
    }

    public static String getClassBasedColour(String className)
    {
        String toBeReturned = "";
        switch (className.toLowerCase())
        {
            case "necromancer":
                toBeReturned += ChatColor.LIGHT_PURPLE;
                break;
            case "cleric":
                toBeReturned += ChatColor.YELLOW;
                break;
            case "warrior":
                toBeReturned += ChatColor.BLUE;
                break;
        }
        return toBeReturned;
    }

    public static MMOClass checkIfLeveledUp(MMOClass mmoClass, Player player, Plugin plugin)
    {
        if(mmoClass.getXp() >= getExpRequired(mmoClass.getLevel()))
        {
            player.sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Seviye atladınız, seviyeniz artık: " + ChatColor.BLUE + Integer.toString(mmoClass.getLevel()+1));
            mmoClass.levelup();
            mmoClass.setXp(0);
            int teamid = MainClass.playerTeamMap.get(player.getUniqueId());
            MainClass.mainScoreboard.getTeam(Integer.toString(teamid)).setPrefix(ChatColor.WHITE + "LVL " + Integer.toString(mmoClass.getLevel()) + " " + getClassBasedColour(mmoClass.getClassName()) + (mmoClass.getClassName().equalsIgnoreCase("necromancer") ? "SUMMONER" : mmoClass.getClassName().toUpperCase()) + " ");
            Location loc = player.getLocation();
            World w = Bukkit.getWorld(player.getWorld().getName());
            if(mmoClass.getLevel() > 20)
            {
                if(mmoClass.getLevel() % 2 == 0)
                {
                    player.setHealthScale(mmoClass.getLevel());
                }
                else
                    player.setHealthScale(mmoClass.getLevel()-1);
            }
            int diameter = 3; //Diameter of the circle centered on loc
            plugin.getServer().getLogger().info("Starting to spawn fireworks...");
            BukkitTask task = new BukkitRunnable() {
                int leftToCancel = 4;
                @Override
                public void run() {
                    Location newLocation = loc.add(new Vector(Math.random()-0.5, 2, Math.random()-0.5).multiply(diameter));
                    assert w != null;

                    Firework fw = w.spawn(newLocation, Firework.class);
                    FireworkMeta fwMeta = fw.getFireworkMeta();
                    FireworkEffect.Builder builder = FireworkEffect.builder();


                    fwMeta.addEffect(builder.flicker(true).withColor(Color.AQUA).build());
                    fwMeta.addEffect(builder.trail(true).build());
                    fwMeta.addEffect(builder.withFade(Color.YELLOW).build());
                    fwMeta.addEffect(builder.with(FireworkEffect.Type.STAR).build());
                    fwMeta.setPower(1);
                    fw.setFireworkMeta(fwMeta);

                    leftToCancel--;
                    if(leftToCancel==0)
                    {
                        cancel();
                    }
                }
            }.runTaskTimer(plugin,0,10);
            sendLevelUpMessage(player, mmoClass.getClassName(), mmoClass.getLevel());
            ActionBarUtil.sendTitle(player,"",ChatColor.GREEN + "SEVİYE ATLADINIZ!",20,20,20);
            player.setExp(0);
        }
        return mmoClass;
    }

    public static void sendLevelUpMessage(Player player, String className, int level)
    {
        switch (className.toLowerCase())
        {
            case "cleric":
            {
                switch(level)
                {
                    case 25:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Light's Guidance" + ChatColor.WHITE + ", max gücüne ulaştı!");
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Purging Fire Talent'ı" + ChatColor.WHITE + " açıldı!");
                        break;
                    case 26:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Light Infusion " + ChatColor.WHITE + " yeteneği açıldı!");
                        break;
                    case 50:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Kalkan Talent'i " + ChatColor.WHITE + " açıldı. Saldırı gücün azaldı, fakat kalkanlar artık bonus zırh kazanıyor.");
                        break;
                    case 51:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Light's Will" + ChatColor.WHITE + " yeteneği açıldı!");
                        break;
                    case 75:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Light's Will" + ChatColor.WHITE + " max gücüne ulaştı!");
                        player.sendMessage(MainClass.getPluginPrefix() + "Artık canın %40'ın altına düşünce Güç ve Hız kazanırsın!");
                        break;
                    case 100:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Aura of Protection" + ChatColor.WHITE + " yeteneği açıldı!");
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "100LVL Talent'ı " + ChatColor.WHITE + "açıldı, artık ölmek üzereyken 5 saniye yaşamaya devam edebilirsin. Bu 5 saniyenin sonunda patlar ve etrafa hasar vurursun.");
                        break;
                }
                break;
            }
            case "warrior":
            {
                switch(level)
                {
                    case 20:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Sunder" + ChatColor.WHITE + ", max gücüne ulaştı!");
                        break;
                    case 25:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bloody Rage" + ChatColor.WHITE + ", yeteneği açıldı!");
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Balta Talent'ı" + ChatColor.WHITE + ", açıldı. Artık baltalar ile extra hasar vurursun.");
                        break;
                    case 45:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Bloody Rage" + ChatColor.WHITE + ", max gücüne ulaştı!");
                        break;
                    case 50:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Whirlwind" + ChatColor.WHITE + ", yeteneği açıldı! Artık bir kılıç ile sağ tıklayarak etrafında kılıç döndürebilirsin.");
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Wither Talent'ı" + ChatColor.WHITE + ", açıldı! Artık vuruşlarının wither efekti vurma şansı var.");
                        break;
                    case 70:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Whirlwind" + ChatColor.WHITE + ", yeteneği max gücüne ulaştı!");
                        break;
                    case 75:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Ancestors Strength" + ChatColor.WHITE + ", yeteneği açıldı! Off-Hand'ine kılıç veya balta almak seni güçlendirecektir!");
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Saldırı Hızı Talent'ı" + ChatColor.WHITE + ", açıldı. Artık canın azaldıkça saldırı hızı kazanırsın.");
                        break;
                    case 100:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "İnfazcı Talent'ı" + ChatColor.WHITE + ", açıldı! Artık %15 canın altına indirdiklerin otomatik olarak ölür, bunun üstüne hareket hızı kazanırsın.");
                        break;
                }
                break;
            }
            case "necromancer":
            {
                switch (level)
                {
                    case 25:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Hareket Hızı talent'ı" + ChatColor.WHITE + ", açıldı! Summon'ın vuruşları, artık size hareket hızı verir.");
                        break;
                    case 26:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "İskelet" + ChatColor.WHITE + " summon'ı açıldı!");
                        break;
                    case 50:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Zehir talent'ı" + ChatColor.WHITE + " açıldı! Artık Summon'ınızın vuruşları zehir ve yavaşlatma uygular.");
                        break;
                    case 51:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Wither İskeleti" + ChatColor.WHITE + " summon'ı açıldı!");
                        break;
                    case 75:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Ekipman talent'ı" + ChatColor.WHITE + " açıldı! Artık Summon'ınıza sağ tıklayarak ekipman giydirebilirsiniz.");
                        break;
                    case 76:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Atlı Wither İskeleti" + ChatColor.WHITE + " summon'ı açıldı!");
                        break;
                    case 100:
                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Seviye 100 talent'ı" + ChatColor.WHITE + " açıldı! Artık ölecek olursanız, summon'ınınız kendini feda ederek, canınızı fuller.");
                        break;
                }
                break;
            }
        }
    }

    public void reduceCurrency(int amount)
    {
        int oldAmount = this.getCurrency();
        this.currency -= amount;
        PrivateSideBar.updateCurrency(classOwner,oldAmount);
        classOwner.sendMessage(MainClass.getPluginPrefix() + ChatColor.GOLD + amount + " para " + ChatColor.LIGHT_PURPLE + " kaybettiniz! ");
    }

    public static void transferMoney(MMOClass transferer, MMOClass transfee, int amount)
    {
        transferer.reduceCurrency(amount);
        int oldAmount = transfee.getCurrency();
        transfee.addCurrency(amount);
        PrivateSideBar.updateCurrency(transfee.getClassOwner(),oldAmount);
        transfee.classOwner.sendMessage(MainClass.getPluginPrefix() + ChatColor.GOLD + amount + " para " + ChatColor.GREEN + " kazandınız! ");
    }

    public static void gainXP(Player player, double xp, Plugin plugin)
    {
        MMOClass mmoClass = MainClass.classObjectMap.get(player.getUniqueId());
        if (mmoClass.getLevel() < 100)
        {
            int oldLevel = mmoClass.getLevel();
            double oldXp = mmoClass.getXp();
            player.sendMessage(MainClass.getPluginPrefix() + ChatColor.GOLD + "" + ChatColor.BOLD + String.format("%.2f",xp) + "xp" + ChatColor.GREEN + " kazandınız!");
            mmoClass.giveEXP(xp);
            mmoClass = MMOClass.checkIfLeveledUp(mmoClass,player,plugin);
            MainClass.classObjectMap.put(player.getUniqueId(),mmoClass);

            MMOClass.adjustXPBar(player);

            PrivateSideBar.updateLevelAndXP(player, oldLevel, oldXp);
        }
    }

    public static boolean ifSameFaction(Entity damager, Entity damagee) //THIS RETURNS TRUE IF BOTH PLAYERS ARE IN THE SAME FACTION OR ARE IN ALLIED FACTIONS!
    {
        if(damager instanceof Player && damagee instanceof Player)
        {
            if(MainClass.classObjectMap.containsKey(damager.getUniqueId()) && MainClass.classObjectMap.containsKey(damagee.getUniqueId()))
            {
                MMOClass damagerClass = MainClass.classObjectMap.get(damager.getUniqueId());
                MMOClass damageeClass = MainClass.classObjectMap.get(damagee.getUniqueId());
                if(damagerClass.getFaction() != null && damageeClass.getFaction() != null)
                {
                    if(damageeClass.getFaction().equalsIgnoreCase(damagerClass.getFaction()))
                    {
                        return true;
                    }
                    Faction damageeFaction = MainClass.factionMap.get(damageeClass.getFaction().toLowerCase());
                    Faction damagerFaction = MainClass.factionMap.get(damagerClass.getFaction().toLowerCase());
                    if(damagerFaction.getAllyFactions().containsKey(damageeFaction.getFactionName().toLowerCase()))
                    {
                        return true;
                    }
                }
            }
        }
        else if(damager instanceof Player && damagee instanceof CraftEntity && ((CraftEntity)damagee).getHandle() instanceof Necromancer.Summon)
        {
            Necromancer.Summon summon = (Necromancer.Summon) (((CraftEntity)damagee).getHandle());
            if(MainClass.classObjectMap.containsKey(damager.getUniqueId()))
            {
                MMOClass damagerClass = MainClass.classObjectMap.get(damager.getUniqueId());
                MMOClass damageeClass = MainClass.classObjectMap.get(summon.getOwner().getUniqueId());
                if(damagerClass.getFaction() != null && damageeClass.getFaction() != null)
                {
                    if(damageeClass.getFaction().equalsIgnoreCase(damagerClass.getFaction()))
                    {
                        return true;
                    }
                    Faction damageeFaction = MainClass.factionMap.get(damageeClass.getFaction().toLowerCase());
                    Faction damagerFaction = MainClass.factionMap.get(damagerClass.getFaction().toLowerCase());
                    if(damagerFaction.getAllyFactions().containsKey(damageeFaction.getFactionName().toLowerCase()))
                    {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    public static double damageCalculation(double damage, double armor, double toughness)
    {
        double armorPen = (toughness/(double)4 +2);
        armor = armor - damage/armorPen;
        double aboveTheDivision = Math.min(20,Math.max(armor/(double) 5,armor-damage/(2+toughness/(double) 4)));
        damage = damage * (1-aboveTheDivision/(double)25);
        return damage;
    }

    public static boolean currentlyAtWar(Player damager, Player damagee)
    {
        if(MainClass.classObjectMap.containsKey(damager.getUniqueId()) && MainClass.classObjectMap.containsKey(damagee.getUniqueId()))
        {
            MMOClass damagerClass = MainClass.classObjectMap.get(damager.getUniqueId());
            MMOClass damageeClass = MainClass.classObjectMap.get(damagee.getUniqueId());
            if(damagerClass.getFaction() != null && damageeClass.getFaction() != null)
            {
                String damagerFactionName = damagerClass.getFaction();
                String damageeFactionName = damageeClass.getFaction();

                Faction damagerFaction = MainClass.factionMap.get(damagerFactionName.toLowerCase());
                Faction damageeFaction = MainClass.factionMap.get(damageeFactionName.toLowerCase());

                if(!damagerFactionName.equalsIgnoreCase(damageeFactionName) && damagerFaction.getEnemies().containsKey(damageeFactionName.toLowerCase()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean ifStrictlySameFaction(Entity damager, Entity damagee) //THIS ONLY RETURNS TRUE IF BOTH PLAYERS ARE IN THE SAME FACTION!
    {
        if(damager instanceof Player && damagee instanceof Player)
        {
            if(MainClass.classObjectMap.containsKey(damager.getUniqueId()) && MainClass.classObjectMap.containsKey(damagee.getUniqueId()))
            {
                MMOClass damagerClass = MainClass.classObjectMap.get(damager.getUniqueId());
                MMOClass damageeClass = MainClass.classObjectMap.get(damagee.getUniqueId());
                if(damagerClass.getFaction() != null && damageeClass.getFaction() != null)
                {
                    if(damageeClass.getFaction().equalsIgnoreCase(damagerClass.getFaction()))
                    {
                        return true;
                    }
                }
            }
        }
        else if(damager instanceof Player && damagee instanceof CraftEntity && ((CraftEntity)damagee).getHandle() instanceof Necromancer.Summon)
        {
            Necromancer.Summon summon = (Necromancer.Summon) (((CraftEntity)damagee).getHandle());
            if(MainClass.classObjectMap.containsKey(damager.getUniqueId()))
            {
                MMOClass damagerClass = MainClass.classObjectMap.get(damager.getUniqueId());
                MMOClass damageeClass = MainClass.classObjectMap.get(summon.getOwner().getUniqueId());
                if(damagerClass.getFaction() != null && damageeClass.getFaction() != null)
                {
                    if(damageeClass.getFaction().equalsIgnoreCase(damagerClass.getFaction()))
                    {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    public static void confirmTrade(Inventory inv)
    {
        CustomInventory.TradingInventoryHolder holder = (CustomInventory.TradingInventoryHolder) inv.getHolder();
        Player player1 = holder.getPlayer1();
        Player player2 = holder.getPlayer2();

        MMOClass player1Class = MainClass.classObjectMap.get(player1.getUniqueId());
        player1Class.setTradingInv(null);
        player1Class.setTrading(false);
        player1Class.setTradeRequester(null);

        MMOClass player2Class = MainClass.classObjectMap.get(player2.getUniqueId());
        player2Class.setTradingInv(null);
        player2Class.setTrading(false);
        player2Class.setTradeRequester(null);


        int itemCounter = 0;
        for(int i = 10; i < 40; i++)
        {
            if(i==13)
            {
                i = 18;
                continue;
            }
            else if(i==22)
            {
                i = 27;
                continue;
            }
            else if(i == 31)
            {
                i = 36;
                continue;
            }
            ItemStack toBeGiven = inv.getItem(i);
            if (toBeGiven != null)
            {
                itemCounter++;
                if (player2.isOnline())
                {
                    player2.getInventory().addItem(toBeGiven);
                }
                else
                {
                    CustomInventory.tradeConfig.getConfig().set(player2.getUniqueId() + "." + itemCounter,toBeGiven);
                }
            }
        }

        ItemStack player1Sign = inv.getItem(48);
        ItemMeta player1SignMeta = player1Sign.getItemMeta();
        List<String> lore = player1SignMeta.getLore();
        if(lore.size() > 0)
        {
            String currencyToBeGiven = lore.get(0);
            int amount = Integer.parseInt(currencyToBeGiven);
            MMOClass.transferMoney(player1Class,player2Class,amount);
        }

        lore.clear();

        for(int i = 14; i < 44; i++)
        {
            if(i == 16)
            {
                i = 22;
                continue;
            }
            else if(i == 26)
            {
                i = 31;
                continue;
            }
            else if(i == 35)
            {
                i = 40;
                continue;
            }
            ItemStack toBeGiven = inv.getItem(i);
            if (toBeGiven != null)
            {
                itemCounter++;
                if (player1.isOnline())
                {
                    player1.getInventory().addItem(toBeGiven);
                }
                else
                {
                    CustomInventory.tradeConfig.getConfig().set(player1.getUniqueId() + "." + itemCounter,toBeGiven);
                }
            }
        }

        ItemStack player2Sign = inv.getItem(52);
        ItemMeta player2SignMeta = player2Sign.getItemMeta();
        lore = player2SignMeta.getLore();
        if(lore.size() > 0)
        {
            String currencyToBeGiven = lore.get(0);
            int amount = Integer.parseInt(currencyToBeGiven);
            MMOClass.transferMoney(player2Class,player1Class,amount);
        }


        if(player1.isOnline())
            player1.sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Takas başarıyla gerçekleşti!");
        if(player2.isOnline())
            player2.sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Takas başarıyla gerçekleşti!");

        if(player1.getOpenInventory().getTitle().contains("Takas Menüsü"))
        {
            MainClass.classObjectMap.get(player1.getUniqueId()).setUsingSignGUI(true);
            player1.closeInventory();
        }

        if(player2.getOpenInventory().getTitle().contains("Takas Menüsü"))
        {
            MainClass.classObjectMap.get(player2.getUniqueId()).setUsingSignGUI(true);
            player2.closeInventory();
        }
    }

    public static void cancelTrade(Inventory inv)
    {
        CustomInventory.TradingInventoryHolder holder = (CustomInventory.TradingInventoryHolder) inv.getHolder();
        Player player1 = holder.getPlayer1();
        Player player2 = holder.getPlayer2();

        if(MainClass.classObjectMap.containsKey(player1.getUniqueId()))
        {
            MMOClass closerClass = MainClass.classObjectMap.get(player1.getUniqueId());
            closerClass.setTradingInv(null);
            closerClass.setTrading(false);
            closerClass.setTradeRequester(null);
        }
        if (MainClass.classObjectMap.containsKey(player2.getUniqueId()))
        {
            MMOClass player2Class = MainClass.classObjectMap.get(player2.getUniqueId());
            player2Class.setTradingInv(null);
            player2Class.setTrading(false);
            player2Class.setTradeRequester(null);
        }
        int itemCounter = 0;
        for(int i = 10; i < 40; i++)
        {
            if(i==13)
            {
                i = 18;
                continue;
            }
            else if(i==22)
            {
                i = 27;
                continue;
            }
            else if(i == 31)
            {
                i = 36;
                continue;
            }
            ItemStack toBeGiven = inv.getItem(i);
            if (toBeGiven != null)
            {
                itemCounter++;
                if (player1.isOnline())
                {
                    player1.getInventory().addItem(toBeGiven);
                }
                else
                {
                    CustomInventory.tradeConfig.getConfig().set(player1.getUniqueId() + "." + itemCounter,toBeGiven);
                }
            }
        }
        for(int i = 14; i < 44; i++)
        {
            if(i == 16)
            {
                i = 22;
                continue;
            }
            else if(i == 26)
            {
                i = 31;
                continue;
            }
            else if(i == 35)
            {
                i = 40;
                continue;
            }
            ItemStack toBeGiven = inv.getItem(i);
            if (toBeGiven != null)
            {
                itemCounter++;
                if (player2.isOnline())
                {
                    player2.getInventory().addItem(toBeGiven);
                }
                else
                {
                    CustomInventory.tradeConfig.getConfig().set(player2.getUniqueId() + "." + itemCounter,toBeGiven);
                }
            }
        }
        if(player1.getOpenInventory().getTitle().contains("Takas Menüsü"))
        {
            MainClass.classObjectMap.get(player1.getUniqueId()).setUsingSignGUI(true);
            player1.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        }
        if(player2.getOpenInventory().getTitle().contains("Takas Menüsü"))
        {
            MainClass.classObjectMap.get(player2.getUniqueId()).setUsingSignGUI(true);
            player2.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        }

        if(player1.isOnline())
            player1.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Takas iptal oldu!");
        if(player2.isOnline())
            player2.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Takas iptal oldu.");

    }

    public static boolean canUseSkills(String worldName)
    {
        switch (worldName.toLowerCase())
        {
            case "main_hub":
            case "world":
            case "market_place":
            case "paladin_hub":
            case "summoner_hub":
            case "warrior_hub":
                return false;
            default:
                return true;
        }
    }

}
