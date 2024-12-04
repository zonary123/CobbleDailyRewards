package com.kingpixel.cobbledailyrewards.models;

import com.kingpixel.cobbleutils.Model.AdvancedItemChance;
import com.kingpixel.cobbleutils.Model.ItemChance;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

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
  private long cooldown;
  private String permission;
  private ItemModel withoutCooldown;
  private ItemModel withCooldown;
  private AdvancedItemChance rewards;

  public Rewards() {
    this.id = "Default";
    this.slot = 0;
    this.cooldown = 1440;
    this.permission = "";
    this.withoutCooldown = new ItemModel("minecraft:chest_minecart");
    this.withCooldown = new ItemModel("minecraft:minecart");
    this.rewards = new AdvancedItemChance();

  }
}
