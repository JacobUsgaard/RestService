package usgaard.jacob.rest;

public class DefaultTypeGenerator implements TypeGenerator {

	@Override
	public Object generateType(Class<?> clazz, Object object) {
		if (clazz == null || object == null) {
			return null;
		}
		Class<?> type = clazz.getClass();

		if (type.equals(String.class)) {
			return object.toString();
		}

		if (type.equals(Integer.class) || type.equals(int.class)) {
			return new Integer(object.toString());
		}

		if (type.equals(Long.class) || type.equals(long.class)) {
			return new Long(object.toString());
		}

		if (type.equals(Double.class) || type.equals(double.class)) {
			return new Double(object.toString());
		}

		if (type.equals(Float.class) || type.equals(float.class)) {
			return new Float(object.toString());
		}

		if (type.equals(object.getClass())) {
			return object;
		}

		return null;
	}

}
