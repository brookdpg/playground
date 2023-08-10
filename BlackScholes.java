public class BlackScholes {
    public static double blsDelta(double spotPrice, double strikePrice, double timeToMaturity, double volatility, double riskFreeRate) {
        double d1 = (Math.log(spotPrice / strikePrice) + (riskFreeRate + 0.5 * Math.pow(volatility, 2)) * timeToMaturity) / (volatility * Math.sqrt(timeToMaturity));
        return cumulativeNormalDistribution(d1);
    }

    private static double cumulativeNormalDistribution(double x) {
        return 0.5 * (1 + erf(x / Math.sqrt(2)));
    }

    private static double erf(double z) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));
        double ans = 1 - t * Math.exp(-z * z - 1.26551223 +
                t * (1.00002368 +
                t * (0.37409196 +
                t * (0.09678418 +
                t * (-0.18628806 +
                t * (0.27886807 +
                t * (-1.13520398 +
                t * (1.48851587 +
                t * (-0.82215223 +
                t * (0.17087277))))))))));
        return z >= 0 ? ans : -ans;
    }

    public static void main(String[] args) {
        double spotPrice = 100.0;
        double strikePrice = 100.0;
        double timeToMaturity = 1.0;
        double volatility = 0.2;
        double riskFreeRate = 0.05;

        double delta = blsDelta(spotPrice, strikePrice, timeToMaturity, volatility, riskFreeRate);
        System.out.println("Black-Scholes Delta: " + delta);
    }
}
