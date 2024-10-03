package auctionagents;

import auctionframework.AbstractAgent;
import auctionframework.Auction;
import auctionframework.AuctionItem;

import java.util.Random;

/**
 *
 * @author 
 */
public class Agent3  extends AbstractAgent {
    private double excitement = 0.5; // 0-1 tartomány: Mennyire lelkes az ágens a licitálással kapcsolatban
    private double frustration = 0.0; // 0-1 tartomány: Mennyire frusztrált az ágens a vesztések miatt
    private int consecutiveLosses = 0;
    private final Random random = new Random();
    private AuctionItem lastItemBidOn = null;
    private double baseThreshold = 0.7; // Base threshold for value ratio

    /**
     * Default constructor of the third agent.
     * 
     * @param name          Generated name of the agent.
     * @param auction       The auction the agent participates in.
     * @param money         The money the agent has.
     */
    public Agent3(String name, Auction auction, int money) {
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
        // Calculate value ratio (how good of a deal it seems)
        double valueRatio = (double)item.getStartingPrice() / item.getPrice();

        // Update emotional state
        if (lastItemBidOn != null && lastItemBidOn.getOwner() != this) {
            // Lost previous bid
            frustration += 0.2;
            consecutiveLosses++;
            excitement *= 0.8; // Excitement decreases
        } else if (lastItemBidOn != null && lastItemBidOn.getOwner() == this) {
            // Won previous bid
            frustration = Math.max(0, frustration - 0.3);
            consecutiveLosses = 0;
            excitement = Math.min(1.0, excitement + 0.2); // Excitement increases
        }

        // Normalize emotions
        frustration = Math.min(1.0, frustration);
        excitement = Math.max(0.1, excitement);

        // Desperate buying when frustrated
        if (frustration > 0.8) baseThreshold *= 0.6; // More likely to bid when frustrated

        // Excitement influences risk-taking
        if (excitement > 0.7) baseThreshold *= 0.8; // More likely to bid when excited

        // Market momentum: More likely to bid if on a winning streak
        if (consecutiveLosses == 0 && !getItems().isEmpty()) baseThreshold *= 0.9;

        // Add some randomness based on emotional state
        double randomFactor = 0.1 * (excitement + frustration) * (random.nextDouble() - 0.5);

        // Budget consideration
        double budgetRatio = (double)getMoney() / auction.getItems().size();
        boolean canAfford = getMoney() >= item.getPrice() * 1.5; // Ensure we can afford future price increases

        // Make final decision
        boolean shouldBid = valueRatio > (baseThreshold + randomFactor) && canAfford;

        // Update last item bid on
        lastItemBidOn = item;

        return shouldBid;
    }
}
