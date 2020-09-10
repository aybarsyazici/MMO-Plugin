package minecraft.mmoplugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ClassInfoInventory
{

    public static List<ItemStack> generateWarriorSkills()
    {
        List<ItemStack> listToReturn = new ArrayList<>();

        ItemStack sunder = new ItemStack(Material.IRON_AXE,1);
        ItemMeta sunderMeta = sunder.getItemMeta();
        sunderMeta.setDisplayName(ChatColor.RED + "Sunder");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "25 saniyede bir sonraki vuruşu Root 0/1/2/3/4 etkisine sahip olur.");
        lore.add(ChatColor.WHITE + "Seviye 1'de açılır, her 5 seviyede bir gelişir, seviye 20'de maxlanır.");
        sunderMeta.setLore(lore);
        sunderMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        sunder.setItemMeta(sunderMeta);

        ItemStack bloodyRage = new ItemStack(Material.RED_BANNER, 1);
        ItemMeta bloodyRageMeta = bloodyRage.getItemMeta();
        bloodyRageMeta.setDisplayName(ChatColor.BLUE + "Bloody Rage");
        lore.clear();
        lore.add(ChatColor.YELLOW + "Düz vuruşlar hedefe %12/%14/%16/%18/%20 gerçek hasar vurur.");
        lore.add(ChatColor.WHITE + "Seviye 50'de açılır, her 5 seviyede bir gelişir, seviye 70'de maxlanır.");
        bloodyRageMeta.setLore(lore);
        bloodyRage.setItemMeta(bloodyRageMeta);

        ItemStack whirlwind  = new ItemStack(Material.NETHERITE_SWORD, 1);
        ItemMeta whirlwindMeta = whirlwind.getItemMeta();
        whirlwindMeta.setDisplayName(ChatColor.DARK_AQUA + "Whirlwind");
        whirlwindMeta.addEnchant(Enchantment.DURABILITY,1,true);
        whirlwindMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        whirlwindMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        lore.clear();
        lore.add(ChatColor.YELLOW + "Oyuncu kılıç ile sağ tıkladığında etrafında döndürür ");
        lore.add(ChatColor.YELLOW + "ve 0.5 saniyede bir 3/4/5/6/7 hasar vurur (30 saniye cooldown)");
        lore.add(ChatColor.WHITE + "Seviye 25'de açılır, her 5 seviyede bir gelişir, seviye 45'de maxlanır.");
        whirlwindMeta.setLore(lore);
        whirlwind.setItemMeta(whirlwindMeta);


        ItemStack ancestorsStrength = new ItemStack(Material.ENCHANTED_BOOK, 1);
        ItemMeta ancestorsStrengthMeta = ancestorsStrength.getItemMeta();
        ancestorsStrengthMeta.setDisplayName(ChatColor.GREEN + "Ancestors Strength");
        ancestorsStrengthMeta.addEnchant(Enchantment.DURABILITY,2,true);
        ancestorsStrengthMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
        ancestorsStrengthMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        lore.clear();
        lore.add(ChatColor.YELLOW + "Oyuncu off-hande bir silah aldığında, o silahın statlarının %30’u main hand’e geçer.");
        lore.add(ChatColor.WHITE + "Seviye 75'de açılır.");
        ancestorsStrengthMeta.setLore(lore);
        ancestorsStrength.setItemMeta(ancestorsStrengthMeta);

        /*
        *
        *
        * TRAITS
        *
        * */

        ItemStack balta = new ItemStack(Material.GOLDEN_AXE, 1);
        ItemMeta baltaMeta = balta.getItemMeta();
        baltaMeta.setDisplayName(ChatColor.GREEN + "Axe Talent");
        lore.clear();
        lore.add(ChatColor.YELLOW + "Baltalar %20 daha fazla hasar verir. ");
        lore.add(ChatColor.WHITE + "Seviye 25'de açılır.");
        baltaMeta.setLore(lore);
        baltaMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        balta.setItemMeta(baltaMeta);


        ItemStack wither = new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
        ItemMeta witherMeta = wither.getItemMeta();
        witherMeta.setDisplayName(ChatColor.GREEN + "Wither Talent");
        lore.clear();
        lore.add(ChatColor.YELLOW + "%10 şansla vurulan hedefe wither uygulanır ve 5 saniye boyunca hasar alır.");
        lore.add(ChatColor.WHITE +  "Seviye 50'de açılır.");
        witherMeta.setLore(lore);
        wither.setItemMeta(witherMeta);


        ItemStack olaf = new ItemStack(Material.RED_DYE, 1);
        ItemMeta olafMeta = olaf.getItemMeta();
        olafMeta.setDisplayName(ChatColor.GREEN + "Attack Speed Talent");
        olafMeta.addEnchant(Enchantment.DURABILITY,2,true);
        olafMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
        olafMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        lore.clear();
        lore.add(ChatColor.YELLOW + "Oyuncu canı azaldıkça daha hızlı vurur.");
        lore.add(ChatColor.WHITE +  "Seviye 75'de açılır.");
        olafMeta.setLore(lore);
        olaf.setItemMeta(olafMeta);


        ItemStack execute = new ItemStack(Material.SHEARS, 1);
        ItemMeta executeMeta = execute.getItemMeta();
        executeMeta.setDisplayName(ChatColor.GREEN + "Execute Talent");
        executeMeta.addEnchant(Enchantment.DURABILITY,2,true);
        executeMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
        executeMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        lore.clear();
        lore.add(ChatColor.YELLOW + "%15 canın altındaki hedefleri anında öldürür.");
        lore.add(ChatColor.YELLOW + "10 saniye boyunca Movement Speed kazanırsın.");
        lore.add(ChatColor.WHITE +  "Seviye 100'de açılır.");
        executeMeta.setLore(lore);
        execute.setItemMeta(executeMeta);


        listToReturn.add(sunder);
        listToReturn.add(bloodyRage);
        listToReturn.add(whirlwind);
        listToReturn.add(ancestorsStrength);

        listToReturn.add(balta);
        listToReturn.add(wither);
        listToReturn.add(olaf);
        listToReturn.add(execute);

        return listToReturn;
    }

    public static List<ItemStack> generateClericSkills()
    {
        List<ItemStack> listToReturn = new ArrayList<>();

        ItemStack skill1 = new ItemStack(Material.POTION,1);
        ItemMeta skill1ItemMeta = skill1.getItemMeta();
        skill1ItemMeta.setDisplayName(ChatColor.RED + "Light's Guidance");
        skill1ItemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Oyuncu hasar aldığında can yenilenmesi kazanır.");
        lore.add(ChatColor.WHITE + "Seviye 1'de açılır");
        skill1ItemMeta.setLore(lore);
        skill1ItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        skill1.setItemMeta(skill1ItemMeta);

        ItemStack skill2 = new ItemStack(Material.GOLDEN_SWORD, 1);
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

        ItemStack whirlwind  = new ItemStack(Material.GLOWSTONE_DUST, 1);
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


        ItemStack execute = new ItemStack(Material.BEACON, 1);
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

        /*
         *
         *
         * TRAITS
         *
         * */


        //System.out.println("Generating, fire skill info.");
        ItemStack fire = new ItemStack(Material.FIRE_CORAL_FAN, 1);
        ItemMeta fireMeta = fire.getItemMeta();
        fireMeta.setDisplayName(ChatColor.RED + "Purging Fire");
        fireMeta.addEnchant(Enchantment.DURABILITY,2,true);
        fireMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
        fireMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        lore.clear();
        lore.add(ChatColor.YELLOW + "Oyuncunun düz vuruşları, hedefini 1 saniyeliğine yakar.");
        lore.add(ChatColor.WHITE +  "Seviye 25'de açılır.");
        fireMeta.setLore(lore);
        fire.setItemMeta(fireMeta);

        ItemStack wither = new ItemStack(Material.SHIELD, 1);
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


        ItemStack olaf = new ItemStack(Material.GOLDEN_HORSE_ARMOR, 1);
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

        ItemStack ancestorsStrength = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1);
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

    public static List<ItemStack> generateSummonerSkills()
    {
        List<ItemStack> listToReturn = new ArrayList<>();
        List<String> lore = new ArrayList<>();

        ItemStack zombie = new ItemStack(Material.ZOMBIE_HEAD);
        ItemMeta zombieMeta = zombie.getItemMeta();
        zombieMeta.setDisplayName(ChatColor.GOLD + "Lvl 1 Summonı");
        lore.add("");
        lore.add(ChatColor.GRAY + "Sizin için savaşan bir");
        lore.add(ChatColor.GRAY + "zombie çağırın.");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Seviye 1-26 arasında geçerlidir.");
        zombieMeta.setLore(lore);
        lore.clear();
        zombie.setItemMeta(zombieMeta);

        ItemStack skeleton = new ItemStack(Material.SKELETON_SKULL);
        ItemMeta skeletonMeta = skeleton.getItemMeta();
        skeletonMeta.setDisplayName(ChatColor.GOLD + "Lvl 26 Summonı");
        lore.add("");
        lore.add(ChatColor.GRAY + "Sizin için savaşan bir");
        lore.add(ChatColor.GRAY + "iskelet çağırın.");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Seviye 26-51 arasında geçerlidir.");
        skeletonMeta.setLore(lore);
        lore.clear();
        skeleton.setItemMeta(skeletonMeta);

        ItemStack witherSkeleton = new ItemStack(Material.WITHER_SKELETON_SKULL);
        ItemMeta witherSkeletonMeta = witherSkeleton.getItemMeta();
        witherSkeletonMeta.setDisplayName(ChatColor.GOLD + "Lvl 51 Summonı");
        lore.add("");
        lore.add(ChatColor.GRAY + "Sizin için savaşan bir");
        lore.add(ChatColor.GRAY + "wither iskeleti çağırın.");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Seviye 51-76 arasında geçerlidir.");
        witherSkeletonMeta.setLore(lore);
        lore.clear();
        witherSkeleton.setItemMeta(witherSkeletonMeta);

        ItemStack horse = new ItemStack(Material.DIAMOND_HORSE_ARMOR);
        ItemMeta horseMeta = horse.getItemMeta();
        horseMeta.setDisplayName(ChatColor.GOLD + "Lvl 76 Summonı");
        horseMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        horseMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        horseMeta.addEnchant(Enchantment.DURABILITY,1,true);
        lore.add("");
        lore.add(ChatColor.GRAY + "Sizin için savaşan");
        lore.add(ChatColor.GRAY + "at üstünde bir");
        lore.add(ChatColor.GRAY + "wither iskeleti çağırın.");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Seviye 76 sonrasında geçerlidir.");
        horseMeta.setLore(lore);
        lore.clear();
        horse.setItemMeta(horseMeta);


        ItemStack fire = new ItemStack(Material.DIAMOND_BOOTS, 1);
        ItemMeta fireMeta = fire.getItemMeta();
        fireMeta.setDisplayName(ChatColor.RED + "LVL 25 Talent'i");
        fireMeta.addEnchant(Enchantment.DURABILITY,2,true);
        fireMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
        fireMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        fireMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        lore.clear();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Summon'ın vuruşları sahibine saniyelik");
        lore.add(ChatColor.YELLOW + "movement speed verir.");
        lore.add(ChatColor.WHITE +  "Seviye 25'de açılır.");
        fireMeta.setLore(lore);
        fire.setItemMeta(fireMeta);

        ItemStack wither = new ItemStack(Material.POISONOUS_POTATO, 1);
        ItemMeta witherMeta = wither.getItemMeta();
        witherMeta.setDisplayName(ChatColor.GREEN + "LVL 50 Talent'i");
        witherMeta.addEnchant(Enchantment.DURABILITY,2,true);
        witherMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
        witherMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        witherMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        lore.clear();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Summonın vuruşları slow ve poison uygular.");
        lore.add(ChatColor.WHITE +  "Seviye 50'de açılır.");
        witherMeta.setLore(lore);
        wither.setItemMeta(witherMeta);


        ItemStack olaf = new ItemStack(Material.NETHERITE_CHESTPLATE, 1);
        ItemMeta olafMeta = olaf.getItemMeta();
        olafMeta.setDisplayName(ChatColor.GREEN + "Ekipman Talent'ı");
        olafMeta.addEnchant(Enchantment.DURABILITY,2,true);
        olafMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
        olafMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        olafMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        lore.clear();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Summon'a sağ tıklayınca ekipman envanteri açılır");
        lore.add(ChatColor.YELLOW + "bu envanterden summon'a farklı ekipmanlar verilebilir.");
        lore.add(ChatColor.WHITE +  "Seviye 75'de açılır.");
        olafMeta.setLore(lore);
        olaf.setItemMeta(olafMeta);


        ItemStack execute = new ItemStack(Material.DIAMOND_SWORD, 1);
        ItemMeta executeMeta = execute.getItemMeta();
        executeMeta.setDisplayName(ChatColor.GREEN + "LVL 100 Talent'i");
        executeMeta.addEnchant(Enchantment.DURABILITY,2,true);
        executeMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
        executeMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        executeMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        lore.clear();
        lore.add("");
        lore.add(ChatColor.YELLOW + "Oyuncu ölecek olursa, Summon'ı kendini feda ederek, oyuncunun canını fuller.");
        lore.add(ChatColor.YELLOW + "Oyuncu Summonıyla aynı hedefe saldırırsa %20 daha fazla vurur.");
        lore.add(ChatColor.WHITE +  "Seviye 100'de açılır.");
        executeMeta.setLore(lore);
        execute.setItemMeta(executeMeta);

        listToReturn.add(zombie); //Index 0
        listToReturn.add(skeleton); //Index 1
        listToReturn.add(witherSkeleton); //Index 2
        listToReturn.add(horse); //Index 3

        listToReturn.add(fire); //Index 4
        listToReturn.add(wither); //Index 5
        listToReturn.add(olaf); //Index 6
        listToReturn.add(execute); //Index 7

        return listToReturn;
    }

    public static void createInfoInventory(Plugin plugin, Player player, String className)
    {

        Inventory inventory = plugin.getServer().createInventory(new CustomInventory.CustomInventoryHolder(), 36, ChatColor.GOLD + "" + ChatColor.BOLD + (className.equalsIgnoreCase("necromancer") ? "SUMMONER" : className.toUpperCase()) + " Class Info");

        ItemStack classInfo = new ItemStack(Material.PAINTING, 1);
        ItemMeta classInfoMeta = classInfo.getItemMeta();
        classInfoMeta.setDisplayName(ChatColor.DARK_PURPLE + "Bu class hakkında");
        ArrayList<String> lore = new ArrayList<>();

        switch (className.toLowerCase())
        {
            case "warrior":
            {
                lore.add(ChatColor.YELLOW + "Yüzyıl savaşından sağ çıkmayı başarmış onurlu bir ırk.");
                lore.add(ChatColor.YELLOW + "Şereflerini her şeyden üstün tutarlar ve atalarına son derece saygılıdırlar.");
                lore.add(ChatColor.YELLOW + "Yanlışlıkla birinin namusuna yan bakarsanız kafanıza balta yemeniz kaçınılmazdır.");
                break;
            }
            case "cleric":
            {
                lore.add(ChatColor.YELLOW + "Yüzyıl savaşından galibiyetle ayrılan tek taraf.");
                lore.add(ChatColor.YELLOW + "Bu korksuzuz savaşçılar Tanrı'nın ismini");
                lore.add(ChatColor.YELLOW + "ağızlarından eksik etmeyerek savaşıyorlar.");
                lore.add(ChatColor.YELLOW + "Savunmada olduğu kadar saldırıda da kusursuzlardır.");
                break;
            }
            case "necromancer":
            {
                lore.add(ChatColor.YELLOW + "Yüzyıl savaşı sonrası geriye bırakılan cesetlerle");
                lore.add(ChatColor.YELLOW + "bilgilerine bilgi, güçlerine güç katmış lanetli");
                lore.add(ChatColor.YELLOW + "kişilerden oluşan karanlık bir grup.");
                break;
            }
        }

        classInfoMeta.addEnchant(Enchantment.DURABILITY,2,true);
        classInfoMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
        classInfoMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        classInfoMeta.setLore(lore);
        classInfo.setItemMeta(classInfoMeta);

        ItemStack chooseThisClass = new ItemStack(Material.CRAFTING_TABLE, 1);
        ItemMeta chooseThisClassMeta = chooseThisClass.getItemMeta();
        chooseThisClassMeta.setDisplayName(ChatColor.DARK_PURPLE + "Bu sınıfı seç");
        chooseThisClassMeta.addEnchant(Enchantment.DURABILITY,2,true);
        chooseThisClassMeta.addEnchant(Enchantment.DAMAGE_ALL,1,true);
        chooseThisClassMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        chooseThisClass.setItemMeta(chooseThisClassMeta);

        List<ItemStack> skills = new ArrayList<>();

        switch (className.toLowerCase())
        {
            case "warrior":
                skills = ClassInfoInventory.generateWarriorSkills();
                break;
            case "cleric":
                skills = ClassInfoInventory.generateClericSkills();
                break;
            case "necromancer":
                skills = ClassInfoInventory.generateSummonerSkills();
                break;
        }

        for(int i = 0; i < skills.size(); i++)
        {
            if(i < 4)
                inventory.setItem(12+i, skills.get(i));
            else
                inventory.setItem(17+i, skills.get(i));
        }

        inventory.setItem(8, classInfo);
        inventory.setItem(35, chooseThisClass);

        player.openInventory(inventory);

    }
}
