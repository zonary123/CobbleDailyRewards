package com.kingpixel.cobbledailyrewards;

import ca.landonjw.gooeylibs2.api.tasks.Task;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kingpixel.cobbledailyrewards.command.CommandTree;
import com.kingpixel.cobbledailyrewards.config.Config;
import com.kingpixel.cobbledailyrewards.config.Lang;
import com.kingpixel.cobbledailyrewards.config.RewardsConfig;
import com.kingpixel.cobbledailyrewards.database.DatabaseClientFactory;
import com.kingpixel.cobbledailyrewards.models.Rewards;
import com.kingpixel.cobbledailyrewards.models.UserInfo;
import com.kingpixel.cobbledailyrewards.utils.UtilsLogger;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CobbleDailyRewards implements ModInitializer {
  public static final String MOD_ID = "cobbledailyrewards";
  public static final String MOD_NAME = "CobbleDailyRewards";
  public static final String PATH = "/config/cobbledailyrewards";
  public static final String PATH_LANG = "/config/cobbledailyrewards/lang/";
  public static final String PATH_REWARDS = "/config/cobbledailyrewards/rewards/";
  public static final String PATH_DATA = CobbleDailyRewards.PATH + "/data/";
  public static final UtilsLogger LOGGER = new UtilsLogger();
  public static MinecraftServer server;
  // Config and Lang
  public static Config config = new Config();
  public static Lang language = new Lang();
  public static RewardsConfig rewardsConfig = new RewardsConfig();
  public static Map<UUID, UserInfo> userInfoMap = new HashMap<>();
  public static ExecutorService EXECUTOR_DAILY_REWARDS = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder()
    .setDaemon(true)
    .setNameFormat("CobbleDailyRewards-Executor-%d")
    .build());
  // Manager
  public static Task taskAlert;

  @Override public void onInitialize() {
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
    config.init();
    language.init();
    rewardsConfig.init();
  }

  private static void sign() {
    LOGGER.info("§e+-------------------------------+");
    LOGGER.info("§e| §6CobbleDailyRewards");
    LOGGER.info("§e+-------------------------------+");
    LOGGER.info("§e| §6Version: §e" + "1.1.1");
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


    LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> server = level.getServer());

    PlayerEvent.PLAYER_JOIN.register(player -> {
      CompletableFuture.runAsync(() -> {
        UserInfo userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player);
        boolean update = false;
        for (Rewards reward : rewardsConfig.getRewards()) {
          if (userInfo.getCooldowns().containsKey(reward.getId())) return;
          userInfo.addCooldown(reward, player);
          update = true;
        }
        if (update) DatabaseClientFactory.databaseClient.updateUserInfo(userInfo);
        sendAlert(player);
      }, EXECUTOR_DAILY_REWARDS);
    });

    PlayerEvent.PLAYER_QUIT.register(player -> userInfoMap.remove(player.getUuid()));
  }


  private static void tasks() {
    if (taskAlert != null) taskAlert.setExpired();

    taskAlert = Task.builder()
      .execute(() -> {
        CompletableFuture.runAsync(() -> {
          List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
          for (ServerPlayerEntity player : players) {
            sendAlert(player);
          }
        }, EXECUTOR_DAILY_REWARDS);
      })
      .interval(20L * 60 * config.getCheckReward())
      .infinite()
      .build();
  }

  private static void sendAlert(ServerPlayerEntity player) {
    UserInfo userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player);
    boolean someForClaim =
      userInfo.getCooldowns().values().stream().anyMatch(date -> date < System.currentTimeMillis());
    if (someForClaim) {
      player.sendMessage(
        AdventureTranslator.toNative(
          language.getMessageCanClaim()
            .replace("%prefix%", language.getPrefix())
        )
      );
    }
  }

}
