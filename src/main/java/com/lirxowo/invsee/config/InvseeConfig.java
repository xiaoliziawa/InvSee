package com.lirxowo.invsee.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * InvSee 模组配置类
 * 使用 NeoForge 的配置系统，支持游戏内修改
 */
@EventBusSubscriber(modid = "invsee")
public class InvseeConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // =============== 标记实体配置 ===============

    /**
     * 标记实体显示持续时间（秒）
     * 默认: 5秒
     * 范围: 1-60秒
     */
    private static final ModConfigSpec.IntValue MARK_DURATION_SECONDS = BUILDER
            .comment("标记物品在世界中显示的持续时间（秒）",
                     "Duration that marked items are displayed in the world (seconds)")
            .defineInRange("mark_duration_seconds", 5, 1, 60);

    /**
     * 标记实体可见距离（方块）
     * 默认: 64方块
     * 范围: 16-256方块
     */
    private static final ModConfigSpec.IntValue MARK_DISPLAY_RANGE = BUILDER
            .comment("标记物品可见的最大距离（方块）",
                     "Maximum distance to see marked items (blocks)")
            .defineInRange("mark_display_range", 64, 16, 256);

    // =============== 槽位高亮配置 ===============

    /**
     * 槽位高亮持续时间（秒）
     * 默认: 5秒
     * 范围: 1-60秒
     */
    private static final ModConfigSpec.IntValue HIGHLIGHT_DURATION_SECONDS = BUILDER
            .comment("容器槽位高亮显示的持续时间（秒）",
                     "Duration that container slots remain highlighted (seconds)")
            .defineInRange("highlight_duration_seconds", 5, 1, 60);

    // 构建配置规范
    public static final ModConfigSpec SPEC = BUILDER.build();

    // =============== 运行时缓存值 ===============
    // 这些值在配置加载/重载时更新，避免每次访问都从配置读取

    private static int markDurationTicks = 100;      // 默认 5秒 = 100 ticks
    private static int markDisplayRange = 64;        // 默认 64 方块
    private static int highlightDurationTicks = 100; // 默认 5秒 = 100 ticks

    /**
     * 获取标记实体持续时间（ticks）
     * @return 持续时间（1秒=20ticks）
     */
    public static int getMarkDurationTicks() {
        return markDurationTicks;
    }

    /**
     * 获取标记实体持续时间（秒）
     * @return 持续时间（秒）
     */
    public static int getMarkDurationSeconds() {
        return markDurationTicks / 20;
    }

    /**
     * 获取标记可见距离（方块）
     * @return 可见距离
     */
    public static int getMarkDisplayRange() {
        return markDisplayRange;
    }

    /**
     * 获取标记可见距离的平方（用于距离比较优化）
     * @return 可见距离的平方
     */
    public static double getMarkDisplayRangeSq() {
        return (double) markDisplayRange * markDisplayRange;
    }

    /**
     * 获取槽位高亮持续时间（ticks）
     * @return 持续时间（1秒=20ticks）
     */
    public static int getHighlightDurationTicks() {
        return highlightDurationTicks;
    }

    /**
     * 获取槽位高亮持续时间（秒）
     * @return 持续时间（秒）
     */
    public static int getHighlightDurationSeconds() {
        return highlightDurationTicks / 20;
    }

    /**
     * 配置加载/重载事件处理
     * 当配置文件被加载或在游戏中修改时触发
     */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // 将秒转换为 ticks（1秒 = 20 ticks）
        markDurationTicks = MARK_DURATION_SECONDS.get() * 20;
        markDisplayRange = MARK_DISPLAY_RANGE.get();
        highlightDurationTicks = HIGHLIGHT_DURATION_SECONDS.get() * 20;
    }
}
