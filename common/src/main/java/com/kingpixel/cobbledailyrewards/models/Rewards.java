package com.kingpixel.cobbledailyrewards.models;

import com.kingpixel.cobbleutils.Model.ItemChance;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
  private int cooldown;
  private String permission;
  private ItemModel withoutCooldown;
  private ItemModel withCooldown;
  private List<PermissionRewards> permissionRewards;

  @Getter
  @Setter
  @Data
  @ToString
  private static class PermissionRewards {
    private String permission;
    private List<ItemChance> itemChances;

    public PermissionRewards() {
      this.permission = "";
      this.itemChances = new ArrayList<>();
      this.itemChances.addAll(ItemChance.defaultItemChances());
    }

    public PermissionRewards(String permission, List<ItemChance> itemChances) {
      this.permission = permission;
      this.itemChances = itemChances;
    }
  }


  public Rewards() {
    this.id = "Default";
    this.slot = 0;
    this.cooldown = 1440;
    this.permission = "";
    this.withoutCooldown = new ItemModel("minecraft:chest_minecart");
    this.withCooldown = new ItemModel("minecraft:minecart");
    this.permissionRewards = new ArrayList<>();
    permissionRewards.add(new PermissionRewards());
  }


  public void giveReward(ServerPlayerEntity player) {
    if (permission.isEmpty() || LuckPermsUtil.checkPermission(player, permission)) {
      permissionRewards.forEach(reward -> {
        if (reward.getPermission().isEmpty() || LuckPermsUtil.checkPermission(player, reward.getPermission())) {
          ItemChance.getAllRewards(reward.getItemChances(), player);
        }
      });
    }

  }

}
