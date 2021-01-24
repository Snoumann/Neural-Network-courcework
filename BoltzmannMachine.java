package coursework;

import java.util.concurrent.ThreadLocalRandom;
import static java.lang.Math.*;

public class BoltzmannMachine {
    double[] entryNeurons = new double[4];       //масив вхідних нейронів
    double[] hiddenLayerNeurons = new double[4]; //масив нейронів прихованого шару
    double output;    //змінна, яка зберігає значення виходу нейромережі
    double T = 273.0; //штучна температура (крок 1)
    double k = 0.001; //константа

    /*вага[шар][від якого нейрона][до якого нейрона]                                   [1ий шар][4][4]   [2ий шар][4][1]*/
    double[][][] wages = new double[2][4][4]; //масив для збереження значення ваг

    //ініціалізація ваг !!! (перед початком роботи машини Больцмана)
    public void initializeAllWages() {
        for (int indexLayer = 0; indexLayer < wages.length; indexLayer++)
            for (int indexFromNeuron = 0; indexFromNeuron < wages[indexLayer].length; indexFromNeuron++)
                for (int indexToNeuron = 0; indexToNeuron < wages[indexLayer][indexFromNeuron].length; indexToNeuron++)
                    wages[indexLayer][indexFromNeuron][indexToNeuron] = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
    }

    //змінні, які відображають "координати ваги "з якою  будемо працювати
    int indexLayer = 0;       //координата шару
    int indexFromNeuron = 0;  //координати початкового нейрону
    int indexToNeuron = 0;    //координата кінцевого нейрону

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
            calculateOutput();     //обчислюємо виходи (крок 2)

            double firstTargetFunction = calculateTargetFunction(); //обчислюємо "1-шу" ціл функцію (крок 2)

            temporaryChangeWageValue(indexLayer, indexFromNeuron, indexToNeuron);//тимчасова змін ваг коеф (крок 3)
            double secondTargetFunction = calculateTargetFunction();//обчислюємо "2-гу" ціл ф-цію (перерахунок) (крок 3)

            if (targetFunctionBecameBetter(firstTargetFunction, secondTargetFunction)) { //порівнюємо 1. і 2.(крок 4)
                //ф-ція покращилася. зміна ваги відбулася. нічого не змінюємо(крок 4)
            } else if (calculateChangeProbabilityOfTargetFunction() > chooseRandomNumberFrom0To1()) { //(крок 4/5)
                    //зміна ваги відбулася за умови розподілу Больцмана. нічого не змінюємо (крок 5)
                } else
                    UNDOWageValueChange(); //повертаємо значення ваги до попереднього значення (крок 5)


            nextWage(); //готуємо наст вагу
            decreaseT();//зменшуємо температуру Т

            if (enoughSmallValueOfTargetFunctionIsReached()) {
                if (vectorIndex < 14)
                    vectorIndex++; // пред'являємо мережі наступний вхідний вектор
                else
                    vectorIndex = 0;
            }

            /*                       перевірка чи цільова ф-ція є допутимою для всіх векторів                         */
            int saveIndexOfCurrentVector = vectorIndex; //зберігаємо індекс поточного вектора

            boolean validForAllVectors = true; //змінна для фіксації допустимості

            for (vectorIndex = 0; vectorIndex < entries.length; vectorIndex++) {
                for (int indexEntryNeuron = 0; indexEntryNeuron < entryNeurons.length; indexEntryNeuron++)
                    entryNeurons[indexEntryNeuron] = entries[vectorIndex][indexEntryNeuron];

                calculateOutput();

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
            for(int indexEntryNeuron = 0; indexEntryNeuron < entryNeurons.length; indexEntryNeuron++)
                entryNeurons[indexEntryNeuron] = testingVector[raw][indexEntryNeuron];

            System.out.println(entryNeurons[0] + " " + entryNeurons[1] + " " + entryNeurons[2] + " " + entryNeurons[3]);
            calculateOutput();
            CollectorOrPocrushka(output);
        }
    }

    public void calculateOutput() { //обчислюємо виходи
        for (int indexHiddenLayerNeuron = 0; indexHiddenLayerNeuron < hiddenLayerNeurons.length; indexHiddenLayerNeuron++) /*накоп суми нейронів 1ого [0ого](прихованого)шару*/
                for (int indexStartNeuron = 0; indexStartNeuron < entryNeurons.length; indexStartNeuron++)
                    hiddenLayerNeurons[indexHiddenLayerNeuron] += entryNeurons[indexStartNeuron] * wages[0][indexStartNeuron][indexHiddenLayerNeuron];

        for (int index = 0; index < hiddenLayerNeurons.length; index++) //проганяємо накопичену суму через активаційну функцію
            hiddenLayerNeurons[index] = 1 / (1 + exp(-1 * hiddenLayerNeurons[index]));

        for (int index = 0; index < hiddenLayerNeurons.length; index++) //накоп суми для вихідного нейрона*///(один вихідний нейрон)
            output += hiddenLayerNeurons[index] * wages[1][index][0];

        output = 1 / (1 + exp(-1 * output)); //проганяємо накопичену суму через активаційну функцію
    }

    public void CollectorOrPocrushka(double output) {
        if (output <= 0.4999999999999999999999)
            System.out.println("покришка");
        if (output > 0.5 && output <= 1.0)
            System.out.println("колектор");
    }

    public double calculateTargetFunction() { //обчислюємо "середньоквадратичну" цільову функцію
        calculateOutput();
        return pow( (output - getDesiredResult()), 2);  //при одному вихідному нейроні
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
        if (calculateTargetFunction() <= 0.06249)
            return true;
        else
            return false;
    }

    //метод, який обирає наступну вагу, для можливого подальшого зміни значення
    public void nextWage() {
        if (indexLayer == 0)  //якщо попередня вага була в 1шому шарі
            if (indexFromNeuron == 3)  //якщо попер вага йшла від останнього нейрону
                if (indexToNeuron == 3) { //якщо попер вага йшла до останнього нейрону
                    indexFromNeuron = 0;
                    indexToNeuron = 0;
                    indexLayer++; //зміна шару
                } else  //якщо попер вага йшла НЕ до останнього нейрону
                    indexToNeuron++;
            else if (indexToNeuron == 3) { //якщо попер вага йшла до останнього нейрону
                indexFromNeuron++;
                indexToNeuron = 0;
            } else  //якщо попер вага йшла НЕ до останнього нейрону
                indexToNeuron++;
        else { //якщо попередня вага була в 2ому шарі
            if (indexFromNeuron == 3)  //якщо попер вага йшла від останнього нейрону
                if (indexToNeuron == 0) { //якщо попер вага йшла до останнього нейрону  (вихідний нейрон один)
                    indexFromNeuron = 0;
                    indexToNeuron = 0;
                    indexLayer--; //зміна шару
                } else if (indexToNeuron == 0)  //якщо попер вага йшла до останнього нейрону  (вихідний нейрон один)
                    indexFromNeuron++;          //якщо попер вага йшла НЕ від останнього нейрону
        }
    }
}

