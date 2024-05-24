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
You'll then need the appropriate installation on Java on your computer, and the Epicbot API located on their website. 

## Things that could be improved

This version of BoneMan does not take enemies into consideration, it only accounts for when it is in combat or low on essential stats (HP/Stamina) 
A huge improvement would be to have the bot check the number of enemies in Boneyard areas before picking one, to eliminate ones with a higher enemy density. 
Because this improvement has not been implemented, the current bot can still occasionally die due to running through multiple enemies occasionally occuring. 

## Credit where its due 

A big thanks to Suko and Fallacy for helping familiarize me with the API, as Im sure that will still take some experience as time goes on. 
And another thanks to Fallacy for the Custom Walking function/Keying me into the usage of widgets. You fixed a bug for me, mad thanks. 
