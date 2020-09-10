package minecraft.mmoplugin.events;

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardDisplayObjective;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardObjective;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardScore;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import minecraft.mmoplugin.MMOClass;
import minecraft.mmoplugin.MainClass;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PrivateSideBar
{
    public static void createPrivateSideBar(Player player)
    {
        MMOClass mmoClass = MainClass.classObjectMap.get(player.getUniqueId());
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        //packet.getIntegers().write(0,0);
        WrapperPlayServerScoreboardObjective scoreboardObjective = new WrapperPlayServerScoreboardObjective();
        scoreboardObjective.setDisplayName(WrappedChatComponent.fromText(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Class Bilgileri"));
        scoreboardObjective.setMode(0);
        scoreboardObjective.setName("ClassInfo");
        scoreboardObjective.setHealthDisplay(WrapperPlayServerScoreboardObjective.HealthDisplay.INTEGER);

        WrapperPlayServerScoreboardDisplayObjective displayObjective = new WrapperPlayServerScoreboardDisplayObjective();
        displayObjective.setPosition(1);
        displayObjective.setScoreName("ClassInfo");

        WrapperPlayServerScoreboardScore score = new WrapperPlayServerScoreboardScore();
        score.setObjectiveName("ClassInfo");
        score.setScoreName(ChatColor.GOLD + "Classınız: " + ChatColor.GREEN + (mmoClass.getClassName().equalsIgnoreCase("necromancer") ? "SUMMONER" : mmoClass.getClassName().toUpperCase()));
        score.setValue(19);
        score.setScoreboardAction(EnumWrappers.ScoreboardAction.CHANGE);

        WrapperPlayServerScoreboardScore score2 = new WrapperPlayServerScoreboardScore();
        score2.setObjectiveName("ClassInfo");
        score2.setScoreName("");
        score2.setValue(18);

        WrapperPlayServerScoreboardScore score3 = new WrapperPlayServerScoreboardScore();
        score3.setObjectiveName("ClassInfo");
        score3.setScoreName(ChatColor.GOLD + "Level: " + ChatColor.GREEN + mmoClass.getLevel());
        score3.setValue(17);

        WrapperPlayServerScoreboardScore score4 = new WrapperPlayServerScoreboardScore();
        score4.setObjectiveName("ClassInfo");
        score4.setScoreName(" ");
        score4.setValue(16);

        WrapperPlayServerScoreboardScore score5 = new WrapperPlayServerScoreboardScore();
        score5.setObjectiveName("ClassInfo");
        score5.setScoreName(ChatColor.GOLD + "EXP: " + ChatColor.GREEN + String.format("%.2f",mmoClass.getXp()));
        score5.setValue(15);

        WrapperPlayServerScoreboardScore score6 = new WrapperPlayServerScoreboardScore();
        score6.setObjectiveName("ClassInfo");
        score6.setScoreName("  ");
        score6.setValue(14);

        WrapperPlayServerScoreboardScore job = new WrapperPlayServerScoreboardScore();
        job.setObjectiveName("ClassInfo");
        job.setScoreName(ChatColor.GOLD + "İşiniz: " + ChatColor.GREEN + mmoClass.getJob());
        job.setValue(13);

        WrapperPlayServerScoreboardScore jobSpace1 = new WrapperPlayServerScoreboardScore();
        jobSpace1.setObjectiveName("ClassInfo");
        jobSpace1.setScoreName("   ");
        jobSpace1.setValue(12);

        WrapperPlayServerScoreboardScore jobLevel = new WrapperPlayServerScoreboardScore();
        jobLevel.setObjectiveName("ClassInfo");
        jobLevel.setScoreName(ChatColor.GOLD + "İş seviyesi: " + ChatColor.GREEN + mmoClass.getJobLevel());
        jobLevel.setValue(11);

        WrapperPlayServerScoreboardScore jobSpace2 = new WrapperPlayServerScoreboardScore();
        jobSpace2.setObjectiveName("ClassInfo");
        jobSpace2.setScoreName("    ");
        jobSpace2.setValue(10);

        WrapperPlayServerScoreboardScore jobXP = new WrapperPlayServerScoreboardScore();
        jobXP.setObjectiveName("ClassInfo");
        jobXP.setScoreName(ChatColor.GOLD + "İş XPsi: " + ChatColor.GREEN + mmoClass.getJobXp());
        jobXP.setValue(9);

        WrapperPlayServerScoreboardScore jobSpace3 = new WrapperPlayServerScoreboardScore();
        jobSpace3.setObjectiveName("ClassInfo");
        jobSpace3.setScoreName("     ");
        jobSpace3.setValue(8);

        WrapperPlayServerScoreboardScore score8 = new WrapperPlayServerScoreboardScore();
        score8.setObjectiveName("ClassInfo");
        score8.setScoreName(ChatColor.GOLD + "Para: " + ChatColor.YELLOW + mmoClass.getCurrency());
        score8.setValue(7);

        WrapperPlayServerScoreboardScore score7 = new WrapperPlayServerScoreboardScore();
        score7.setObjectiveName("ClassInfo");
        score7.setScoreName("      ");
        score7.setValue(6);

        String faction = "";
        if (mmoClass.getFaction() != null) {
            faction = mmoClass.getFaction();
        }

        WrapperPlayServerScoreboardScore score9 = new WrapperPlayServerScoreboardScore();;
        score9.setObjectiveName("ClassInfo");
        score9.setScoreName(ChatColor.GOLD + "Faction: " + ChatColor.RED + faction);
        score9.setValue(5);

        WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();

        scoreboardObjective.sendPacket(player);
        displayObjective.sendPacket(player);
        score.sendPacket(player);
        score2.sendPacket(player);
        score3.sendPacket(player);
        score4.sendPacket(player);
        score5.sendPacket(player);
        score6.sendPacket(player);
        score7.sendPacket(player);
        score8.sendPacket(player);
        score9.sendPacket(player);
        job.sendPacket(player);
        jobLevel.sendPacket(player);
        jobSpace1.sendPacket(player);
        jobSpace2.sendPacket(player);
        jobSpace3.sendPacket(player);
        jobXP.sendPacket(player);
        team.sendPacket(player);
    }

    public static void updateLevelAndXP(Player player, int oldLevel, double oldXp)
    {
        MMOClass mmoClass = MainClass.classObjectMap.get(player.getUniqueId());

        WrapperPlayServerScoreboardObjective scoreboardObjective = new WrapperPlayServerScoreboardObjective();
        scoreboardObjective.setDisplayName(WrappedChatComponent.fromText(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Class Bilgileri"));
        scoreboardObjective.setMode(2);


        WrapperPlayServerScoreboardDisplayObjective displayObjective = new WrapperPlayServerScoreboardDisplayObjective();
        displayObjective.setPosition(1);
        displayObjective.setScoreName("ClassInfo");
        scoreboardObjective.setName("ClassInfo");
        scoreboardObjective.setHealthDisplay(WrapperPlayServerScoreboardObjective.HealthDisplay.INTEGER);

        WrapperPlayServerScoreboardScore score = new WrapperPlayServerScoreboardScore();
        score.setObjectiveName("ClassInfo");
        score.setScoreName(ChatColor.GOLD + "Classınız: " + ChatColor.GREEN + (mmoClass.getClassName().equalsIgnoreCase("necromancer") ? "SUMMONER" : mmoClass.getClassName().toUpperCase()));
        score.setValue(19);
        score.setScoreboardAction(EnumWrappers.ScoreboardAction.CHANGE);

        WrapperPlayServerScoreboardScore score2 = new WrapperPlayServerScoreboardScore();
        score2.setObjectiveName("ClassInfo");
        score2.setScoreName("");
        score2.setValue(18);

        WrapperPlayServerScoreboardScore oldscore3 = new WrapperPlayServerScoreboardScore();
        oldscore3.setObjectiveName("ClassInfo");
        oldscore3.setScoreName(ChatColor.GOLD + "Level: " + ChatColor.GREEN + oldLevel);
        oldscore3.setValue(17);
        oldscore3.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        WrapperPlayServerScoreboardScore score3 = new WrapperPlayServerScoreboardScore();
        score3.setObjectiveName("ClassInfo");
        score3.setScoreName(ChatColor.GOLD + "Level: " + ChatColor.GREEN + mmoClass.getLevel());
        score3.setValue(17);

        WrapperPlayServerScoreboardScore score4 = new WrapperPlayServerScoreboardScore();
        score4.setObjectiveName("ClassInfo");
        score4.setScoreName(" ");
        score4.setValue(16);

        WrapperPlayServerScoreboardScore oldscore5 = new WrapperPlayServerScoreboardScore();
        oldscore5.setObjectiveName("ClassInfo");
        oldscore5.setScoreName(ChatColor.GOLD + "EXP: " + ChatColor.GREEN + String.format("%.2f",oldXp));
        oldscore5.setValue(15);
        oldscore5.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        WrapperPlayServerScoreboardScore score5 = new WrapperPlayServerScoreboardScore();
        score5.setObjectiveName("ClassInfo");
        score5.setScoreName(ChatColor.GOLD + "EXP: " + ChatColor.GREEN + String.format("%.2f",mmoClass.getXp()));
        score5.setValue(15);

        WrapperPlayServerScoreboardScore score6 = new WrapperPlayServerScoreboardScore();
        score6.setObjectiveName("ClassInfo");
        score6.setScoreName("  ");
        score6.setValue(14);

        WrapperPlayServerScoreboardScore job = new WrapperPlayServerScoreboardScore();
        job.setObjectiveName("ClassInfo");
        job.setScoreName(ChatColor.GOLD + "İşiniz: " + ChatColor.GREEN + mmoClass.getJob());
        job.setValue(13);

        WrapperPlayServerScoreboardScore jobSpace1 = new WrapperPlayServerScoreboardScore();
        jobSpace1.setObjectiveName("ClassInfo");
        jobSpace1.setScoreName("   ");
        jobSpace1.setValue(12);

        WrapperPlayServerScoreboardScore jobLevel = new WrapperPlayServerScoreboardScore();
        jobLevel.setObjectiveName("ClassInfo");
        jobLevel.setScoreName(ChatColor.GOLD + "İş seviyesi: " + ChatColor.GREEN + mmoClass.getJobLevel());
        jobLevel.setValue(11);

        WrapperPlayServerScoreboardScore jobSpace2 = new WrapperPlayServerScoreboardScore();
        jobSpace2.setObjectiveName("ClassInfo");
        jobSpace2.setScoreName("    ");
        jobSpace2.setValue(10);

        WrapperPlayServerScoreboardScore jobXP = new WrapperPlayServerScoreboardScore();
        jobXP.setObjectiveName("ClassInfo");
        jobXP.setScoreName(ChatColor.GOLD + "İş XPsi: " + ChatColor.GREEN + mmoClass.getJobXp());
        jobXP.setValue(9);

        WrapperPlayServerScoreboardScore jobSpace3 = new WrapperPlayServerScoreboardScore();
        jobSpace3.setObjectiveName("ClassInfo");
        jobSpace3.setScoreName("     ");
        jobSpace3.setValue(8);


        WrapperPlayServerScoreboardScore score8 = new WrapperPlayServerScoreboardScore();
        score8.setObjectiveName("ClassInfo");
        score8.setScoreName(ChatColor.GOLD + "Para: " + ChatColor.YELLOW + mmoClass.getCurrency());
        score8.setValue(7);

        String faction = "";
        if (mmoClass.getFaction() != null) {
            faction = mmoClass.getFaction();
        }

        WrapperPlayServerScoreboardScore score9 = new WrapperPlayServerScoreboardScore();
        score9.setObjectiveName("ClassInfo");
        score9.setScoreName("      ");
        score9.setValue(6);

        WrapperPlayServerScoreboardScore score7 = new WrapperPlayServerScoreboardScore();
        score7.setObjectiveName("ClassInfo");
        score7.setScoreName(ChatColor.GOLD + "Faction: " + ChatColor.RED + faction);
        score7.setValue(5);

        //WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();

        scoreboardObjective.sendPacket(player);
        displayObjective.sendPacket(player);
        score.sendPacket(player);
        score2.sendPacket(player);
        oldscore3.sendPacket(player);
        score3.sendPacket(player);
        score4.sendPacket(player);
        oldscore5.sendPacket(player);
        score5.sendPacket(player);
        score6.sendPacket(player);
        score7.sendPacket(player);
        score8.sendPacket(player);
        score9.sendPacket(player);
        job.sendPacket(player);
        jobLevel.sendPacket(player);
        jobXP.sendPacket(player);
        jobSpace1.sendPacket(player);
        jobSpace2.sendPacket(player);
        jobSpace3.sendPacket(player);
        //team.sendPacket(player);
    }

    public static void updateFaction(Player player, String oldFaction)
    {
        MMOClass mmoClass = MainClass.classObjectMap.get(player.getUniqueId());
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        //packet.getIntegers().write(0,0);
        WrapperPlayServerScoreboardObjective scoreboardObjective = new WrapperPlayServerScoreboardObjective();
        scoreboardObjective.setDisplayName(WrappedChatComponent.fromText(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Class Bilgileri"));
        scoreboardObjective.setMode(0);
        scoreboardObjective.setName("ClassInfo");
        scoreboardObjective.setHealthDisplay(WrapperPlayServerScoreboardObjective.HealthDisplay.INTEGER);

        WrapperPlayServerScoreboardDisplayObjective displayObjective = new WrapperPlayServerScoreboardDisplayObjective();
        displayObjective.setPosition(1);
        displayObjective.setScoreName("ClassInfo");

        WrapperPlayServerScoreboardScore score = new WrapperPlayServerScoreboardScore();
        score.setObjectiveName("ClassInfo");
        score.setScoreName(ChatColor.GOLD + "Classınız: " + ChatColor.GREEN + (mmoClass.getClassName().equalsIgnoreCase("necromancer") ? "SUMMONER" : mmoClass.getClassName().toUpperCase()));
        score.setValue(19);
        score.setScoreboardAction(EnumWrappers.ScoreboardAction.CHANGE);

        WrapperPlayServerScoreboardScore score2 = new WrapperPlayServerScoreboardScore();
        score2.setObjectiveName("ClassInfo");
        score2.setScoreName("");
        score2.setValue(18);

        WrapperPlayServerScoreboardScore score3 = new WrapperPlayServerScoreboardScore();
        score3.setObjectiveName("ClassInfo");
        score3.setScoreName(ChatColor.GOLD + "Level: " + ChatColor.GREEN + mmoClass.getLevel());
        score3.setValue(17);

        WrapperPlayServerScoreboardScore score4 = new WrapperPlayServerScoreboardScore();
        score4.setObjectiveName("ClassInfo");
        score4.setScoreName(" ");
        score4.setValue(16);

        WrapperPlayServerScoreboardScore score5 = new WrapperPlayServerScoreboardScore();
        score5.setObjectiveName("ClassInfo");
        score5.setScoreName(ChatColor.GOLD + "EXP: " + ChatColor.GREEN + String.format("%.2f",mmoClass.getXp()));
        score5.setValue(15);

        WrapperPlayServerScoreboardScore score6 = new WrapperPlayServerScoreboardScore();
        score6.setObjectiveName("ClassInfo");
        score6.setScoreName("  ");
        score6.setValue(14);

        WrapperPlayServerScoreboardScore job = new WrapperPlayServerScoreboardScore();
        job.setObjectiveName("ClassInfo");
        job.setScoreName(ChatColor.GOLD + "İşiniz: " + ChatColor.GREEN + mmoClass.getJob());
        job.setValue(13);

        WrapperPlayServerScoreboardScore jobSpace1 = new WrapperPlayServerScoreboardScore();
        jobSpace1.setObjectiveName("ClassInfo");
        jobSpace1.setScoreName("   ");
        jobSpace1.setValue(12);

        WrapperPlayServerScoreboardScore jobLevel = new WrapperPlayServerScoreboardScore();
        jobLevel.setObjectiveName("ClassInfo");
        jobLevel.setScoreName(ChatColor.GOLD + "İş seviyesi: " + ChatColor.GREEN + mmoClass.getJobLevel());
        jobLevel.setValue(11);

        WrapperPlayServerScoreboardScore jobSpace2 = new WrapperPlayServerScoreboardScore();
        jobSpace2.setObjectiveName("ClassInfo");
        jobSpace2.setScoreName("    ");
        jobSpace2.setValue(10);

        WrapperPlayServerScoreboardScore jobXP = new WrapperPlayServerScoreboardScore();
        jobXP.setObjectiveName("ClassInfo");
        jobXP.setScoreName(ChatColor.GOLD + "İş XPsi: " + ChatColor.GREEN + mmoClass.getJobXp());
        jobXP.setValue(9);

        WrapperPlayServerScoreboardScore jobSpace3 = new WrapperPlayServerScoreboardScore();
        jobSpace3.setObjectiveName("ClassInfo");
        jobSpace3.setScoreName("     ");
        jobSpace3.setValue(8);

        WrapperPlayServerScoreboardScore score8 = new WrapperPlayServerScoreboardScore();
        score8.setObjectiveName("ClassInfo");
        score8.setScoreName(ChatColor.GOLD + "Para: " + ChatColor.YELLOW + mmoClass.getCurrency());
        score8.setValue(7);

        WrapperPlayServerScoreboardScore score9 = new WrapperPlayServerScoreboardScore();
        score9.setObjectiveName("ClassInfo");
        score9.setScoreName("      ");
        score9.setValue(6);

        String faction = "";
        if (mmoClass.getFaction() != null) {
            faction = mmoClass.getFaction();
        }

        WrapperPlayServerScoreboardScore oldscore7 = new WrapperPlayServerScoreboardScore();;
        oldscore7.setObjectiveName("ClassInfo");
        oldscore7.setScoreName(ChatColor.GOLD + "Faction: " + ChatColor.RED + oldFaction);
        oldscore7.setValue(5);
        oldscore7.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        WrapperPlayServerScoreboardScore score7 = new WrapperPlayServerScoreboardScore();
        score7.setObjectiveName("ClassInfo");
        score7.setScoreName(ChatColor.GOLD + "Faction: " + ChatColor.RED + faction);
        score7.setValue(5);

        //WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();

        scoreboardObjective.sendPacket(player);
        displayObjective.sendPacket(player);
        score.sendPacket(player);
        score2.sendPacket(player);
        score3.sendPacket(player);
        score4.sendPacket(player);
        score5.sendPacket(player);
        score6.sendPacket(player);
        oldscore7.sendPacket(player);
        score7.sendPacket(player);
        score8.sendPacket(player);
        score9.sendPacket(player);
        job.sendPacket(player);
        jobLevel.sendPacket(player);
        jobXP.sendPacket(player);
        jobSpace1.sendPacket(player);
        jobSpace2.sendPacket(player);
        jobSpace3.sendPacket(player);
    }

    public static void updateCurrency(Player player, int oldAmount)
    {
        MMOClass mmoClass = MainClass.classObjectMap.get(player.getUniqueId());
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        //packet.getIntegers().write(0,0);
        WrapperPlayServerScoreboardObjective scoreboardObjective = new WrapperPlayServerScoreboardObjective();
        scoreboardObjective.setDisplayName(WrappedChatComponent.fromText(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Class Bilgileri"));
        scoreboardObjective.setMode(0);
        scoreboardObjective.setName("ClassInfo");
        scoreboardObjective.setHealthDisplay(WrapperPlayServerScoreboardObjective.HealthDisplay.INTEGER);

        WrapperPlayServerScoreboardDisplayObjective displayObjective = new WrapperPlayServerScoreboardDisplayObjective();
        displayObjective.setPosition(1);
        displayObjective.setScoreName("ClassInfo");

        WrapperPlayServerScoreboardScore score = new WrapperPlayServerScoreboardScore();
        score.setObjectiveName("ClassInfo");
        score.setScoreName(ChatColor.GOLD + "Classınız: " + ChatColor.GREEN + (mmoClass.getClassName().equalsIgnoreCase("necromancer") ? "SUMMONER" : mmoClass.getClassName().toUpperCase()));
        score.setValue(19);
        score.setScoreboardAction(EnumWrappers.ScoreboardAction.CHANGE);

        WrapperPlayServerScoreboardScore score2 = new WrapperPlayServerScoreboardScore();
        score2.setObjectiveName("ClassInfo");
        score2.setScoreName("");
        score2.setValue(18);

        WrapperPlayServerScoreboardScore score3 = new WrapperPlayServerScoreboardScore();
        score3.setObjectiveName("ClassInfo");
        score3.setScoreName(ChatColor.GOLD + "Level: " + ChatColor.GREEN + mmoClass.getLevel());
        score3.setValue(17);

        WrapperPlayServerScoreboardScore score4 = new WrapperPlayServerScoreboardScore();
        score4.setObjectiveName("ClassInfo");
        score4.setScoreName(" ");
        score4.setValue(16);

        WrapperPlayServerScoreboardScore score5 = new WrapperPlayServerScoreboardScore();
        score5.setObjectiveName("ClassInfo");
        score5.setScoreName(ChatColor.GOLD + "EXP: " + ChatColor.GREEN + String.format("%.2f",mmoClass.getXp()));
        score5.setValue(15);

        WrapperPlayServerScoreboardScore score6 = new WrapperPlayServerScoreboardScore();
        score6.setObjectiveName("ClassInfo");
        score6.setScoreName("  ");
        score6.setValue(14);

        WrapperPlayServerScoreboardScore job = new WrapperPlayServerScoreboardScore();
        job.setObjectiveName("ClassInfo");
        job.setScoreName(ChatColor.GOLD + "İşiniz: " + ChatColor.GREEN + mmoClass.getJob());
        job.setValue(13);

        WrapperPlayServerScoreboardScore jobSpace1 = new WrapperPlayServerScoreboardScore();
        jobSpace1.setObjectiveName("ClassInfo");
        jobSpace1.setScoreName("   ");
        jobSpace1.setValue(12);

        WrapperPlayServerScoreboardScore jobLevel = new WrapperPlayServerScoreboardScore();
        jobLevel.setObjectiveName("ClassInfo");
        jobLevel.setScoreName(ChatColor.GOLD + "İş seviyesi: " + ChatColor.GREEN + mmoClass.getJobLevel());
        jobLevel.setValue(11);

        WrapperPlayServerScoreboardScore jobSpace2 = new WrapperPlayServerScoreboardScore();
        jobSpace2.setObjectiveName("ClassInfo");
        jobSpace2.setScoreName("    ");
        jobSpace2.setValue(10);

        WrapperPlayServerScoreboardScore jobXP = new WrapperPlayServerScoreboardScore();
        jobXP.setObjectiveName("ClassInfo");
        jobXP.setScoreName(ChatColor.GOLD + "İş XPsi: " + ChatColor.GREEN + mmoClass.getJobXp());
        jobXP.setValue(9);

        WrapperPlayServerScoreboardScore jobSpace3 = new WrapperPlayServerScoreboardScore();
        jobSpace3.setObjectiveName("ClassInfo");
        jobSpace3.setScoreName("     ");
        jobSpace3.setValue(8);

        WrapperPlayServerScoreboardScore score8 = new WrapperPlayServerScoreboardScore();
        score8.setObjectiveName("ClassInfo");
        score8.setScoreName(ChatColor.GOLD + "Para: " + ChatColor.YELLOW + mmoClass.getCurrency());
        score8.setValue(7);

        WrapperPlayServerScoreboardScore score9 = new WrapperPlayServerScoreboardScore();
        score9.setObjectiveName("ClassInfo");
        score9.setScoreName("      ");
        score9.setValue(6);

        WrapperPlayServerScoreboardScore oldscore7 = new WrapperPlayServerScoreboardScore();
        oldscore7.setObjectiveName("ClassInfo");
        oldscore7.setScoreName(ChatColor.GOLD + "Para: " + ChatColor.YELLOW + oldAmount);
        oldscore7.setValue(7);
        oldscore7.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        String faction = "";
        if (mmoClass.getFaction() != null) {
            faction = mmoClass.getFaction();
        }

        WrapperPlayServerScoreboardScore score7 = new WrapperPlayServerScoreboardScore();;
        score7.setObjectiveName("ClassInfo");
        score7.setScoreName(ChatColor.GOLD + "Faction: " + ChatColor.RED + faction);
        score7.setValue(5);

        //WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();

        scoreboardObjective.sendPacket(player);
        displayObjective.sendPacket(player);
        score.sendPacket(player);
        score2.sendPacket(player);
        score3.sendPacket(player);
        score4.sendPacket(player);
        score5.sendPacket(player);
        score6.sendPacket(player);
        oldscore7.sendPacket(player);
        score7.sendPacket(player);
        score8.sendPacket(player);
        score9.sendPacket(player);
        job.sendPacket(player);
        jobLevel.sendPacket(player);
        jobXP.sendPacket(player);
        jobSpace1.sendPacket(player);
        jobSpace2.sendPacket(player);
        jobSpace3.sendPacket(player);
    }

    public static void removeBar(Player player, MMOClass mmoClass)
    {
        WrapperPlayServerScoreboardObjective scoreboardObjective = new WrapperPlayServerScoreboardObjective();
        scoreboardObjective.setDisplayName(WrappedChatComponent.fromText(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Class Bilgileri"));
        scoreboardObjective.setMode(1);

        WrapperPlayServerScoreboardScore score = new WrapperPlayServerScoreboardScore();
        score.setObjectiveName("ClassInfo");
        score.setScoreName(ChatColor.GOLD + "Classınız: " + ChatColor.GREEN + (mmoClass.getClassName().equalsIgnoreCase("necromancer") ? "SUMMONER" : mmoClass.getClassName().toUpperCase()));
        score.setValue(19);
        score.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        WrapperPlayServerScoreboardScore score2 = new WrapperPlayServerScoreboardScore();
        score2.setObjectiveName("ClassInfo");
        score2.setScoreName("");
        score2.setValue(18);
        score2.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        WrapperPlayServerScoreboardScore score3 = new WrapperPlayServerScoreboardScore();
        score3.setObjectiveName("ClassInfo");
        score3.setScoreName(ChatColor.GOLD + "Level: " + ChatColor.GREEN + mmoClass.getLevel());
        score3.setValue(17);
        score3.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        WrapperPlayServerScoreboardScore score4 = new WrapperPlayServerScoreboardScore();
        score4.setObjectiveName("ClassInfo");
        score4.setScoreName(" ");
        score4.setValue(16);
        score4.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        WrapperPlayServerScoreboardScore score5 = new WrapperPlayServerScoreboardScore();
        score5.setObjectiveName("ClassInfo");
        score5.setScoreName(ChatColor.GOLD + "EXP: " + ChatColor.GREEN + String.format("%.2f",mmoClass.getXp()));
        score5.setValue(15);
        score5.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        WrapperPlayServerScoreboardScore score6 = new WrapperPlayServerScoreboardScore();
        score6.setObjectiveName("ClassInfo");
        score6.setScoreName("  ");
        score6.setValue(14);
        score6.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        WrapperPlayServerScoreboardScore job = new WrapperPlayServerScoreboardScore();
        job.setObjectiveName("ClassInfo");
        job.setScoreName(ChatColor.GOLD + "İşiniz: " + ChatColor.GREEN + mmoClass.getJob());
        job.setValue(13);
        job.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        WrapperPlayServerScoreboardScore jobSpace1 = new WrapperPlayServerScoreboardScore();
        jobSpace1.setObjectiveName("ClassInfo");
        jobSpace1.setScoreName("   ");
        jobSpace1.setValue(12);
        jobSpace1.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        WrapperPlayServerScoreboardScore jobLevel = new WrapperPlayServerScoreboardScore();
        jobLevel.setObjectiveName("ClassInfo");
        jobLevel.setScoreName(ChatColor.GOLD + "İş seviyesi: " + ChatColor.GREEN + mmoClass.getJobLevel());
        jobLevel.setValue(11);
        jobLevel.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        WrapperPlayServerScoreboardScore jobSpace2 = new WrapperPlayServerScoreboardScore();
        jobSpace2.setObjectiveName("ClassInfo");
        jobSpace2.setScoreName("    ");
        jobSpace2.setValue(10);
        jobSpace2.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        WrapperPlayServerScoreboardScore jobXP = new WrapperPlayServerScoreboardScore();
        jobXP.setObjectiveName("ClassInfo");
        jobXP.setScoreName(ChatColor.GOLD + "İş XPsi: " + ChatColor.GREEN + mmoClass.getJobXp());
        jobXP.setValue(9);
        jobXP.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        WrapperPlayServerScoreboardScore jobSpace3 = new WrapperPlayServerScoreboardScore();
        jobSpace3.setObjectiveName("ClassInfo");
        jobSpace3.setScoreName("     ");
        jobSpace3.setValue(8);
        jobSpace3.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        WrapperPlayServerScoreboardScore score8 = new WrapperPlayServerScoreboardScore();
        score8.setObjectiveName("ClassInfo");
        score8.setScoreName(ChatColor.GOLD + "Para: " + ChatColor.YELLOW + mmoClass.getCurrency());
        score8.setValue(7);

        WrapperPlayServerScoreboardScore score9 = new WrapperPlayServerScoreboardScore();
        score9.setObjectiveName("ClassInfo");
        score9.setScoreName("   ");
        score9.setValue(6);

        String faction = "";
        if (mmoClass.getFaction() != null) {
            faction = mmoClass.getFaction();
        }

        WrapperPlayServerScoreboardScore score7 = new WrapperPlayServerScoreboardScore();;
        score7.setObjectiveName("ClassInfo");
        score7.setScoreName(ChatColor.GOLD + "Faction: " + ChatColor.RED + faction);
        score7.setValue(5);
        score7.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);

        scoreboardObjective.sendPacket(player);
        score.sendPacket(player);
        score2.sendPacket(player);
        score3.sendPacket(player);
        score4.sendPacket(player);
        score5.sendPacket(player);
        score6.sendPacket(player);
        score7.sendPacket(player);
        job.sendPacket(player);
        jobLevel.sendPacket(player);
        jobXP.sendPacket(player);
        jobSpace1.sendPacket(player);
        jobSpace2.sendPacket(player);
        jobSpace3.sendPacket(player);
    }
}
