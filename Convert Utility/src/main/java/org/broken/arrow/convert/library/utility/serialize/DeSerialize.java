package org.broken.arrow.convert.library.utility.serialize;

import org.broken.arrow.convert.library.utility.Validate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

public class DeSerialize {


	public <T extends ConfigurationSerializable> T invokeDeSerializeMethod(final Class<T> clazz, final String methodName, final Object... params) {
		if (methodName == null) return null;
		try {
			Method method = getMethod(clazz, methodName, Map.class);

			Validate.checkBoolean(!Modifier.isStatic(method.getModifiers()), "deserialize method need to be static");
			return clazz.cast(method.invoke(method, params));
		} catch (final IllegalAccessException | InvocationTargetException ex) {
			throw new Validate.CatchExceptions(ex, "Could not invoke static method " + methodName + " with params " + Arrays.toString(params));
		}
	}

	public Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... args) {
		for (final Method method : clazz.getMethods())
			if (method.getName().equals(methodName) && isClassListEqual(args, method.getParameterTypes())) {
				method.setAccessible(true);
				return method;
			}

		return null;
	}

	private boolean isClassListEqual(final Class<?>[] first, final Class<?>[] second) {
		if (first.length != second.length) {
			return false;
		} else {
			for (int i = 0; i < first.length; ++i) {
				if (first[i] != second[i]) {
					return false;
				}
			}

			return true;
		}
	}
}

