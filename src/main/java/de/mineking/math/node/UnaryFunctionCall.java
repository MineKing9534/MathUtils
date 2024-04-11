package de.mineking.math.node;

import de.mineking.math.MathEnvironment;
import de.mineking.math.function.UnaryFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UnaryFunctionCall implements Node {
	private final UnaryFunction function;
	private final Node param;

	UnaryFunctionCall(@NotNull UnaryFunction function, @NotNull Node param) {
		this.function = function;
		this.param = param;
	}

	@NotNull
	public static UnaryFunctionCall create(@NotNull UnaryFunction function, @NotNull Node param) {
		return new UnaryFunctionCall(function, param);
	}

	@NotNull
	@Override
	public Node apply(@NotNull MathEnvironment environment) {
		var param = this.param.apply(environment);

		var temp = function.simplify(environment, param);
		if (temp != null) return temp;

		return environment.choose(
				create(function, param),
				() -> param.hasValue(environment),
				() -> function.apply(param.value())
		);
	}

	@NotNull
	@Override
	public Node getDerivative(@NotNull String variable) {
		return Product.create(
				function.getDerivative(variable, param),
				param.getDerivative(variable)
		);
	}

	@NotNull
	@Override
	public Node getIntegral(@NotNull String variable) {
		if (param.isLinear(variable)) {
			return Fraction.create(
					function.getIntegral(variable, param),
					param.getDerivative(variable)
			);
		}

		throw new UnsupportedOperationException();
	}

	@Override
	public double getDegree(@NotNull String variable) {
		return param.getDegree(variable) == 0 ? 0 : Integer.MAX_VALUE;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof UnaryFunctionCall u && u.function.equals(function) && u.param.equals(param);
	}

	@Override
	public int hashCode() {
		return Objects.hash("unary function", function, param);
	}

	@Override
	public String toString() {
		return function.getName() + "(" + param + ")";
	}

	@Override
	public int getPriority() {
		return Integer.MAX_VALUE;
	}
}
