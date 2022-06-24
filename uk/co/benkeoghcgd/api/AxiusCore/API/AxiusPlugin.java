package uk.co.benkeoghcgd.api.AxiusCore.API;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.benkeoghcgd.api.AxiusCore.API.Premium.PremiumLink;
import uk.co.benkeoghcgd.api.AxiusCore.AxiusCore;
import uk.co.benkeoghcgd.api.AxiusCore.API.Enums.PluginStatus;
import uk.co.benkeoghcgd.api.AxiusCore.Exceptions.CommandRegisterException;
import uk.co.benkeoghcgd.api.AxiusCore.Exceptions.InvalidPremiumAuthException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class AxiusPlugin extends JavaPlugin {
    public List<Command> commands = new ArrayList<>();
    public List<Exception> errors = new ArrayList<>();
    protected PluginStatus status = PluginStatus.RUNNING;
    protected ItemStack guiIcon;
    private String nameFormatted;
    public AxiusCore core;
    public long lastUpdate = 0L;
    private static CommandMap commandMap;
    protected PremiumLink premiumData = null;

    static {
        try {
            Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (CommandMap) f.get(Bukkit.getServer());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public ItemStack getIcon() {
        return guiIcon;
    }

    public String getNameFormatted() {
        return nameFormatted;
    }

    protected void setFormattedName(String name) {
        nameFormatted = ChatColor.translateAlternateColorCodes('&', name);
    }

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
     * Checks premium plugin registry and sets up plugin for key registry
     * @param pluginId A valid plugin ID must be provided, and must match that of the plugin registry
     */
    protected boolean PremiumRegister(Integer pluginId) {
        try {
            premiumData = new PremiumLink(this, pluginId);
            return true;
        } catch (InvalidPremiumAuthException e) {
            errors.add(e);
            return false;
        }
    }

    @Override
    public void onEnable() {
        core = (AxiusCore) getServer().getPluginManager().getPlugin("AxiusCore");
        Preregister();
        lastUpdate = System.currentTimeMillis();
        Register();
        Postregister();
    }

    private void Register() {
        core.registerPlugin(this);
    }

    @Override
    public void onDisable() {
        Stop();
        Unregister();
        FullStop();
    }

    private void Unregister() {
        core.unregisterPlugin(this);
    }

    protected boolean registerCommands() {
        for(Command c : commands) {
            if(!commandMap.register(getName(), c)) errors.add(new CommandRegisterException(c));
        }
        return true;
    }

    public PluginStatus pullStatus() {
        return status;
    }
    public void setStatus(PluginStatus newStatus) {status = newStatus;}
}
