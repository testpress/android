package in.testpress.testpress.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import static in.testpress.testpress.BuildConfig.PRIVATE_KEY;

public class HmacSignature {
    public static String getHmacSignature(String payload) {
        try {
            // Constructing HMAC Signature
            Mac hasher = Mac.getInstance("HmacSHA256");
            hasher.init(new SecretKeySpec(PRIVATE_KEY.getBytes(), "HmacSHA256"));

            byte[] hmac = hasher.doFinal(payload.getBytes());
            String signature = DatatypeConverter.printHexBinary(hmac);
            String hmac_signature = signature.toLowerCase();

            return hmac_signature;
        } catch (NoSuchAlgorithmException e) {
        } catch (InvalidKeyException e) {
        }

        return "";
    }
}
