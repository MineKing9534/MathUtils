package de.mineking.math.node;

import de.mineking.math.FactorCondition;
import de.mineking.math.MathEnvironment;
import de.mineking.math.function.DefaultFunctions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Exponent implements Node {
	private final Node base;
	private final Node exponent;

	Exponent(@NotNull Node base, @NotNull Node exponent) {
		this.base = base;
		this.exponent = exponent;
	}

	@NotNull
	public Node getBase() {
		return base;
	}

	@NotNull
	public Node getExponent() {
		return exponent;
	}

	@NotNull
	public static Node create(@NotNull Node base, @NotNull Node exponent) {
		if (base == Constant.ZERO) return Constant.ZERO;
		if (base == Constant.ONE) return Constant.ONE;

		if (exponent == Constant.ZERO) return Constant.ONE;
		if (exponent == Constant.ONE) return base;

		if (exponent.isNegative()) return Fraction.create(Constant.ONE, Exponent.create(base, exponent.negate()));

		if (base instanceof Exponent e) return create(e.base, Product.create(e.exponent, exponent));

		return new Exponent(base, exponent);
	}

	@NotNull
	@Override
	public Node apply(@NotNull MathEnvironment environment) {
		var a = this.base.apply(environment);
		var b = this.exponent.apply(environment);

		return environment.choose(
				create(a, b),
				() -> a.hasValue(environment) && b.hasValue(environment),
				() -> Math.pow(a.value(), b.value())
		);
	}

	@NotNull
	@Override
	public Node getDerivative(@NotNull String variable) {
		return Product.create(this, Sum.create(
				Product.create(UnaryFunctionCall.create(DefaultFunctions.ln, base), exponent.getDerivative(variable)),
				Fraction.create(Product.create(base.getDerivative(variable), exponent), base)
		));
	}

	@NotNull
	@Override
	public Node getIntegral(@NotNull String variable) {
		if (exponent.isConstant() && base.isLinear(variable)) {
			var nc = Constant.create(exponent.value() + 1);
			return Fraction.create(
					Exponent.create(base, nc),
					Product.create(base.getDerivative(variable), nc)
			);
		} else if (base.isConstant() && exponent.isLinear(variable)) {
			return Fraction.create(
					this,
					Product.create(
							exponent.getDerivative(variable),
							UnaryFunctionCall.create(DefaultFunctions.ln, base)
					)
			);
		}

		throw new UnsupportedOperationException();
	}

	@Override
	public double getDegree(@NotNull String variable) {
		var bd = base.getDegree(variable);
		var ed = exponent.getDegree(variable);

		if (ed != 0) return Integer.MAX_VALUE;
		return bd * exponent.value();
	}

	@Override
	public boolean isConstant() {
		return base.isConstant() && exponent.isConstant();
	}

	@NotNull
	@Override
	public List<Node> getFactors(@NotNull MathEnvironment environment) {
		if (exponent.hasValue(environment) && exponent.value() == (int) exponent.value()) return Collections.nCopies((int) exponent.value(), base);
		return Node.super.getFactors(environment);
	}

	@Nullable
	@Override
	public Node removeFactor(@NotNull Node node, @NotNull MathEnvironment environment) {
		if (equals(node)) return Constant.ONE;

		if (exponent.isConstant() || environment.getFactorConditions().contains(FactorCondition.FUNCTION_EXPONENT)) {
			if (node.equals(base)) return create(base, Sum.create(exponent, Constant.NEGATIVE));
			if (node instanceof Exponent e && e.getBase().equals(base)) return create(base, Sum.create(exponent, e.getExponent().negate()));
		}

		return Node.super.removeFactor(node, environment);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Node n)) return false;
		var a = simplify();
		var b = n.simplify();

		if (a instanceof Exponent ea) {
			if (b instanceof Exponent eb) return ea.base.equals(eb.base) && ea.exponent.equals(eb.exponent);
			else return false;
		}
		return a.equals(b);
	}

	@Override
	public int hashCode() {
		return Objects.hash("exponent", base, exponent);
	}

	@Override
	public String toString() {
		return base.stringWithParentheses(getPriority()) + "^" + exponent.stringWithParentheses(getPriority());
	}

	@Override
	public int getPriority() {
		return 200;
	}
}
