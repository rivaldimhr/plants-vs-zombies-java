package game;

// Daftar zombie: klik kartu untuk melihat deskripsi & stat
public class ZombiesList extends CatalogScreen {

	public ZombiesList(Game game) {
		super(game);
		addEntry(46, 117, 70, 70, "image/IMAGE/NORMAL.png");
		addEntry(141, 117, 70, 70, "image/IMAGE/DUCKY TUBE-CONE.png");
		addEntry(237, 117, 70, 70, "image/IMAGE/CONEHEAD.png");
		addEntry(331, 117, 70, 70, "image/IMAGE/POLE VAULTING.png");
		addEntry(46, 212, 70, 70, "image/IMAGE/FOOTBALL.png");
		addEntry(142, 212, 70, 70, "image/IMAGE/BUCKETHEAD.png");
		addEntry(238, 212, 70, 70, "image/IMAGE/DUCKY TUBE.png");
		addEntry(331, 212, 70, 70, "image/IMAGE/DOLPHIN RIDER.png");
		addEntry(46, 308, 70, 70, "image/IMAGE/NEWSPAPER.png");
		addEntry(141, 307, 70, 70, "image/IMAGE/SNORKEL.png");
	}

	@Override
	protected String getBackground() {
		return "image/IMAGE/ZOMBIE.png";
	}

}
