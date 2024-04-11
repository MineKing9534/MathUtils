package de.mineking.math.node;

import de.mineking.math.MathEnvironment;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Variable implements Node {
	public final static Variable defaultVariable = create(Node.defaultVariable);

	private final String name;

	Variable(@NotNull String name) {
		this.name = name;
	}

	@NotNull
	public static Variable create(@NotNull String name) {
		return new Variable(name);
	}

	@NotNull
	@Override
	public Node apply(@NotNull MathEnvironment environment) {
		return environment.getVariables().getOrDefault(name, this);
	}

	@NotNull
	@Override
	public Node getDerivative(@NotNull String variable) {
		if (variable.equals(name)) return Constant.ONE;
		return Constant.ZERO;
	}

	@NotNull
	@Override
	public Node getIntegral(@NotNull String variable) {
		return variable.equals(name)
				? Product.create(Fraction.create(Constant.ONE, Constant.TWO), Exponent.create(this, Constant.TWO))
				: Product.create(this, create(variable));
	}

	@Override
	public double getDegree(@NotNull String variable) {
		return variable.equals(name) ? 1 : 0;
	}

	@Override
	public boolean isLinear(@NotNull String variable) {
		return variable.equals(name);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Variable v && v.name.equals(name);
	}

	@Override
	public int hashCode() {
		return Objects.hash("variable", name);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int getPriority() {
		return Integer.MAX_VALUE;
	}
}
