package org.broken.arrow.serialize.library.utility.serialize;

import org.broken.arrow.logging.library.Validate;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

/**
 * Utility class for retrieving class methods using reflection.
 * This class provides various methods to invoke and retrieve specific methods from a class.
 */
public class MethodReflectionUtils {

	private MethodReflectionUtils() {
	}

	/**
	 * Deserialize an object by invoking the specified static method on a class.
	 *
	 * @param <T>        The type of the deserialized object.
	 * @param clazz      The class on which the deserialization method should be invoked.
	 * @param methodName The name of the static method.
	 * @param params     Additional parameters required by the deserialization method.
	 * @return The deserialized object of type T.
	 * @throws Validate.ValidateExceptions If the static method could not be invoked.
	 */
	public static <T extends ConfigurationSerializable> T invokeStaticMethodByName(final Class<T> clazz, final String methodName, final Object... params) {
		Method method = getMethod(clazz, methodName, Map.class);
		Validate.checkNotNull(method, "This method " + methodName + " could not be found");
		return invokeStaticMethod(clazz, method, params);
	}

	/**
	 * Deserialize an object by invoking the specified static method on a class.
	 *
	 * @param <T>    The type of the deserialized object.
	 * @param clazz  The class on which the deserialization method should be invoked.
	 * @param method The method to invoke.
	 * @param params Additional parameters required by the deserialization method.
	 * @return The deserialized object of type T.
	 * @throws Validate.ValidateExceptions If the static method could not be invoked.
	 */
	public static <T extends ConfigurationSerializable> T invokeStaticMethod(final Class<T> clazz, final Method method, final Object... params) {
		if (method == null) return null;
		try {
			Validate.checkBoolean(!Modifier.isStatic(method.getModifiers()), method + " need to be static");
			return clazz.cast(method.invoke(method, params));
		} catch (final IllegalAccessException | InvocationTargetException ex) {
			throw new Validate.ValidateExceptions(ex, "Could not invoke static method " + method + " with params " + Arrays.toString(params));
		}
	}

	/**
	 * Retrieve a method by its name from a given class.
	 *
	 * @param clazz      The class from which to retrieve the method.
	 * @param methodName The name of the method to retrieve.
	 * @param args       The parameter types of the method.
	 * @return The Method object representing the desired method.
	 */
	@Nullable
	public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... args) {
		for (final Method method : clazz.getMethods())
			if (method.getName().equals(methodName) && isParameterTypesEqual(args, method.getParameterTypes())) {
				method.setAccessible(true);
				return method;
			}
		return null;
	}

	/**
	 * Check if two arrays of parameter types are equal.
	 *
	 * @param first  The first array of parameter types.
	 * @param second The second array of parameter types.
	 * @return True if the arrays are equal, false otherwise.
	 */
	public static boolean isParameterTypesEqual(final Class<?>[] first, final Class<?>[] second) {
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

