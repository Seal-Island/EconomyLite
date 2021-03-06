/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.commands.virtual;

import io.github.flibio.utils.commands.ParentCommand;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.command.spec.CommandSpec.Builder;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;
import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.utils.commands.AsyncCommand;
import io.github.flibio.utils.commands.BaseCommandExecutor;
import io.github.flibio.utils.commands.Command;
import io.github.flibio.utils.message.MessageStorage;

import java.math.BigDecimal;
import java.util.Optional;

@AsyncCommand
@ParentCommand(parentCommand = VirtualEconCommand.class)
@Command(aliases = {"set"}, permission = "economylite.admin.virtual.set")
public class VirtualSetCommand extends BaseCommandExecutor<CommandSource> {

    private MessageStorage messageStorage = EconomyLite.getMessageStorage();

    @Override
    public Builder getCommandSpecBuilder() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(GenericArguments.string(Text.of("account")), GenericArguments.doubleNum(Text.of("amount")));
    }

    @Override
    public void run(CommandSource src, CommandContext args) {
        if (args.getOne("account").isPresent() && args.getOne("amount").isPresent()) {
            String target = args.<String>getOne("account").get();
            BigDecimal newBal = BigDecimal.valueOf(args.<Double>getOne("amount").get());
            Optional<Account> aOpt = EconomyLite.getEconomyService().getOrCreateAccount(target);
            if (aOpt.isPresent()) {
                Account targetAccount = aOpt.get();
                if (targetAccount.setBalance(EconomyLite.getCurrencyService().getCurrentCurrency(), newBal,
                        Cause.of(EventContext.empty(), (EconomyLite.getInstance()))).getResult().equals(ResultType.SUCCESS)) {
                    src.sendMessage(messageStorage.getMessage("command.econ.setsuccess", "name", targetAccount.getDisplayName().toPlain()));
                } else {
                    src.sendMessage(messageStorage.getMessage("command.econ.setfail", "name", targetAccount.getDisplayName().toPlain()));
                }
            } else {
                src.sendMessage(messageStorage.getMessage("command.error"));
            }
        } else {
            src.sendMessage(messageStorage.getMessage("command.error"));
        }
    }

}
