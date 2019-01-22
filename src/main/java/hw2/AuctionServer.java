package hw2;

/**
 *  @author Feng Huang
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AuctionServer {
	/**
	 * Singleton: the following code makes the server a Singleton. You should not
	 * edit the code in the following noted section.
	 * 
	 * For test purposes, we made the constructor protected.
	 */

	/* Singleton: Begin code that you SHOULD NOT CHANGE! */
	protected AuctionServer() {
	}

	private static AuctionServer instance = new AuctionServer();

	public static AuctionServer getInstance() {
		return instance;
	}

	/* Singleton: End code that you SHOULD NOT CHANGE! */

	/*
	 * Statistic variables and server constants: Begin code you should likely leave
	 * alone.
	 */

	/**
	 * Server statistic variables and access methods:
	 */
	private long t = System.currentTimeMillis();
	private int soldCount = 0;
	private int unbidCount = 0;
	private int revenue = 0;

	public double soldItemsCount() {
		long time = System.currentTimeMillis() - t;
		return soldCount * 100.0 / time;
	}

	
	public int getSoldCount() {
		return soldCount;
	}


	public int unbidCount() {
		return this.unbidCount;
	}

	public int revenue() {
		return this.revenue;
	}

	public int remain() {
		return itemsUpForBidding.size();
	}

	public int all() {
		return itemsAndIDs.size();
	}

	/**
	 * Server restriction constants:
	 */
	public static final int maxBidCount = 10; // The maximum number of bids at any given time for a buyer.
	public static final int maxSellerItems = 20; // The maximum number of items that a seller can submit at any given
													// time.
	public static final int serverCapacity = 80; // The maximum number of active items at a given time.

	/*
	 * Statistic variables and server constants: End code you should likely leave
	 * alone.
	 */

	/**
	 * Some variables we think will be of potential use as you implement the
	 * server...
	 */

	// List of items currently up for bidding (will eventually remove things that
	// have expired).
	private List<Item> itemsUpForBidding = new ArrayList<Item>();

	// The last value used as a listing ID. We'll assume the first thing added gets
	// a listing ID of 0.
	private int lastListingID = -1;

	// List of item IDs and actual items. This is a running list with everything
	// ever added to the auction.
	private HashMap<Integer, Item> itemsAndIDs = new HashMap<Integer, Item>();

	// List of itemIDs and the highest bid for each item. This is a running list
	// with everything ever added to the auction.
	private HashMap<Integer, Integer> highestBids = new HashMap<Integer, Integer>();

	// List of itemIDs and the person who made the highest bid for each item. This
	// is a running list with everything ever bid upon.
	private HashMap<Integer, String> highestBidders = new HashMap<Integer, String>();

	// List of sellers and how many items they have currently up for bidding.
	private HashMap<String, Integer> itemsPerSeller = new HashMap<String, Integer>();

	// List of buyers and how many items on which they are currently bidding.
	private HashMap<String, Integer> itemsPerBuyer = new HashMap<String, Integer>();

	// Object used for instance synchronization if you need to do it at some point
	// since as a good practice we don't use synchronized (this) if we are doing
	// internal
	// synchronization.
	//
	// private Object instanceLock = new Object();

	// Invariant: itemsUpForBidding.size() < serverCapacity
	// itemsPerSeller.get(sellerName) < maxSellerItems
	// itemsPerBuyer.get(bidderName) < maxBidCount

	/*
	 * The code from this point forward can and should be changed to correctly and
	 * safely implement the methods as needed to create a working multi-threaded
	 * server for the system. If you need to add Object instances here to use for
	 * locking, place a comment with them saying what they represent. Note that if
	 * they just represent one structure then you should probably be using that
	 * structure's intrinsic lock.
	 */

	/**
	 * Attempt to submit an <code>Item</code> to the auction
	 * 
	 * @param sellerName
	 *            Name of the <code>Seller</code>
	 * @param itemName
	 *            Name of the <code>Item</code>
	 * @param lowestBiddingPrice
	 *            Opening price
	 * @param biddingDurationMs
	 *            Bidding duration in milliseconds
	 * @return A positive, unique listing ID if the <code>Item</code> listed
	 *         successfully, otherwise -1
	 */
	// Precondition: lowestBiddingPrice > 0 && lowestBiddingPrice < 100
	// Postcondition:check itemsPerSeller
	// get new lastListingID and update itemsAndIDs
	// add Item to itemsUpForBidding
	public int submitItem(String sellerName, String itemName, int lowestBiddingPrice, int biddingDurationMs) {
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		// Make sure there's room in the auction site.
		// If the seller is a new one, add them to the list of sellers.
		// If the seller has too many items up for bidding, don't let them add this one.
		// Don't forget to increment the number of things the seller has currently
		// listed.
		if (lowestBiddingPrice < 0 || lowestBiddingPrice > 99)
			return -1;
		Item item;
		synchronized (itemsPerSeller) {
			if (!itemsPerSeller.containsKey(sellerName))
				itemsPerSeller.put(sellerName, 0);
			if (itemsPerSeller.get(sellerName) >= maxSellerItems)
				return -1;
			int s = itemsPerSeller.get(sellerName);
			itemsPerSeller.put(sellerName, s++);
		}
		synchronized (itemsUpForBidding) {
			if (itemsUpForBidding.size() >= serverCapacity)
				return -1;
			synchronized (itemsAndIDs) {
				lastListingID++;
				item = new Item(sellerName, itemName, lastListingID, lowestBiddingPrice, biddingDurationMs);
				itemsAndIDs.put(lastListingID, item);
				// System.out.println(lastListingID + " " + itemsAndIDs.size());
			}
			itemsUpForBidding.add(item);
		}
		System.out.println(Thread.currentThread().getName() + ": " + sellerName + " Submit " + itemName + " $"
				+ item.lowestBiddingPrice());
		return item.listingID();
	}

	/**
	 * Get all <code>Items</code> active in the auction
	 * 
	 * @return A copy of the <code>List</code> of <code>Items</code>
	 */
	// Precondition: none
	// Postcondition: copy itemsUpForBidding
	public List<Item> getItems() {
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		// Don't forget that whatever you return is now outside of your control.
		List<Item> items = new ArrayList<>();
		List<Item> unbid = new ArrayList<>();
		synchronized (itemsUpForBidding) {
			for (Item item : itemsUpForBidding) {
				if (item.biddingOpen())
					items.add(item);
				else if (itemUnbid(item.listingID())) {
					unbid.add(item);
				}
			}
			for (Item item : unbid) {
				unbidCount++;
				itemsUpForBidding.remove(item);
				synchronized (itemsPerSeller) {
					String seller = item.seller();
					int s = itemsPerSeller.get(seller);
					itemsPerSeller.put(seller, s--);
				}
				System.out.println(Thread.currentThread().getName() + ": " + item.name() + " end up unbid");
			}
		}
		return items;
	}

	/**
	 * Attempt to submit a bid for an <code>Item</code>
	 * 
	 * @param bidderName
	 *            Name of the <code>Bidder</code>
	 * @param listingID
	 *            Unique ID of the <code>Item</code>
	 * @param biddingAmount
	 *            Total amount to bid
	 * @return True if successfully bid, false otherwise
	 */
	// Precondition: biddingAmount > itemPrice(listingID)
	// Postcondition:check itemsAndIDs, itemsUpForBidding, itemsPerBuyer
	// check highestBidders
	// update highestBidders, highestBids, itemsPerBuyer
	public boolean submitBid(String bidderName, int listingID, int biddingAmount) {
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		// See if the item exists.
		// See if it can be bid upon.
		// See if this bidder has too many items in their bidding list.
		// Get current bidding info.
		// See if they already hold the highest bid.
		// See if the new bid isn't better than the existing/opening bid floor.
		// Decrement the former winning bidder's count
		// Put your bid in place
		Item item;
		synchronized (itemsAndIDs) {
			if (!itemsAndIDs.containsKey(listingID))
				return false;
			item = itemsAndIDs.get(listingID);
		}
		synchronized (itemsPerBuyer) {
			if (!itemsPerBuyer.containsKey(bidderName))
				itemsPerBuyer.put(bidderName, 0);
			if (itemsPerBuyer.get(bidderName) >= maxBidCount)
				return false;
			if (!item.biddingOpen())
				return false;
			synchronized (highestBidders) {
				if (!itemUnbid(listingID)) {
					String formerBidder = highestBidders.get(listingID);
					if (highestBids.get(listingID) >= biddingAmount || bidderName.equals(formerBidder)) {
						return false;
					}
					int s = itemsPerBuyer.get(formerBidder);
					itemsPerBuyer.put(formerBidder, s--);
				}
				highestBidders.put(listingID, bidderName);
				highestBids.put(listingID, biddingAmount);
			}
			int s = itemsPerBuyer.get(bidderName);
			itemsPerBuyer.put(bidderName, s++);
			System.out.println(Thread.currentThread().getName() + ": " + bidderName + " bid $" + biddingAmount + " for "
					+ item.name());
			return true;
		}
	}

	/**
	 * Check the status of a <code>Bidder</code>'s bid on an <code>Item</code>
	 * 
	 * @param bidderName
	 *            Name of <code>Bidder</code>
	 * @param listingID
	 *            Unique ID of the <code>Item</code>
	 * @return 1 (success) if bid is over and this <code>Bidder</code> has won<br>
	 *         2 (open) if this <code>Item</code> is still up for auction<br>
	 *         3 (failed) If this <code>Bidder</code> did not win or the
	 *         <code>Item</code> does not exist
	 */
	// Precondition: none
	// Postcondition: check itemsAndIDs, itemsUpForBidding
	// if(!biddingOpen()) remove itemsUpForBidding and update itemsPerSeller,
	// itemsPerBuyer
	public int checkBidStatus(String bidderName, int listingID) {
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		// If the bidding is closed, clean up for that item.
		// Remove item from the list of things up for bidding.
		// Decrease the count of items being bid on by the winning bidder if there was
		// any...
		// Update the number of open bids for this seller
		Item item;
		synchronized (itemsAndIDs) {
			if (!itemsAndIDs.containsKey(listingID))
				return 3;
			item = itemsAndIDs.get(listingID);
		}
		if (item.biddingOpen())
			return 2;
		String winner;
		synchronized (highestBidders) {
			winner = highestBidders.get(listingID);
		}
		synchronized (itemsUpForBidding) {
			if (itemsUpForBidding.contains(item)) {
				soldCount++;
				revenue += highestBids.get(listingID);
				itemsUpForBidding.remove(item);
				synchronized (itemsPerSeller) {
					String seller = item.seller();
					int s = itemsPerSeller.get(seller);
					itemsPerSeller.put(seller, s--);
				}
				synchronized (itemsPerBuyer) {
					int s = itemsPerBuyer.get(winner);
					itemsPerBuyer.put(winner, s--);
				}
			}
		}
		if (winner.equals(bidderName)) {
			System.out.println(Thread.currentThread().getName() + ": " + winner + " pay $" + highestBids.get(listingID)
					+ " for " + item.name());
			return 1;
		} else {
			System.out.println(Thread.currentThread().getName() + ": " + bidderName + " lost " + item.name());
			return 3;
		}

	}

	/**
	 * Check the current bid for an <code>Item</code>
	 * 
	 * @param listingID
	 *            Unique ID of the <code>Item</code>
	 * @return The highest bid so far or the opening price if no bid has been made,
	 *         -1 if no <code>Item</code> exists
	 */
	// Precondition: none
	// Postcondition: check itemsAndIDs, highestBids
	// get price from highestBids
	public int itemPrice(int listingID) {
		// TODO: IMPLEMENT CODE HERE
		Item item;
		synchronized (itemsAndIDs) {
			if (!itemsAndIDs.containsKey(listingID))
				return -1;
			item = itemsAndIDs.get(listingID);
		}
		if (itemUnbid(listingID))
			return item.lowestBiddingPrice() - 1;
		synchronized (highestBidders) {
			return highestBids.get(listingID);
		}
	}

	/**
	 * Check whether an <code>Item</code> has been bid upon yet
	 * 
	 * @param listingID
	 *            Unique ID of the <code>Item</code>
	 * @return True if there is no bid or the <code>Item</code> does not exist,
	 *         false otherwise
	 */
	// Precondition: none
	// Postcondition: check highestBids
	public Boolean itemUnbid(int listingID) {
		// TODO: IMPLEMENT CODE HERE
		synchronized (itemsAndIDs) {
			if (!itemsAndIDs.containsKey(listingID))
				return true;
		}
		synchronized (highestBidders) {
			if (!highestBidders.containsKey(listingID))
				return true;
		}
		return false;
	}

}
