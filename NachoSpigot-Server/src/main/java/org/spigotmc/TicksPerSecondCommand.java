package org.spigotmc;

import dev.cobblesword.nachospigot.CC;
import net.jafama.FastMath;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TicksPerSecondCommand extends Command
{

    public TicksPerSecondCommand(String name)
    {
        super( name );
        this.description = "Gets the current ticks per second for the server";
        this.usageMessage = "/tps";
        this.setPermission( "bukkit.command.tps" );
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args)
    {
        if ( !testPermission( sender ) )
        {
            return true;
        }

        // PaperSpigot start - Further improve tick handling
        double[] tps = org.bukkit.Bukkit.spigot().getTPS();
        String[] tpsAvg = new String[tps.length];

        for ( int i = 0; i < tps.length; i++) {
            tpsAvg[i] = format( tps[i] );
        }
        sender.sendMessage( ChatColor.GRAY + "TPS actuel, à 1, 5 et 15 minutes: §e" + String.format("%.2f, ", MinecraftServer.getServer().lastTps) + org.apache.commons.lang.StringUtils.join(tpsAvg, "§7, §e"));

        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        sender.sendMessage(ChatColor.GRAY + "Usage processeur actuel: §e" + String.format("%.2f", osBean.getProcessCpuLoad() * 100.00D) + "% §7(système: §e" + String.format("%.2f", osBean.getSystemCpuLoad() * 100.00D) + "%§7)");

        sender.sendMessage(ChatColor.GRAY + "Usage mémoire actuel: §e" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)) + "§7/§e" + (Runtime.getRuntime().totalMemory() / (1024 * 1024)) + " mb §7(alloué: " + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + " mb)");

        sender.sendMessage("§7");
        sender.sendMessage("§7Serveur en ligne depuis: §e" + DurationFormatUtils.formatDuration(ManagementFactory.getRuntimeMXBean().getUptime(), "HH:mm:ss,SSS"));
        sender.sendMessage("§e" + MinecraftServer.getServer().getPlayerList().getPlayerCount() + "§7 joueurs en ligne, avec en moyenne §e" + MinecraftServer.getServer().averagePlayerCount
                + "§7 joueurs. Maximum de joueurs sur cette instance atteint le §e" + new SimpleDateFormat("dd/MM HH:mm:ss").format(MinecraftServer.getServer().maximumPlayerCountDate) +
                " §7(§e" + MinecraftServer.getServer().maximumPlayerCount + "§7).");

        sender.sendMessage("§8");

        sender.sendMessage("§7§m---------§e Graphique TPS §7§m---------");
        getGraph(Bukkit.spigot().getTPSOverTime(30)).forEach(sender::sendMessage);
        sender.sendMessage("§7§m-------------------------------");

        sender.sendMessage("§8");

        sender.sendMessage("§7Informations sur les mondes:");

        final List<World> worlds = Bukkit.getWorlds();
        for (final World w : worlds) {
            String worldType = "World";
            switch (w.getEnvironment()) {
                case NETHER:
                    worldType = "Nether";
                    break;
                case THE_END:
                    worldType = "The End";
                    break;
            }

            int tileEntities = 0;

            try {
                for (final Chunk chunk : w.getLoadedChunks()) {
                    tileEntities += chunk.getTileEntities().length;
                }
            } catch (final java.lang.ClassCastException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Corrupted chunk data on world " + w, ex);
            }

            sender.sendMessage(" " + CC.strikeThrough + "  §7 " + worldType + " \"" + w.getName() + "\": §e" + w.getLoadedChunks().length + " §7chunks chargés, §e" + w.getEntities().size() + " §7entitées et §e" + tileEntities + " §7tiles.");
        }

        return true;
    }

    private static List<String> getGraph(List<Double> tpsHistory) {
        List<String> graphMessage = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            graphMessage.add(new String(""));

        tpsHistory.forEach((tpsValue) -> {
            ChatColor color = getGraphColor(tpsValue);
            graphMessage.set(0, graphMessage.get(0) + color + ((tpsValue > 19.00D) ? "#" : "§7#"));
            graphMessage.set(1, graphMessage.get(1) + color + ((tpsValue > 17.00D) ? "#" : "§7#"));
            graphMessage.set(2, graphMessage.get(2) + color + ((tpsValue > 14.00D) ? "#" : "§7#"));
            graphMessage.set(3, graphMessage.get(3) + color + ((tpsValue > 10.00D) ? "#" : "§7#"));
            graphMessage.set(4, graphMessage.get(4) + color + ((tpsValue > 0.00D) ? "#" : "§7#"));
        });

        return graphMessage;
    }

    private static ChatColor getGraphColor(double tps) {
        if (tps >= 19.00D)
            return ChatColor.DARK_GREEN;
        else if (isBetween(tps, 17.00D, 19.00D))
            return ChatColor.GREEN;
        else if (isBetween(tps, 14.00D, 17.00D))
            return ChatColor.YELLOW;
        else if (isBetween(tps, 10.00D, 14.00D))
            return ChatColor.GOLD;
        else if (isBetween(tps, 0.00D, 10.00D))
            return ChatColor.RED;
        return ChatColor.BLACK;
    }

    private static boolean isBetween(double value, double min, double max) {
        return value > min && value <= max;
    }

    private static String format(double tps) // PaperSpigot - made static
    {
        return ( ( tps > 18.0 ) ? ChatColor.GREEN : ( tps > 16.0 ) ? ChatColor.YELLOW : ChatColor.RED ).toString()
                + ( ( tps > 20.0 ) ? "*" : "" ) + FastMath.min( FastMath.round( tps * 100.0 ) / 100.0, 20.0 );
    }
}
