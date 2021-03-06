package com.massivecraft.factions;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;

import com.massivecraft.mcore.mixin.Mixin;
import com.massivecraft.mcore.store.MStore;
import com.massivecraft.mcore.store.SenderColl;
import com.massivecraft.mcore.util.DiscUtil;
import com.massivecraft.mcore.util.TimeUnit;
import com.massivecraft.mcore.xlib.gson.reflect.TypeToken;

public class FPlayerColl extends SenderColl<FPlayer>
{
	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //
	
	private static FPlayerColl i = new FPlayerColl();
	public static FPlayerColl get() { return i; }
	private FPlayerColl()
	{
		super(Const.COLLECTION_BASENAME_PLAYER, FPlayer.class, MStore.getDb(ConfServer.dburi), Factions.get());
	}
	
	// -------------------------------------------- //
	// OVERRIDE: COLL
	// -------------------------------------------- //
	
	// TODO: Init and migration routine!
	
	@Override
	public void init()
	{
		super.init();

		this.migrate();
	}
	
	public void migrate()
	{
		// Create file objects
		File oldFile = new File(Factions.get().getDataFolder(), "players.json");
		File newFile = new File(Factions.get().getDataFolder(), "players.json.migrated");
		
		// Already migrated?
		if ( ! oldFile.exists()) return;
		
		// Read the file content through GSON. 
		Type type = new TypeToken<Map<String, FPlayer>>(){}.getType();
		Map<String, FPlayer> id2fplayer = Factions.get().gson.fromJson(DiscUtil.readCatch(oldFile), type);
		
		// Set the data
		for (Entry<String, FPlayer> entry : id2fplayer.entrySet())
		{
			String playerId = entry.getKey();
			FPlayer fplayer = entry.getValue();
			FPlayerColl.get().create(playerId).load(fplayer);
		}
		
		// Mark as migrated
		oldFile.renameTo(newFile);
	}
	
	@Override
	protected synchronized String attach(FPlayer entity, Object oid, boolean noteChange)
	{
		String ret = super.attach(entity, oid, noteChange);
		
		// If inited ...
		if (!this.inited()) return ret;
		if (!FactionColl.get().inited()) return ret;
		
		// ... update the index.
		Faction faction = entity.getFaction();
		faction.fplayers.add(entity);
		
		return ret;
	}
	
	@Override
	public FPlayer detachId(Object oid)
	{
		FPlayer ret = super.detachId(oid);
		if (ret == null) return null;
		
		// If inited ...
		if (!this.inited()) return ret;
		
		// ... update the index.
		Faction faction = ret.getFaction();
		faction.fplayers.remove(ret);
		
		return ret;
	}
	
	// -------------------------------------------- //
	// EXTRAS
	// -------------------------------------------- //
	
	public void clean()
	{
		for (FPlayer fplayer : this.getAll())
		{
			if (FactionColl.get().containsId(fplayer.getFactionId())) continue;
			
			Factions.get().log("Reset faction data (invalid faction) for player "+fplayer.getName());
			fplayer.resetFactionData(false);
		}
	}
	
	public void autoLeaveOnInactivityRoutine()
	{
		if (ConfServer.autoLeaveAfterDaysOfInactivity <= 0.0) return;
		
		long now = System.currentTimeMillis();
		double toleranceMillis = ConfServer.autoLeaveAfterDaysOfInactivity * TimeUnit.MILLIS_PER_DAY;
		
		for (FPlayer fplayer : this.getAll())
		{
			Long lastPlayed = Mixin.getLastPlayed(fplayer.getId());
			if (lastPlayed == null) continue;
			
			if (fplayer.isOnline()) continue;
			if (now - lastPlayed <= toleranceMillis) continue;
			
			if (ConfServer.logFactionLeave || ConfServer.logFactionKick)
			{
				Factions.get().log("Player "+fplayer.getName()+" was auto-removed due to inactivity.");
			}

			// if player is faction leader, sort out the faction since he's going away
			if (fplayer.getRole() == Rel.LEADER)
			{
				Faction faction = fplayer.getFaction();
				if (faction != null)
				{
					fplayer.getFaction().promoteNewLeader();
				}
			}

			fplayer.leave(false);
			fplayer.detach();
		}
	}
}
