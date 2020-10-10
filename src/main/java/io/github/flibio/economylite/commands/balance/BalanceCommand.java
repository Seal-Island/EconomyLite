/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.commands.balance;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.api.CurrencyEconService;
import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.message.MessageStorage;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

@AsyncCommand
@Command(aliases = {"balance", "money", "bal"}, permission = "economylite.balance")
public class BalanceCommand extends BaseCommandExecutor<CommandSource> {

    private MessageStorage messageStorage = EconomyLite.getMessageStorage();
    private CurrencyEconService currencyService = EconomyLite.getCurrencyService();

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(GenericArguments.optional(GenericArguments.user(Text.of("player"))), GenericArguments.optional(GenericArguments.string(Text.of("currency"))));
    }

    @Override
    public void run(CommandSource src, CommandContext args) {
        Currency currency = currencyService.getCurrentCurrency();
        if (args.getOne("player").isPresent()) {
            if (args.getOne("currency").isPresent()) {
                User target = args.<User>getOne("player").get();
                String targetName = target.getName();
                boolean isAccountOwner = src instanceof Player && target.getUniqueId().equals(((Player) src).getUniqueId());
                if(isAccountOwner || src.hasPermission("economylite.admin.balanceothers")) {
                    Optional<UniqueAccount> uOpt = EconomyLite.getEconomyService().getOrCreateAccount(target.getUniqueId());
                    if (uOpt.isPresent()) {
                        String currencyArg = args.<String>getOne("currency").get();
                        if(EconomyLite.currencies.containsKey(currencyArg)) {
                            Currency currencyToUse = EconomyLite.currencies.get(currencyArg);
                            BigDecimal bal = uOpt.get().getBalance(currencyToUse);
                            Text label = currencyToUse.getPluralDisplayName();
                            if (bal.equals(BigDecimal.ONE)) {
                                label = currencyToUse.getDisplayName();
                            }
                            src.sendMessage(messageStorage
                                    .getMessage(isAccountOwner ? "command.balance" : "command.balanceother", "player", targetName, "balance", String.format(Locale.ENGLISH, "%,.2f", bal), "label",
                                            TextSerializers.FORMATTING_CODE.serialize(label)));
                        } else {
                            src.sendMessage(messageStorage.getMessage("command.currency.invalid"));
                        }
                    } else {
                        src.sendMessage(messageStorage.getMessage("command.error"));
                    }
                } else {
                    src.sendMessage(messageStorage.getMessage("command.noperm"));
                }
            } else if (src.hasPermission("economylite.admin.balanceothers")) {
                User target = args.<User>getOne("player").get();
                String targetName = target.getName();
                Optional<UniqueAccount> uOpt = EconomyLite.getEconomyService().getOrCreateAccount(target.getUniqueId());
                if (uOpt.isPresent()) {
                    BigDecimal bal = uOpt.get().getBalance(currency);
                    Text label = currency.getPluralDisplayName();
                    if (bal.equals(BigDecimal.ONE)) {
                        label = currency.getDisplayName();
                    }
                    src.sendMessage(messageStorage
                            .getMessage("command.balanceother", "player", targetName, "balance", String.format(Locale.ENGLISH, "%,.2f", bal), "label",
                                    TextSerializers.FORMATTING_CODE.serialize(label)));
                } else {
                    src.sendMessage(messageStorage.getMessage("command.error"));
                }
            } else {
                src.sendMessage(messageStorage.getMessage("command.noperm"));
            }
        } else {
            // Get the balance of the running player
            if (src instanceof Player) {
                Player player = (Player) src;
                Optional<UniqueAccount> uOpt = EconomyLite.getEconomyService().getOrCreateAccount(player.getUniqueId());
                if (uOpt.isPresent()) {
                    BigDecimal bal = uOpt.get().getBalance(currency);
                    Text label = currency.getPluralDisplayName();
                    if (bal.equals(BigDecimal.ONE)) {
                        label = currency.getDisplayName();
                    }
                    src.sendMessage(messageStorage.getMessage("command.balance", "balance", String.format(Locale.ENGLISH, "%,.2f", bal),
                            "label", TextSerializers.FORMATTING_CODE.serialize(label)));
                } else {
                    src.sendMessage(messageStorage.getMessage("command.error"));
                }
            } else {
                // If the there are no arguments the source must be a player
                src.sendMessage(messageStorage.getMessage("command.invalidsource", "sourcetype", "player"));
            }
        }
    }

}
