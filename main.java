//Copyright 2024 @ Christopher Harris II
//Open Source, available for anyone to use for any reason.

import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.GroundItem;
import com.epicbot.api.shared.entity.WidgetChild;
import com.epicbot.api.shared.methods.ICombatAPI;
import com.epicbot.api.shared.methods.ITabsAPI;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.model.ge.GrandExchangeOffer;
import com.epicbot.api.shared.model.path.finding.AStarAlgorithm;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.Random;
import com.epicbot.api.shared.util.time.Time;
import com.epicbot.api.shared.webwalking.model.RSBank;

@ScriptManifest(name = "BoneMan", gameType = GameType.OS)
public class main extends LoopScript {
    //Bot Variables
    final boolean debugging = true;//if outputs to console are enabled
    //Locations
    Area[] safespot_Areas = {
      new Area(3211,3717,3214,3720),
      new Area(3226,3722,3231,3723),
      new Area(3236,3723,3239,3724),
      new Area(3244,3720,3247,3722)
    };
    Area[] boneyard_Areas = {
            new Area(3217,3729,3220,3731),
            new Area(3224,3733,3226,3735),
            new Area(3236,3734,3240,3735),
            new Area(3250,3725,3253,3726)
    };
    //Survival Checks
    final double hp_Min = 70.0;
    final double hp_Max = 100.0;
    final int stamina_Min = 35;
    public int gold_Collected = 0;
    //Location Checks
    boolean in_Safespot = false;
    boolean in_Boneyard = false;
    //bone price
    public int bone_Price = 295;//295
    //\\
    //Bot Functions
    void openInventory(){
        if(!getAPIContext().tabs().isOpen(ITabsAPI.Tabs.INVENTORY)){
            getAPIContext().tabs().open(ITabsAPI.Tabs.INVENTORY);
        }
    }
    boolean checkProblemWidget() {
        WidgetChild problemWidget = getAPIContext().widgets().get(1, 2); // this is the box that pops up when entering the wilderness
        if (problemWidget != null && problemWidget.isVisible()) {
            System.out.println("WILDERNESS PROBLEM WIDGET DETECTED, CLICKING ENTER");
            problemWidget.getChild(3).click(); // click the exact button of the widget
        }
        return false; // always return false to bypass lamda/boolean in condition
    }
    void customWalking(Tile tile, int targetRadius){
        if(!tile.getArea(targetRadius).contains(getAPIContext().localPlayer().getLocation())) {
            // Walk to location if we are NOT inside our target radius
            getAPIContext().webWalking().walkTo(tile, () -> checkProblemWidget());
        }
    }
    private void print(String value){ //prints to output
        if(debugging) {
            System.out.println(value);
        }
    }
    public boolean at_bank(){
        return RSBank.GRAND_EXCHANGE.getTile().getArea(5).contains(getAPIContext().localPlayer().getLocation());
    }
    //bank items function needs to auto travel to GE(if not there)
    public void bank_Items(){
        //check if at Grand Exchange
        if(!at_bank()){
            //if not at grand exchange walk there
            print("Not at bank");
            customWalking(RSBank.GRAND_EXCHANGE.getTile(),3);
            Time.sleep(5000,30000, this::at_bank);        }
        //player is certainly at grand exchange now
        if(!getAPIContext().bank().isOpen()){
            getAPIContext().bank().open();
            Time.sleep(1000,5000,()->getAPIContext().bank().isOpen());
        }
        //bank is certainly open now
        getAPIContext().bank().depositInventory(); //automatically closes the bank menu
        Time.sleep(1000,5000,()->getAPIContext().inventory().isEmpty());
    }
    //sell items function needs to auto travel to GE(if not there) - and bank gold.
    public void sell_Items(String item_name,int amount, int price){
        //check if at Grand Exchange
        if(!at_bank()){
            //if not at grand exchange walk there
            print("Not at bank");
            customWalking(RSBank.GRAND_EXCHANGE.getTile(),3);
            Time.sleep(5000,30000, this::at_bank);
        }
        //player is certainly at grand exchange now
        if(!getAPIContext().grandExchange().isOpen()){
            getAPIContext().grandExchange().open();
            Time.sleep(1000,10000,()->getAPIContext().grandExchange().isOpen());
        }
        //Grand exchange menu is open
        getAPIContext().grandExchange().placeSellOffer(item_name, amount, price);
        print("Placed sell offer");
        Time.sleep(1000, 30000, () -> getAPIContext().grandExchange().getSlot(0).getState() == GrandExchangeOffer.OfferState.SOLD); //wait to see if sells

        if (getAPIContext().grandExchange().getSlot(0).getState() != GrandExchangeOffer.OfferState.SOLD) { //if it didn't sell
            print("Waited, cannot collect");
            int repeats = 0;
            do {
                repeats = repeats + 1;
                print("Aborting Offer");
                getAPIContext().grandExchange().getSlot(0).abortOffer();//abort offer
                Time.sleep(1000,10000,()->getAPIContext().grandExchange().getSlot(0).getState() == GrandExchangeOffer.OfferState.CANCELLED_SELL);
                print("Done,Collecting to inventory");
                getAPIContext().grandExchange().collectToInventory();
                Time.sleep(1000,10000,()->getAPIContext().inventory().contains("Big Bones"));
                print("Done,Creating new sell offer");
                getAPIContext().grandExchange().placeSellOffer(item_name, amount, price-(repeats * 5));
                Time.sleep(1000, 30000, () -> getAPIContext().grandExchange().getSlot(0).getState() == GrandExchangeOffer.OfferState.SOLD); //wait to see if sells
            }while(getAPIContext().grandExchange().getSlot(0).getState() != GrandExchangeOffer.OfferState.SOLD);
        }
        print("Items Sold");
        getAPIContext().grandExchange().collectToInventory();
        Time.sleep(1000,10000,()->getAPIContext().inventory().getEmptySlotCount() == 27);
        gold_Collected = gold_Collected + (price * amount);
        print("Gold made:"+(price*amount)+"- Total Gold:"+gold_Collected);
        //Items have either sold or failed to sell at this point, bank everything you've got.
        if(getAPIContext().grandExchange().isOpen()){
            getAPIContext().grandExchange().close();
            Time.sleep(1000,10000,()->!getAPIContext().grandExchange().isOpen());
        }
        bank_Items();
        Time.sleep(1000,10000,()->getAPIContext().inventory().isEmpty());
    }
    //walk to safespot(Max wait time MS)
    //walk to safespot
    public void goto_Safespot(int max) {
        //find nearest safespot
        Area selected = safespot_Areas[1];
        double distance = AStarAlgorithm.distance(selected.getCentralTile().getLocation(),getAPIContext().localPlayer().getLocation(),false,getAPIContext());
        for(Area spot : safespot_Areas){
            double new_distance = AStarAlgorithm.distance(spot.getCentralTile().getLocation(),getAPIContext().localPlayer().getLocation(),false,getAPIContext());
            if(new_distance < distance){
                selected = spot;
                distance = new_distance;
            }
        }
        print("Closest safespot found");
        //select random tile in closest safespot
        Area finalSelected = selected;
        Tile rand_tile = finalSelected.getRandomTile();
        customWalking(rand_tile,2);
        //wait until at Area

        Time.sleep(1000, max, () -> finalSelected.contains(getAPIContext().localPlayer().getLocation()));
        if (!finalSelected.contains(getAPIContext().localPlayer().getLocation())) {
            print("Boneyard exit failed; reattempting");
            Tile rand_Tile = finalSelected.getRandomTile();
            customWalking(rand_Tile,2);
            //wait until at Area
            Time.sleep(1000, max, () -> finalSelected.contains(getAPIContext().localPlayer().getLocation()));
        }
        print("Arrived");
    }
    //walk to boneyard
    public void goto_Boneyard(int max){
        int area_selected = Random.nextInt(0,boneyard_Areas.length);
        Tile selected_Tile = boneyard_Areas[area_selected].getRandomTile();//select random boneyard tile to travel to
        customWalking(selected_Tile,2);
        //wait until at tile (Potential flaw if being hit while traveling)
        //Sleep until player is within 3 tiles of boneyard tile
        Time.sleep(100,1000,()->getAPIContext().localPlayer().isMoving() ); //started moving
        print("Started moving into boneyard");
        Time.sleep(100,max,()->!getAPIContext().localPlayer().isMoving() || getAPIContext().localPlayer().isInCombat());//stopped moving or interrupted by combat
        print("Arrived at random tile or interrupted");
        if(getAPIContext().localPlayer().isInCombat()||getAPIContext().localPlayer().getHealthPercent() <= hp_Min){
            goto_Safespot(10000);
            in_Safespot = true;
            in_Boneyard = false;
        }else {
            in_Safespot = false;
            in_Boneyard = true;
        }
    }
    //\\
    @Override
    protected int loop() {
        //Check if player is logged in
        if(!getAPIContext().client().isLoggedIn()){
            print("Not logged in; waiting");
            //if not logged in, wait until logged in (1-10s)
            Time.sleep(10000,45000,()->getAPIContext().client().isLoggedIn()); //Time in MS, (min,max,pointer interrupt)
            print("Now logged in");
            Time.sleep(1000,15000,()->getAPIContext().localPlayer().isMoving());
        }
        //Player is definitely logged in now

        //the player might be in the boneyard or safespot area, or standing in the GE
        boolean in_safespot = false;
        for(Area spot : safespot_Areas){
            if(spot.contains(getAPIContext().localPlayer().getLocation())){
                in_safespot = true;
            }
        }
        if(in_safespot){//if in safespot
            in_Safespot = true;
        }else{//If not in safespot check boneyards for player
            for (Area current_Area : boneyard_Areas) {//iterate through boneyard areas
                if (current_Area.contains(getAPIContext().localPlayer().getLocation())) {//if player is in this area
                    in_Boneyard = true;
                }
            }
        }
        //The players location has now been determined, if Safespot and Boneyard is false = at GE
        //If in the safespot
        if(in_Safespot){
            //player is in safespot
            print("In safespot;");
            //check why player is in safespot
            //Possibilities: Low HP, Low Stamina,Full inventory
            //Solutions: Wait,Wait,end statement will handle
            //Check if here for low HP
            if(getAPIContext().localPlayer().getHealthPercent() < hp_Max){
                print("Low HP");
                //wait until Health is above or equal to set maximum
                int max_Wait = (int)(hp_Max - getAPIContext().localPlayer().getHealthPercent());
                max_Wait = max_Wait*6000;
                Time.sleep(max_Wait,max_Wait+5000,()->getAPIContext().localPlayer().getHealthPercent() >= hp_Max);
                print("Healed");
            }
            //Check Combat Mode / Auto Retaliate
            if(getAPIContext().combat().isAutoRetaliateOn()){
                getAPIContext().combat().toggleAutoRetaliate(false);
                Time.sleep(1000,10000,()->!getAPIContext().combat().isAutoRetaliateOn());
            }
            if(getAPIContext().combat().getAttackStyle() != ICombatAPI.AttackStyle.DEFENSIVE){
                getAPIContext().combat().toggleAttackStyle(ICombatAPI.AttackStyle.DEFENSIVE);
                Time.sleep(1000,10000,()->getAPIContext().combat().getAttackStyle() == ICombatAPI.AttackStyle.DEFENSIVE);
            }
            //Definitely in defense/no auto retaliate
            //Open Inventory if not already
            openInventory();
            //at this point the player most certainly has an acceptable amount of health, travel time.
            //If the player has a full inventory, go to the grand exchange
            if(getAPIContext().inventory().isFull()){
                print("Full inventory;walking to bank!");
                customWalking(RSBank.GRAND_EXCHANGE.getTile(),3);
                //wait until player has reached their destination
                Time.sleep(60000,300000, this::at_bank);
                print("Arrived at bank");
                in_Safespot = false;
                in_Boneyard = false;
            }else{//if not the player came here for low stats, and is now ready to go to a new Boneyard tile.
                //Check if here for low stamina
                if(getAPIContext().walking().getRunEnergy() < stamina_Min){
                    print("Low Stamina");
                    //Stamina takes 7.34S or 7340 MS per to regen on average, round calculations up to 7400
                    int max_Wait = (stamina_Min - getAPIContext().walking().getRunEnergy()) * 7400;
                    Time.sleep(max_Wait,max_Wait+5000,()->getAPIContext().walking().getRunEnergy() >= stamina_Min);
                    print("Stamina regenerated");
                }
                print("Stats healed, walking to random boneyard area;");
                goto_Boneyard(8500);
            }
            //ends with walking to the safespot or the grand exchange
        }else if(in_Boneyard){
            //player is in a boneyard area
            print("In boneyard;");
            //if the player has low health go to safespot
            if(getAPIContext().localPlayer().getHealthPercent() <= hp_Min){
                print("Low HP; walking to safespot");
                //walk to safespot
                goto_Safespot(12000);
                in_Safespot = true;
                in_Boneyard = false;
            }else if(getAPIContext().walking().getRunEnergy() <= 15){//if not low health but low stamina go to safespot
                print("Low Stamina; walking to safespot");
                //walk to safespot
                goto_Safespot(12000);
                in_Safespot = true;
                in_Boneyard = false;
            }else {
                print("Looking for bones;");
                //at this point we know the player is in the boneyard, at a random area, with good health and stamina.
                //Look for bones/attempt to take (Interrupt if in combat or stopped moving)
                GroundItem bones = getAPIContext().groundItems().query().named("Big Bones").results().nearest();
                if (bones!=null) {//if AI found bones
                    int amount_bones = getAPIContext().inventory().getCount("Big Bones"); // count current amount
                    print("Found bones; waiting until stopped moving");
                    //attempt to take bones
                    Time.sleep(100,3000,()->!getAPIContext().localPlayer().isMoving()); //if still walking wait til not
                    print("Not moving, attempt to take;");
                    bones.interact("Take"); // try to take bones
                    Time.sleep(100,5000,()->getAPIContext().localPlayer().isMoving()|| getAPIContext().localPlayer().isInCombat()); //sleep until moving towards bones or interrupted
                    if(getAPIContext().localPlayer().isMoving()) { //if moving assume towards bones
                        print("Going towards bones");
                        Time.sleep(100, 7000, () -> getAPIContext().localPlayer().isInCombat() || getAPIContext().inventory().getCount("Big Bones") > amount_bones);
                        if (amount_bones < getAPIContext().inventory().getCount("Big Bones")) {
                            print("Got some bones");
                        }
                    }else if(!getAPIContext().localPlayer().isInCombat()){//if not moving assume failed to pickup bones
                        print("Not in combat, did not move");

                    }
                }
                if(!getAPIContext().localPlayer().isInCombat()) {
                    if (getAPIContext().walking().getRunEnergy() >= 15 && getAPIContext().localPlayer().getHealthPercent() >= hp_Max && !getAPIContext().inventory().isFull()) {
                        print("Inventory not full;stamina good;hp good;moving to random bone area.");
                        goto_Boneyard(8500);
                    } else {
                        //at this point the player has either found and picked up bones, or has stopped waiting because combat has been engaged.
                        //walk to safespot
                        goto_Safespot(15000);
                        in_Safespot = true;
                        in_Boneyard = false;
                    }
                }else{//interrupted because of combat
                    print("Combat Interrupt");
                    goto_Safespot(15000);
                    in_Safespot = true;
                    in_Boneyard = false;
                }
            }
        }else{
            print("At grand exchange;");
            //check players inventory
            if(!getAPIContext().inventory().isEmpty()){ //if it's not empty
                if(getAPIContext().inventory().contains("Big bones")) {//and has bones
                    if(getAPIContext().inventory().getCount("Big Bones")==28) {//if full of big bones
                        //sell items
                        print("Inventory full of bones; attempting to sell");
                        sell_Items("Big Bones",getAPIContext().inventory().getCount("Big Bones"),bone_Price);
                        print("Total gold collected:"+gold_Collected);
                    }
                }else{//players inventory isn't empty, but has no bones
                    print("Has items that arent bones; banking");
                    //bank all items
                    bank_Items();
                }
            }
            print("Grand exchange cycle done; returning to safespot");
            //player has sold and banked all items at this point and is ready to go to the safespot.
            //walk to safespot
            goto_Safespot(300000);
            in_Safespot = true;
            in_Boneyard = false;
        }
        //if the player was full he has sold/banked gold, if he was fresh off tutorial island he has banked, if he had gold he has banked
        return Random.nextInt(300, 650); //average reaction time determined randomly
    }
    @Override
    public boolean onStart(String... strings){ //startup function
        return true;
    }
}
