package com.kingpixel.cobbledailyrewards.command.base;

import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbledailyrewards.database.DatabaseClientFactory;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Carlos Varas Alonso - 02/08/2024 12:23
 */
public class DailyRewardRestartCommand implements Command<ServerCommandSource> {
  private static Map<UUID, Long> cooldowns = new HashMap<>();

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base
        .then(
          CommandManager.literal("restart")
            .requires(source -> LuckPermsUtil.checkPermission(source, 2, List.of("cobbledailyrewards.restart", "cobbledailyrewards.admin")))
            .executes(context -> {
              if (!CobbleDailyRewards.config.isActive()) return 0;
              ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
              DatabaseClientFactory.databaseClient.restart(player);
              return 1;
            }).then(
              CommandManager.argument("player", EntityArgumentType.players())
                .executes(context -> {
                  if (!CobbleDailyRewards.config.isActive()) return 0;
                  ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                  DatabaseClientFactory.databaseClient.restart(player);
                  return 1;
                })
            )
        )
    );

  }


  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    return 0;
  }

}
