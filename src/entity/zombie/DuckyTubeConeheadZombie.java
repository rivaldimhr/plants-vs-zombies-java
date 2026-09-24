package entity.zombie;

// Conehead dengan pelampung bebek (cone 150 HP), hanya di kolam
public class DuckyTubeConeheadZombie extends Zombie {

    public DuckyTubeConeheadZombie(int x, int y) {
        super("Ducky Tube Conehead Zombie", 100, true, 100, 1, x, y, "duckytube");
        setArmor(new Armor(Armor.Kind.CONE, 150, "duckytube_cone"));
    }

}
