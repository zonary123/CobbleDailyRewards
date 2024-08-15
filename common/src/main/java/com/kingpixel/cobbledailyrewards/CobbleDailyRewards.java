package com.kingpixel.cobbledailyrewards;

import com.kingpixel.cobbledailyrewards.command.CommandTree;
import com.kingpixel.cobbledailyrewards.config.Config;
import com.kingpixel.cobbledailyrewards.config.Lang;
import com.kingpixel.cobbledailyrewards.database.DatabaseClientFactory;
import com.kingpixel.cobbledailyrewards.managers.DailyRewardsManager;
import com.kingpixel.cobbledailyrewards.utils.UtilsLogger;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.concurrent.*;

public class CobbleDailyRewards {
  public static final String MOD_ID = "cobbledailyrewards";
  public static final String MOD_NAME = "CobbleDailyRewards";
  public static final String PATH = "/config/cobbledailyrewards";
  public static final String PATH_LANG = "/config/cobbledailyrewards/lang/";
  public static final UtilsLogger LOGGER = new UtilsLogger();
  public static MinecraftServer server;

  // Config and Lang
  public static Config config = new Config();
  public static Lang language = new Lang();

  // Manager
  public static DailyRewardsManager manager = new DailyRewardsManager();

  // Tasks
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private static final List<ScheduledFuture<?>> scheduledTasks = new CopyOnWriteArrayList<>();

  public static void init() {
    events();
  }

  public static void load() {
    files();
    sign();
    tasks();
    DatabaseClientFactory.createDatabaseClient(
      config.getDatabase()
    );
  }


  private static void files() {
    language.init();
    config.init();
  }

  private static void sign() {
    LOGGER.info("§e+-------------------------------+");
    LOGGER.info("§e| §6CobbleDailyRewards");
    LOGGER.info("§e+-------------------------------+");
    LOGGER.info("§e| §6Version: §e" + "1.0.0");
    LOGGER.info("§e| §6Author: §eZonary123");
    LOGGER.info("§e| §6Website: §9https://github.com/Zonary123/CobbleDailyRewards");
    LOGGER.info("§e| §6Discord: §9https://discord.com/invite/fKNc7FnXpa");
    LOGGER.info("§e| §6Support: §9https://github.com/Zonary123/CobbleDailyRewards/issues");
    LOGGER.info("§e| &dDonate: §9https://ko-fi.com/zonary123");
    LOGGER.info("§e+-------------------------------+");
  }

  private static void events() {
    files();

    CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
      CommandTree.register(dispatcher, registry);
    });

    LifecycleEvent.SERVER_STARTED.register(server -> {
      load();
    });

    LifecycleEvent.SERVER_STOPPING.register(server -> {
      scheduledTasks.forEach(task -> task.cancel(true));
      scheduledTasks.clear();
      LOGGER.info("CobbleDailyRewards has been stopped.");
    });

    LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> server = level.getServer());

    PlayerEvent.PLAYER_JOIN.register(player -> {
      manager.init(player);
    });
  }


  private static void tasks() {
    for (ScheduledFuture<?> task : scheduledTasks) {
      task.cancel(false);
    }
    scheduledTasks.clear();

    ScheduledFuture<?> alertreward =
      scheduler.scheduleAtFixedRate(() -> server.getPlayerManager().getPlayerList().forEach(player -> {

      }), 0, CobbleDailyRewards.config.getCheckReward(), TimeUnit.MINUTES);


    scheduledTasks.add(alertreward);
  }

}
