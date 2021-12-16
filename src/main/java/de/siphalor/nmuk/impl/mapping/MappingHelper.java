package de.siphalor.nmuk.impl.mapping;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Function;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.Type;

import de.siphalor.nmuk.impl.NMUK;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class MappingHelper {

	public static final MappingResolver MAPPING_RESOLVER = FabricLoader.getInstance().getMappingResolver();

	public static final String NAMESPACE_NAMED = "named";
	public static final String NAMESPACE_INTERMEDIARY = "intermediary";

	public static final Function<String, String> CLASS_MAPPER_FUNCTION = (className) -> MAPPING_RESOLVER.mapClassName(NAMESPACE_INTERMEDIARY, className);
	public static final Function<String, String> CLASS_UNMAPPER_FUNCTION = (className) -> MAPPING_RESOLVER.unmapClassName(NAMESPACE_INTERMEDIARY, className);

	public static Class<?> getClassFromType(Type type, Function<String, String> remapFunction) throws ClassNotFoundException {
		if (type == Type.INT_TYPE) {
			return Integer.TYPE;
		} else if (type == Type.VOID_TYPE) {
			return Void.TYPE;
		} else if (type == Type.BOOLEAN_TYPE) {
			return Boolean.TYPE;
		} else if (type == Type.BYTE_TYPE) {
			return Byte.TYPE;
		} else if (type == Type.CHAR_TYPE) {
			return Character.TYPE;
		} else if (type == Type.SHORT_TYPE) {
			return Short.TYPE;
		} else if (type == Type.DOUBLE_TYPE) {
			return Double.TYPE;
		} else if (type == Type.FLOAT_TYPE) {
			return Float.TYPE;
		} else if (type == Type.LONG_TYPE) {
			return Long.TYPE;
		} else if (type.getSort() >= Type.VOID && type.getSort() <= Type.DOUBLE) {
			// it is a primitive but it is non of the known instances?!?
			throw new AssertionError();
		} else if (type.getSort() == Type.ARRAY) {
			Class<?> elementClass = getClassFromType(type.getElementType(), remapFunction);
			for (int i = type.getDimensions(); i > 0; i--) {
				elementClass = Array.newInstance(elementClass, 0).getClass();
			}
			return elementClass;
		} else if (type.getSort() == Type.OBJECT) {
			String className = type.getClassName();
			if (remapFunction != null) {
				className = remapFunction.apply(className);
			}
			return Class.forName(className);
		} else {
			throw new AssertionError();
		}
	}

	public static String createSignature(String signatureFormat, Class<?>... formatFillClasses) {
		Object[] args = new Object[formatFillClasses.length];
		for (int i = 0; i < formatFillClasses.length; i++) {
			args[i] = Type.getType(formatFillClasses[i]);
		}
		return String.format(signatureFormat, args);
	}

	public static Class<?>[] getArgumentTypesFromSignature(String signature) throws ClassNotFoundException {
		Type[] methodArgumentTypes = Type.getArgumentTypes(signature);
		Class<?>[] ret = new Class<?>[methodArgumentTypes.length];
		for (int i = 0; i < methodArgumentTypes.length; i++) {
			ret[i] = getClassFromType(methodArgumentTypes[i], null);
		}
		return ret;
	}

	public static Class<?> mapAndLoadClass(String className, Function<String, String> remapFunction) {
		String mappedClass = remapFunction.apply(className);
		return loadClass(mappedClass);
	}

	public static Class<?> loadClass(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			NMUK.log(Level.ERROR, "Failed to load class \"" + className + "\"");
			e.printStackTrace();
		}
		return null;
	}

	public static String mapMethod(String className, String methodIntermediary, String signature) {
		String unmappedClass = MAPPING_RESOLVER.unmapClassName(NAMESPACE_INTERMEDIARY, className);
		return MAPPING_RESOLVER.mapMethodName(NAMESPACE_INTERMEDIARY, unmappedClass, methodIntermediary, signature);
	}

	public static Method getMethod(Class<?> clazz, String remappedMethodName, String methodSignature) {
		try {
			Class<?>[] methodArgumentTypes = MappingHelper.getArgumentTypesFromSignature(methodSignature);
			Method method = clazz.getDeclaredMethod(remappedMethodName, methodArgumentTypes);
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			NMUK.log(Level.ERROR, "Failed to load method \"" + remappedMethodName + "\" from class \"" + clazz.getSimpleName() + "\" with reflection");
			e.printStackTrace();
			return null;
		}
	}

	public static Constructor<?> getConstructor(Class<?> clazz, String constructorSignature) {
		try {
			Class<?>[] methodArgumentTypes = MappingHelper.getArgumentTypesFromSignature(constructorSignature);
			Constructor<?> contructor = clazz.getDeclaredConstructor(methodArgumentTypes);
			contructor.setAccessible(true);
			return contructor;
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			NMUK.log(Level.ERROR, "Failed to load constructor from class \"" + clazz.getSimpleName() + "\" with reflection");
			e.printStackTrace();
			return null;
		}
	}
}
