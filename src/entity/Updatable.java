package entity;

import game.Board;

/**
 * Objek yang di-update sekali tiap tick (60x per detik) oleh Board.
 */
public interface Updatable {
    void update(Board board);
}
