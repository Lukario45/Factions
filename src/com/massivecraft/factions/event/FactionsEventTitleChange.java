package com.massivecraft.factions.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import com.massivecraft.factions.FPlayer;

public class FactionsEventTitleChange extends FactionsEventAbstractSender
{	
	// -------------------------------------------- //
	// REQUIRED EVENT CODE
	// -------------------------------------------- //
	
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
	
	// -------------------------------------------- //
	// FIELDS
	// -------------------------------------------- //
	
	private final FPlayer fplayer;
	public FPlayer getFPlayer() { return this.fplayer; }
	
	private String newTitle;
	public String getNewTitle() { return this.newTitle; }
	public void setNewTitle(String newTitle) { this.newTitle = newTitle; }
	
	// -------------------------------------------- //
	// CONSTRUCT
	// -------------------------------------------- //
	
	public FactionsEventTitleChange(CommandSender sender, FPlayer fplayer, String newTitle)
	{
		super(sender);
		this.fplayer = fplayer;
		this.newTitle = newTitle;
	}
	
}
