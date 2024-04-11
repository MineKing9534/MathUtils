package de.mineking.math;

public class MathUtils {
	public static int gcd(int a, int b) {
		return b == 0 ? a : gcd(b, b % a);
	}

	public static int lcm(int a, int b) {
		return (a * b) / gcd(a, b);
	}

	public static double nCr(double n, double k) {
		if (2 * k > n) k = n - k;
		double result = 1;
		for (int i = 1; i <= k; i++) result *= (n + 1 - i) / i;

		return result;
	}
}
