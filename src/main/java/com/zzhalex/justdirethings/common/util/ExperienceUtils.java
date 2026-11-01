package com.zzhalex.justdirethings.common.util;

import net.minecraft.entity.player.EntityPlayer;

public final class ExperienceUtils {

    private ExperienceUtils() {
    }

    public static int getTotalExperienceForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level <= 16) {
            return level * level + 6 * level;
        }
        if (level <= 31) {
            return (int) (2.5D * level * level - 40.5D * level + 360.0D);
        }
        long totalXp = (long) (4.5D * level * level - 162.5D * level + 2220.0D);
        return (int) Math.min(totalXp, Integer.MAX_VALUE);
    }

    public static int getLevelFromTotalExperience(int totalExperience) {
        totalExperience = Math.max(0, totalExperience);
        if (totalExperience < getTotalExperienceForLevel(16)) {
            return (int) Math.floor((-6.0D + Math.sqrt(36.0D + 4.0D * totalExperience)) / 2.0D);
        }
        if (totalExperience < getTotalExperienceForLevel(31)) {
            return (int) Math.floor((40.5D + Math.sqrt(40.5D * 40.5D - 10.0D * (360.0D - totalExperience))) / 5.0D);
        }
        return (int) Math.floor((162.5D + Math.sqrt(162.5D * 162.5D - 18.0D * (2220.0D - totalExperience))) / 9.0D);
    }

    public static int getExperienceForNextLevel(int level) {
        level = Math.max(0, level);
        if (level >= 30) {
            return (int) Math.min(Integer.MAX_VALUE, 112L + (level - 30L) * 9L);
        }
        return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
    }

    public static int getExpNeededForNextLevel(EntityPlayer player) {
        int total = getPlayerTotalExperience(player);
        int level = getLevelFromTotalExperience(total);
        return getTotalExperienceForLevel(level + 1) - total;
    }

    public static int getPlayerTotalExperience(EntityPlayer player) {
        int base = getTotalExperienceForLevel(player.experienceLevel);
        float progress = Float.isFinite(player.experience) ? Math.max(0.0F, Math.min(1.0F, player.experience)) : 0.0F;
        int partial = Math.round(progress * getExperienceForNextLevel(player.experienceLevel));
        return (int) Math.min(Integer.MAX_VALUE, (long) base + partial);
    }

    public static void setPlayerTotalExperience(EntityPlayer player, int totalExperience) {
        int total = Math.max(0, totalExperience);
        int level = getLevelFromTotalExperience(total);
        player.experienceLevel = level;
        player.experience = getProgressToNextLevel(total);
        player.experienceTotal = total;
    }

    public static int addPoints(EntityPlayer player, int pointsToAdd) {
        int current = getPlayerTotalExperience(player);
        int added = Math.min(Integer.MAX_VALUE - current, Math.max(0, pointsToAdd));
        setPlayerTotalExperience(player, current + added);
        return added;
    }

    public static int removeLevels(EntityPlayer player, int levelsToRemove) {
        if (levelsToRemove <= 0) {
            return 0;
        }
        int currentTotalExp = getPlayerTotalExperience(player);
        int targetLevel = Math.max(0, player.experienceLevel - Math.max(0, levelsToRemove));
        int targetTotalExp = getTotalExperienceForLevel(targetLevel);
        int expToRemove = Math.max(0, currentTotalExp - targetTotalExp);
        setPlayerTotalExperience(player, currentTotalExp - expToRemove);
        return expToRemove;
    }

    public static float getProgressToNextLevel(int totalExperience) {
        int level = getLevelFromTotalExperience(totalExperience);
        int levelStart = getTotalExperienceForLevel(level);
        int next = Math.max(1, getExperienceForNextLevel(level));
        return Math.max(0.0F, Math.min(1.0F, (totalExperience - levelStart) / (float) next));
    }

    public static int removePoints(EntityPlayer player, int pointsToRemove) {
        int current = getPlayerTotalExperience(player);
        int removed = Math.min(current, Math.max(0, pointsToRemove));
        setPlayerTotalExperience(player, current - removed);
        return removed;
    }

    public static int pointsForLevels(EntityPlayer player, int levels) {
        if (levels <= 0) {
            return 0;
        }
        int targetLevel = Math.max(0, player.experienceLevel - levels);
        return Math.max(0, getPlayerTotalExperience(player) - getTotalExperienceForLevel(targetLevel));
    }
}
