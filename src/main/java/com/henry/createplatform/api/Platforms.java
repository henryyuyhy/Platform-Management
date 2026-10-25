package com.henry.createplatform.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * 站台号 / 站台限制的解析与匹配规则（服务端与客户端共用的纯逻辑，不放任何游戏状态）。
 *
 * <ul>
 *   <li>车站侧：单个纯正整数，空 = 未设置。</li>
 *   <li>时刻表侧：留空 = 全部站台；否则是逗号分隔的正整数列表（如 {@code 1,2}）。</li>
 *   <li>车站未设置站台号时，任何非空限制都不会匹配它——宁可拦下，也不让列车停到没标站台的轨道上。</li>
 * </ul>
 */
public final class Platforms {

    private Platforms() {}

    public static StationPlatform of(Object station) {
        return (StationPlatform) station;
    }

    /** 车站侧归一化：只保留数字，去掉前导零，最长 9 位；非法输入得到空串（未设置）。 */
    public static String normalizeStation(String raw) {
        if (raw == null)
            return "";
        String digits = raw.trim()
            .replaceAll("[^0-9]", "");
        if (digits.isEmpty())
            return "";
        if (digits.length() > 9)
            digits = digits.substring(0, 9);
        return Integer.toString(Integer.parseInt(digits));
    }

    /** 时刻表侧解析：返回限制的站台号集合；空列表 = 不限制。 */
    public static List<Integer> parseFilter(String raw) {
        if (raw == null || raw.isBlank())
            return List.of();
        List<Integer> platforms = new ArrayList<>(2);
        for (String part : raw.split(",")) {
            String digits = part.trim()
                .replaceAll("[^0-9]", "");
            if (digits.isEmpty())
                continue;
            int value;
            try {
                value = Integer.parseInt(digits);
            } catch (NumberFormatException overflow) {
                continue;
            }
            if (!platforms.contains(value))
                platforms.add(value);
        }
        return platforms;
    }

    /** 空限制匹配一切；非空限制只匹配明确标了其中某个站台号的车站。 */
    public static boolean matchesAny(List<Integer> filter, String stationPlatform) {
        if (filter.isEmpty())
            return true;
        String normalized = normalizeStation(stationPlatform);
        if (normalized.isEmpty())
            return false;
        return filter.contains(Integer.parseInt(normalized));
    }

    /** 输入框过滤器：只允许数字与逗号。 */
    public static boolean isPlatformFilterInput(String text) {
        if (text == null || text.isEmpty())
            return true;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != ',' && (c < '0' || c > '9'))
                return false;
        }
        return true;
    }

    /** 输入框过滤器：车站侧只允许数字。 */
    public static boolean isStationPlatformInput(String text) {
        if (text == null || text.isEmpty())
            return true;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9')
                return false;
        }
        return true;
    }

    /** 把限制串渲染成可读文本（用于界面与列车状态提示）。 */
    public static String describe(String raw) {
        List<Integer> platforms = parseFilter(raw);
        if (platforms.isEmpty())
            return "-";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < platforms.size(); i++) {
            if (i > 0)
                sb.append(',');
            sb.append(platforms.get(i));
        }
        return sb.toString();
    }

    /** 列车找不到「站名 + 站台」时写进状态栏的筛选描述，让玩家看得懂为什么不停车。 */
    public static String describeLabel(String stationFilter, List<Integer> platformFilter) {
        StringBuilder sb = new StringBuilder(stationFilter);
        for (int i = 0; i < platformFilter.size(); i++) {
            if (i == 0)
                sb.append(" [");
            else
                sb.append(',');
            sb.append(platformFilter.get(i));
            if (i == platformFilter.size() - 1)
                sb.append(']');
        }
        return sb.toString();
    }

    public static MutableComponent platformTooltip(String raw) {
        return Component.translatable("createplatform.schedule.platform_tooltip", describe(raw));
    }
}
