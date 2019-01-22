package hw2;

/**
 * Class provided for ease of test. This will not be used in the project
 * evaluation, so feel free to modify it as you like.
 */
public class Simulation {
	public static void main(String[] args) {
		int nrSellers = 50;
		int nrBidders = 20;

		Thread[] sellerThreads = new Thread[nrSellers];
		Thread[] bidderThreads = new Thread[nrBidders];
		Seller[] sellers = new Seller[nrSellers];
		Bidder[] bidders = new Bidder[nrBidders];

		// Start the sellers
		for (int i = 0; i < nrSellers; ++i) {
			sellers[i] = new Seller(AuctionServer.getInstance(), "Seller" + i, 100, 50, i);
			sellerThreads[i] = new Thread(sellers[i]);
			sellerThreads[i].start();
		}

		// add a rich man
		Bidder rich = new Bidder(AuctionServer.getInstance(), "Buyer", 10000, 20, 150, 0);
		Thread thread = new Thread(rich);
		thread.start();

		// Start the buyers
		for (int i = 0; i < nrBidders; ++i) {
			bidders[i] = new Bidder(AuctionServer.getInstance(), "Buyer" + i, 2000, 20, 150, i);
			bidderThreads[i] = new Thread(bidders[i]);
			bidderThreads[i].start();
		}

		// Join on the sellers
		for (int i = 0; i < nrSellers; ++i) {
			try {
				sellerThreads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Join on the bidders
		for (int i = 0; i < nrBidders; ++i) {
			try {
				bidderThreads[i].join();
				System.out.println(bidders[i].name() + " " + bidders[i].cash() + " " + bidders[i].cashSpent());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {
			thread.join();
			System.out.println(rich.name() + " " + rich.cash() + " " + rich.cashSpent());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// TODO: Add code as needed to debug
		AuctionServer a = AuctionServer.getInstance();
		System.out.println(a.soldItemsCount());
		System.out.println(a.getSoldCount() + " " + a.unbidCount() + " " + a.remain());
		System.out.println(a.getSoldCount() + " " + a.unbidCount() + " " + a.remain() + " $" + a.revenue());
	}
}