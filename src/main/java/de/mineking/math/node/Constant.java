package de.mineking.math.node;

import de.mineking.math.FactorCondition;
import de.mineking.math.MathEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

public class Constant implements Node {
	public final static DecimalFormat format = new DecimalFormat();

	static {
		format.setMinimumFractionDigits(0);
		format.setMaximumFractionDigits(Integer.MAX_VALUE);
		format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
	}

	public final static Constant UNDEFINED = create(Double.NaN, false);
	public final static Constant POSITIVE_INFINITY = create(Double.POSITIVE_INFINITY, false);
	public final static Constant NEGATIVE_INFINITY = create(Double.NEGATIVE_INFINITY, false);

	public final static Constant NEGATIVE = create(-1);
	public final static Constant ZERO = create(0);
	public final static Constant ONE = create(1);
	public final static Constant TWO = create(2);

	public final static Constant E = create(Math.E, false);
	public final static Constant PI = create(Math.PI, false);

	private final double value;
	private final boolean shouldCombine;

	Constant(double value, boolean shouldCombine) {
		this.value = value;
		this.shouldCombine = shouldCombine;
	}

	public boolean shouldCombine() {
		return shouldCombine;
	}

	@NotNull
	public static Constant create(double value, boolean shouldCombine) {
		if (Double.isNaN(value) && UNDEFINED != null) return UNDEFINED;
		if (value == Double.POSITIVE_INFINITY && POSITIVE_INFINITY != null) return POSITIVE_INFINITY;
		if (value == Double.NEGATIVE_INFINITY && NEGATIVE_INFINITY != null) return NEGATIVE_INFINITY;

		if (value == -1 && NEGATIVE != null) return NEGATIVE;
		if (value == 0 && ZERO != null) return ZERO;
		if (value == 1 && ONE != null) return ONE;
		if (value == 2 && TWO != null) return TWO;

		if (value == Math.E && E != null) return E;
		if (value == Math.PI && PI != null) return PI;

		return new Constant(value, shouldCombine);
	}

	@NotNull
	public static Constant create(double value) {
		return create(value, true);
	}

	@NotNull
	@Override
	public Node apply(@NotNull MathEnvironment environment) {
		var fraction = environment.createFraction(value);

		if (!environment.isEvaluate() && fraction instanceof Fraction f && (f.getTop() instanceof Constant ct && ct.value < 500) && (f.getBottom() instanceof Constant cb && cb.value < 500)) return fraction;
		return this;
	}

	@Override
	public double value() {
		return value;
	}

	@NotNull
	@Override
	public Node getDerivative(@NotNull String variable) {
		return ZERO;
	}

	@NotNull
	@Override
	public Node getIntegral(@NotNull String variable) {
		return Product.create(this, Variable.create(variable));
	}

	@Override
	public double getDegree(@NotNull String variable) {
		return 0;
	}

	@Override
	public boolean isConstant() {
		return true;
	}

	@Override
	public boolean isNegative() {
		return value < 0;
	}

	@Nullable
	@Override
	public Node removeFactor(@NotNull Node node, @NotNull MathEnvironment environment) {
		if (!node.hasValue(environment)) return null;

		var temp = value / node.value();
		if (environment.isEvaluate()) return create(value);

		if (temp == (int) temp) return create(temp);
		if (environment.getFactorConditions().contains(FactorCondition.CONSTANT_FRACTION) && environment.createFraction(temp) instanceof Fraction f) return f;

		return Node.super.removeFactor(node, environment);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Constant c && c.value == value;
	}

	@Override
	public int hashCode() {
		return Objects.hash("constant", value);
	}

	@Override
	public String toString() {
		if (this == E) return "e";
		if (this == PI) return "π";
		if (this == UNDEFINED) return "undefined";
		if (this == POSITIVE_INFINITY) return "∞";
		if (this == NEGATIVE_INFINITY) return "-∞";

		return format.format(value);
	}

	@Override
	public int getPriority() {
		return Integer.MAX_VALUE;
	}
}
