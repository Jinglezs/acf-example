package net.jingles.acf;

import co.aikar.commands.CommandHelp;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/*
 * When you use @CommandAlias on the class declaration, it sets the command's
 * default name. Sub-commands start with this command by default, but can also
 * be given their own aliases.
*/
@CommandAlias("acf-config")
@CommandPermission("some.permission.goes.here") // All sub-commands inherit this permission
public class ConfigCommand {

  // Dependencies must be marked with the @Dependency annotation. They may be private, but not final.
  // When this command is registered in the CommandManager, ACF will assign values to these fields,
  // so as long as you didn't register a null dependency, these cannot be null.

  @Dependency private ACFExample plugin;
  @Dependency private FileConfiguration config;

  @Default // This annotation means that this method is executed when no arguments are provided by the CommandSender.
  @HelpCommand // This command displays help when executed
  public void onNoArgCommand(CommandHelp help) {
    help.showHelp(); // This displays a help message to whoever issued the command.
  }

  /**
   * A simple config reload command that uses the dependencies we registered.
   *
   * @param sender The person sending the command. Does not have to be a player if you want to
   * allow the command to be sent from the server console. Otherwise, use a Player or some other
   * type that you have registered as a CommandContext
   */

  @Subcommand("reload") // To use this command, you can do /acf-config reload
  @CommandAlias("config-reload") // OR you can do /config-reload
  @Description("Reloads the configuration, which allows any changes done to the file to take effect.")
  public void onConfigReload(CommandSender sender) {

    // The first argument is usually whoever is sending the command, which is a CommandSender in this case,
    // allowing someone to use this command from the console.

    plugin.reloadConfig();
    sender.sendMessage("Successfully reloaded the default configuration file.");

  }

  /**
   * This command allows a player to change a value in the configuration through a command.
   * @param player the player sending the command. They must fulfill the "op" condition defined in the CommandManager
   * @param path the path of the value to edit
   * @param input the new config value
   */
  @Subcommand("edit-value")
  @CommandCompletion("@config-values") // This skips the first parameter, meaning this completion is applicable to ConfigValueType
  @Description("Changes a value in the configuration.")
  @Syntax("<the type of value you are changed to> <the path of the value> <the value, or nothing if the value type is DELETE>")
  public void onEditConfigString(@Conditions("op") Player player, ConfigValueType type, String path, @Optional String input) {

    // Since we registered ConfigValueType as a CommandContext, we can have it passed as a parameter because ACF
    // will automatically convert the command argument into a ConfigValueType :D

    // None of this will be executed if the player does not meet the "op" condition :D
    // Optional inputs do not have to be included when the player executes the command!

    if (input == null && type != ConfigValueType.DELETE)
      throw new InvalidCommandArgument("Must provide an input value if you are not deleting the current config value!");

    // Input may be null because it is marked as
    Object newValue = type.convert(input);
    config.set(path, type.convert(input));

    player.sendMessage("Successfully set the configuration value to " + newValue);

  }

}
