package coursework;

import static java.lang.Math.*;

public class LearningVectorQuantization {
    int numberOfEpochs = 10000000; //к-сть ітерацій, яку плануємо пройти
    int indexOfEpoch = 0; //лічильний к-сті ітерацій

    double learningSpeed = 0.1; //коеф швидкості навчання

    double[][] weightMatrix = new double[4][2]; //матриця ваг
    int indexOf1stWeightArray; //індекси векторів, які використовуються в якості початкових ваг (у матриці ваг)
    int indexOf2ndWeightArray; //(не використовуються для зміни ваг)

    int currentVectorIndex; //індекс поточного вектора (з яким безпосередньо працюємо)

    double[] inputs = new double[4]; //входи
    double[] exits = new double[2]; //<виходи>

    double claster1 = 1.0;
    double claster2 = 0.0;

    double distanseTo1st;
    double distanseTo2nd;

    public void learn(double[][] learningVector) { //метод навчання нейронної мережі
        long startTime = System.currentTimeMillis(); //зміна для підрахунку часу навчання нейромережі

        setWeightMatrix(learningVector); //задаємо вагову матрицю
        currentVectorIndex = setStartVectorIndex(learningVector); //зад початк індекс вектора, з я ким будемо працювати

        do {
            indexOfEpoch++;

            distanseTo1st = 0.0;
            distanseTo2nd = 0.0;
            for (int index = 0; index < 4; index++) {  // обчислюємо відстані до кластерів
                distanseTo1st += pow(weightMatrix[index][0] - learningVector[currentVectorIndex][index], 2);
                distanseTo2nd += pow(weightMatrix[index][1] - learningVector[currentVectorIndex][index], 2);
            }

            /*визначення переможця та зміна векторів ваг*/
            if (distanseTo1st < distanseTo2nd) {
                //якщо <переможець> - 1ший кластер (змінюємо ваги в 1ій колонці матриці ваг)
                if (learningVector[currentVectorIndex][4] == claster1/*1.0*/)
                    //якщо переможець = кластеру поточного навчального вектора
                    for (int index = 0; index < 4; index++)
                        weightMatrix[index][0] = weightMatrix[index][0] + learningSpeed * (learningVector[currentVectorIndex][index] - weightMatrix[index][0]);
                else
                    //якщо переможець != кластеру поточного навчального вектора
                    for (int index = 0; index < 4; index++)
                        weightMatrix[index][0] = weightMatrix[index][0] - learningSpeed * (learningVector[currentVectorIndex][index] - weightMatrix[index][0]);
            } else if (learningVector[currentVectorIndex][4] == claster2/*0.0*/) { //якщо <переможець> - 2ий кластер
                //якщо переможець = кластеру поточного навчального вектора
                for (int index = 0; index < 4; index++)
                    weightMatrix[index][1] = weightMatrix[index][1] + learningSpeed * (learningVector[currentVectorIndex][index] - weightMatrix[index][1]);
            } else
                //якщо переможець != кластеру поточного навчального вектора
                for (int index = 0; index < 4; index++)
                    weightMatrix[index][1] = weightMatrix[index][1] - learningSpeed * (learningVector[currentVectorIndex][index] - weightMatrix[index][1]);

            updateLearnSpeed();
            changeCurrentVector(learningVector); // подаємо наступний навчальний вектор

        } while (!maxNumberOfEpochsIsReached()); //умова завершення навчання

        long endTime = System.currentTimeMillis();//фіксація часу закінчення навчання нейромережі
        long timeOfWork = endTime - startTime;
        float timeOfWorkInSeconds = timeOfWork / 1000F; //вираж в секундах

        System.out.println("\n<Квантування навчального вектора (LVQ)>");
        System.out.println("Навчання завершене за " + timeOfWorkInSeconds + " сек.");
        System.out.println("кількість ітерацій: " + numberOfEpochs);
    }

    public void updateLearnSpeed() {
        learningSpeed = 0.1 * (1 - (indexOfEpoch / numberOfEpochs));
    }

    //змінюємо вектор
    public void changeCurrentVector(double[][] learningVector) {

        if (currentVectorIndex == learningVector.length - 1) { //якщо був останній
            currentVectorIndex = 0;
            do {
                if (indexOf1stWeightArray == currentVectorIndex || indexOf2ndWeightArray == currentVectorIndex)
                    currentVectorIndex++;

            } while (!(currentVectorIndex != indexOf1stWeightArray && currentVectorIndex != indexOf2ndWeightArray));

        } else { //якщо був НЕ останній
            currentVectorIndex++;
            do {
                //якщо <cтав> останній, але відноситься до матриці ваг
                if (currentVectorIndex == learningVector.length - 1 && (indexOf1stWeightArray == currentVectorIndex || indexOf2ndWeightArray == currentVectorIndex))  //якщо <cтав> останній
                    currentVectorIndex = 0; //повертаємося на початок

                if (indexOf1stWeightArray == currentVectorIndex || indexOf2ndWeightArray == currentVectorIndex)
                    currentVectorIndex++;
            } while (!(currentVectorIndex != indexOf1stWeightArray && currentVectorIndex != indexOf2ndWeightArray));
        }
    }

    public int setStartVectorIndex(double[][] learningVector) { //задаємо індекс початкового вектора (який бере безпосередню участь у навчанні)
        int indexToReturn = 0;

        for (int index = 0; index < learningVector.length; index++)
            if (index != indexOf1stWeightArray && index != indexOf2ndWeightArray) {
                indexToReturn = index;
                break;
            }

        return indexToReturn;
    }

    public void setWeightMatrix(double[][] learningVector) { //задаємо вагову матрицю
        boolean learningVector1IsReady = false;
        boolean learningVector2IsReady = false;

        for (int raw = 0; raw < learningVector.length; raw++) {
            if (learningVector[raw][4] == 0 && !learningVector1IsReady) {
                indexOf1stWeightArray = raw;

                for (int index = 0; index < 4; index++)
                    weightMatrix[index][0] = learningVector[raw][index];

                learningVector1IsReady = true;
            }
            if (learningVector[raw][4] == 1 && !learningVector2IsReady) {
                indexOf2ndWeightArray = raw;

                for (int index = 0; index < 4; index++)
                    weightMatrix[index][1] = learningVector[raw][index];

                learningVector2IsReady = true;
            }

            if (learningVector1IsReady && learningVector2IsReady)
                break;
        }
    }

    public boolean maxNumberOfEpochsIsReached() { //метод для перевірки, чи пройдено всі ітерації
        if (indexOfEpoch == numberOfEpochs)
            return true;
        else
            return false;
    }

    public void test(double[][] testVectors) { //метод тестування нейронної мережі

        for (int index = 0; index < testVectors.length; index++) {
            distanseTo1st = 0;
            distanseTo2nd = 0;
            for (int index2 = 0; index2 < 4; index2++) {
                distanseTo1st += pow(weightMatrix[index2][0] - testVectors[index][index2], 2);
                distanseTo2nd += pow(weightMatrix[index2][1] - testVectors[index][index2], 2);
            }

            System.out.println("вектор: " + testVectors[index][0] + " " + testVectors[index][1] + " " + testVectors[index][2] + " " + testVectors[index][3]);

            if (distanseTo1st < distanseTo2nd)
                System.out.println("колектор");
            else
                System.out.println("покришка");
        }
    }
}
