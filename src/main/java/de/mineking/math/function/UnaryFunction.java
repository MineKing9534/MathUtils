package de.mineking.math.function;

import de.mineking.math.MathEnvironment;
import de.mineking.math.node.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleUnaryOperator;

public interface UnaryFunction {
	@NotNull
	String getName();

	double apply(double param);

	@Nullable
	default Node simplify(@NotNull MathEnvironment environment, @NotNull Node param) {
		return null;
	}

	@NotNull
	default Node getDerivative(@NotNull String variable, @NotNull Node param) {
		throw new UnsupportedOperationException();
	}

	@NotNull
	default Node getIntegral(@NotNull String variable, @NotNull Node param) {
		throw new UnsupportedOperationException();
	}

	@NotNull
	static UnaryFunction create(@NotNull String name, @NotNull DoubleUnaryOperator function, @Nullable UnarySimplificationFunction simplifier, @Nullable UnaryFunctionOperator derivative, @Nullable UnaryFunctionOperator integral) {
		return new UnaryFunction() {
			@NotNull
			@Override
			public String getName() {
				return name;
			}

			@Override
			public double apply(double param) {
				return function.applyAsDouble(param);
			}

			@Nullable
			@Override
			public Node simplify(@NotNull MathEnvironment environment, @NotNull Node param) {
				if (simplifier != null) return simplifier.simplify(environment, param);
				return null;
			}

			@NotNull
			public Node getDerivative(@NotNull String variable, @NotNull Node param) {
				if (derivative != null) return derivative.apply(variable, param);
				return UnaryFunction.super.getDerivative(variable, param);
			}

			@NotNull
			@Override
			public Node getIntegral(@NotNull String variable, @NotNull Node param) {
				if (integral != null) return integral.apply(variable, param);
				return UnaryFunction.super.getIntegral(variable, param);
			}

			@Override
			public boolean equals(Object obj) {
				return obj instanceof UnaryFunction f && f.getName().equals(getName());
			}

			@Override
			public int hashCode() {
				return name.hashCode();
			}
		};
	}

	interface UnarySimplificationFunction {
		@Nullable
		Node simplify(@NotNull MathEnvironment environment, @NotNull Node param);
	}

	interface UnaryFunctionOperator {
		@NotNull
		Node apply(@NotNull String variable, @NotNull Node param);
	}
}
