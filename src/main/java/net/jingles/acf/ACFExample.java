package net.jingles.acf;

import org.bukkit.plugin.java.JavaPlugin;

public final class ACFExample extends JavaPlugin {

  @Override
  public void onEnable() {

    // The CommandManager will register all of our commands and stuffs.
    new CommandManager(this);

  }

}
