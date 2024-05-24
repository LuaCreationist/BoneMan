## Description

This is my first script for Epicbot, I call it “Bone Man” . I’m excited to show it and see what people might think of it. 

Bone Man is a script meant to take over right after Pro Tutorial Island. It’s to help people who want to have bots essentially start themselves off rather than needing player intervention. 

When Bone Man is active, the player will bank everything and start going to collect Big Bones from the Boneyard wilderness area. 

When the bots inventory is full, he will navigate back to the GE and automatically sell Big Bones, store the gold, and return. 

BoneMan will choose a random tile from the closest of 4 “Safe Spot” areas whenever its needed to navigate to one. 

BoneMan then chooses a random Boneyard Area, from 4 potential boneyard areas, and picks a random tile to navigate to from that area to search for bones. 

When in a safespot Bone Man will automatically disable Auto-Retaliate and switch to defense. The bot will also keep its inventory visible so the user can track its progress.

There is no on screen status display, though you can enable/disable debugging to output via the debugging variable which is tied to the “Print” function. This is mostly just to save resources in the case of running more bots. 

Some notes towards the code I’ve made for myself, in the future I would try to avoid nesting as much as I did in some areas for readability. Though in other cases lack of readability could be due to the code trying to use as little memory as possible, and allow for more bots. 

In my case I was programming in a linear mindset to become familiar, which resulted in my code being a tad less readable and taking up more lines than needed. 

Im also looking for feedback and criticism, as this is the first time in a long time I've touched java. 

## Using this code

To use this script, you're going to need to install a code editor for Java, I use IntelliJ.

You'll then need the appropriate installation of Java on your computer, and the Epicbot API located on their website. 


## Credit where its due 

A big thanks to Suko and Fallacy for helping familiarize me with the API, as Im sure that will still take some experience as time goes on. 
And another thanks to Fallacy for the Custom Walking function/Keying me into the usage of widgets. You fixed a bug for me, mad thanks. 

## Pros, Cons, and desired improvements. 

Pros: 

-Successfully De-Aggros enemies 

-Handles failure states well 

-Keyturn functionality (Hit play and walk away) 

-Helps get new bots ready to buy early equipment

-Automatically enables defense mode and disables auto-retaliate

Cons: 

-Slow

-Can still die, allbeit not often in my limited testing.

-Does not avoid enemies

-Does not avoid players

-Pathing from GE -> Safespot or Safespot -> GE poses some risk. 

## What to improve: 

Developing a method for the bot to avoid enemies and pick more ideal locations for bones would greatly reduce the amount of time taken per inventory of bones. 

Putting a middle pathing point between the GE and Safespot would prevent the bot from navigating through PvP Event areas. 

Finding the most optimal places to check for strong players near and implementing them into the interruptions would keep the bot safe from PKing. 


