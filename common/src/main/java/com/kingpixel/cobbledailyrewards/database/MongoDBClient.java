package com.kingpixel.cobbledailyrewards.database;

import com.kingpixel.cobbledailyrewards.CobbleDailyRewards;
import com.kingpixel.cobbledailyrewards.models.Rewards;
import com.kingpixel.cobbledailyrewards.models.UserInfo;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDBClient implements DatabaseClient {
  private MongoCollection<UserInfo> mongoCollection;
  private MongoClient mongoClient;

  public MongoDBClient(DataBaseConfig database) {
    try {
      // Configurar el CodecRegistry para POJOs
      CodecRegistry pojoCodecRegistry = fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        fromProviders(PojoCodecProvider.builder().automatic(true).build())
      );

      // Configurar el MongoClientSettings con UuidRepresentation.STANDARD
      MongoClientSettings settings = MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(database.getUrl()))
        .uuidRepresentation(UuidRepresentation.STANDARD)
        .codecRegistry(pojoCodecRegistry)
        .build();

      // Crear el MongoClient
      mongoClient = MongoClients.create(settings);

      // Obtener la base de datos y colección
      MongoDatabase mongoDatabase = mongoClient.getDatabase(database.getDatabase());
      mongoCollection = mongoDatabase.getCollection("dailyrewards", UserInfo.class);

      connect();
    } catch (Exception e) {
      e.printStackTrace();
      CobbleDailyRewards.LOGGER.error("Error connecting to MongoDB" + e);
    }
  }

  @Override public void connect() {
    CobbleDailyRewards.LOGGER.info("Connected to MongoDB");
  }

  @Override public UserInfo getUserInfo(ServerPlayerEntity player) {
    try {
      UserInfo userInfo = mongoCollection.find(Filters.eq("uuid", player.getUuid())).first();
      if (userInfo == null) {
        userInfo = new UserInfo(player);
        mongoCollection.insertOne(userInfo);
      }
      return userInfo;
    } catch (Exception e) {
      CobbleDailyRewards.LOGGER.error("Error getting user info from MongoDB" + e);
      return null;
    }
  }

  @Override public boolean isCooldownActive(Rewards rewards, ServerPlayerEntity player) {
    try {
      UserInfo userInfo = getUserInfo(player);
      return PlayerUtils.isCooldown(userInfo.getCooldowns().getOrDefault(rewards.getId(), 1L));
    } catch (Exception e) {
      CobbleDailyRewards.LOGGER.error("Error checking cooldown from MongoDB" + e);
      return false;
    }
  }

  @Override
  public void updateUserInfo(Rewards rewards, ServerPlayerEntity player) {
    try {
      UserInfo userInfo = getUserInfo(player);

      // Actualizar la información de cooldowns
      userInfo.getCooldowns().put(rewards.getId(), new Date().getTime() + TimeUnit.MINUTES.toMillis(rewards.getCooldown()));

      // Crear un documento de actualización
      Document updateDocument = new Document("$set", new Document("cooldowns", userInfo.getCooldowns()));

      // Actualizar el documento en MongoDB
      mongoCollection.updateOne(Filters.eq("uuid", player.getUuid()), updateDocument);
    } catch (Exception e) {
      CobbleDailyRewards.LOGGER.error("Error updating user info in MongoDB" + e);
    }
  }

  @Override public void disconnect() {
    if (mongoClient != null) {
      mongoClient.close();
      CobbleDailyRewards.LOGGER.info("Disconnected from MongoDB");
    }
  }

  @Override public void save() {
    CobbleDailyRewards.LOGGER.info("Saved to MongoDB");
  }

  @Override public void restart(ServerPlayerEntity player) {
    try {
      UserInfo userInfo = getUserInfo(player);
      userInfo.getCooldowns().clear();
      mongoCollection.updateOne(Filters.eq("uuid", player.getUuid()), new Document("$set", new Document("cooldowns", userInfo.getCooldowns())));
    } catch (Exception e) {
      CobbleDailyRewards.LOGGER.error("Error restarting user info in MongoDB" + e);
    }
  }
}