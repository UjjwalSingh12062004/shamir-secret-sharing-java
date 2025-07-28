import org.json.JSONObject;
import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.*;

public class SecretFinder {

    // Decode base-N encoded value to BigInteger
    private static BigInteger decode(String value, int base) {
        return new BigInteger(value, base);
    }

    // Lagrange Interpolation at x = 0 to find constant term (secret)
    private static BigInteger lagrangeConstantTerm(List<BigInteger[]> points) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < points.size(); i++) {
            BigInteger xi = points.get(i)[0];
            BigInteger yi = points.get(i)[1];

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < points.size(); j++) {
                if (i == j) continue;
                BigInteger xj = points.get(j)[0];
                numerator = numerator.multiply(xj.negate());
                denominator = denominator.multiply(xi.subtract(xj));
            }

            BigInteger term = yi.multiply(numerator).divide(denominator);
            result = result.add(term);
        }

        return result;
    }

    // Process a single test case and return the secret
    public static BigInteger process(JSONObject json) {
        JSONObject keys = json.getJSONObject("keys");
        int n = keys.getInt("n");
        int k = keys.getInt("k");

        List<BigInteger[]> decodedPoints = new ArrayList<>();

        for (String key : json.keySet()) {
            if (key.equals("keys")) continue;
            int x = Integer.parseInt(key);
            JSONObject point = json.getJSONObject(key);
            int base = point.getInt("base");
            String value = point.getString("value");
            BigInteger y = decode(value, base);
            decodedPoints.add(new BigInteger[]{BigInteger.valueOf(x), y});
        }

        BigInteger secret = null;
        Map<BigInteger, Integer> frequencyMap = new HashMap<>();
        combine(decodedPoints, k, 0, new ArrayList<>(), frequencyMap);

        int maxFreq = 0;
        for (Map.Entry<BigInteger, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > maxFreq) {
                maxFreq = entry.getValue();
                secret = entry.getKey();
            }
        }

        return secret;
    }

    // Generate all combinations of size k and compute f(0) for each
    private static void combine(List<BigInteger[]> points, int k, int start,
                                List<BigInteger[]> current, Map<BigInteger, Integer> freqMap) {
        if (current.size() == k) {
            BigInteger secret = lagrangeConstantTerm(current);
            freqMap.put(secret, freqMap.getOrDefault(secret, 0) + 1);
            return;
        }
        for (int i = start; i < points.size(); i++) {
            current.add(points.get(i));
            combine(points, k, i + 1, current, freqMap);
            current.remove(current.size() - 1);
        }
    }

    public static void main(String[] args) throws Exception {
        // Read first test case from resources
        String json1 = Files.readString(new File("src/main/resources/testcase1.json").toPath());
        JSONObject testCase1 = new JSONObject(json1);
        BigInteger secret1 = process(testCase1);
        System.out.println("Secret 1: " + secret1);

        // Read second test case from resources
        String json2 = Files.readString(new File("src/main/resources/testcase2.json").toPath());
        JSONObject testCase2 = new JSONObject(json2);
        BigInteger secret2 = process(testCase2);
        System.out.println("Secret 2: " + secret2);
    }
}
