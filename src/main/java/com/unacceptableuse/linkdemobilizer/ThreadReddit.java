package com.unacceptableuse.linkdemobilizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Paginator;
import net.dean.jraw.paginators.Paginators;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

public class ThreadReddit implements Runnable{

	public RedditClient reddit;
	public AccountManager manager;
	public Properties props;
	private Pattern subredditMatcher = Pattern.compile("\\br\\/([^\\s\\/;\\-\\.,!?]+)\\b");
	public ArrayList<String> alreadyReplied = new ArrayList<String>();
	private LinkDemobilizerBot parent;
	
	public ThreadReddit(LinkDemobilizerBot parent, RedditClient reddit, AccountManager manager, Properties props){
		this.reddit = reddit;
		this.manager = manager;
		this.props = props;
		this.parent = parent;
	}
	
	
	public void run() {
		double currentIndex = 0;
		Paginator<Submission> subreddit = Paginators.subreddit(reddit, "all"); //Check all subreddits
		subreddit.setTimePeriod(TimePeriod.HOUR);	//Check only within the last hour
		subreddit.setLimit(1000); 					//Only do 1000 at a time
		subreddit.setSorting(Sorting.RISING); 		//Rising is going to have more comments
		while(subreddit.hasNext()){					//Whilst there are still posts to check
			currentIndex++;
			parent.window.overallProgress.setProgress(currentIndex/100);
			try{
				double currentSubIndex = 0;
				Listing<Submission> submissions = subreddit.next(); //Array of listings
				for(Submission sub : submissions)					//For every submission in the listing
				{
					currentSubIndex++;
					parent.window.submissionProgress.setProgress(currentSubIndex/submissions.size());
					boolean hasReplied = false;						 //We havn't replied to it yet
					Submission s = reddit.getSubmission(sub.getId());//We get the full submission so we can see the comments (Reddit's API doesn't return comments from the listings)
					parent.log("Submission: "+s.getTitle());
					
					for(Comment comment : s.getComments())			 //For every comment in the submission
					{
						parent.log("--Comment: "+comment.getAuthor());
						if(comment.getAuthor().equals(props.getProperty("username"))) //If one of the comments belongs to the bot we don't want to reply to it
						{
							hasReplied = true;
							continue;
						}				
						String rawCommentBody = comment.getBody().toString();					//The raw comment so we can find the link easier
						Matcher match = subredditMatcher.matcher(rawCommentBody);				//We use regex to match comments with r/something in
						if(match.find()){
							String target = match.group();										
							if(!rawCommentBody.contains("/r/")){								//We check if it's already correctly formatted
								if(!alreadyReplied.contains(comment.getId())){					//We check the array of comments this session to see if we've done it already
									if(!hasReplied){											//Then we check the boolean too, we can't have double posts!
										if(!target.contains(comment.getSubredditName())){		//Check it's not referencing itsself
											//if(Arrays.asList(props.getProperty("nsfwBlacklist").split(",")).contains(comment.getSubredditName()) && reddit.getSubreddit(target).isNsfw()){
												
												manager.reply(comment, String.format(props.getProperty("subredditTemplate"), match.group()));
												alreadyReplied.add(comment.getId());
												parent.log("---Replied to "+rawCommentBody);
												parent.addSubreddit(comment.getSubredditName());
												
											//}else{
											//	parent.log("---Comment Skipped: Subreddit is on NSFW blacklist.");
											//}
										}else{
											parent.log("---Comment Skipped: Comment references current subreddit.");
										}
									}else{
										parent.log("---Comment Skipped: Previous reply in child comments.");
									}
								}else{
									parent.log("---Comment Skipped: Already checked this comment.");
								}
							}else{
								parent.log("---Comment Skipped: Starts with a /");
							}
	
						}
						for(String[] replacement : replacements) //We also check for mobile links. Nobody really cares about that though anymroe
						{
							
							if(comment.getBody().toString().contains(replacement[0]) && !hasReplied)
							{								
								reply(s, "http://"+replacement[1]+s.getSelftext().md().split(replacement[0])[1].split(" ")[0]);
								break;
							}
						}
						
					}
					
					if(s.isSelfPost()) //If it's a self post, we check for links to replace too
					{
						for(String[] replacement : replacements)
						{
							if(s.getSelftext().toString().contains(replacement[0]) && !hasReplied)
							{
								reply(s,"http://"+replacement[1]+s.getSelftext().md().split(replacement[0])[1].split(" ")[0]);
								break;
							}
						}
					}
				}
			}catch(Exception e){}
		}
		
		parent.log("Waiting...");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		run();
		
	}
	
	public void reply(Contribution thing,String replacement)
	{
		if(alreadyReplied.contains(thing.getId()))
		{
			parent.log("---This comment has already been done. Skipping.");
		}else
		{
			parent.log("---Commenting on "+thing.getId());
			try
			{
				manager.reply(thing, String.format(props.getProperty("replyTemplate"), replacement));
			} catch (NetworkException e)
			{
				e.printStackTrace();
			} catch (ApiException e)
			{
				e.printStackTrace();
			}
			alreadyReplied.add(thing.getId());
		}		
	}
	
	public static final String[][] replacements = {
		{ "en.m.wikipedia.org", "en.wikipedia.org" },
		{ "m.wikipedia.org", "wikipedia.org" },
		{ "m.reddit.com", "reddit.com" },
		{ "mobile.slate.com", "slate.com" },
		{ "mobile.theverge.com", "theverge.com" },
		{ "m.theatlantic.com", "theatlantic.com" },
		{ "m.ign.com", "ign.com" },
		{ "m.guardian.com", "guardian.com" },
		{ "m.facebook.com", "facebook.com" },
		{ "m.wolframalpha.com", "wolframalpha.com" },
		{ "m.flickr.com", "flickr.com" },
		{ "mobile.twitter.com", "twitter.com" },
		{ "mobile.myspace.com", "myspace.com" }
	};

}
