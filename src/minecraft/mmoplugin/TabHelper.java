package minecraft.mmoplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TabHelper implements TabCompleter
{
    List<String> classArguments = new ArrayList<>();
    List<String> factionArguments = new ArrayList<>();
    List<String> facionRanks = new ArrayList<>();
    List<String> dungeonArguments = new ArrayList<>();
    List<String> dungeonMobArguments = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args)
    {
        if(classArguments.isEmpty())
        {
            classArguments.add("warrior");
            classArguments.add("cleric");
            classArguments.add("necromancer");
            classArguments.add("reset");
        }
        if(factionArguments.isEmpty())
        {
            factionArguments.add("chat");
            factionArguments.add("claim");
            factionArguments.add("rütbe");
            factionArguments.add("create");
            factionArguments.add("transfer");
            factionArguments.add("join");
            factionArguments.add("delete");
            factionArguments.add("leave");
        }
        if(facionRanks.isEmpty())
        {
            facionRanks.add("üye");
            facionRanks.add("memur");
            facionRanks.add("moderatör");
            facionRanks.add("admin");
        }
        if(dungeonArguments.isEmpty())
        {
            dungeonArguments.add("spawn");
            dungeonArguments.add("remove");
        }
        if(dungeonMobArguments.isEmpty())
        {
            dungeonMobArguments.add("husk");
            dungeonMobArguments.add("stray");
            dungeonMobArguments.add("blaze");
            dungeonMobArguments.add("witherskeleton");
            dungeonMobArguments.add("magmacube");
            dungeonMobArguments.add("zombie");
            dungeonMobArguments.add("spider");
            dungeonMobArguments.add("witch");
            dungeonMobArguments.add("elitegiant");
        }


        List<String> toReturn = new ArrayList<>();
        String commandStr = command.getName().toLowerCase();
        switch (commandStr)
        {
            case "class":
            {
                if(args.length == 1)
                {
                    for(String a : classArguments)
                    {
                        if(a.toLowerCase().startsWith(args[0].toLowerCase()))
                        {
                            toReturn.add(a);
                        }
                    }
                    return toReturn;
                }
                break;
            }
            case "faction":
            {
                if(args.length == 1)
                {
                    for(String a : factionArguments)
                    {
                        if(a.toLowerCase().startsWith(args[0].toLowerCase()))
                        {
                            toReturn.add(a);
                        }
                    }
                }
                else if(args.length == 3)
                {
                    if(args[1].toLowerCase().equalsIgnoreCase("rütbe"))
                    {
                        for(String a : facionRanks)
                        {
                            if(a.toLowerCase().startsWith(args[2].toLowerCase()))
                            {
                                toReturn.add(a);
                            }
                        }
                    }
                }
                break;
            }
            case "dungeon":
            {
                if(args.length == 1)
                {
                    for(String a : dungeonArguments)
                    {
                        if (a.toLowerCase().startsWith(args[0].toLowerCase()))
                        {
                            toReturn.add(a);
                        }
                    }
                }
                if (args.length == 2 && args[0].equalsIgnoreCase("spawn"))
                {
                    for(String a : dungeonMobArguments)
                    {
                        if (a.toLowerCase().startsWith(args[1].toLowerCase()))
                        {
                            toReturn.add(a);
                        }
                    }
                }
                break;
            }

        }
        return toReturn;
    }
}
