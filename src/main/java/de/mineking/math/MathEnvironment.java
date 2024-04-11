package de.mineking.math;

import de.mineking.math.node.Constant;
import de.mineking.math.node.Fraction;
import de.mineking.math.node.Node;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

public class MathEnvironment {
	public final static MathEnvironment DEFAULT = new MathEnvironment();

	private final Map<String, Node> variables = new HashMap<>();

	private boolean evaluate = false;
	private boolean factorOut = false;

	private double accuracy = 0.000_000_1;
	private EnumSet<FactorCondition> factorConditions = EnumSet.noneOf(FactorCondition.class);

	@NotNull
	public MathEnvironment clone() {
		return new MathEnvironment()
				.variables(variables)
				.evaluate(evaluate)
				.factorOut(factorOut)
				.accuracy(accuracy)
				.factorConditions(EnumSet.copyOf(factorConditions));
	}

	@NotNull
	public Map<String, Node> getVariables() {
		return variables;
	}

	public boolean isEvaluate() {
		return evaluate;
	}

	public boolean isFactorOut() {
		return factorOut;
	}

	public double getAccuracy() {
		return accuracy;
	}

	@NotNull
	public EnumSet<FactorCondition> getFactorConditions() {
		return factorConditions;
	}

	@NotNull
	public MathEnvironment variable(@NotNull String name, @NotNull Node node) {
		variables.put(name, node);
		return this;
	}

	@NotNull
	public MathEnvironment variables(@NotNull Map<String, Node> variables) {
		this.variables.putAll(variables);
		return this;
	}

	@NotNull
	public MathEnvironment evaluate(boolean evaluate) {
		this.evaluate = evaluate;
		return this;
	}

	@NotNull
	public MathEnvironment evaluate() {
		return evaluate(true);
	}

	@NotNull
	public MathEnvironment factorOut(boolean factorOut) {
		this.factorOut = factorOut;
		return this;
	}

	@NotNull
	public MathEnvironment factorOut() {
		return factorOut(true);
	}

	@NotNull
	public MathEnvironment accuracy(double accuracy) {
		this.accuracy = accuracy;
		return this;
	}

	@NotNull
	public MathEnvironment factorConditions(@NotNull EnumSet<FactorCondition> urgency) {
		this.factorConditions = urgency;
		return this;
	}

	@NotNull
	public MathEnvironment factor(@NotNull FactorCondition condition) {
		this.factorConditions.add(condition);
		return this;
	}

	@NotNull
	public Node choose(@NotNull Node general, @NotNull BooleanSupplier condition, @NotNull DoubleSupplier value) {
		if (condition.getAsBoolean()) {
			var temp = value.getAsDouble();
			if (temp == (int) temp || evaluate) return Constant.create(temp);
		}

		return general;
	}

	@NotNull
	public Node createFraction(double value) {
		return Fraction.fraction(value, accuracy);
	}
}
