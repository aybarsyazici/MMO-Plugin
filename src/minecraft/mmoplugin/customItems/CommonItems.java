package minecraft.mmoplugin.customItems;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.w3c.dom.Attr;

import java.util.*;

public class CommonItems
{

    public static ItemStack createCommonItem(Material material, String name, boolean isWeapon, double damage, double attackSpeed, boolean onMarket, int price)
    {
        List<String> lore = new ArrayList<>();

        ItemStack sword = new ItemStack(material);
        ItemMeta swordMeta = sword.getItemMeta();
        swordMeta.setDisplayName(ChatColor.WHITE + name);
        swordMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        swordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        lore.add(ChatColor.WHITE + "Enderlik : Sıradan");
        if(isWeapon)
        {
            lore.add("");
            lore.add(ChatColor.GREEN + "Saldırı gücü: " + damage);
            lore.add(ChatColor.GREEN + "Saldırı Hızı: " + attackSpeed);

        }
        if(onMarket)
        {
            lore.add("");
            lore.add(ChatColor.YELLOW + "Gereken para: " + ChatColor.GOLD + price);
        }
        swordMeta.setLore(lore);
        lore.clear();
        sword.setItemMeta(swordMeta);

        return sword;
    }

    public static class ItemPrices
    {
        public static final int commonShieldPrice = 500;
        public static final int commonApplePrice = 5;

        public static final int commonStoneSwordPrice = 400;
        public static final int commonStonePickAxePrice = 500;
        public static final int commonStoneShovelPrice = 400;
        public static final int commonStoneHoePrice = 400;
        public static final int commonStoneAxePrice = 400;
        public static final int commonChainHelmetPrice = 400;
        public static final int commonChainPlatePrice = 550;
        public static final int commonChainLeggingsPrice = 500;
        public static final int commonChainBootsPrice = 425;

        public static final int commonIronSwordPrice = 600;
        public static final int commonIronPickAxePrice = 700;
        public static final int commonIronShovelPrice = 600;
        public static final int commonIronHoePrice = 600;
        public static final int commonIronAxePrice = 600;
        public static final int commonIronHelmetPrice = 600;
        public static final int commonIronPlatePrice = 750;
        public static final int commonIronLeggingsPrice = 700;
        public static final int commonIronBootsPrice = 725;

        public static final int commonLogPrice = 40;
        public static final int commonSaplingPrice = 10;
        public static final int commonBiyologMiscPrice = 5;



        public static final int commonRedStonePrice = 20;
        public static final int commonLapisPrice = 20;
        public static final int commonCoalPrice = 20;
        public static final int commonGoldIngotPrice = 150;
        public static final int commonIronIngotPrice = 300;
        public static final int commonDiamondPrice = 5000;

        public static final int commonHealPotionPrice = 150;
    }


    public static ItemStack createElma()
    {
        List<String> lore;

        ItemStack elma = new ItemStack(Material.APPLE);
        ItemMeta elmaMeta = elma.getItemMeta();
        elmaMeta.setDisplayName(ChatColor.WHITE + "Elma");
        elma.setItemMeta(elmaMeta);

        return elma;
    }


    public static class Equipment
    {
        public static ItemStack commonStoneSword()
        {
            List<String> lore = new ArrayList<>();

            ItemStack sword = new ItemStack(Material.STONE_SWORD);
            ItemMeta swordMeta = sword.getItemMeta();
            swordMeta.setDisplayName(ChatColor.WHITE + "Taş Kılıç");
            swordMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            swordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            lore.add(ChatColor.WHITE + "Enderlik : Sıradan");
            lore.add("");
            lore.add(ChatColor.GREEN + "Saldırı gücü: 5");
            lore.add(ChatColor.GREEN + "Saldırı Hızı: 1.60");
            swordMeta.setLore(lore);
            lore.clear();
            sword.setItemMeta(swordMeta);

            return sword;
        }

        public static ItemStack commonStoneAxe()
        {
            List<String> lore = new ArrayList<>();

            ItemStack axe = new ItemStack(Material.STONE_AXE);
            ItemMeta axeMeta = axe.getItemMeta();
            axeMeta.setDisplayName(ChatColor.WHITE + "Taş Balta");
            axeMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            axeMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            lore.add(ChatColor.WHITE + "Enderlik : Sıradan");
            lore.add("");
            lore.add(ChatColor.GREEN + "Saldırı gücü: 9");
            lore.add(ChatColor.GREEN + "Saldırı Hızı: 0.80");
            axeMeta.setLore(lore);
            lore.clear();
            axe.setItemMeta(axeMeta);

            return axe;
        }

        public static ItemStack commonChainHelmet()
        {
            List<String> lore = new ArrayList<>();

            ItemStack helmet = new ItemStack(Material.CHAINMAIL_HELMET);
            ItemMeta helmetMeta = helmet.getItemMeta();
            helmetMeta.setDisplayName(ChatColor.WHITE + "Zincirli Kask");
            helmetMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            helmetMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            helmetMeta.getAttributeModifiers(EquipmentSlot.HEAD).clear();
            Multimap<Attribute, AttributeModifier> map = ImmutableMultimap.<Attribute,AttributeModifier>builder().put(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(),"generic.armor",1, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HEAD)).build();
            helmetMeta.setAttributeModifiers(map);
            lore.add(ChatColor.WHITE + "Enderlik : Sıradan");
            lore.add("");
            lore.add(ChatColor.GREEN + "Zırh: 1");
            helmetMeta.setLore(lore);
            lore.clear();
            helmet.setItemMeta(helmetMeta);

            return helmet;

        }

        public static ItemStack commonChainPlate()
        {
            List<String> lore = new ArrayList<>();

            ItemStack plate = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
            ItemMeta plateMeta = plate.getItemMeta();
            plateMeta.setDisplayName(ChatColor.WHITE + "Zincirli Göğüslük");
            plateMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            plateMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            lore.add(ChatColor.WHITE + "Enderlik : Sıradan");
            lore.add("");
            lore.add(ChatColor.GREEN + "Zırh: 5");
            plateMeta.setLore(lore);
            lore.clear();
            plate.setItemMeta(plateMeta);

            return plate;

        }

        public static ItemStack commonChainLeggings()
        {
            List<String> lore = new ArrayList<>();

            ItemStack leggings = new ItemStack(Material.CHAINMAIL_LEGGINGS);
            ItemMeta leggingsMeta = leggings.getItemMeta();
            leggingsMeta.setDisplayName(ChatColor.WHITE + "Zincirli Pantalon");
            leggingsMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            leggingsMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            lore.add(ChatColor.WHITE + "Enderlik : Sıradan");
            lore.add("");
            lore.add(ChatColor.GREEN + "Zırh: 4");
            leggingsMeta.setLore(lore);
            lore.clear();
            leggings.setItemMeta(leggingsMeta);

            return leggings;
        }

        public static ItemStack commonChainBoots()
        {
            List<String> lore = new ArrayList<>();

            ItemStack boots = new ItemStack(Material.CHAINMAIL_BOOTS);
            ItemMeta bootsMeta = boots.getItemMeta();
            bootsMeta.setDisplayName(ChatColor.WHITE + "Zincirli Bot");
            bootsMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            bootsMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            lore.add(ChatColor.WHITE + "Enderlik : Sıradan");
            lore.add("");
            lore.add(ChatColor.GREEN + "Zırh: 1");
            bootsMeta.setLore(lore);
            lore.clear();
            boots.setItemMeta(bootsMeta);

            return boots;
        }

        public static ItemStack commonIronSword()
        {
            List<String> lore = new ArrayList<>();

            ItemStack sword = new ItemStack(Material.IRON_SWORD);
            ItemMeta swordMeta = sword.getItemMeta();
            swordMeta.setDisplayName(ChatColor.WHITE + "Demir Kılıç");
            swordMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            swordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            lore.add(ChatColor.WHITE + "Enderlik : Sıradan");
            lore.add("");
            lore.add(ChatColor.GREEN + "Saldırı gücü: 6");
            lore.add(ChatColor.GREEN + "Saldırı Hızı: 1.60");
            swordMeta.setLore(lore);
            lore.clear();
            sword.setItemMeta(swordMeta);

            return sword;
        }

        public static ItemStack commonIronAxe()
        {
            List<String> lore = new ArrayList<>();

            ItemStack sword = new ItemStack(Material.IRON_AXE);
            ItemMeta swordMeta = sword.getItemMeta();
            swordMeta.setDisplayName(ChatColor.WHITE + "Demir Balta");
            swordMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            swordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            lore.add(ChatColor.WHITE + "Enderlik : Sıradan");
            lore.add("");
            lore.add(ChatColor.GREEN + "Saldırı gücü: 9");
            lore.add(ChatColor.GREEN + "Saldırı Hızı: 0.9");
            swordMeta.setLore(lore);
            lore.clear();
            sword.setItemMeta(swordMeta);

            return sword;
        }

        public static ItemStack commonIronHelmet()
        {
            List<String> lore = new ArrayList<>();

            ItemStack helmet = new ItemStack(Material.IRON_HELMET);
            ItemMeta helmetMeta = helmet.getItemMeta();
            helmetMeta.setDisplayName(ChatColor.WHITE + "Demir Kask");
            helmetMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            helmetMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            lore.add(ChatColor.WHITE + "Enderlik : Sıradan");
            lore.add("");
            lore.add(ChatColor.GREEN + "Zırh: 2");
            helmetMeta.setLore(lore);
            lore.clear();
            helmet.setItemMeta(helmetMeta);

            return helmet;

        }

        public static ItemStack commonIronPlate()
        {
            List<String> lore = new ArrayList<>();

            ItemStack plate = new ItemStack(Material.IRON_CHESTPLATE);
            ItemMeta plateMeta = plate.getItemMeta();
            plateMeta.setDisplayName(ChatColor.WHITE + "Demir Göğüslük");
            plateMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            plateMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            lore.add(ChatColor.WHITE + "Enderlik : Sıradan");
            lore.add("");
            lore.add(ChatColor.GREEN + "Zırh: 6");
            plateMeta.setLore(lore);
            lore.clear();
            plate.setItemMeta(plateMeta);

            return plate;

        }

        public static ItemStack commonIronLeggings()
        {
            List<String> lore = new ArrayList<>();

            ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
            ItemMeta leggingsMeta = leggings.getItemMeta();
            leggingsMeta.setDisplayName(ChatColor.WHITE + "Demir Pantalon");
            leggingsMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            leggingsMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            lore.add(ChatColor.WHITE + "Enderlik : Sıradan");
            lore.add("");
            lore.add(ChatColor.GREEN + "Zırh: 5");
            leggingsMeta.setLore(lore);
            lore.clear();
            leggings.setItemMeta(leggingsMeta);

            return leggings;
        }

        public static ItemStack commonIronBoots()
        {
            List<String> lore = new ArrayList<>();

            ItemStack boots = new ItemStack(Material.IRON_BOOTS);
            ItemMeta bootsMeta = boots.getItemMeta();
            bootsMeta.setDisplayName(ChatColor.WHITE + "Demir Bot");
            bootsMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            bootsMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            lore.add(ChatColor.WHITE + "Enderlik : Sıradan");
            lore.add("");
            lore.add(ChatColor.GREEN + "Zırh: 2");
            bootsMeta.setLore(lore);
            lore.clear();
            boots.setItemMeta(bootsMeta);

            return boots;
        }
    }

    public static class Blocks
    {
        public static ItemStack commonBirchLog()
        {
            List<String> lore = new ArrayList<>();

            ItemStack sword = new ItemStack(Material.STONE_SWORD);
            ItemMeta swordMeta = sword.getItemMeta();
            swordMeta.setDisplayName(ChatColor.WHITE + "Huş Kütüğü");
            swordMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            swordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            lore.add(ChatColor.WHITE + "Enderlik : Sıradan");
            lore.add("");
            swordMeta.setLore(lore);
            lore.clear();
            sword.setItemMeta(swordMeta);

            return sword;
        }

    }

}
