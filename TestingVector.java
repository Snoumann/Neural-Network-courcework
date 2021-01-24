package coursework;

import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.*;
import static java.lang.Math.log;

public class TestingVector {
    double[][] testingVector = new double[10][5];

    TestingVector() {
        for (int raw = 0; raw < testingVector.length; raw++)
            for (int col = 0; col < testingVector[raw].length; col++) {
                if (col == 4)
                    testingVector[raw][col] = ThreadLocalRandom.current().nextInt(0, 1 + 1);
                else {
                    do {
                        double randomValue = ratioMethod();
                        if (randomValue > 0.0) {
                            testingVector[raw][col] = randomValue;
                            break;
                        }
                    } while (true);
                }
            }
    }

    public void showTestingVector() {
        System.out.println("\tТЕСТОВІ ВЕКТОРИ");

        for (int raw = 0; raw < testingVector.length; raw++) {

            for (int col = 0; col < testingVector[raw].length; col++)
                System.out.print(String.format("%4.1f", testingVector[raw][col]) + " ");
            System.out.println();
        }
    }

    //метод співвідношень (ГШЗ)        [використовуємо у методі setEntries()]
    public static double ratioMethod() {
        double U = 0.0;
        double V = 0.0;
        double X = 0.0;

        while (true) {
            do { //8.1
                //генеруємо 2 незалежні випадкові величини [0; 1]
                U = ThreadLocalRandom.current().nextDouble(0.0, 1.0);
                V = ThreadLocalRandom.current().nextDouble(0.0, 1.0);

                if ((U != V) && (U != 0.0))
                    break;

            } while (true);

            X = sqrt(8/exp(1)) * (V - 0.5) / U; //8.2

            if (pow(X,2) <= 5 - 4 * exp(1/4) * U) //8.3 перевірка верхньої грані
                return X;

            //8.4 перевірка нижньої грані
            if (pow(X,2) >= 4 * exp(-1.35) / U + 1.4) {
                //повертаємося на крок 8.1
            } else if (pow(X,2) <= -4 * log(U))  //8.5 остаточна перевірка
                    return X;
        }
    }
}
