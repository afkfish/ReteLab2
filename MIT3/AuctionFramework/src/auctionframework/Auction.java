package auctionframework;

import auctionagents.Agent1;
import auctionagents.Agent2;
import auctionagents.Agent3;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  Auction class aggregating all items and maintaining the state of the auction
 */
public class Auction {
    // Items offered in the auction
    ArrayList<AuctionItem> items;
    
    // The index of the item currently on sale 
    int activeItemIndex = -1;
    
    // The current round ended an the next item should be offered
    boolean nextItem = true;
    
    // List of agents participating in the auction
    ArrayList<AbstractAgent> participants;
    
    // List of agents participating in the current round
    ArrayList<AbstractAgent> roundParticipants;
    
    // The previous price of the item currently offered
    int previousPrice = 0;
    
    // Active bidders in the previous round
    ArrayList<AbstractAgent> previousRoundParticipants;
    
    /**
     * Construct a new auction with the given parameters.
     * 
     * @param fileName              Auction catalo file name
     * @param agent1                Number of Agent1 participants
     * @param agent2                Number of Agent2 participants
     * @param agent3                Number of Agent3 participants
     * @param moneyMultiplier       The ratio of money each agent has at the 
     *                              beginning compared to the total value of all
     *                              the items offered.
     */
    public Auction (String fileName, int agent1, int agent2, int agent3, float moneyMultiplier) {
        // Load auction items from file
        items = new ArrayList<AuctionItem>();
        int totalItemValue = 0;
        
        try {
            // Read the whole content of the catalog file
            List<String> lines = Files.readAllLines(Paths.get("auctions/"+fileName), StandardCharsets.UTF_8);
            
            // Process each line
            for (int i=0; i<lines.size(); i++) {
                // Detect tokens separated by TAB
                String[] tokens = lines.get(i).split("\t");
                if (tokens.length>2) {
                    try {
                        // Register new item for the auction.
                        int itemValue = Integer.valueOf(tokens[2]);
                        totalItemValue += itemValue;
                        AuctionItem newItem = new AuctionItem(tokens[0],tokens[1], itemValue);
                        items.add(newItem);
                    } catch (Exception e) {
                        AuctionFramework.log("Exception ("+e.getMessage()+") while parsing item line: "+lines.get(i));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
                
        // Create participatin agents
        int agentStartingMoney = (int)Math.round(totalItemValue * moneyMultiplier);
        participants = new ArrayList<AbstractAgent>();
        for (int i=0; i<agent1; i++) participants.add(new Agent1( "Agent1_"+(i+1), this, agentStartingMoney));
        for (int i=0; i<agent2; i++) participants.add(new Agent2( "Agent2_"+(i+1), this, agentStartingMoney));
        for (int i=0; i<agent3; i++) participants.add(new Agent3( "Agent3_"+(i+1), this, agentStartingMoney));
        
        // Init participant GUIs
        AuctionFramework.gui.reset(this);
        for (int i=0; i<participants.size(); i++) {
            AuctionFramework.gui.addParticipantPanel(participants.get(i).getDisplayPanel());
        }
        
        // Clear round and previous round participant lists
        roundParticipants = new ArrayList<>();
        previousRoundParticipants = new ArrayList<>();
    }
    
    /**
     * Execute one step in the auction.
     * 
     * @return          Returns false if the auction ends.
     */
    public boolean step() {
        // Select next item for sale if necessary
        if (activeItemIndex == -1 || nextItem) {
            activeItemIndex++;
            nextItem = false;
        }
        
        // Indicate auction end when no items left to offer
        if (activeItemIndex>= items.size()) return false;
        
        // Select the active item
        AuctionItem item = items.get(activeItemIndex);
        item.setStatus(1);
        if (item.getPrice() == 0) {            
            // New item to bid for
            item.setPrice(item.getStartingPrice());
            AuctionFramework.log("Next item is '"+item.getName()+"' for starting price "+item.getPrice()+".");
            
            // Add everybody to the participant list
            roundParticipants.clear();
            previousRoundParticipants.clear();           
        } else {
            // The bidding for the item is already active, it is time to increase the price
            item.setPrice(item.getPrice()+AuctionFramework.priceIncreseStep);
        }
        
        // Ask all agents with sufficient budget
        roundParticipants.clear();
        for (int i=0; i<participants.size(); i++) {
            AbstractAgent participant = participants.get(i);
            
            // Check if the agent has enoug money
            if (participant.getMoney()>=item.getPrice()) {
                
                // Ask the agent if it wants to bid
                if (participant.ask(item)) {
                    // The agent bids
                    roundParticipants.add(participant);
                    participant.setState(2);
                } else {
                    // The agent skips
                    participant.setState(1);
                }
            } else {
                // The agent has insufficient funds
                participant.setState(0);
            }
        }
        AuctionFramework.log("Price "+item.getPrice()+": "+roundParticipants.size()+" agents active.");
        
        // Analyze agent answers
        AbstractAgent winner = null;
        int price = -1;
        if (roundParticipants.isEmpty()) {
            // No agent bids in this round
            
            if (previousRoundParticipants.size()>0) {
                // The previous round has active bidders, let's select a random winner
                Collections.shuffle(previousRoundParticipants);
                winner = previousRoundParticipants.get(0);
                price = previousPrice;
            } else {
                // No prevoius roudn bidders, thus no agent is interesed.
                AuctionFramework.log("No agent is interested in this item.");
                item.setStatus(0);
            }
            
            // Move on to the next item
            nextItem = true;           
            
        } else if (roundParticipants.size() == 1) {
            // Only one agents bids and thus wins.
            winner = roundParticipants.get(0);
            price = item.getPrice();
            nextItem = true;
            
        } else {
            // More than one active bidders, thus new round is needed
            nextItem = false;
            
            // Saving the active bidders of the actual round
            previousPrice = item.getPrice();
            previousRoundParticipants.clear();
            previousRoundParticipants.addAll(roundParticipants);
        }
        
        // Look for a winner
        if (winner != null) {
            // There is a winner in this round
            item.setPrice(price);
            AuctionFramework.log("Agent "+winner.getName()+" wins with "+price+" bid.");
            item.setStatus(2);
            winner.addItem(item);

            // Update GUI chart
            AuctionFramework.gui.addChartStep(activeItemIndex,this);
        }
        
        // Update the whole GUI
        AuctionFramework.updateGUI();
        
        // There are more rounds to exectue or more items to sell
        return true;
    }
    
    /**
     * Returns the active item for sale. 
     * @return              The active item for sale.
     */
    public AuctionItem getActiveItem() {
        if (activeItemIndex>-1 && activeItemIndex<items.size())
            return items.get(activeItemIndex);
        return null;
    }
    
    /**
     * Return all items offered for sale.
     *  
     * @return              List of all items offered for sale.
     */
    public ArrayList<AuctionItem> getItems() {
        return items;
    }

    /**
     * Returns all agents participating in this auction.
     * 
     * @return              Agents participating in this auction.
     */
    public ArrayList<AbstractAgent> getParticipants() {
        return participants;
    }
    
}
