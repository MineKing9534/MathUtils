package de.mineking.math.function;

import de.mineking.math.MathEnvironment;
import de.mineking.math.node.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleBinaryOperator;

public interface BinaryFunction {
	@NotNull
	String getName();

	double apply(double param1, double param2);

	@Nullable
	default Node simplify(@NotNull MathEnvironment environment, @NotNull Node param1, @NotNull Node param2) {
		return null;
	}

	@NotNull
	default Node getDerivative(@NotNull String variable, @NotNull Node param1, @NotNull Node param2) {
		throw new UnsupportedOperationException();
	}

	@NotNull
	default Node getIntegral(@NotNull String variable, @NotNull Node param1, @NotNull Node param2) {
		throw new UnsupportedOperationException();
	}

	@NotNull
	static BinaryFunction create(@NotNull String name, @NotNull DoubleBinaryOperator function, @Nullable BinarySimplificationFunction simplifier, @Nullable BinaryFunctionOperator derivative, @Nullable BinaryFunctionOperator integral) {
		return new BinaryFunction() {
			@NotNull
			@Override
			public String getName() {
				return name;
			}

			@Override
			public double apply(double param1, double param2) {
				return function.applyAsDouble(param1, param2);
			}

			@Nullable
			@Override
			public Node simplify(@NotNull MathEnvironment environment, @NotNull Node param1, @NotNull Node param2) {
				if (simplifier != null) return simplifier.simplify(environment, param1, param2);
				return null;
			}

			@NotNull
			@Override
			public Node getDerivative(@NotNull String variable, @NotNull Node param1, @NotNull Node param2) {
				if (derivative != null) return derivative.apply(variable, param1, param2);
				return BinaryFunction.super.getDerivative(variable, param1, param2);
			}

			@NotNull
			@Override
			public Node getIntegral(@NotNull String variable, @NotNull Node param1, @NotNull Node param2) {
				if (integral != null) return integral.apply(variable, param1, param2);
				return BinaryFunction.super.getIntegral(variable, param1, param2);
			}

			@Override
			public boolean equals(Object obj) {
				return obj instanceof BinaryFunction f && f.getName().equals(getName());
			}

			@Override
			public int hashCode() {
				return name.hashCode();
			}
		};
	}

	interface BinarySimplificationFunction {
		@Nullable
		Node simplify(@NotNull MathEnvironment environment, @NotNull Node param1, @NotNull Node param2);
	}

	interface BinaryFunctionOperator {
		@NotNull
		Node apply(@NotNull String variable, @NotNull Node param1, @NotNull Node param2);
	}
}
