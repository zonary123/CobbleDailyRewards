package com.kingpixel.cobbledailyrewards.command;

import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbledailyrewards.command.base.DailyRewardCommand;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

/**
 * @author Carlos Varas Alonso - 10/06/2024 14:08
 */
public class CommandTree {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registry) {
    LiteralArgumentBuilder<ServerCommandSource> base = CommandManager.literal("dailyrewards");

    DailyRewardCommand.register(dispatcher, base);

    dispatcher.register(
      base.then(
        CommandManager.literal("reload")
          .requires(source -> LuckPermsUtil.checkPermission(source, 2, "cobbledailyrewards.reload"))
          .executes(context -> {
            CobbleDailyRewards.load();
            return 1;
          })
      )
    );
  }


}
