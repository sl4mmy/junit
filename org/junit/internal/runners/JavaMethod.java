/**
 * 
 */
package org.junit.internal.runners;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.Test.None;

public class JavaMethod extends JavaModelElement {
	// TODO: push out
	private final Method fMethod;

	public JavaMethod(Method current) {
		fMethod= current;
	}

	private boolean isShadowedBy(JavaMethod previousJavaMethod) {
		Method previous= previousJavaMethod.fMethod;
		if (!previous.getName().equals(fMethod.getName()))
			return false;
		if (previous.getParameterTypes().length != fMethod
				.getParameterTypes().length)
			return false;
		for (int i= 0; i < previous.getParameterTypes().length; i++) {
			if (!previous.getParameterTypes()[i].equals(fMethod
					.getParameterTypes()[i]))
				return false;
		}
		return true;
	}

	boolean isShadowedBy(List<JavaMethod> results) {
		for (JavaMethod each : results) {
			if (isShadowedBy(each))
				return true;
		}
		return false;
	}

	public Annotation getAnnotation(MethodAnnotation methodAnnotation) {
		return fMethod.getAnnotation(methodAnnotation.getAnnotationClass());
	}

	boolean isIgnored() {
		return fMethod.getAnnotation(Ignore.class) != null;
	}

	long getTimeout() {
		return getTestAnnotation().timeout();
	}

	private Test getTestAnnotation() {
		return fMethod.getAnnotation(Test.class);
	}

	Class<? extends Throwable> expectedException() {
		Test annotation= getTestAnnotation();
		if (annotation.expected() == None.class)
			return null;
		else
			return annotation.expected();
	}

	boolean isUnexpected(Throwable exception) {
		return ! expectedException().isAssignableFrom(exception.getClass());
	}

	boolean expectsException() {
		return expectedException() != null;
	}

	void invoke(Object object) throws IllegalAccessException,
			InvocationTargetException {
		fMethod.invoke(object);
	}

	@Override
	public String getName() {
		return fMethod.getName();
	}

	void validateAsTestMethod(boolean isStatic, List<Throwable> errors) {
		Method each= fMethod;
		if (Modifier.isStatic(each.getModifiers()) != isStatic) {
			String state= isStatic ? "should" : "should not";
			errors.add(new Exception("Method " + each.getName() + "() "
					+ state + " be static"));
		}
		if (!Modifier.isPublic(each.getDeclaringClass().getModifiers()))
			errors.add(new Exception("Class " + each.getDeclaringClass().getName()
					+ " should be public"));
		if (!Modifier.isPublic(each.getModifiers()))
			errors.add(new Exception("Method " + each.getName()
					+ " should be public"));
		if (each.getReturnType() != Void.TYPE)
			errors.add(new Exception("Method " + each.getName()
					+ " should be void"));
		if (each.getParameterTypes().length != 0)
			errors.add(new Exception("Method " + each.getName()
					+ " should have no parameters"));
	}

	void runUnprotected(JavaTestInterpreter javaTestInterpreter, Object test, PerTestNotifier perTestNotifier) {
		try {
			javaTestInterpreter.executeMethodBody(test, this);
			if (expectsException())
				perTestNotifier.addFailure(new AssertionError(
						"Expected exception: "
								+ expectedException().getName()));
		} catch (InvocationTargetException e) {
			Throwable actual= e.getTargetException();
			if (!expectsException())
				perTestNotifier.addFailure(actual);
			else if (isUnexpected(actual)) {
				String message= "Unexpected exception, expected<"
						+ expectedException().getName()
						+ "> but was<" + actual.getClass().getName() + ">";
				perTestNotifier.addFailure(new Exception(message, actual));
			}
		} catch (Throwable e) {
			perTestNotifier.addFailure(e);
		}
	}
}