package com.massivecraft.factions.cmd;

import com.massivecraft.factions.BoardColl;
import com.massivecraft.factions.FPerm;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Perm;
import com.massivecraft.factions.TerritoryAccess;
import com.massivecraft.factions.cmd.arg.ARFPlayer;
import com.massivecraft.factions.cmd.arg.ARFaction;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.mcore.cmd.req.ReqIsPlayer;
import com.massivecraft.mcore.ps.PS;
import com.massivecraft.mcore.util.Txt;


public class CmdFactionsAccess extends FCommand
{
	public CmdFactionsAccess()
	{
		this.addAliases("access");
		
		this.addOptionalArg("view|p|player|f|faction", "view");
		this.addOptionalArg("name", "you");
		
		this.setDesc("view or grant access for the claimed territory you are in");
		
		// TODO: Missing permission node here!?
		this.addRequirements(ReqIsPlayer.get());
	}
	
	@Override
	public void perform()
	{
		String type = this.arg(0);
		type = (type == null) ? "" : type.toLowerCase();
		PS chunk = PS.valueOf(me).getChunk(true);

		TerritoryAccess territory = BoardColl.get().getTerritoryAccessAt(chunk);
		Faction locFaction = territory.getHostFaction();
		boolean accessAny = Perm.ACCESS_ANY.has(sender, false);

		if (type.isEmpty() || type.equals("view"))
		{
			if ( ! accessAny && ! Perm.ACCESS_VIEW.has(sender, true)) return;
			if ( ! accessAny && ! territory.doesHostFactionMatch(fme))
			{
				msg("<b>This territory isn't controlled by your faction, so you can't view the access list.");
				return;
			}
			showAccessList(territory, locFaction);
			return;
		}

		if ( ! accessAny && ! Perm.ACCESS.has(sender, true)) return;
		if ( ! accessAny && ! FPerm.ACCESS.has(fme, locFaction, true)) return;

		boolean doPlayer = true;
		if (type.equals("f") || type.equals("faction"))
		{
			doPlayer = false;
		}
		else if (!type.equals("p") && !type.equals("player"))
		{
			msg("<b>You must specify \"p\" or \"player\" to indicate a player or \"f\" or \"faction\" to indicate a faction.");
			msg("<b>ex. /f access p SomePlayer  -or-  /f access f SomeFaction");
			msg("<b>Alternately, you can use the command with nothing (or \"view\") specified to simply view the access list.");
			return;
		}

		String target = "";
		boolean added;

		if (doPlayer)
		{
			FPlayer targetPlayer = this.arg(1, ARFPlayer.getStartAny(), fme);
			if (targetPlayer == null) return;
			added = territory.toggleFPlayer(targetPlayer);
			target = "Player \""+targetPlayer.getName()+"\"";
		}
		else
		{
			Faction targetFaction = this.arg(1, ARFaction.get(), myFaction);
			if (targetFaction == null) return;
			added = territory.toggleFaction(targetFaction);
			target = "Faction \""+targetFaction.getTag()+"\"";
		}

		msg("<i>%s has been %s<i> the access list for this territory.", target, Txt.parse(added ? "<lime>added to" : "<rose>removed from"));
		SpoutFeatures.updateAccessInfoLoc(chunk);
		showAccessList(territory, locFaction);
	}

	private void showAccessList(TerritoryAccess territory, Faction locFaction)
	{
		msg("<i>Host faction %s has %s<i> in this territory.", locFaction.getTag(), Txt.parse(territory.isHostFactionAllowed() ? "<lime>normal access" : "<rose>restricted access"));

		String players = territory.fplayerList();
		String factions = territory.factionList();

		if (factions.isEmpty())
			msg("No factions have been explicitly granted access.");
		else
			msg("Factions with explicit access: " + factions);

		if (players.isEmpty())
			msg("No players have been explicitly granted access.");
		else
			msg("Players with explicit access: " + players);
	}
}
