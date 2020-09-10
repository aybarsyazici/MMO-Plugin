package minecraft.mmoplugin.customItems;

import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CustomEnchants
{
    public static final Enchantment THUNDERLORDS = new EnchantmentWrapper("thunderlords", "Thunderlords", 1);

    public static void register()
    {
        boolean registered = Arrays.stream(Enchantment.values()).collect(Collectors.toList()).contains(THUNDERLORDS);

        if (!registered)
            registerEnchantment(THUNDERLORDS);
    }

    public static void registerEnchantment(Enchantment enchantment)
    {
        try
        {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null,true);
            Enchantment.registerEnchantment(enchantment);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
