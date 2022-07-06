package uk.co.benkeoghcgd.api.AxiusCore.API;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.benkeoghcgd.api.AxiusCore.API.Enums.VersionFormat;
import uk.co.benkeoghcgd.api.AxiusCore.API.Utilities.PublicPluginData;
import uk.co.benkeoghcgd.api.AxiusCore.AxiusCore;
import uk.co.benkeoghcgd.api.AxiusCore.API.Enums.PluginStatus;
import uk.co.benkeoghcgd.api.AxiusCore.Exceptions.CommandRegisterException;
import uk.co.benkeoghcgd.api.AxiusCore.Utils.GUIAssets;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class AxiusPlugin extends JavaPlugin {
    /*
    Plugin Instances
     */
    JavaPlugin jpinstance;
    AxiusPlugin apinstance;

    /**
     * Assigns default instance values for getters #getJavaPlugin() and #getAxiusPlugin()
     */
    public AxiusPlugin() {
        jpinstance = this;
        apinstance = this;
    }

    /**
     * Returns the instance of the plugins JavaPlugin
     *
     * @return a JavaPlugin of this plugin instance
     */
    public JavaPlugin getJavaPlugin() { return jpinstance; }

    /**
     * Returns the instance of this AxiusPlugin
     *
     * @return returns itself
     */
    public AxiusPlugin getAxiusPlugin() { return apinstance; }
    public File pluginFile() { return this.getFile(); }



    /*
    Variables
     */
    protected List<Command> commands = new ArrayList<>();
    public List<Exception> errors = new ArrayList<>();

    static {
        try {
            Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (CommandMap) f.get(Bukkit.getServer());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    protected AxiusCore core;
    private static CommandMap commandMap;



    /*
    Plugin Status
     */
    public PluginStatus status = PluginStatus.RUNNING;

    /**
     * Retrieve the plugin status
     *
     * @return the current PluginStatus
     */
    public PluginStatus pullStatus() { return status; }

    /**
     * Set the plugin status
     *
     * @param newStatus the new status for the plugin
     */
    public void setStatus(PluginStatus newStatus) {status = newStatus;}

    /**
     * Refresh the current plugin status based on amount of errors.
     * This function can be overriden to provide custom status checking
     */
    public void refreshStatus() {
        if(errors.size() == 0) status = PluginStatus.RUNNING;
        if(errors.size() > 0) status = PluginStatus.OPERATIONAL;
        if(errors.size() > 5) status = PluginStatus.MALFUNCTIONED;
    }



    /*
    Public Plugin Data
     */
    PublicPluginData ppd = new PublicPluginData();

    /**
     * Get the public plugin data type from an AxiusPlugin
     *
     * @return instances PublicPluginData type
     * @see PublicPluginData
     */
    public PublicPluginData GetPublicPluginData() { return ppd; }

    /**
     * Set the public plugin data type for an AxiusPlugin
     *
     * @param ppd new PublicPluginData type to replace current
     */
    public void SetPublicPluginData(PublicPluginData ppd) { this.ppd = ppd; }

    /**
     * Enable Auto-updating with your AxiusPlugin, through Spigot
     *
     * @param SpigotResourceID Resource ID from your uploaded Spigot resource
     */
    protected void EnableUpdater(int SpigotResourceID) {
        ppd.setSpigotResourceID(SpigotResourceID);
        ppd.setPublicStatus(true);
    }

    /**
     * Enable Auto-updating with your AxiusPlugin, through Spigot
     *
     * @param SpigotResourceID Resource ID from your uploaded Spigot resource
     */
    protected void EnableUpdater(int SpigotResourceID, VersionFormat versionFormat, String versionSeperator) {
        ppd.setSpigotResourceID(SpigotResourceID);
        ppd.setPublicStatus(true);
        ppd.setVersionFormat(versionFormat);
        ppd.setVersionSeperator(versionSeperator);
    }



    /*
    Cosmetic Changes
     */
    String nameFormatted;
    ItemStack guiIcon;

    /**
     * Get the formatted name of this AxiusPlugin
     *
     * @return a color translated message prefix
     */
    public String getNameFormatted() { return nameFormatted; }

    /**
     * Set the formatted name of this AxiusPlugin
     * ALLOWS FORMATTING USING '&' COLOR CODES
     *
     * @param name the raw string for the prefix
     */
    protected void setFormattedName(String name) { nameFormatted = ChatColor.translateAlternateColorCodes('&', name); }

    /**
     * Get the plugin Icon as an ItemStack
     *
     * @return returns the ItemStack type set as the Icon in plugin post-registry
     */
    public ItemStack getIcon() { return guiIcon; }

    /**
     * Set the plugin Icon using an ItemStack
     *
     * @param stack The new ItemStack to set as the plugin Icon
     */
    public void setIcon(ItemStack stack) { guiIcon = stack; }



    /*
    Functionality and Mechanics
     */
    /**
     * Intermittent function that runs before the plugin self-registers to the core.
     * Should be overriden to implement pre-registry tasks like command generation
     * and listener registration.
     */
    protected abstract void Preregister();

    /**
     * Intermittent function that runs after the plugin self-registers to the core.
     * Should be overriden to implement post-registry tasks, such as those which depend
     * on the core.
     */
    protected abstract void Postregister();

    /**
     * Intermittent function that runs before the plugin deregisters itself from the core.
     * Should be overriden to implement pre-shut down procedures.
     */
    protected abstract void Stop();

    /**
     * Intermittent function that runs after the plugin deregisters itself from the core.
     * Should be overriden to implement post-register shut down procedures.
     */
    protected abstract void FullStop();

    /**
     * Register all commands from "commands" list to the command map.
     */
    protected void registerCommands() {
        for(Command c : commands) {
            if(!commandMap.register(getName(), c))
                errors.add(new CommandRegisterException(c));
        }
    }



    /*
    JavaPlugin requirements
     */
    @Override
    public void onEnable() {
        Plugin pl = Bukkit.getPluginManager().getPlugin("AxiusCore");

        if(pl == null) return;
        core = (AxiusCore) pl;
        assert core != null;

        Preregister();
        GUIAssets.generateDecor();
        if(!core.registerPlugin(this)) return;
        Postregister();
    }

    @Override
    public void onDisable() {
        Stop();
        core.unregisterPlugin(this);
        FullStop();
    }
}
