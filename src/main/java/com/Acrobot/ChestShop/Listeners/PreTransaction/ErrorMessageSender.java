package com.Acrobot.ChestShop.Listeners.PreTransaction;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Commands.Toggle;
import com.Acrobot.ChestShop.Configuration.Messages;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Database.Account;
import com.Acrobot.ChestShop.Economy.Economy;
import com.Acrobot.ChestShop.Events.PreTransactionEvent;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

import static com.Acrobot.ChestShop.Configuration.Messages.*;

/**
 * @author Acrobot
 */
public class ErrorMessageSender implements Listener {

    private static final Table<UUID, String, Long> notificationCooldowns = HashBasedTable.create();

    @EventHandler(priority = EventPriority.MONITOR)
    public static void onQuit(PlayerQuitEvent event) {
        if (Properties.NOTIFICATION_MESSAGE_COOLDOWN > 0) {
            notificationCooldowns.rowMap().remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public static void onMessage(PreTransactionEvent event) {
        if (!event.isCancelled()) {
            return;
        }

        String message = null;

        switch (event.getTransactionOutcome()) {
            case SHOP_DOES_NOT_BUY_THIS_ITEM:
                message = Messages.NO_BUYING_HERE;
                break;
            case SHOP_DOES_NOT_SELL_THIS_ITEM:
                message = Messages.NO_SELLING_HERE;
                break;
            case CLIENT_DOES_NOT_HAVE_PERMISSION:
                message = Messages.NO_PERMISSION;
                break;
            case CLIENT_DOES_NOT_HAVE_ENOUGH_MONEY:
                message = Messages.NOT_ENOUGH_MONEY;
                break;
            case SHOP_DOES_NOT_HAVE_ENOUGH_MONEY:
                message = Messages.NOT_ENOUGH_MONEY_SHOP;
                break;
            case NOT_ENOUGH_SPACE_IN_CHEST:
                if (Properties.SHOW_MESSAGE_FULL_SHOP && !Properties.CSTOGGLE_TOGGLES_FULL_SHOP || !Toggle.isIgnoring(event.getOwnerAccount().getUuid())) {
                    Location loc = event.getSign().getLocation();
                    String messageNotEnoughSpace = Messages.prefix(NOT_ENOUGH_SPACE_IN_YOUR_SHOP)
                            .replace("%price", Economy.formatBalance(event.getExactPrice()))
                            .replace("%seller", event.getClient().getName())
                            .replace("%world", loc.getWorld().getName())
                            .replace("%x", String.valueOf(loc.getBlockX()))
                            .replace("%y", String.valueOf(loc.getBlockY()))
                            .replace("%z", String.valueOf(loc.getBlockZ()));
                    sendMessageToOwner(event.getOwnerAccount(), messageNotEnoughSpace, event.getStock());
                }
                message = Messages.NOT_ENOUGH_SPACE_IN_CHEST;
                break;
            case NOT_ENOUGH_SPACE_IN_INVENTORY:
                message = Messages.NOT_ENOUGH_SPACE_IN_INVENTORY;
                break;
            case NOT_ENOUGH_STOCK_IN_INVENTORY:
                message = Messages.NOT_ENOUGH_ITEMS_TO_SELL;
                break;
            case NOT_ENOUGH_STOCK_IN_CHEST:
                if (Properties.SHOW_MESSAGE_OUT_OF_STOCK && !Properties.CSTOGGLE_TOGGLES_OUT_OF_STOCK || !Toggle.isIgnoring(event.getOwnerAccount().getUuid())) {
                    Location loc = event.getSign().getLocation();
                    String messageOutOfStock = Messages.prefix(NOT_ENOUGH_STOCK_IN_YOUR_SHOP)
                            .replace("%price", Economy.formatBalance(event.getExactPrice()))
                            .replace("%buyer", event.getClient().getName())
                            .replace("%world", loc.getWorld().getName())
                            .replace("%x", String.valueOf(loc.getBlockX()))
                            .replace("%y", String.valueOf(loc.getBlockY()))
                            .replace("%z", String.valueOf(loc.getBlockZ()));
                    sendMessageToOwner(event.getOwnerAccount(), messageOutOfStock, event.getStock());
                }
                message = Messages.NOT_ENOUGH_STOCK;
                break;
            case CLIENT_DEPOSIT_FAILED:
                message = Messages.CLIENT_DEPOSIT_FAILED;
                break;
            case SHOP_DEPOSIT_FAILED:
                String messageDepositFailed = Messages.prefix(CLIENT_DEPOSIT_FAILED);
                sendMessageToOwner(event.getOwnerAccount(), messageDepositFailed);
                message = Messages.SHOP_DEPOSIT_FAILED;
                break;
            case SHOP_IS_RESTRICTED:
                message = Messages.ACCESS_DENIED;
                break;
            case INVALID_SHOP:
                message = Messages.INVALID_SHOP_DETECTED;
                break;
            default:
                break;
        }

        if (message != null) {
            event.getClient().sendMessage(Messages.prefix(message));
        }
    }

    private static void sendMessageToOwner(Account ownerAccount, String message, ItemStack... stock) {
        Player player = Bukkit.getPlayer(ownerAccount.getUuid());
        if (player != null || Properties.BUNGEECORD_MESSAGES) {
            message = message.replace("%material", "%item");
            String replacedMessage = message.replace("%item", MaterialUtil.getItemList(stock));

            if (Properties.NOTIFICATION_MESSAGE_COOLDOWN > 0) {
                Long last = notificationCooldowns.get(ownerAccount.getUuid(), replacedMessage);
                if (last != null && last + Properties.NOTIFICATION_MESSAGE_COOLDOWN * 1000 > System.currentTimeMillis()) {
                    return;
                }
                notificationCooldowns.put(ownerAccount.getUuid(), replacedMessage, System.currentTimeMillis());
            }

            if (player != null) {
                if (Properties.SHOWITEM_MESSAGE && MaterialUtil.Show.sendMessage(player, message, stock)) {
                    return;
                }
                player.sendMessage(replacedMessage);
            } else {
                ChestShop.sendBungeeMessage(ownerAccount.getName(), replacedMessage);
            }
        }
    }
}
