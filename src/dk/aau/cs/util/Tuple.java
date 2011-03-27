package dk.aau.cs.util;

public class Tuple<T1, T2> {
	private T1 value1;
	private T2 value2;

	public Tuple(T1 value1, T2 value2) {
		this.value1 = value1;
		this.value2 = value2;
	}

	public T1 value1() {
		return value1;
	}

	public T2 value2() {
		return value2;
	}
	
	@Override
	public String toString() {
		return value1.toString() + "," + value2.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value1 == null) ? 0 : value1.hashCode());
		result = prime * result + ((value2 == null) ? 0 : value2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Tuple<?, ?>))
			return false;
		Tuple<?, ?> other = (Tuple<?, ?>) obj;
		if (value1 == null) {
			if (other.value1 != null)
				return false;
		} else if (!value1.equals(other.value1))
			return false;
		if (value2 == null) {
			if (other.value2 != null)
				return false;
		} else if (!value2.equals(other.value2))
			return false;
		return true;
	}
}
