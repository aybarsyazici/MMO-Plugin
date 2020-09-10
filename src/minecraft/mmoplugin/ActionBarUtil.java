package minecraft.mmoplugin;

import net.minecraft.server.v1_16_R1.IChatBaseComponent;
import net.minecraft.server.v1_16_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_16_R1.PlayerConnection;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ActionBarUtil
{
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut)
    {
        CraftPlayer craftPlayer = (CraftPlayer)player;
        PlayerConnection connection = craftPlayer.getHandle().playerConnection;

        IChatBaseComponent titleJSON= IChatBaseComponent.ChatSerializer.a("{\"text\":\""+ title +"\"}");
        IChatBaseComponent subtitleJSON= IChatBaseComponent.ChatSerializer.a("{\"text\":\""+ subtitle +"\"}");

        PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, titleJSON, fadeIn, stay, fadeOut);
        PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, subtitleJSON);

        connection.sendPacket(titlePacket);
        connection.sendPacket(subtitlePacket);
    }

    public static void sendActionBar(Player player, String message, int fadeIn, int stay, int fadeOut)
    {
        CraftPlayer craftPlayer = (CraftPlayer)player;
        PlayerConnection connection = craftPlayer.getHandle().playerConnection;

        IChatBaseComponent titleJSON= IChatBaseComponent.ChatSerializer.a("{\"text\":\""+ message +"\"}");

        PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.ACTIONBAR, titleJSON, fadeIn, stay, fadeOut);

        connection.sendPacket(titlePacket);
    }
}
