package dk.aau.cs.model.tapn;

public interface Bound {
	int value();

	Bound copy();
	boolean equals(Object other);
	int hashCode();
	
	Bound Infinity = new InfBound();

	class InfBound implements Bound {
		public int value() {
			return -1;
		}

		@Override
		public String toString() {
			return "inf";
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object arg0) {
			return arg0 instanceof InfBound;
		}

		public Bound copy() {
			return new InfBound();
		}

	}
}
