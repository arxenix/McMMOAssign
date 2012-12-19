package com.github.bobacadodl.McMMOAssign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.datatypes.PlayerStat;
import com.gmail.nossr50.datatypes.SkillType;

public class McMMOAssign extends JavaPlugin implements Listener,CommandExecutor{
	public HashMap<String,Integer> points = new HashMap<String,Integer>();
	public void onEnable(){
		if(getServer().getPluginManager().isPluginEnabled("mcMMO")==false){
			getServer().getLogger().log(Level.SEVERE, "mcMMO is not enabled! Disabling plugin!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		loadConfiguration();
		for(String ppoints:getConfig().getStringList("Points")){
			String[] ppointsa = ppoints.split(",");
			String player = ppointsa[0];
			String pointsvalue = ppointsa[1];
			this.points.put(player, Integer.parseInt(pointsvalue));
		}
	}
	public void loadConfiguration(){
		if(getConfig().getInt("Config-Version")!=1){
			getConfig().set("Points",new ArrayList<String>());
			getConfig().set("Config-Version", 1);
		}
	    getConfig().options().copyDefaults(true);
	    saveConfig();
	}
	
	public void onDisable(){
		List<String> pointslist = new ArrayList<String>();
		for(String player:points.keySet()){
			pointslist.add(player+","+Integer.toString(points.get(player)));
		}
		getConfig().set("Points", pointslist);
		saveConfig();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("mcmmoassign")){
			if(args.length==0){
				if(!sender.hasPermission("mcmmoassign.help")) return false;
				
				sender.sendMessage(ChatColor.BOLD+(ChatColor.GOLD+"McMMOAssign"));
				sender.sendMessage(ChatColor.AQUA+"-/mcma add [player] [amount] - add the given amount of points to a player.");
				sender.sendMessage(ChatColor.AQUA+"-/mcma remove [player] [amount] - remove points from a player.");
				sender.sendMessage(ChatColor.AQUA+"-/mcma assign [skill] [amount] - assign the given amount of points to a skill.");
				sender.sendMessage(ChatColor.AQUA+"-/mcma info - view how many points you have left to spend.");


			}
			if(args.length>=1){
				if(args[0].equalsIgnoreCase("assign")){
					if(!sender.hasPermission("mcmmoassign.assign")) return false;
					
					if(!(sender instanceof Player)){
						sender.sendMessage("Must be a player!");
						return true;
					}
					Player p = (Player) sender;
					if(points.containsKey(p.getName())){
						//has points
						String skill = args[1];
						Config mc = Config.getInstance();
						SkillType skilltype = null;
						int cap = 0;
						if(skill.equalsIgnoreCase("taming")){
							skilltype = SkillType.TAMING;
							cap=mc.getLevelCapTaming();
						}
						if(skill.equalsIgnoreCase("swords")){
							skilltype = SkillType.SWORDS;
							cap=mc.getLevelCapSwords();
						}
						if(skill.equalsIgnoreCase("unarmed")){
							skilltype = SkillType.UNARMED;
							cap=mc.getLevelCapUnarmed();
						}
						if(skill.equalsIgnoreCase("archery")){
							skilltype = SkillType.ARCHERY;
							cap=mc.getLevelCapArchery();
						}
						if(skill.equalsIgnoreCase("axes")){
							skilltype = SkillType.AXES;
							cap=mc.getLevelCapAxes();
						}
						if(skill.equalsIgnoreCase("acro")||skill.equalsIgnoreCase("acrobatics")){
							skilltype = SkillType.ACROBATICS;
							cap=mc.getLevelCapAcrobatics();
						}
						if(skill.equalsIgnoreCase("fishing")){
							skilltype = SkillType.FISHING;
							cap=mc.getLevelCapFishing();
						}
						if(skill.equalsIgnoreCase("excavation")){
							skilltype = SkillType.EXCAVATION;
							cap=mc.getLevelCapExcavation();
						}
						if(skill.equalsIgnoreCase("mining")){
							skilltype = SkillType.MINING;
							cap=mc.getLevelCapMining();
						}
						if(skill.equalsIgnoreCase("herbalism")){
							skilltype = SkillType.HERBALISM;
							cap=mc.getLevelCapHerbalism();
						}
						if(skill.equalsIgnoreCase("repair")){
							skilltype = SkillType.REPAIR;
							cap=mc.getLevelCapRepair();
						}
						if(skill.equalsIgnoreCase("woodcutting")){
							skilltype = SkillType.WOODCUTTING;
							cap=mc.getLevelCapWoodcutting();
						}
						
						if(skilltype==null){
							p.sendMessage(ChatColor.RED+"Not a valid skill!");
							return true;
						}
						try{
							Integer.parseInt(args[2]);
						}
						catch(NumberFormatException ex){
							sender.sendMessage(ChatColor.RED+"Points must be a number!");
							return true;
						}
						int pointstoadd = Integer.parseInt(args[2]);
						if(points.get(p.getName())<pointstoadd){
							p.sendMessage(ChatColor.RED+"Not enough points!");
							return true;
						}
						if(ExperienceAPI.getLevel(p, skilltype)+pointstoadd>cap){
							p.sendMessage(ChatColor.RED+"Can't add points! Goes above level cap of "+Integer.toString(cap));
							return true;
						}
						int newpoints = points.get(p.getName())-pointstoadd;
						if(newpoints ==0) points.remove(p.getName());
						else points.put(p.getName(), newpoints);
						
						
						
						ExperienceAPI.addLevel(p, skilltype, pointstoadd, true);
						PlayerStat ps = new PlayerStat();
						ps.name=p.getName();
						ps.statVal=newpoints;
						com.gmail.nossr50.util.Leaderboard.updateLeaderboard(ps, skilltype);
						p.sendMessage(ChatColor.GREEN+args[2]+" points successfully added to "+args[1]+"!");
					}
				}
				if(args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("info")){
					if(!sender.hasPermission("mcmmoassign.check")) return false;

					if(points.containsKey(sender.getName())){
						sender.sendMessage(ChatColor.GREEN+"You currently have "+Integer.toString(points.get(sender.getName()))+" points!");
					}
					else{
						sender.sendMessage(ChatColor.RED+"You currently have 0 points!");
					}	
				}
				if(args[0].equalsIgnoreCase("take")||args[0].equalsIgnoreCase("remove")){
					if(args.length==3){
						if(!sender.hasPermission("mcmmoassign.remove")) return false;

						//correct args
						
						String p = args[1];
						//check if points is a number
						try{
							Integer.parseInt(args[2]);
						}
						catch(NumberFormatException ex){
							sender.sendMessage(ChatColor.RED+"Points must be a number!");
							return true;
						}
						
						int pointstoremove = Integer.parseInt(args[2]);
						
						//if they already have some points
						if(points.containsKey(p)){
							points.put(p,points.get(p)-pointstoremove);
						}
						//if they dont
						else{
							points.put(p, pointstoremove);
						}
						
						if(points.get(p)<=0){
							points.remove(p);
							sender.sendMessage(ChatColor.GREEN+args[2]+" points successfully removed from player "+p);
							sender.sendMessage(ChatColor.GREEN+p+"'s point total is now :0");
						}
						else{
							sender.sendMessage(ChatColor.GREEN+args[2]+" points successfully removed from player "+p);
							sender.sendMessage(ChatColor.GREEN+p+"'s point total is now :"+Integer.toString(points.get(p)));
						}
					}
				}
				if(args[0].equalsIgnoreCase("add")||args[0].equalsIgnoreCase("give")){
					if(args.length==3){
						if(!sender.hasPermission("mcmmoassign.add")) return false;

						//correct args
						
						String p = args[1];
						//check if points is a number
						try{
							Integer.parseInt(args[2]);
						}
						catch(NumberFormatException ex){
							sender.sendMessage(ChatColor.RED+"Points must be a number!");
							return true;
						}
						
						int pointstoadd = Integer.parseInt(args[2]);
						
						//if they already have some points
						if(points.containsKey(p)){
							points.put(p,points.get(p)+pointstoadd);
						}
						//if they dont
						else{
							points.put(p, pointstoadd);
						}
						sender.sendMessage(ChatColor.GREEN+args[2]+" points successfully added to player "+p);
						sender.sendMessage(ChatColor.GREEN+p+"'s point total is now :"+Integer.toString(points.get(p)));
					}
				}
				
			}
		}
		
		
		
		return false;
	}
}
