package org.conquest.enchantmentLimiter.commandHandler;

import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AutoTabManager implements TabCompleter {

    private static final List<String> ROOT_SUBCOMMANDS = List.of("reload");
    private static final List<String> TYPES = Arrays.asList("min", "max");

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {

        if (!sender.hasPermission("enchantmentlimiter.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(ROOT_SUBCOMMANDS);
            Registry.ENCHANTMENT.stream().map(e -> e.getKey().getKey()).forEach(suggestions::add);
            return partial(args[0], suggestions);
        }

        if (args.length == 2) {
            return partial(args[1], TYPES);
        }

        return Collections.emptyList();
    }

    private List<String> partial(String input, List<String> options) {
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(input.toLowerCase())) {
                result.add(option);
            }
        }
        return result;
    }
}
