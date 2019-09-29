package net.jingles.acf;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandManager extends BukkitCommandManager {

  /**
   * The BukkitCommandManager has a getPlugin() method, but that
   * returns a Plugin object rather than your implementation.
   */
  private final ACFExample plugin;

  public CommandManager(ACFExample plugin) {
    super(plugin);
    this.plugin = plugin;

    // For organization purposes, I separate all of these into
    // their own methods.
    registerDependencies();
    registerCompletions();
    registerConditions();
    registerContexts();

    /* This method is deprecated because it is considered "unstable," though
       I've never had any problems with it. The "help" API generates a help
       description for you, including syntax and descriptions, as long as your
       command has the correct annotations for it. */
    enableUnstableAPI("help");

  }

  /*
    You'll want to register dependencies that can be used by other Command classes
    later on right here. These cannot be used by non-command classes because it only
    works on commands that you register through ACF.

    Note that dependencies can be of any type.
  */
  private void registerDependencies() {

    // Register our main class as a dependency.
    registerDependency(ACFExample.class, plugin);

    // Here I add the plugin's config file as a dependency.
    registerDependency(FileConfiguration.class, plugin.getConfig());

    /*
      If you register two dependencies of the same type, you must give them keys.
      When retrieving them in a command class, you would do @Dependency("key")
      instead of just @Dependency
     */
    registerDependency(String.class, "example_one", "Hi");
    registerDependency(String.class, "example_two", "Bye");
  }

  /**
   * Command Contexts automatically convert player arguments into objects of any type,
   * as long as you register a context for it.
   * <p>
   * ACF automatically handles all primitive types and their Class representations.
   * Example -> int, Integer, and BigInteger are all supported.
   * <p>
   * It also handles these non-primitive types:
   * - String and String[]
   * - Enum
   * - CommandHelp (an ACF class)
   * - BigDecimal and BigInteger
   * - Number
   */
  private void registerContexts() {

    // A simple example is a getting a ConfigurationSection from our config file with
    // the string a player provides through command arguments.
    getCommandContexts().registerContext(ConfigurationSection.class, handler -> {

      // popFirstArg simply gets the next argument that YOU HAVE NOT YET HANDLED.
      // After calling popFirstArg, calling it again will get the NEXT ARGUMENT.
      // "pop" signifies that it is being REMOVED from the argument list.

      String commandArgument = handler.popFirstArg();
      return plugin.getConfig().getConfigurationSection(commandArgument);

      // This could easily be reduced to one line, but yenno...

    });

    /*
     * If you want a command to fail for any reason DUE TO INVALID ARGUMENTS,
     * throw an InvalidCommandArgument exception with the reason as the argument.
     * This sends the CommandSender an error message. IT DOES NOT PRINT A STACKTRACE
     * SO DO NOT WORRY BOI
     */
    getCommandContexts().registerIssuerOnlyContext(String.class, handler -> {

      String playerName = handler.getPlayer().getName();

      if (playerName.equals("Kuwa_"))
        throw new InvalidCommandArgument("Hoes can't use this command. Smh my head...");

      return playerName;

    });

    /*
     * An IssuerAwareContext may or may not consume command arguments. You can check
     * if you want to use the argument first, and then use the Issuer if you must.
     *
     * This isn't very useful with only Spigot installed, but it might be useful for
     * plugins like WorldGuard or something...
     *
     * However, ACF *does* allow command arguments to be optional with the @Optional annotation,
     * therefore there may be times where the player *does not* have to include a parameter.
     * In those cases, you may want to check if the player provided an argument. If they did
     * not, just use the Issuer instead.
     *
     * An example of this is getting a World.
     *
     * NOTE: If you do decide to consume the input, POP THE ARGUMENT
     */
    getCommandContexts().registerIssuerAwareContext(World.class, handler -> {

      if (handler.getFirstArg() != null) {

        // ITS VERY IMPORTANT THAT YOU POP THE ARG, ESPECIALLY
        // WHEN YOU HAVE MULTIPLE PARAMETERS.
        String worldName = handler.popFirstArg();
        return Bukkit.getWorld(worldName);

      } else return handler.getPlayer().getWorld();

    });

    /* An IssuerOnlyContext does not use command arguments to return an object.
       It is based solely on whoever sent the command. A simple example is getting
       a player's UUID. */
    getCommandContexts().registerIssuerOnlyContext(UUID.class, handler -> handler.getPlayer().getUniqueId());

    // This uses ACF's utility class to get an Enum value from a String.
    // See the example command class to see how this werks.
    getCommandContexts().registerContext(ConfigValueType.class, handler ->
        ACFUtil.getEnumFromName(ConfigValueType.values(), handler.popFirstArg()));

  }

  /**
   * Completions allow players to tab-complete command arguments. The completions that
   * you register here can be used by all of your command classes.
   */
  private void registerCompletions() {

    // Going off of the World content example above, here is a completion that
    // gets the names of all the Worlds.
    getCommandCompletions().registerCompletion("worlds", handler -> {

      // The final result of any completion should be a List/Set of Strings.

      return Bukkit.getWorlds().stream() // Create a Stream of World objects
          .map(World::getName)           // Convert each World into a String by getting its name
          .collect(Collectors.toList()); // Put all of the names into a List and return it

    });

    /*
     * Static completions are those that never change. These should NOT be based on
     * anything that may change in the future, because then your completion may be
     * missing possible options, or have options that do not exist anymore.
     */
    getCommandCompletions().registerStaticCompletion("weekdays",
        Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"));

    getCommandCompletions().registerStaticCompletion("config-values",
        Stream.of(ConfigValueType.values()) // Create Stream from the enum's value array
            .map(ConfigValueType::name)     // Convert them to Strings based on their name
            .collect(Collectors.toList())); // Put all of the names into a list and return it

  }

  /**
   * Conditions are certain things that must be true in order for someone to
   * use a command. This is not the same thing as PERMISSIONS, cause ACF handles
   * that separately.
   * <p>
   * If they do not meet the condition, you must throw an exception called
   * ConditionFailedException, with the reason as its only argument. This
   * sends the CommandSender an error message. NO STACKTRACE IS PRINTED.
   *
   * Conditions can either be used on methods or on parameters, depending
   * on how you register them.
   *
   */
  private void registerConditions() {

    /*
     * An example is checking to see if a player is below half health
     * before they can use a command.
     */
    getCommandConditions().addCondition("belowHalfHealth", handler -> {

      if (!handler.getIssuer().isPlayer())
        throw new ConditionFailedException("Only players can use this command!");

      Player player = handler.getIssuer().getPlayer();
      AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
      double maxHealth = attribute != null ? attribute.getValue() : 20;

      if (player.getHealth() > (maxHealth / 2))
        throw new ConditionFailedException("You must be below half health to use this command!");

      // You do not return anything for command conditions.

    });



    /*
     * Okay this looks a little confusing I'm sure, but this method is used to register PARAMETER CONDITIONS.
     * This means that the condition does not have to apply to whoever sent the Command, but it DOES apply
     * to whatever the parameter is. Now let's break this down...
     *
     * The first parameter is Player.class. This is the class type that the parameter is. This means the condition
     * applies to a PLAYER.
     *
     * The second parameter is just the name of this condition. So when writing your command, you would
     * put @Conditions("op") next to the PARAMETER, not above the method. This will make more sense when
     * you look at a Command class implementation.
     *
     * The final parameter is what I would call a TriPredicate, meaning it takes in three values and returns
     * a boolean. The three values this time are:
     *
     *  1. The CommandIssuer (whoever is sending the command)
     *  2. The CommandContext (arguments and all that stuff)
     *  3. The value of the parameter, in this case a PLAYER
     *
     * With all of these things at your disposal, you could probably make some super complex condition, but
     * we're just checking if the player passed as a parameter is OP'd >_>
     */
    getCommandConditions().addCondition(Player.class, "op", (commandIssuer, executionContext, player) -> player.isOp());

  }

}
