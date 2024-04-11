package de.mineking.math.node;

import de.mineking.math.MathEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public interface Node {
	String defaultVariable = "x";

	@NotNull
	Node apply(@NotNull MathEnvironment environment);

	@NotNull
	default Node negate() {
		return Product.create(Constant.NEGATIVE, this);
	}

	default int getPriority() {
		return 0;
	}

	@NotNull
	default String stringWithParentheses(boolean parentheses) {
		return (parentheses ? "(" : "") + this + (parentheses ? ")" : "");
	}

	@NotNull
	default String stringWithParentheses(int priority) {
		return stringWithParentheses(priority > getPriority());
	}

	@NotNull
	default Node getDerivative(@NotNull String variable) {
		throw new UnsupportedOperationException();
	}

	@NotNull
	default Node getIntegral(@NotNull String variable) {
		throw new UnsupportedOperationException();
	}

	default double getDegree(@NotNull String variable) {
		return Integer.MAX_VALUE;
	}

	default boolean isLinear(@NotNull String variable) {
		return getDegree(variable) <= 1;
	}

	default boolean isConstant() {
		return false;
	}

	default boolean isNegative() {
		return false;
	}

	@NotNull
	default List<Node> getFactors(@NotNull MathEnvironment environment) {
		return Collections.singletonList(this);
	}

	@Nullable
	default Node removeFactor(@NotNull Node node, @NotNull MathEnvironment environment) {
		if (equals(node)) return Constant.ONE;
		return null;
	}

	default boolean hasValue(@NotNull MathEnvironment environment) {
		return (environment.isEvaluate() && isConstant()) || (this instanceof Constant c && c.shouldCombine());
	}

	default double value() {
		var temp = apply(new MathEnvironment().evaluate());
		if (temp instanceof Constant c) return c.value();

		throw new UnsupportedOperationException();
	}

	@NotNull
	default Node simplify() {
		return apply(MathEnvironment.DEFAULT);
	}
}
