package me.shakeforprotein.bungeetalk;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Chars;
import me.shakeforprotein.bungeetalk.Commands.CommandBungeeTalk;
import me.shakeforprotein.bungeetalk.Commands.CommandWins;
import me.shakeforprotein.bungeetalk.Listeners.GamesListener;
import me.shakeforprotein.bungeetalk.Listeners.LaunchListener;
import me.shakeforprotein.bungeetalk.Manager.GameManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public final class BungeeTalk extends Plugin {

    public List<GameManager> registeredGames = new ArrayList<>();
    public String badge = ChatColor.translateAlternateColorCodes('&', "&3&l[&2BungeeTalk&3&l]&r");
    private Configuration config;
    private ScheduledTask scheduledTask;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getProxy().registerChannel("bungeetalk:channel");
        saveDefaultResource("config.yml");
        config = loadYaml("config.yml");
        if (config != null && config.getSection("Games") != null) {
            for (String key : config.getSection("Games").getKeys()) {
                saveDefaultResource(config.getString("Games." + key + ".Filename"));
            }
        }

        GameManager speedTyperManager = new GameManager(this, "SpeedTyper");
        GameManager unscrambleManager = new GameManager(this, "Unscramble");
        registeredGames.add(speedTyperManager);
        registeredGames.add(unscrambleManager);

        getProxy().getPluginManager().registerListener(this, new LaunchListener(this));
        getProxy().getPluginManager().registerListener(this, new GamesListener(this));
        getProxy().getPluginManager().registerCommand(this, new CommandBungeeTalk(this));
        getProxy().getPluginManager().registerCommand(this, new CommandWins(this));
        //getProxy().getPluginManager().registerCommand(this, new CommandWins());
        reloadConfigs();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reloadConfigs() {
        config = loadYaml("config.yml");
        for (GameManager manager : registeredGames) {
            if (config != null && config.get("Games." + manager.getGamemode() + ".Filename") != null) {
                manager.setGameConfig(loadYaml(config.getString("Games." + manager.getGamemode() + ".Filename")));
                manager.reload();
            } else {
                getLogger().warning("Unable to reload manager: " + manager.getGamemode());
            }
        }
    }

    public void convertToYaml(String filename) {
        File inputFile = new File(ProxyServer.getInstance().getPluginsFolder() + File.separator + "BungeeTalk", filename);
        File targetFile = new File(ProxyServer.getInstance().getPluginsFolder() + File.separator + "BungeeTalk", inputFile + ".yml");
        Configuration outputYaml = loadYaml(filename + ".yml");
        List<String> outList = new ArrayList<>();
        try {
            if (!targetFile.exists()) {
                InputStream initialStream = new FileInputStream(inputFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(initialStream));
                while (reader.ready()) {
                    String line = reader.readLine();
                    outList.add(line);
                }
            }
            if (outputYaml != null) {
                outputYaml.set("Phrases", outList);
            }
            saveYaml(outputYaml, filename + ".yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Configuration getConfig() {
        return this.config;
    }

    public Configuration loadYaml(String filename) {
        File file = new File(ProxyServer.getInstance().getPluginsFolder() + File.separator + "BungeeTalk", filename);
        Configuration yaml;
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    getLogger().warning("Failed to load file: " + filename);
                }
                getLogger().warning(badge + "Could not find file: " + filename);
                getLogger().warning(badge + "A new file with name: " + filename + " has been generated.");
            }
            yaml = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return yaml;
    }

    public void saveYaml(Configuration config, String filename) {
        File file = new File(ProxyServer.getInstance().getPluginsFolder() + File.separator + "BungeeTalk", filename);
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    getLogger().warning("Failed to create file: " + filename);
                }
                getLogger().warning(badge + "A new file with name: " + filename + " has been generated.");
            }
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void saveDefaultResource(String filename) {
        try {
            File targetFile = new File(ProxyServer.getInstance().getPluginsFolder() + File.separator + "BungeeTalk", filename);


            if (new File(ProxyServer.getInstance().getPluginsFolder() + File.separator + "BungeeTalk").mkdir()) {
                getLogger().info("Created plugin folder");
            }


            if (!targetFile.exists()) {
                InputStream initialStream = this.getResourceAsStream(filename);
                byte[] buffer = new byte[initialStream.available()];
                if (initialStream.read(buffer) == 99999999) {
                    getLogger().info("This line can safely be ignored. Though I never thought it would actually be seen.");
                }

                OutputStream outStream = new FileOutputStream(targetFile);
                outStream.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startRunnable(GameManager manager) {
        System.out.println("Starting delayed tasks");
        String key = manager.getGamemode();
        int frequency = getConfig().getInt("Games." + key + ".Settings.Frequency");
        int startDelay = getConfig().getInt("Games." + key + ".Settings.StartOffset");
        getProxy().getScheduler().schedule(this, () -> {
            if (ProxyServer.getInstance().getPlayers().size() >= getConfig().getInt("Games." + key + ".Settings.MinimumPlayers")) {
                runGame(key, manager);
            }
        }, startDelay, frequency, TimeUnit.SECONDS);
    }

    public void rewardPlayer(GameManager manager, String playerName) {
        int rand = ThreadLocalRandom.current().nextInt(0, manager.getRewards().size() - 1);
        String reward = manager.getRewards().get(rand).replace("%player%", playerName);
        if (reward.toLowerCase().startsWith("bccommand:")) {
            getProxy().getPluginManager().dispatchCommand(getProxy().getConsole(), reward.split(":")[1]);

        } else if (reward.toLowerCase().startsWith("spcommand:")) {
            if (getProxy().getPlayer(playerName) != null) {
                sendCustomData(getProxy().getPlayer(playerName).getServer(), "execute", reward.split(":")[1]);
            }
        } else if (reward.toLowerCase().startsWith("somethingelse:")) {
            sendCustomData(getProxy().getPlayer(playerName).getServer(), "somethingelse", reward.split(":")[1]);
        }
    }

    public void announceWinner(GameManager manager) {
        getProxy().broadcast(new ComponentBuilder(convertString(manager, "" + manager.getBadge() + " " + manager.getWinMessage())).create());
    }

    private void announceNoWinner(GameManager manager) {
        getProxy().broadcast(new ComponentBuilder(convertString(manager, manager.getBadge() + " " + manager.getNoWinnerMessage())).create());
    }

    @SuppressWarnings("ConstantConditions")
    public void updateStatistic(GameManager manager, String stat) {
        ProxiedPlayer player = getProxy().getPlayer(manager.getWinner());

        Configuration leaderboard = loadYaml(manager.getLeaderboardFile());
        try {
            if (stat.equalsIgnoreCase("TopSpeed")) {
                if (leaderboard != null && leaderboard.getLong(stat + "." + player.getUniqueId().toString()) == 0 || manager.getGameLength() < leaderboard.getLong(stat + "." + player.getUniqueId().toString())) {
                    leaderboard.set(stat + "." + player.getUniqueId().toString(), manager.getGameLength());
                }
            } else if (leaderboard != null && stat.equalsIgnoreCase("Wins")) {
                leaderboard.set(stat + "." + player.getUniqueId().toString(), leaderboard.getLong(stat + "." + player.getUniqueId().toString()) + 1);
            }
            saveYaml(leaderboard, manager.getLeaderboardFile());
        } catch (NullPointerException ex) {
            getLogger().info("Null pointer occurred when checking if stat exists.");
        }
    }

    private void runGame(String mode, GameManager manager) {
        int randomNumber;
        List<String> wordList = manager.getWordList();
        if (mode.equalsIgnoreCase("Unscramble")) {
            randomNumber = ThreadLocalRandom.current().nextInt(0, wordList.size() - 1);
            manager.setAnswer(wordList.get(randomNumber));
            manager.setWord(shuffleWord(wordList.get(randomNumber)));
        } else if (mode.equalsIgnoreCase("SpeedTyper")) {
            randomNumber = ThreadLocalRandom.current().nextInt(0, wordList.size() - 1);
            manager.setAnswer(wordList.get(randomNumber));
            manager.setWord(manager.getAnswer());
        }
        manager.setGameRunning(true);
        manager.setGameStarted(System.currentTimeMillis());
        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            p.sendMessage(ChatMessageType.CHAT, new ComponentBuilder(convertString(manager, getConfig().getString("Games." + mode + ".Messages.Badge") + getConfig().getString("Games." + mode + ".Messages.GameStart"))).create());
        }

        try {
            scheduledTask.cancel();
        } catch (NullPointerException ex) {//do nothing
        }
        scheduledTask = getProxy().getScheduler().schedule(this, () ->
        {
            if (manager.isGameRunning()) {
                announceNoWinner(manager);
                manager.setGameRunning(false);
            }
            try {
                scheduledTask.cancel();
            } catch (NullPointerException ex) {//do nothing
            }
        }, config.getInt("Games." + mode + ".Settings.GameLength"), TimeUnit.SECONDS);
    }


    private String shuffleWord(String word) {
        List<Character> chars = Chars.asList(word.toCharArray());
        Collections.shuffle(chars);
        return new String(Chars.toArray(chars));
    }

    private String convertString(GameManager manager, String input) {
        return ChatColor.translateAlternateColorCodes('&', input.replace("%winner%", manager.getWinner()).replace("%value%", (manager.getGameLength() / 1000) + " Seconds").replace("%answer%", manager.getAnswer()).replace("%word%", manager.getWord()).replace("%gamelength%", config.getString("Games." + manager.getGameLength() + ".Settings.GameLength")));
    }

    public void startRunnables() {
        for (GameManager gameManager : registeredGames) {
            startRunnable(gameManager);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void sendCustomData(Server server, String subChannel, String data1) {
        if (getProxy().getPlayers() == null || getProxy().getPlayers().isEmpty()) {
            return;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subChannel); // the channel could be whatever you want
        out.writeUTF(data1); // this data could be whatever you want
        server.getInfo().sendData("bungeetalk:channel", out.toByteArray()); // we send the data to the server
    }
}
