package coursework;

import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.*;

public class BoltzmannMachine {

    double[] entryNeurons = new double[4];
    double[] hiddenLayerNeurons = new double[4];
    double exit;

    double T = 273.0; //штучна температура (крок 1)
    double k = 0.001;

    //вага[шар][від якого нейрона][до якого нейрона]                                   [1ий шар][4][4]   [2ий шар][4][1]
    double[][][] wages = new double[2][4][4]; //масив для збереження значення ваг

    //ініціалізація ваг !!! (перед початком роботи машини Больцмана)
    public void initializeAllWages() {
        for (int indexLayer = 0; indexLayer < wages.length; indexLayer++) {
            for (int indexFromNeuron = 0; indexFromNeuron < wages[indexLayer].length; indexFromNeuron++) {
                for (int indexToNeuron = 0; indexToNeuron < wages[indexLayer][indexFromNeuron].length; indexToNeuron++) {
                    wages[indexLayer][indexFromNeuron][indexToNeuron] =
                            ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
                }
            }
        }
    }

    int indexLayer = 0;       //<<з якою вагою спочатку будемо працювати>>
    int indexFromNeuron = 0;
    int indexToNeuron = 0;

    double temporaryChanged; //зміна для зберігання старого значення ваги

    int iterationCount = 0; //лічильник ітерацій (для зміни температури)
    int vectorIndex = 0;//зміна для визначення вектору [для методу targetFunctionIsValidForAllVectors()]

    public void learn (double[][] entries) {//метод навчання машини Больцмана  //пред'явл мережі множину входів (крок2)
        long startTime = System.currentTimeMillis(); //зміна для підрахунку часу роботи машини Больцмана

        int vectorIndex = 0; // подаємо на вхід 1ший (0) вектор

        while (true) {
            for (int index = 0; index < 3; index++)                //ініціалізуємо вхідні нейрони
                entryNeurons[index] = entries[vectorIndex][index];

            iterationCount++; //рахуємо число ітерацій

            //кроки 1-5
            calculateExit();     //обчислюємо виходи (крок 2)

            double firstTargetFunction = calculateTargetFunction(); //обчислюємо "1-шу" ціл функцію (крок 2)

            temporaryChangeWageValue(indexLayer, indexFromNeuron, indexToNeuron);//тимчасова змін ваг коеф (крок 3)
            double secondTargetFunction = calculateTargetFunction();//обчислюємо "2-гу" ціл ф-цію (перерахунок) (крок 3)

            if (targetFunctionBecameBetter(firstTargetFunction, secondTargetFunction)) { //порівнюємо 1. і 2.(крок 4)
                //ф-ція покращилася. зміна ваги відбулася. нічого не змінюємо(крок 4)
            } else {
                if (calculateChangeProbabilityOfTargetFunction() > chooseRandomNumberFrom0To1()) { //(крок 4/5)
                    //зміна ваги відбулася за умови розподілу Больцмана. нічого не змінюємо (крок 5)
                } else {
                    UNDOWageValueChange(); //повертаємо значення ваги до попереднього значення (крок 5)
                }
            }

            nextWage(); //готуємо наст вагу
            decreaseT();//зменшуємо температуру Т

            if (enoughSmallValueOfTargetFunctionIsReached()) {
                if(vectorIndex < 14) {
                    vectorIndex++; // пред'являємо мережі наступний вхідний вектор
                } else {
                    vectorIndex = 0;
                }
            }

            /*                       перевірка чи цільова ф-ція є допутимою для всіх векторів                         */
            int saveIndexOfCurrentVector = vectorIndex; //зберігаємо індекс поточного вектора

            boolean validForAllVectors = true; //змінна для фіксації допустимості

            for (vectorIndex = 0; vectorIndex < entries.length; vectorIndex++) {
                entryNeurons[0] = entries[vectorIndex][0];
                entryNeurons[1] = entries[vectorIndex][1];
                entryNeurons[2] = entries[vectorIndex][2];
                entryNeurons[3] = entries[vectorIndex][3];

                calculateExit();

                if (!enoughSmallValueOfTargetFunctionIsReached()) {
                    validForAllVectors = false;
                    break;
                }
            }

            vectorIndex = saveIndexOfCurrentVector;//повертаємо індекс вектора в початковий стан

            if (validForAllVectors == true){
                long endTime = System.currentTimeMillis();//фіксація часу закінчення роботи машини Больцмана
                long timeOfWork = endTime - startTime;
                float timeOfWorkInSeconds = timeOfWork / 1000F; //вираж в секундах

                System.out.println("Навчання машини Больцмана завершене за " + timeOfWorkInSeconds + " сек.");
                System.out.println("кількість ітерацій: " + iterationCount);

                break;
            }
        }
    }

    public void test(double[][] testingVector) {

        for (int raw = 0; raw < testingVector.length; raw++) {
            entryNeurons[0] = testingVector[raw][0];
            entryNeurons[1] = testingVector[raw][1];
            entryNeurons[2] = testingVector[raw][2];
            entryNeurons[3] = testingVector[raw][3];
            System.out.println(entryNeurons[0] + " " + entryNeurons[1] + " " + entryNeurons[2] + " " + entryNeurons[3]);
            calculateExit();
            CollectorOrPocrushka(exit);
        }
    }

    public void calculateExit() { //обчислюємо виходи
        hiddenLayerNeurons[0] = entryNeurons[0] * wages[0][0][0] + /*накоп суми нейронів 1ого [0ого](прихованого)шару*/
                                entryNeurons[1] * wages[0][1][0] +
                                entryNeurons[2] * wages[0][2][0] +
                                entryNeurons[3] * wages[0][3][0];
        hiddenLayerNeurons[1] = entryNeurons[0] * wages[0][0][1] +
                                entryNeurons[1] * wages[0][1][1] +
                                entryNeurons[2] * wages[0][2][1] +
                                entryNeurons[3] * wages[0][3][1];
        hiddenLayerNeurons[2] = entryNeurons[0] * wages[0][0][2] +
                                entryNeurons[1] * wages[0][1][2] +
                                entryNeurons[2] * wages[0][2][2] +
                                entryNeurons[3] * wages[0][3][2];
        hiddenLayerNeurons[3] = entryNeurons[0] * wages[0][0][3] +
                                entryNeurons[1] * wages[0][1][3] +
                                entryNeurons[2] * wages[0][2][3] +
                                entryNeurons[3] * wages[0][3][3];

        /*проганяємо накопичену суму через активаційну функцію*/
        hiddenLayerNeurons[0] = 1 / (1 + exp(- 1 * hiddenLayerNeurons[0]));
        hiddenLayerNeurons[1] = 1 / (1 + exp(- 1 *hiddenLayerNeurons[1]));
        hiddenLayerNeurons[2] = 1 / (1 + exp(- 1 * hiddenLayerNeurons[2]));
        hiddenLayerNeurons[3] = 1 / (1 + exp(- 1 *hiddenLayerNeurons[3]));

        exit = hiddenLayerNeurons[0] * wages[1][0][0] + /*накоп суми для вихідного нейрона*///(один вихідний нейрон)
                   hiddenLayerNeurons[1] * wages[1][1][0] +
                   hiddenLayerNeurons[2] * wages[1][2][0] +
                   hiddenLayerNeurons[3] * wages[1][3][0];

        /*проганяємо накопичену суму через активаційну функцію*/
        exit = 1 / (1 + exp(-1 * exit));
    }

    public void CollectorOrPocrushka(double exit) {
        if (exit <= 0.4999999999999999999999)
            System.out.println("покришка");
        if (exit > 0.5 && exit <= 1.0)
            System.out.println("колектор");
    }

    public double calculateTargetFunction() { //обчислюємо "середньоквадратичну" цільову функцію
        calculateExit();
        return pow( (exit - getDesiredResult()), 2);  //при одному вихідному нейроні
    }

    public double getDesiredResult() { //бажаний результат
        if (vectorIndex < 8) {
            return 0.75; //для колектора
        } else {
            return 0.25; //для покришки
        }
    }

    //метод повернення змінюваного значення ваги до попереднього значення
    public void UNDOWageValueChange () { //повертаємо значення ваги до попереднього значення
        wages[indexLayer][indexFromNeuron][indexToNeuron] = temporaryChanged;
    }

    //метод зменшення температури Т
    public void decreaseT() {
        this.T -= this.T / log(iterationCount + 1);
    }

    //перевірка, чи покращилася цільова функція (крок 4)
    public boolean targetFunctionBecameBetter(double firstTargetFunction, double secondTargetFunction) {
        if (firstTargetFunction > secondTargetFunction) {
            return true; //покращилась
        } else {
            return false;//не покращилась
        }
    }

    //обчислення ймовірності зміни цільової функції (крок 4)
    public double calculateChangeProbabilityOfTargetFunction() {
        return exp(-1 * calculateTargetFunction() / k * T);
    }

    //вибираємо випадкове число [0, 1] (крок 5)
    public double chooseRandomNumberFrom0To1() {
        return ThreadLocalRandom.current().nextDouble(0.0, 1.0);
    }

    //тимчасова зміна вагового коеф-та (крок 3)
    public void temporaryChangeWageValue (int indexLayer, int indexFromNeuron, int indexToNeuron) {
        temporaryChanged = wages[indexLayer][indexFromNeuron][indexToNeuron];
        wages[indexLayer][indexFromNeuron][indexToNeuron] = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
    }

    //перевірка чи досягнуто достатньо мале значення цільової функції (крок "6")
    public boolean enoughSmallValueOfTargetFunctionIsReached() {
        if (calculateTargetFunction() <= 0.06249999999999) {
            return true;
        } else {
            return false;
        }
    }

    //метод, який обрирає наступну вагу, для можливого подальшого зміни значення
    public void nextWage() {
        if (indexLayer == 0) { //якщо попередня вага була в 1шому шарі
            if (indexFromNeuron == 3) { //якщо попер вага йшла від останнього нейрону
                if (indexToNeuron == 3) { //якщо попер вага йшла до останнього нейрону
                    indexFromNeuron = 0;
                    indexToNeuron = 0;
                    indexLayer++; //зміна шару
                } else { //якщо попер вага йшла НЕ до останнього нейрону
                    indexToNeuron++;
                }
            } else { //якщо попер вага йшла НЕ від останнього нейрону
                if (indexToNeuron == 3) { //якщо попер вага йшла до останнього нейрону
                    indexFromNeuron++;
                    indexToNeuron = 0;
                } else { //якщо попер вага йшла НЕ до останнього нейрону
                    indexToNeuron++;
                }
            }
        } else { //якщо попередня вага була в 2ому шарі
            if (indexFromNeuron == 3) { //якщо попер вага йшла від останнього нейрону
                if (indexToNeuron == 0) { //якщо попер вага йшла до останнього нейрону  (вихідний нейрон один)
                    indexFromNeuron = 0;
                    indexToNeuron = 0;
                    indexLayer--; //зміна шару
                }
            } else { //якщо попер вага йшла НЕ від останнього нейрону
                if (indexToNeuron == 0) { //якщо попер вага йшла до останнього нейрону  (вихідний нейрон один)
                    indexFromNeuron++;
                }
            }
        }
    }
}

