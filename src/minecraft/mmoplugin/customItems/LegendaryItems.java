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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LegendaryItems
{
    public static ItemStack createYoshiramaru()
    {
        ItemStack item = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(ChatColor.GOLD + "Kutsal Kılıç Yoshiramaru");
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "Enderlik : Destansı");

        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.getAttributeModifiers(EquipmentSlot.HAND).clear();
        ImmutableMultimap.Builder<Attribute,AttributeModifier> builder = ImmutableMultimap.<Attribute,AttributeModifier>builder();
        builder.put(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(),"generic.attack_damage",7, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HAND));
        builder.put(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(),"generic.attack_speed",-1.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HAND)); //Thus it has 2.5 Attack Speed
        builder.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(),"generic.movement_speed",0.15, AttributeModifier.Operation.ADD_SCALAR,EquipmentSlot.HAND)); //Extra 15% movement speed
        Multimap<Attribute, AttributeModifier> map = builder.build();
        itemMeta.setAttributeModifiers(map);

        lore.add("");
        lore.add(ChatColor.YELLOW + "Saldırı Gücü: 8" + ChatColor.BLUE + " (+3)");
        lore.add(ChatColor.YELLOW + "Saldırı Hızı: 2.5");
        lore.add(ChatColor.YELLOW + "Elinizdeyken: %15 Hareket Hızı");


        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.addEnchant(Enchantment.DAMAGE_ALL,5,true); //Sharp 1: 1, 2: 1.5, 3: 2, 4: 2.5, 5: 3
        lore.add("");
        lore.add(ChatColor.BLUE + "Keskinlik: 5");
        lore.add(ChatColor.AQUA + "Silahınızın saldırı gücünü 3 artırır.");
        itemMeta.addEnchant(Enchantment.DAMAGE_UNDEAD,4,true);
        lore.add("");
        lore.add(ChatColor.BLUE + "Darbe: 4");
        lore.add(ChatColor.AQUA + "Hortlaklara ekstra 10 hasar vurur.");

        lore.add("");
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "Bir efsaneye göre, yüzyıllar önce,");
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "uzak bir ülkedeki serbest bırakılmış");
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "korkunç bir şeytanı zaptetmek");
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "için kullanıldı.");

        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }

    public static ItemStack createCrimsonRage()
    {
        ItemStack item = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(ChatColor.GOLD + "Kızıl Öfke");
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "Enderlik : Destansı");

        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.getAttributeModifiers(EquipmentSlot.HAND).clear();
        ImmutableMultimap.Builder<Attribute,AttributeModifier> builder = ImmutableMultimap.<Attribute,AttributeModifier>builder();
        builder.put(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(),"generic.attack_damage",11, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HAND));
        builder.put(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(),"generic.attack_speed",-2.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HAND)); //Thus it has 2.5 Attack Speed
        Multimap<Attribute, AttributeModifier> map = builder.build();
        itemMeta.setAttributeModifiers(map);

        lore.add("");
        lore.add(ChatColor.YELLOW + "Saldırı Gücü: 12" + ChatColor.BLUE + " (+3)");
        lore.add(ChatColor.YELLOW + "Saldırı Hızı: 1.5");
        lore.add(ChatColor.DARK_RED + "Warriorlara Özel:");
        lore.add(ChatColor.DARK_RED + "Can Çalma: %25");


        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.addEnchant(Enchantment.DAMAGE_ALL,5,true); //Sharp 1: 1, 2: 1.5, 3: 2, 4: 2.5, 5: 3
        lore.add("");
        lore.add(ChatColor.BLUE + "Keskinlik: 5");
        lore.add(ChatColor.AQUA + "Silahınızın saldırı gücünü 3 artırır.");
        itemMeta.addEnchant(Enchantment.FIRE_ASPECT,3,true);
        lore.add("");
        lore.add(ChatColor.BLUE + "Alevden Çehre: 3");
        lore.add(ChatColor.AQUA + "Hedifinizi 12 saniye boyunca yakar.");

        lore.add("");
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "Bir efsaneye göre, bu balta");
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "cehennem ateşinin kalbinde dövülmüş.");
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "Bu baltanın vurduğu hiçbir canlı");
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "ateşin gazabından kaçamaz.");

        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }

    public static ItemStack createEnginTutku()
    {
        ItemStack item = new ItemStack(Material.GOLDEN_HOE);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(ChatColor.GOLD + "Engin Tutku");
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "Enderlik : Destansı");

        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.getAttributeModifiers(EquipmentSlot.HAND).clear();
        ImmutableMultimap.Builder<Attribute,AttributeModifier> builder = ImmutableMultimap.<Attribute,AttributeModifier>builder();
        builder.put(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(),"generic.attack_damage",19, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HAND));
        builder.put(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(),"generic.attack_speed",-3.5, AttributeModifier.Operation.ADD_NUMBER,EquipmentSlot.HAND)); //Thus it has 0.5 Attack Speed
        Multimap<Attribute, AttributeModifier> map = builder.build();
        itemMeta.setAttributeModifiers(map);


        lore.add("");
        lore.add(ChatColor.YELLOW + "Saldırı Gücü: 20");
        lore.add(ChatColor.YELLOW + "Saldırı Hızı: 0.5");
        lore.add(ChatColor.DARK_RED + "Summonerlara Özel:");
        lore.add(ChatColor.DARK_RED + "Blink");
        lore.add(ChatColor.DARK_RED + "Sağ tık ile küçük bir mesafe ışınlanın.");

        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.addEnchant(CustomEnchants.THUNDERLORDS,1,true); //Sharp 1: 1, 2: 1.5, 3: 2, 4: 2.5, 5: 3
        lore.add("");
        lore.add(ChatColor.BLUE + "Yıldırım Efendisinin Hükmü");
        lore.add(ChatColor.AQUA + "Saldırlarınız hedefinize yıldırım indirir.");

        lore.add("");
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "Çevresindeki ışığı yutan bu şeytani araç,");
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "unutulmuş bir diyarın varlığını kanıtlayan");
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "tek semboldür.Çevreye yaydığı karanlığa");
        lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "bakanların sonsuzlukta kaybolduğu düşunülüyordur.");

        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        return item;
    }
}
