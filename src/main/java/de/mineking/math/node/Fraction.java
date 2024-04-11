package de.mineking.math.node;

import de.mineking.math.MathEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;

public class Fraction implements Node {
	private final Node top;
	private final Node bottom;

	Fraction(@NotNull Node top, @NotNull Node bottom) {
		this.top = top;
		this.bottom = bottom;
	}

	@NotNull
	public Node getTop() {
		return top;
	}

	@NotNull
	public Node getBottom() {
		return bottom;
	}

	@NotNull
	public static Node create(@NotNull Node top, @NotNull Node bottom) {
		if (top.isNegative() && bottom.isNegative()) return create(top.negate(), bottom.negate());

		if (bottom == Constant.ONE) return top;
		if (top == Constant.ZERO) return Constant.ZERO;

		if (top.equals(bottom)) return Constant.ONE;
		if (bottom == Constant.ZERO) return top.isNegative() ? Constant.NEGATIVE_INFINITY : Constant.POSITIVE_INFINITY;

		return new Fraction(top, bottom);
	}

	@NotNull
	public static Node fraction(double value, double error) {
		var n = Math.floor(value);
		value -= n;

		if (value < error) return Constant.create(n);
		else if (1 - error < value) return Constant.create(n + 1);

		var lower_top = 0;
		var lower_bottom = 1;

		var upper_top = 1;
		var upper_bottom = 1;

		while (true) {
			var middle_top = lower_top + upper_top;
			var middle_bottom = lower_bottom + upper_bottom;

			if (middle_top > 500 || middle_bottom > 500) return Constant.create(value + n);

			if (middle_bottom * (value + error) < middle_top) {
				upper_top = middle_top;
				upper_bottom = middle_bottom;
			} else if (middle_top < (value - error) * middle_bottom) {
				lower_top = middle_top;
				lower_bottom = middle_bottom;
			} else return create(Constant.create(n * middle_bottom + middle_top), Constant.create(middle_bottom));
		}
	}

	@NotNull
	@Override
	public Node apply(@NotNull MathEnvironment environment) {
		var temp = create(this.top.apply(environment), this.bottom.apply(environment));

		if (temp instanceof Fraction fraction) {
			var map = new HashMap<Node, Node>();

			for(var n : fraction.getTop().getFactors(environment)) {
				var base = n instanceof Exponent e ? e.getBase() : n;
				var exponent = n instanceof Exponent e ? e.getExponent() : Constant.ONE;

				map.compute(base, (k, v) -> v == null ? exponent : Sum.create(v, exponent));
			}

			for(var n : fraction.getBottom().getFactors(environment)) {
				var base = n instanceof Exponent e ? e.getBase() : n;
				var exponent = n instanceof Exponent e ? e.getExponent().negate() : Constant.NEGATIVE;

				map.compute(base, (k, v) -> v == null ? exponent : Sum.create(v, exponent));
			}

			return Product.create(map.entrySet().stream()
					.map(e -> Exponent.create(e.getKey(), e.getValue()))
					.toList()
			);
		}

		return temp.apply(environment);
	}

	@NotNull
	@Override
	public Node getDerivative(@NotNull String variable) {
		return Fraction.create(
				Sum.create(Product.create(top.getDerivative(variable), bottom), Product.create(bottom.getDerivative(variable), top)),
				Exponent.create(bottom, Constant.TWO)
		);
	}

	@NotNull
	@Override
	public Node getIntegral(@NotNull String variable) {
		if(isConstant()) return Product.create(this, Variable.create(variable));

		throw new UnsupportedOperationException(); //TODO
	}

	@Override
	public double getDegree(@NotNull String variable) {
		return Node.super.getDegree(variable);
	}

	@Override
	public boolean isConstant() {
		return top.isConstant() && bottom.isConstant();
	}

	@Override
	public boolean isNegative() {
		return top.isNegative() || bottom.isNegative(); //Both negative is already canceled out in #create
	}

	@Nullable
	@Override
	public Node removeFactor(@NotNull Node node, @NotNull MathEnvironment environment) {
		if (node instanceof Fraction f) {
			var top = this.top.removeFactor(f.getTop(), environment);
			var bottom = this.bottom.removeFactor(f.getBottom(), environment);
			if (top != null && bottom != null) return create(top, bottom);
		}

		var top = this.top.removeFactor(node, environment);
		if (top != null) return create(top, bottom);

		return Node.super.removeFactor(node, environment);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Node n)) return false;
		var a = simplify();
		var b = n.simplify();

		if (a instanceof Fraction fa) {
			if (b instanceof Fraction fb) return fa.top.equals(fb.top) && fa.bottom.equals(fb.bottom);
			else return false;
		}
		return a.equals(b);
	}

	@Override
	public int hashCode() {
		return Objects.hash("fraction", top, bottom);
	}

	@Override
	public String toString() {
		return top.stringWithParentheses(getPriority()) + " / " + bottom.stringWithParentheses(getPriority());
	}

	@Override
	public int getPriority() {
		return 110;
	}
}
