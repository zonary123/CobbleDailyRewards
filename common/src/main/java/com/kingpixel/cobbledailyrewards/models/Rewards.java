package com.kingpixel.cobbledailyrewards.models;

import com.kingpixel.cobbleutils.Model.AdvancedItemChance;
import com.kingpixel.cobbleutils.Model.ItemChance;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Carlos Varas Alonso - 14/08/2024 22:44
 */
@Getter
@Setter
@Data
@ToString
public class Rewards {
  private String id;
  private short slot;
  private Integer cooldown;
  private Map<String, Integer> cooldowns;
  private String permission;
  private ItemModel withoutCooldown;
  private ItemModel withCooldown;
  private AdvancedItemChance rewards;

  public Rewards() {
    this.id = "Default";
    this.slot = 0;
    this.cooldown = 1440;
    this.cooldowns = new HashMap<>();
    cooldowns.put("", cooldown);
    cooldowns.put("group.vip", 720);
    this.permission = "";
    this.withoutCooldown = new ItemModel("minecraft:chest_minecart", "<green>Default Reward", List.of(
      "Cooldown: <red>%cooldown%",
      "Permission: <red>%permission%"));
    this.withCooldown = new ItemModel("minecraft:minecart", "<green>Default Reward", List.of(
      "Cooldown: <red>%cooldown%",
      "Permission: <red>%permission%"));
    this.rewards = new AdvancedItemChance();

  }

  public void check() {
    if (this.cooldown == null) {
      this.cooldown = 1440;
    }
    if (this.cooldowns == null) {
      this.cooldowns = new HashMap<>();
      cooldowns.put("", cooldown);
    }
    if (this.permission == null) {
      this.permission = "";
    }
    if (this.withoutCooldown == null) {
      this.withoutCooldown = new ItemModel("minecraft:chest_minecart", "<green>Default Reward", List.of(
        "Cooldown: <red>%cooldown%",
        "Permission: <red>%permission%"));
    }
    if (this.withCooldown == null) {
      this.withCooldown = new ItemModel("minecraft:minecart", "<green>Default Reward", List.of(
        "Cooldown: <red>%cooldown%",
        "Permission: <red>%permission%"));
    }
  }

  public long getCalculteCooldown(ServerPlayerEntity player) {
    return PlayerUtils.getCooldown(cooldowns, cooldown, player);
  }
}
