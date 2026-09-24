package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entity.plant.PlantType;

// 6 tanaman yang dipilih pemain di Inventory, beserta cooldown tiap slot
public class Deck {
    public static final int SIZE = 6;

    private final List<PlantType> cards = new ArrayList<>();
    private final int[] cooldown = new int[SIZE]; // sisa cooldown (tick)
    private final int[] cooldownTotal = new int[SIZE]; // cooldown penuh (tick), untuk overlay kartu

    // Tambah kalau belum ada, hapus kalau sudah ada. Return false kalau deck penuh.
    public synchronized boolean toggle(PlantType type) {
        if (cards.remove(type)) {
            return true;
        }
        if (cards.size() >= SIZE) {
            return false;
        }
        cards.add(type);
        return true;
    }

    // Tukar posisi dua kartu di deck (menentukan tombol 1-6 saat bermain)
    public synchronized boolean swap(int slotA, int slotB) {
        if (!isValidSlot(slotA) || !isValidSlot(slotB)) {
            return false;
        }
        Collections.swap(cards, slotA, slotB);
        int tmp = cooldown[slotA];
        cooldown[slotA] = cooldown[slotB];
        cooldown[slotB] = tmp;
        tmp = cooldownTotal[slotA];
        cooldownTotal[slotA] = cooldownTotal[slotB];
        cooldownTotal[slotB] = tmp;
        return true;
    }

    // Ganti kartu di slot dengan tanaman lain yang belum ada di deck
    public synchronized boolean replace(int slot, PlantType type) {
        if (!isValidSlot(slot) || cards.contains(type)) {
            return false;
        }
        cards.set(slot, type);
        cooldown[slot] = 0;
        cooldownTotal[slot] = 0;
        return true;
    }

    public synchronized int indexOf(PlantType type) {
        return cards.indexOf(type);
    }

    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot < cards.size();
    }

    public synchronized boolean contains(PlantType type) {
        return cards.contains(type);
    }

    public synchronized boolean isFull() {
        return cards.size() == SIZE;
    }

    public synchronized int size() {
        return cards.size();
    }

    public synchronized PlantType get(int slot) {
        return slot >= 0 && slot < cards.size() ? cards.get(slot) : null;
    }

    public synchronized List<PlantType> getCards() {
        return Collections.unmodifiableList(new ArrayList<>(cards));
    }

    public synchronized void clear() {
        cards.clear();
        resetCooldowns();
    }

    // ------------------------------------------------------------------ cooldown

    public synchronized boolean isReady(int slot) {
        return cooldown[slot] <= 0;
    }

    public synchronized void startCooldown(int slot) {
        int ticks = cards.get(slot).getCooldown() * Game.UPS;
        cooldown[slot] = ticks;
        cooldownTotal[slot] = ticks;
    }

    // Dipanggil tiap tick
    public synchronized void tick() {
        for (int i = 0; i < SIZE; i++) {
            if (cooldown[i] > 0) {
                cooldown[i]--;
            }
        }
    }

    public synchronized void resetCooldowns() {
        for (int i = 0; i < SIZE; i++) {
            cooldown[i] = 0;
            cooldownTotal[i] = 0;
        }
    }

    // 1.0 = baru mulai cooldown, 0.0 = siap
    public synchronized double getCooldownFraction(int slot) {
        return cooldownTotal[slot] == 0 ? 0 : (double) cooldown[slot] / cooldownTotal[slot];
    }

    public synchronized int getCooldownSeconds(int slot) {
        return (cooldown[slot] + Game.UPS - 1) / Game.UPS;
    }
}
