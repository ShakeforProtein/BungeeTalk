package me.shakeforprotein.bungeetalk.Manager;

import me.shakeforprotein.bungeetalk.BungeeTalk;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AnnounceManager {

    private Configuration config;
    private BungeeTalk pl;
    private boolean enabled;
    private long startDelay;
    private long frequency;
    private ScheduledTask announcerTask;

    public AnnounceManager(BungeeTalk main) {
        this.pl = main;
        pl.saveDefaultResource(pl.getConfig().getString("Announcer.Settings.StringsFilename"));
        this.enabled = pl.getConfig().getBoolean("Announcer.Settings.Enabled");
        this.config = pl.loadYaml(pl.getConfig().getString("Announcer.Settings.StringsFilename"));
        this.startDelay = pl.getConfig().getLong("Announcer.Settings.StartOffset");
        this.frequency = pl.getConfig().getLong("Announcer.Settings.Frequency");
    }

    private BaseComponent prepareLinks(String inputKey, ProxiedPlayer receiver) {
        String input = config.getString("Messages." + inputKey + ".Text");
        input = input.replace("%PLAYER_NAME%", receiver.getName());
        input = input.replace("%SERVER%", receiver.getServer().getInfo().getName());
        input = input.replace("%MOTD%", receiver.getServer().getInfo().getMotd());
        input = input.replace("%BADGE%", pl.badge);



        input = ChatColor.translateAlternateColorCodes('&', input);
        List<String> parts = Arrays.stream(input.split("%")).collect(Collectors.toList());
        BaseComponent actualOutput = new TextComponent();
        for (int i = 0; i < parts.size(); i++) {
            if (i % 2 != 0) {
                if (config.getSection("Effects").getKeys().contains(parts.get(i))) {
                    String inText = ChatColor.translateAlternateColorCodes('&', config.getString("Effects." + parts.get(i) + ".Text"));
                    inText = inText.replace("%PLAYER_NAME%", receiver.getName());
                    inText = inText.replace("%SERVER%", receiver.getServer().getInfo().getName());
                    inText = inText.replace("%MOTD%", receiver.getServer().getInfo().getMotd());
                    inText = inText.replace("%BADGE%", pl.badge);
                    TextComponent text = new TextComponent(inText);
                    if (config.getString("Effects." + parts.get(i) + ".Type") != null && config.getString("Effects." + parts.get(i) + ".Type").equalsIgnoreCase("RUN_COMMAND")) {
                        ClickEvent event = new ClickEvent(ClickEvent.Action.RUN_COMMAND, config.getString("Effects." + parts.get(i) + ".Value").toUpperCase());
                        text.setClickEvent(event);
                    } else if (config.getString("Effects." + parts.get(i) + ".Type") != null && config.getString("Effects." + parts.get(i) + ".Type").equalsIgnoreCase("SUGGEST_COMMAND")) {
                        ClickEvent event = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, config.getString("Effects." + parts.get(i) + ".Value"));
                        text.setClickEvent(event);
                    } else if (config.getString("Effects." + parts.get(i) + ".Type") != null && config.getString("Effects." + parts.get(i) + ".Type").equalsIgnoreCase("OPEN_URL")) {
                        ClickEvent event = new ClickEvent(ClickEvent.Action.OPEN_URL, config.getString("Effects." + parts.get(i) + ".Value"));
                        text.setClickEvent(event);
                    }

                    if (config.getString("Effects." + parts.get(i) + ".HoverText") != null) {
                        HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', config.getString("Effects." + parts.get(i) + ".HoverText"))).create());
                        text.setHoverEvent(event);
                    } else {
                        pl.getLogger().info("Effects HoverText = null");
                    }
                    actualOutput.addExtra(text);
                }
            } else {
                actualOutput.addExtra(ChatColor.translateAlternateColorCodes('&', parts.get(i)));
            }

        }

        return actualOutput;
    }

    private void doAnnouncement() {
        if (enabled) {
            ArrayList<String> messages = new ArrayList<>(config.getSection("Messages").getKeys());
            String key;
            if (!messages.isEmpty() && messages.size() > 1) {
                key = messages.get(ThreadLocalRandom.current().nextInt(0, messages.size()));
            } else {
                key = messages.get(0);
            }

            String server = config.getString("Messages." + key + ".Server");
            for (ProxiedPlayer player : pl.getProxy().getPlayers()) {
                if (server.equalsIgnoreCase("All") || server.equalsIgnoreCase(player.getServer().getInfo().getName())) {
                    player.sendMessage(ChatMessageType.CHAT, prepareLinks(key, player));
                }
            }
        }
    }

    public void reload() {
        this.enabled = pl.getConfig().getBoolean("Announcer.Settings.Enabled");
        this.config = pl.loadYaml(pl.getConfig().getString("Announcer.Settings.StringsFilename"));
        this.startDelay = pl.getConfig().getLong("Announcer.Settings.StartOffset");
        this.frequency = pl.getConfig().getLong("Announcer.Settings.Frequency");
    }

    public void start() {
        announcerTask = pl.getProxy().getScheduler().schedule(pl, () -> {
            if (pl.getConfig().getBoolean("Announcer.Settings.Enabled")) {
                doAnnouncement();
            } else {
                pl.getLogger().warning("Announcer is disabled???");
                pl.getLogger().warning((getConfig() == null) + "");
            }
        }, startDelay, frequency, TimeUnit.SECONDS);
    }

    public void stop() {
        if (announcerTask != null) {
            announcerTask.cancel();
        }
    }

    private Configuration getConfig() {
        return config;
    }

}
