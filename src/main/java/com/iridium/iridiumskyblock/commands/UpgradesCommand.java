package com.iridium.iridiumskyblock.commands;

import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.LogAction;
import com.iridium.iridiumskyblock.Upgrade;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandLog;
import com.iridium.iridiumskyblock.database.IslandUpgrade;
import com.iridium.iridiumskyblock.database.User;
import com.iridium.iridiumskyblock.gui.UpgradesGUI;
import com.iridium.iridiumskyblock.upgrades.UpgradeData;
import com.iridium.iridiumskyblock.utils.PlayerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UpgradesCommand extends Command {

    /**
     * The default constructor.
     */
    public UpgradesCommand() {
        super(Arrays.asList("upgrades", "upgrade"), "Open the Island Upgrades Menu", "", true, Duration.ZERO);
    }

    /**
     * Executes the command for the specified {@link CommandSender} with the provided arguments.
     * Not called when the command execution was invalid (no permission, no player or command disabled).
     * Shows an overview over the members of the Island and allows quick rank management.
     *
     * @param sender The CommandSender which executes this command
     * @param args   The arguments used with this command. They contain the sub-command
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);
        Optional<Island> island = user.getIsland();

        if (island.isPresent()) {
            if (args.length == 2) {
                String upgradeName = args[1];
                Upgrade<?> upgrade = IridiumSkyblock.getInstance().getUpgradesList().get(upgradeName);
                if (upgrade != null) {
                    IslandUpgrade islandUpgrade = IridiumSkyblock.getInstance().getIslandManager().getIslandUpgrade(island.get(), upgradeName);
                    if (upgrade.upgrades.containsKey(islandUpgrade.getLevel() + 1)) {
                        UpgradeData upgradeData = upgrade.upgrades.get(islandUpgrade.getLevel() + 1);
                        if (PlayerUtils.pay(player, island.get(), upgradeData.crystals, upgradeData.money)) {
                            islandUpgrade.setLevel(islandUpgrade.getLevel() + 1);
                            IslandLog islandLog = new IslandLog(island.get(), LogAction.UPGRADE_PURCHASE, user, null, 0, upgradeName);
                            IridiumSkyblock.getInstance().getDatabaseManager().getIslandLogTableManager().addEntry(islandLog);
                            IridiumSkyblock.getInstance().getIslandManager().sendIslandBorder(island.get());
                            island.get().resetCache();
                            return true;
                        } else {
                            player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().cannotAfford.replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
                        }
                    } else {
                        player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().maxLevelReached.replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
                    }
                } else {
                    player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().unknownUpgrade.replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
                }
            } else {
                player.openInventory(new UpgradesGUI(island.get()).getInventory());
                return true;
            }
        } else {
            player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().noIsland.replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
        }

        return false;
    }

    /**
     * Handles tab-completion for this command.
     *
     * @param commandSender The CommandSender which tries to tab-complete
     * @param command       The command
     * @param label         The label of the command
     * @param args          The arguments already provided by the sender
     * @return The list of tab completions for this command
     */
    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String label, String[] args) {
        return IridiumSkyblock.getInstance().getUpgradesList().keySet().stream().filter(s -> s.toLowerCase().contains(args[1].toLowerCase())).collect(Collectors.toList());
    }

}
