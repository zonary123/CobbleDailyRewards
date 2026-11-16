package com.kingpixel.cobbledailyrewards.api.action;

/**
 * Result model representing the outcome of a daily reward action (e.g. claim, reset).
 */
public class DailyRewardActionResult {

  public enum Status {
    SUCCESS,
    REWARD_NOT_FOUND,
    ON_COOLDOWN,
    PERMISSION_DENIED,
    DISABLED,
    ERROR
  }

  private final Status status;
  private final String message;
  private final String rewardId;
  private final long cooldownRemainingMillis;
  private final int claimedCount;

  public DailyRewardActionResult(Status status, String message, String rewardId, long cooldownRemainingMillis, int claimedCount) {
    this.status = status;
    this.message = message != null ? message : "";
    this.rewardId = rewardId != null ? rewardId : "";
    this.cooldownRemainingMillis = Math.max(0L, cooldownRemainingMillis);
    this.claimedCount = claimedCount;
  }

  public static DailyRewardActionResult success(String message) {
    return new DailyRewardActionResult(Status.SUCCESS, message, "", 0L, 1);
  }

  public static DailyRewardActionResult successClaim(String message, String rewardId) {
    return new DailyRewardActionResult(Status.SUCCESS, message, rewardId, 0L, 1);
  }

  public static DailyRewardActionResult successBatch(String message, int claimedCount) {
    return new DailyRewardActionResult(Status.SUCCESS, message, "", 0L, claimedCount);
  }

  public static DailyRewardActionResult cooldown(String message, String rewardId, long cooldownRemainingMillis) {
    return new DailyRewardActionResult(Status.ON_COOLDOWN, message, rewardId, cooldownRemainingMillis, 0);
  }

  public static DailyRewardActionResult failure(Status status, String message) {
    return new DailyRewardActionResult(status, message, "", 0L, 0);
  }

  public static DailyRewardActionResult failure(Status status, String message, String rewardId) {
    return new DailyRewardActionResult(status, message, rewardId, 0L, 0);
  }

  public boolean isSuccess() {
    return status == Status.SUCCESS;
  }

  public Status getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public String getRewardId() {
    return rewardId;
  }

  public long getCooldownRemainingMillis() {
    return cooldownRemainingMillis;
  }

  public int getClaimedCount() {
    return claimedCount;
  }

  @Override
  public String toString() {
    return "DailyRewardActionResult{" +
      "status=" + status +
      ", message='" + message + '\'' +
      ", rewardId='" + rewardId + '\'' +
      ", cooldownRemainingMillis=" + cooldownRemainingMillis +
      ", claimedCount=" + claimedCount +
      '}';
  }
}
