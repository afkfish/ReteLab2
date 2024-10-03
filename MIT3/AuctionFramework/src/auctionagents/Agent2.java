package auctionagents;

import auctionframework.AbstractAgent;
import auctionframework.Auction;
import auctionframework.AuctionItem;

import java.util.Random;

/**
 *
 * @author 
 */
public class Agent2  extends AbstractAgent {

    /**
     * Default constructor of the second agent.
     * 
     * @param name          Generated name of the agent.
     * @param auction       The auction the agent participates in.
     * @param money         The money the agent has.
     */
    public Agent2(String name, Auction auction, int money) {
        super(name, auction, money);
    }
    
    /**
     * The return value of this function indicates if the agent bids for the 
     * actual item in the auction.
     * 
     * @param item          The item to bid for
     * @return              True if the agent bids for the current price.
     */
    public boolean ask(AuctionItem item) {
        // The agent bids if the other agents have more than 1/3 of the items, and the price is no more than randomly 100-200% of the starting price.
        int itemsSold = auction.getItems().indexOf(item);
//        int agentItemsCount = auction.getParticipants().stream().map(agent -> agent.getItems().size()).reduce(0, Integer::sum);
        return item.getPrice() < item.getStartingPrice() * (new Random().nextFloat() + 1) && itemsSold > auction.getItems().size() / 3;
    }
}
