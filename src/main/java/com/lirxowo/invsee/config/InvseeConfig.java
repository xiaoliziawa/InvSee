package com.lirxowo.invsee.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = "invsee")
public class InvseeConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue MARK_DURATION_SECONDS = BUILDER
            .comment("标记物品在世界中显示的持续时间（秒）",
                     "Duration that marked items are displayed in the world (seconds)")
            .defineInRange("mark_duration_seconds", 5, 1, 60);

    private static final ModConfigSpec.IntValue MARK_DISPLAY_RANGE = BUILDER
            .comment("标记物品可见的最大距离（方块）",
                     "Maximum distance to see marked items (blocks)")
            .defineInRange("mark_display_range", 64, 16, 256);

    private static final ModConfigSpec.IntValue HIGHLIGHT_DURATION_SECONDS = BUILDER
            .comment("容器槽位高亮显示的持续时间（秒）",
                     "Duration that container slots remain highlighted (seconds)")
            .defineInRange("highlight_duration_seconds", 5, 1, 60);

    private static final ModConfigSpec.BooleanValue SHOW_TOOLTIP = BUILDER
            .comment("是否在标记信息框旁显示原版 Tooltip 信息",
                     "Whether to show vanilla tooltip info next to the mark info box")
            .define("show_tooltip", true);

    private static final ModConfigSpec.BooleanValue SHOW_EXTRA_INFO = BUILDER
            .comment("是否显示更多物品信息（如耐久、附魔、能量等）",
                     "Whether to show extra item info (durability, enchantments, energy, etc.)",
                     "关闭时只显示物品图标和名称 / When disabled, only shows item icon and name")
            .define("show_extra_info", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static int markDurationTicks = 100;
    private static int markDisplayRange = 64;
    private static int highlightDurationTicks = 100;
    private static boolean showTooltip = true;
    private static boolean showExtraInfo = false;

    public static int getMarkDurationTicks() {
        return markDurationTicks;
    }

    public static int getMarkDurationSeconds() {
        return markDurationTicks / 20;
    }

    public static int getMarkDisplayRange() {
        return markDisplayRange;
    }

    public static double getMarkDisplayRangeSq() {
        return (double) markDisplayRange * markDisplayRange;
    }

    public static int getHighlightDurationTicks() {
        return highlightDurationTicks;
    }

    public static int getHighlightDurationSeconds() {
        return highlightDurationTicks / 20;
    }

    public static boolean isShowTooltip() {
        return showTooltip;
    }

    public static boolean isShowExtraInfo() {
        return showExtraInfo;
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        markDurationTicks = MARK_DURATION_SECONDS.get() * 20;
        markDisplayRange = MARK_DISPLAY_RANGE.get();
        highlightDurationTicks = HIGHLIGHT_DURATION_SECONDS.get() * 20;
        showTooltip = SHOW_TOOLTIP.get();
        showExtraInfo = SHOW_EXTRA_INFO.get();
    }
}
