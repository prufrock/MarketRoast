package com.dkanen.MarketRoast;

/**
 * Runs the application from the command line.
 *
 * @author David Kanenwisher
 */
public class Main {

    /**
     * Just add a few required parameters, and try the service
     * Get Report functionality
     *
     * @param args unused
     */
    public static void main(String... args) {
        MarketRoast app = new MarketRoast("marketroast.properties");
        app.run();
    }
}
