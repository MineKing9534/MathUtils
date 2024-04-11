package de.mineking.math.node;

import de.mineking.math.MathEnvironment;
import de.mineking.math.function.BinaryFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BinaryFunctionCall implements Node {
	private final BinaryFunction function;
	private final Node param1;
	private final Node param2;

	BinaryFunctionCall(@NotNull BinaryFunction function, @NotNull Node param1, @NotNull Node param2) {
		this.function = function;
		this.param1 = param1;
		this.param2 = param2;
	}

	@NotNull
	public static BinaryFunctionCall create(@NotNull BinaryFunction function, @NotNull Node param1, @NotNull Node param2) {
		return new BinaryFunctionCall(function, param1, param2);
	}

	@NotNull
	@Override
	public Node apply(@NotNull MathEnvironment environment) {
		var param1 = this.param1.apply(environment);
		var param2 = this.param2.apply(environment);

		var temp = function.simplify(environment, param1, param2);
		if (temp != null) return temp;

		return environment.choose(
				create(function, param1, param2),
				() -> param1.hasValue(environment) && param2.hasValue(environment),
				() -> function.apply(param1.value(), param2.value())
		);
	}

	@NotNull
	@Override
	public Node getDerivative(@NotNull String variable) {
		return function.getDerivative(variable, param1, param2);
	}

	@NotNull
	@Override
	public Node getIntegral(@NotNull String variable) {
		return function.getIntegral(variable, param1, param2);
	}

	@Override
	public double getDegree(@NotNull String variable) {
		return param1.getDegree(variable) == 0 && param2.getDegree(variable) == 0 ? 0 : Integer.MAX_VALUE;
	}

	@Override
	public boolean isConstant() {
		return param1.isConstant() && param2.isConstant();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BinaryFunctionCall f && f.function.equals(function) && f.param1.equals(param1) && f.param2.equals(param2);
	}

	@Override
	public int hashCode() {
		return Objects.hash("binary function", function, param1, param2);
	}

	@Override
	public String toString() {
		return function.getName() + "(" + param1 + ", " + param2 + ")";
	}

	@Override
	public int getPriority() {
		return Integer.MAX_VALUE;
	}
}
