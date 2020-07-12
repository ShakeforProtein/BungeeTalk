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
            /*ThoughtProcess: Loop all gameModes in the main config and compare against argument [0]. If match is found, proceed else  */
            for (String key : pl.getConfig().getSection("Games").getKeys()) {
                if (key.equalsIgnoreCase(args[0])) {
                    lastError = "NoError";
                    Configuration gameLeaderBoard = pl.loadYaml(pl.getConfig().getString("Games." + key + ".LeaderBoardFile"));
                    if (gameLeaderBoard != null && gameLeaderBoard.get("Wins") != null) {
                        if (pl.getConfig() != null && pl.getConfig().get("Games." + key + ".Messages.Badge") != null) {
                            if (args.length == 1) {/*ThoughtProcess: Player has not specified another player, or special stat.*/
                                if (commandSender instanceof ProxiedPlayer) {
                                    String msg = ChatColor.translateAlternateColorCodes('&',
                                            pl.getConfig().getString("Games." + key + ".Messages.Badge")
                                                    + pl.getConfig().getString("General.Messages.Commands.Wins.Success.DisplayWinsOwn")
                                                    .replace("%wins%", gameLeaderBoard.getInt("Wins." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) + ""))
                                            .replace("%withTime%", pl.getConfig().getString("General.Messages.Commands.Wins.Success.WithTime"))
                                            .replace("%time%", (gameLeaderBoard.getLong("TopSpeed." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) / 1000.0) + "");
                                    commandSender.sendMessage(msg);
                                } else {
                                    lastError = "NotPlayer";
                                }
                            } else if (args[1].equalsIgnoreCase("top")) {
                                /*ThoughtProcess: Display top 10 by wins*/
                                Map<String, Long> dataMap = new HashMap<>();
                                for (String winners : gameLeaderBoard.getSection("Wins").getKeys()) {
                                    dataMap.put(winners, gameLeaderBoard.getLong("Wins." + winners));
                                }
                                LinkedHashMap<String, Long> leaderBoard;

                                leaderBoard = dataMap.entrySet().stream()
                                        .sorted(Map.Entry.comparingByValue())
                                        .collect(Collectors
                                                .toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        pl.getConfig().getString("Games." + key + ".Messages.Badge")
                                                + pl.getConfig().getString("General.Messages.Commands.Wins.Success.TopWinsTitle")

                                                .replace("%wins%", gameLeaderBoard.getInt("Wins." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) + ""))
                                        .replace("%withTime%", pl.getConfig().getString("General.Messages.Commands.Wins.Success.WithTime"))
                                        .replace("%time%", (gameLeaderBoard.getLong("TopSpeed." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) / 1000.0) + ""));


                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        pl.getConfig().getString("General.Messages.Commands.Wins.Success.DisplayTopWinsHeaders")
                                                .replace("%wins%", gameLeaderBoard.getInt("Wins." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) + "")
                                                .replace("%withTime%", pl.getConfig().getString("General.Messages.Commands.Wins.Success.WithTime"))
                                                .replace("%time%", (gameLeaderBoard.getLong("TopSpeed." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) / 1000.0) + "")));


                                int i = 0;
                                for (String tempKey : leaderBoard.keySet()) {
                                    if (i < 10 && i < tempKey.length()) {
                                        commandSender.sendMessage(pl.getConfig().getString("General.Messages.Commands.Wins.Success.DisplayTopWinsFormat")
                                                .replace("%wins%", gameLeaderBoard.getLong("Wins." + tempKey) + "")
                                                .replace("%withTime%", pl.getConfig().getString("General.Messages.Commands.Wins.Success.WithTime"))
                                                .replace("%time%", (gameLeaderBoard.getLong("TopSpeed." + tempKey) / 1000.0) + " Seconds")
                                                .replace("%other%", pl.getUuidCache().getString(tempKey))
                                                .replace("%position%", i + ""));
                                        i++;
                                    }
                                }


                            } else if (args[1].equalsIgnoreCase("TopSpeed")) {
                                /*ThoughtProcess: Display top 10 by speed*/
                                Map<String, Long> dataMap = new HashMap<>();
                                for (String winners : gameLeaderBoard.getSection("TopSpeed").getKeys()) {
                                    dataMap.put(winners, gameLeaderBoard.getLong("TopSpeed." + winners));
                                }
                                LinkedHashMap<String, Long> leaderBoard;

                                leaderBoard = dataMap.entrySet().stream()
                                        .sorted(Map.Entry.comparingByValue())
                                        .collect(Collectors
                                                .toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        pl.getConfig().getString("Games." + key + ".Messages.Badge")
                                                + pl.getConfig().getString("General.Messages.Commands.Wins.Success.TopSpeedTitle")

                                                .replace("%wins%", gameLeaderBoard.getInt("Wins." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) + ""))
                                        .replace("%withTime%", pl.getConfig().getString("General.Messages.Commands.Wins.Success.WithTime"))
                                        .replace("%time%", (gameLeaderBoard.getLong("TopSpeed." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) / 1000.0) + ""));


                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        pl.getConfig().getString("General.Messages.Commands.Wins.Success.DisplayTopSpeedHeaders")
                                                .replace("%wins%", gameLeaderBoard.getInt("Wins." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) + "")
                                                .replace("%withTime%", pl.getConfig().getString("General.Messages.Commands.Wins.Success.WithTime"))
                                                .replace("%time%", (gameLeaderBoard.getLong("TopSpeed." + ((ProxiedPlayer) commandSender).getUniqueId().toString()) / 1000.0) + "")));


                                int i = 0;
                                for (String tempKey : leaderBoard.keySet()) {
                                    if (i < 10 && i < tempKey.length()) {
                                        commandSender.sendMessage(pl.getConfig().getString("General.Messages.Commands.Wins.Success.DisplayTopSpeedFormat")
                                                .replace("%wins%", gameLeaderBoard.getLong("Wins." + tempKey) + "")
                                                .replace("%withTime%", pl.getConfig().getString("General.Messages.Commands.Wins.Success.WithTime"))
                                                .replace("%time%", (gameLeaderBoard.getLong("TopSpeed." + tempKey) / 1000.0) + " Seconds")
                                                .replace("%other%", pl.getUuidCache().getString(tempKey))
                                                .replace("%position%", i + ""));
                                        i++;
                                    }
                                }
                            } else if (pl.getProxy().getPlayer(args[1]) != null) {
                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', pl.getConfig().getString("Games." + key + ".Messages.Badge") + ""));
                            } else {
                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('%', pl.badge + "" + pl.getConfig().getString("General.Messages.Commands.Wins.Errors.IncorrectUsage")));
                            }
                        } else {
                            lastError = "MissingConfig";
                        }
                    } else {
                        lastError = "ReadError";
                    }
                    break;
                    /*ThoughtProcess: If argument [0] matches a game mode, break out of loop.*/
                }
            }
            if (!lastError.equalsIgnoreCase("NoError") && pl.getConfig() != null && pl.getConfig().get("General.Messages.Commands.Wins.Errors") != null) {
                commandSender.sendMessage(pl.badge + pl.getConfig().getString("General.Messages.Commands.Wins.Errors." + lastError) + " '" + args[0] + "'");
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        List<String> outputStrings = new ArrayList<>();
        List<String> inputStrings = new ArrayList<>();

        if (args.length == 0) {
            inputStrings.add("SpeedTypist");
            inputStrings.add("Unscramble");
        }

        if (args.length == 1) {
            inputStrings.add("SpeedTypist");
            inputStrings.add("Unscramble");
        }

        if (args.length == 2) {
            inputStrings.add("Top");
            inputStrings.add("TopSpeed");
            for (ProxiedPlayer proxiedPlayer : pl.getProxy().getPlayers()) {
                inputStrings.add(proxiedPlayer.getName());
            }
        }

        for (String input : inputStrings) {
            if (args.length == 0 || args[args.length - 1].toLowerCase().startsWith(input)) {
                outputStrings.add(input);
            }
        }
        return outputStrings;
    }

}