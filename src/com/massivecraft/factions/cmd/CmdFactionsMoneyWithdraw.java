package com.massivecraft.factions.cmd;

import com.massivecraft.factions.ConfServer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Perm;
import com.massivecraft.factions.cmd.arg.ARFaction;
import com.massivecraft.factions.cmd.req.ReqBankCommandsEnabled;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.mcore.cmd.arg.ARDouble;
import com.massivecraft.mcore.cmd.req.ReqHasPerm;
import com.massivecraft.mcore.util.Txt;

import org.bukkit.ChatColor;


public class CmdFactionsMoneyWithdraw extends FCommand
{
	public CmdFactionsMoneyWithdraw()
	{
		this.addAliases("w", "withdraw");
		
		this.addRequiredArg("amount");
		this.addOptionalArg("faction", "you");
		
		this.addRequirements(ReqHasPerm.get(Perm.MONEY_WITHDRAW.node));
		this.addRequirements(ReqBankCommandsEnabled.get());
	}
	
	@Override
	public void perform()
	{
		Double amount = this.arg(0, ARDouble.get());
		if (amount == null) return;
		
		Faction faction = this.arg(1, ARFaction.get(), myFaction);
		if (faction == null) return;
		
		boolean success = Econ.transferMoney(fme, faction, fme, amount);

		if (success && ConfServer.logMoneyTransactions)
			Factions.get().log(ChatColor.stripColor(Txt.parse("%s withdrew %s from the faction bank: %s", fme.getName(), Econ.moneyString(amount), faction.describeTo(null))));
	}
}
