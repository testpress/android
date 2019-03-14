package in.testpress.testpress.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
public class ValidationsTest {

    @Test
    public void test_phone_number_validation() {
        HashMap<Integer, Object[]> phone_number = new HashMap<Integer, Object[]>();

        phone_number.put(1, new Object[]{"9999999999", true});
        phone_number.put(2, new Object[]{"999999999", false});
        phone_number.put(3, new Object[]{"999999999_", false});
        phone_number.put(4, new Object[]{"99999@9999", false});
        phone_number.put(5, new Object[]{"999999999p", false});
        phone_number.put(6, new Object[]{"0999999999", false});
        phone_number.put(7, new Object[]{"9999989999", true});

        for (Map.Entry<Integer, Object[]> number : phone_number.entrySet()) {
            Object[] value = number.getValue();

            assertEquals(Validations.validatePhoneNumber(value[0].toString()), value[1]);
        }
    }
}
