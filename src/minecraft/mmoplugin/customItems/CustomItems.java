package minecraft.mmoplugin.customItems;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CustomItems
{

    public enum Enderlik
    {
        SIRADAN,
        NADIR,
        ESSIZ,
        OLAGANUSTU,
        DESTANSI
    }


    public static String rarityColor(Enderlik rarity)
    {
        switch (rarity)
        {
            case SIRADAN:
                return "" + ChatColor.WHITE;
            case NADIR:
                return "" + ChatColor.GREEN;
            case ESSIZ:
                return "" + ChatColor.AQUA;
            case OLAGANUSTU:
                return "" + ChatColor.DARK_PURPLE;
            case DESTANSI:
                return "" + ChatColor.GOLD;
        }

        return "";
    }

    public static class CustomItemStats
    {
        //Base player attack damage is: 1
        //Base player attack speed is: 4
        public static final double commonWoodenAxeDamage = 4;
        public static final double commonStoneAxeDamage = 5;
        public static final double commonIronAxeDamage = 6;
        public static final double commonDiamondAxeDamage = 7;
        public static final double commonNetheriteAxeDamage = 8;

        public static final double commonWoodenAxeSpeed = 0.8;
        public static final double commonStoneAxeSpeed = 0.8;
        public static final double commonIronAxeSpeed = 0.9;
        public static final double commonDiamondAxeSpeed = 1;
        public static final double commonNetheriteAxeSpeed = 1;

        public static final double commonWoodenSwordDamage = 3;
        public static final double commonStoneSwordDamage = 4;
        public static final double commonIronSwordDamage = 5;
        public static final double commonDiamondSwordDamage = 6;
        public static final double commonNetheriteSwordDamage = 7;

        public static final double commonWoodenSwordSpeed = 1.6;
        public static final double commonStoneSwordSpeed = 1.6;
        public static final double commonIronSwordSpeed = 1.6;
        public static final double commonDiamondSwordSpeed = 1.6;
        public static final double commonNetheriteSwordSpeed = 1.6;

        public static final double commonLeatherHelmetArmor = 1;
        public static final double commonLeatherPlateArmor = 3;
        public static final double commonLeatherLeggingsArmor = 2;
        public static final double commonLeatherBootsArmor = 1;

        public static final double commonLeatherHelmetToughness = 0;
        public static final double commonLeatherPlateToughness = 0;
        public static final double commonLeatherLeggingsToughness = 0;
        public static final double commonLeatherBootsToughness = 0;

        public static final double commonChainHelmetArmor = 1;
        public static final double commonChainPlateArmor = 5;
        public static final double commonChainLeggingsArmor = 4;
        public static final double commonChainBootsArmor = 2;

        public static final double commonChainHelmetToughness = 0;
        public static final double commonChainPlateToughness = 0;
        public static final double commonChainLeggingsToughness = 0;
        public static final double commonChainBootsToughness = 0;

        public static final double commonIronHelmetArmor = 2;
        public static final double commonIronPlateArmor = 6;
        public static final double commonIronLeggingsArmor = 5;
        public static final double commonIronBootsArmor = 2;

        public static final double commonIronHelmetToughness = 0;
        public static final double commonIronPlateToughness = 0;
        public static final double commonIronLeggingsToughness = 0;
        public static final double commonIronBootsToughness = 0;

        public static final double commonDiamondHelmetArmor = 3;
        public static final double commonDiamondPlateArmor = 8;
        public static final double commonDiamondLeggingsArmor = 6;
        public static final double commonDiamondBootsArmor = 3;

        public static final double commonDiamondHelmetToughness = 2;
        public static final double commonDiamondPlateToughness = 2;
        public static final double commonDiamondLeggingsToughness = 2;
        public static final double commonDiamondBootsToughness = 2;

        public static final double commonNetheriteHelmetArmor = 3;
        public static final double commonNetheritePlateArmor = 8;
        public static final double commonNetheriteLeggingsArmor = 6;
        public static final double commonNetheriteBootsArmor = 3;

        public static final double commonNetheriteHelmetToughness = 3;
        public static final double commonNetheritePlateToughness = 3;
        public static final double commonNetheriteLeggingsToughness = 3;
        public static final double commonNetheriteBootsToughness = 3;


    }

// boolean isArmor, double armor, double toughness, EquipmentSlot slot
    public static ItemStack createCustomItem(Material material, String name, Enderlik rarity, boolean isWeapon, double damage, double speed)
    {
        ItemStack result = new ItemStack(material);
        ItemMeta resultMeta = result.getItemMeta();
        String color = CustomItems.rarityColor(rarity);
        resultMeta.setDisplayName( color + name);
        String rarityString = "Sıradan";
        switch (rarity)
        {
            case SIRADAN:
                rarityString = "Sıradan";
                break;
            case NADIR:
                rarityString = "Nadir";
                break;
            case ESSIZ:
                rarityString = "Eşsiz";
                break;
            case OLAGANUSTU:
                rarityString = "Olağanüstü";
                break;
            case DESTANSI:
                rarityString = "Destansı";
                break;
        }

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(color + "Enderlik : " + rarityString);

        if (isWeapon)
        {
            resultMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            resultMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            resultMeta.getAttributeModifiers(EquipmentSlot.HAND).clear();
            ImmutableMultimap.Builder<Attribute,AttributeModifier> builder = ImmutableMultimap.<Attribute,AttributeModifier>builder();
            builder.put(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(),"generic.attack_damage",damage, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HAND));
            builder.put(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(),"generic.attack_speed",-(4-speed), AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HAND));
            Multimap<Attribute, AttributeModifier> map = builder.build();
            resultMeta.setAttributeModifiers(map);
            lore.add("");
            lore.add(ChatColor.DARK_GREEN + "Saldırı Gücü : " + damage);
            lore.add(ChatColor.DARK_GREEN + "Saldırı Hızı : " + speed);
        }

        resultMeta.setLore(lore);
        lore.clear();
        result.setItemMeta(resultMeta);
        return result;
    }

    public static ItemStack createCustomItem(Material material, String name, Enderlik rarity, boolean isArmour, double armor, double toughness, EquipmentSlot slot, HashMap<Enchantment,Integer> enchantmentMap)
    {
        ItemStack result = new ItemStack(material);
        ItemMeta resultMeta = result.getItemMeta();
        String color = CustomItems.rarityColor(rarity);
        resultMeta.setDisplayName( color + name);
        String rarityString = "Sıradan";
        switch (rarity)
        {
            case SIRADAN:
                rarityString = "Sıradan";
                break;
            case NADIR:
                rarityString = "Nadir";
                break;
            case ESSIZ:
                rarityString = "Eşsiz";
                break;
            case OLAGANUSTU:
                rarityString = "Olağanüstü";
                break;
            case DESTANSI:
                rarityString = "Destansı";
                break;
        }

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(color + "Enderlik : " + rarityString);

        if (isArmour)
        {
            resultMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            resultMeta.getAttributeModifiers(slot).clear();
            ImmutableMultimap.Builder<Attribute,AttributeModifier> builder = ImmutableMultimap.<Attribute,AttributeModifier>builder();
            builder.put(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(),"generic.armor",armor, AttributeModifier.Operation.ADD_NUMBER,slot));
            builder.put(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(),"generic.armor_toughness",toughness, AttributeModifier.Operation.ADD_NUMBER,slot));
            if(material.toString().contains("NETHERITE"))
            {
                builder.put(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(),"generic.knockback_resistance",1, AttributeModifier.Operation.ADD_NUMBER,slot));
            }
            Multimap<Attribute, AttributeModifier> map = builder.build();
            resultMeta.setAttributeModifiers(map);
            lore.add("");
            lore.add(ChatColor.DARK_GREEN + "Zırh : " + armor);
            if (toughness != 0)
                lore.add(ChatColor.DARK_GREEN + "Sertlik : " + toughness);
            if(material.toString().contains("NETHERITE"))
                lore.add(ChatColor.DARK_GREEN + "Savrulma Direnci : " + 1);
        }

        if(enchantmentMap != null)
        {
            resultMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            lore.add("");
            for(Enchantment ench : enchantmentMap.keySet())
            {
                resultMeta.addEnchant(ench,enchantmentMap.get(ench),true);
                if (ench.equals(Enchantment.PROTECTION_ENVIRONMENTAL))
                {
                    lore.add(ChatColor.BLUE + "Koruma : " + enchantmentMap.get(ench));
                    lore.add(ChatColor.DARK_AQUA + "Gelen hasarı %"+ Integer.toString(4*enchantmentMap.get(ench)) +" azaltır.");
                }
                else if (ench.equals(Enchantment.PROTECTION_EXPLOSIONS))
                {
                    lore.add(ChatColor.BLUE + "Patlama Koruması : " + enchantmentMap.get(ench));
                    lore.add(ChatColor.DARK_AQUA + "Gelen patlama hasarını %" + Integer.toString(8*enchantmentMap.get(ench)) + " azaltır.");
                }
                else if(ench.equals(Enchantment.PROTECTION_PROJECTILE))
                {
                    lore.add(ChatColor.BLUE + "Ok Koruması : " + enchantmentMap.get(ench));
                    lore.add(ChatColor.DARK_AQUA + "Gelen ok hasarını %" + Integer.toString(8*enchantmentMap.get(ench)) + " azaltır.");
                }
            }
        }

        resultMeta.setLore(lore);
        lore.clear();
        result.setItemMeta(resultMeta);
        return result;
    }

    public static ItemStack createCustomPotion(String name, PotionType effect, Enderlik rarity, Material potionType)
    {
        ItemStack potion = new ItemStack(potionType);
        PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
        int level = 0;
        switch (rarity)
        {
            case SIRADAN:
                level = 1; //Restores 8
                break;
            case NADIR:
                level = 2; //Restores 16
                break;
            case ESSIZ:
                level = 3; //Restores 32
                break;
            case OLAGANUSTU:
                level = 3; //Restores 32
                break;
            case DESTANSI:
                level = 4;//Restores 64
                break;
        }
        potionMeta.addCustomEffect(effect.getEffectType().createEffect(0,level),true);
        String color = CustomItems.rarityColor(rarity);
        String rarityString = "Sıradan";
        switch (rarity)
        {
            case SIRADAN:
                rarityString = "Sıradan";
                break;
            case NADIR:
                rarityString = "Nadir";
                break;
            case ESSIZ:
                rarityString = "Eşsiz";
                break;
            case OLAGANUSTU:
                rarityString = "Olağanüstü";
                break;
            case DESTANSI:
                rarityString = "Destansı";
                break;
        }
        potionMeta.setDisplayName(color + name);
        potionMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(color + "Enderlik : " + rarityString);
        switch (effect)
        {
            case INSTANT_HEAL:
                lore.add("");
                lore.add(ChatColor.RED + "İyileşme : " + (level+1));
                lore.add(ChatColor.DARK_PURPLE + "Kullanıldığında canınızı doldurur.");
                if(rarity.equals(Enderlik.OLAGANUSTU))
                {
                    potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION,10*20,2),true);
                    lore.add(ChatColor.RED + "Yenilenme : 3");
                    lore.add(ChatColor.DARK_PURPLE + "10 saniye boyunca canınız yenilenir.");
                }
                else if(rarity.equals(Enderlik.DESTANSI))
                {
                    potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION,10*20,2),true);
                    lore.add(ChatColor.RED + "Yenilenme : 3");
                    lore.add(ChatColor.DARK_PURPLE + "10 saniye boyunca canınız yenilenir.");
                    potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,10*20,1),true);
                    lore.add(ChatColor.RED + "Koruma : 1");
                    lore.add(ChatColor.DARK_PURPLE + "10 saniye boyunca gelen hasarı %10 azaltır.");
                }
                break;
        }

        potionMeta.setLore(lore);
        potion.setItemMeta(potionMeta);
        return potion;
    }

    public static ItemStack createCustomShield(String name, Enderlik rarity)
    {
        ItemStack shield = new ItemStack(Material.SHIELD);
        ItemMeta shieldMeta = shield.getItemMeta();
        String color = CustomItems.rarityColor(rarity);
        shieldMeta.setDisplayName(color + name);

        String rarityString = "Sıradan";
        switch (rarity)
        {
            case SIRADAN:
                rarityString = "Sıradan";
                break;
            case NADIR:
                rarityString = "Nadir";
                break;
            case ESSIZ:
                rarityString = "Eşsiz";
                break;
            case OLAGANUSTU:
                rarityString = "Olağanüstü";
                break;
            case DESTANSI:
                rarityString = "Destansı";
                break;
        }

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(color + "Enderlik : " + rarityString);

        shieldMeta.setLore(lore);
        shield.setItemMeta(shieldMeta);
        return shield;
    }
}
