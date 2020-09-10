package minecraft.mmoplugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class NPCManager {

    private static Plugin plugin = MainClass.getPlugin(MainClass.class);

    private static List<EntityPlayer> NPCList = new ArrayList<>();
    public static Map<EntityPlayer, UUID> npcIdMap = new HashMap<>();

    public static void createNPC(Player player, String name, String skin, String color)
    {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) Bukkit.getWorld(player.getWorld().getName())).getHandle();
        UUID uuid = UUID.randomUUID();
        GameProfile gameProfile = new GameProfile(uuid,"§"+color+"[NPC]" + name);
        EntityPlayer npc = new EntityPlayer(server, world, gameProfile, new PlayerInteractManager(world));
        Location loc = player.getLocation();
        npc.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());


        String[] skinString = getSkin(player,skin);
        gameProfile.getProperties().put("textures", new Property("textures", skinString[0], skinString[1]));

        addNPCPacket(npc,loc.getWorld());
        NPCList.add(npc);
        /*if(MainClass.npcConfig.getConfig().contains("data"))
        {
            System.out.println("Inside the if");
        }*/
        System.out.println("NPCConfig is " + MainClass.npcConfig);
        MainClass.npcConfig.getConfig().set("data." + uuid + ".x", loc.getX());
        MainClass.npcConfig.getConfig().set("data." + uuid + ".y", loc.getY());
        MainClass.npcConfig.getConfig().set("data." + uuid + ".z", loc.getZ());
        MainClass.npcConfig.getConfig().set("data." + uuid + ".yaw", loc.getYaw());
        MainClass.npcConfig.getConfig().set("data." + uuid + ".pitch", loc.getPitch());
        MainClass.npcConfig.getConfig().set("data." + uuid + ".world", loc.getWorld().getName());
        MainClass.npcConfig.getConfig().set("data." + uuid + ".name", name);
        MainClass.npcConfig.getConfig().set("data." + uuid + ".texture", skinString[0]);
        MainClass.npcConfig.getConfig().set("data." + uuid + ".signature", skinString[1]);
        MainClass.npcConfig.getConfig().set("data." + uuid + ".color", color);
        MainClass.npcConfig.saveConfig();
        loc = null;
        npcIdMap.put(npc,uuid);
    }

    public static String[] getSkin(Player player, String name)
    {
        try
        {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            String uuid = new JsonParser().parse(reader).getAsJsonObject().get("id").getAsString();

            URL url2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader2 = new InputStreamReader(url2.openStream());
            JsonObject property = new JsonParser().parse(reader2).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();


            String texture = property.get("value").getAsString();
            String signature = property.get("signature").getAsString();
            return new String[] {texture, signature};
        }
        catch (Exception e)
        {
            EntityPlayer p = ((CraftPlayer) player).getHandle();
            GameProfile profile = p.getProfile();
            Property property = profile.getProperties().get("textures").iterator().next();
            String texture = property.getValue();
            String signature = property.getSignature();
            e.printStackTrace();
            return new String[] {texture, signature};
        }
    }

    public static void addNPCPacket(EntityPlayer npc, org.bukkit.World world)
    {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(world)) {
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Worlds were the same.");
                PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
                connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
                connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
                connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) (npc.yaw * 256 / 300)));
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,npc));
                    }
                },15);
            }
        }
    }

    public static void addJoinPacket(Player player) {
        for (EntityPlayer npc : NPCList) {
            if (npc.getWorld().getWorld().equals(player.getWorld()))
            {
                //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Worlds were the same.");
                PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
                connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
                connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
                connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) (npc.yaw * 256 / 300)));
                //connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,npc));
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,npc));
                    }
                },15);
            }
        }
    }

    public static List<EntityPlayer> getNPCs()
    {
        return NPCList;
    }

    public static EntityPlayer loadNPC(Location location, GameProfile profile)
    {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        GameProfile gameProfile = profile;
        EntityPlayer npc = new EntityPlayer(server, world, gameProfile, new PlayerInteractManager(world));
        npc.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        addNPCPacket(npc,location.getWorld());
        NPCList.add(npc);
        return npc;
    }

    public static void removeNPC(Player player, EntityPlayer npc)
    {
        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutEntityDestroy(npc.getId()));
    }
}
