package me.shakeforprotein.bungeetalk;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.shakeforprotein.bungeetalk.Commands.CommandBungeeTalk;
import me.shakeforprotein.bungeetalk.Commands.CommandWins;
import me.shakeforprotein.bungeetalk.Listeners.GamesListener;
import me.shakeforprotein.bungeetalk.Listeners.LaunchListener;
import me.shakeforprotein.bungeetalk.Manager.AnnounceManager;
import me.shakeforprotein.bungeetalk.Manager.GameManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class BungeeTalk extends Plugin {

    public List<GameManager> registeredGames = new ArrayList<>();
    public String badge = ChatColor.translateAlternateColorCodes('&', "&3&l[&2BungeeTalk&3&l]&r");
    private Configuration config;
    private Configuration uuidCache;

    private Configuration uuidCacheFull;
    private AnnounceManager announcer;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getProxy().registerChannel("bungeetalk:channel");
        saveDefaultResource("config.yml");
        saveDefaultResource("uuidCache.yml");
        saveDefaultResource("uuidCacheFull.yml");
        config = loadYaml("config.yml");
        uuidCache = loadYaml("uuidCache.yml");
        uuidCacheFull = loadYaml("uuidCacheFull.yml");
        registerManagers();

        getProxy().getPluginManager().registerListener(this, new LaunchListener(this));
        getProxy().getPluginManager().registerListener(this, new GamesListener(this));
        getProxy().getPluginManager().registerCommand(this, new CommandBungeeTalk(this));
        getProxy().getPluginManager().registerCommand(this, new CommandWins(this));
        reloadConfigs();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveYaml(uuidCacheFull, "uuidCacheFull.yml");
    }

    private void registerManagers(){

        GameManager speedTypistManager = new GameManager(this, "SpeedTypist");
        GameManager unscrambleManager = new GameManager(this, "Unscramble");
        announcer = new AnnounceManager(this);
        registeredGames.add(speedTypistManager);
        registeredGames.add(unscrambleManager);
    }


    public void reloadConfigs() {
        config = loadYaml("config.yml");
        for (GameManager manager : registeredGames) {
            if (config != null && config.get("Games." + manager.getGameMode() + ".Filename") != null) {
                if(config.getBoolean("Games." + manager.getGameMode() + ".Settings.Enabled")) {
                    manager.setGameConfig(loadYaml(config.getString("Games." + manager.getGameMode() + ".Filename")));
                    manager.reload();
                } else{
                    manager.setEnabled(false);
                }
            } else {
                getLogger().warning("Unable to reload manager: " + manager.getGameMode());
            }
        }
        announcer.stop();
        announcer.reload();
        announcer.start();
        getLogger().info(badge + "Announcer has been reloaded");
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
                getLogger().info("Saved default file:" + filename);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startTasks() {
        System.out.println("Starting delayed tasks");

        for(GameManager game : registeredGames){
            game.start();
        }
        announcer.start();
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
        } else if (reward.toLowerCase().startsWith("somethingElse:")) {
            sendCustomData(getProxy().getPlayer(playerName).getServer(), "somethingElse", reward.split(":")[1]);
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


    public Configuration getUuidCache() {
        return uuidCache;
    }

    public Configuration getUuidCacheFull() {
        return uuidCacheFull;
    }

}
