package com.pob.tabtweaks;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = TabTweaksMod.MOD_ID)
public final class TabTweaksCommand {

    private TabTweaksCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tabtweaks")
                .then(Commands.literal("list").executes(context -> list(context.getSource())))
                .then(Commands.literal("reload").executes(context -> reload(context.getSource()))));
    }

    private static int list(CommandSourceStack source) {
        List<CreativeModeTab> tabs = CreativeModeTabRegistry.getSortedCreativeModeTabs();

        source.sendSuccess(() -> Component.literal("Вкладок всего: " + tabs.size())
                .withStyle(ChatFormatting.GOLD), false);

        for (int i = 0; i < tabs.size(); i++) {
            String id = TabTweaks.nameOf(tabs.get(i));
            int page = i / TabRule.SLOTS_PER_PAGE + 1;
            int slot = i % TabRule.SLOTS_PER_PAGE + 1;

            Component line = Component.literal(id)
                    .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.AQUA)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, id)))
                    .append(Component.literal("  стр. " + page + ", слот " + slot)
                            .withStyle(ChatFormatting.GRAY));

            source.sendSuccess(() -> line, false);
        }
        return tabs.size();
    }

    private static int reload(CommandSourceStack source) {
        TabTweaksMod.reload();
        source.sendSuccess(() -> Component.literal("Конфиг вкладок перечитан: "
                + TabTweaksMod.rules().size() + " правил").withStyle(ChatFormatting.GREEN), false);
        return 1;
    }
}
