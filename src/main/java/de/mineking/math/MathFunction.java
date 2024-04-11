package de.mineking.math;

import de.mineking.math.node.Constant;
import de.mineking.math.node.Node;
import de.mineking.math.node.Sum;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MathFunction implements Node {
	private final Node node;

	private MathFunction(@NotNull Node node) {
		this.node = node;
	}

	@NotNull
	public static MathFunction create(@NotNull Node node) {
		return new MathFunction(node);
	}

	@NotNull
	@Override
	public Node apply(@NotNull MathEnvironment environment) {
		return node.apply(environment);
	}

	@NotNull
	@Override
	public Node negate() {
		return node.negate();
	}

	@Override
	public int getPriority() {
		return node.getPriority();
	}

	@Override
	@NotNull
	public String stringWithParentheses(boolean parentheses) {
		return node.stringWithParentheses(parentheses);
	}

	@Override
	@NotNull
	public String stringWithParentheses(int priority) {
		return node.stringWithParentheses(priority);
	}

	@Override
	@NotNull
	public Node getDerivative(@NotNull String variable) {
		return node.getDerivative(variable);
	}

	public double getSlope(double x) {
		return getDerivative(Node.defaultVariable).apply(MathEnvironment.DEFAULT.clone().variable(Node.defaultVariable, Constant.create(x))).value();
	}

	@Override
	@NotNull
	public Node getIntegral(@NotNull String variable) {
		return node.getIntegral(variable);
	}

	@NotNull
	public Node getArea(@NotNull String variable, double lowerBound, @NotNull Node upperBound) {
		var integral = getIntegral(variable);

		return Sum.create(
				integral.apply(MathEnvironment.DEFAULT.clone().variable(variable, upperBound)),
				integral.apply(MathEnvironment.DEFAULT.clone().variable(variable, Constant.create(lowerBound))).negate()
		);
	}

	public double getArea(double lowerBound, double upperBound) {
		return getArea(defaultVariable, lowerBound, Constant.create(upperBound)).value();
	}

	public double getAverage(double lowerBound, double upperBound) {
		return getArea(lowerBound, upperBound) / (upperBound - lowerBound);
	}

	@Override
	public double getDegree(@NotNull String variable) {
		return node.getDegree(variable);
	}

	@Override
	public boolean isLinear(@NotNull String variable) {
		return node.isLinear(variable);
	}

	@Override
	public boolean isConstant() {
		return node.isConstant();
	}

	@Override
	public boolean isNegative() {
		return node.isNegative();
	}

	@Override
	@NotNull
	public List<Node> getFactors(@NotNull MathEnvironment environment) {
		return node.getFactors(environment);
	}

	@Override
	@Nullable
	public Node removeFactor(@NotNull Node node, @NotNull MathEnvironment environment) {
		return node.removeFactor(node, environment);
	}

	@Override
	public boolean hasValue(@NotNull MathEnvironment environment) {
		return node.hasValue(environment);
	}

	@Override
	public double value() {
		return node.value();
	}

	@NotNull
	public Node evaluate(double value) {
		return apply(MathEnvironment.DEFAULT.clone().variable(defaultVariable, Constant.create(value)).evaluate());
	}

	@NotNull
	public Node evaluate() {
		return evaluate(0);
	}
}
