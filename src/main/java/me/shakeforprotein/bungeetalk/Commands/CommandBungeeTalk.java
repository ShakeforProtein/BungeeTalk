package me.shakeforprotein.bungeetalk.Commands;

import me.shakeforprotein.bungeetalk.BungeeTalk;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public class CommandBungeeTalk extends Command implements TabExecutor {

    private BungeeTalk pl;

    public CommandBungeeTalk(BungeeTalk main) {
        super("bungeetalk", "treebo.staff");
        this.pl = main;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (args.length > 0) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    pl.reloadConfigs();
                } else if (args[0].equalsIgnoreCase("SaveDefaultConfigs")) {
                    pl.saveDefaultResource("config.yml");
                    for (String key : pl.getConfig().getSection("Games").getKeys()) {
                        pl.saveDefaultResource(pl.getConfig().getString("Games." + key + ".Filename"));
                    }
                    pl.saveYaml(pl.getConfig(), "config.yml");
                    commandSender.sendMessage(pl.badge + "Default files have been written (This command will not overwrite an existing file)");
                } else if (args[0].equalsIgnoreCase("version")){
                    commandSender.sendMessage(pl.badge + "Version: " + pl.getDescription().getVersion() + ", Written by: " + pl.getDescription().getAuthor() + ", for Treebo Minecraft");
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("convertToYaml")) {
                    pl.convertToYaml(args[1]);
                    Configuration testFile = pl.loadYaml(args[1] + ".yml");
                    if (testFile != null && testFile.get("Phrases") != null) {
                        commandSender.sendMessage(pl.badge + "File conversion successful");
                    } else {
                        commandSender.sendMessage(pl.badge + "File conversion unsuccessful");
                    }
                }
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args) {
        List<String> outputStrings = new ArrayList<>();
        List<String> inputStrings = new ArrayList<>();

        inputStrings.add("reload");
        inputStrings.add("SaveDefaultConfigs");
        inputStrings.add("ConvertToYaml");
        inputStrings.add("Version");

        if (args.length > 0) {
            if (args.length == 1) {
                if (!inputStrings.isEmpty()) {
                    for (String input : inputStrings) {
                        if (args[0].toLowerCase().startsWith(input)) {
                            outputStrings.add(input);
                        }
                    }
                }
            }
        }
        return outputStrings;
    }

}
