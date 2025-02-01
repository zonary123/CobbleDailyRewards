package com.kingpixel.cobbledailyrewards;

import ca.landonjw.gooeylibs2.api.tasks.Task;
import com.kingpixel.cobbledailyrewards.command.CommandTree;
import com.kingpixel.cobbledailyrewards.config.Config;
import com.kingpixel.cobbledailyrewards.config.Lang;
import com.kingpixel.cobbledailyrewards.config.RewardsConfig;
import com.kingpixel.cobbledailyrewards.database.DatabaseClientFactory;
import com.kingpixel.cobbledailyrewards.managers.DailyRewardsManager;
import com.kingpixel.cobbledailyrewards.models.Rewards;
import com.kingpixel.cobbledailyrewards.models.UserInfo;
import com.kingpixel.cobbledailyrewards.utils.UtilsLogger;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CobbleDailyRewards {
  public static final String MOD_ID = "cobbledailyrewards";
  public static final String MOD_NAME = "CobbleDailyRewards";
  public static final String PATH = "/config/cobbledailyrewards";
  public static final String PATH_LANG = "/config/cobbledailyrewards/lang/";
  public static final String PATH_REWARDS = "/config/cobbledailyrewards/rewards/";
  public static final UtilsLogger LOGGER = new UtilsLogger();
  public static MinecraftServer server;

  // Config and Lang
  public static Config config = new Config();
  public static Lang language = new Lang();
  public static RewardsConfig rewardsConfig = new RewardsConfig();

  // Manager
  public static DailyRewardsManager manager = new DailyRewardsManager();
  public static Task alertreward;

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
    config.init();
    language.init();
    rewardsConfig.init();
  }

  private static void sign() {
    LOGGER.info("§e+-------------------------------+");
    LOGGER.info("§e| §6CobbleDailyRewards");
    LOGGER.info("§e+-------------------------------+");
    LOGGER.info("§e| §6Version: §e" + "1.0.6");
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
      manager.init(player);
      UserInfo userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player);
      boolean update = false;
      for (Rewards reward : rewardsConfig.getRewards()) {
        if (userInfo.getCooldowns().containsKey(reward.getId())) return;
        userInfo.addCooldown(reward, player);
        update = true;
      }
      if (update) DatabaseClientFactory.databaseClient.updateUserInfo(userInfo);
      sendAlert(player);
    });
  }


  private static void tasks() {
    if (alertreward != null) alertreward.setExpired();

    alertreward = Task.builder()
      .execute(() -> {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        for (ServerPlayerEntity player : players) {
          sendAlert(player);
        }
      })
      .interval(20L * 60 * config.getCheckReward())
      .build();
  }

  private static void sendAlert(ServerPlayerEntity player) {
    UserInfo userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player);
    boolean someforclaim = userInfo.getCooldowns().values().stream().anyMatch(date -> date < System.currentTimeMillis());
    if (someforclaim) {
      player.sendMessage(
        AdventureTranslator.toNative(
          language.getMessageCanClaim()
            .replace("%prefix%", language.getPrefix())
        )
      );
    }
  }
}
