package utils;

import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Random;

import gui.MyApp;

public class VerifyLicense {

    public static int getTypeLicense(String encryptedString) {

        final String cipher = "1,0,1,1,0,0,0,1,0,8,4,2,5,-3,-1,0,-2,6,7,-3,5,-2,12,5,9,8,-3,-6,1,1,1,1,0,0,1,0,0,0,1,1,1,0,1,0,0,0,13,8,14,16,12,15,11,7,6,14,3,16,8,4,12,3,6,15";

        for (int i = 0; i < 10; i++) {

            String dStr = decryptString(formatString(deshuffle(encryptedString.replace("_", ""), i)), cipher);

            String serialNumber = dStr.substring(0, 16);
            String typeLicense = dStr.substring(16, 18);


            if (serialNumber.equals(MyApp.DEVICE_SN)) {
                Log.d("MyK", typeLicense);
                if (typeLicense.equals("10") || typeLicense.equals("11") || typeLicense.equals("50") || typeLicense.equals("60") || typeLicense.equals("70") || typeLicense.equals("80") ||
                        typeLicense.equals("81") || typeLicense.equals("82") || typeLicense.equals("83") || typeLicense.equals("84")) {
                    switch (typeLicense) {
                        case "10": //Drill
                            return 10;
                        case "11": //Drill +AUTO
                            return 11;
                        case "50":
                            return 1; // 1D
                        case "60":
                            return 2; // 2D
                        case "70":
                            return 3; // 3D EASY
                        case "80":
                            return 4; // 3D PRO
                        case "81":
                            return 33; // 1D+AUTO
                        case "82":
                            return 34; // 2D+AUTO
                        case "83":
                            return 35; // 3D EASY+AUTO
                        case "84":
                            return 36; // 3D PRO+AUTO
                    }
                }
            }
        }

        return -1;
    }

    public static String switchString(String input, int[] flags) {
        if (input.length() % 2 != 0) {
            throw new IllegalArgumentException("Input string length must be even.");
        }

        StringBuilder result = new StringBuilder(input.length());

        for (int i = 0; i < input.length(); i += 2) {
            result.append(input, i, i + 2);
        }

        for (int i = 0; i < flags.length; i++) {
            if (flags[i] > 0) {
                int partIndex = i * 2;
                int endIndex = partIndex + 2;
                String part = result.substring(partIndex, endIndex);
                result.replace(partIndex, endIndex, new StringBuilder(part).reverse().toString());
            }
        }

        return result.toString();
    }


    public static String shiftString(String input, int k) {
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Input string empty!");
        }

        int length = input.length();

        k = k % length; // Gestione dei valori di k maggiori della lunghezza della stringa

        if (k < 0) {
            k = length + k; // Converti k negativo in positivo per shift a sinistra
        }

        return input.substring(length - k) + input.substring(0, length - k);
    }


    public static String incrementString(String input, int[] k) {

        if (k.length != input.length()) {
            throw new IllegalArgumentException("Input string not valid!");
        }

        final String valid = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        int validLength = valid.length();

        StringBuilder result = new StringBuilder(input.length());

        for (int i = 0; i < input.length(); i++) {
            int inputIndex = valid.indexOf(input.charAt(i));
            if (inputIndex == -1) {
                throw new IllegalArgumentException("Invalid character in input string!");
            }

            // Calculate the new index with circular increment
            int newIndex = (inputIndex + k[i]) % validLength;
            if (newIndex < 0) {
                newIndex += validLength; // Handle negative indices
            }

            result.append(valid.charAt(newIndex));
        }

        return result.toString();
    }

    public static String caseString(String input, int[] flags) {

        if (input.length() != flags.length) {
            throw new IllegalArgumentException("Input string not valid!");
        }

        StringBuilder result = new StringBuilder(input.length());

        for (int i = 0; i < input.length(); i++) {

            char currentChar = input.charAt(i);

            if (Character.isLetter(currentChar)) {
                result.append(flags[i] > 0 ? Character.toLowerCase(currentChar) : currentChar);
            } else {
                result.append(currentChar);
            }
        }

        return result.toString();
    }

    public static String shuffleString(String input, int[] swapSequence) {

        char[] array = input.toCharArray();
        for (int k = 0; k < swapSequence.length; k += 2) {
            int i = swapSequence[k];
            int j = swapSequence[k + 1];
            char temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
        return new String(array);
    }

    public static int[] getShuffleExchanges(int size, int key) {
        int[] exchanges = new int[size - 1];
        Random rand = new Random(key);
        for (int i = size - 1; i > 0; i--) {
            int n = rand.nextInt(i + 1);
            exchanges[size - 1 - i] = n;
        }
        return exchanges;
    }

    public static String shuffle(String toShuffle, int key) {
        int size = toShuffle.length();
        char[] chars = toShuffle.toCharArray();
        int[] exchanges = getShuffleExchanges(size, key);
        for (int i = size - 1; i > 0; i--) {
            int n = exchanges[size - 1 - i];
            char tmp = chars[i];
            chars[i] = chars[n];
            chars[n] = tmp;
        }
        return new String(chars);
    }

    public static String deshuffle(String shuffled, int key) {
        int size = shuffled.length();
        char[] chars = shuffled.toCharArray();
        int[] exchanges = getShuffleExchanges(size, key);
        for (int i = 1; i < size; i++) {
            int n = exchanges[size - i - 1];
            char tmp = chars[i];
            chars[i] = chars[n];
            chars[n] = tmp;
        }
        return new String(chars);
    }


    public static String formatString(String input) {

        if (input.length() != 18) {
            throw new IllegalArgumentException("Input string not valid!");
        }

        StringBuilder result = new StringBuilder();
        int length = input.length();

        for (int i = 0; i < length; i++) {
            if (i > 0 && i % 6 == 0) {
                result.append('_');
            }
            result.append(input.charAt(i));
        }

        return result.toString();
    }

    public static String encryptString(String input, String cipher) {


        int[] condition = Arrays.stream(cipher.split(",")).mapToInt(Integer::parseInt).toArray();

        if (input.length() != 18 || !input.equals(input.toUpperCase()) || condition.length != 64) {
            throw new IllegalArgumentException("Input/Cipher not valid!");
        }

        int[] condA = Arrays.copyOfRange(condition, 0, 8);
        int condB = condition[9];
        int[] condC = Arrays.copyOfRange(condition, 10, 28);
        int[] condD = Arrays.copyOfRange(condition, 28, 46);
        int[] condE = Arrays.copyOfRange(condition, 46, 64);

        int key = new Random().nextInt(10);

        String shuffledRandom = shuffle(shuffleString(incrementString(shiftString(switchString(input, condA), condB), condC), condE), key);

        return formatString(shuffledRandom);
    }

    public static String decryptString(String output, String cipher) {

        int[] condition = Arrays.stream(cipher.split(",")).mapToInt(Integer::parseInt).toArray();

        if (output.length() != 20 || !output.contains("_") || condition.length != 64) {
            throw new IllegalArgumentException("Input/Cipher not valid!");
        }

        int[] condA = Arrays.copyOfRange(condition, 46, 64);
        ArrayUtils.reverse(condA);
        int[] condB = Arrays.stream(Arrays.copyOfRange(condition, 10, 28)).map(i -> -i).toArray();
        int condC = -condition[9];
        int[] condD = Arrays.copyOfRange(condition, 0, 8);


        return switchString(shiftString(incrementString(shuffleString(output.replace("_", "").toUpperCase(), condA), condB), condC), condD);
    }

    public static String generateRestoreCode(String serialNumber, int type){

        final String cipher = "1,0,1,1,0,0,0,1,0,8,4,2,5,-3,-1,0,-2,6,7,-3,5,-2,12,5,9,8,-3,-6,1,1,1,1,0,0,1,0,0,0,1,1,1,0,1,0,0,0,13,8,14,16,12,15,11,7,6,14,3,16,8,4,12,3,6,15";

        String input = serialNumber.concat(String.valueOf(type));

        int[] condition = Arrays.stream(cipher.split(",")).mapToInt(Integer::parseInt).toArray();

        if (input.length() != 18 || !input.equals(input.toUpperCase()) || condition.length != 64) {
            throw new IllegalArgumentException("Input/Cipher not valid!");
        }

        int[] condA = Arrays.copyOfRange(condition, 0, 8);
        int condB = condition[9];
        int[] condC = Arrays.copyOfRange(condition, 10, 28);
        int[] condD = Arrays.copyOfRange(condition, 28, 46);
        int[] condE = Arrays.copyOfRange(condition, 46, 64);

        int[] numbers = {15, 20, 25};
        Random random = new Random();

        int randomIndex = random.nextInt(numbers.length);

        int key = numbers[randomIndex];

        String shuffledRandom = shuffle(shuffleString(incrementString(shiftString(switchString(input, condA), condB), condC), condE), key);

        return formatString(shuffledRandom).replace("_", "");
    }
}