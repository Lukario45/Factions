package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.Perm;
import com.massivecraft.factions.cmd.arg.ARFPlayer;
import com.massivecraft.factions.cmd.arg.ARFaction;
import com.massivecraft.mcore.cmd.arg.ARDouble;
import com.massivecraft.mcore.cmd.req.ReqHasPerm;

public class CmdFactionsPowerBoost extends FCommand
{
	public CmdFactionsPowerBoost()
	{
		this.addAliases("powerboost");
		
		this.addRequiredArg("p|f|player|faction");
		this.addRequiredArg("name");
		this.addRequiredArg("#");
		
		this.addRequirements(ReqHasPerm.get(Perm.POWERBOOST.node));
	}
	
	@Override
	public void perform()
	{
		String type = this.arg(0).toLowerCase();
		boolean doPlayer = true;
		if (type.equals("f") || type.equals("faction"))
		{
			doPlayer = false;
		}
		else if (!type.equals("p") && !type.equals("player"))
		{
			msg("<b>You must specify \"p\" or \"player\" to target a player or \"f\" or \"faction\" to target a faction.");
			msg("<b>ex. /f powerboost p SomePlayer 0.5  -or-  /f powerboost f SomeFaction -5");
			return;
		}
		
		Double targetPower = this.arg(2, ARDouble.get());
		if (targetPower == null) return;

		String target;

		if (doPlayer)
		{
			FPlayer targetPlayer = this.arg(1, ARFPlayer.getStartAny());
			if (targetPlayer == null) return;
			
			targetPlayer.setPowerBoost(targetPower);
			target = "Player \""+targetPlayer.getName()+"\"";
		}
		else
		{
			Faction targetFaction = this.arg(1, ARFaction.get());
			if (targetFaction == null) return;
			
			targetFaction.setPowerBoost(targetPower);
			target = "Faction \""+targetFaction.getTag()+"\"";
		}

		msg("<i>"+target+" now has a power bonus/penalty of "+targetPower+" to min and max power levels.");
		Factions.get().log(fme.getName()+" has set the power bonus/penalty for "+target+" to "+targetPower+".");
	}
}
