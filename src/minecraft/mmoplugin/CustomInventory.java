package minecraft.mmoplugin;

import minecraft.mmoplugin.customItems.CommonItems;
import minecraft.mmoplugin.customItems.CustomItems;
import minecraft.mmoplugin.events.AuctionRunnables;
import minecraft.mmoplugin.events.DBSaver;
import minecraft.mmoplugin.events.TradeConfigManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CustomInventory implements Listener
{
    private Plugin plugin;

    public static TradeConfigManager tradeConfig;

    public CustomInventory(Plugin plugin, Player player, MMOClass mmoClass)
    {
        this.plugin = plugin;
        Inventory inventory = plugin.getServer().createInventory(new CustomInventoryHolder(), 27, ChatColor.GOLD + "Statlar");


        ItemStack level = new ItemStack(Material.ENCHANTED_BOOK,1);
        ItemMeta levelMeta = level.getItemMeta();
        levelMeta.setDisplayName(ChatColor.DARK_AQUA + "Level");
        ArrayList<String> lore = new ArrayList<>();
        lore.add("Şu anki seviyen: " + mmoClass.getLevel());
        levelMeta.setLore(lore);
        level.setItemMeta(levelMeta);


        ItemStack exp = new ItemStack(Material.EMERALD, 1);
        ItemMeta expMeta = exp.getItemMeta();
        expMeta.setDisplayName(ChatColor.DARK_AQUA + "EXP");
        lore.clear();
        lore.add("Şu anki EXP'in " + String.format("%.2f",mmoClass.getXp()));
        expMeta.setLore(lore);
        exp.setItemMeta(expMeta);

        ItemStack requiredExp = new ItemStack(Material.NETHER_STAR);
        ItemMeta requiredExpMeta = requiredExp.getItemMeta();
        requiredExpMeta.setDisplayName(ChatColor.DARK_AQUA + "Gereken EXP");
        lore.clear();
        lore.add(String.format("%.2f",MMOClass.getExpRequired(mmoClass.getLevel())));
        requiredExpMeta.setLore(lore);
        requiredExpMeta.addEnchant(Enchantment.DURABILITY,2,true);
        requiredExpMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        requiredExp.setItemMeta(requiredExpMeta);

        List<ItemStack> classSpecific = addClassInfo(player, mmoClass);

        inventory.setItem(3, level);
        inventory.setItem(4, exp);
        inventory.setItem(5, requiredExp);

        for (int i = 0; i < classSpecific.size(); i++) {
            if (i < 4)
                inventory.setItem(12 + i, classSpecific.get(i));
            else
                inventory.setItem(17 + i, classSpecific.get(i));
        }

        player.openInventory(inventory);

    }

    public static ItemStack addLock(ItemStack toBeLocked)
    {
        ItemStack lockedItem = new ItemStack(Material.IRON_DOOR);

        ItemMeta oldMeta = toBeLocked.getItemMeta();
        ItemMeta newMeta = lockedItem.getItemMeta();

        newMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        newMeta.addEnchant(Enchantment.DURABILITY,1,true);
        newMeta.setDisplayName(oldMeta.getDisplayName() + ChatColor.RED + " [KİTLİ]");

        List<String> lore = oldMeta.getLore();
        List<String> newLore = new ArrayList<>();

        for(String s : lore)
        {
            String lastColor = ChatColor.getLastColors(s);
            s = ChatColor.stripColor(s);
            newLore.add(lastColor + ChatColor.STRIKETHROUGH + s);
        }
        newMeta.setLore(newLore);
        lockedItem.setItemMeta(newMeta);
        return lockedItem;
    }

    public List<ItemStack> addClassInfo(Player player, MMOClass mmoClass)
    {
        List<ItemStack> listToReturn = new ArrayList<>();
        String className = mmoClass.getClassName();
        int level = mmoClass.getLevel();
        switch (className)
        {
            case "warrior":
            {
                ItemStack sunder = new ItemStack(Material.IRON_AXE, 1);
                ItemMeta sunderMeta = sunder.getItemMeta();
                sunderMeta.setDisplayName(ChatColor.RED + "Sunder");
                ArrayList<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "25 saniyede bir sonraki vuruşu Root 0/1/2/3/4 etkisine sahip olur.");
                lore.add(ChatColor.WHITE + "Seviye 1'de açılır, her 5 seviyede bir gelişir, seviye 20'de maxlanır.");
                sunderMeta.setLore(lore);
                sunderMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                sunder.setItemMeta(sunderMeta);

                ItemStack bloodyRage;
                if (level > 49) {
                    bloodyRage = new ItemStack(Material.RED_BANNER, 1);
                    ItemMeta bloodyRageMeta = bloodyRage.getItemMeta();
                    bloodyRageMeta.setDisplayName(ChatColor.BLUE + "Bloody Rage");
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "Düz vuruşlar hedefe %12/%14/%16/%18/%20 gerçek hasar vurur.");
                    lore.add(ChatColor.WHITE + "Seviye 25'de açılır, her 5 seviyede bir gelişir, seviye 45'de maxlanır.");
                    bloodyRageMeta.setLore(lore);
                    bloodyRage.setItemMeta(bloodyRageMeta);
                } else {
                    bloodyRage = new ItemStack(Material.IRON_DOOR, 1);
                    ItemMeta bloodyRageMeta = bloodyRage.getItemMeta();
                    bloodyRageMeta.setDisplayName(ChatColor.BLUE + "Bloody Rage " + ChatColor.RED + "[KİTLİ]");
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "Düz vuruşlar hedefe %12/%14/%16/%18/%20 gerçek hasar vurur.");
                    lore.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "Seviye 25'de açılır, her 5 seviyede bir gelişir, seviye 45'de maxlanır.");
                    bloodyRageMeta.setLore(lore);
                    bloodyRage.setItemMeta(bloodyRageMeta);
                }

                ItemStack whirlwind;
                if (level > 24) {
                    whirlwind = new ItemStack(Material.NETHERITE_SWORD, 1);
                    ItemMeta whirlwindMeta = whirlwind.getItemMeta();
                    whirlwindMeta.setDisplayName(ChatColor.DARK_AQUA + "Whirlwind");
                    whirlwindMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                    whirlwindMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    whirlwindMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "Oyuncu kılıç ile sağ tıkladığında etrafında döndürür ");
                    lore.add(ChatColor.YELLOW + "ve 0.5 saniyede bir 3/4/5/6/7 hasar vurur (30 saniye cooldown)");
                    lore.add(ChatColor.WHITE + "Seviye 25'de açılır, her 5 seviyede bir gelişir, seviye 45'de maxlanır.");
                    whirlwindMeta.setLore(lore);
                    whirlwind.setItemMeta(whirlwindMeta);
                } else {
                    whirlwind = new ItemStack(Material.IRON_DOOR, 1);
                    ItemMeta whirlwindMeta = whirlwind.getItemMeta();
                    whirlwindMeta.setDisplayName(ChatColor.DARK_AQUA + "Whirlwind" + ChatColor.RED + " [KİTLİ]");
                    whirlwindMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                    whirlwindMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    whirlwindMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "Oyuncu kılıç ile sağ tıkladığında etrafında döndürür");
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "ve 0.5 saniyede bir 3/4/5/6/7 hasar vurur(30 saniye cooldown)");
                    lore.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "Seviye 25'de açılır, her 5 seviyede bir gelişir, seviye 45'de maxlanır.");
                    whirlwindMeta.setLore(lore);
                    whirlwind.setItemMeta(whirlwindMeta);
                }

                ItemStack ancestorsStrength;
                if (level > 74) {
                    ancestorsStrength = new ItemStack(Material.ENCHANTED_BOOK, 1);
                    ItemMeta ancestorsStrengthMeta = ancestorsStrength.getItemMeta();
                    ancestorsStrengthMeta.setDisplayName(ChatColor.GREEN + "Ancestors Strength");
                    ancestorsStrengthMeta.addEnchant(Enchantment.DURABILITY, 2, true);
                    ancestorsStrengthMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
                    ancestorsStrengthMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "Oyuncu off-hande bir silah aldığında, o silahın statlarının %30’u main hand’e geçer.");
                    lore.add(ChatColor.WHITE + "Seviye 75'de açılır.");
                    ancestorsStrengthMeta.setLore(lore);
                    ancestorsStrength.setItemMeta(ancestorsStrengthMeta);
                } else {
                    ancestorsStrength = new ItemStack(Material.IRON_DOOR, 1);
                    ItemMeta ancestorsStrengthMeta = ancestorsStrength.getItemMeta();
                    ancestorsStrengthMeta.setDisplayName(ChatColor.GREEN + "Ancestors Strength" + ChatColor.RED + "[KİTLİ]");
                    ancestorsStrengthMeta.addEnchant(Enchantment.DURABILITY, 2, true);
                    ancestorsStrengthMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
                    ancestorsStrengthMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "Oyuncu off-hande bir silah aldığında, o silahın statlarının %30’u main hand’e geçer.");
                    lore.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "Seviye 75'de açılır.");
                    ancestorsStrengthMeta.setLore(lore);
                    ancestorsStrength.setItemMeta(ancestorsStrengthMeta);
                }
                /*
                 *
                 *
                 * TRAITS
                 *
                 * */
                ItemStack balta;
                if (level > 24) {
                    balta = new ItemStack(Material.GOLDEN_AXE, 1);
                    ItemMeta baltaMeta = balta.getItemMeta();
                    baltaMeta.setDisplayName(ChatColor.GREEN + "Axe Talent");
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "Baltalar %20 daha fazla hasar verir. ");
                    lore.add(ChatColor.WHITE + "Seviye 25'de açılır.");
                    baltaMeta.setLore(lore);
                    baltaMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    balta.setItemMeta(baltaMeta);
                } else {
                    balta = new ItemStack(Material.IRON_DOOR);
                    ItemMeta baltaMeta = balta.getItemMeta();
                    baltaMeta.setDisplayName(ChatColor.GREEN + "Axe Talent" + ChatColor.RED + " [KİTLİ]");
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "Baltalar %20 daha fazla hasar verir. ");
                    lore.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "Seviye 25'de açılır.");
                    baltaMeta.setLore(lore);
                    baltaMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    balta.setItemMeta(baltaMeta);
                }

                ItemStack wither;
                if (level > 49) {
                    wither = new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
                    ItemMeta witherMeta = wither.getItemMeta();
                    witherMeta.setDisplayName(ChatColor.GREEN + "Wither Talent");
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "%10 şansla vurulan hedefe wither uygulanır ve 5 saniye boyunca hasar alır.");
                    lore.add(ChatColor.WHITE + "Seviye 50'de açılır.");
                    witherMeta.setLore(lore);
                    wither.setItemMeta(witherMeta);
                } else {
                    wither = new ItemStack(Material.IRON_DOOR, 1);
                    ItemMeta witherMeta = wither.getItemMeta();
                    witherMeta.setDisplayName(ChatColor.GREEN + "Wither Talent" + ChatColor.RED + " [KİTLİ]");
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "%10 şansla vurulan hedefe wither uygulanır ve 5 saniye boyunca hasar alır.");
                    lore.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "Seviye 50'de açılır.");
                    witherMeta.setLore(lore);
                    wither.setItemMeta(witherMeta);
                }

                ItemStack olaf;
                if (level > 74) {
                    olaf = new ItemStack(Material.RED_DYE, 1);
                    ItemMeta olafMeta = olaf.getItemMeta();
                    olafMeta.setDisplayName(ChatColor.GREEN + "Attack Speed Talent");
                    olafMeta.addEnchant(Enchantment.DURABILITY, 2, true);
                    olafMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
                    olafMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "Oyuncu canı azaldıkça daha hızlı vurur.");
                    lore.add(ChatColor.WHITE + "Seviye 75'de açılır.");
                    olafMeta.setLore(lore);
                    olaf.setItemMeta(olafMeta);
                } else {
                    olaf = new ItemStack(Material.IRON_DOOR, 1);
                    ItemMeta olafMeta = olaf.getItemMeta();
                    olafMeta.setDisplayName(ChatColor.GREEN + "Attack Speed Talent" + ChatColor.RED + " [KİTLİ]");
                    olafMeta.addEnchant(Enchantment.DURABILITY, 2, true);
                    olafMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
                    olafMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "Oyuncu canı azaldıkça daha hızlı vurur.");
                    lore.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "Seviye 75'de açılır.");
                    olafMeta.setLore(lore);
                    olaf.setItemMeta(olafMeta);
                }

                ItemStack execute;
                if (level > 99) {
                    execute = new ItemStack(Material.SHEARS, 1);
                    ItemMeta executeMeta = execute.getItemMeta();
                    executeMeta.setDisplayName(ChatColor.GREEN + "Execute Talent");
                    executeMeta.addEnchant(Enchantment.DURABILITY, 2, true);
                    executeMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
                    executeMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "%15 canın altındaki hedefleri anında öldürür. 10 saniye boyunca Movement Speed kazanır.");
                    lore.add(ChatColor.WHITE + "Seviye 100'de açılır.");
                    executeMeta.setLore(lore);
                    execute.setItemMeta(executeMeta);
                } else {
                    execute = new ItemStack(Material.IRON_DOOR, 1);
                    ItemMeta executeMeta = execute.getItemMeta();
                    executeMeta.setDisplayName(ChatColor.GREEN + "Execute Talent" + ChatColor.RED + " [KİTLİ]");
                    executeMeta.addEnchant(Enchantment.DURABILITY, 2, true);
                    executeMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
                    executeMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "%15 canın altındaki hedefleri anında öldürür. 10 saniye boyunca Movement Speed kazanır.");
                    lore.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "Seviye 100'de açılır.");
                    executeMeta.setLore(lore);
                    execute.setItemMeta(executeMeta);
                }

                listToReturn.add(sunder);
                listToReturn.add(whirlwind);
                listToReturn.add(bloodyRage);
                listToReturn.add(ancestorsStrength);

                listToReturn.add(balta);
                listToReturn.add(wither);
                listToReturn.add(olaf);
                listToReturn.add(execute);

                return listToReturn;
            }
            case "cleric":
            {
                ItemStack skill1 = new ItemStack(Material.POTION,1);
                ItemMeta skill1ItemMeta = skill1.getItemMeta();
                skill1ItemMeta.setDisplayName(ChatColor.RED + "Light's Guidance");
                skill1ItemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                ArrayList<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "Oyuncu hasar aldığında can yenilenmesi kazanır.");
                lore.add(ChatColor.WHITE + "Seviye 1'de açılır.");
                skill1ItemMeta.setLore(lore);
                skill1ItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                skill1.setItemMeta(skill1ItemMeta);


                ItemStack skill2;
                if(level > 25)
                {
                    skill2 = new ItemStack(Material.GOLDEN_SWORD, 1);
                    ItemMeta skill2Meta = skill2.getItemMeta();
                    skill2Meta.setDisplayName(ChatColor.GOLD + "Holy Preparation");
                    skill2Meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "Oyuncu boş eli ile sağ tıkladığı zaman");
                    lore.add(ChatColor.YELLOW + "etrafında daire oluşturur, kısa bir süre");
                    lore.add(ChatColor.YELLOW + "sonra alandaki herkesi kendine çeker.");
                    lore.add(ChatColor.YELLOW + "Etkilenin kişiler hasar yer, ve oyuncu");
                    lore.add(ChatColor.YELLOW + "ek can kazanır.");
                    lore.add("");
                    lore.add(ChatColor.GRAY + "Bu yetenek ile çekilen");
                    lore.add(ChatColor.GRAY + "canavarlar oyuncuya saldırır.");
                    lore.add("");
                    lore.add(ChatColor.WHITE + "Seviye 26'da açılır.");
                    skill2Meta.setLore(lore);
                    skill2.setItemMeta(skill2Meta);
                }
                else
                {
                    skill2 = new ItemStack(Material.IRON_DOOR, 1);
                    ItemMeta skill2Meta = skill2.getItemMeta();
                    skill2Meta.setDisplayName(ChatColor.BLUE + "Light Infusion" + ChatColor.RED + " [KİTLİ]");
                    skill2Meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "Oyuncu boş eli ile sağ tıkladığı zaman");
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "etrafında daire oluşturur, kısa bir süre");
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "sonra alandaki herkesi kendine çeker.");
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "Etkilenin kişiler hasar yer, ve oyuncu");
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "ek can kazanır.");
                    lore.add("");
                    lore.add(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "Bu yetenek ile çekilen");
                    lore.add(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "canavarlar oyuncuya saldırır.");
                    lore.add("");
                    lore.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "Seviye 26'da açılır.");
                    skill2Meta.setLore(lore);
                    skill2.setItemMeta(skill2Meta);
                }
                ItemStack whirlwind;
                if(level > 50)
                {
                    whirlwind = new ItemStack(Material.GLOWSTONE_DUST, 1);
                    ItemMeta whirlwindMeta = whirlwind.getItemMeta();
                    whirlwindMeta.setDisplayName(ChatColor.DARK_AQUA + "Light's Will");
                    whirlwindMeta.addEnchant(Enchantment.DURABILITY,1,true);
                    whirlwindMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    whirlwindMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "Silahlardaki smite enchanti oyunculara silahın enchantine ve");
                    lore.add(ChatColor.YELLOW + "oyuncunun seviyesine bağlı olarak %10/%15/%20/%25/%30/%35 extra hasar vurur.");
                    lore.add(ChatColor.WHITE + "Seviye 51'de açılır, her 5 seviyede bir gelişir, seviye 75'de maxlanır.");
                    whirlwindMeta.setLore(lore);
                    whirlwind.setItemMeta(whirlwindMeta);
                }
                else
                {
                    whirlwind = new ItemStack(Material.IRON_DOOR, 1);
                    ItemMeta whirlwindMeta = whirlwind.getItemMeta();
                    whirlwindMeta.setDisplayName(ChatColor.DARK_AQUA + "Light's Will" + ChatColor.RED + " [KİTLİ]");
                    whirlwindMeta.addEnchant(Enchantment.DURABILITY,1,true);
                    whirlwindMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    whirlwindMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "Silahlardaki smite enchanti oyunculara silahın enchantine ve");
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "oyuncunun seviyesine bağlı olarak %10/%15/%20/%25/%30/%35 extra hasar vurur.");
                    lore.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "Seviye 51'de açılır, her 5 seviyede bir gelişir, seviye 75'de maxlanır.");
                    whirlwindMeta.setLore(lore);
                    whirlwind.setItemMeta(whirlwindMeta);
                }
                ItemStack execute;
                if(level > 99)
                {
                    execute = new ItemStack(Material.BEACON, 1);
                    ItemMeta executeMeta = execute.getItemMeta();
                    executeMeta.setDisplayName(ChatColor.GREEN + "Light's Defiance");
                    executeMeta.addEnchant(Enchantment.DURABILITY,2,true);
                    executeMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
                    executeMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "Oyuncu ölecek olursa, 7 saniye boyunca ölümsüzlük kazanır.");
                    lore.add(ChatColor.YELLOW + "7 saniye sonra oyuncu ölerek, etrafındaki oyunculara şimşek indirir.");
                    lore.add(ChatColor.WHITE +  "Seviye 100'de açılır.");
                    executeMeta.setLore(lore);
                    execute.setItemMeta(executeMeta);
                }
                else
                {
                    execute = new ItemStack(Material.IRON_DOOR, 1);
                    ItemMeta executeMeta = execute.getItemMeta();
                    executeMeta.setDisplayName(ChatColor.GREEN + "Light's Defiance" + " [KİTLİ]");
                    executeMeta.addEnchant(Enchantment.DURABILITY,2,true);
                    executeMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
                    executeMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "Oyuncu ölecek olursa, 7 saniye boyunca ölümsüzlük kazanır.");
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "7 saniye sonra oyuncu ölerek, etrafındaki oyunculara şimşek indirir.");
                    lore.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "Seviye 100'de açılır.");
                    executeMeta.setLore(lore);
                    execute.setItemMeta(executeMeta);
                }


                /*
                 *
                 *
                 * TRAITS
                 *
                 * */

                ItemStack fire;
                if(level >24)
                {
                    fire = new ItemStack(Material.FIRE_CORAL_FAN, 1);
                    ItemMeta fireMeta = fire.getItemMeta();
                    fireMeta.setDisplayName(ChatColor.GREEN + "Purging Fire");
                    fireMeta.addEnchant(Enchantment.DURABILITY,2,true);
                    fireMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
                    fireMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "Oyuncunun düz vuruşları, hedefini 1 saniyeliğine yakar.");
                    lore.add(ChatColor.WHITE +  "Seviye 25'de açılır.");
                    fireMeta.setLore(lore);
                    fire.setItemMeta(fireMeta);
                }
                else
                {
                    fire = new ItemStack(Material.IRON_DOOR, 1);
                    ItemMeta fireMeta = fire.getItemMeta();
                    fireMeta.setDisplayName(ChatColor.GREEN + "Purging Fire" + ChatColor.RED + " [KİTLİ]");
                    fireMeta.addEnchant(Enchantment.DURABILITY,2,true);
                    fireMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
                    fireMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "Oyuncunun düz vuruşları, hedefini 1 saniyeliğine yakar.");
                    lore.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "Seviye 25'de açılır.");
                    fireMeta.setLore(lore);
                    fire.setItemMeta(fireMeta);
                }

                ItemStack wither;
                if(level>49)
                {
                    wither = new ItemStack(Material.SHIELD, 1);
                    ItemMeta witherMeta = wither.getItemMeta();
                    witherMeta.setDisplayName(ChatColor.GREEN + "Kalkan Talent'ı");
                    witherMeta.addEnchant(Enchantment.DURABILITY,2,true);
                    witherMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
                    witherMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "Oyuncu kalkan kullandığında, kalkan bonus zırh verir.");
                    lore.add(ChatColor.YELLOW + "fakat, oyuncunun hasarı azalır.");
                    lore.add(ChatColor.WHITE +  "Seviye 50'de açılır.");
                    witherMeta.setLore(lore);
                    wither.setItemMeta(witherMeta);
                }
                else
                {
                    wither = new ItemStack(Material.IRON_DOOR, 1);
                    ItemMeta witherMeta = wither.getItemMeta();
                    witherMeta.setDisplayName(ChatColor.GREEN + "Kalkan Talent'ı" + ChatColor.RED + " [KİTLİ]");
                    witherMeta.addEnchant(Enchantment.DURABILITY,2,true);
                    witherMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
                    witherMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "Oyuncu kalkan kullandığında, kalkan bonus zırh verir.");
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "fakat, oyuncunun hasarı azalır.");
                    lore.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "Seviye 50'de açılır.");
                    witherMeta.setLore(lore);
                    wither.setItemMeta(witherMeta);
                }
                ItemStack olaf;
                if(level > 74)
                {
                    olaf = new ItemStack(Material.GOLDEN_HORSE_ARMOR, 1);
                    ItemMeta olafMeta = olaf.getItemMeta();
                    olafMeta.setDisplayName(ChatColor.GREEN + "Light's Help");
                    olafMeta.addEnchant(Enchantment.DURABILITY,2,true);
                    olafMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
                    olafMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "Oyuncunun canı %40'ın altına düşerse, oyuncu");
                    lore.add(ChatColor.YELLOW + "Hız ve güç kazanır.");
                    lore.add(ChatColor.WHITE +  "Seviye 75'de açılır.");
                    olafMeta.setLore(lore);
                    olaf.setItemMeta(olafMeta);
                }
                else
                {
                    olaf = new ItemStack(Material.IRON_DOOR, 1);
                    ItemMeta olafMeta = olaf.getItemMeta();
                    olafMeta.setDisplayName(ChatColor.GREEN + "Light's Help" + ChatColor.RED + " [KİTLİ]");
                    olafMeta.addEnchant(Enchantment.DURABILITY,2,true);
                    olafMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
                    olafMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "Oyuncunun canı %40'ın altına düşerse, oyuncu");
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "Hız ve güç kazanır.");
                    lore.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "Seviye 75'de açılır.");
                    olafMeta.setLore(lore);
                    olaf.setItemMeta(olafMeta);
                }
                ItemStack ancestorsStrength;
                if(level > 99)
                {
                    ancestorsStrength = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1);
                    ItemMeta ancestorsStrengthMeta = ancestorsStrength.getItemMeta();
                    ancestorsStrengthMeta.setDisplayName(ChatColor.GREEN + "Aura of Protection");
                    ancestorsStrengthMeta.addEnchant(Enchantment.DURABILITY,2,true);
                    ancestorsStrengthMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
                    ancestorsStrengthMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "Oyuncu hasar aldığında, 5 saniye boyunca gelen hasarı azaltır.");
                    lore.add(ChatColor.YELLOW + "Bu yeteneğin bekleme süresi yoktur.");
                    lore.add(ChatColor.WHITE + "Seviye 100'de açılır.");
                    ancestorsStrengthMeta.setLore(lore);
                    ancestorsStrength.setItemMeta(ancestorsStrengthMeta);
                }
                else
                {
                    ancestorsStrength = new ItemStack(Material.IRON_DOOR, 1);
                    ItemMeta ancestorsStrengthMeta = ancestorsStrength.getItemMeta();
                    ancestorsStrengthMeta.setDisplayName(ChatColor.GREEN + "Aura of Protection" + ChatColor.RED + " [KİTLİ]");
                    ancestorsStrengthMeta.addEnchant(Enchantment.DURABILITY,2,true);
                    ancestorsStrengthMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
                    ancestorsStrengthMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.clear();
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "Oyuncu hasar aldığında, 5 saniye boyunca gelen hasarı azaltır.");
                    lore.add(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "Bu yeteneğin bekleme süresi yoktur.");
                    lore.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "Seviye 100'de açılır.");
                    ancestorsStrengthMeta.setLore(lore);
                    ancestorsStrength.setItemMeta(ancestorsStrengthMeta);
                }


                listToReturn.add(skill1);
                listToReturn.add(skill2);
                listToReturn.add(whirlwind);
                listToReturn.add(execute);

                listToReturn.add(fire);
                listToReturn.add(wither);
                listToReturn.add(olaf);
                listToReturn.add(ancestorsStrength);
                return listToReturn;
            }
            case "necromancer":
            {
                List<ItemStack> beforeModification = ClassInfoInventory.generateSummonerSkills();
                if(level == 100)
                    return beforeModification;
                if(level < 100)
                {
                    ItemStack lvl100Talent = beforeModification.get(7);
                    beforeModification.remove(7);
                    lvl100Talent = addLock(lvl100Talent);
                    beforeModification.add(7,lvl100Talent);
                }
                if(level < 76)
                {
                    ItemStack horseSkeleton = beforeModification.get(3);
                    beforeModification.remove(3);
                    horseSkeleton = addLock(horseSkeleton);
                    beforeModification.add(3,horseSkeleton);
                }
                if(level < 75)
                {
                    ItemStack equipmentTalent = beforeModification.get(6);
                    beforeModification.remove(6);
                    equipmentTalent = addLock(equipmentTalent);
                    beforeModification.add(6,equipmentTalent);
                }
                if(level < 51)
                {
                    ItemStack witherSkeleton = beforeModification.get(2);
                    beforeModification.remove(2);
                    witherSkeleton = addLock(witherSkeleton);
                    beforeModification.add(2,witherSkeleton);
                }
                if(level < 50)
                {
                    ItemStack equipmentTalent = beforeModification.get(5);
                    beforeModification.remove(5);
                    equipmentTalent = addLock(equipmentTalent);
                    beforeModification.add(5,equipmentTalent);
                }
                if(level < 26)
                {
                    ItemStack equipmentTalent = beforeModification.get(1);
                    beforeModification.remove(1);
                    equipmentTalent = addLock(equipmentTalent);
                    beforeModification.add(1,equipmentTalent);
                }
                if(level < 25)
                {
                    ItemStack equipmentTalent = beforeModification.get(4);
                    beforeModification.remove(4);
                    equipmentTalent = addLock(equipmentTalent);
                    beforeModification.add(4,equipmentTalent);
                }
                return beforeModification;
            }
        }
        return listToReturn;
    }

    public static void createDungeonInfoInventory(Player player, Plugin plugin, String dungeonName)
    {
        int startingIndex = dungeonName.indexOf("_");
        int endingIndex = dungeonName.lastIndexOf("_");
        String substringName = dungeonName.substring(startingIndex+1,endingIndex);
        substringName = StringUtils.capitalize(substringName);
        Inventory inventory = plugin.getServer().createInventory(new CustomInventoryHolder(), 27, ChatColor.GREEN + substringName + " Dungeon Menu");


        ItemStack normalChest = new ItemStack(Material.CHEST);
        ItemMeta normalChestMeta = normalChest.getItemMeta();
        normalChestMeta.addEnchant(Enchantment.DURABILITY,1,true);
        normalChestMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        normalChest.setItemMeta(normalChestMeta);

        ItemStack enderChest = new ItemStack(Material.ENDER_CHEST);
        ItemMeta enderChestMeta = enderChest.getItemMeta();
        enderChestMeta.addEnchant(Enchantment.DURABILITY,1,true);
        enderChestMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        enderChest.setItemMeta(enderChestMeta);

        ItemStack blueGlass = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta blueItemMeta = blueGlass.getItemMeta();
        blueItemMeta.setDisplayName(" ");
        blueGlass.setItemMeta(blueItemMeta);

        ItemStack greenGlass = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta greenGlassMeta = greenGlass.getItemMeta();
        greenGlassMeta.setDisplayName(" ");
        greenGlass.setItemMeta(greenGlassMeta);

        ItemStack redGlass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redGlassMeta = redGlass.getItemMeta();
        redGlassMeta.setDisplayName(" ");
        redGlass.setItemMeta(redGlassMeta);

        List<Integer> dungeonPlayerCount = new ArrayList<>();
        List<String> lore = new ArrayList<>();


        int counter = 0;
        for(int i = 0; i < 5; i++, counter=counter+2)
        {
            DungeonManager.Dungeon dungeon =  DungeonManager.dungeonMap.get(dungeonName+i);
            List<Player> playerList = dungeon.getPlayerList();
            int playercount = playerList.size();
            if(playercount == 0)
            {
                ItemStack tempChest = normalChest;
                lore.clear();
                lore.add(ChatColor.AQUA + "Dungeondaki oyuncu sayısı: " + ChatColor.WHITE + playercount);
                ItemMeta tempMeta = tempChest.getItemMeta();
                tempMeta.setLore(lore);
                tempMeta.setDisplayName(ChatColor.RED + dungeonName+i);
                tempChest.setItemMeta(tempMeta);
                inventory.setItem(9+counter,normalChest);
                inventory.setItem(18+counter,greenGlass);
            }
            else if(!dungeon.isInProgress())
            {
                ItemStack tempChest = normalChest;
                lore.clear();
                lore.add(ChatColor.AQUA + "Dungeondaki oyuncu sayısı: " + ChatColor.WHITE + playercount);
                lore.add(ChatColor.YELLOW + "Şu an içeride olan faction: " + ChatColor.GREEN + MainClass.classObjectMap.get(playerList.get(0).getUniqueId()).getFaction());
                ItemMeta tempMeta = tempChest.getItemMeta();
                tempMeta.setLore(lore);
                tempMeta.setDisplayName(ChatColor.RED + dungeonName+i);
                tempChest.setItemMeta(tempMeta);
                inventory.setItem(9+counter,normalChest);
                inventory.setItem(18+counter,greenGlass);
            }
            else if(dungeon.isInProgress())
            {
                ItemStack tempEnderChest = enderChest;
                lore.clear();
                lore.add(ChatColor.AQUA + "Dungeondaki oyuncu sayısı: " + ChatColor.WHITE + playercount);
                lore.add(ChatColor.YELLOW + "Şu an içeride olan faction: " + ChatColor.GREEN + MainClass.classObjectMap.get(playerList.get(0).getUniqueId()).getFaction());
                lore.add(ChatColor.RED + "" + ChatColor.BOLD + "Bu dungeon şu anda dolu.");
                ItemMeta tempEnderChestMeta = tempEnderChest.getItemMeta();
                tempEnderChestMeta.setLore(lore);
                tempEnderChestMeta.setDisplayName(ChatColor.RED + dungeonName+i);
                tempEnderChest.setItemMeta(tempEnderChestMeta);
                inventory.setItem(9+counter,tempEnderChest);
                inventory.setItem(18+counter,redGlass);
            }
        }

        for(int i = 0; i < 9; i++)
        {
            inventory.setItem(i,blueGlass);
        }

        for(int i = 10; i < 18; i= i+2)
        {
            inventory.setItem(i,blueGlass);
        }

        for(int i = 19; i < 26; i= i+2)
        {
            inventory.setItem(i,blueGlass);
        }

        player.openInventory(inventory);
    }

    public static void createFactionMenu(Player player, Plugin plugin, String likeCondition, int page)
    {
        int offset = 21*page;
        Inventory inventory = plugin.getServer().createInventory(new FactionInventoryHolder(likeCondition,page), 45,  ChatColor.LIGHT_PURPLE + "Faction Arama Menüsü sayfa: " + (page+1));
        ItemStack filler = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        ItemStack geri = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta geriMeta = geri.getItemMeta();
        geriMeta.setDisplayName(ChatColor.RED + "Geri");
        geri.setItemMeta(geriMeta);
        inventory.setItem(37,geri);

        ItemStack sign = new ItemStack(Material.SPRUCE_SIGN);
        ItemMeta signMeta = sign.getItemMeta();
        signMeta.setDisplayName(ChatColor.GOLD + "Faction ara!");
        List<String> lore = new ArrayList<>();
        if(likeCondition != null)
        {
            lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + likeCondition);
            signMeta.setLore(lore);
        }
        lore.clear();
        sign.setItemMeta(signMeta);
        inventory.setItem(40,sign);

        ItemStack ileri = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta meta = (PotionMeta)ileri.getItemMeta();
        meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
        meta.setDisplayName(ChatColor.GREEN + "İleri");
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        ileri.setItemMeta(meta);
        inventory.setItem(43,ileri);

        for(int i = 0; i < 9; i++)
        {
            inventory.setItem(i,filler);
        }
        for(int i = 9; i < 36; i+=9)
        {
            inventory.setItem(i,filler);
            inventory.setItem(i+8,filler);
        }
        for(int i = 36; i < 45; i++)
        {
            if(i != 40 && i!= 37 && i!=43)
                inventory.setItem(i,filler);
        }

        try
        {
            PreparedStatement ps;
            if(likeCondition != null)
            {
                ps = MainClass.conn.prepareStatement("select * from all_factions where Name like CONCAT('%', ?, '%') limit 21 offset " + offset);
                ps.setString(1,likeCondition);
            }
            else
                ps = MainClass.conn.prepareStatement("select * from all_factions limit 21 offset " + offset);

            ResultSet rs = ps.executeQuery();
            int index = 10;
            while(rs.next())
            {
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Found the faction: " + rs.getString("Name"));
                int size = rs.getInt("members");
                ItemStack faction = null;
                if(size < 5)
                {
                    faction = new ItemStack(Material.GREEN_BANNER);
                }
                else if(size < 10)
                {
                    faction = new ItemStack(Material.BLUE_BANNER);
                }
                else
                {
                    faction = new ItemStack(Material.RED_BANNER);
                }
                ItemMeta factionMeta = faction.getItemMeta();
                factionMeta.setDisplayName(ChatColor.GREEN + rs.getString("Name"));
                factionMeta.addEnchant(Enchantment.DURABILITY,1,true);
                factionMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                lore.add(ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + "Faction üye sayısı: " + ChatColor.YELLOW + size);
                lore.add(ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + "Faction katılma şekli: " + ChatColor.YELLOW +rs.getString("join"));
                lore.add(ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + "Faction Lideri: " + ChatColor.RED + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + rs.getString("owner"));
                factionMeta.setLore(lore);
                lore.clear();

                faction.setItemMeta(factionMeta);

                inventory.setItem(index,faction);
                index++;
            }
            ps.close();
            rs.close();

            ps = null;
            rs = null;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        player.openInventory(inventory);
    }

    public static void createTradingMenu(Player player1, Player player2, Plugin plugin)
    {
        TradingInventoryHolder ts = new TradingInventoryHolder(player1,player2);
        Inventory inventory = plugin.getServer().createInventory(ts, 54,  ChatColor.DARK_RED + "Takas Menüsü");
        ts.setInventory(inventory);

        MainClass.classObjectMap.get(player1.getUniqueId()).setTradingInv(inventory); //Set players' trading inventories
        MainClass.classObjectMap.get(player2.getUniqueId()).setTradingInv(inventory);
        ItemStack filler = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for(int i = 0; i < 9; i++)
        {
            inventory.setItem(i,filler);
        }

        inventory.setItem(9,filler);
        inventory.setItem(18,filler);
        inventory.setItem(27,filler);
        inventory.setItem(36,filler);

        inventory.setItem(17,filler);
        inventory.setItem(26,filler);
        inventory.setItem(35,filler);
        inventory.setItem(44,filler);


        inventory.setItem(45,filler);
        inventory.setItem(46,filler);
        inventory.setItem(49,filler);
        inventory.setItem(50,filler);
        inventory.setItem(53,filler);

        //47 ve 51 Ready state

        ItemStack readyState = new ItemStack(Material.RED_WOOL);
        ItemMeta readyMeta = readyState.getItemMeta();
        readyMeta.setDisplayName(ChatColor.RED + "Hazır değil");
        readyState.setItemMeta(readyMeta);

        inventory.setItem(47,readyState);
        inventory.setItem(51,readyState);

        ItemStack sign = new ItemStack(Material.SPRUCE_SIGN);
        ItemMeta signMeta = sign.getItemMeta();
        signMeta.setDisplayName(ChatColor.YELLOW + "Sunulan para: ");
        List<String> lore = new ArrayList<>();
        lore.add("0");
        signMeta.setLore(lore);
        sign.setItemMeta(signMeta);

        //48 ve 52 currency
        inventory.setItem(48,sign);
        inventory.setItem(52,sign);

        //13, 22, 31, 40 sticks

        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta stickMeta = stick.getItemMeta();
        stickMeta.setDisplayName(" ");
        stick.setItemMeta(stickMeta);

        inventory.setItem(13,stick);
        inventory.setItem(22,stick);
        inventory.setItem(31,stick);
        inventory.setItem(40,stick);

        player1.openInventory(inventory);
        player2.openInventory(inventory);

    }

    public static boolean AllowedTradingSlot(int trader, int slot)
    {
        switch (slot)
        {
            case 10:
            case 11:
            case 12:
            case 19:
            case 20:
            case 21:
            case 28:
            case 29:
            case 30:
            case 37:
            case 38:
            case 39:
            case 47:
            case 48:
                if(trader == 1)
                    return true;
                return false;
            case 14:
            case 15:
            case 16:
            case 23:
            case 24:
            case 25:
            case 32:
            case 33:
            case 34:
            case 41:
            case 42:
            case 43:
            case 51:
            case 52:
                if(trader==2)
                    return true;
                return false;

        }
        return false;
    }

    public static class CustomInventoryHolder implements InventoryHolder
    {

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public static class FactionInventoryHolder implements InventoryHolder
    {

        private String searchQuery;
        private int page;

        public int getPage()
        {
            return this.page;
        }

        public String getSearchQuery()
        {
            return this.searchQuery;
        }

        FactionInventoryHolder(String query, int page)
        {
            this.searchQuery = query;
            this.page = page;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public static class TradingInventoryHolder implements InventoryHolder
    {

        Player player1;
        Player player2;

        boolean player1Ready;
        boolean player2Ready;

        Inventory inventory;
        BukkitTask tradeConfirmer;

        public BukkitTask getTradeConfirmer() {
            return tradeConfirmer;
        }

        public void setTradeConfirmer(BukkitTask tradeConfirmer) {
            this.tradeConfirmer = tradeConfirmer;
        }


        public boolean isPlayer1Ready() {
            return player1Ready;
        }

        public void setPlayer1Ready(boolean player1Ready) {
            this.player1Ready = player1Ready;
        }

        public boolean isPlayer2Ready() {
            return player2Ready;
        }

        public void setPlayer2Ready(boolean player2Ready) {
            this.player2Ready = player2Ready;
        }

        public Player getPlayer1() {
            return player1;
        }

        public void setPlayer1(Player player1) {
            this.player1 = player1;
        }

        public Player getPlayer2() {
            return player2;
        }

        public void setPlayer2(Player player2) {
            this.player2 = player2;
        }

        public TradingInventoryHolder(Player player1, Player player2) {
            this.player1 = player1;
            this.player2 = player2;
            this.player1Ready = false;
            this.player2Ready = false;
            this.inventory = null;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory)
        {
            this.inventory = inventory;
        }
    }

    public static class ConfirmTrade extends BukkitRunnable
    {

        Inventory inventory;
        Player player1;
        Player player2;
        int counter;

        ConfirmTrade(Inventory inv, Player p1, Player p2)
        {
            this.inventory = inv;
            this.player1 = p1;
            this.player2 = p2;
            counter = 6;
        }

        @Override
        public void run()
        {
            counter--;
            if(this.isCancelled())
                return;
            if(!inventory.getItem(47).getType().equals(Material.GREEN_WOOL) || !inventory.getItem(51).getType().equals(Material.GREEN_WOOL))
            {
                cancel();
                player1.sendMessage(MainClass.getPluginPrefix() + ChatColor.YELLOW + "Bir oyuncu hazır olmaktan vazgeçti");
                player2.sendMessage(MainClass.getPluginPrefix() + ChatColor.YELLOW + "Bir oyuncu hazır olmaktan vazgeçti");
                TradingInventoryHolder holder = (TradingInventoryHolder)inventory.getHolder();
                holder.setTradeConfirmer(null);
            }
            if(player1.isOnline() && player2.isOnline())
            {
                player1.sendMessage(MainClass.getPluginPrefix() + ChatColor.YELLOW + "Takas, " + ChatColor.BLUE + counter + " saniye " + ChatColor.YELLOW + "sonra tamamlanacak! ");
                player2.sendMessage(MainClass.getPluginPrefix() + ChatColor.YELLOW + "Takas, " + ChatColor.BLUE + counter + " saniye " + ChatColor.YELLOW + "sonra tamamlanacak! ");
            }
            else
            {
                MMOClass.cancelTrade(inventory);
                cancel();
            }
            if(counter == 0)
            {
                MMOClass.confirmTrade(inventory);
                cancel();
            }
        }
    }

    public static void createAuctionWelcomeMenu(Player player, Plugin plugin)
    {
        Inventory inventory = plugin.getServer().createInventory(new CustomInventory.CustomInventoryHolder(), 36, ChatColor.GRAY + "Market Menüsü");
        List<String> lore = new ArrayList<>();


        ItemStack searchForAuctions = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta searchMeta = searchForAuctions.getItemMeta();
        searchMeta.setDisplayName(ChatColor.GOLD + "Ürünlere göz at");
        lore.add(ChatColor.GRAY + "Başka oyuncular tarafından koyulumuş");
        lore.add(ChatColor.GRAY + "ürünlere buraya basarak göz atabilirsin.");

        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.GRAY + "Burdaki ürünler en yüksek teklifi");
        lore.add(ChatColor.GRAY + "öneren oyuncuya gidecektir.");
        lore.add(ChatColor.GRAY + "");

        lore.add(ChatColor.GOLD + "Bakmak için tıkla!");
        searchMeta.setLore(lore);
        lore.clear();
        searchForAuctions.setItemMeta(searchMeta);

        ItemStack viewBids = new ItemStack(Material.GOLDEN_CARROT);
        ItemMeta viewMeta = viewBids.getItemMeta();
        viewMeta.setDisplayName(ChatColor.YELLOW + "Tekliflerine göz at!");

        try
        {
            PreparedStatement ps = MainClass.conn.prepareStatement("select COUNT(*) as count from items_bidders a inner join(select item_id, max(bidAmount) bidAmount from items_bidders group by item_id) b on a.item_id = b.item_id and a.bidAmount = b.bidAmount where player_id = ? group by player_id"); // Get max amount of bids for player
            ps.setString(1, String.valueOf(player.getUniqueId()));
            ResultSet rs = ps.executeQuery();
            int count = 0;
            if(rs.next())
            {
                 count = rs.getInt("count");
            }
            lore.add(ChatColor.GRAY + "Yenmekte olduğun teklif sayısı : " + ChatColor.GOLD + count);
            lore.add("");
            lore.add(ChatColor.GOLD + "Market menüsü" + ChatColor.GRAY + "nden alınabilecek eşyalara göz atabilirsin!");
            viewMeta.setLore(lore);
            lore.clear();
            viewBids.setItemMeta(viewMeta);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        ItemStack createAuction = new ItemStack(Material.GOLDEN_HORSE_ARMOR);
        ItemMeta createMeta = createAuction.getItemMeta();
        createMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        createMeta.setDisplayName(ChatColor.GREEN + "Ürün sat");
        lore.clear();
        lore.add(ChatColor.GRAY + "Kendi ürünlerini markette satmak için");
        lore.add(ChatColor.GRAY + "buraya tıklayabilrisin.");
        createMeta.setLore(lore);
        lore.clear();
        createAuction.setItemMeta(createMeta);


        ItemStack vault = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        ItemMeta vaultMeta = vault.getItemMeta();
        vaultMeta.setDisplayName(ChatColor.GOLD + "Depo");
        lore.add("");
        lore.add(ChatColor.GRAY + "Satın aldığınız veya");
        lore.add(ChatColor.GRAY + "satamadığınız eşyalar");
        lore.add(ChatColor.GRAY + "buraya gelecektir.");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.YELLOW + "Bakmak için tıkla!");
        vaultMeta.setLore(lore);
        lore.clear();
        vault.setItemMeta(vaultMeta);

        inventory.setItem(11,searchForAuctions);
        inventory.setItem(13,viewBids);
        inventory.setItem(15,createAuction);
        inventory.setItem(31,vault);

        player.openInventory(inventory);
    }

    public static void createAuctionCreateMenu(Player player, Plugin plugin, int defaultDuration, int defaultBid, ItemStack alreadyPut)
    {
        AuctionInventoryHolder holder = new AuctionInventoryHolder(defaultDuration,defaultBid,alreadyPut);
        Inventory inventory = plugin.getServer().createInventory(holder, 54, ChatColor.GRAY + "Ürün koy");
        holder.setInv(inventory);


        List<String> lore = new ArrayList<>();

        ItemStack auctionItem = new ItemStack(Material.STONE_BUTTON);
        ItemMeta itemMeta = auctionItem.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "Envanterinde bir eşyaya tıkla!");
        lore.add(ChatColor.GRAY + "Satmak istediğin eşyayı buraya koy!");
        lore.add(ChatColor.GRAY + "Shift ile de tıklayabilirsin!");
        itemMeta.setLore(lore);
        lore.clear();
        auctionItem.setItemMeta(itemMeta);

        int extraForBid = defaultBid*5/100;

        ItemStack startingBid = new ItemStack(Material.GOLD_INGOT);
        ItemMeta bidMeta = startingBid.getItemMeta();
        bidMeta.setDisplayName(ChatColor.WHITE + "Başlangıç teklifi: " + ChatColor.GOLD + defaultBid + " para");
        lore.add(ChatColor.GRAY + "Eşyanızın marketteki başlangıç");
        lore.add(ChatColor.GRAY + "teklif parası. Oyuncular");
        lore.add(ChatColor.GRAY + "bu paradan fazla bir");
        lore.add(ChatColor.GRAY + "teklif yapmak zorundadırlar.");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.GRAY + "Süre bittiğinde en yüksek");
        lore.add(ChatColor.GRAY + " teklifi yapan oyuncu");
        lore.add(ChatColor.GRAY + "eşyayı almaya hak kazanır.");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.GRAY + "Extra ücret: " + ChatColor.GOLD + "+"+ extraForBid + " para " + ChatColor.YELLOW + "(%5)");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.YELLOW + "Değiştirmek için tıkla!");
        bidMeta.setLore(lore);
        lore.clear();
        startingBid.setItemMeta(bidMeta);

        int extraForDuration = 20*defaultDuration;

        ItemStack duration = new ItemStack(Material.CLOCK);
        ItemMeta durationMeta = duration.getItemMeta();
        durationMeta.setDisplayName(ChatColor.WHITE + "Süre: " + ChatColor.YELLOW + defaultDuration + " Saat");
        lore.add(ChatColor.GRAY + "Eşyanın markette ne kadar");
        lore.add(ChatColor.GRAY + "süre kalacağı.");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.GRAY + "Not: ürüne teklif yapılması");
        lore.add(ChatColor.GRAY + "süreyi otomatik olarak artırır.");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.GRAY + "Extra ücret: " + ChatColor.GOLD + "+" + extraForDuration+" para");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.YELLOW + "Değiştirmek için tıkla!");
        durationMeta.setLore(lore);
        lore.clear();
        duration.setItemMeta(durationMeta);

        int totalExtra = extraForDuration + extraForBid;

        ItemStack create = new ItemStack(Material.GREEN_WOOL);
        ItemMeta createMeta = create.getItemMeta();
        createMeta.setDisplayName(ChatColor.GREEN + "Markete koy!");
        lore.add(ChatColor.GRAY + "Bu ürün markete başka");
        lore.add(ChatColor.GRAY + "oyuncular tarafından satın");
        lore.add(ChatColor.GRAY + "alınması için konulacak.");
        lore.add(ChatColor.GRAY + "");
        if(alreadyPut == null)
            lore.add(ChatColor.GRAY + "Eşya: " + ChatColor.WHITE + "YOK");
        else
            lore.add(ChatColor.GRAY + "Eşya: " + ChatColor.WHITE + alreadyPut.getI18NDisplayName());
        lore.add(ChatColor.GRAY + "Süre: " + ChatColor.YELLOW + defaultDuration);
        lore.add(ChatColor.GRAY + "Başlangıç teklifi: " + ChatColor.GOLD + defaultBid + " para");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.GRAY + "Toplam extra ücret: " + ChatColor.GOLD + totalExtra + " para");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.YELLOW + "Yaratmak için tıkla!");
        createMeta.setLore(lore);
        lore.clear();
        create.setItemMeta(createMeta);

        ItemStack geri = new ItemStack(Material.ARROW);
        ItemMeta geriMeta = geri.getItemMeta();
        geriMeta.setDisplayName(ChatColor.GREEN + "Geri git");
        lore.add(ChatColor.GRAY + "Ana menüye geri gider.");
        geriMeta.setLore(lore);
        lore.clear();
        geri.setItemMeta(geriMeta);


        if(alreadyPut == null)
            inventory.setItem(13,auctionItem);
        else
            inventory.setItem(13,alreadyPut);

        inventory.setItem(29,create);
        inventory.setItem(31,startingBid);
        inventory.setItem(33,duration);
        inventory.setItem(49,geri);

        player.openInventory(inventory);
    }

    public static void createDurationSelectionMenu(Player player, Plugin plugin,int defaultDuration, int defaultBid, ItemStack alreadyPut)
    {
        AuctionInventoryHolder holder = new AuctionInventoryHolder(defaultDuration,defaultBid,alreadyPut);
        Inventory inventory = plugin.getServer().createInventory(holder, 36, ChatColor.GRAY + "Zaman seç");
        holder.setInv(inventory);
        List<String> lore = new ArrayList<>();

        ItemStack customDuration = new ItemStack(Material.CLOCK);
        ItemMeta customMeta = customDuration.getItemMeta();
        customMeta.setDisplayName(ChatColor.GREEN + "Kendiniz belirleyin!");
        lore.add(ChatColor.GRAY + "Özel bir süre belirlemek istiyorsanız");
        lore.add(ChatColor.GRAY + "buradan yapabilirsiniz.");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.YELLOW + "Tıklayın!");
        customMeta.setLore(lore);
        lore.clear();
        customDuration.setItemMeta(customMeta);

        ItemStack twoHours = new ItemStack(Material.RED_WOOL);
        ItemMeta twoHourMeta = twoHours.getItemMeta();
        twoHourMeta.addEnchant(Enchantment.DURABILITY,1,true);
        twoHourMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        twoHourMeta.setDisplayName(ChatColor.GREEN + "2 saat");
        lore.add(ChatColor.GRAY + "Extra ücret: " + ChatColor.GOLD + "40 para");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.YELLOW + "Seçmek için tıkla!");
        twoHourMeta.setLore(lore);
        lore.clear();
        twoHours.setItemMeta(twoHourMeta);

        ItemStack twelveHours = new ItemStack(Material.ORANGE_WOOL);
        ItemMeta twelveHourMeta = twelveHours.getItemMeta();
        twelveHourMeta.addEnchant(Enchantment.DURABILITY,1,true);
        twelveHourMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        twelveHourMeta.setDisplayName(ChatColor.GREEN + "12 saat");
        lore.add(ChatColor.GRAY + "Extra ücret: " + ChatColor.GOLD + "240 para");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.YELLOW + "Seçmek için tıkla!");
        twelveHourMeta.setLore(lore);
        lore.clear();
        twelveHours.setItemMeta(twelveHourMeta);

        ItemStack twoDays = new ItemStack(Material.PINK_WOOL);
        ItemMeta twoDaysMeta = twoDays.getItemMeta();
        twoDaysMeta.addEnchant(Enchantment.DURABILITY,1,true);
        twoDaysMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        twoDaysMeta.setDisplayName(ChatColor.GREEN + "2 gün");
        lore.add(ChatColor.GRAY + "Extra ücret: " + ChatColor.GOLD + "960 para");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.YELLOW + "Seçmek için tıkla!");
        twoDaysMeta.setLore(lore);
        lore.clear();
        twoDays.setItemMeta(twoDaysMeta);

        ItemStack geri = new ItemStack(Material.ARROW);
        ItemMeta geriMeta = geri.getItemMeta();
        geriMeta.setDisplayName(ChatColor.GREEN + "Geri git");
        lore.add(ChatColor.GRAY + "Ürün koyma menüsüne ");
        lore.add(ChatColor.GRAY + "geri gider.");
        geriMeta.setLore(lore);
        lore.clear();
        geri.setItemMeta(geriMeta);

        inventory.setItem(10, twoHours);
        inventory.setItem(12, twelveHours);
        inventory.setItem(14, twoDays);
        inventory.setItem(16, customDuration);
        inventory.setItem(31, geri);

        player.openInventory(inventory);
    }

    public static class AuctionInventoryHolder implements InventoryHolder
    {
        private int duration;
        private int startingBid;
        private Inventory inv;
        private ItemStack alreadyPut;
        private boolean changingWindows;

        public boolean isChangingWindows() {
            return changingWindows;
        }

        public void setChangingWindows(boolean changingWindows) {
            this.changingWindows = changingWindows;
        }

        public ItemStack getAlreadyPut() {
            return alreadyPut;
        }

        public void setAlreadyPut(ItemStack alreadyPut) {
            this.alreadyPut = alreadyPut;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getStartingBid() {
            return startingBid;
        }

        public void setStartingBid(int startingBid) {
            this.startingBid = startingBid;
        }

        public Inventory getInv() {
            return inv;
        }

        public void setInv(Inventory inv) {
            this.inv = inv;
        }

        AuctionInventoryHolder(int duration, int startingBid, ItemStack alreadyPut)
        {
            this.duration = duration;
            this.startingBid = startingBid;
            this.inv = null;
            this.alreadyPut = alreadyPut;
            this.changingWindows = false;
        }


        @Override
        public Inventory getInventory() {
            return inv;
        }
    }

    /*@deprecated*/
    public static String getItemCategory(ItemStack itemStack)
    {
        switch (itemStack.getType())
        {
            case STONE_SWORD:
            case NETHERITE_SWORD:
            case WOODEN_SWORD:
            case GOLDEN_SWORD:
            case DIAMOND_SWORD:
            case IRON_SWORD:
            {
                return "Sword";
            }
            case CHAINMAIL_HELMET:
            case NETHERITE_HELMET:
            case LEATHER_HELMET:
            case GOLDEN_HELMET:
            case DIAMOND_HELMET:
            case IRON_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case NETHERITE_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case IRON_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case NETHERITE_LEGGINGS:
            case LEATHER_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case IRON_LEGGINGS:
            case CHAINMAIL_BOOTS:
            case NETHERITE_BOOTS:
            case LEATHER_BOOTS:
            case GOLDEN_BOOTS:
            case DIAMOND_BOOTS:
            case IRON_BOOTS:
            {
                return "Armour";
            }
            case STONE_PICKAXE:
            case NETHERITE_PICKAXE:
            case WOODEN_PICKAXE:
            case GOLDEN_PICKAXE:
            case DIAMOND_PICKAXE:
            case IRON_PICKAXE:
            {
                return "Pickaxe";
            }
            case STONE_HOE:
            case NETHERITE_HOE:
            case WOODEN_HOE:
            case GOLDEN_HOE:
            case DIAMOND_HOE:
            case IRON_HOE:
            {
                return "Hoe";
            }
            case STONE_AXE:
            case NETHERITE_AXE:
            case WOODEN_AXE:
            case GOLDEN_AXE:
            case DIAMOND_AXE:
            case IRON_AXE:
            {
                return "Axe";
            }
            case STONE_SHOVEL:
            case NETHERITE_SHOVEL:
            case WOODEN_SHOVEL:
            case GOLDEN_SHOVEL:
            case DIAMOND_SHOVEL:
            case IRON_SHOVEL:
            {
                return "Shovel";
            }
            case ENCHANTED_BOOK:
            case POTION:
                return "Consumable";
        }
        return "Misc";
    }

    public static String getItemCategoryString(ItemStack itemStack)
    {
        String type = itemStack.getType().toString().toLowerCase();

        if(type.contains("block"))
            return "Block";
        else if(type.contains("sword"))
            return "Silahlar";
        else if(type.contains("axe"))
            return "Silahlar";
        else if(type.contains("pickaxe"))
            return "Araçlar";
        else if(type.contains("hoe"))
            return "Araçlar";
        else if(type.contains("shovel"))
            return "Araçlar";
        else if(type.contains("book"))
            return "Tüketilebilir";
        else if(type.contains("potion"))
            return "Tüketilebilir";
        else if(type.contains("cooked"))
            return "Tüketilebilir";
        else if(type.contains("helmet"))
            return "Zırhlar";
        else if(type.contains("chestplate"))
            return "Zırhlar";
        else if(type.contains("leggings"))
            return "Zırhlar";
        else if(type.contains("boots"))
            return "Zırhlar";
        else if(type.contains("shield"))
            return "Zırhlar";

        return "Misc";
    }

    public static void createAuctionSearchMenu(Player player, Plugin plugin, int page, String kategori, String tier, String sort, String likeCondition)
    {
        AuctionSearchHolder holder = new AuctionSearchHolder(page,kategori,tier, sort,likeCondition);
        Inventory inventory = plugin.getServer().createInventory(holder, 54, ChatColor.GRAY + "Ürün ara");
        holder.setInv(inventory);

        ItemStack filler = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        List<String> lore = new ArrayList<>();

        ItemStack swordCategory = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta swordMeta = swordCategory.getItemMeta();
        swordMeta.setDisplayName(ChatColor.GOLD + "Silahlar");
        swordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        lore.add(ChatColor.DARK_GRAY + "Kategori");
        lore.add("");
        lore.add(ChatColor.GRAY + "Örnekler:");
        lore.add(ChatColor.GRAY + "•Kılıçlar");
        lore.add(ChatColor.GRAY + "•Oklar");
        lore.add(ChatColor.GRAY + "•Baltalar");
        lore.add(ChatColor.GRAY + "•Sihirli silahlar");
        lore.add(ChatColor.GRAY + "");
        if(kategori != null && kategori.equalsIgnoreCase("silahlar"))
        {
            lore.add(ChatColor.GREEN + "Şu an buna göre filtreli!");
            lore.add(ChatColor.YELLOW + "Fitreyi kaldırmak için tıklayın!");
        }
        else
            lore.add(ChatColor.YELLOW + "Filtrelemek için tıklayın!");
        swordMeta.setLore(lore);
        lore.clear();
        swordCategory.setItemMeta(swordMeta);

        ItemStack armourCategory = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta armourMeta = armourCategory.getItemMeta();
        armourMeta.setDisplayName(ChatColor.AQUA + "Zırhlar");
        armourMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        lore.add(ChatColor.DARK_GRAY + "Kategori");
        lore.add("");
        lore.add(ChatColor.GRAY + "Örnekler:");
        lore.add(ChatColor.GRAY + "•Şapkalar");
        lore.add(ChatColor.GRAY + "•Göğüslükler");
        lore.add(ChatColor.GRAY + "•Pantalonlar");
        lore.add(ChatColor.GRAY + "•Ayakabbılar");
        lore.add(ChatColor.GRAY + "");
        if(kategori != null && kategori.equalsIgnoreCase("zırhlar"))
        {
            lore.add(ChatColor.GREEN + "Şu an buna göre filtreli!");
            lore.add(ChatColor.YELLOW + "Fitreyi kaldırmak için tıklayın!");
        }
        else
            lore.add(ChatColor.YELLOW + "Filtrelemek için tıklayın!");
        armourMeta.setLore(lore);
        lore.clear();
        armourCategory.setItemMeta(armourMeta);

        ItemStack accessories = new ItemStack(Material.COMPOSTER);
        ItemMeta accessoryMeta = accessories.getItemMeta();
        accessoryMeta.setDisplayName(ChatColor.GREEN + "Aksesuarlar");
        lore.add(ChatColor.DARK_GRAY + "Kategori");
        lore.add("");
        lore.add(ChatColor.GRAY + "Örnekler:");
        lore.add(ChatColor.GRAY + "•Özel görev eşyaları");
        lore.add(ChatColor.GRAY + "•Tılsımlar");
        lore.add(ChatColor.GRAY + "•Küreler");
        lore.add(ChatColor.GRAY + "•???");
        lore.add(ChatColor.GRAY + "");
        if(kategori != null && kategori.equalsIgnoreCase("aksesuarlar"))
        {
            lore.add(ChatColor.GREEN + "Şu an buna göre filtreli!");
            lore.add(ChatColor.YELLOW + "Fitreyi kaldırmak için tıklayın!");
        }
        else
            lore.add(ChatColor.YELLOW + "Filtrelemek için tıklayın!");
        accessoryMeta.setLore(lore);
        lore.clear();
        accessories.setItemMeta(accessoryMeta);

        ItemStack consumable = new ItemStack(Material.APPLE);
        ItemMeta consumableMeta = consumable.getItemMeta();
        consumableMeta.setDisplayName(ChatColor.RED + "Tüketilebilirler");
        lore.add(ChatColor.DARK_GRAY + "Kategori");
        lore.add("");
        lore.add(ChatColor.GRAY + "Örnekler:");
        lore.add(ChatColor.GRAY + "•Potlar");
        lore.add(ChatColor.GRAY + "•Yemekler");
        lore.add(ChatColor.GRAY + "•Kitaplar");
        lore.add(ChatColor.GRAY + "");
        if(kategori != null && kategori.equalsIgnoreCase("tüketilebilirler"))
        {
            lore.add(ChatColor.GREEN + "Şu an buna göre filtreli!");
            lore.add(ChatColor.YELLOW + "Fitreyi kaldırmak için tıklayın!");
        }
        else
            lore.add(ChatColor.YELLOW + "Filtrelemek için tıklayın!");
        consumableMeta.setLore(lore);
        lore.clear();
        consumable.setItemMeta(consumableMeta);

        ItemStack block = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta blockMeta = block.getItemMeta();
        blockMeta.setDisplayName(ChatColor.RED + "Blocklar");
        lore.add(ChatColor.DARK_GRAY + "Kategori");
        lore.add("");
        lore.add(ChatColor.GRAY + "Örnekler:");
        lore.add(ChatColor.GRAY + "•Herhangi bir block");
        lore.add(ChatColor.GRAY + "");
        if(kategori != null && kategori.equalsIgnoreCase("blocklar"))
        {
            lore.add(ChatColor.GREEN + "Şu an buna göre filtreli!");
            lore.add(ChatColor.YELLOW + "Fitreyi kaldırmak için tıklayın!");
        }
        else
            lore.add(ChatColor.YELLOW + "Filtrelemek için tıklayın!");
        blockMeta.setLore(lore);
        lore.clear();
        block.setItemMeta(blockMeta);

        ItemStack misc = new ItemStack(Material.BLAZE_ROD);
        ItemMeta miscMeta = misc.getItemMeta();
        miscMeta.setDisplayName(ChatColor.RED + "Miscler");
        lore.add(ChatColor.DARK_GRAY + "Kategori");
        lore.add("");
        lore.add(ChatColor.GRAY + "Örnekler:");
        lore.add(ChatColor.GRAY + "•Araçlar");
        lore.add(ChatColor.GRAY + "•Büyüler");
        lore.add(ChatColor.GRAY + "");
        if(kategori != null && kategori.equalsIgnoreCase("miscler"))
        {
            lore.add(ChatColor.GREEN + "Şu an buna göre filtreli!");
            lore.add(ChatColor.YELLOW + "Fitreyi kaldırmak için tıklayın!");
        }
        else
            lore.add(ChatColor.YELLOW + "Filtrelemek için tıklayın!");
        miscMeta.setLore(lore);
        lore.clear();
        misc.setItemMeta(miscMeta);

        ItemStack search = new ItemStack(Material.OAK_SIGN);
        ItemMeta searchMeta = search.getItemMeta();
        searchMeta.setDisplayName(ChatColor.GREEN + "Ara");
        lore.add(ChatColor.GRAY + "Ürünleri isimlerine göre");
        lore.add(ChatColor.GRAY + "arayın");
        lore.add(ChatColor.GRAY + "");
        if(likeCondition != null)
        {
            lore.add(ChatColor.DARK_AQUA + "Aranan isim:");
            lore.add(ChatColor.GRAY + likeCondition);
        }
        else
            lore.add(ChatColor.YELLOW + "Aramak için tıklayın!");
        searchMeta.setLore(lore);
        lore.clear();
        search.setItemMeta(searchMeta);

        ItemStack geri = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta meta = (PotionMeta)geri.getItemMeta();
        meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
        meta.setDisplayName(ChatColor.GREEN + "Geri");
        lore.add(ChatColor.GRAY + "Sayfa: " + Integer.toString(page+1) + "/20");
        meta.setLore(lore);
        lore.clear();
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        geri.setItemMeta(meta);

        ItemStack ileri = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta ileriMeta = (PotionMeta)ileri.getItemMeta();
        ileriMeta.setBasePotionData(new PotionData(PotionType.JUMP));
        ileriMeta.setDisplayName(ChatColor.GREEN + "İleri");
        ileriMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        lore.add(ChatColor.GRAY + "Sayfa: " + Integer.toString(page+1) + "/20");
        ileriMeta.setLore(lore);
        lore.clear();
        ileri.setItemMeta(ileriMeta);

        ItemStack tierFilter = new ItemStack(Material.ENDER_EYE);
        ItemMeta tierMeta = tierFilter.getItemMeta();
        tierMeta.setDisplayName(ChatColor.GREEN + "Eşya tier'ı");
        lore.add(""); //index 0
        lore.add(ChatColor.GRAY + "Filtre yok"); //index 1
        lore.add(ChatColor.GRAY + "Sıradan");//index 2
        lore.add(ChatColor.GRAY + "Nadir");//index 3
        lore.add(ChatColor.GRAY + "Eşsiz");//index 4
        lore.add(ChatColor.GRAY + "Olağanüstü");//index 5
        lore.add(ChatColor.GRAY + "Destansı");//index 6
        if(tier == null)
        {
            lore.remove(1);
            lore.add(1,ChatColor.DARK_GRAY + ">Filtre yok.");
        }
        else if(tier.equalsIgnoreCase("Sıradan"))
        {
            lore.remove(2);
            lore.add(2,ChatColor.WHITE + ">Sıradan");
        }
        else if(tier.equalsIgnoreCase("Nadir"))
        {
            lore.remove(3);
            lore.add(3,ChatColor.GREEN + ">Nadir");
        }
        else if(tier.equalsIgnoreCase("Eşsiz"))
        {
            lore.remove(4);
            lore.add(4,ChatColor.AQUA + ">Eşsiz");
        }
        else if(tier.equalsIgnoreCase("Olağanüstü"))
        {
            lore.remove(5);
            lore.add(5,ChatColor.DARK_PURPLE + ">Olağanüstü");
        }
        else if(tier.equalsIgnoreCase("Destansı"))
        {
            lore.remove(6);
            lore.add(6,ChatColor.GOLD + ">Destansı");
        }
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.YELLOW + "Tıklayarak filtre değiştirebilirsin!");

        tierMeta.setLore(lore);
        lore.clear();
        tierFilter.setItemMeta(tierMeta);

        ItemStack sortFilter = new ItemStack(Material.HOPPER);
        ItemMeta sortMeta = sortFilter.getItemMeta();
        sortMeta.setDisplayName(ChatColor.GREEN + "Sırala");
        lore.add(""); //index 1
        lore.add(ChatColor.GRAY + "Sıralama yok");//index 2
        lore.add(ChatColor.GRAY + "En ucuz teklif");//index 3
        lore.add(ChatColor.GRAY + "En pahallı teklif");//index 4
        lore.add(ChatColor.GRAY + "Yakında bitiyor");//index 5

        if(sort == null)
        {
            lore.remove(1);
            lore.add(1, ChatColor.WHITE + ">Sıralama yok");
        }
        else if(sort.equalsIgnoreCase("en ucuz"))
        {
            lore.remove(2);
            lore.add(2, ChatColor.AQUA + ">En ucuz teklif");
        }
        else if(sort.equalsIgnoreCase("en pahallı"))
        {
            lore.remove(3);
            lore.add(3, ChatColor.AQUA + ">En pahallı teklif");
        }
        else if(sort.equalsIgnoreCase("yakında bitiyor"))
        {
            lore.remove(4);
            lore.add(4, ChatColor.AQUA + ">Yakında bitiyor");
        }

        lore.add("");
        lore.add(ChatColor.YELLOW + "Filtreyi değiştirmek için tıkla!");

        sortMeta.setLore(lore);
        lore.clear();

        sortFilter.setItemMeta(sortMeta);


        inventory.setItem(0,swordCategory);
        inventory.setItem(9,armourCategory);
        inventory.setItem(18,accessories);
        inventory.setItem(27,consumable);
        inventory.setItem(36,block);
        inventory.setItem(45,misc);
        inventory.setItem(46,filler);
        inventory.setItem(47,filler);
        inventory.setItem(48,search);
        inventory.setItem(49,geri);
        inventory.setItem(50,filler);
        inventory.setItem(51,tierFilter);
        inventory.setItem(52,sortFilter);
        inventory.setItem(53,ileri);

        for(int i = 1; i < 9; i++)
        {
            inventory.setItem(i,filler);
        }

        for(int i = 10; i < 18; i+=7)
        {
            inventory.setItem(i,filler);
        }
        for(int i = 19; i < 27; i+=7)
        {
            inventory.setItem(i,filler);
        }
        for(int i = 28; i < 36; i+=7)
        {
            inventory.setItem(i,filler);
        }
        for(int i = 37; i < 45; i+=7)
        {
            inventory.setItem(i,filler);
        }

        try
        {
            int offset = page*24;
            String query = "select * from auction_items where EndDate>=DATE_ADD(now(),interval 5 minute) ";
            int nonNullConditions = 0;
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Kategori is: " + kategori);
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "likeCondition is: " + likeCondition);
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "tier is: " + tier);
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "sort is: " + sort);
            if(kategori != null)
            {
                query += "and category="+ "'" + kategori+ "'" +" ";
            }
            if(likeCondition != null)
            {
                query += "and Name like '%" + likeCondition + "%' ";
            }
            if(tier != null)
            {
                query += "and Quality="+ "'" + tier + "'" + " ";
            }
            if(sort != null)
            {
                if(sort.equalsIgnoreCase("en ucuz"))
                    query += "order by currentBid ASC ";
                else if(sort.equalsIgnoreCase("en pahallı"))
                    query += "order by currentBid DESC ";
                else if(sort.equalsIgnoreCase("yakında bitiyor"))
                    query += "order by EndDate ASC ";
            }
            query += "limit 24 offset " + offset;
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.AQUA + "Query is: " + query);
            PreparedStatement ps = MainClass.conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            int i = 11;
            while(rs.next())
            {
                String base64 = rs.getString("base64String");
                ItemStack toBePut = DBSaver.fromBase64(base64);
                ItemMeta tempMeta = toBePut.getItemMeta();
                List<String> newLore;
                if(tempMeta.getLore() != null)
                {
                    newLore = tempMeta.getLore();
                }
                else
                {
                    newLore = new ArrayList<>();
                }
                newLore.add("");
                newLore.add(ChatColor.DARK_GRAY + "_______________________");
                OfflinePlayer seller = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("Seller")));
                newLore.add(ChatColor.YELLOW + "Satıcı: " + seller.getName());

                int id = rs.getInt("id");

                newLore.add(ChatColor.YELLOW + "Eşya IDsi: " + id);


                int totalBids = 0;
                ps = MainClass.conn.prepareStatement("select count(*) as cnt from items_bidders where item_id=? group by item_id");
                ps.setInt(1,id);
                ResultSet temp = ps.executeQuery();
                if(temp.next())
                {
                    totalBids = temp.getInt("cnt");
                }
                ps.close();
                temp.close();
                temp = null;
                newLore.add(ChatColor.YELLOW + "Toplam teklif sayısı: " + ChatColor.AQUA + totalBids);
                newLore.add("");
                int highestBid = rs.getInt("currentBid");
                String playerName = "Yok";
                if(totalBids != 0)
                {
                    //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "We are in totalbids != 0");
                    ps = MainClass.conn.prepareStatement("select player_id from items_bidders where item_id=? and bidAmount=?");
                    ps.setInt(1,id);
                    ps.setInt(2,highestBid);
                    temp = ps.executeQuery();
                    if(temp.next())
                    {
                        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Temp did have a next.");
                        UUID sellerUUID = UUID.fromString(temp.getString("player_id"));
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(sellerUUID);
                        playerName = offlinePlayer.getName();
                    }
                }
                else
                {
                    playerName = seller.getName();
                }
                newLore.add(ChatColor.YELLOW + "En yüksek teklif: " + ChatColor.GOLD + highestBid);
                newLore.add(ChatColor.YELLOW + "Teklif eden oyuncu: " + ChatColor.WHITE + playerName);
                newLore.add("");
                Timestamp ts = rs.getTimestamp("EndDate");
                DateTime dt = new DateTime(ts);
                DateTime current = new DateTime();
                Hours hours = Hours.hoursBetween(current, dt);
                Minutes minutes = Minutes.minutesBetween(current, dt);
                Seconds seconds = Seconds.secondsBetween(current, dt);
                newLore.add(ChatColor.YELLOW + "Bitmesine: " + ChatColor.WHITE + hours.getHours()+"saat "+minutes.getMinutes()%60+"dk "+seconds.getSeconds()%60+"sn");
                tempMeta.setLore(newLore);
                toBePut.setItemMeta(tempMeta);
                inventory.setItem(i,toBePut);
                holder.addEndDate(toBePut,dt);
                i++;
                if(i == 17)
                    i = 20;
                else if(i==26)
                    i = 29;
                else if(i==35)
                    i = 38;
            }

            boolean found = false;
            for(UpdateItemMeta runnable : AuctionRunnables.runnableMap.keySet())
            {
                if(runnable.getHolderList().size() < 21)
                {
                    found = true;
                    runnable.addToHolderList(holder);
                    break;
                }
            }
            if(!found)
            {
                UpdateItemMeta newRunnable = new UpdateItemMeta();
                newRunnable.addToHolderList(holder);
                newRunnable.runTaskTimerAsynchronously(plugin,0,20);
            }

            ps.close();
            rs.close();

            ps = null;
            rs = null;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        player.openInventory(inventory);
    }

    public static void createBiddingMenu(Player player, Plugin plugin, int itemId, int page, int maxBid, String sellerName, String maxBidder)
    {
        BiddingMenuHolder biddingMenuHolder = new BiddingMenuHolder(page,itemId,maxBid, sellerName, maxBidder);
        Inventory inventory = plugin.getServer().createInventory(biddingMenuHolder, 36, ChatColor.BLUE + "Ürün teklifleri");
        biddingMenuHolder.setInv(inventory);

        ItemStack filler = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for(int i = 0; i < 9; i++)
            inventory.setItem(i,filler);

        inventory.setItem(9,filler);
        inventory.setItem(18,filler);
        inventory.setItem(27,filler);


        inventory.setItem(17,filler);
        inventory.setItem(26,filler);
        inventory.setItem(27,filler);
        inventory.setItem(29,filler);
        inventory.setItem(30,filler);
        inventory.setItem(32,filler);
        inventory.setItem(33,filler);
        inventory.setItem(35,filler);

        List<String> lore = new ArrayList<>();

        ItemStack geri = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta meta = (PotionMeta)geri.getItemMeta();
        meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
        meta.setDisplayName(ChatColor.GREEN + "Geri");
        lore.add(ChatColor.GRAY + "Sayfa: " + Integer.toString(page+1) + "/20");
        meta.setLore(lore);
        lore.clear();
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        geri.setItemMeta(meta);

        ItemStack ileri = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta ileriMeta = (PotionMeta)ileri.getItemMeta();
        ileriMeta.setBasePotionData(new PotionData(PotionType.JUMP));
        ileriMeta.setDisplayName(ChatColor.GREEN + "İleri");
        ileriMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        lore.add(ChatColor.GRAY + "Sayfa: " + Integer.toString(page+1) + "/20");
        ileriMeta.setLore(lore);
        lore.clear();
        ileri.setItemMeta(ileriMeta);

        inventory.setItem(28,geri);
        inventory.setItem(34,ileri);

        ItemStack addBid = new ItemStack(Material.DIAMOND);
        ItemMeta addBidMeta = addBid.getItemMeta();
        addBidMeta.setDisplayName(ChatColor.GOLD + "Teklif sunun!");
        lore.add("");
        lore.add(ChatColor.GRAY + "Varolan bir teklifinizi");
        lore.add(ChatColor.GRAY + "yeni bir teklif ile");
        lore.add(ChatColor.GRAY + "güncelleyin.");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.GRAY + "Veya hiç teklifiniz");
        lore.add(ChatColor.GRAY + "yoksa yeni bir teklif");
        lore.add(ChatColor.GRAY + "koyun.");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.YELLOW + "Tıklayın!");
        addBidMeta.setLore(lore);
        lore.clear();
        addBid.setItemMeta(addBidMeta);

        inventory.setItem(31,addBid);



        try
        {
            int offset = page*14;
            PreparedStatement ps = MainClass.conn.prepareStatement("select * from items_bidders where item_id=? order by bidAmount DESC limit 14 offset " + offset);
            ps.setInt(1,itemId);

            ResultSet rs = ps.executeQuery();

            int skullIndex = 10;
            while(rs.next())
            {
                UUID playerUUID = UUID.fromString(rs.getString("player_id"));
                OfflinePlayer owner = Bukkit.getOfflinePlayer(playerUUID);
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD,1);
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                skullMeta.setOwningPlayer(owner);
                skullMeta.setDisplayName(ChatColor.GREEN + owner.getName());
                lore.add("");
                lore.add(ChatColor.GRAY + "Teklif edilen para: " + ChatColor.GOLD + rs.getInt("bidAmount"));
                skullMeta.setLore(lore);
                lore.clear();
                skull.setItemMeta(skullMeta);

                inventory.setItem(skullIndex,skull);

                skullIndex++;
                if(skullIndex == 17)
                {
                    skullIndex = 19;
                }
            }

            ps.close();
            rs.close();

            ps = null;
            rs = null;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        player.openInventory(inventory);
    }

    public static class AuctionSearchHolder implements InventoryHolder, updateInterface
    {
        private int page;
        private Inventory inv;
        private String category;
        private String tier;
        private String sort;
        private UpdateItemMeta updateItemMeta;
        private HashMap<ItemStack, DateTime> endDates;
        private String likeCondition;

        public String getLikeCondition() {
            return likeCondition;
        }

        public void setLikeCondition(String likeCondition) {
            this.likeCondition = likeCondition;
        }

        public HashMap<ItemStack, DateTime> getEndDates() {
            return endDates;
        }

        public void setEndDates(HashMap<ItemStack, DateTime> endDates) {
            this.endDates = endDates;
        }

        public void addEndDate(ItemStack item, DateTime dt)
        {
            this.endDates.put(item,dt);
        }

        public UpdateItemMeta getUpdateItemMeta() {
            return updateItemMeta;
        }

        public void setUpdateItemMeta(UpdateItemMeta updateItemMeta) {
            this.updateItemMeta = updateItemMeta;
        }

        public String getSort() {
            return sort;
        }

        public void setSort(String sort) {
            this.sort = sort;
        }

        public String getTier() {
            return tier;
        }

        public void setTier(String tier) {
            this.tier = tier;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public void setInv(Inventory inv) {
            this.inv = inv;
        }

        AuctionSearchHolder(int page, String category, String tier, String sort, String likeCondition)
        {
            this.page = page;
            this.inv = null;
            this.category = category;
            this.tier = tier;
            this.sort = sort;
            this.endDates = new HashMap<>();
            this.likeCondition = likeCondition;
        }


        @Override
        public Inventory getInventory() {
            return inv;
        }
    }

    public interface updateInterface
    {
        public HashMap<ItemStack, DateTime> getEndDates();
        public Inventory getInventory();
        public void setUpdateItemMeta(UpdateItemMeta updateItemMeta);
    }

    public static class UpdateItemMeta extends BukkitRunnable {

        HashMap<updateInterface, Integer> holderList;

        public HashMap<updateInterface, Integer> getHolderList() {
            return holderList;
        }

        public void setHolderList(HashMap<updateInterface, Integer> holderList) {
            this.holderList = holderList;
        }

        UpdateItemMeta()
        {
            this.holderList = new HashMap<>();
        }

        public void addToHolderList(updateInterface holder)
        {
            this.holderList.put(holder,1);
            //MainClass.getPlugin(MainClass.class).getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.DARK_AQUA + "Task, " + this + " now has a new holder, to update. ");
            AuctionRunnables.runnableMap.put(this,holderList.size());
            holder.setUpdateItemMeta(this);
            //MainClass.getPlugin(MainClass.class).getServer().getConsoleSender().sendMessage("New holderList is, " + holderList);
            //MainClass.getPlugin(MainClass.class).getServer().getConsoleSender().sendMessage("TaskMap is, " + AuctionRunnables.runnableMap);
        }

        public void removeFromHolderList(updateInterface holder)
        {
            //MainClass.getPlugin(MainClass.class).getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GOLD + "Holder, " + holder + " has been removed from the runnable's holder list. ");
            this.holderList.remove(holder);
            //MainClass.getPlugin(MainClass.class).getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GOLD + "New holderlist: " + holderList);

            if(holderList.size() > 0)
                AuctionRunnables.runnableMap.put(this,holderList.size());
            else
            {
                //MainClass.getPlugin(MainClass.class).getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.AQUA + "Runnable holder list is now empty, canceling the runnable.");
                cancel();
                AuctionRunnables.runnableMap.remove(this);
            }
            //MainClass.getPlugin(MainClass.class).getServer().getConsoleSender().sendMessage("TaskMap is, " + AuctionRunnables.runnableMap);
        }


        @Override
        public void run() {
            for (updateInterface holder : holderList.keySet()) {
                Inventory inv = holder.getInventory();
                DateTime current = new DateTime();
                DateTime date = null;
                for(int invIndex = 0; invIndex < inv.getSize(); invIndex++)
                {
                    ItemStack item = inv.getItem(invIndex);
                    if (item != null)
                    {
                        ItemMeta temp = item.getItemMeta();
                        List<String> lore = temp.getLore();
                        if (lore != null)
                        {
                            for(int index = 0; index < lore.size(); index++)
                            {
                                String s = lore.get(index);
                                if(s.contains("Bitmesine"))
                                {
                                    date = holder.getEndDates().get(item);
                                    holder.getEndDates().remove(item);
                                    Hours hours = Hours.hoursBetween(current, date);
                                    Minutes minutes = Minutes.minutesBetween(current, date);
                                    Seconds seconds = Seconds.secondsBetween(current, date);

                                    s = ChatColor.YELLOW + "Bitmesine: " + ChatColor.WHITE + hours.getHours()+"saat "+minutes.getMinutes()%60+"dk "+seconds.getSeconds()%60+"sn";
                                    lore.set(index,s);
                                    break;
                                }
                            }
                            temp.setLore(lore);
                            item.setItemMeta(temp);
                            inv.setItem(invIndex,item);
                            holder.getEndDates().put(item,date);
                        }
                    }
                }
            }
        }
    }

    public static class BiddingMenuHolder implements InventoryHolder
    {
        private int page;
        private int itemId;
        private Inventory inv;
        private int maxBid;
        private String sellerName;
        private String maxBidder;
        private UpdateItemMeta updateItemMeta;

        public UpdateItemMeta getUpdateItemMeta() {
            return updateItemMeta;
        }

        public void setUpdateItemMeta(UpdateItemMeta updateItemMeta) {
            this.updateItemMeta = updateItemMeta;
        }

        public String getMaxBidder() {
            return maxBidder;
        }

        public void setMaxBidder(String maxBidder) {
            this.maxBidder = maxBidder;
        }

        public String getSellerName() {
            return sellerName;
        }

        public void setSellerName(String sellerName) {
            this.sellerName = sellerName;
        }

        public int getMaxBid() {
            return maxBid;
        }

        public void setMaxBid(int maxBid) {
            this.maxBid = maxBid;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getItemId() {
            return itemId;
        }

        public void setItemId(int itemId) {
            this.itemId = itemId;
        }

        public void setInv(Inventory inv) {
            this.inv = inv;
        }

        BiddingMenuHolder(int page, int itemId, int maxBid, String sellerName, String maxBidder)
        {
            this.page = page;
            this.itemId = itemId;
            this.inv = null;
            this.maxBid = maxBid;
            this.sellerName = sellerName;
            this.maxBidder = maxBidder;
        }

        @Override
        public Inventory getInventory() {
            return inv;
        }
    }

    public static void createItemVault(Player player, Plugin plugin, int page)
    {
        VaultHolder vaultHolder = new VaultHolder(page);
        Inventory inventory = plugin.getServer().createInventory(vaultHolder, 45, ChatColor.DARK_GREEN + "Ürünleriniz");
        vaultHolder.setInventory(inventory);

        List<String> lore = new ArrayList<>();

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        ItemStack geri = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta meta = (PotionMeta)geri.getItemMeta();
        meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
        meta.setDisplayName(ChatColor.GREEN + "Geri");
        lore.add(ChatColor.GRAY + "Sayfa: " + Integer.toString(page+1) + "/5");
        meta.setLore(lore);
        lore.clear();
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        geri.setItemMeta(meta);

        ItemStack ileri = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta ileriMeta = (PotionMeta)ileri.getItemMeta();
        ileriMeta.setBasePotionData(new PotionData(PotionType.JUMP));
        ileriMeta.setDisplayName(ChatColor.GREEN + "İleri");
        ileriMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        lore.add(ChatColor.GRAY + "Sayfa: " + Integer.toString(page+1) + "/5");
        ileriMeta.setLore(lore);
        lore.clear();
        ileri.setItemMeta(ileriMeta);

        for(int i = 0; i < 9; i++)
        {
            inventory.setItem(i,filler);
        }

        inventory.setItem(9,filler);
        inventory.setItem(18,filler);
        inventory.setItem(27,filler);


        inventory.setItem(17,filler);
        inventory.setItem(26,filler);
        inventory.setItem(35,filler);

        for(int i = 36; i < 45; i++)
        {
            if(i == 39)
                inventory.setItem(i,geri);
            else if(i==41)
                inventory.setItem(i,ileri);
            else
                inventory.setItem(i,filler);
        }


        int itemIndex = 10;

        try
        {
            String playerUUID = String.valueOf(player.getUniqueId());

            int offset = 21*page;
            PreparedStatement ps = MainClass.conn.prepareStatement("select base64String from player_boughtItems where player_id=? and page=? limit 21 ");
            ps.setString(1,playerUUID);
            ps.setInt(2,(page+1));
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                //Put the items the player owns into the chest.
                String base64 = rs.getString("base64String");
                ItemStack toBePut = DBSaver.fromBase64(base64);
                inventory.setItem(itemIndex,toBePut);

                itemIndex++;
                if(itemIndex == 17)
                    itemIndex = 19;
                if(itemIndex == 26)
                    itemIndex = 28;
            }

            rs.close();
            ps.close();

            rs = null;
            ps = null;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        player.openInventory(inventory);

    }

    public static class VaultHolder implements InventoryHolder
    {
        private int page;
        private Inventory inventory;

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        VaultHolder(int page)
        {
            this.page = page;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    public static class OldBidMenuHolder implements InventoryHolder, updateInterface
    {
        private int page;
        private Player player;
        private Inventory inventory;
        private UpdateItemMeta updateItemMeta;
        private HashMap<ItemStack, DateTime> endDates;

        public void addEndDate(ItemStack item, DateTime dt)
        {
            this.endDates.put(item,dt);
        }

        public UpdateItemMeta getUpdateItemMeta() {
            return updateItemMeta;
        }

        public void setUpdateItemMeta(UpdateItemMeta updateItemMeta) {
            this.updateItemMeta = updateItemMeta;
        }

        public HashMap<ItemStack, DateTime> getEndDates() {
            return endDates;
        }

        public void setEndDates(HashMap<ItemStack, DateTime> endDates) {
            this.endDates = endDates;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public Player getPlayer() {
            return player;
        }

        public void setPlayer(Player player) {
            this.player = player;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        OldBidMenuHolder(int page, Player player)
        {
            this.page = page;
            this.player = player;
            this.updateItemMeta = null;
            this.endDates = new HashMap<>();
        }

    }

    public static void createOldBidMenu(Player player, Plugin plugin, int page)
    {
        OldBidMenuHolder holder = new OldBidMenuHolder(page, player);
        Inventory inventory = plugin.getServer().createInventory(holder, 45, ChatColor.DARK_GREEN + "Teklifde bulunduğunuz ürünler");
        holder.setInventory(inventory);

        List<String> lore = new ArrayList<>();
        ItemStack filler = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for(int i = 0; i < 9; i++)
        {
            inventory.setItem(i,filler);
        }

        inventory.setItem(9,filler);
        inventory.setItem(18,filler);
        inventory.setItem(27,filler);


        inventory.setItem(17,filler);
        inventory.setItem(26,filler);
        inventory.setItem(35,filler);

        ItemStack geri = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta meta = (PotionMeta)geri.getItemMeta();
        meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
        meta.setDisplayName(ChatColor.GREEN + "Geri");
        lore.add(ChatColor.GRAY + "Sayfa: " + Integer.toString(page+1) + "/20");
        meta.setLore(lore);
        lore.clear();
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        geri.setItemMeta(meta);

        ItemStack ileri = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta ileriMeta = (PotionMeta)ileri.getItemMeta();
        ileriMeta.setBasePotionData(new PotionData(PotionType.JUMP));
        ileriMeta.setDisplayName(ChatColor.GREEN + "İleri");
        ileriMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        lore.add(ChatColor.GRAY + "Sayfa: " + Integer.toString(page+1) + "/20");
        ileriMeta.setLore(lore);
        lore.clear();
        ileri.setItemMeta(ileriMeta);

        for(int i = 36; i < 45; i++)
        {
            if(i == 39)
                inventory.setItem(i,geri);
            else if(i==41)
                inventory.setItem(i,ileri);
            else
                inventory.setItem(i,filler);
        }

        try
        {
            int offset = 21*page;
            PreparedStatement ps = MainClass.conn.prepareStatement("select item_id, bidAmount from items_bidders where player_id=? limit 21 offset " + offset); //Get all the bids of the player.
            ps.setString(1, String.valueOf(player.getUniqueId()));
            ResultSet firstRs = ps.executeQuery();
            int itemIndex = 10;
            while (firstRs.next())
            {
                //Iterate over the item id's
                //First get item's details.

                ps = MainClass.conn.prepareStatement("select Seller,currentBid,EndDate,base64String from auction_items where id=? and EndDate>=DATE_ADD(now(),interval 5 minute)");
                ps.setInt(1,firstRs.getInt("item_id"));
                ResultSet rs = ps.executeQuery();
                if(rs.next())
                {
                    String base64 = rs.getString("base64String");
                    ItemStack toBePut = DBSaver.fromBase64(base64);
                    ItemMeta tempMeta = toBePut.getItemMeta();
                    List<String> newLore;
                    if(tempMeta.getLore() != null)
                    {
                        newLore = tempMeta.getLore();
                    }
                    else
                    {
                        newLore = new ArrayList<>();
                    }
                    newLore.add("");
                    newLore.add(ChatColor.DARK_GRAY + "_______________________");
                    OfflinePlayer seller = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("Seller")));
                    newLore.add(ChatColor.YELLOW + "Satıcı: " + seller.getName());
                    int id = firstRs.getInt("item_id");

                    newLore.add(ChatColor.YELLOW + "Eşya IDsi: " + id);


                    int totalBids = 0;
                    ps = MainClass.conn.prepareStatement("select count(*) as cnt from items_bidders where item_id=? group by item_id");
                    ps.setInt(1,id);
                    ResultSet temp = ps.executeQuery();
                    if(temp.next())
                    {
                        totalBids = temp.getInt("cnt");
                    }
                    ps.close();
                    temp.close();
                    temp = null;
                    newLore.add(ChatColor.YELLOW + "Toplam teklif sayısı: " + ChatColor.AQUA + totalBids);
                    newLore.add("");
                    int highestBid = rs.getInt("currentBid");
                    String playerName = "Yok";
                    if(totalBids != 0)
                    {
                        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "We are in totalbids != 0");
                        ps = MainClass.conn.prepareStatement("select player_id from items_bidders where item_id=? and bidAmount=?");
                        ps.setInt(1,id);
                        ps.setInt(2,highestBid);
                        temp = ps.executeQuery();
                        if(temp.next())
                        {
                            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Temp did have a next.");
                            UUID sellerUUID = UUID.fromString(temp.getString("player_id"));
                            playerName = Bukkit.getOfflinePlayer(sellerUUID).getName();
                        }
                    }
                    else
                    {
                        playerName = seller.getName();
                    }
                    newLore.add(ChatColor.YELLOW + "En yüksek teklif: " + ChatColor.GOLD + highestBid);
                    newLore.add(ChatColor.YELLOW + "Teklif eden oyuncu: " + ChatColor.WHITE + playerName);
                    newLore.add(ChatColor.DARK_AQUA + "Sizin teklifiniz: " + ChatColor.GOLD + firstRs.getInt("bidAmount"));
                    newLore.add("");
                    Timestamp ts = rs.getTimestamp("EndDate");
                    DateTime dt = new DateTime(ts);
                    DateTime current = new DateTime();
                    Hours hours = Hours.hoursBetween(current, dt);
                    Minutes minutes = Minutes.minutesBetween(current, dt);
                    Seconds seconds = Seconds.secondsBetween(current, dt);
                    newLore.add(ChatColor.YELLOW + "Bitmesine: " + ChatColor.WHITE + hours.getHours()+"saat "+minutes.getMinutes()%60+"dk "+seconds.getSeconds()%60+"sn");
                    tempMeta.setLore(newLore);
                    toBePut.setItemMeta(tempMeta);
                    inventory.setItem(itemIndex,toBePut);
                    holder.addEndDate(toBePut,dt);

                    itemIndex++;
                    if(itemIndex == 17)
                        itemIndex = 19;
                    if(itemIndex == 26)
                        itemIndex = 28;
                }
                rs.close();
                rs = null;
            }

            boolean found = false;
            for(UpdateItemMeta runnable : AuctionRunnables.runnableMap.keySet())
            {
                if(runnable.getHolderList().size() < 21)
                {
                    found = true;
                    runnable.addToHolderList(holder);
                    break;
                }
            }
            if(!found)
            {
                UpdateItemMeta newRunnable = new UpdateItemMeta();
                newRunnable.addToHolderList(holder);
                newRunnable.runTaskTimerAsynchronously(plugin,0,20);
            }




            ps.close();
            firstRs.close();

            ps = null;
            firstRs = null;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        player.openInventory(inventory);
    }

    public static void createDemirciInventory(Player player, Plugin plugin)
    {
        Inventory inventory = plugin.getServer().createInventory(new NPCShopMenuHolder(), 45, ChatColor.YELLOW + "Demirci");

        ItemStack filler = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);


        for(int i = 0; i < 10; i++)
        {
            inventory.setItem(i,filler);
        }

        for(int i = 17; i < 28; i++)
        {
            inventory.setItem(i,filler);
        }

        for(int i = 35; i < 45; i++)
        {
            inventory.setItem(i,filler);
        }


        ItemStack stoneSword = CommonItems.Equipment.commonStoneSword();
        ItemMeta stoneSwordMeta = stoneSword.getItemMeta();
        List<String> lore = stoneSwordMeta.getLore();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + CommonItems.ItemPrices.commonStoneSwordPrice);
        stoneSwordMeta.setLore(lore);
        lore.clear();
        stoneSword.setItemMeta(stoneSwordMeta);

        ItemStack chainHelmet =  CommonItems.Equipment.commonChainHelmet();
        ItemMeta chainHelmetMeta = chainHelmet.getItemMeta();
        lore = chainHelmetMeta.getLore();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + CommonItems.ItemPrices.commonChainHelmetPrice);
        chainHelmetMeta.setLore(lore);
        lore.clear();
        chainHelmet.setItemMeta(chainHelmetMeta);

        ItemStack chainPlate = CommonItems.Equipment.commonChainPlate();
        ItemMeta chainPlateMeta = chainPlate.getItemMeta();
        lore = chainPlateMeta.getLore();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + CommonItems.ItemPrices.commonChainPlatePrice);
        chainPlateMeta.setLore(lore);
        lore.clear();
        chainPlate.setItemMeta(chainPlateMeta);

        ItemStack chainLeggings = CommonItems.Equipment.commonChainLeggings();
        ItemMeta chainLegginsMeta = chainLeggings.getItemMeta();
        lore = chainLegginsMeta.getLore();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + CommonItems.ItemPrices.commonChainLeggingsPrice);
        chainLegginsMeta.setLore(lore);
        lore.clear();
        chainLeggings.setItemMeta(chainLegginsMeta);

        ItemStack chainBoots = CommonItems.Equipment.commonChainBoots();
        ItemMeta chainBootsMeta = chainBoots.getItemMeta();
        lore = chainBootsMeta.getLore();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + CommonItems.ItemPrices.commonChainBootsPrice);
        chainBootsMeta.setLore(lore);
        lore.clear();
        chainBoots.setItemMeta(chainBootsMeta);



        ItemStack ironSword = CommonItems.Equipment.commonIronSword();
        ItemMeta ironSwordMeta = ironSword.getItemMeta();
        lore = ironSwordMeta.getLore();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + CommonItems.ItemPrices.commonIronSwordPrice);
        ironSwordMeta.setLore(lore);
        lore.clear();
        ironSword.setItemMeta(ironSwordMeta);

        ItemStack ironHelmet = CommonItems.Equipment.commonIronHelmet();
        ItemMeta ironHelmetMeta = ironHelmet.getItemMeta();
        lore = ironHelmetMeta.getLore();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + CommonItems.ItemPrices.commonIronHelmetPrice);
        ironHelmetMeta.setLore(lore);
        lore.clear();
        ironHelmet.setItemMeta(ironHelmetMeta);

        ItemStack ironPlate = CommonItems.Equipment.commonIronPlate();
        ItemMeta ironPlateMeta = ironPlate.getItemMeta();
        lore = ironPlateMeta.getLore();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + CommonItems.ItemPrices.commonIronPlatePrice);
        ironPlateMeta.setLore(lore);
        lore.clear();
        ironPlate.setItemMeta(ironPlateMeta);

        ItemStack ironLeggings = CommonItems.Equipment.commonIronLeggings();
        ItemMeta ironLeggingsMeta = ironLeggings.getItemMeta();
        lore = ironLeggingsMeta.getLore();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + CommonItems.ItemPrices.commonIronLeggingsPrice);
        ironLeggingsMeta.setLore(lore);
        lore.clear();
        ironLeggings.setItemMeta(ironLeggingsMeta);

        ItemStack ironBoots = CommonItems.Equipment.commonIronBoots();
        ItemMeta ironBootsMeta = ironBoots.getItemMeta();
        lore = ironBootsMeta.getLore();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + CommonItems.ItemPrices.commonIronBootsPrice);
        ironBootsMeta.setLore(lore);
        lore.clear();
        ironBoots.setItemMeta(ironBootsMeta);

        ItemStack shield = CustomItems.createCustomShield("Kalkan", CustomItems.Enderlik.SIRADAN);
        ItemMeta shieldMeta = shield.getItemMeta();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + CommonItems.ItemPrices.commonShieldPrice);
        shieldMeta.setLore(lore);
        lore.clear();
        shield.setItemMeta(shieldMeta);


        inventory.setItem(11, stoneSword);
        inventory.setItem(12, chainHelmet);
        inventory.setItem(13, chainPlate);
        inventory.setItem(14, chainLeggings);
        inventory.setItem(15, chainBoots);

        inventory.setItem(29, ironSword);
        inventory.setItem(30, ironHelmet);
        inventory.setItem(31, ironPlate);
        inventory.setItem(32, ironLeggings);
        inventory.setItem(33, ironBoots);
        inventory.setItem(34, shield);

        player.openInventory(inventory);

    }

    public static void createOduncuInventory(Player player, Plugin plugin)
    {
        Inventory inventory = plugin.getServer().createInventory(new NPCShopMenuHolder(), 45, ChatColor.DARK_AQUA + "Oduncu");

        ItemStack filler = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);


        for(int i = 0; i < 10; i++)
        {
            inventory.setItem(i,filler);
        }

        for(int i = 17; i < 28; i++)
        {
            if(i == 19 || i == 22)
                continue;
            inventory.setItem(i,filler);
        }

        for(int i = 35; i < 45; i++)
        {
            inventory.setItem(i,filler);
        }


        ItemStack stoneSword = CommonItems.Equipment.commonStoneAxe();
        ItemMeta stoneSwordMeta = stoneSword.getItemMeta();
        List<String> lore = stoneSwordMeta.getLore();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + CommonItems.ItemPrices.commonStoneAxePrice);
        stoneSwordMeta.setLore(lore);
        lore.clear();
        stoneSword.setItemMeta(stoneSwordMeta);

        ItemStack ironAxe = CommonItems.createCommonItem(Material.IRON_AXE,"Demir Balta",true,9,0.9,true,CommonItems.ItemPrices.commonIronAxePrice);
        ItemMeta ironAxeMeta = ironAxe.getItemMeta();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + CommonItems.ItemPrices.commonIronAxePrice);
        stoneSwordMeta.setLore(lore);
        lore.clear();

        ItemStack elma = CommonItems.createElma();
        ItemMeta elmaMeta = elma.getItemMeta();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + CommonItems.ItemPrices.commonApplePrice);
        elmaMeta.setLore(lore);
        lore.clear();
        elma.setItemMeta(elmaMeta);


        inventory.setItem(14, CommonItems.createCommonItem(Material.BIRCH_LOG,"Huş Kütüğü",false,0,0,true,CommonItems.ItemPrices.commonLogPrice));
        inventory.setItem(15, CommonItems.createCommonItem(Material.JUNGLE_LOG,"Orman Ağacı Kütüğü",false,0,0,true,CommonItems.ItemPrices.commonLogPrice));
        inventory.setItem(16, CommonItems.createCommonItem(Material.ACACIA_LOG,"Akasya Kütüğü",false,0,0,true,CommonItems.ItemPrices.commonLogPrice));
        inventory.setItem(23, CommonItems.createCommonItem(Material.SPRUCE_LOG,"Ladin Kütüğü",false,0,0,true,CommonItems.ItemPrices.commonLogPrice));
        inventory.setItem(24, CommonItems.createCommonItem(Material.OAK_LOG,"Meşe Kütüğü",false,0,0,true,CommonItems.ItemPrices.commonLogPrice));
        inventory.setItem(25, CommonItems.createCommonItem(Material.DARK_OAK_LOG,"Koyu Meşe Kütüğü",false,0,0,true,CommonItems.ItemPrices.commonLogPrice));
        inventory.setItem(29, elma);
        inventory.setItem(32, CommonItems.createCommonItem(Material.WARPED_STEM,"Çarpık Kök",false,0,0,true,CommonItems.ItemPrices.commonLogPrice));
        inventory.setItem(33, CommonItems.createCommonItem(Material.CRIMSON_STEM,"Kızıl Kök",false,0,0,true,CommonItems.ItemPrices.commonLogPrice));
        inventory.setItem(20, stoneSword);
        inventory.setItem(21, ironAxe);


        player.openInventory(inventory);

    }

    public static void createMadenciInventory(Player player, Plugin plugin)
    {
        Inventory inventory = plugin.getServer().createInventory(new NPCShopMenuHolder(), 45, ChatColor.GRAY + "Madenci");

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);


        for(int i = 0; i < 10; i++)
        {
            inventory.setItem(i,filler);
        }

        inventory.setItem(13,filler);
        inventory.setItem(14,filler);

        inventory.setItem(22,filler);
        inventory.setItem(23,filler);

        inventory.setItem(31,filler);
        inventory.setItem(32,filler);

        for(int i = 17; i < 28; i++)
        {
            if(i == 24 || i == 25)
                continue;
            inventory.setItem(i,filler);
        }

        for(int i = 35; i < 45; i++)
        {
            inventory.setItem(i,filler);
        }

        inventory.setItem(10,CommonItems.createCommonItem(Material.STONE_PICKAXE,"Taş Kazma",true,3,1.2,true,CommonItems.ItemPrices.commonStonePickAxePrice));
        inventory.setItem(11,CommonItems.createCommonItem(Material.STONE_SHOVEL,"Taş Kürek",true,3.5,1,true,CommonItems.ItemPrices.commonStoneShovelPrice));
        inventory.setItem(12,CommonItems.createCommonItem(Material.STONE_HOE,"Taş Çapa",true,1,2,true,CommonItems.ItemPrices.commonStoneHoePrice));

        inventory.setItem(28,CommonItems.createCommonItem(Material.IRON_PICKAXE,"Demir Kazma",true,4,1.2,true,CommonItems.ItemPrices.commonIronPickAxePrice));
        inventory.setItem(29,CommonItems.createCommonItem(Material.IRON_SHOVEL,"Demir Kürek",true,4.5,1,true,CommonItems.ItemPrices.commonIronShovelPrice));
        inventory.setItem(30,CommonItems.createCommonItem(Material.IRON_HOE,"Demir Çapa",true,1,3,true,CommonItems.ItemPrices.commonIronHoePrice));


        //ORES:

        inventory.setItem(15,CommonItems.createCommonItem(Material.REDSTONE,"Kızıl Taş Tozu",false,0,0,true,CommonItems.ItemPrices.commonRedStonePrice));
        inventory.setItem(16,CommonItems.createCommonItem(Material.LAPIS_LAZULI,"Lapis Lazuli",false,0,0,true,CommonItems.ItemPrices.commonLapisPrice));

        inventory.setItem(24,CommonItems.createCommonItem(Material.COAL,"Kömür",false,0,0,true,CommonItems.ItemPrices.commonCoalPrice));
        inventory.setItem(25,CommonItems.createCommonItem(Material.GOLD_INGOT,"Altın Külçesi",false,0,0,true,CommonItems.ItemPrices.commonGoldIngotPrice));

        inventory.setItem(33,CommonItems.createCommonItem(Material.DIAMOND,"Elmas",false,0,0,true,CommonItems.ItemPrices.commonDiamondPrice));
        inventory.setItem(34,CommonItems.createCommonItem(Material.IRON_INGOT,"Demir Külçesi",false,0,0,true,CommonItems.ItemPrices.commonIronIngotPrice));

        player.openInventory(inventory);
    }

    public static void createMarketNPCWelcomeInventory(Player player, Plugin plugin, String NPCName)
    {
        NPCWelcomeMenuHolder holder = new NPCWelcomeMenuHolder(NPCName);

        Inventory inventory = plugin.getServer().createInventory(holder, 27, ChatColor.YELLOW + "Market");

        ItemStack filler = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        List<String> lore = new ArrayList<>();

        ItemStack gold = new ItemStack(Material.GOLD_INGOT);
        ItemMeta goldMeta = gold.getItemMeta();
        goldMeta.setDisplayName(ChatColor.GREEN + "Eşyalara göz at");
        goldMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        goldMeta.addEnchant(Enchantment.DURABILITY,1,true);
        lore.add("");
        lore.add(ChatColor.GRAY + "Bu NPC'nin sattığı");
        lore.add(ChatColor.GRAY + "eşyalara göz atın.");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Tıkla!");
        goldMeta.setLore(lore);
        lore.clear();
        gold.setItemMeta(goldMeta);

        Material material = null;
        String displayName;

        switch (NPCName.toLowerCase())
        {
            case "demirci":
            {
                material = Material.ANVIL;
                displayName = ChatColor.YELLOW + "Demirci olun!";
                break;
            }
            case "oduncu":
            {
                material = Material.DIAMOND_AXE;
                displayName = ChatColor.DARK_AQUA + "Oduncu olun!";
                break;
            }
            case "madenci":
            {
                material = Material.DIAMOND_PICKAXE;
                displayName = ChatColor.YELLOW + "Madenci olun!";
                break;
            }
            case "biyolog":
            {
                material = Material.PINK_TULIP;
                displayName = ChatColor.YELLOW + "Biyolog olun!";
                break;
            }
            default:
                material = Material.EMERALD;
                displayName = "???";
        }

        ItemStack job = new ItemStack(material);
        ItemMeta jobMeta = job.getItemMeta();
        jobMeta.setDisplayName(displayName);
        jobMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        jobMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        jobMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        jobMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        jobMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        lore.add("");
        lore.add(ChatColor.GRAY + "Bir " + NPCName + " olmak");
        lore.add(ChatColor.GRAY + "için tıklayın!");
        lore.add("");
        lore.add(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "Çok yakında geliyor!");
        jobMeta.setLore(lore);
        lore.clear();
        job.setItemMeta(jobMeta);

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta bookMeta = book.getItemMeta();
        bookMeta.setDisplayName(ChatColor.GOLD + "Görevler");
        bookMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        bookMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        bookMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        bookMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        lore.add("");
        lore.add(ChatColor.GRAY + "Bu NPC tarafından");
        lore.add(ChatColor.GRAY + "verilen görevlere göz atın.");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "Çok yakında geliyor!");
        bookMeta.setLore(lore);
        lore.clear();
        book.setItemMeta(bookMeta);

        for(int i = 0; i < 9; i++)
        {
            inventory.setItem(i,filler);
        }

        inventory.setItem(9,filler);
        inventory.setItem(17,filler);

        for(int i = 18; i < 27; i++)
        {
            inventory.setItem(i,filler);
        }

        inventory.setItem(11,gold);
        inventory.setItem(13,job);
        inventory.setItem(15,book);


        player.openInventory(inventory);

    }

    public static class NPCWelcomeMenuHolder implements InventoryHolder
    {
        private String NPCName;

        public String getNPCName() {
            return NPCName;
        }

        public void setNPCName(String NPCName) {
            this.NPCName = NPCName;
        }

        NPCWelcomeMenuHolder(String NPCName)
        {
            this.NPCName = NPCName;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
    public static class NPCShopMenuHolder implements InventoryHolder
    {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public static void createBiyologInventory(Player player, Plugin plugin)
    {
        Inventory inventory = plugin.getServer().createInventory(new NPCShopMenuHolder(), 45, ChatColor.DARK_GREEN + "Biyolog");

        ItemStack filler = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for(int i = 0; i < 9; i++)
        {
            inventory.setItem(i,filler);
        }

        inventory.setItem(9, filler);
        inventory.setItem(13, filler);
        inventory.setItem(17, filler);
        inventory.setItem(18, filler);
        inventory.setItem(22, filler);
        inventory.setItem(26, filler);
        inventory.setItem(27, filler);
        inventory.setItem(31, filler);
        inventory.setItem(35, filler);

        for(int i = 36; i < 45; i++)
        {
            inventory.setItem(i,filler);
        }

        inventory.setItem(10, CommonItems.createCommonItem(Material.HORN_CORAL_FAN,"Boynuz Yelpaze Mercanı",false,0,0,true,CommonItems.ItemPrices.commonBiyologMiscPrice));
        inventory.setItem(11, CommonItems.createCommonItem(Material.TUBE_CORAL_FAN,"Tüp Mercanı Yelpazesi",false,0,0,true,CommonItems.ItemPrices.commonBiyologMiscPrice));
        inventory.setItem(12, CommonItems.createCommonItem(Material.BRAIN_CORAL_FAN,"Beyin Mercanı Yelpazesi",false,0,0,true,CommonItems.ItemPrices.commonBiyologMiscPrice));
        inventory.setItem(19, CommonItems.createCommonItem(Material.BUBBLE_CORAL_FAN,"Baloncuk Mercanı Yelpazesi",false,0,0,true,CommonItems.ItemPrices.commonBiyologMiscPrice));
        inventory.setItem(20, CommonItems.createCommonItem(Material.FIRE_CORAL_FAN,"Ateş Mercanı Yelpazesi",false,0,0,true,CommonItems.ItemPrices.commonBiyologMiscPrice));
        inventory.setItem(21, CommonItems.createCommonItem(Material.VINE,"Sarmaşık",false,0,0,true,CommonItems.ItemPrices.commonBiyologMiscPrice));
        inventory.setItem(28, CommonItems.createCommonItem(Material.LILY_PAD,"Nilüfer",false,0,0,true,CommonItems.ItemPrices.commonBiyologMiscPrice));
        inventory.setItem(29, CommonItems.createCommonItem(Material.BAMBOO,"Bambu",false,0,0,true,CommonItems.ItemPrices.commonBiyologMiscPrice));


        inventory.setItem(14, CommonItems.createCommonItem(Material.DARK_OAK_SAPLING,"Koyu Meşe Fidanı",false,0,0,true,CommonItems.ItemPrices.commonSaplingPrice));
        inventory.setItem(15, CommonItems.createCommonItem(Material.OAK_SAPLING,"Meşe Fidanı",false,0,0,true,CommonItems.ItemPrices.commonSaplingPrice));
        inventory.setItem(16, CommonItems.createCommonItem(Material.SPRUCE_SAPLING,"Ladin Fidanı",false,0,0,true,CommonItems.ItemPrices.commonSaplingPrice));
        inventory.setItem(23, CommonItems.createCommonItem(Material.BIRCH_SAPLING,"Huş Fidanı",false,0,0,true,CommonItems.ItemPrices.commonSaplingPrice));
        inventory.setItem(24, CommonItems.createCommonItem(Material.JUNGLE_SAPLING,"Orman Ağacı Fidanı",false,0,0,true,CommonItems.ItemPrices.commonSaplingPrice));
        inventory.setItem(25, CommonItems.createCommonItem(Material.ACACIA_SAPLING,"Akasya Fidanı",false,0,0,true,CommonItems.ItemPrices.commonSaplingPrice));
        ItemStack potion = CustomItems.createCustomPotion("Ani İyileştirme İksiri",PotionType.INSTANT_HEAL, CustomItems.Enderlik.SIRADAN,Material.POTION);
        ItemMeta potionMeta = potion.getItemMeta();
        List<String> lore = potionMeta.getLore();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + CommonItems.ItemPrices.commonHealPotionPrice);
        potionMeta.setLore(lore);
        potion.setItemMeta(potionMeta);
        inventory.setItem(30, potion);

        player.openInventory(inventory);
    }

    public static void createTeleportUI(Player player, Plugin plugin)
    {
        Inventory inventory = plugin.getServer().createInventory(null, 27, ChatColor.DARK_AQUA + "Isınlanma Menusu");

        List<String> lore = new ArrayList<>();

        ItemStack filler = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        ItemStack summoner = new ItemStack(Material.WITHER_SKELETON_SKULL);
        ItemMeta summonerMeta = summoner.getItemMeta();
        summonerMeta.setDisplayName(ChatColor.DARK_PURPLE + "Summoner Hub'ı");
        lore.add("");
        lore.add(ChatColor.AQUA + "Işınlanmak için tıklayın!");
        summonerMeta.setLore(lore);
        lore.clear();
        summoner.setItemMeta(summonerMeta);


        ItemStack warrior = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta warriorMeta = warrior.getItemMeta();
        warriorMeta.setDisplayName(ChatColor.BLUE + "Warrior Hub'ı");
        warriorMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        warriorMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        warriorMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        lore.add("");
        lore.add(ChatColor.AQUA + "Işınlanmak için tıklayın!");
        warriorMeta.setLore(lore);
        lore.clear();
        warrior.setItemMeta(warriorMeta);


        ItemStack cleric = new ItemStack(Material.SHIELD);
        ItemMeta clericMeta = cleric.getItemMeta();
        clericMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        clericMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        clericMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        clericMeta.setDisplayName(ChatColor.YELLOW + "Cleric Hub'ı");
        lore.add("");
        lore.add(ChatColor.AQUA + "Işınlanmak için tıklayın!");
        clericMeta.setLore(lore);
        lore.clear();
        cleric.setItemMeta(clericMeta);


        for(int i = 0; i < 9; i++)
        {
            inventory.setItem(i,filler);
        }

        inventory.setItem(9,filler);
        inventory.setItem(17,filler);

        for(int i = 18; i < 27; i++)
        {
            inventory.setItem(i,filler);
        }

        inventory.setItem(11,summoner);
        inventory.setItem(13,warrior);
        inventory.setItem(15,cleric);

        player.openInventory(inventory);
    }

}
