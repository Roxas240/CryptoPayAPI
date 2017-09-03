package com.xemplar.games.cross.cryptominer;

/**
 * Created by Rohan on 8/21/2017.
 */
public interface MoneyListener {
    void addMoney(long amount);
    void buyMiner(String name);
}
