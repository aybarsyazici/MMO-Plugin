package minecraft.mmoplugin;

import minecraft.mmoplugin.customItems.CustomItems;
import net.minecraft.server.v1_16_R1.GenericAttributes;
import net.minecraft.server.v1_16_R1.NBTTagCompound;
import net.minecraft.server.v1_16_R1.NBTTagList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Warrior extends MMOClass
{

    private BukkitTask moveSpeed;

    public BukkitTask getMoveSpeedID()
    {
        return this.moveSpeed;
    }

    public void setMoveSpeedID(BukkitTask task)
    {
        this.moveSpeed = task;
    }

    public Warrior(Plugin plugin, Player player, double xp, int level, String className) {
        super(plugin, player, xp, level, className);
        this.moveSpeed = null;
        //plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "Warrior Constructor RUN!");
    }


    public static ItemStack addAncestorsStrength(ItemStack item, int damageToAdd, Plugin plugin)
    {
        if(!item.getType().equals(Material.AIR)) {
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,new AttributeModifier(UUID.randomUUID(),"generic.attack_damage",damageToAdd, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND));

            List<String> lore = itemMeta.getLore();
            CustomItems.Enderlik rarity = CustomItems.Enderlik.SIRADAN;

            if(lore == null)
                lore = new ArrayList<>();
            else
            {
                for(String s : lore)
                {
                    if(s.contains("Enderlik : "))
                    {
                        String temp = s;
                        temp = ChatColor.stripColor(temp);
                        temp = temp.substring(11);
                        switch (temp)
                        {
                            case "Sıradan":
                                rarity = CustomItems.Enderlik.SIRADAN;
                                break;
                            case "Nadir":
                                rarity = CustomItems.Enderlik.NADIR;
                                break;
                            case "Eşsiz":
                                rarity = CustomItems.Enderlik.ESSIZ;
                                break;
                            case "Olağanüstü":
                                rarity = CustomItems.Enderlik.OLAGANUSTU;
                                break;
                            case "Destansı":
                                rarity = CustomItems.Enderlik.DESTANSI;
                                break;
                        }
                        break;
                    }
                }
            }

            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "Ana ele aktarılan güç : " + damageToAdd);

            String oldName = itemMeta.getDisplayName();
            oldName = ChatColor.translateAlternateColorCodes('&', "&5[ANCESTORS STRENGTH] " + CustomItems.rarityColor(rarity) +oldName);

            itemMeta.setLore(lore);
            itemMeta.setDisplayName(oldName);
            item.setItemMeta(itemMeta);
            return item;
        }
        return item;
    }
}

