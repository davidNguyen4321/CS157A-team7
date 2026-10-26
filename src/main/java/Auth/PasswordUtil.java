package Auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

public class PasswordUtil {
    private static final int MEMORY_KB = 65536, ITERATIONS = 3, PARALLELISM = 1;
    private static final int SALT_BYTES = 16, HASH_BYTES = 32;
    private static final SecureRandom RNG = new SecureRandom();

    public static String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RNG.nextBytes(salt);
        byte[] hash = derive(password, salt, MEMORY_KB, ITERATIONS, PARALLELISM, HASH_BYTES);
        Base64.Encoder b64 = Base64.getEncoder().withoutPadding();
        return "$argon2id$v=19$m=" + MEMORY_KB + ",t=" + ITERATIONS + ",p=" + PARALLELISM
                + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(hash);
    }

    public static boolean verify(String password, String stored) {
        try {
            // "$argon2id$v=19$m=..,t=..,p=..$salt$hash" -> ["", "argon2id", "v=19", params, salt, hash]
            String[] parts = stored.split("\\$");
            if (parts.length != 6 || !parts[1].equals("argon2id")) return false;
            int m = 0, t = 0, p = 0;
            for (String kv : parts[3].split(",")) {
                String[] pair = kv.split("=");
                switch (pair[0]) {
                    case "m": m = Integer.parseInt(pair[1]); break;
                    case "t": t = Integer.parseInt(pair[1]); break;
                    case "p": p = Integer.parseInt(pair[1]); break;
                }
            }
            Base64.Decoder dec = Base64.getDecoder();
            byte[] salt = dec.decode(parts[4]);
            byte[] expected = dec.decode(parts[5]);
            byte[] actual = derive(password, salt, m, t, p, expected.length);
            return MessageDigest.isEqual(expected, actual); // constant-time compare
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int m, int t, int p, int len) {
        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withMemoryAsKB(m).withIterations(t).withParallelism(p)
                .withSalt(salt).build();
        Argon2BytesGenerator gen = new Argon2BytesGenerator();
        gen.init(params);
        byte[] out = new byte[len];
        gen.generateBytes(password.getBytes(StandardCharsets.UTF_8), out);
        return out;
    }
}
