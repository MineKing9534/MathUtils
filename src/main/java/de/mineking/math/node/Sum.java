package de.mineking.math.node;

import de.mineking.math.MathEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sum implements Node {
	private final List<Node> nodes;

	Sum(@NotNull List<Node> nodes) {
		this.nodes = nodes;
	}

	@NotNull
	public List<Node> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	@NotNull
	public static Node create(@NotNull Collection<Node> nodes) {
		var temp = process(nodes, MathEnvironment.DEFAULT);

		if (temp.isEmpty()) return Constant.ZERO;
		if (temp.size() == 1) return temp.stream().findFirst().get();

		return new Sum(temp);
	}

	@NotNull
	public static Node create(@NotNull Node... nodes) {
		return create(Arrays.asList(nodes));
	}

	@NotNull
	private static List<Node> process(@NotNull Collection<Node> nodes, @NotNull MathEnvironment environment) {
		var constant = new AtomicReference<>(0.0);
		var temp = nodes.stream()
				.flatMap(n -> n instanceof Sum s ? s.nodes.stream() : Stream.of(n))
				.filter(n -> n != Constant.ZERO)
				.filter(n -> {
					if (n.hasValue(environment)) {
						constant.updateAndGet(d -> d + n.value());
						return false;
					}

					return true;
				})
				.collect(Collectors.toList());

		if (constant.get() != 0) temp.add(Constant.create(constant.get()));

		return temp;
	}

	@NotNull
	@Override
	public Node apply(@NotNull MathEnvironment environment) {
		var temp = create(this.nodes.stream().map(n -> n.apply(environment)).toList());

		if (temp instanceof Sum sum) {
			var neg = new AtomicInteger();
			var nodes = process(sum.nodes, environment).stream()
					.peek(n -> {
						if (n.isNegative()) neg.incrementAndGet();
					})
					.collect(Collectors.toList());

			if (neg.get() > nodes.size() / 2) return create(nodes.stream().map(Node::negate).toList()).negate().apply(environment);

			//TODO (...)^n

			combine:
			while (true) {
				for (var n : new ArrayList<>(nodes)) {
					var factors = n.getFactors(environment);

					for (var factor : factors) {
						var s = new ArrayList<Node>();

						for (int j = nodes.size() - 1; j >= 0; j--) { //Go backward to ensure valid indices for remove call
							var t = nodes.get(j).removeFactor(factor, environment);
							if (t != null) {
								nodes.remove(j);
								s.add(t);
							}
						}

						if (!s.isEmpty()) nodes.add(Product.create(factor, create(s)));
						if (s.size() > 1) continue combine;
					}
				}

				break;
			}

			return create(nodes);
		}

		return temp.apply(environment);
	}

	@NotNull
	@Override
	public Node getDerivative(@NotNull String variable) {
		return create(nodes.stream().map(n -> n.getDerivative(variable)).toList());
	}

	@NotNull
	@Override
	public Node getIntegral(@NotNull String variable) {
		return create(nodes.stream().map(n -> n.getIntegral(variable)).toList());
	}

	@Override
	public double getDegree(@NotNull String variable) {
		return nodes.stream().mapToDouble(node -> node.getDegree(variable)).max().orElse(0);
	}

	@NotNull
	public FactorResult factor(@NotNull MathEnvironment environment) {
		var common = new ArrayList<Node>();
		var sum = new ArrayList<>(nodes);

		var factors = nodes.stream()
				.flatMap(n -> n.getFactors(environment).stream())
				.toList();

		check:
		for (var factor : factors) {

			var temp = new ArrayList<Node>();
			for (var n : sum) {
				var t = n.removeFactor(factor, environment);
				if (t == null) continue check;
				temp.add(t);
			}

			sum = temp;
			common.add(factor);
		}

		return new FactorResult(common, sum);
	}

	@NotNull
	@Override
	public List<Node> getFactors(@NotNull MathEnvironment environment) {
		return factor(environment).common();
	}

	@Nullable
	@Override
	public Node removeFactor(@NotNull Node node, @NotNull MathEnvironment environment) {
		var temp = nodes.stream()
				.map(n -> n.removeFactor(node, environment))
				.toList();

		if (temp.stream().noneMatch(Objects::isNull)) return create(temp);
		return Node.super.removeFactor(node, environment);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Node n)) return false;
		var a = simplify();
		var b = n.simplify();

		if (a instanceof Sum sa) {
			if (b instanceof Sum sb) return new HashSet<>(sa.nodes).equals(new HashSet<>(sb.nodes));
			else return false;
		}
		return a.equals(b);
	}

	@Override
	public int hashCode() {
		return Objects.hash("sum", nodes);
	}

	@Override
	public String toString() {
		var first = new AtomicBoolean(true);
		return nodes.stream()
				.sorted(Comparator.comparing(Node::isNegative))
				.sorted(Comparator.comparing(n -> n.getDegree(Node.defaultVariable), Comparator.reverseOrder()))
				.map(n -> {
					var neg = n.isNegative();
					if (neg) n = n.negate();

					return (first.getAndSet(false) && !neg ? "" : (neg ? "- " : "+ ")) + n.stringWithParentheses(getPriority());
				})
				.collect(Collectors.joining(" "));
	}

	public record FactorResult(@NotNull List<Node> common, @NotNull List<Node> rest) {
	}
}
