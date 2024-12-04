package com.kingpixel.cobbledailyrewards.models;

import com.google.gson.Gson;
import com.kingpixel.cobbledailyrewards.managers.DailyRewardsManager;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Carlos Varas Alonso - 15/08/2024 17:03
 */
@Getter
@Setter
@ToString
public class UserInfo implements Serializable {
  private UUID uuid;
  private String name;
  // ID, Cooldown
  private Map<String, Long> cooldowns;

  public UserInfo() {
    this.uuid = UUID.randomUUID();
    this.name = "";
    this.cooldowns = new HashMap<>();
  }

  public UserInfo(ServerPlayerEntity player) {
    this.uuid = player.getUuid();
    this.name = player.getGameProfile().getName();
    this.cooldowns = new HashMap<>();
  }

  public UserInfo(UUID uuid, String name, Map<String, Long> cooldowns) {
    this.uuid = uuid;
    this.name = name;
    this.cooldowns = cooldowns;
  }

  public void writeInfo(UUID uuid) {
    File dir = Utils.getAbsolutePath(DailyRewardsManager.PATH_USER_INFO);
    if (!dir.exists()) {
      dir.mkdirs(); // Crea el directorio si no existe
    }

    File file = new File(dir, uuid.toString() + ".json");
    try (FileWriter writer = new FileWriter(file)) {
      Gson gson = new Gson();
      gson.toJson(this, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
