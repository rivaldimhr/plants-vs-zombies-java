package game;

import java.util.List;

import entity.zombie.ZombieType;

// Satu gelombang zombie. flag = huge wave (ditandai bendera di progress bar)
public record Wave(List<ZombieType> zombies, boolean flag) {
}
