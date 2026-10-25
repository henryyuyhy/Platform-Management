package com.henry.createplatform.api;

/**
 * 「站台号」挂载接口：由 mixin 织入 {@code GlobalStation}，让每个列车站带一个站台号。
 *
 * <p>约定：站台号是「纯正整数」的十进制字符串（如 {@code "3"}），空串表示未设置。
 * 允许不同车站使用相同站台号（正/反发、同站台双向停靠都会用到）。</p>
 */
public interface StationPlatform {

    /** 站台号；未设置时返回空串，永不为 null。 */
    String createplatform$getPlatform();

    /** 设置站台号；传入 null 等价于清空。 */
    void createplatform$setPlatform(String platform);
}
