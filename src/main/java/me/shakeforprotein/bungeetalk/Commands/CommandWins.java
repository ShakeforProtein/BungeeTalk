package me.shakeforprotein.bungeetalk.Commands;

import me.shakeforprotein.bungeetalk.BungeeTalk;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.config.Configuration;

import java.util.*;
import java.util.stream.Collectors;

public class CommandWins extends Command implements TabExecutor {

    private BungeeTalk pl;

    public CommandWins(BungeeTalk main) {
        super("wins");
        this.pl = main;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (args.length > 0 && args.length < 3) {
            String lastError = "UnknownGameMode";
            /*ThoughtProcess: Loop all gamemodes in the main config and compare against argument [0]. If match is found, proceed else  */
            for (String key : pl.getConfig().getSection("Games").getKeys()) {
                if (key.equalsIgnoreCase(args[0])) {
                    lastError = "NoError";
                    Configuration gameConfiguration = pl.loadYaml(pl.getConfig().getString("Games." + key + ".Filename"));
                    Configuration gameLeaderboard = pl.loadYaml(pl.getConfig().getString("Games." + key + ".LeaderboardFile"));
                    if (gameLeaderboard != null && gameLeaderboard.get("Wins") != null) {
                        if (gameConfiguration != null && gameConfiguration.get("Games." + key + ".Messages.Badge") != null) {
                            if (args.length == 1) {/*ThoughtProcess: Player has not specified another player, or special stat.*/
                                if (commandSender instanceof ProxiedPlayer) {
                                    String msg = ChatColor.translateAlternateColorCodes('&',
                                            gameConfiguration.getString("Games." + key + ".Messages.Badge")
                                                    + pl.getConfig().getString("General.Messages.Commands.Wins.Success.DisplayWinsOwn")
                                                    .replace("%wins%", gameLeaderboard.getInt("Wins." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) + ""))
                                                    .replace("%withtime%", pl.getConfig().getString("General.Messages.Commands.Wins.Success.WithTime"))
                                                    .replace("%time%", (gameLeaderboard.getLong("TopSpeed." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) /1000)+ "");
                                    commandSender.sendMessage(msg);
                                } else {
                                    lastError = "NotPlayer";
                                }
                            } else if (args[1].equalsIgnoreCase("top")) {
                                /*ThoughtProcess: Display top 10 by wins*/
                                Map<String, Long> dataMap = new HashMap<>();
                                for(String winners : gameLeaderboard.getSection("Wins").getKeys()){
                                    dataMap.put(winners, gameLeaderboard.getLong("Wins." + winners));
                                }
                                LinkedHashMap<String,  Long> leaderboard;

                                leaderboard = dataMap.entrySet().stream()
                                    .sorted(Map.Entry.comparingByValue())
                                    .collect(Collectors
                                            .toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        gameConfiguration.getString("Games." + key + ".Messages.Badge")
                                                + pl.getConfig().getString("General.Messages.Commands.Wins.Success.TopWinsTitle")

                                        .replace("%wins%", gameLeaderboard.getInt("Wins." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) + ""))
                                        .replace("%withtime%", pl.getConfig().getString("General.Messages.Commands.Wins.Success.WithTime"))
                                        .replace("%time%", (gameLeaderboard.getLong("TopSpeed." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) /1000)+ ""));


                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                                pl.getConfig().getString("General.Messages.Commands.Wins.Success.DisplayTopWinsHeaders")
                                        .replace("%wins%", gameLeaderboard.getInt("Wins." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) + "")
                                        .replace("%withtime%", pl.getConfig().getString("General.Messages.Commands.Wins.Success.WithTime"))
                                        .replace("%time%", (gameLeaderboard.getLong("TopSpeed." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) /1000)+ "")));


                                int i = 0;
                                for(String tempKey : leaderboard.keySet()){
                                    if(i < 10 && i < tempKey.length()) {
                                        commandSender.sendMessage(pl.getConfig().getString("General.Messages.Commands.Wins.Success.DisplayTopWinsFormat")
                                                .replace("%wins%", gameLeaderboard.getLong("Wins." + tempKey) + "")
                                                .replace("%withtime%", pl.getConfig().getString("General.Messages.Commands.Wins.Success.WithTime"))
                                                .replace("%time%", (gameLeaderboard.getLong("TopSpeed." + tempKey) / 1000) + " Seconds")
                                                .replace("%other%", pl.getProxy().getPlayer(tempKey).getName())
                                                .replace("%position%", i + ""));
                                        i++;
                                    }
                                }



                            } else if (args[1].equalsIgnoreCase("TopSpeed")) {
                                /*ThoughtProcess: Display top 10 by speed*/
                                Map<String, Long> dataMap = new HashMap<>();
                                for(String winners : gameLeaderboard.getSection("TopSpeed").getKeys()){
                                    dataMap.put(winners, gameLeaderboard.getLong("TopSpeed." + winners));
                                }
                                LinkedHashMap<String,  Long> leaderboard;

                                leaderboard = dataMap.entrySet().stream()
                                        .sorted(Map.Entry.comparingByValue())
                                        .collect(Collectors
                                                .toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        gameConfiguration.getString("Games." + key + ".Messages.Badge")
                                                + pl.getConfig().getString("General.Messages.Commands.Wins.Success.TopSpeedTitle")

                                                .replace("%wins%", gameLeaderboard.getInt("Wins." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) + ""))
                                        .replace("%withtime%", pl.getConfig().getString("General.Messages.Commands.Wins.Success.WithTime"))
                                        .replace("%time%", (gameLeaderboard.getLong("TopSpeed." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) /1000)+ ""));


                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        pl.getConfig().getString("General.Messages.Commands.Wins.Success.DisplayTopSpeedHeaders")
                                        .replace("%wins%", gameLeaderboard.getInt("Wins." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) + "")
                                        .replace("%withtime%", pl.getConfig().getString("General.Messages.Commands.Wins.Success.WithTime"))
                                        .replace("%time%", (gameLeaderboard.getLong("TopSpeed." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) /1000)+ "")));


                                int i = 0;
                                for(String tempKey : leaderboard.keySet()){
                                    if(i < 10 && i < tempKey.length()) {
                                        commandSender.sendMessage(pl.getConfig().getString("General.Messages.Commands.Wins.Success.DisplayTopSpeedFormat")
                                                .replace("%wins%", gameLeaderboard.getLong("Wins." + tempKey) + "")
                                                .replace("%withtime%", pl.getConfig().getString("General.Messages.Commands.Wins.Success.WithTime"))
                                                .replace("%time%", (gameLeaderboard.getLong("TopSpeed." + tempKey) / 1000) + " Seconds")
                                                .replace("%other%", pl.getProxy().getPlayer(tempKey).getName())
                                                .replace("%position%", i + ""));
                                        i++;
                                    }
                                }
                            } else if (pl.getProxy().getPlayer(args[1]) != null) {
                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', gameConfiguration.getString("Games." + key + ".Messages.Badge") + ""));
                            } else {
                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('%', pl.badge + "" + pl.getConfig().getString("General.Messages.Commands.Wins.Errors.IncorrectUsage")));
                            }
                        } else {
                            lastError = "MissingConfig";
                        }
                    } else {
                        lastError = "ReadError";
                    }
                   break; /*ThoughtProcess: If argument [0] matches a game mode, break out of loop.*/
                }
            }
            if (!lastError.equalsIgnoreCase("NoError")) {
                commandSender.sendMessage(pl.badge + pl.getConfig().getString("General.Messages.Commands.Wins.Errors." + lastError) + " '" + args[0] + "'");
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        List<String> outputStrings = new ArrayList<>();
        List<String> inputStrings = new ArrayList<>();

        if(args.length == 0){
            inputStrings.add("SpeedTyper");
            inputStrings.add("Unscramble");
        }

        if (args.length == 1) {
            inputStrings.add("SpeedTyper");
            inputStrings.add("Unscamble");
        }

        if(args.length == 2){
            inputStrings.add("Top");
            inputStrings.add("TopSpeed");
            for(ProxiedPlayer proxiedPlayer : pl.getProxy().getPlayers()){
                inputStrings.add(proxiedPlayer.getName());
            }
        }

        for (String input : inputStrings) {
            if (args.length == 0 || args[args.length-1].toLowerCase().startsWith(input)) {
                outputStrings.add(input);
            }
        }
        return outputStrings;
    }

}